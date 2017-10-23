package org.komparator.supplier.ws.cli;

import org.komparator.supplier.ws.ProductView;

/** Main class that starts the Supplier Web Service client. */
public class SupplierClientApp {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
          		System.err.println("Argument(s) missing!");
            		System.err.println("Usage: java " + SupplierClientApp.class.getName()
                    		+ " wsURL OR uddiURL wsName");
            		return;
        	}
        	String uddiURL = null;
        	String wsName = null;
        	String wsURL = null;
        	if (args.length == 1) {
            		wsURL = args[0];
        	} else if (args.length >= 2) {
            		uddiURL = args[0];
            		wsName = args[1];
			System.out.println(uddiURL);
        	}

        	// Create client
        	SupplierClient client = null;

        	if (wsURL != null) {
            		System.out.printf("Creating client for server at %s%n", wsURL);
            		client = new SupplierClient(wsURL);
       		} else if (uddiURL != null) {
            		System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                		uddiURL, wsName);
            		client = new SupplierClient(uddiURL, wsName);
        	}

		// the following remote invocations are just basic examples
		// the actual tests are made using JUnit

		System.out.println("Invoke ping()...");
		String result = client.ping("client");
		System.out.print("Result: ");
		System.out.println(result);

        {
            ProductView prod = new ProductView();
            prod.setId("p1");
            prod.setDesc("AAA bateries (pack of 3)");
            prod.setPrice(3);
            prod.setQuantity(10);
            client.createProduct(prod);
        }

        {
            ProductView prod = new ProductView();
            prod.setId("p2");
            prod.setDesc("10x AAA battery");
            prod.setPrice(5);
            prod.setQuantity(25);
            client.createProduct(prod);
        }

        {
            ProductView prod = new ProductView();
            prod.setId("p3");
			prod.setDesc("Digital Multimeter");
			prod.setPrice(15);
			prod.setQuantity(50);
            client.createProduct(prod);
        }
	}

}
