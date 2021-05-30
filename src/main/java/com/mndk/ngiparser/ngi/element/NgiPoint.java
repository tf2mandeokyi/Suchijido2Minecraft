package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.gattr.NgiPointGAttribute;
import com.mndk.ngiparser.ngi.vertex.NgiVector;

public class NgiPoint extends NgiRecord<NgiPointGAttribute> {
	
	public NgiPoint(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVector position;

}
