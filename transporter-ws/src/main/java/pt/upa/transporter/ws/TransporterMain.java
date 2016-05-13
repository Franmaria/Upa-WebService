package pt.upa.transporter.ws;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Endpoint;

import pt.upa.transporter.ws.handler.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterPort;

public class TransporterMain {
	public static Map<String,Certificate> certMap = new HashMap<String,Certificate>();
	public static ArrayList<byte[]> nonceList = new ArrayList<byte[]>();

	public static void main(String[] args) {
		// Check arguments
		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL wsName wsURL%n", TransporterMain.class.getName());
			return;
		}
		
		String uddiURL = args[0];
		String name = args[1];
		String url = args[2];
		String number = args[3];
		Certificate cert;
		String serverName;
;
		String certificateFilePath;
		try {
			serverName="UpaTransporter"+number;
			certificateFilePath = "../keys_2016_05_06__20_39_05/"+serverName + "/"+ serverName + ".cer";
			cert = readCertificateFile(certificateFilePath);
			certMap.put(serverName, cert);
			serverName="ca";
			certificateFilePath = "../keys_2016_05_06__20_39_05/"+serverName + "/"+ serverName + ".cer";
			cert = readCertificateFile(certificateFilePath);
			certMap.put(serverName, cert);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServerHandler.PROPERTY_ORGANIZATION="UpaTransporter"+number;

		Endpoint endpoint = null;
		UDDINaming uddiNaming = null;
		try {
			TransporterPort port = new TransporterPort(Integer.parseInt(number));
			endpoint = Endpoint.create(port);
			// publish endpoint
			
			System.out.printf("Starting %s%n", url);
			endpoint.publish(url);

			// publish to UDDI
			System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
			uddiNaming = new UDDINaming(uddiURL);
			uddiNaming.rebind(name, url);

			// wait
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
			System.in.read();

		} catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();

		} finally {
			try {
				if (endpoint != null) {
					// stop endpoint
					endpoint.stop();
					System.out.printf("Stopped %s%n", url);
				}
			} catch (Exception e) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
			try {
				if (uddiNaming != null) {
					// delete from UDDI
					uddiNaming.unbind(name);
					System.out.printf("Deleted '%s' from UDDI%n", name);
				}
			} catch (Exception e) {
				System.out.printf("Caught exception when deleting: %s%n", e);
			}
		}

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
