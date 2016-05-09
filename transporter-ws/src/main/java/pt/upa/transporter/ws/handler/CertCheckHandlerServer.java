package pt.upa.transporter.ws.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.xml.namespace.QName;
import javax.xml.registry.JAXRException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import example.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;



public class CertCheckHandlerServer implements SOAPHandler<SOAPMessageContext> {
	// this handler gets the missing certificate
	

	public static Map<String,Certificate> certMap = new HashMap<String,Certificate>();
	//TODO check if certMap needs to be public
	public static Certificate cert=null;
	
	
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		//TODO check if outbound inbound message differentiation needed
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) {
			//does nothing in outbound message - in client it should only do something in outbound message
		} else {
			
			if (certMap.get(ServerHandler.RESPONSE_PROPERTY_ORGANIZATION)!=null){
				// certificado já existe
				return true;
			}
			// get certificate
			UDDINaming uddi=null;
			CAClient CAcli=null;
			try {
				uddi = new UDDINaming("http://localhost:9090");
				CAcli = new CAClient(uddi.lookup(ServerHandler.RESPONSE_PROPERTY_ORGANIZATION));
	
			} catch (JAXRException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	
			//System.out.println("Remote call ...");
			//o certificado vem em byte[]
			byte[] result = CAcli.getCertificate(ServerHandler.RESPONSE_PROPERTY_ORGANIZATION);
			//tem que se construir uma fabrica para criar o certeficado outra vez e depois poder usa-lo 
			CertificateFactory certFactory;
			try {
				certFactory = CertificateFactory.getInstance("X.509");
				InputStream in = new ByteArrayInputStream(result);
				Certificate cert = (Certificate)certFactory.generateCertificate(in);
				
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//serve para ver se tudo correu bem ou nao 
			if(cert == null){
				System.out.println("no certified for organization with given name exists");
			}
			System.out.printf("printing certified.. %n"+cert.toString());
		}
		return false;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close(MessageContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
