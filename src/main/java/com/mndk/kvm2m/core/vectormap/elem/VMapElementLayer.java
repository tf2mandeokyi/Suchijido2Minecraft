package com.mndk.kvm2m.core.vectormap.elem;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.vectormap.VMapElementType;
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
	
	public VMapElementLayer(NgiLayer layer, Grs80Projection projection) {
		super();
		this.type = VMapElementType.getTypeFromLayerName(layer.name);
		Collection<NgiElement<?>> ngiElements = layer.data.values();
		for(NgiElement<?> ngiElement : ngiElements) {
			VMapElement element = VMapElement.fromNgiElement(layer, ngiElement, projection);
			if(element == null) continue;
			
			if(element instanceof VMapContour) {
				
			}
		}
	}
	
	public VMapElementType getType() {
		return this.type;
	}
	
}
