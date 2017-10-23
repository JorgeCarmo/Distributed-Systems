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
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPException;

import java.lang.RuntimeException;

public class CheckTokenHandler implements SOAPHandler<SOAPMessageContext> {

    private List<String> randomStrings = new ArrayList<String>();

    @Override
	public Set<QName> getHeaders() {
		return null;
	}

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        System.out.println("CheckTokenHandler: Handling message.");
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!outbound){
            try{
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                // check header
                if (sh == null) {
                    throw new RuntimeException("Header not found");
                }

                // get first header element
                Name name = se.createName("Header", "h", "http://header");
                Iterator it = sh.getChildElements(name);

                // check header element
                if (!it.hasNext()) {
                    System.out.println("Header element not found.");
                    return true;
                }
                SOAPElement element = (SOAPElement) it.next();

                String randomString = element.getValue();


                if(randomStrings.contains(randomString)){
                    System.out.println("Already received this message");
                    throw new RuntimeException("Already received this message");
                }
                else{
                    randomStrings.add(randomString);
                    System.out.println("CheckTokenHandler: Token checked.");
                }

            } catch(SOAPException e){
                System.out.println("Soap exception");
            }

        }
        else{
            System.out.println("CheckTokenHandler: Passing message.");
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
