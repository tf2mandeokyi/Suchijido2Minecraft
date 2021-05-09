package com.mndk.kvm2m.core.vectorparser;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.VMapParserResult;
import com.mndk.kvm2m.core.vmap.VMapUtils;

import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public abstract class VMapParser {
	
	
	
	protected File mapFile;
	protected GeographicProjection worldProjection;
	protected Grs80Projection targetProjection;
	protected Map<String, String> options;
	
	
	
	public final VMapParserResult parse(File mapFile, GeographicProjection worldProjection, Map<String, String> options) throws IOException {
		
		this.mapFile = mapFile;
		this.worldProjection = worldProjection;
		this.options = options;
		this.targetProjection = this.getTargetProjection();
		return getResult();
		
	}
	
	
	
	protected abstract VMapParserResult getResult() throws IOException;
	
	
	
	protected Vector2DH targetProjToWorldProjCoord(double x, double y) {
		double[] geoCoordinate = this.targetProjection.toGeo(x, y), bteCoordinate;
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
	
	
	
	public Grs80Projection getTargetProjection() {
		return getProjFromFileName(this.mapFile);
	}
	
	
	/*protected static void extractElevationPoints(VMapElement element, List<Vector2DH> elevPoints) {
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
	}*/
	
}
