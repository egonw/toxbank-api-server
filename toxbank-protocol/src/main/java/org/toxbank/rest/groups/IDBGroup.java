package org.toxbank.rest.groups;

import java.io.Serializable;


public interface IDBGroup extends Serializable {
	public GroupType getGroupType();
	public void setGroupType(GroupType groupType);
	public String getTitle();
	public void setTitle(String title);
	public String getGroupName();
	public void setGroupName(String ldapgroup);
	public int getID();
	public void setID(int iD);
	public int parseURI(String baseReference);
}
