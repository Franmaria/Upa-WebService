package pt.upa.broker.ws;

import java.util.*;
import javax.jws.WebService;

@WebService(
    endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="BrokerWebService",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
)

public class BrokerPort implements BrokerPortType {

	public String ping(String name) {
		return "string";
	}


	public String requestTransport(String origin, String destination, int price) throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
								UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		return "string";
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
    TransportView t = new TransportView();
    return t;
	}

	public List<TransportView> listTransports() {
		List<TransportView> l = new ArrayList<TransportView>();
		return l;
	}

	public void clearTransports(){

	}
}
