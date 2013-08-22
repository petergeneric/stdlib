package com.mediasmiths.std.crypto.signing;

import java.io.*;
import java.security.*;
import java.util.*;

import java.security.cert.*;

import javax.xml.parsers.DocumentBuilderFactory;

import com.mediasmiths.std.crypto.keystore.Keystore;
import com.mediasmiths.std.net.IpHelper;

import org.apache.log4j.Logger;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.*;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/**
 * An XML Document signing class
 * 
 * 
 */
public class XMLDocumentSigner {
	private static final Logger log = Logger.getLogger(XMLDocumentSigner.class);

	/**
	 * The sequence number; counts the number of signatures produced since this application started.
	 */
	private static int seq = 0;

	/**
	 * A pseudorandom id assigned to this XML Document Signer. <br />
	 * When combined with the IP+date this has a fair chance of being unique
	 */
	private static double instanceId = Math.random();

	static {
		log.debug("[XMLDocumentSigning] <init> Initialising apache xml security");
		org.apache.xml.security.Init.init();
		log.debug("[XMLDocumentSigning] <init> Apache xml security initialised.");

		if (Security.getProvider("BC") == null) {
			log.info("[XMLDocumentSigner] Loading Bouncy Castle Provider");
			Security.addProvider(new BouncyCastleProvider());
			log.debug("[XMLDocumentSigner] Bouncy Castle Provider loaded");
		}

		log.info("[XMLDocumentSigner] initialised. instanceId is " + instanceId);
	}

	/**
	 * The XML Document this object was constructed with; this is the object upon which all manipulations are performed
	 */
	private final Document doc;


	/**
	 * Creates a new XML Document Signer, using an XML document represented by the given String.
	 * 
	 * @param xml The XML document as a String
	 * @throws Exception If the xml document could not be loaded
	 */
	public XMLDocumentSigner(String xml) throws Exception {
		assert (xml != null) : "XML string must be provided";
		assert (xml.length() != 0) : "XML string must not be empty";

		try {
			log.debug("[XmlDocSigner] <ctor> Constructing document builder factory");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			// XML Signatures use namespaces
			dbf.setNamespaceAware(true);

			log.debug("[XmlDocSigner] <ctor> Reading document");
			InputSource is = new InputSource(new StringReader(xml));
			Document xmlDoc = dbf.newDocumentBuilder().parse(is);

			this.doc = xmlDoc;
		}
		catch (Exception e) {
			log.error("[XMLDocumentSigning] {ctor} Could not load XML document: " + e.getMessage(), e);
			throw new Exception("Could not load the XML document: " + e.getMessage(), e);
		}
	}


