package org.toxbank.rest.groups;

import java.sql.Connection;

import net.idea.modbcum.i.IQueryRetrieval;
import net.idea.modbcum.i.query.IQueryUpdate;
import net.idea.restnet.db.update.CallableDBUpdateTask;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.toxbank.rest.groups.db.CreateGroup;
import org.toxbank.rest.groups.db.DeleteGroup;
import org.toxbank.rest.groups.db.UpdateGroup;
import org.toxbank.rest.groups.resource.GroupQueryURIReporter;

public class CallableGroupCreator extends CallableDBUpdateTask<IDBGroup,Form,String> {
	protected GroupQueryURIReporter<IQueryRetrieval<IDBGroup>> reporter;
	protected GroupType type;
	
	protected IDBGroup item;
	
	public CallableGroupCreator(
						Method method,
						IDBGroup item,
						GroupType type,
						GroupQueryURIReporter<IQueryRetrieval<IDBGroup>> reporter,
						Form input, Connection connection,String token)  {
		super(method,input,connection,token);
		this.method = method;
		this.item = item;
		this.reporter = reporter;
		this.type = type;
	}

	@Override
	protected IDBGroup getTarget(Form input) throws Exception {
		if (input==null) {
			return item;
		} else {
			DBGroup user = new DBGroup(type);
			user.setTitle(input.getFirstValue(DBGroup.fields.name.name()));
			user.setGroupName(input.getFirstValue(DBGroup.fields.ldapgroup.name()));
	 		return user;
		}
	}

	@Override
	protected IQueryUpdate<Object, IDBGroup> createUpdate(IDBGroup group)
			throws Exception {
		if (Method.POST.equals(method)) return new CreateGroup(group);
		else if (Method.DELETE.equals(method)) return new DeleteGroup(group);
		else if (Method.PUT.equals(method)) return new UpdateGroup(group);
		throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
	}

	@Override
	protected String getURI(IDBGroup user) throws Exception {
		return reporter.getURI(user);
	}

	

}
