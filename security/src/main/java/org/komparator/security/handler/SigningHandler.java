package org.komparator.security.handler;

import java.util.Set;

import java.io.ByteArrayOutputStream;

import java.security.PrivateKey;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;

import org.komparator.security.Manager;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class SigningHandler implements SOAPHandler<SOAPMessageContext> {

    /** Digital signature algorithm. */
	private static final String SIGNATURE_ALGO = "SHA256withRSA";

    @Override
	public Set<QName> getHeaders() {
		return null;
	}

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        System.out.println("SigningHandler: Handling message.");
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound){
            try {
                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                msg.writeTo(bos);
                byte[] plainBytes = bos.toByteArray();

                Manager manager = Manager.getInstance();

                PrivateKey privateKey = CryptoUtil.getPrivateKeyFromKeyStoreResource(manager.getKeystore(),
				manager.getKeystorePassword().toCharArray(), manager.getKeyAlias(), manager.getKeyPassword().toCharArray());


                byte[] signature = CryptoUtil.makeDigitalSignature(SIGNATURE_ALGO, privateKey, plainBytes);

                String signatureString = printBase64Binary(signature);

				Name name = se.createName("Signature", "s", "http://signature");
                SOAPHeaderElement element = sh.addHeaderElement(name);
				element.addTextNode(signatureString);

                System.out.println("SigningHandler: Signature added successfully");

            } catch (Exception e) {
			    System.out.print("Caught exception in handleMessage: ");
			    System.out.println(e);
			    System.out.println("Continue normal processing...");
		    }
        }
        else{
            System.out.println("SigningHandler: Passing message.");
        }
        return true;
    }

    /** The handleFault method is invoked for fault message processing. */
    @Override
    public boolean handleFault(SOAPMessageContext smc) {
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
