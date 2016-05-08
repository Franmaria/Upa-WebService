package pt.upa.broker.ws;


import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.*;

import javax.jws.WebService;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.*;

import javax.xml.registry.*;
import javax.xml.ws.BindingProvider;

@WebService(
    endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="BrokerWebService",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
)

public class BrokerPort implements BrokerPortType {

  private BrokerPortType brokerReplica;
  private UDDINaming uddiNaming;
  private List<TransportView> contratos = new ArrayList<TransportView>();
  private List<String> cidades = new ArrayList<String>(Arrays.asList("Porto","Braga","Viana do Castelo","Vila Real","Bragança",
                                                              "Lisboa","Leiria","Santarém","Castelo Branco","Coimbra","Aveiro","Viseu","Guarda",
                                                              "Setúbal","Évora","Portalegre","Beja","Faro"));
  
  public BrokerPort(String uddiURL) {
    /*Broker port cria uma instancia do UDDINaming e atribui a variavel global uddiNaming*/
	try {
    	uddiNaming = new UDDINaming(uddiURL);
    } catch(JAXRException e){
    	System.out.printf("Caught exception: %s%n", e);
    	e.printStackTrace();
    }
	
  }

  public String ping(String name) {
	/*ping retorna o prorio argumento, serve para testar a ligacao*/
	 return name; 
  }


  public String requestTransport(String origin, String destination, int price) throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
								UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
	  /*requestTransport pede a todas as transportadoras uma oferta e escolhe a melhor*/
	boolean orig = false , dest = false;
	int x;
    TransporterClient trans;
    
    /*-------------------------teste para verificar se a localizacao e o price sao validos--------------------------*/
    for(x = 0; x < cidades.size(); x++) {
        if(origin.equals(cidades.get(x))) {
            orig = true;
        }

        if(destination.equals(cidades.get(x))) {
            dest = true;
        }

        if( dest && orig) {
          break;
        }
    }

    if( !dest || !orig) {
      UnknownLocationFault f = new UnknownLocationFault();
      if(!dest && !orig) {
        f.setLocation(origin + " " + destination);
      } else if (!dest) {
        f.setLocation(destination);
      } else {
        f.setLocation(origin);
      }

      throw new UnknownLocationFault_Exception("Localização errada",f);
    }

    if(price < 0) {
      InvalidPriceFault i = new InvalidPriceFault();
      i.setPrice(price);
      throw new InvalidPriceFault_Exception("Preço incorreto", i);
    }

    /*------------------------Criacao de um novo transport e procura da melhor oferta---------------------------*/
    TransportView job = new TransportView();
    job.setState(TransportStateView.REQUESTED);
    job.setDestination(destination);
    job.setOrigin(origin);
    contratos.add(job);
    
    boolean existeOferta = false; 
	String url = null; // variavel que guarda o url da melhor oferta ou no caso de nenhuma oferta ser aceite da primeria valida 
    
	
    List<String> urls;
	try {
		urls = new ArrayList<String>(uddiNaming.list("UpaTransporter%"));
			
		for(x = 0;x < urls.size(); x++){
			JobView s;
				
			try {
				
				trans = new TransporterClient(urls.get(x));	
				s = trans.requestJob(origin, destination, price);
	   
				if(s != null) {	
					
					if(url == null) {  // se for a primeira oferta valida entra dento do if
						existeOferta = true;
						job.setPrice(s.getJobPrice());
						job.setId(s.getJobIdentifier());
						job.setState(TransportStateView.BUDGETED);
						url = urls.get(x);
					}
					
	    			if(s.getJobPrice() <= job.getPrice()) {	
	    				job.setId(s.getJobIdentifier());
	    				job.setPrice(s.getJobPrice()); // o preco do job e sempre atualizado dentro do if para no caso de nenhuma oferta ser aceite termos a melhor oferta 
	    			
	    				if (s.getJobPrice() <= price) { 
		    				job.setTransporterCompany(s.getCompanyName());
		    				url = urls.get(x);
	    				} else 
		    				trans.decideJob(s.getJobIdentifier(), false);
	    			
	    			} else
							trans.decideJob(s.getJobIdentifier(), false); 	
				}
			} catch (BadLocationFault_Exception | BadPriceFault_Exception | BadJobFault_Exception e) {
				System.out.printf("Caught exception: %s%n", e);
			    e.printStackTrace();
			} catch(Exception e) {
				System.out.printf("Caught exception: %s%n", e);
			    e.printStackTrace();
			}
		}
	} catch (JAXRException e) {
		System.out.printf("Caught exception: %s%n", e);
	    e.printStackTrace();
	}	
    
	if(job.getTransporterCompany() != null) { // verifica se existe uma transportadora no job pois so lhe e atribuida uma companhia se houver uma proposta que pode ser aceite
    	
    	trans = new TransporterClient(url);
    	
    	try {
    		trans.decideJob(job.getId(), true);
    		job.setState(TransportStateView.BOOKED);
    		
    	} catch(BadJobFault_Exception e) {
    		job.setState(TransportStateView.FAILED);
    		System.out.printf("Caught exception: %s%n", e);
    		e.printStackTrace();
    		
        }
    	
    } else{
    	job.setState(TransportStateView.FAILED);
    	
    	if(existeOferta) { // se existir pelo menos uma oferta quer dizer que nao foi encontrada nenhuma proposta com o price menor que o maximo 
    		UnavailableTransportPriceFault f = new UnavailableTransportPriceFault();
    		f.setBestPriceFound(job.getPrice());
    		throw new UnavailableTransportPriceFault_Exception("Não existe uma oferta com um preço menor",f);
    	}else {
    		UnavailableTransportFault f = new UnavailableTransportFault();
    		f.setDestination(destination);
    		f.setOrigin(origin);
    		throw new UnavailableTransportFault_Exception("Nao existe um transporter disponivel",f);
    	}
    }
    
