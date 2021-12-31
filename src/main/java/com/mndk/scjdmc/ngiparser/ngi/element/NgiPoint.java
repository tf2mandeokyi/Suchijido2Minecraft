package com.mndk.scjdmc.ngiparser.ngi.element;

import com.mndk.scjdmc.ngiparser.ngi.NgiLayer;
import com.mndk.scjdmc.ngiparser.ngi.gattr.NgiPointGAttribute;
import com.mndk.scjdmc.ngiparser.ngi.vertex.NgiVector;

public class NgiPoint extends NgiRecord<NgiPointGAttribute> {
	
	public NgiPoint(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVector position;

}
