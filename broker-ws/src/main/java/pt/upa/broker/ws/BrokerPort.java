package pt.upa.broker.ws;


import java.util.*;
import javax.jws.WebService;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.*;

import javax.xml.registry.*;

@WebService(
    endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="BrokerWebService",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
)

public class BrokerPort implements BrokerPortType {

  private UDDINaming uddiNaming;
  private List<TransportView> contratos = new ArrayList<TransportView>();
  private List<String> cidades = new ArrayList<String>(Arrays.asList("Porto","Braga","Viana do Castelo","Vila Real","Bragança",
                                                              "Lisboa","Leiria","Santarém","Castelo Branco","Coimbra","Aveiro","Viseu","Guarda",
                                                              "Setúbal","Évora","Portalegre","Beja","Faro"));

  public BrokerPort(String uddiURL) {
    try {
      uddiNaming = new UDDINaming(uddiURL);
    } catch(JAXRException e){
      System.out.printf("Caught exception: %s%n", e);
      e.printStackTrace();
    }
  }

  public String ping(String name) {
	  return name; 
  }


  public String requestTransport(String origin, String destination, int price) throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
								UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    boolean orig = false , dest = false;
    int x;
    TransporterClient trans;
    String url = null;
    
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

    TransportView job = new TransportView();
    job.setState(TransportStateView.REQUESTED);
    job.setDestination(destination);
    job.setOrigin(origin);
    job.setPrice(price);
    contratos.add(job);
    
    try{
    	List<String> urls = new ArrayList<String>(uddiNaming.list("UpaTransporter%"));
    	
    	for(x = 0;x < urls.size(); x++){
    		trans = new TransporterClient(urls.get(x));
    		
    		try{
    			JobView s = trans.requestJob(origin, destination, price);
	    		if(s != null) {	
	    			if (s.getJobPrice() < job.getPrice()) {
	    				/* Diz que o melhor trabalho anterior nao vai ser aceite*/
	    				if (url != null) {
	    					trans = new TransporterClient(url);
	    					trans.decideJob(s.getJobIdentifier(), false);
	    				} else {
	    					job.setState(TransportStateView.BUDGETED);
	    				}
	    				
	    				job.setPrice(s.getJobPrice());
	    				job.setId(s.getJobIdentifier());
	    				job.setTransporterCompany(s.getCompanyName());
	    				url = urls.get(x);
	    				
	    			} else {
	    				trans.decideJob(s.getJobIdentifier(), false);
	    			}
	    		}
    			
    		} catch(Exception e){
    			// o que fazer naquele caso lol 
    			System.out.printf("Caught exception: %s%n", e);
    			e.printStackTrace();
    		}
    	}
    } catch(JAXRException e) {
    	System.out.printf("Caught exception: %s%n", e);
		e.printStackTrace();
    }
 
    if(job.getTransporterCompany() != null) {
    	
    	trans = new TransporterClient(url);
    	
    	try {
    		trans.decideJob(job.getId(), true);
    		job.setState(TransportStateView.BOOKED);
    		return job.getId();
    	
    	} catch(BadJobFault_Exception e) {
    		job.setState(TransportStateView.FAILED);
    		System.out.printf("Caught exception: %s%n", e);
    		e.printStackTrace();
    		return job.getId();
        }
    	
    } else{
    	
    	job.setState(TransportStateView.FAILED);
    	return job.getId(); 
    }
  }

  public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
	TransportView y;
	TransporterClient trans;
	
	if(id != null) {
		for(int x = 0; x < contratos.size(); x++) {
			y = contratos.get(x);
					
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
					}
					return y;
					
				} catch(JAXRException e){
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
	  return contratos;
  }

  public void clearTransports(){
	contratos = new ArrayList<TransportView>();
  }
}
