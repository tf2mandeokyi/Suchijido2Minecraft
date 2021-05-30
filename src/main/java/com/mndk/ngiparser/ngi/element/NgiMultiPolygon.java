package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.gattr.NgiRegionGAttribute;
import com.mndk.ngiparser.ngi.vertex.NgiVectorList;

public class NgiMultiPolygon extends NgiRecord<NgiRegionGAttribute> {

	public NgiMultiPolygon(NgiLayer parent) {
		super(parent);
	}
	
	public NgiVectorList[][] vertexData;
}
