package com.peterphi.std.guice.web.rest.pagewriter;

import com.peterphi.std.guice.web.rest.util.BootstrapStaticResources;
import org.apache.commons.lang.StringEscapeUtils;

public class TwitterBootstrapPageWriter
{
	public void writeHTML(StringBuilder sb)
	{
		writePrologue(sb);
		{
			writeBodyContent(sb);
		}
		writeEpilogue(sb);
	}

	protected void writePrologue(StringBuilder sb)
	{
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		sb.append("<head>\n");
		sb.append("<title>").append(escape(getTitle())).append("</title>\n");
		sb.append("<meta charset=\"utf-8\" />\n");
		sb.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\" />\n");
		writeBootstrapCSS(sb);
		writeCustomHeadContent(sb);
		sb.append("</head>\n\n");
		sb.append("<body id='top'>\n");
	}

	protected void writeEpilogue(StringBuilder sb)
	{
		sb.append("</body>\n");
		sb.append("</html>\n");
	}

	protected void writeBodyContent(StringBuilder sb)
	{
	}

	protected String escape(String text)
	{
		return StringEscapeUtils.escapeHtml(text);
	}

	protected String getTitle()
	{
		return "";
	}

	protected void writeCustomHeadContent(StringBuilder sb)
	{

	}

	protected void writeBootstrapCSS(StringBuilder sb)
	{
		sb.append("\n<style>\n");
		BootstrapStaticResources.get().appendCSS(sb);
		sb.append("\n</style>\n");
	}
}
