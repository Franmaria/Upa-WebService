package example.ws;


import example.ws.cli.CAClient;

// classes generated from WSDL

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class CAClientApplication {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL name%n", CAClient.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];

		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);

		System.out.printf("Looking for '%s'%n", name);
		String endpointAddress = uddiNaming.lookup(name);

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}
		System.out.println("Creating stub ...");
		CAClient CAcli = new CAClient(endpointAddress,"ca");
		
		System.out.println("Remote call ...");
		try {
			System.out.printf("ping francisco: %s%n", CAcli.ping("francisco"));
		} catch(Exception e) {
			System.out.println("caught exception");

		}
	}
}
