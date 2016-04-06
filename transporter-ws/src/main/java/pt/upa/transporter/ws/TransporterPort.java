package pt.upa.transporter.ws;

import java.util.*;
import javax.jws.WebService;

@WebService(
	    endpointInterface="pt.upa.transporter.ws.TransporterPortType",
	    wsdlLocation="transporter.1_0.wsdl",
	    name="TransporterWebService",
	    portName="TransporterPort",
	    targetNamespace="http://ws.transporter.upa.pt/",
	    serviceName="TransporterService"
	)

public class TransporterPort implements TransporterPortType {

	public String ping(String name){
		return "string";
	}

	public JobView requestJob( String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception {
		JobView n = new JobView();
		return n;
	}

	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		JobView n = new JobView();
		return n;
	}

	public JobView jobStatus(String id){
		JobView n = new JobView();
		return n;
	}

	public List<JobView> listJobs(){
		List<JobView> j = new ArrayList<JobView>();
		return j;
	}

	public void clearJobs(){

	}
}
