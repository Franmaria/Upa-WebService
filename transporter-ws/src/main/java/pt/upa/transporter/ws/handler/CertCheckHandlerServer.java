package pt.upa.transporter.ws.handler;


import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.xml.namespace.QName;
import javax.xml.registry.JAXRException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import example.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterMain;



public class CertCheckHandlerServer implements SOAPHandler<SOAPMessageContext> {
	// this handler gets the missing certificate
	public static final String CLASS_NAME = CertCheckHandlerServer.class.getSimpleName();

	public static Certificate cert=null;
	
	
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		System.out.printf("%s current class %n", CLASS_NAME);
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) {
// certcheckServer does nothing in outbound message - in client it should only do something in outbound message
		} else {
			if (TransporterMain.certMap.get("UpaBroker")!=null){
				// certificado já existe
				return true;
			}
			// get certificate
			UDDINaming uddi=null;
			CAClient CAcli=null;
			System.out.println("UpaCA lookup");

			try {
				uddi = new UDDINaming("http://localhost:9090");
				CAcli = new CAClient(uddi.lookup("UpaCA"),ServerHandler.PROPERTY_ORGANIZATION);
			} catch (JAXRException e1) {
				e1.printStackTrace();
				throw new RuntimeException("erro com a conexao");
			}
			
			byte[] result = CAcli.getCertificate("UpaBroker"); // vai sempre buscar o certificado do broker pois os transporters so comunicam com o broker
			if(result==null) {
				System.out.println("certificate not found");

			} else {
				System.out.println("got certificate");
			}
			//tem que se construir uma fabrica para criar o certeficado outra vez e depois poder usa-lo 
			CertificateFactory certFactory=null;
			Certificate cert=null;
			try {
				certFactory = CertificateFactory.getInstance("X.509");
				InputStream in = new ByteArrayInputStream(result);
				cert = certFactory.generateCertificate(in);
			} catch (CertificateException e) {
				System.out.println("couldnt regenerate certificate");
				e.printStackTrace();
				throw new RuntimeException("couldnt generate certificate");
			}
			
			//serve para ver se tudo correu bem ou nao 
			if(cert == null) {
				System.out.println("no certified for organization with given name exists");
				throw new RuntimeException("certificado para org n existe");
			}
			TransporterMain.certMap.put("UpaBroker", cert);
			System.out.println("finished certCheckServer");

			//System.out.printf("printing certified.. %n" + cert); // error
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		System.out.printf("%s entered %n", CLASS_NAME);
		System.out.printf("organizacao: %s %n", ServerHandler.PROPERTY_ORGANIZATION);
		throw new RuntimeException("erro certCheck handler server");
	}

	@Override
	public void close(MessageContext context) {
		
	}

	@Override
	public Set<QName> getHeaders() {
		return null;
	}
	
}
