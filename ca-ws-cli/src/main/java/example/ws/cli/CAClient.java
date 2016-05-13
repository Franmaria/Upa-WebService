package example.ws.cli;
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.ws.BindingProvider;

import example.ws.*;
import example.ws.cli.handler.ClientHandler;

public class CAClient {
	public static Map<String,Certificate> certMap = new HashMap<String,Certificate>();
	public static ArrayList<byte[]> nonceList = new ArrayList<byte[]>();

	
	private CA port;

	public CAClient(String url, String serverName) {
		//serverName e' o nome de quem chama o client

		Certificate cert;
		String certificateFilePath = "../keys_2016_05_06__20_39_05/"+serverName + "/"+ serverName + ".cer";
		
		try {
			cert = readCertificateFile(certificateFilePath);
			certMap.put(serverName, cert);
			certificateFilePath = "../keys_2016_05_06__20_39_05/"+"ca" + "/"+ "ca" + ".cer";
			cert = readCertificateFile(certificateFilePath);
			certMap.put("ca", cert);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//binding
		CAImplService service = new CAImplService();
		port = service.getCAImplPort();
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		//properties
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, url);
		ClientHandler.PROPERTY_ORGANIZATION=serverName;
		ClientHandler.FOREIGN_ORG_PROPERTY="ca";
	}
	
	public byte[] getCertificate(String name) {
		try {
			return port.getCertificate(name);
		} catch(Exception e) {
			System.out.println("caught exception in client get certificate");
		}
		return null;
	}
	public String ping(String name) {
		return port.ping(name);
	}
	public static Certificate readCertificateFile(String certificateFilePath) throws Exception {
		FileInputStream fis;

		try {
			fis = new FileInputStream(certificateFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Certificate file <" + certificateFilePath + "> not fount.");
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