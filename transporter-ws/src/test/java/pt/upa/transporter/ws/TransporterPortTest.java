package pt.upa.transporter.ws;


import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Mocked;



public class TransporterPortTest {
	private static TransporterPort transporter1;
	private static TransporterPort transporter2; 
		
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// mock UDDI
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		transporter1 = new TransporterPort(1); // T impar
		transporter2 = new TransporterPort(2); // T par
	}

	@After
	public void tearDown() throws Exception {
		transporter1.clearJobs();
		transporter2.clearJobs();
	}

	
	
	
	
	@Test
	public  
	void testRequestJobFullCheck()
        throws Exception {
		JobView j1;
		JobView j2;
		JobView j3;
		JobView j4;
		JobView j5;
		
		// TESTS
		
		
		assertNotNull(transporter1);
		assertNotNull(transporter2);
		j1 = transporter1.requestJob("Lisboa", "Setúbal", 49); // price check
		assertEquals("check origin","Lisboa",j1.getJobOrigin());
		assertEquals("check dest","Setúbal",j1.getJobDestination());
		assertEquals("check ID","1/0",j1.getJobIdentifier());
		assertEquals("impar T impar price",48,j1.getJobPrice());  
		j2 = transporter1.requestJob("Lisboa", "Setúbal", 50); 
		assertEquals("impar T par price",51,j2.getJobPrice());  
		j3 = transporter2.requestJob("Lisboa", "Lisboa", 49); 
		assertEquals("par T impar price",50,j3.getJobPrice());
		j4 = transporter2.requestJob("Lisboa", "Porto", 50); 
		assertEquals("par T par price",49,j4.getJobPrice()); 
		j5 = transporter2.requestJob("Lisboa","Porto",5); // test < 10
		assertEquals("impar T price < 10",4,j5.getJobPrice()); // checks price
		j5 = transporter1.requestJob("Lisboa","Setúbal",5); // test < 10
		assertEquals("impar T price < 10",4,j5.getJobPrice()); // checks price
	}
	
	
	@Test
	public  
	void testRequestJobBadPriceFault()
        throws Exception {
		JobView j1;
		
		// transporter impar
		assertNotNull(transporter1);
		
		
		try {
			// transporter impar
			j1 = transporter1.requestJob("Lisboa", "Setúbal", 100); // impar T par price
			// tests price must be 100
			assertEquals("impar T impar price", 101, j1.getJobPrice());
		} catch(BadPriceFault_Exception e) {
			fail("shouldnt have caugh badprice fault in usual check");
		}	
		
		try {
			j1 = transporter1.requestJob("Lisboa", "Setúbal", 0); // impar T par price
			assertEquals("impar T par price(0)", 0, j1.getJobPrice());
		} catch(BadPriceFault_Exception e) {
			fail("shouldnt have caugh badprice fault in 0 input check");
		}
		
		try {
			j1 = transporter1.requestJob("Lisboa", "Setúbal", -1);
			// this causes exception
			fail("should have caught badpricefault");
		} catch(BadPriceFault_Exception e) {
			assertEquals("Preco incorreto", e.getMessage());	
		}
		// verifications -> verifies expectations
	}
	
	@Test
	public  
	void testRequestJobOriginDestUnknownToTransporter()
        throws Exception {
		JobView j1;
		/*
		 * Este teste verifica se regioes que existem, nao devolvem excecao
		 * e que se existirem mas nao pertencam ao ambito de uma dada 
		 * transportadora retornem null
		 * 
		 * */

		assertNotNull(transporter1);
		assertNotNull(transporter2);
		
		
		try {
			// checks transporter impar
			j1 = transporter1.requestJob("Évora", "Setúbal", 100);
			// tests price must be 100
			assertNotNull("T impar null error",j1); 
			// deve retornar != null regioes pertencem centro e sul
			 
		} catch(BadLocationFault_Exception e) {
			fail("badLocationFault in T impar not null");
		}
		
		try {
			j1 = transporter1.requestJob("Lisboa", "Porto", 100);
			// tests price must be 100
			assertNull("T impar null error",j1);
			// deve retornar != null regioes pertencem centro e norte
			 
		} catch(BadLocationFault_Exception e) {
			fail("badLocationFault in T impar null");
		}
		
		try {
			j1 = transporter2.requestJob("Porto", "Lisboa", 100);
			assertNotNull("T par not null error",j1);
		} catch(BadLocationFault_Exception e) {
			fail("badLocationFault in T par not null");
		}
		
		try {
			j1 = transporter2.requestJob("Setúbal", "Lisboa", 100);
			assertNull("T par null error",j1);
		} catch(BadLocationFault_Exception e) {
			fail("badLocationFault in T par null");
		}
		
	}
	
	@Test
	public  
	void testRequestJobBadLocationFault()
        throws Exception {
		
		JobView j1;
		// TESTS
		
		// impar transporter south center
		assertNotNull(transporter1);

		try {
			j1 = transporter1.requestJob("Lisboa", "Setúbal", 100);
		} catch(BadLocationFault_Exception e) {
			fail("BadLocationFault in valid call Lisboa Setubal");
		}
		
		try {
			j1 = transporter1.requestJob("Lisboa", "Porto", 100);
			assertNull("impar transporter center-north",j1);
		} catch(BadLocationFault_Exception e) {
			fail("BadLocationFault in valid call Lisboa Porto, should be Null");
		}
		
		
		try {
			j1 = transporter1.requestJob("China", "Franca", 100);
			// this causes exception
			fail("fail in invalid origin-dest should return BadJobLocException");
		} catch(BadLocationFault_Exception e) {
			// if it gets exception in first decideJob fails
			assertEquals("Localizacao nao existe", e.getMessage());
			
		}
		// verifications -> verifies expectations
		
		
		
	}
	

	@Test
	public
	void testDecideJobCheckREJECTEDState()
        throws Exception {
		
		JobView j1;
		List<JobView> jobs = transporter1.listJobs();
		// TESTS
		assertNotNull(transporter1);
		createJob(jobs,"T1","Lisboa","Porto", "1/0", 11, JobStateView.ACCEPTED);
		createJob(jobs,"T1","Lisboa","Porto", "1/1", 11, JobStateView.ACCEPTED);
		createJob(jobs,"T1","Lisboa","Porto", "1/2", 11, JobStateView.ACCEPTED);

		try {
			j1 = transporter1.decideJob("1/0", false);
			assertEquals(JobStateView.REJECTED,j1.getJobState());
		} catch(BadJobFault_Exception e) {
			// if it gets exception in first decideJob fails
			fail("caught exception");
		}
		
		try {
			j1 = transporter1.decideJob("id", false); // wrong id
			fail("exception not caught");
		} catch(BadJobFault_Exception e) {
			assertEquals("badJobFault", e.getMessage());
		}
		
	}
	
	@Test
	public
	void testDecideJobBadJobFaultException()
        throws Exception {
		
		List<JobView> jobs = transporter1.listJobs();
		// TESTS
		assertNotNull(transporter1);
		createJob(jobs, "T1","Lisboa","Porto","1/0", 11, JobStateView.REJECTED);
		createJob(jobs, "T1","Lisboa","Porto","1/1", 11, JobStateView.ACCEPTED);
		createJob(jobs, "T1","Lisboa","Porto","1/2", 11, JobStateView.ACCEPTED);

		try {
			transporter1.decideJob("1/0", false); 
		} catch(BadJobFault_Exception e) {
			// if it gets exception in first decideJob fails
			fail("wrong exception caught");
		}
		
		try {
			transporter1.decideJob("china", false);
			// if it doesn't get exception in second decideJob fails
			fail("didnt catch exception");
		} catch(BadJobFault_Exception e) {
			assertEquals("badJobFault", e.getMessage());
		}
		
		// verifications -> verifies expectations
		
	}
	

	
	// test jobStatus wrong id input should return null
	// job Status input should be (indexNum|TransporterNum)
	@Test
	public 
	void testJobStatusNullJobsList() { 
			
		JobView jv;
		// TESTS
		jv = transporter1.jobStatus("T1");
		
		// verifications -> verifies expectations or assertions
		
		
		assertNull("correct job status input",jv); // checks if NULL
		// nothing in list must be NULL
		
	}
	@Test
	public 
	void testJobStatus() { 
		// this tests JobStatus to check 
		// test data
		JobView j1;
		JobView j2;

		JobStateView js = JobStateView.ACCEPTED;
		JobView jv = new JobView();
		jv.setCompanyName("T1");
		jv.setJobDestination("Porto");
		jv.setJobIdentifier("1/1"); 
		jv.setJobOrigin("Lisboa");
		jv.setJobPrice(11);
		jv.setJobState(js);
		
		List<JobView> jobs = transporter1.listJobs();
		// TESTS
		
		createJob(jobs,"T2","Lisboa","Porto", "1/0", 12, JobStateView.ACCEPTED);
		createJob(jobs,"T1","Lisboa","Porto", "1/1", 11, JobStateView.ACCEPTED);
		j1 = transporter1.jobStatus("1/1"); 
		j2 = transporter1.jobStatus("id");
		
		// verifications -> verifies expectations
		
		assertNotNull(transporter1); // checks if NULL
		assertNotNull(j1); // checks if NULL
		assertEquals(jv.getCompanyName(),j1.getCompanyName());
		assertEquals(jv.getJobDestination(),j1.getJobDestination());
		assertEquals(jv.getJobIdentifier(),j1.getJobIdentifier());
		assertEquals(jv.getJobOrigin(),j1.getJobOrigin());
		assertEquals(jv.getJobPrice(),j1.getJobPrice());
		assertEquals(jv.getJobState(),j1.getJobState());
		assertNull(j2); // j2 must be null because id is wrong
	}
	
	public void createJob(List<JobView> jobs, String name, String origin, String destination,  String id, int preco,JobStateView state ) {
		JobView jv = new JobView();
		jv.setCompanyName(name);
		jv.setJobDestination(destination);
		jv.setJobIdentifier(id); // transporterNum/contratNum->starts at 0
		jv.setJobOrigin(origin);
		jv.setJobPrice(preco);
		jv.setJobState(state);
		jobs.add(jv);
	}
}