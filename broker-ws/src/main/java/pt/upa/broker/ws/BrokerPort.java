package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.*;
import javax.jws.WebService;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import javax.xml.registry.*;
import javax.xml.ws.*;

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
    String url;
    try {
      url = uddiNaming.lookup(name);
      if( url != null) {
          return (name + " found");
        } else {
          return (name + " not found");
        }
    } catch(JAXRException e){
      System.out.printf("Caught exception: %s%n", e);
      e.printStackTrace();
      return "Error making contact";
    }
  }


  public String requestTransport(String origin, String destination, int price) throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
								UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    boolean orig, dest;
    int x;
    TransporterPortType trans;
    
    for(x = 0; x < cidades.size(); x++) {
        if(origin == cidades.get(x)) {
            orig = true;
        }

        if(destination == cidades.get(x)) {
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
    job.setId("1111312321"); // o que é suposto por no ID?
    contratos.add(job);
    
    try{
    	List<String> urls = new ArrayList<String>(uddiNaming.list("UpaTransporter"));
    	
    	for(x = 0;x < urls.size(); x++){
    		trans = binding(urls.get(x));
    		
    		try{
    			JobView s = trans.requestJob(origin, destination, price);
    			if (s.getJobPrice() < job.getPrice()) {
    				job.setPrice(s.getJobPrice());
    				job.setId(s.getJobIdentifier());
    				job.setTransporterCompany(s.getCompanyName());
    			}
    		} catch(Exception e){
    			System.out.printf("Caught exception: %s%n", e);
    			e.printStackTrace();
    		}
    	}
    } catch(JAXRException e) {
    	System.out.printf("Caught exception: %s%n", e);
		e.printStackTrace();
    }
    
    if(job.getTransporterCompany() != null){
    	return job.getId();
    } else{
    	return null; 
    }
  }

  public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
	TransportView y;
	TransporterPortType trans;
	for(int x = 0; x < contratos.size(); x++) {
		y = contratos.get(x);
		if (id == y.getId()){
			try {
				trans = binding(uddiNaming.lookup(y.getTransporterCompany()));
				y.setState(trans.jobStatus(id).getJobState());
				
				return y;
				
			} catch(JAXRException e){
				System.out.printf("Caught exception: %s%n", e);
					e.printStackTrace();
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
  
  private TransporterPortType binding(String url) {
	  TransporterService service = new TransporterService(); 
	  TransporterPortType port = service.getTransporterPort();
	  BindingProvider bindingProvider = (BindingProvider) port;
	  Map<String, Object> requestContext = bindingProvider.getRequestContext();
	  requestContext.put(ENDPOINT_ADDRESS_PROPERTY, url);
	  
	  return port; 
  }
}
