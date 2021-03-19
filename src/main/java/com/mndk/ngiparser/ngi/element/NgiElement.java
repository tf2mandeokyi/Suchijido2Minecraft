package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;

public class NgiElement<T extends NgiElement.Attr> {
	
	public final NgiLayer parent;
	public Object[] rowData;
	public T attribute;
	
	public NgiElement(NgiLayer parent) {
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
