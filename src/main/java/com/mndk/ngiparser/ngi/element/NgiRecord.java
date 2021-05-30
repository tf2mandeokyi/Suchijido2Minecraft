package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.gattr.NgiShapeGAttribute;

public class NgiRecord<T extends NgiShapeGAttribute> {
	
	public final NgiLayer parent;
	public Object[] rowData;
	public T gAttribute;
	
	public NgiRecord(NgiLayer parent) {
		this.parent = parent;
	}
	
	public Object getRowData(String column) {
		int index = parent.header.getColumnIndex(column);
		if(index == -1) return null;
		return rowData[index];
	}

}
