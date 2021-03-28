package com.mndk.kvm2m.core.vectorparser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.vectormap.VMapParserResult;
import com.mndk.kvm2m.core.vectormap.VMapUtils;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapContour;
import com.mndk.ngiparser.NgiParser;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.NgiParserResult;
import com.mndk.ngiparser.ngi.element.NgiElement;

public class NgiMapParser {

	public static VMapParserResult parse(File mapFile) throws IOException {

		VMapParserResult result = new VMapParserResult();

		String fileName = mapFile.getName();
		if(!FilenameUtils.isExtension(fileName, "ngi")) return null;
		Grs80Projection projection = VMapUtils.getProjectionFromMapId(fileName.substring(0, fileName.length() - 4));
		NgiParserResult parseResult = NgiParser.parse(mapFile.getAbsolutePath(), "MS949", true);
		
		Collection<NgiLayer> layers = parseResult.getLayers().values();
		for(NgiLayer layer : layers) {
			if(layer.header.dimensions != 2) continue;
			Collection<NgiElement<?>> elements = layer.data.values();
			for(NgiElement<?> ngiElement : elements) {
				VMapElement element = VMapElement.fromNgiElement(layer, ngiElement, projection);
				if(element == null) continue;
				
				if(element instanceof VMapContour) {
					
				}
			}
		}
		
		return result;
		
	}
	
	
}
