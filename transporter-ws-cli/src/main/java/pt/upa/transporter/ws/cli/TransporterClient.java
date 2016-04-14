package pt.upa.transporter.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;
import javax.xml.ws.BindingProvider;

import pt.upa.transporter.ws.*;

public class TransporterClient {
	private TransporterPortType port;
	
	public TransporterClient(String url) {
		TransporterService service = new TransporterService(); 
		port = service.getTransporterPort();
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, url);
	}
	
	public String ping(String name) {
		return port.ping(name);
	}
	
	public JobView requestJob( String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception {
		return port.requestJob(origin, destination, price);
	}
	
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		return port.decideJob(id, accept);
	}
	
	public JobView jobStatus(String id) {
		return port.jobStatus(id);
	}
	
	public List<JobView> listJobs() {
		return port.listJobs();
	}
	
	public void clearJobs() {
		port.clearJobs();
	}
}
