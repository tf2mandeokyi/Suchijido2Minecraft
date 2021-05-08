package com.mndk.shapefile.dbf;

public enum DBaseFieldType {
	CHARACTER('C'), DATE('D'), FLOAT('F'), LOGICAL('L'), MEMO('M'), NUMERIC('N');
	private final char character;
	private DBaseFieldType(char character) {
		this.character = character;
	}
	public static DBaseFieldType from(char c) {
		for(DBaseFieldType type : values()) {
			if(type.character == c) return type;
		}
		return null;
	}
	public static DBaseFieldType from(byte b) {
		return from((char) b);
	}
}