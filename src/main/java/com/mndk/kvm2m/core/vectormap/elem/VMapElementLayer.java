package com.mndk.kvm2m.core.vectormap.elem;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VMapElementType;
import com.mndk.kvm2m.core.vectormap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapContour;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.element.NgiElement;

@SuppressWarnings("serial")
public class VMapElementLayer extends HashSet<VMapElement> {

	private final VMapElementType type;
	
	public VMapElementLayer(List<VMapElement> elements, VMapElementType type) {
		super(elements);
		this.type = type;
	}
	
	public VMapElementLayer(VMapElementType type) {
		super();
		this.type = type;
	}
	
	public static VMapElementLayer fromNgiLayer(NgiLayer ngiLayer, Grs80Projection projection, List<Vector2DH> elevPoints) {
		VMapElementType type = VMapElementType.getTypeFromLayerName(ngiLayer.name);
		VMapElementLayer elementLayer = new VMapElementLayer(type);
		
		Collection<NgiElement<?>> ngiElements = ngiLayer.data.values();
		for(NgiElement<?> ngiElement : ngiElements) {
			VMapElement element = VMapElement.fromNgiElement(elementLayer, ngiElement, projection);
			if(element == null) continue;
			elementLayer.add(element);			
			if(element instanceof VMapContour) {
				VMapContour contour = (VMapContour) element;
				for(Vector2DH[] va : contour.getVertexList()) for(Vector2DH v : va) {
					elevPoints.add(v.withHeight(contour.getElevation()));
				}
			}
			else if(element instanceof VMapElevationPoint) {
				VMapElevationPoint elevPoint = (VMapElevationPoint) element;
				elevPoints.add(elevPoint.toVector());
			}
		}
		
		return elementLayer;
	}
	
	public VMapElementType getType() {
		return this.type;
	}
	
}
