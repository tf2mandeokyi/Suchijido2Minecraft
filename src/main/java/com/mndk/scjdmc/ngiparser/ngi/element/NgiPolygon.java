package com.mndk.scjdmc.ngiparser.ngi.element;

import com.mndk.scjdmc.ngiparser.ngi.NgiLayer;
import com.mndk.scjdmc.ngiparser.ngi.gattr.NgiRegionGAttribute;
import com.mndk.scjdmc.ngiparser.ngi.vertex.NgiVectorList;

public class NgiPolygon extends NgiRecord<NgiRegionGAttribute> {
	
	public NgiPolygon(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVectorList[] vertexData;

}
