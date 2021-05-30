package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.gattr.NgiLineGAttribute;
import com.mndk.ngiparser.ngi.vertex.NgiVectorList;

public class NgiLine extends NgiRecord<NgiLineGAttribute> {
	
	public NgiLine(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVectorList lineData;

}
