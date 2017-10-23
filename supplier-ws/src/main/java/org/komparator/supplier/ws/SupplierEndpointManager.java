package org.komparator.supplier.ws;

import java.io.IOException;

import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import org.komparator.security.Manager;

/** End point manager */
public class SupplierEndpointManager {

	/** Web Service location to publish */
	private String wsURL = null;

	/** UDDI naming server location */
	private String uddiURL = null;

	/** Web Service name */
	private String wsName = null;

    private String name = "A61_Supplier";

    private final static String MEDIATOR = "A61_Mediator";

	/** Get Web Service UDDI publication name */
	public String getWsName() {
		return wsName;
	}

	/** UDDI Naming instance for contacting UDDI server */
	private UDDINaming uddiNaming = null;

	/** Port implementation */
	private SupplierPortImpl portImpl = new SupplierPortImpl(this);

	/** Obtain Port implementation */
	public SupplierPortType getPort() {
		return portImpl;
	}

	/** Web Service end point */
	private Endpoint endpoint = null;

	/** output option **/
	private boolean verbose = true;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL */
	public SupplierEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;
        Manager manager = Manager.getInstance();
        manager.setKeystore(name + ".jks");
        manager.setKeyAlias(name.toLowerCase());
        manager.setCertificate(MEDIATOR);
	}

	public SupplierEndpointManager(String uddiURL, String wsName, String wsURL) {
		if (uddiURL == null)
			throw new NullPointerException("UDDI URL cannot be null!");
		if (wsName == null)
			throw new NullPointerException("Web Service Name cannot be null!");
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
        Manager manager = Manager.getInstance();
        manager.setKeystore(wsName + ".jks");
        manager.setKeyAlias(wsName.toLowerCase());
        manager.setCertificate(MEDIATOR);
	}
	/* end point management */

	public void start() throws Exception {
		try {
			// publish end point
			endpoint = Endpoint.create(this.portImpl);
			if (verbose) {
				System.out.printf("Starting %s%n", wsURL);
			}
			endpoint.publish(wsURL);
		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
		publishToUDDI();
	}

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) {
				System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
			}
		}
	}


	public void stop() throws Exception {
		try {
			if (endpoint != null) {
				// stop end point
				endpoint.stop();
				if (verbose) {
					System.out.printf("Stopped %s%n", wsURL);
				}
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}
		try {
			if (this.uddiNaming != null) {
			// delete from UDDI
			this.uddiNaming.unbind(wsName);
			System.out.printf("Deleted '%s' from UDDI%n", wsName);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when deleting: %s%n", e);
		}
		this.portImpl = null;
		unpublishFromUDDI();
	}

	/* UDDI */

	void publishToUDDI() throws Exception {
		try {
			// publish to UDDI
			if (uddiURL != null) {
				if (verbose) {
					System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
				}
				uddiNaming = new UDDINaming(uddiURL);
				uddiNaming.rebind(wsName, wsURL);
			}
		} catch (Exception e) {
			uddiNaming = null;
			if (verbose) {
				System.out.printf("Caught exception when binding to UDDI: %s%n", e);
			}
			throw e;
		}
	}


	void unpublishFromUDDI() {
		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(wsName);
				if (verbose) {
					System.out.printf("Unpublished '%s' from UDDI%n", wsName);
				}
				uddiNaming = null;
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when unbinding: %s%n", e);
			}
		}
	}

}
