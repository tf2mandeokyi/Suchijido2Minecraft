package com.mndk.kvm2m.core.vectorparser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.vectormap.VMapElementType;
import com.mndk.kvm2m.core.vectormap.VMapParserResult;
import com.mndk.kvm2m.core.vectormap.VMapUtils;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;
import com.mndk.ngiparser.NgiParser;
import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.NgiParserResult;

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
			VMapElementLayer elementLayer = VMapElementLayer.fromNgiLayer(layer, projection, result.getElevationPoints());
			result.addElement(elementLayer);
		}
		
		return result;
		
	}
	
	public static void main(String[] args) throws IOException {
		VMapParserResult result = NgiMapParser.parse(new File("376081986.ngi"));
		for(VMapElementLayer layer : result.getElementLayers()) {
			if(layer.getType() != VMapElementType.건물) continue;
			System.out.println(layer.getType());
			for(VMapElement element : layer) {
				System.out.println("  " + element);
			}
		}
	}
	
}
