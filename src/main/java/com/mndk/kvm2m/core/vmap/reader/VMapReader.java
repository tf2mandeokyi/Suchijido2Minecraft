package com.mndk.kvm2m.core.vmap.reader;

import com.mndk.kvm2m.core.projection.Korea2010BeltProjection;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.VMapDataPayload;
import com.mndk.kvm2m.core.vmap.VMapGeometryPayload;
import com.mndk.kvm2m.core.vmap.VMapReaderResult;
import com.mndk.kvm2m.core.vmap.VMapUtils;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public abstract class VMapReader {
	
	
	
	protected File mapFile;
	protected GeographicProjection worldProjection;
	protected Korea2010BeltProjection targetProjection;
	protected Map<String, String> options;
	
	
	
	public final VMapReaderResult parse(
			File mapFile,
			GeographicProjection worldProjection,
			Map<String, String> options) throws Exception {

		synchronized (this) {
			this.mapFile = mapFile;
			this.worldProjection = worldProjection;
			this.options = options;
			this.targetProjection = this.getTargetProjection();
			Map.Entry<VMapGeometryPayload, VMapDataPayload> tuple = getResult();
			return VMapUtils.combineVMapPayloads(tuple.getKey(), tuple.getValue(), options);
		}
		
	}
	
	
	
	protected abstract Map.Entry<VMapGeometryPayload, VMapDataPayload> getResult() throws IOException;
	
	
	
	protected Vector2DH targetProjToWorldProjCoord(double x, double y) {
		double[] geoCoordinate = this.targetProjection.toGeo(x, y), bteCoordinate;
		try {
			bteCoordinate = worldProjection.fromGeo(geoCoordinate[0], geoCoordinate[1]);
		} catch(OutOfProjectionBoundsException exception) {
			throw new RuntimeException(exception); // wcpgw lmao
		}
		return new Vector2DH(bteCoordinate[0], bteCoordinate[1]);
	}
	
	
	
	protected static Korea2010BeltProjection getProjFromFileName(File file) {
		String fileName = file.getName();
		return VMapUtils.getProjectionFromMapName(fileName.substring(0, fileName.length() - 4));
	}
	
	
	
	public Korea2010BeltProjection getTargetProjection() {
		return getProjFromFileName(this.mapFile);
	}
	
}
