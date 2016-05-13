package example.ws;

import java.io.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.jws.HandlerChain;
import javax.jws.WebService;




@WebService(endpointInterface = "example.ws.CA")
@HandlerChain(file = "/ws-handler-chain.xml") 

public class CAImpl implements CA {
	public byte[] getCertificate(String serverName) {
		String certificateFilePath = "Certeficate/" + serverName + ".cer";
		FileInputStream fis;
		
		try {
			fis = new FileInputStream(certificateFilePath);
			System.out.println("encontrou o path");
		} catch (FileNotFoundException e) {
			System.err.println("Certificate file <" + certificateFilePath + "> not fount.");
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(fis);

		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			System.err.println("error creating certificate factory");
		}

		try {
			if (bis.available() > 0) {
				Certificate cert = cf.generateCertificate(bis);
				return cert.getEncoded(); //TODO parse 64 function?, check if works fine
			}
		} catch (CertificateException | IOException e) {
			System.err.println("error generating ceretificate");
		}
		try {
			bis.close();
			fis.close();
		} catch (IOException e) {
			System.err.println("error closing bis or fis");
		}
		return null;
	}
	public String ping(String name) {
		return name;
	}

}
