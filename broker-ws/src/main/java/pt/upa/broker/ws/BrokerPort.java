package pt.upa.broker.ws;

import java.util.*;
import javax.jws.WebService;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

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
    } catch(JAXRException e){
      System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();
    } finally {
      if( url != null) {
        return (name + " found");
      } else {
        return (name + " not found");
      }
    }
	}


	public String requestTransport(String origin, String destination, int price) throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
								UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    TransportView bestJob;
    return "string";
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
    for(x = 0; x < contratos.size(); x++) {
      y = contratos.get(x);
      if (id == y.getId()){
        try {
          TransporterPort trans = uddiNaming.lookup(y.getTransporterCompany());
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
}
