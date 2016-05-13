package pt.upa.transporter;


import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.cli.TransporterClient;

public class TransporterClientApplication {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL name%n", TransporterClientApplication.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = "UpaTransporter1";

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
		/*
		TransporterService service = new TransporterService();
		TransporterPortType port = service.getTransporterPort();
		
		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		
		requestContext.put(ENDPOIsNT_ADDRESS_PROPERTY, endpointAddress);
		// extra property
		requestContext.put(CertCheckHandlerClient.PROPERTY, "handlers TransportApp 1");
		*/
		TransporterClient trans = new TransporterClient(endpointAddress,"UpaTransporter1");
		//TODO apagar testes
		System.out.println("ping");
		String a = trans.ping("francisco");
		System.out.printf("ping done answer:%s%n", a);

	
	}
}
