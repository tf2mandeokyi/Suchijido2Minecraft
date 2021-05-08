package com.mndk.shapefile.dbf;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.mndk.shapefile.util.ShapefileCustomInputStream;

public class DBaseRecord {

	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("YYYYMMDD");
	
	
	
	DBaseHeader parent;
	Object[] data;
	boolean active;
	
	
	
	public DBaseRecord(DBaseHeader parent, ShapefileCustomInputStream is) throws IOException {
		
		byte deletionByte = (byte) is.read(); // 1 byte deletion flag.
		if(deletionByte == ' ') active = true;
		else if(deletionByte == '*') active = false;
		else throw new IOException("Invalid deletion byte");
		
		this.parent = parent;
		int length = parent.fields.length;
		this.data = new Object[length];
		
		for(int i = 0; i < length; i++) {
			DBaseField field = parent.fields[i];
			
			String stringData = is.readString(field.length);
			
			switch(field.type) {
				case CHARACTER:
				case MEMO:
					this.data[i] = stringData.trim();
					break;
				case DATE:
					try { 
						this.data[i] = DATE_FORMAT.parse(stringData); 
					} catch(ParseException e) { 
						throw new IOException("Invalid date format");
					}
					break;
				case FLOAT:
				case NUMERIC:
					try {
						this.data[i] = Double.parseDouble(stringData);
					} catch(NumberFormatException e) {
						throw new IOException("Invalid number format");
					}
					break;
				case LOGICAL:
					switch(stringData.toLowerCase().charAt(0)) {
						case 'Y': case 'y': case 'T': case 't':
							this.data[i] = true;
							break;
						case 'N': case 'n': case 'F': case 'f':
							this.data[i] = false;
							break;
						case '?':
							this.data[i] = null;
							break;
						default:
							throw new IOException("Invalid logical character");
					}
					break;
				default:
					throw new IOException("Invalid field type");
			}
		}
		
	}
	
	
	
	public Object getDataByField(String fieldName) {
		return this.data[parent.getFieldIndexByName(fieldName)];
	}
	
	
	
	@Override
	public String toString() {
		String result = "DBaseRecord[";
		for(int i = 0; i < parent.fields.length; i++) {
			DBaseField field = parent.fields[i];
			result += field.name + "=" + this.data[i];
			if(i != parent.fields.length - 1) {
				result += ", ";
			}
		}
		return result + "]";
	}
	
}
