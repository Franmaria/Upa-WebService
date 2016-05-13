package pt.upa.transporter.ws.handler;

import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.xml.namespace.QName;
import javax.xml.registry.JAXRException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import example.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.cli.TransporterClient;



public class CertCheckHandlerClient implements SOAPHandler<SOAPMessageContext> {
	// this handler gets the missing certificate
	
	public static final String HEADER_ORG = "org";
	public static final String REQUEST_NS_ORG = "urn:org";
	public static final String RESPONSE_NS_ORG = REQUEST_NS_ORG;
	//TODO check if TransporterClient.certMap needs to be public
	public static Certificate cert=null;
	public static final String CLASS_NAME = CertCheckHandlerClient.class.getSimpleName();
	
	
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		//TODO check if outbound inbound message differentiation needed
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		System.out.printf("%n entered '%s'%n", CLASS_NAME);
		System.out.printf("%n foreign org '%s'%n", ClientHandler.FOREIGN_ORG_PROPERTY);
		System.out.printf("%n current org '%s'%n", ClientHandler.PROPERTY_ORGANIZATION);

		if (!outbound) {
			// in client it should only do something in outbound message
		} else { 
			// OUTBOUND
			if (TransporterClient.certMap.get(ClientHandler.FOREIGN_ORG_PROPERTY)!=null) {
				// certificado já existe
				System.out.println("certificado ja existe");
				return true; // deve retornar true e continuar para o proximo handler, este e' o funcionamento desejado
			}
			// get certificate
			UDDINaming uddi=null;
			CAClient CAcli=null;
			try {
				uddi = new UDDINaming("http://localhost:9090");
				System.out.println("before lookup CA");
				CAcli = new CAClient(uddi.lookup("UpaCA"),ClientHandler.PROPERTY_ORGANIZATION); 
				System.out.printf("after lookup: %s%n",uddi.lookup("UpaCA") );
			} catch (JAXRException e1) {
				e1.printStackTrace();
				System.out.println("erro na conexao");
				throw new RuntimeException("connection problem");
			}
			byte[] result;
			result = CAcli.getCertificate(ClientHandler.FOREIGN_ORG_PROPERTY);
			System.out.printf("%n foreign org '%s'%n", ClientHandler.FOREIGN_ORG_PROPERTY);
			System.out.printf("%n current org '%s'%n", ClientHandler.PROPERTY_ORGANIZATION);
			
			if(result==null) {
				System.out.println("certificate not found");
			} else {
				System.out.println("got certificate");
			}
//TODO des-encrypt certificate with verifyCertificate method in X509 certificateCheck example ?? check if needed
			CertificateFactory certFactory;
			Certificate cert;
			try {
				certFactory = CertificateFactory.getInstance("X.509");
				InputStream in = new ByteArrayInputStream(result);
				cert = (Certificate)certFactory.generateCertificate(in);
				
			} catch (CertificateException e) {
				e.printStackTrace();
				throw new RuntimeException("erro ao tratar certificado");
			}
			
			//serve para ver se tudo correu bem ou nao 
			if(cert == null){
				System.out.println("no certified for organization with given name exists");
				throw new RuntimeException("certificado n existe");
			}
			TransporterClient.certMap.put(ClientHandler.FOREIGN_ORG_PROPERTY, cert);
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		// erro no handler do server deve vir parar aqui
		System.out.printf("%n entered fault handler '%s'%n", CLASS_NAME);
		throw new RuntimeException("error in server handler"); //should end handling message
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
