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
		
		transporter1.clearJobs();
		transporter2.clearJobs();
    }

    @AfterClass
    public static void oneTimeTearDown() {
    	transporter1.clearJobs();
    	transporter2.clearJobs();
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
    	transporter1.clearJobs();
    	transporter2.clearJobs();
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
    public void basicTransporterTests() {
    	JobView job1 = null, job2 = null;
    	try {
	    	job1 = transporter1.requestJob("Lisboa", "Coimbra", 20);
	    	job2 = transporter1.requestJob("Lisboa", "Coimbra", 20);
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
    	assertEquals("Testes basicos do transporter, origem errada","Lisboa" ,job1.getJobOrigin());
    	assertEquals("Testes basicos do transporter, destino errado","Coimbra" ,job1.getJobDestination());
    	assertEquals("Testes basicos do transporter, estado errado",JobStateView.PROPOSED ,job1.getJobState());
    	assertEquals("Testes basicos do transporter, preco errado" ,21 ,job1.getJobPrice());
    	assertEquals("Testes basicos do transporter, companhia errada" ,"UpaTransporter1" ,job1.getCompanyName());
    	assertEquals("Testes basicos do transporter, identificador errado","1/0" ,job1.getJobIdentifier());
    	
    	assertEquals("Testes basicos do transporter, identificador errado","1/1" ,job2.getJobIdentifier());
    	
    	
    }
    
    
    @Test
    public void priceTests() throws Exception{
    	JobView job1 = null, job2 = null, job3 = null, job4 = null, job5 = null, job6 = null, job7 = null, job8 = null, job9 = null;
    	
    	try {
	    	job1 = transporter1.requestJob("Lisboa", "Coimbra", 0);  //testa com preco 0 em trasnportadora impar
	    	job2 = transporter2.requestJob("Lisboa", "Coimbra", 0);	//testa com preco 0 em transportadora par
	    	job3 = transporter1.requestJob("Lisboa", "Coimbra",	101); // testa com preco maior que 100
	    	job4 = transporter2.requestJob("Lisboa", "Coimbra", 9); // testa com preco menor que 10
	    	job5 = transporter1.requestJob("Lisboa", "Coimbra", 9); // testa com preco menor que 10 
	    	job6 = transporter2.requestJob("Lisboa", "Coimbra", 21); // testa com preco impar maior que 10 em transportadora par 
	    	job7 = transporter1.requestJob("Lisboa", "Coimbra", 21); // testa com preco impar maior que 10 em transportadora impar 
	    	job8 = transporter2.requestJob("Lisboa", "Coimbra", 20); // testa com preco par maior que 10 em transportadora par 
	    	job9 = transporter1.requestJob("Lisboa", "Coimbra", 20); // testa com preco par maio que 10 em trasnportadora impar 
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
    	assertEquals("Teste de preco, preco errado", 0, job1.getJobPrice());
    	assertEquals("Teste de preco, preco errado", 0, job2.getJobPrice());
    	assertNull("Teste de preco, esperado null",job3); // quando o preco e maior que 100 e returnado null
    	assertEquals("Teste de preco, preco errado", 8, job4.getJobPrice());
    	assertEquals("Teste de preco, preco errado", 8, job5.getJobPrice());
    	assertEquals("Teste de preco, preco errado", 22, job6.getJobPrice());
    	assertEquals("Teste de preco, preco errado", 20, job7.getJobPrice());
    	assertEquals("Teste de preco, preco errado", 19, job8.getJobPrice());
    	assertEquals("Teste de preco, preco errado", 21, job9.getJobPrice()); 
    	
    	try {
	    	job1 = transporter1.requestJob("Lisboa", "Coimbra", -1);  //testa o lancamento de excepcao quando o preco maximo e menor que 0 
	    	fail("Teste de localizacao, nao lancou excepcao");
    	} catch(BadPriceFault_Exception e) {
    		assertEquals("Preco incorreto", e.getMessage());
    	}
    }
     
    @Test
    public void locationTest() throws Exception{
    	JobView job1 = null, job2 = null, job3 = null, job4 = null;
    	
    	try {
	    	job1 = transporter1.requestJob("Lisboa", "Faro", 20);  //para ver se  marca um transporte normal nas zonas operadoas por transportadoras impares
	    	job2 = transporter2.requestJob("Lisboa", "Porto", 20);	//para ver se  marca um transporte normal nas zonas operadoas por transportadoras pares
	    	job3 = transporter1.requestJob("Lisboa", "Porto", 20); //para ver se nao marca um trasnporte normal nas zonas nao operadoas por transportadoras impares
	    	job4 = transporter2.requestJob("Lisboa", "Faro", 20);  //para ver se nao marca um trasnporte normal nas zonas nao operadoas por transportadoras pares 
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
    	assertNotNull("Teste de localizacao, esperado nao null", job1);
    	assertNotNull("Teste de localizacao, esperado nao null", job2);
    	assertNull("Teste de localizacao, esperado null", job3);
    	assertNull("Teste de localizacao, esperado null", job4);
    	
    	try {
	    	job1 = transporter1.requestJob("Lisboa", "Paris", 20);  //testa o lancamento de excepcao quando ha uma regiao desconhecida 
	    	fail("Teste de localizacao, nao lancou excepcao");
    	} catch(BadLocationFault_Exception e) {
    		assertEquals("Localizacao nao existe", e.getMessage());
    	}
    	
    }
    
    @Test
    public void basicDecideTest() {
    	JobView job1 = null, job2 = null;
    	
    	try {
	    	job1 = transporter1.requestJob("Lisboa", "Coimbra", 20); 
	    	job2 = transporter1.requestJob("Lisboa", "Coimbra", 20);
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
    	
    	try {	
    		job1 = transporter1.decideJob(job1.getJobIdentifier(), true);
    		job2 = transporter1.decideJob(job2.getJobIdentifier(), false);
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
    	
    	assertEquals("Decidejob teste basico, mudanca de estado errada", JobStateView.ACCEPTED, job1.getJobState());
    	assertEquals("Decidejob teste basico, mudanca de estado errada", JobStateView.REJECTED, job2.getJobState());
    }
    
    @Test
    public void stateVariationTest() {
    	JobView job1 = null, job2 = null;
    	
    	try {
	    	job1 = transporter1.requestJob("Lisboa", "Coimbra", 20); 
	    	job2 = transporter1.requestJob("Lisboa", "Coimbra", 20);
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
   
    	try {	
    		job1 = transporter1.decideJob(job1.getJobIdentifier(), true);
    		job2 = transporter1.decideJob(job2.getJobIdentifier(), true);
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
    	
    	
    	try {
			  Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.printf("Caught exception: %s%n", e);
			    e.printStackTrace();
			}
    	 
    	
    	job1 = transporter1.jobStatus(job1.getJobIdentifier());
    	job2 = transporter1.jobStatus(job2.getJobIdentifier());
    	
    	
    	
    	if(JobStateView.ACCEPTED.equals(job1.getJobState())) { // se o estado for diferente de accepted entao o estado mudou
    		fail("Teste de vaiacao do state, o state nao mudou ao longo do tempo" + job1.getJobIdentifier());
    	}
    	if(JobStateView.ACCEPTED.equals(job2.getJobState())) {
    		fail("Teste de variacao do state, o state nao mudou ao longo do tempo 2");
    	}
    }
    
    @Test
    public void decideExceptionTest() {
    	JobView job1 = null;
    	
    	try {
	    	job1 = transporter1.requestJob("Lisboa", "Coimbra", 20); 
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
    	
    	try{
    		job1 = transporter1.decideJob("1/1", true);
    		fail("Teste da excepcao do decideJob, excepcao nao foi mandada");
    	} catch(BadJobFault_Exception e) {
    		assertEquals("badJobFault", e.getMessage());
    	}
    }
    
    @Test
    public void jobStatusTest() {
    	JobView job1 = null, job2 = null;
    	
    	try {
	    	job1 = transporter1.requestJob("Lisboa", "Coimbra", 20); 
    	} catch(Exception e) {
    		fail(e.getMessage());
    	}
    	
    	job2 = transporter1.jobStatus(job1.getJobIdentifier());
    	assertNotNull("Teste do job status, deu null ",job2);
    	assertEquals("Teste do job status, o jobView nao e o mesmo", job1.getJobIdentifier(), job2.getJobIdentifier()); // ve se o job view e o mesmo
    	
    	job2 = transporter1.jobStatus("1/1");
    	assertNull("Teste do job status, esperado null", job2);
    	}
}