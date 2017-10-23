package org.komparator.security.handler;

import java.util.Set;

import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPException;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

public class AddTokenHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
	public Set<QName> getHeaders() {
		return null;
	}

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        System.out.println("AddTokenHandler: Handling message.");
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound){
            try {
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();


                SOAPHeader sh = se.getHeader();
				if (sh == null){
					sh = se.addHeader();
                }

                Name name = se.createName("Header", "h", "http://header");
                SOAPHeaderElement element = sh.addHeaderElement(name);

                SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

                byte tokenElement[] = new byte[32];
    		    random.nextBytes(tokenElement);

                String randomToken = printHexBinary(tokenElement);

                element.addTextNode(randomToken);
                System.out.println("AddTokenHandler: Successfully added Token");

            } catch (SOAPException e){
                System.out.println("Ups");
            } catch (NoSuchAlgorithmException e){
                System.out.println("Ups");
            }
        }
        else{
            System.out.println("AddTokenHandler: Passing Message");
        }

        return true;
    }

    //FIXME

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
