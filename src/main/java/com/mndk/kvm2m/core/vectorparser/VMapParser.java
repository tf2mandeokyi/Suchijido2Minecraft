package com.mndk.kvm2m.core.vectorparser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VMapParserResult;
import com.mndk.kvm2m.core.vectormap.VMapUtils;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapContour;

import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public abstract class VMapParser {
	
	public abstract VMapParserResult parse(File mapFile, GeographicProjection worldProjection) throws IOException;
	
	protected static Vector2DH projectGrs80CoordToWorldCoord(Grs80Projection projection, GeographicProjection worldProjection, double x, double y) {
		double[] geoCoordinate = projection.toGeo(x, y), bteCoordinate;
		try {
			bteCoordinate = worldProjection.fromGeo(geoCoordinate[0], geoCoordinate[1]);
		} catch(OutOfProjectionBoundsException exception) {
			throw new RuntimeException(exception); // wcpgw lmao
		}
		return new Vector2DH(bteCoordinate[0], bteCoordinate[1]);
	}
	
	
	protected static Grs80Projection getProjFromFileName(File file) {
		String fileName = file.getName();
		return VMapUtils.getProjectionFromMapId(fileName.substring(0, fileName.length() - 4));
	}
	
	
	protected static void extractElevationPoints(VMapElement element, List<Vector2DH> elevPoints) {
		if(element instanceof VMapContour) {
			VMapContour contour = (VMapContour) element;
			for(Vector2DH[] va : contour.getVertexList()) for(Vector2DH v : va) {
				elevPoints.add(v.withHeight(contour.elevation));
			}
		}
		else if(element instanceof VMapElevationPoint) {
			VMapElevationPoint elevPoint = (VMapElevationPoint) element;
			elevPoints.add(elevPoint.toVector());
		}
	}
	
}
