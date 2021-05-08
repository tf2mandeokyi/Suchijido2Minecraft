package com.mndk.kvm2m.core.vectormap.elem;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VMapElementStyleSelector;

public class VMapContour extends VMapLine {

	public final int elevation;
	
	public VMapContour(VMapElementLayer parent, Vector2DH[] vertexes, Object[] rowData) {
		super(parent, new Vector2DH[][] {vertexes}, rowData, false);
		this.elevation = VMapElementStyleSelector.getStyle(this)[0].y;
	}
}
