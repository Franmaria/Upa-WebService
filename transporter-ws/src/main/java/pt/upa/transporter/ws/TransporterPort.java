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

	private List<JobView> jobs = new ArrayList<JobView>();
	private int transportNumber; 
	private int contratNumber = 0;
	private List<String> rNorte = new ArrayList<String>(Arrays.asList("Porto","Braga","Viana do Castelo","Vila Real","Bragança"));
	private List<String> rCentro = new ArrayList<String>(Arrays.asList("Lisboa","Leiria","Santarém","Castelo Branco","Coimbra","Aveiro","Viseu","Guarda"));
	private List<String> rSul = new ArrayList<String>(Arrays.asList("Setúbel","Évora","Portalegre","Beja","Faro"));
	
	public TransporterPort(int n) {
		transportNumber = n; 
	}
	public String ping(String name){
		return name;
	}

	public JobView requestJob( String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception {
		int paridade = transportNumber % 2;
		
		if (checkRegion(paridade, origin, 0) && checkRegion(paridade, destination, 0)) {
			JobView n = new JobView();
			jobs.add(n);
		} else if (!checkRegion(paridade, origin, 1) || !checkRegion(paridade, destination, 1) ){
			throw BadLocationFault_Exception();
		}
		
		return null;
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
		return jobs;
	}

	public void clearJobs(){
		jobs = new ArrayList<JobView>();
		contratNumber = 0; 
	}
	
	private boolean checkRegion(int paridade, String regiao, int modo) {
		/* se o inteiro paridade for 0 o numero e par se for 1 o numero e impar. Quando modo e 0 percorrre as listas normais, qunado 
		 * o modo e 1 ele percorre a que nao correria se fosse 0 */
		int x; 
		
		if(modo == 0) {
			for(x = 0; x < rCentro.size(); x++) {
				if (regiao.equals(rCentro.get(x))) {
					return true; 
				}
			}
		}	
		
		if(paridade == 0 || (paridade == 1 && modo == 1)) {
			for(x = 0; x < rNorte.size(); x++){
				if (regiao.equals(rNorte.get(x))) {
					return true; 
				}
			}
		} 
		
		if(paridade == 1 || (paridade == 0 && modo == 1)) {
			for(x = 0; x < rSul.size(); x++){
				if (regiao.equals(rSul.get(x))) {
					return true; 
				}
			}
		}
		
		return false;
	}
}
