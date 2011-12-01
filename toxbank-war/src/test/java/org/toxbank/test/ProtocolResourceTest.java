package org.toxbank.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;
import net.toxbank.client.io.rdf.ProtocolIO;
import net.toxbank.client.resource.Protocol;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ITable;
import org.junit.Test;
import org.opentox.dsl.task.RemoteTask;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.toxbank.resource.Resources;
import org.toxbank.rest.protocol.db.ReadProtocol;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * test for {@link PropertyResource}
 * @author nina
 *
 */
public class ProtocolResourceTest extends ResourceTest {
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		setUpDatabase(dbFile);
		
	}
	@Override
	public String getTestURI() {
		return String.format("http://localhost:%d%s/P1", port,Resources.protocol);
	}
	
	@Test
	public void testURI() throws Exception {
	//	setUpDatabase(dbFile);
		testGet(getTestURI(),MediaType.TEXT_URI_LIST);
	}
	@Override
	public boolean verifyResponseURI(String uri, MediaType media, InputStream in)
			throws Exception {
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line = null;
		int count = 0;
		while ((line = r.readLine())!= null) {
			Assert.assertEquals(
					String.format("http://localhost:%d%s/P1",port,Resources.protocol)
							, line);
			count++;
		}
		return count==1;
	}	
	
	@Test
	public void testRDF() throws Exception {
		testGet(getTestURI(),MediaType.APPLICATION_RDF_XML);
	}
	
	@Override
	public OntModel verifyResponseRDFXML(String uri, MediaType media,
			InputStream in) throws Exception {
		
		OntModel model = ModelFactory.createOntologyModel();
		model.read(in,null);
		
		ProtocolIO ioClass = new ProtocolIO();
		List<Protocol> protocols = ioClass.fromJena(model);
		Assert.assertEquals(1,protocols.size());
		Assert.assertEquals("http://localhost:8181/protocol/P1",protocols.get(0).getResourceURL().toString());
		Assert.assertEquals("SEURAT-234", protocols.get(0).getIdentifier());
		Assert.assertEquals("Very important protocol", protocols.get(0).getTitle());
		Assert.assertNotNull(protocols.get(0).getAbstract());
		
		Assert.assertNotNull(protocols.get(0).getOwner());
		Assert.assertEquals(String.format("http://localhost:%d%s/U1",port,Resources.user),
				protocols.get(0).getOwner().getResourceURL().toString());
		Assert.assertEquals("abcdef", protocols.get(0).getOwner().getFirstname());
		return model;
	}
	
	/*
	@Test
	public void testQueryName() throws Exception {
		RDFPropertyIterator iterator = new RDFPropertyIterator(new Reference(
				String.format("http://localhost:%d%s?%s=%s", port,
						PropertyResource.featuredef,QueryResource.search_param,"Property")
				));
		iterator.setCloseModel(true);
		iterator.setBaseReference(new Reference(String.format("http://localhost:%d", port)));
		while (iterator.hasNext()) {
			Property p = iterator.next();
			Assert.assertTrue(p.getName().startsWith("Property"));

		}
		iterator.close();
	}	

	@Test
	public void testRDFXML() throws Exception {
		RDFPropertyIterator iterator = new RDFPropertyIterator(new Reference(getTestURI()));
		iterator.setBaseReference(new Reference(String.format("http://localhost:%d", port)));
		while (iterator.hasNext()) {
			
			Property p = iterator.next();
			Assert.assertEquals("Property 1",p.getName());
			Assert.assertEquals(1,p.getId());
		}
		iterator.close();
	}	
	
	/*
	@Test
	public void testHTML() throws Exception {
		testGet(getTestURI(),MediaType.TEXT_HTML);
	}
	@Override
	public boolean verifyResponseHTML(String uri, MediaType media, InputStream in)
			throws Exception {
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line = null;
		int count = 0;
		while ((line = r.readLine())!= null) {

			count++;
		}
		return count>1;
	}
	
	*/

	/*
	@Test
	public void testDeleteEntry() throws Exception {
		
		OTFeature feature = OTFeature.feature(String.format("http://localhost:%d%s/1", port,PropertyResource.featuredef));
		feature.delete();
		IDatabaseConnection c = getConnection();	
		ITable table = 	c.createQueryTable("EXPECTED","SELECT * FROM properties where idproperty=1");
		Assert.assertEquals(0,table.getRowCount());
		c.close();
	}	
	
	@Test
	public void testCopyEntry() throws Exception {
		
		Form form = new Form();  
		form.add(OpenTox.params.feature_uris.toString(),String.format("http://localhost:%d%s/1", port,PropertyResource.featuredef));
		
		Response response =  testPost(
					String.format("http://localhost:%d%s", port,PropertyResource.featuredef),
					MediaType.APPLICATION_RDF_XML,
					form.getWebRepresentation());
		Assert.assertEquals(Status.SUCCESS_OK, response.getStatus());
		Assert.assertEquals("http://localhost:8181/feature/1",response.getLocationRef().toString());
		System.out.println(response.getLocationRef());
		IDatabaseConnection c = getConnection();	
		ITable table = 	c.createQueryTable("EXPECTED","SELECT * FROM properties");
		Assert.assertEquals(3,table.getRowCount());
		c.close();
	}	
	@Test
	public void testCopyEntry1() throws Exception {
		OntModel model = OT.createModel();
		Property p = new Property(null);
		p.setId(1);
		
		Request q = new Request();
		q.setRootRef(new Reference(String.format("http://localhost:%d", port)));
		PropertyURIReporter reporter = new PropertyURIReporter(q,null);
		
		PropertyRDFReporter.addToModel(model, 
				p,
				reporter,
				new ReferenceURIReporter());
		StringWriter writer = new StringWriter();
		model.write(writer,"RDF/XML");
		
		Form form = new Form();  
		form.add(OpenTox.params.feature_uris.toString(),String.format("http://localhost:%d%s/1", port,PropertyResource.featuredef));
		
		Response response =  testPost(
					String.format("http://localhost:%d%s", port,PropertyResource.featuredef),
					MediaType.APPLICATION_RDF_XML,
					writer.toString());
		Assert.assertEquals(Status.SUCCESS_OK, response.getStatus());
		
		IDatabaseConnection c = getConnection();	
		ITable table = 	c.createQueryTable("EXPECTED","SELECT * FROM properties");
		Assert.assertEquals(3,table.getRowCount());
		c.close();
	}		
	
	@Test
	public void testCreateEntry2() throws Exception {
		
		StringWriter writer = new StringWriter();
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("feature.rdf")));
        String line;
        while ((line = br.readLine()) != null) {
        	writer.append(line);
        }
        br.close();    		
		
		Response response =  testPost(
					String.format("http://localhost:%d%s", port,PropertyResource.featuredef),
					MediaType.APPLICATION_RDF_XML,
					writer.toString());
		Assert.assertEquals(Status.SUCCESS_OK, response.getStatus());
		Assert.assertEquals("http://localhost:8181/feature/4", response.getLocationRef().toString());
		
        IDatabaseConnection c = getConnection();	
		ITable table = 	c.createQueryTable("EXPECTED","SELECT * FROM properties join catalog_references using(idreference) where name='cas' and comments='CasRN' and title='http://my.resource.org' and url='Default'");
		Assert.assertEquals(1,table.getRowCount());
		c.close();
	}		
	
	@Test
	public void testCreateEntry3() throws Exception {
		
		StringWriter writer = new StringWriter();
        BufferedReader br = new BufferedReader(
        		new InputStreamReader(getClass().getClassLoader().getResourceAsStream("feature1.rdf")));
        String line;
        while ((line = br.readLine()) != null) {
        	writer.append(line);
        }
        br.close();    		
		
		Response response =  testPost(
					String.format("http://localhost:%d%s", port,PropertyResource.featuredef),
					MediaType.APPLICATION_RDF_XML,
					writer.toString());
		Assert.assertEquals(Status.SUCCESS_OK, response.getStatus());
		Assert.assertEquals(response.getLocationRef().toString(),"http://localhost:8181/feature/4");
		
		//weird nondeterministic error in ambit.uni-plovdiv.bg
		for (int i=0; i < 100;i++) {
			response =  testPost(
					String.format("http://localhost:%d%s", port,PropertyResource.featuredef),
					//String.format("http://localhost:8080/ambit2-www%s",PropertyResource.featuredef),
					//String.format("http://ambit.uni-plovdiv.bg:8080/ambit2%s",PropertyResource.featuredef),
					MediaType.APPLICATION_RDF_XML,
					writer.toString());
			//System.out.println(response.getStatus());
			Assert.assertEquals(Status.SUCCESS_OK, response.getStatus());
			//Assert.assertEquals(response.getLocationRef().toString(),"http://localhost:8181/feature/http%3A%2F%2Fother.com%2Ffeature%2F200Default");
			
		}
        IDatabaseConnection c = getConnection();	
		ITable table = 	c.createQueryTable("EXPECTED","SELECT * FROM properties where name='http://other.com/feature/200'");
		Assert.assertEquals(1,table.getRowCount());
		c.close();
	}	
	
	*/
	/*
	@Test
	public void testGetJavaObject() throws Exception {
		testGetJavaObject(String.format("http://localhost:%d%s?%s=%s", port,IProtocol.resource,
				OpenTox.params.sameas.toString(),Reference.encode(Property.opentox_CAS)),
				MediaType.APPLICATION_JAVA_OBJECT,org.restlet.data.Status.SUCCESS_OK);
	}
	
	@Override
	public Object verifyResponseJavaObject(String uri, MediaType media,
			Representation rep) throws Exception {
		Object o = super.verifyResponseJavaObject(uri, media, rep);
		Assert.assertTrue(o instanceof RetrieveFieldNamesByAlias);
		RetrieveFieldNamesByAlias query = (RetrieveFieldNamesByAlias)o;
		Assert.assertEquals(Property.opentox_CAS,query.getValue());
		return o;
	}
	*/
	
	@Override
	public Object verifyResponseJavaObject(String uri, MediaType media,
			Representation rep) throws Exception {
		Object o = super.verifyResponseJavaObject(uri, media, rep);
		Assert.assertTrue(o instanceof ReadProtocol);

		return o;
	}
	
	@Test
	public void testCreateEntryFromMultipartWeb() throws Exception {
		URL url = getClass().getClassLoader().getResource("org/toxbank/protocol/protocol-sample.pdf");
		File file = new File(url.getFile());
		
		String[] names = new String[ReadProtocol.fields.values().length];
		String[] values = new String[ReadProtocol.fields.values().length];
		int i=0;
		for (ReadProtocol.fields field : ReadProtocol.fields.values()) {
			switch (field) {
			case idprotocol: continue;
			case filename: continue;
			case iduser: { 
				values[i] = String.format("http://localhost:%d%s/%s",port,Resources.user,"U1");
				break;
			}
			case project: {
				values[i] = String.format("http://localhost:%d%s/%s",port,Resources.project,"G1");
				break;
			}
			case organisation: {
				values[i] = String.format("http://localhost:%d%s/%s",port,Resources.organisation,"G2");
				break;
			}
			default: {
				values[i] = field.name();
			}
			}
			names[i] = field.name();
			
			i++;
		}
		Representation rep = getMultipartWebFormRepresentation(names,values,file,MediaType.APPLICATION_PDF.toString());
		
        IDatabaseConnection c = getConnection();	
		ITable table = 	c.createQueryTable("EXPECTED","SELECT * FROM protocol");
		Assert.assertEquals(2,table.getRowCount());
		c.close();

		RemoteTask task = testAsyncPoll(new Reference(String.format("http://localhost:%d%s", port,
				Resources.protocol)),
				MediaType.TEXT_URI_LIST, rep,
				Method.POST);
		//wait to complete
		while (!task.isDone()) {
			task.poll();
			Thread.sleep(100);
			Thread.yield();
		}
		Assert.assertTrue(task.getResult().toString().startsWith(String.format("http://localhost:%d/protocol/P",port)));

        c = getConnection();	
		table = 	c.createQueryTable("EXPECTED","SELECT * FROM protocol");
		Assert.assertEquals(3,table.getRowCount());
		table = 	c.createQueryTable("EXPECTED","SELECT idprotocol,filename from protocol where idprotocol>2");
		Assert.assertEquals(1,table.getRowCount());
		File f = new File(new URI(table.getValue(0,"filename").toString()));
		//System.out.println(f);
		Assert.assertTrue(f.exists());
		f.delete();

		c.close();

	}	
}
