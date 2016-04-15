package pt.upa.transporter.ws;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
	private int transportNumber; // guarda o numero do transporter 
	private int contratNumber = 0;
	private List<String> rNorte = new ArrayList<String>(Arrays.asList("Porto","Braga","Viana do Castelo","Vila Real","Bragança"));
	private List<String> rCentro = new ArrayList<String>(Arrays.asList("Lisboa","Leiria","Santarém","Castelo Branco","Coimbra","Aveiro","Viseu","Guarda"));
	private List<String> rSul = new ArrayList<String>(Arrays.asList("Setúbal","Évora","Portalegre","Beja","Faro"));
	
	public class InnerClass extends TimerTask {
		/*InnerClass e usada para fazer a mudaca do estado do trabalho ao longo do tempo */
		JobView _argR;
		
		public InnerClass (JobView argR) {
			_argR = argR;
		}
		
		public void run() {
			// method to be executed by thread scheduler

			boolean bool = false;
			if(_argR.getJobState().value().equals((JobStateView.ACCEPTED).value())) {
				// to heading
				_argR.setJobState(JobStateView.HEADING);
			}else if(_argR.getJobState().value().equals((JobStateView.HEADING).value())) {
				// to ongoing
				_argR.setJobState(JobStateView.ONGOING);
			}else if(_argR.getJobState().value().equals((JobStateView.ONGOING).value())) {
				//to completed (exit thread)
				_argR.setJobState(JobStateView.COMPLETED);
				bool= true; // cancels timerTask
			}
			if (!bool) {
				Timer timer = new Timer(true);
				TimerTask timerTask = new InnerClass(_argR);
				long rand = ThreadLocalRandom.current().nextInt(4001) + 1000;
				timer.schedule(timerTask,rand); // 1 - 5 sec
			}
		}
	}
	
	
	public TransporterPort(int n) {
		/*TransporterPort recebe como argumento o numero do transporter*/
		transportNumber = n; 
	}
	public String ping(String name){
		/*ping retorna o prorio argumento, serve para testar a ligacao*/
		return name;
	}

	public JobView requestJob( String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception {
		int paridade = transportNumber % 2;
		boolean ori, dest;
		int priceOffer;
		
		if(price < 0) {
			BadPriceFault p = new BadPriceFault();
			p.setPrice(price);
			throw new BadPriceFault_Exception("Preco incorreto",p);
		}
		
		if (!checkRegion(paridade, origin, 0) || !checkRegion(paridade, destination, 0)) {
			ori = checkRegion(paridade, origin, 1);
			dest = checkRegion(paridade, destination, 1);
			
			if(!ori || !dest){
				BadLocationFault f = new BadLocationFault();
				
				if (!ori && !dest){
					f.setLocation(origin + " " + destination);
				} else if(!dest) {
					f.setLocation(destination);
				} else {
					f.setLocation(origin);
				}
				
				throw new BadLocationFault_Exception("Localizacao nao existe",f);
			}
			
			return null;
		}
		
		if(price <= 100) {
			priceOffer = getprice(paridade, price);
		} else{
			return null;
		}
		
		JobView job = new JobView();
		job.setCompanyName("UpaTransporter" + Integer.toString(transportNumber));
		job.setJobIdentifier(Integer.toString(transportNumber) + "/" + Integer.toString(contratNumber));
		contratNumber++;
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobPrice(priceOffer);
		job.setJobState(JobStateView.PROPOSED);
		jobs.add(job);
		return job;
	}

	
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		/*Muda o estado do job e pode adiar uma task para mudar o estado ao longo do tempo*/
		for (JobView i : jobs) {
			if (i.getJobIdentifier().equals(id)) {
				if(accept) {
					Timer timer = new Timer(true);
					Random rand = new Random();
					long time = rand.nextInt(5000) + 1;
					i.setJobState(JobStateView.ACCEPTED);
					TimerTask timerTask = new InnerClass(i);
					//running timer task as daemon thread
					/* schedule(TimerTask task, long delay)
					Schedules the specified task for execution after the specified delay.*/
					timer.schedule(timerTask,time); // 1 - 5 sec
				} else {
					i.setJobState(JobStateView.REJECTED);
				}
				return i;
			}
		}
		
		BadJobFault bj = new BadJobFault();
	    bj.setId("job not found");
	    throw new BadJobFault_Exception("badJobFault",bj); 
	}

	public JobView jobStatus(String id){
		/*retorna o job com o id*/
		JobView j; 
		for(int x = 0; x < jobs.size(); x++) {
			j = jobs.get(x);
			if(j.getJobIdentifier().equals(id)) {
				return j;
			}
		}
		return null; 
	}

	public List<JobView> listJobs(){
		return jobs;
	}

	public void clearJobs(){
		jobs = new ArrayList<JobView>();
		contratNumber = 0; 
	}
	
	private boolean checkRegion(int paridade, String regiao, int modo) {
		/* se o inteiro paridade for 0 o numero e par se for 1 o numero e impar. Quando modo e 0 percorrre as listas normais, quando
		 * o modo e 1 ele percorre todas as listas */
		int x; 
		
		for(x = 0; x < rCentro.size(); x++) {
			if (regiao.equals(rCentro.get(x))) {
				return true; 
			}
		}	
		
		if((paridade == 0 || modo == 1)) {
			for(x = 0; x < rNorte.size(); x++){
				if (regiao.equals(rNorte.get(x))) {
					return true; 
				}
			}
		} 
		
		if((paridade == 1 || modo == 1)) {
			for(x = 0; x < rSul.size(); x++){
				if (regiao.equals(rSul.get(x))) {
					return true; 
				}
			}
		}
		
		return false;
	}
	
	private int getprice(int paridade, int price) {	
		if (price <= 10){
			price--;
		
		}
		
		else if(paridade == 0) {
			if (price % 2 == 0) {
				price--;
			} else {
				price++;
			}
		}
		
		else if(paridade == 1) {
			if (price % 2 == 1) {
				price--;
			} else { 
				price++;
			}
		}	
		
		if(price < 0 ) {
			return 0; 
		} else{
			return price;
		}
	}
}
