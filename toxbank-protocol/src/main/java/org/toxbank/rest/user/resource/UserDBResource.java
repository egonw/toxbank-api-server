package org.toxbank.rest.user.resource;

import java.sql.Connection;
import java.util.List;

import net.idea.modbcum.i.IQueryRetrieval;
import net.idea.modbcum.i.exceptions.AmbitException;
import net.idea.restnet.c.RepresentationConvertor;
import net.idea.restnet.c.StringConvertor;
import net.idea.restnet.c.task.CallableProtectedTask;
import net.idea.restnet.c.task.FactoryTaskConvertor;
import net.idea.restnet.c.task.TaskCreator;
import net.idea.restnet.db.DBConnection;
import net.idea.restnet.db.QueryResource;
import net.idea.restnet.db.QueryURIReporter;
import net.idea.restnet.db.convertors.OutputWriterConvertor;
import net.idea.restnet.db.convertors.RDFJenaConvertor;
import net.idea.restnet.i.task.ITaskStorage;
import net.idea.restnet.rdf.FactoryTaskConvertorRDF;
import net.toxbank.client.io.rdf.TOXBANK;

import org.apache.commons.fileupload.FileItem;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.toxbank.rest.FileResource;
import org.toxbank.rest.protocol.DBProtocol;
import org.toxbank.rest.protocol.resource.db.ProtocolQueryURIReporter;
import org.toxbank.rest.user.DBUser;
import org.toxbank.rest.user.db.ReadUser;

/**
 * Protocol resource
 * @author nina
 *
 * @param <Q>
 */
public class UserDBResource	extends QueryResource<ReadUser,DBUser> {
	public static final String resourceKey = "key";
	
	protected boolean singleItem = false;
	protected boolean editable = true;

	@Override
	public RepresentationConvertor createConvertor(Variant variant)
			throws AmbitException, ResourceException {
		/*
		if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
			
			return new StringConvertor(new PropertyValueReporter(),MediaType.TEXT_PLAIN);
			
		} else
		*/ 
		if (variant.getMediaType().equals(MediaType.TEXT_URI_LIST)) {
				return new StringConvertor(	
						new ProtocolQueryURIReporter(getRequest())
						,MediaType.TEXT_URI_LIST);
				
		} else if (variant.getMediaType().equals(MediaType.APPLICATION_RDF_XML) ||
					variant.getMediaType().equals(MediaType.APPLICATION_RDF_TURTLE) ||
					variant.getMediaType().equals(MediaType.TEXT_RDF_N3) ||
					variant.getMediaType().equals(MediaType.TEXT_RDF_NTRIPLES) ||
					variant.getMediaType().equals(MediaType.APPLICATION_JSON) ||
					variant.getMediaType().equals(MediaType.TEXT_CSV) 
					
					) {
				return new RDFJenaConvertor<DBUser, IQueryRetrieval<DBUser>>(
						new UserRDFReporter<IQueryRetrieval<DBUser>>(
								getRequest(),variant.getMediaType(),getDocumentation())
						,variant.getMediaType()) {
					@Override
					protected String getDefaultNameSpace() {
						return TOXBANK.URI;
					}					
				};
		} else if (variant.getMediaType().equals(MediaType.TEXT_HTML))
				return new OutputWriterConvertor(
						new UserHTMLReporter(getRequest(),!singleItem,editable),
						MediaType.TEXT_HTML);
		else throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
	protected ReadUser createQuery(Context context, Request request, Response response)
			throws ResourceException {
		Form form = request.getResourceRef().getQueryAsForm();
		Object search = null;
		try {
			search = form.getFirstValue("search").toString();
		} catch (Exception x) {
			search = null;
		}		
		try {
			String n = form.getFirstValue("new");
			editable = n==null?false:Boolean.parseBoolean(n);
		} catch (Exception x) {
			editable = false;
		}
		Object key = request.getAttributes().get(FileResource.resourceKey);		
		try {
			if (key==null) key = search;
			
			if (key==null) {
				ReadUser query = new ReadUser();
//				query.setFieldname(search.toString());
				return query;
			}			
			else {
				if (key.toString().startsWith("U")) {
					singleItem = true;
					return new ReadUser(new Integer(Reference.decode(key.toString().substring(1))));
				} else throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		}catch (ResourceException x) {
			throw x;
		} catch (Exception x) {
			throw new ResourceException(
					Status.CLIENT_ERROR_BAD_REQUEST,
					String.format("Invalid protocol id %d",key),
					x
					);
		}
	} 

	@Override
	protected QueryURIReporter<DBUser, ReadUser> getURUReporter(
			Request baseReference) throws ResourceException {
		return new UserURIReporter(getRequest());
	}

	@Override
	public String getConfigFile() {
		return "conf/tbprotocol-db.pref";
	}
	
	@Override
	protected boolean isAllowedMediaType(MediaType mediaType)
			throws ResourceException {
		return MediaType.MULTIPART_FORM_DATA.equals(mediaType);
	}

	@Override
	protected CallableProtectedTask<String> createCallable(Method method,
			List<FileItem> input, DBUser item) throws ResourceException {
		Connection conn = null;
		try {
			UserURIReporter r = new UserURIReporter(getRequest(),"");
			DBConnection dbc = new DBConnection(getApplication().getContext(),getConfigFile());
			conn = dbc.getConnection(getRequest());
			//return new CallableUserUpload(input,conn,r,getToken(),getRequest().getRootRef().toString());
			return null;
		} catch (Exception x) {
			try { conn.close(); } catch (Exception xx) {}
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,x);
		}

		/*
		Form form = new Form();
		form.add(PageParams.params.resulturi.name(),String.format("%s/ProtocolMockup",getRequest().getResourceRef()));
		form.add(PageParams.params.delay.name(),"1");
		return new CallableMockup(form,getToken());
		*/
	}
	
	@Override
	protected ReadUser createPOSTQuery(Context context, Request request,
			Response response) throws ResourceException {
		Object key = request.getAttributes().get(FileResource.resourceKey);		
		if (key==null) return null;//post allowed only on /protocol level, not on /protocol/id
		else throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}
	@Override
	protected FactoryTaskConvertor getFactoryTaskConvertor(ITaskStorage storage)
			throws ResourceException {
		return new FactoryTaskConvertorRDF(storage);
	}
	
	protected TaskCreator getTaskCreator(Form form, final Method method, boolean async, final Reference reference) throws Exception {
		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"Not multipart web form!");
	}
}