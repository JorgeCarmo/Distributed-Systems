package org.komparator.security.handler;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.komparator.security.CryptoUtil;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.ws.handler.MessageContext;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.transform.stream.StreamResult;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.lang.RuntimeException;

import org.komparator.security.Manager;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

public class CheckSigningHandler implements SOAPHandler<SOAPMessageContext> {

    /** Digital signature algorithm. */
	private static final String SIGNATURE_ALGO = "SHA256withRSA";

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
        System.out.println("CheckSigningHandler: Handling message.");
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!outbound){
            try {
                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                // get first header element
                Name name = se.createName("Signature", "s", "http://signature");
                Iterator it = sh.getChildElements(name);
                // check header element
                if (!it.hasNext()) {
                    System.out.println("Header element not found.");
                    return true;
                }
                SOAPElement element = (SOAPElement) it.next();

                // get signature
                String signature = element.getValue();

                sh.removeChild(element);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                msg.writeTo(bos);
                byte[] plainBytes = bos.toByteArray();
                Manager manager = Manager.getInstance();


                CAClient client = new CAClient(CAUrl);

                String certificateString = client.getCertificate(manager.getCertificateName());


                if (certificateString == null) {
                    System.out.println(manager.getCertificateName());
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

                byte[] digitalSignature = parseBase64Binary(signature);

                boolean secondResult = CryptoUtil.verifyDigitalSignature(SIGNATURE_ALGO, publicKey, plainBytes, digitalSignature);

                if(!secondResult){
                    System.out.println(manager.getCertificateName());
                    System.out.println("Invalid Certificate");
                    throw new RuntimeException("Invalid Certificate");
                }

                System.out.println("CheckSigningHandler: Sucess");

            } catch (SOAPException e){
                System.out.println("SOAPException");
            } catch (CAClientException e){
                System.out.println("Certificate not Found");
            } catch (IOException e){
                System.out.println("IO");
            } catch (CertificateException e){
                System.out.println("CertificateException");
            }
        }
        else{
            System.out.println("CheckSigningHandler: Passing message");
        }

        return true;

	}

    private static void writeFile(String fileName, String result) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

		bw.write(result);
		bw.close();
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