    return job.getId();
  }

  public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
	/*vieTransport procura pelo transporte com a Identificacao id*/
	TransporterClient trans;
	
	if(id != null) {
		for(TransportView y : contratos){ 
					
			if (id.equals(y.getId())){
				
				try {
					if(!y.getState().equals(TransportStateView.FAILED)) {
						trans = new TransporterClient(uddiNaming.lookup(y.getTransporterCompany()));
						String v = trans.jobStatus(id).getJobState().value(); 
						if(v.equals("ACCEPTED")) {
							y.setState(TransportStateView.BOOKED);
						}else if(v.equals("REJECTED")) {
							y.setState(TransportStateView.FAILED);
						}else {
							y.setState(TransportStateView.fromValue(v));
						}
						mainUpdateTranspots(y);
					}
					return y;
					
				} catch(JAXRException e){
					System.out.printf("Caught exception: %s%n", e);
					e.printStackTrace();
				} catch(Exception e) {
					System.out.printf("Caught exception: %s%n", e);
					e.printStackTrace();
				}
			}
		}
	}
	UnknownTransportFault t = new UnknownTransportFault();
	t.setId(id);
	throw new UnknownTransportFault_Exception("O contrato de transporte não existe", t);
  }

  public List<TransportView> listTransports() {
	  /*listTransport e um metodo auxiliar que retorna todos a lista contratos*/
	  return contratos;
  }

  public void clearTransports(){
	  /*cleaTransports e um metodo auxiliar que "apaga" a lista de trabalhos e pede as transportadoras para fazer o mesmo*/
	contratos = new ArrayList<TransportView>();
	
	List<String> urls; 
	TransporterClient trans; 
	
	try {
		urls = new ArrayList<String>(uddiNaming.list("UpaTransporter%"));
		
		for(String i : urls) {
			try {
				trans = new TransporterClient(i);
				trans.clearJobs();
			} catch(Exception e){
				System.out.printf("Caught exception: %s%n", e);
				e.printStackTrace();
				continue;
			}
		}
		mainUpdateClearTransports(); //faz o clear no server replica
		
	} catch (JAXRException e) {
		System.out.printf("Caught exception: %s%n", e);
		e.printStackTrace();
	}
  }

  private void mainUpdateTranspots(TransportView job){
	  //Metodo usado pelo Servidor principal para fazer update da replica 
	  connectReplica();
	  brokerReplica.updateTranspots(job);
  }
  
  private void mainUpdateClearTransports() {
	  connectReplica();
	  brokerReplica.updateClearTransports();
  }
  
  private void connectReplica(){  
		if (brokerReplica == null) {
			String brokerReplicaUrl = null;
			try {
				brokerReplicaUrl = uddiNaming.lookup("UpaBrokerReplica");
			} catch (JAXRException e) {
				System.out.println("error in lookup");
			}
			BrokerService service = new BrokerService(); 
			brokerReplica = service.getBrokerPort();
			BindingProvider bindingProvider = (BindingProvider) brokerReplica;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, brokerReplicaUrl);
		}
	  }

  /*------------------------Metodos usados pelo UpaBrokerReplica ---------------------------*/
  public class InnerClass extends TimerTask {
		public void run() {
			checkMainServer();
		}
	}

  public void checkMainServer() {
	  String brokerUrl = null;
	  try {
		brokerUrl = uddiNaming.lookup("UpaBroker");
	} catch (JAXRException e) {
		System.out.println("error in lookup");
	}
	  
	BrokerService service = new BrokerService(); 
	BrokerPortType port = service.getBrokerPort();
	BindingProvider bindingProvider = (BindingProvider) port;
	Map<String, Object> requestContext = bindingProvider.getRequestContext();
	requestContext.put(ENDPOINT_ADDRESS_PROPERTY, brokerUrl);
	
	if(port.ping("test").equals("test")) {
		//operacao feita se o servidor principal do broker estiver activo 
		Timer timer = new Timer(true);
		TimerTask timerTask = new InnerClass();
		timer.schedule(timerTask,1000); 
	} else{
		String brokerReplicaUrl;
		try {
			brokerReplicaUrl = uddiNaming.lookup("UpaBrokerReplica");
			uddiNaming.rebind("UpaBroker", brokerReplicaUrl);
		} catch (JAXRException e) {
			System.out.println("error in lookup or rebind");
		}	
	} 
  }
  
    
  public void updateTranspots(TransportView job) { 
	  for (TransportView j : contratos){
		 if(job.getId().equals(j.getId())) {
			 j.setState(job.getState()); // se o trabalho ja estiver nos contratos quer dizer que é para mudar o estado 
			 return; 
		 } 
	  }
	 
	  contratos.add(job); // se nao tiver adiciona-se 
  }
  
  public void updateClearTransports(){
	  contratos = null; 
  }

}
