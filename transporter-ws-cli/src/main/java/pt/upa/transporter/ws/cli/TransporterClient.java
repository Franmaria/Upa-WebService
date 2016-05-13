package pt.upa.transporter.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.ws.BindingProvider;

import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.handler.ClientHandler;

public class TransporterClient {
	
	private TransporterPortType port;
	public static Map<String,Certificate> certMap = new HashMap<String,Certificate>();
	public static ArrayList<byte[]> nonceList = new ArrayList<byte[]>();
	
	public TransporterClient(String url, String serverName) {
		//serverName e' o nome a quem o cliente se quer conectar
		Certificate cert;
		String certificateFilePath = "../keys_2016_05_06__20_39_05/"+serverName + "/"+ serverName + ".cer";
		try {
			cert = readCertificateFile(certificateFilePath);
			certMap.put("UpaBroker", cert);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// binding
		TransporterService service = new TransporterService(); 
		port = service.getTransporterPort();
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		// properties
		ClientHandler.FOREIGN_ORG_PROPERTY = serverName;
		ClientHandler.PROPERTY_ORGANIZATION="UpaBroker"; // transporter client so' e' criado por broker transporter
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, url);
	}
	
	public String ping(String name) {
		return port.ping(name);
	}
	
	public JobView requestJob( String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception {
		return port.requestJob(origin, destination, price);
	}
	
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		return port.decideJob(id, accept);
	}
	
	public JobView jobStatus(String id) {
		return port.jobStatus(id);
	}
	
	public List<JobView> listJobs() {
		return port.listJobs();
	}
	
	public void clearJobs() {
		port.clearJobs();
	}
	public static Certificate readCertificateFile(String certificateFilePath) throws Exception {
		FileInputStream fis;

		try {
			fis = new FileInputStream(certificateFilePath);
		} catch (FileNotFoundException e) {
			//System.err.println("Certificate file <" + certificateFilePath + "> not fount.");
			throw new FileNotFoundException();
		}
		BufferedInputStream bis = new BufferedInputStream(fis);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		if (bis.available() > 0) {
			Certificate cert =  cf.generateCertificate(bis);
			return cert;
			// It is possible to print the content of the certificate file:
			// System.out.println(cert.toString());
		}
		bis.close();
		fis.close();
		return null;
	}
}
