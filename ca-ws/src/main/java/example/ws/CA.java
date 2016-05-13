package example.ws;



import javax.jws.WebService;

@WebService
public interface CA {

	byte[] getCertificate(String name);
	String ping(String name);
}