package org.komparator.security.handler;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPException;

import org.komparator.security.Manager;
import org.komparator.security.CryptoUtil;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import java.lang.RuntimeException;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

/**
 * This SOAPHandler ciphers the contents of outbound and deciphers the
 * contents of inbound messages.
 */
public class CorrectCipherHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String CAUrl = "http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca";

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
      System.out.println("CorrectCipherHandler: Handling message.");
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound){
            try{
                SOAPMessage msg = smc.getMessage();
			    SOAPPart sp = msg.getSOAPPart();
			    SOAPEnvelope se = sp.getEnvelope();
			    SOAPBody sb = se.getBody();

                Name name = se.createName("buyCart", "ns2", "http://ws.mediator.komparator.org/");

                QName service = (QName) smc.get(MessageContext.WSDL_SERVICE);
                QName operation = (QName) smc.get(MessageContext.WSDL_OPERATION);


                if(!operation.getLocalPart().equals("buyCart")){
                    return true;
                }

                NodeList children = sb.getFirstChild().getChildNodes();

                for(int i = 0; i < children.getLength(); i++){
                    Node argument = children.item(i);
                    if(argument.getNodeName().equals("creditCardNr")){
                        String secretArgument = argument.getTextContent();

                        //getCertificate
                        CAClient client = new CAClient(CAUrl);
                        Manager manager = Manager.getInstance();
                        String certificateString = client.getCertificate(manager.getCertificate());


                        if (certificateString == null) {
                            System.out.println(manager.getCertificate());
                            System.out.println("Certificate not found!");
                        }

                        Certificate caCertificate = CryptoUtil.getX509CertificateFromResource(manager.getCACertificate());

                        byte[] bytes = certificateString.getBytes(StandardCharsets.UTF_8);
                        InputStream in = new ByteArrayInputStream(bytes);
                        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                        Certificate cert = certFactory.generateCertificate(in);

        		        boolean firstResult = CryptoUtil.verifySignedCertificate(cert, caCertificate);

                        if(!firstResult){
                            System.out.println(manager.getCertificateName());
                            System.out.println("Certificate is not from CA");
                            throw new RuntimeException("Invalid Certificate");
                        }

                        PublicKey publicKey = cert.getPublicKey();

                        //cipher
                        byte[] secretBytes = secretArgument.getBytes();

                        byte[] cipheredBytes = CryptoUtil.asymCipher(secretBytes, publicKey);

                        String encodedSecret = printBase64Binary(cipheredBytes);

                        argument.setTextContent(encodedSecret);
                        msg.saveChanges();
                    }
                }

            } catch (Exception e){
                System.out.println("Exception caught");
            }
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

    private void logToSystemOut(SOAPMessageContext smc) {
    }
}
