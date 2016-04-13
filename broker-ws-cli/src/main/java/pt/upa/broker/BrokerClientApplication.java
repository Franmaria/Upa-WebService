package pt.upa.broker;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.TransportView;

public class BrokerClientApplication {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL name%n", BrokerClientApplication.class.getName());
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
		BrokerService service = new BrokerService();
		BrokerPortType port = service.getBrokerPort();
		
		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
		
		String id = port.requestTransport("Lisboa", "Coimbra", 21);
		System.out.println(id);
		TransportView s = port.viewTransport(id);
		System.out.println(s.getDestination());
		System.out.println(s.getOrigin());
		System.out.println(s.getId());
		System.out.println(s.getTransporterCompany());
		System.out.println(s.getState().value());
		System.out.println(s.getPrice());

		try {
			  Thread.sleep(5000);
			} catch (InterruptedException ie) {
			    //Handle exception
			}
		
		s = port.viewTransport(id);
		System.out.println(s.getDestination());
		System.out.println(s.getOrigin());
		System.out.println(s.getId());
		System.out.println(s.getTransporterCompany());
		System.out.println(s.getState().value());
		System.out.println(s.getPrice());
	}
}
