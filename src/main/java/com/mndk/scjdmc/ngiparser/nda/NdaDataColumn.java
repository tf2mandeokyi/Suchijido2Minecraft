package com.mndk.scjdmc.ngiparser.nda;

public class NdaDataColumn {
	
	
	public String name;
	public String type; 
	@Deprecated
	public int size; // Why do you even need this lmao
	public int unknownInt; // TODO figure out what this variable is
	public boolean unknownBoolean; // TODO figure out what this variable is
	
	
	public NdaDataColumn(Object[] args) {
		this.name = (String) args[0];
		this.type = (String) args[1];
		this.size = (Integer) args[2];
		this.unknownInt = (Integer) args[3];
		this.unknownBoolean = (Boolean) args[4];
	}
}
