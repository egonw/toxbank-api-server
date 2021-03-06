package org.toxbank.rest.user.db;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.idea.modbcum.i.exceptions.AmbitException;
import net.idea.modbcum.i.query.QueryParam;

import org.toxbank.rest.user.DBUser;

/**
 * Retrieve references (by id or all)
 * @author nina
 *
 */
public class ReadUser<T>  extends ReadUserID<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6228939989116141217L;
	public enum fields {
		iduser {
			@Override
			public void setParam(DBUser protocol, ResultSet rs) throws SQLException {
				protocol.setID(rs.getInt(name()));
			}
			@Override
			public QueryParam getParam(DBUser protocol) {
				return new QueryParam<Integer>(Integer.class, (Integer)getValue(protocol));
			}	
			@Override
			public Object getValue(DBUser protocol) {
				return protocol==null?null:protocol.getID()>0?protocol.getID():null;
			}
			@Override
			public String toString() {
				return "URI";
			}
		},
	
		username {
			@Override
			public void setParam(DBUser protocol, ResultSet rs) throws SQLException {
				protocol.setUserName(rs.getString(name()));
			}
			@Override
			public Object getValue(DBUser protocol) {
				return protocol==null?null:protocol.getUserName();
			}
		},
		title {
			@Override
			public void setParam(DBUser protocol, ResultSet rs) throws SQLException {
				protocol.setTitle(rs.getString(name()));
			}		
			@Override
			public Object getValue(DBUser protocol) {
				return protocol==null?null:protocol.getTitle();
			}
		},
		firstname {
			@Override
			public void setParam(DBUser protocol, ResultSet rs) throws SQLException {
				protocol.setFirstname(rs.getString(name()));
			}		
			@Override
			public Object getValue(DBUser protocol) {
				return protocol==null?null:protocol.getFirstname();
			}
			public String getCondition() {
				return String.format(" %s regexp ? ",name());
			}
		},
		lastname {
			@Override
			public void setParam(DBUser protocol, ResultSet rs) throws SQLException {
				protocol.setLastname(rs.getString(name()));
			}		
			@Override
			public Object getValue(DBUser protocol) {
				return protocol==null?null:protocol.getLastname();
			}
			public String getCondition() {
				return String.format(" %s regexp ? ",name());
			}
		},	
		weblog {
			@Override
			public void setParam(DBUser user, ResultSet rs) throws SQLException {
				try {
					String n = rs.getString(name());
					user.setWeblog(n==null?null:new URL(n));}
				catch (Exception x) {throw new SQLException(x);}
			}		
			@Override
			public Object getValue(DBUser protocol) {
				return protocol==null?null:protocol.getWeblog();
			}			
		},
		homepage {
			@Override
			public void setParam(DBUser user, ResultSet rs) throws SQLException {
				try {
					String n = rs.getString(name());
					user.setHomepage(n==null?null:new URL(n));
				} catch (Exception x) {throw new SQLException(x);}
			}		
			@Override
			public Object getValue(DBUser protocol) {
				return protocol==null?null:protocol.getHomepage();
			}				
		}
	
		;
		public String getCondition() {
			return String.format(" %s = ? ",name());
		}
		public QueryParam getParam(DBUser protocol) {
			return new QueryParam<String>(String.class,  getValue(protocol).toString());
		}
		public Class getClassType(DBUser user) {
			return String.class;
		}
		public void setParam(DBUser protocol, ResultSet rs) throws SQLException {
			protocol.setLastname(rs.getString(name()));
		}		
		public Object getValue(DBUser protocol) {
			return protocol==null?null:protocol.getLastname();
		}
		
		public String getHTMLField(DBUser user) {
			Object value = getValue(user);
			return String.format("<input name='%s' type='text' size='40' value='%s'>\n",
					name(),getDescription(),value==null?"":value.toString());
		}
		public String getDescription() { return toString();}
		@Override
		public String toString() {
			String name= name();
			return  String.format("%s%s",
					name.substring(0,1).toUpperCase(),
					name.substring(1).toLowerCase());
		}

	}
	
	protected static String sql = 
		"SELECT iduser,username,title,firstname,lastname,institute,weblog,homepage from user %s %s";

	
	public ReadUser(DBUser user) {
		super();
		setValue(user);
	}

	public ReadUser(Integer id) {
		super();
		setValue(id==null?null:new DBUser(id));
	}
	
	public ReadUser() {
		this((Integer)null);
	}
	@Override
	protected String getSQLTemplate() {
		return sql;
	}
	public DBUser getObject(ResultSet rs) throws AmbitException {
		try {
			DBUser p =  new DBUser();
			for (fields field:fields.values()) try {
				field.setParam(p,rs);
				
			} catch (Exception x) {
				System.err.println(field);
				x.printStackTrace();
			}
			return p;
		} catch (Exception x) {
			return null;
		}
	}
	@Override
	public String toString() {
		return getValue()==null?"All users":String.format("User id=U%s",getValue().getID());
	}

}