	/**
	 * Creates a new XML Document Signer, using the contents of a File as the XML Document
	 * 
	 * @param xmlFile The XML document file
	 * @throws Exception If the xml document could not be loaded
	 */
	public XMLDocumentSigner(File xmlFile) throws Exception {
		assert (xmlFile != null) : "File must be provided";
		assert (xmlFile.exists()) : "XML file must exist";

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			// XML Signature needs to be namespace aware
			dbf.setNamespaceAware(true);

			Document xmlDoc = dbf.newDocumentBuilder().parse(new FileInputStream(xmlFile));

			this.doc = xmlDoc;
		}
		catch (Exception e) {
			log.error("[XMLDocumentSigning] {ctor} Could not load XML document: " + e.getMessage(), e);
			throw new Exception("Could not load the XML document: " + e.getMessage(), e);
		}
	}


	/**
	 * Creates a new XML Document Signer out of a given Document; <strong>the caller guarantees the Document can handle namespaces</strong>
	 * 
	 * @param d the xml document
	 */
	public XMLDocumentSigner(Document d) {
		assert (d != null) : "XML Document must be provided";

		this.doc = d;
	}


	/**
	 * Given a keypair, signs this document and adds the public key to the signature tag
	 * 
	 * @param keys KeyPair The public/private keypair
	 * @return Document The signed document. The internal document is also signed.
	 */
	public Document sign(String machineId, KeyPair keys) {
		return sign(machineId, keys.getPublic(), keys.getPrivate());
	}


	/**
	 * Helper method; retrieves a named public/private keypair from a KeyStore.
	 * 
	 * @param ks The KeyStore
	 * @param alias The name of the key
	 * @param passphrase The passphrase (if necessary)
	 * @return The KeyPair
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableEntryException
	 * @throws KeyStoreException
	 */
	public static KeyPair getKeypair(KeyStore ks, String alias, char[] passphrase)
			throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
		KeyStore.Entry e = ks.getEntry(alias, new KeyStore.PasswordProtection(passphrase));

		KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) e;

		PublicKey pubkey = pkEntry.getCertificate().getPublicKey();
		PrivateKey privkey = pkEntry.getPrivateKey();

		return new KeyPair(pubkey, privkey);
	}


	/**
	 * Given a pair of keys, signs this document and adds the public key to the signature tag
	 * 
	 * @param publicKey PublicKey The public key
	 * @param privateKey PrivateKey The private key
	 * @return Document The signed document. The internal document is also signed.
	 */
	public Document sign(String machineId, PublicKey publicKey, PrivateKey privateKey) {
		try {
			String baseUri = "";
			// Create an XML Signature object from the document, BaseURI and signature algorithm
			XMLSignature signature = new XMLSignature(doc, baseUri, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512);

			// Append the signature to the end of the children of the root element
			doc.getFirstChild().appendChild(signature.getElement());

			{
				// create the transforms object for the Document/Reference
				Transforms transforms = new Transforms(doc);

				transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
				transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);
				// Add the above Document/Reference
				signature.addDocument(baseUri, transforms, Constants.ALGO_ID_DIGEST_SHA1);
			}

			// Give this signature a unique Id
			String signatureId;
			signatureId = "id:" + machineId + " ";
			signatureId += "ip:" + IpHelper.getLocalIp() + " ";
			signatureId += "inst:" + instanceId + " ";
			signatureId += "seq:" + (++seq) + " ";
			signatureId += "now:" + System.currentTimeMillis();
			signature.setId(signatureId);

			// Add the public key to the signature to make it easier to identify who signed the document
			signature.addKeyInfo(publicKey);

			// Now sign the document
			signature.sign(privateKey);

			log.info("[XMLDocumentSigner] {sign} Issued signature " + signatureId);

			return signature.getDocument();
		}
		catch (XMLSecurityException e) {
			log.error("[XMLDocumentSigning] {sign} Error signing document: " + e.getMessage(), e);
			return null;
		}
	}


	/**
	 * Saves the Document, as manipulated by this class, as a File
	 * 
	 * @param dest The destination file
	 * @return <code>true</code> if the document was saved, otherwise <code>false</code>
	 */
	public boolean save(File dest) {
		if (dest.exists()) {
			dest.delete();
		}

		try {
			XMLUtils.outputDOMc14nWithComments(doc, new FileOutputStream(dest));

			return true;
		}
		catch (FileNotFoundException e) {
			log.error("[XMLDocumentSigner] {save} Couldn't save to file " + dest);
			return false;
		}
	}


	/**
	 * Removes the signature tag from this document if they are present
	 * 
	 * @return Document A docuemnt without the signature tag. the internal document is also modified
	 */
	public Document stripSignature() {
		if (hasSignature()) {
			Element sig = (Element) doc.getLastChild().getLastChild();

			sig.getParentNode().removeChild(sig);
		}
		else {
			log.warn("[XMLDocumentSigner] {stripSignature} Signature not found: nothing stripped");
		}

		return doc;
	}


	/**
	 * Determines whether this document has a signature tag
	 * 
	 * @return boolean True if the document has a signature tag, otherwise false
	 */
	public boolean hasSignature() {
		Node n = doc.getLastChild().getLastChild();

		if (n instanceof Element) {
			Element e = (Element) n;

			// If the namespace is the signature namespace and the element is a Signature element, it's an xml sig
			if (e.getNamespaceURI().equals(Constants.SignatureSpecNS) && e.getLocalName().equals("Signature")) {
				return true;
			}
		}

		// No signature was found
		return false;
	}


	/**
	 * Produces an XML String representation of the document
	 * 
	 * @return String an XML String representation of the document
	 */
	@Override
	public String toString() {
		// Holds the output temporarily so we can get a String out of it
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		// Output the XML to the byte array stream
		XMLUtils.outputDOMc14nWithComments(doc, bos);

		// Now convert the output stream to a string, giving an xml string
		return bos.toString();
	}


	/**
	 * Validates the document using the public keys of the <code>Certificate</code>s in <code>truststore</code>
	 * 
	 * @param truststore The trust store
	 * @return True if the document validates, otherwise false
	 * @throws CertificateException If there was a problem with the certificate
	 * @throws FileNotFoundException If the truststore's file could't be loaded
	 * @throws IOException If a generic IO exception occurred
	 * @throws KeyStoreException If a problem occurred during keystore loading
	 * @throws NoSuchAlgorithmException If the keystore couldn't be loaded
	 */
	public boolean validate(Keystore truststore) throws CertificateException, FileNotFoundException, IOException,
	KeyStoreException, NoSuchAlgorithmException {
		FileInputStream tsis = null;
		try {
			tsis = new FileInputStream(truststore.file);
			KeyStore ts = KeyStore.getInstance(truststore.getType());
			ts.load(tsis, truststore.password.toCharArray());

			List<PublicKey> keys = new ArrayList<PublicKey>();

			Enumeration<String> aliases = ts.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				PublicKey pk = ts.getCertificate(alias).getPublicKey();

				if (pk != null) {
					keys.add(pk);
				}
			}

			PublicKey[] keyarray = new PublicKey[keys.size()];
			for (int i = 0; i < keyarray.length; i++) {
				keyarray[i] = keys.get(i);
			}

			return validate(keyarray);
		}
		finally {
			try {
				if (tsis != null)
					tsis.close();
			}
			catch (IOException e) {
				log.warn("[XMLDocumentSigner] {validate} Error closing truststore file: " + e.getMessage(), e);
			}
		}
	}


	/**
	 * Validates the signature tag on the document
	 * 
	 * @param allowedKeys the <code>PublicKey</code>s whose signatures will be considered valid
	 * @return <code>true</code> if the document was signed by one of the keys listed & the signature is valid, otherwise <code>false</code>
	 */
	public boolean validate(PublicKey... allowedKeys) {
		Element sigElement = (Element) doc.getLastChild().getLastChild();

		try {
			XMLSignature signature = new XMLSignature(sigElement, "");

			for (PublicKey element : allowedKeys) {
				try {
					if (element != null) {
						boolean validationResult = signature.checkSignatureValue(element);

						if (validationResult) {
							return true;
						}
					}
				}
				catch (XMLSignatureException e) {
					log.error("[XMLDocumentSigning] {validate} Error with signature/key combo: " + e.getMessage(), e);
				}
			}
		}
		catch (XMLSecurityException ex) {
			log.error("[XMLDocumentSigning] {validate} Error loading signature from document");
			return false;
		}

		// No keys matched
		return false;
	}
}
