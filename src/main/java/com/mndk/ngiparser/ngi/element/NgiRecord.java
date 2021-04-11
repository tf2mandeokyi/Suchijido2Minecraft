package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;

public class NgiRecord<T extends NgiRecord.Attr> {
	
	public final NgiLayer parent;
	public Object[] rowData;
	public T attribute;
	
	public NgiRecord(NgiLayer parent) {
		this.parent = parent;
	}
	
	public Object getRowData(String column) {
		int index = parent.header.getColumnIndex(column);
		if(index == -1) return null;
		return rowData[index];
	}
	
	public static interface Attr { 
		void from(String[] args);
	}
}
