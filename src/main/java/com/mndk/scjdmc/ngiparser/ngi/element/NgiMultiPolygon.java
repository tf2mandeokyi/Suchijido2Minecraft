package com.mndk.scjdmc.ngiparser.ngi.element;

import com.mndk.scjdmc.ngiparser.ngi.NgiLayer;
import com.mndk.scjdmc.ngiparser.ngi.gattr.NgiRegionGAttribute;
import com.mndk.scjdmc.ngiparser.ngi.vertex.NgiVectorList;

public class NgiMultiPolygon extends NgiRecord<NgiRegionGAttribute> {

	public NgiMultiPolygon(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVectorList[][] vertexData;
}
