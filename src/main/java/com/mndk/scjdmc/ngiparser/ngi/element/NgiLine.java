package com.mndk.scjdmc.ngiparser.ngi.element;

import com.mndk.scjdmc.ngiparser.ngi.NgiLayer;
import com.mndk.scjdmc.ngiparser.ngi.gattr.NgiLineGAttribute;
import com.mndk.scjdmc.ngiparser.ngi.vertex.NgiVectorList;

public class NgiLine extends NgiRecord<NgiLineGAttribute> {
	
	public NgiLine(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVectorList lineData;

}
