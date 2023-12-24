package com.mndk.scjdmc.ngiparser.nda;

public class NdaDataColumn {
	
	
	public final String name;
	public final String type;
	@Deprecated
	public final int size; // Why do you even need this lmao
	public final int unknownInt;
	public final boolean unknownBoolean;
	
	
	public NdaDataColumn(Object[] args) {
		this.name = (String) args[0];
		this.type = (String) args[1];
		this.size = (Integer) args[2];
		this.unknownInt = (Integer) args[3];
		this.unknownBoolean = (Boolean) args[4];
	}
}
