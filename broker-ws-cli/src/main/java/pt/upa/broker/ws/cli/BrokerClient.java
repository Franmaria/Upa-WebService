package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;


public class BrokerClient {

private BrokerPortType port;
private String uddiURL;  	
	
	public BrokerClient(String uddiUrl) {
		uddiURL = uddiUrl; 
		makeConection(uddiUrl);
	}
	
	public String ping(String name) {
		try{ 
			return port.ping(name);
		}catch(WebServiceException wse){	
			makeConection(uddiURL);
			if(name.equals("kill")){ // sereve para nao matar tambem a replica 
				return "morto"; 
			} else{ 
				return ping(name);
			}	
		}
	}
	
	public String requestTransport(String origin, String destination, int price) throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
	UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		try{
			return port.requestTransport(origin, destination, price);
		}catch(WebServiceException wse){
			makeConection(uddiURL);
			return requestTransport(origin, destination, price);
			
		}
	}
	
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		try{
			return port.viewTransport(id);
		}catch(WebServiceException wse){
			makeConection(uddiURL);
			return viewTransport(id);
		}
	}
	
	public List<TransportView> listTransports() {
		try{
			return port.listTransports();
		}catch(WebServiceException wse){
				makeConection(uddiURL);
				return listTransports();
		}
	}
	
	public void clearTransports() {
		try{	
			port.clearTransports();
		}catch(WebServiceException wse){
			makeConection(uddiURL);
			clearTransports();
		}
	}
	
	private void makeConection(String uddiUrl){
		UDDINaming uddiNaming;
		String url = null; 
		try {
			uddiNaming = new UDDINaming(uddiUrl);
			url = uddiNaming.lookup("UpaBroker");
		} catch (JAXRException e) {
			return; 
		}
		
		try {
			BrokerService service = new BrokerService(); 
			port = service.getBrokerPort();
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, url);
			
			int receiveTimeout = 3000;
	        // The receive timeout property has alternative names
	        // Again, set them all to avoid compability issues
	        final List<String> RECV_TIME_PROPS = new ArrayList<String>();
	        RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
	        RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
	        RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");
	        // Set timeout until the response is received (unit is milliseconds; 0 means infinite)
	        for (String propName : RECV_TIME_PROPS)
	            requestContext.put(propName, receiveTimeout);
		
		} catch (Exception e){
			return; 
		}
	}	
}
