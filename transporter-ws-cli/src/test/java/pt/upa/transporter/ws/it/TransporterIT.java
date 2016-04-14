package pt.upa.transporter.ws.it;

import org.junit.*;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.*;
import pt.upa.transporter.ws.cli.TransporterClient;

import static org.junit.Assert.*;

import javax.xml.registry.JAXRException;

/**
 *  Integration Test example
 *  
 *  Invoked by Maven in the "verify" life-cycle phase
 *  Should invoke "live" remote servers 
 */
public class TransporterIT {

    // static members
	private static TransporterClient transporter1;
	private static TransporterClient transporter2;
	private static String uddiURL = "http://localhost:9090"; 
    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {
    	UDDINaming uddiNaming = null;
    	String url1 = null, url2 = null; 
    	try {
			uddiNaming = new UDDINaming(uddiURL);
		    } catch(JAXRException e){
		      System.out.printf("Caught exception: %s%n", e);
		      e.printStackTrace();
		    }
		
    	try {
		url1 = uddiNaming.lookup("UpaTransporter1");
    	url2 = uddiNaming.lookup("UpaTransporter2");
    	} catch(JAXRException e){
		      System.out.printf("Caught exception: %s%n", e);
		      e.printStackTrace();
		}
		
		transporter1 = new TransporterClient(url1);
		transporter2 = new TransporterClient(url2);
    }

    @AfterClass
    public static void oneTimeTearDown() {
    	transporter1 = null;
    	transporter2 = null;
    }


    // members


    // initialization and clean-up for each test

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    // tests

    @Test
    public void pingTest() {	
    	String r1 = transporter1.ping("hello");
    	String r2 = transporter1.ping("hello");
    	assertEquals("Ping erro", "hello",r1);
    	assertEquals("Ping erro", "hello",r2);
    }
    
    @Test
    public void test1() {
    	JobView job1 = null, job2 = null, job3 = null, job4 = null, job5 = null, job6 = null;
    	
    	
    	try {
	    	job1 = transporter1.requestJob("Lisboa", "Porto", 20);
	    	job2 = transporter2.requestJob("Lisboa", "Porto", 20);
	    	job3 = transporter1.requestJob("Lisboa", "Faro", 21);
	    	job4 = transporter2.requestJob("Lisboa", "Faro", 21);
	    	job5 = transporter1.requestJob("Lisboa", "Coimbra", 21);
	    	job6 = transporter2.requestJob("Lisboa", "Coimbra", 21);
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
    	
    	assertNull("Teste1 job1 esperado null",job1);
    	assertNotNull("Teste1 job2 esperado not null", job2);
    	assertNotNull("Teste1 job3 esperado not null",job3);
    	assertNull("Teste1 job4 esperado null",job4);
    	assertNotNull("Teste1 job5 esperado not null",job5);
    	assertNotNull("Teste1 job6 esperado not null",job6);
    	
    	try{
    		job2 = transporter2.decideJob(job2.getJobIdentifier(),true);
    		job3 = transporter1.decideJob(job3.getJobIdentifier(), true);
    		job5 = transporter1.decideJob(job5.getJobIdentifier(), true);
    		job6 = transporter2.decideJob(job2.getJobIdentifier(), false);
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
    	
    	assertEquals("Teste1 erro state view", JobStateView.ACCEPTED, job2.getJobState());
    	assertEquals("Teste1 erro state view", JobStateView.ACCEPTED, job3.getJobState());
    	assertEquals("Teste1 erro state view", JobStateView.ACCEPTED, job5.getJobState());
    	assertEquals("Teste1 erro state view", JobStateView.REJECTED, job6.getJobState());
    	
    	try {
			  Thread.sleep(5000);
			} catch (InterruptedException ie) {
			    //Handle exception
			}
    	job2 = transporter2.jobStatus(job2.getJobIdentifier());
    	job3 = transporter1.jobStatus(job3.getJobIdentifier());
    	job5 = transporter1.jobStatus(job5.getJobIdentifier());
    	
    	if(JobStateView.ACCEPTED.equals(job2.getJobState())) {
    		fail("Teste1 o state nao mudou ao longo do tempo");
    	}
    	if(JobStateView.ACCEPTED.equals(job3.getJobState())) {
    		fail("Teste1 o state nao mudou ao longo do tempo");
    	}
    	if(JobStateView.ACCEPTED.equals(job5.getJobState())) {
    		fail("Teste1 o state nao mudou ao longo do tempo");
    	}
    }
    

}