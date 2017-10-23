package org.komparator.mediator.ws;

import org.komparator.mediator.ws.MediatorEndpointManager.State;
import java.util.Timer;


public class MediatorApp {


	public static void main(String[] args) throws Exception {
        final int TIME = 5000;

		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;

		// Create server implementation object, according to options
		MediatorEndpointManager endpoint = null;
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(wsURL);
		}
        else if (args.length >= 3) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
            if (wsURL.contains("1")){
                endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL);
                endpoint.setState(State.MAIN);
            }
            else if (wsURL.contains("2")){
                endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL);
                endpoint.setState(State.SECONDARY);
            }
			endpoint.setVerbose(true);
            if (endpoint.getState() == State.MAIN){
                System.out.println("This is the Primary Mediator");
            }
            else{
                System.out.println("This is the Secondary Mediator");
            }
		}

		try {
			endpoint.start();

            Timer timer = new Timer(true);

            LifeProof lifeProof = new LifeProof(endpoint);

            timer.schedule(lifeProof, 0, TIME);

			endpoint.awaitConnections();
            timer.cancel();
            timer.purge();
		} finally {
			endpoint.stop();
		}

	}

}
