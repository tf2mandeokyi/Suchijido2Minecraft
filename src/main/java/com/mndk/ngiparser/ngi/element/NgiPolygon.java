package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.gattr.NgiRegionGAttribute;
import com.mndk.ngiparser.ngi.vertex.NgiVectorList;

public class NgiPolygon extends NgiRecord<NgiRegionGAttribute> {
	
	public NgiPolygon(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVectorList[] vertexData;

}
