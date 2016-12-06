package com.peterphi.servicemanager.hostagent.cmdline;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NginxSiteGenerator
{
	public static final boolean ENABLE_HSTS = false;

	/**
	 * Per https://cipherli.st/ 2016-11-30
	 */
	private static final String SSL_CONFIG = "\t# Per https://cipherli.st/ 2016-11-30\n" +
	                                         "\tssl_protocols TLSv1 TLSv1.1 TLSv1.2;\n" +
	                                         "\tssl_prefer_server_ciphers on;\n" +
	                                         "\tssl_ciphers \"EECDH+AESGCM:EDH+AESGCM:AES256+EECDH:AES256+EDH\";\n" +
	                                         "\tssl_ecdh_curve secp384r1; # Requires nginx >= 1.1.0\n" +
	                                         "\tssl_session_cache shared:SSL:10m;\n" +
	                                         "\tssl_session_tickets off; # Requires nginx >= 1.5.9\n" +
	                                         "\tssl_stapling on; # Requires nginx >= 1.3.7\n" +
	                                         "\tssl_stapling_verify on; # Requires nginx => 1.3.7\n" +
	                                         "\tadd_header X-Frame-Options DENY;\n" +
	                                         "\tadd_header X-Content-Type-Options nosniff;\n";

	private static final String HSTS_CONFIG = "\tadd_header Strict-Transport-Security \"max-age=63072000; includeSubDomains; preload\";\n";

	private static final String EXPLICIT_DNS_CONFIG = "\tresolver 8.8.8.8 8.8.4.4 valid=300s;\n" + "\tresolver_timeout 5s;\n";

	private static final String LOCATION_CONFIG = "\t\tproxy_cache off;\n" +
	                                              "\t\tproxy_set_header Host $host;\n" +
	                                              "\t\tproxy_set_header X-Real-IP $remote_addr;\n" +
	                                              "\t\tproxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" +
	                                              "\t\tproxy_set_header X-Forwarded-Proto https;\n" +
	                                              "\t\tproxy_http_version 1.1;\n" +
	                                              "\t\tproxy_set_header Connection \"\";\n" +
	                                              "\t\tproxy_cache_bypass $cookie_session;\n" +
	                                              "\t\tproxy_no_cache $cookie_session;\n" +
	                                              "\t\tproxy_buffers 32 4k;\n" +
	                                              "\t\tclient_max_body_size 1G;\n";

	private static class Resource
	{
		public final String prefix;
		public final String host;
		public final int port;


		public Resource(final String prefix, final String host, final int port)
		{
			this.prefix = prefix;
			this.host = host;
			this.port = port;
		}


		public void append(StringBuilder sb)
		{
			sb.append("\tlocation " + prefix + " {\n");
			sb.append("\t\tproxy_pass http://" + host + ":" + port + prefix + ";\n\n");
			sb.append(LOCATION_CONFIG);
			sb.append("\t}\n");
		}
	}

	private static class Server
	{
		public final String name;
		public final int port;
		public boolean defaultServer = false;

		public File sslCert;
		public File sslKey;

		public final List<Resource> resources = new ArrayList<>();


		public Server(final String name, final int port, final File sslCert, final File sslKey)
		{
			this.name = name;
			this.port = port;
			this.sslCert = sslCert;
			this.sslKey = sslKey;
		}


		public Server resource(Resource resource)
		{
			if (resource != null)
				resources.add(resource);

			return this;
		}


		public void append(StringBuilder sb)
		{
			sb.append("server {\n");
			sb.append("\tlisten 0.0.0.0:" + port + " ssl http2" + (defaultServer ? " default_server" : "") + ";\n\n");
			sb.append("\tserver_name " + name + ";\n\n");
			sb.append("\tssl on;\n");
			sb.append("\tssl_certificate " + sslCert.getAbsolutePath() + ";\n");
			sb.append("\tssl_certificate_key " + sslKey.getAbsolutePath() + ";\n\n");
			sb.append(SSL_CONFIG);

			if (ENABLE_HSTS)
				sb.append(HSTS_CONFIG);

			// Immediately drop connections to unexpected webapps
			sb.append("\n");
			sb.append("\tlocation / {\n");
			sb.append("\t\treturn 444;\n");
			sb.append("\t}\n");
			sb.append("\n");

			//
			for (Resource resource : resources)
			{
				sb.append("\n");
				resource.append(sb);
				sb.append("\n");
			}

			sb.append("}\n");
		}
	}


	public String render(final String name, final File sslCert, final File sslKey, final Map<File, Integer> foldersAndPorts)
	{
		Server server = new Server(name, 443, sslCert, sslKey);

		for (Map.Entry<File, Integer> webappFolder : foldersAndPorts.entrySet())
		{
			final File folder = webappFolder.getKey();
			final String host = "127.0.0.1";
			final int port = webappFolder.getValue();

			for (String webapp : getWebapps(folder))
			{
				final String prefix = "/" + webapp;

				server.resource(new Resource(prefix, host, port));
			}
		}

		// Write the config out
		StringBuilder sb = new StringBuilder();
		server.append(sb);

		return sb.toString();
	}


	private static Set<String> getWebapps(final File folder)
	{
		if (!folder.exists() || !folder.isDirectory())
			return Collections.emptySet();

		final Set<String> webapps = new HashSet<>();

		for (File file : folder.listFiles())
		{
			final String webapp;

			if (file.getName().startsWith("."))
			{
				continue;
			}
			else if (file.isFile() && file.getName().endsWith(".war"))
			{
				webapp = file.getName().replace(".war", "");
			}
			else if (file.isDirectory())
			{
				webapp = file.getName();
			}
			else
			{
				continue; // this file is not a webapp file
			}

			// Strip anything after the # (webapp version number)
			if (webapp.contains("#"))
			{
				final String[] parts = webapp.split("#");

				webapps.add(parts[0]);
			}
			else
			{
				webapps.add(webapp);
			}
		}

		// Special-case the ROOT webapp name, it maps to the root of the container
		if (webapps.contains("ROOT"))
		{
			webapps.remove("ROOT");
			webapps.add("");
		}

		return webapps;
	}


	private static Map<File, Integer> getFoldersAndPorts(final int skipArgs, final String[] args)
	{
		Map<File, Integer> map = new HashMap<>();

		for (int i = skipArgs; i < args.length; i += 2)
		{
			final String folderStr = args[i];
			final String portStr = args[i + 1];

			final File folder = new File(folderStr);
			final int port = Integer.parseInt(portStr);

			map.put(folder, port);
		}

		return map;
	}


	/**
	 * Entry point for initial generation, called from the commandline
	 *
	 * @param args
	 *
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		final String name = args[0];
		final File sslCert = new File(args[1]);
		final File sslKey = new File(args[2]);
		final Map<File, Integer> foldersAndPorts = getFoldersAndPorts(3, args);

		NginxSiteGenerator generator = new NginxSiteGenerator();

		final String config = generator.render(name, sslCert, sslKey, foldersAndPorts);

		System.out.println(config);
	}
}
