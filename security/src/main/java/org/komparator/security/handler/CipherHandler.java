package org.komparator.security.handler;

import java.util.Set;
import java.util.Iterator;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.xml.bind.DatatypeConverter;

import java.io.*;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.security.cert.Certificate;
import java.security.PublicKey;
import java.security.PrivateKey;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import org.komparator.security.Manager;

import org.komparator.security.CryptoUtil;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;


/**
 * This SOAPHandler ciphers the contents of outbound and deciphers the
 * contents of inbound messages.
 */
public class CipherHandler implements SOAPHandler<SOAPMessageContext> {
	
    private static final String CAUrl = "http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca";

	final static String KEYSTORE = "A61_Mediator.jks";
	final static String KEYSTORE_PASSWORD = "Rf5rbs";

	final static String KEY_ALIAS = "A61_Mediator";
	final static String KEY_PASSWORD = "Rf5rbs";

	//
	// Handler interface implementation
	//

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	
	
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		CryptoUtil c = new CryptoUtil();
		System.out.println("CipherHandler: securing cc");
		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		Manager manager = Manager.getInstance();
		try {
			if (outboundElement.booleanValue()) {
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = se.getBody();
				NodeList children = sb.getFirstChild().getChildNodes();
				/*
				Name name = se.createName("creditCardNr", "http://ws.mediator.komparator.org/", "ns2");
				Iterator it = sb.getChildElements(name);
				System.out.println("----------------");
				System.out.println(name);
				if (!it.hasNext()) {
					System.out.println("No cc found");
					return true;
				}
				System.out.println("encrypting cc");
				SOAPBodyElement element = (SOAPBodyElement) it.next();
				String content = element.getTextContent();*/
				for (int i = 0; i < children.getLength(); i++) {
					Node element = children.item(i);
					if (element.getNodeName().equals("creditCardNr")) {
						String content = element.getTextContent();
						
						
						System.out.println("Encontrou CC");
						
				
						ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
						byteOut.write(content.getBytes());
						/*  Connect ao CA */
						CAClient client = new CAClient(CAUrl);
						/* Obter Certificado */
						
						String certificateString = client.getCertificate(manager.getCertificateName());
						if (certificateString == null) {
							System.out.println(manager.getCertificateName());
							System.out.println("Certificate not found!");
						}	
						
						Certificate ca_cert = c.getX509CertificateFromPEMString(certificateString);
						
						PublicKey pkey = c.getPublicKeyFromCertificate(ca_cert);
						byte[] cipheredContent = c.asymCipher(byteOut.toByteArray(), pkey);
						String encodedContent = DatatypeConverter.printBase64Binary(cipheredContent);
						System.out.println("CC Found - Encrypted = " + cipheredContent);
						element.setTextContent(encodedContent);
						msg.saveChanges();
					}
				}
			} else {
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = se.getBody();
				NodeList children = sb.getFirstChild().getChildNodes();
				/*
				Name name = se.createName("creditCardNr");
				Iterator it = sb.getChildElements(name);
				if (!it.hasNext()) {
					System.out.println("no cc found");
					return true;
				}
				System.out.println("decrypting cc");
				SOAPBodyElement element = (SOAPBodyElement) it.next();
				String content = element.getTextContent();*/
				
				
				for (int i = 0; i < children.getLength(); i++) {
					Node element = children.item(i);
					if (element.getNodeName().equals("creditCardNr")) {
						String content = element.getTextContent();
						
						
						System.out.println("Encontrou CC");
						ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
						byteOut.write(content.getBytes());
						System.out.println("phase 1");
						/* Getting private key */
						PrivateKey privateKey = c.getPrivateKeyFromKeyStoreResource(manager.getKeystore(),
				manager.getKeystorePassword().toCharArray(), manager.getKeyAlias(), manager.getKeyPassword().toCharArray());

						System.out.println("phase 2");
						byte[] decipheredContent = c.asymDecipher(byteOut.toByteArray(), privateKey);System.out.println("phase 3");
						
						String decodedContent = DatatypeConverter.printBase64Binary(decipheredContent);
						System.out.println("CC Found - Decrypted = " + decodedContent);
						element.setTextContent(decodedContent);
						msg.saveChanges();
					}
				}
			}
		} catch (Exception e) {
			System.out.print("Caught exception in handleMessage: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		}
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		logToSystemOut(smc);
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}

	/** Date formatter used for outputting timestamps in ISO 8601 format */
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");


	private void logToSystemOut(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		// print current timestamp
		System.out.print("[");
		System.out.print(dateFormatter.format(new Date()));
		System.out.print("] ");

		System.out.print("intercepted ");
		if (outbound){
			System.out.print("OUTbound");
        }
		else{
			System.out.print(" INbound");
        }
		System.out.println(" SOAP message:");

		SOAPMessage message = smc.getMessage();
		try {
			message.writeTo(System.out);
			System.out.println(); // add a newline after message

		} catch (SOAPException se) {
			System.out.print("Ignoring SOAPException in handler: ");
			System.out.println(se);
		} catch (IOException ioe) {
			System.out.print("Ignoring IOException in handler: ");
			System.out.println(ioe);
		}
	}
}
