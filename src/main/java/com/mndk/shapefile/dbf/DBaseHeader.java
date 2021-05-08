package com.mndk.shapefile.dbf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import com.mndk.shapefile.util.ShapefileCustomInputStream;

public class DBaseHeader {

	
	public final int version;
	public final DBaseMemoPresence memoPresence;
	public final int sqlTablePresence;
	public final Date lastUpdate;
	public final int recordCount;
	public final short recordBytes;
	public final boolean incompleteTransaction, encrypted;
	public final boolean mdxPresence;
	public final byte languageDriverId;
	public final DBaseField[] fields;
	
	
	
	public DBaseHeader(ShapefileCustomInputStream is) throws IOException {
		
		byte byte0 = (byte) is.read();
		
		this.version = byte0 & 0b111;
		
		this.sqlTablePresence = (byte0 & 0b1110000) >> 4;
		
		boolean memoPresence_dos = (byte0 & 0b1000) == 0b1000;
		boolean memoPresence_any = (byte0 & 0b10000000) == 0b10000000;
		this.memoPresence = memoPresence_any ? (memoPresence_dos ? DBaseMemoPresence.DOS : DBaseMemoPresence.PLUS) : DBaseMemoPresence.NONE;
		
		int year = is.read();
		int month = is.read();
		int date = is.read();
		this.lastUpdate = new GregorianCalendar(year + 1900, month, date).getTime();
		
		this.recordCount = is.readIntLittle();
		int headerBytes = is.readShortLittle();
		int fieldCount = (headerBytes - 33) / 32; // 
		this.recordBytes = is.readShortLittle();
		
		/*short reserved0 = */ is.readShortLittle();
		
		this.incompleteTransaction = is.read() == 1;
		this.encrypted = is.read() == 1;
		
		/*uint12 multi_user_env = */ is.readLongLittle(); is.readIntLittle();
		
		this.mdxPresence = is.read() == 1;
		
		this.languageDriverId = (byte) is.read(); // TODO Make an enum of this I guess
		
		/*short reserved1 = */ is.readShortLittle();
		
		this.fields = new DBaseField[fieldCount];
		for(int i = 0; i < fieldCount; i++) {
			this.fields[i] = new DBaseField(is);
		}
		
		is.read(); // Field descriptor array terminator (0x0D)
	}
	
	
	
	public DBaseField getFieldByName(String fieldName) {
		for(DBaseField field : fields) {
			if(field.name.equals(fieldName)) {
				return field;
			}
		}
		return null;
	}
	
	
	
	public int getFieldIndexByName(String fieldName) {
		for(int i = 0; i < fields.length; i++) {
			DBaseField field = fields[i];
			if(field.name.equals(fieldName)) {
				return i;
			}
		}
		return -1;
	}
	
	
	
	@Override
	public String toString() {
		return "DBaseHeader{\n  version=" + version + ",\n  lastUpdate=" + lastUpdate + ",\n  recordCount=" + recordCount + ",\n  recordBytes=" + recordBytes + ",\n  fields=" + Arrays.toString(fields) + ",\n  ...\n}";
	}
	
}
