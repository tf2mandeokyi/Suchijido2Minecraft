package com.mndk.scjd2mc.core.scjd.reader;

import com.mndk.scjd2mc.core.projection.Korea2010BeltProjection;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.scjd.*;
import net.buildtheearth.terraplusplus.projection.EquirectangularProjection;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public abstract class SuchijidoFileReader {
	
	
	
	protected File mapFile;
	protected GeographicProjection worldProjection;
	protected Korea2010BeltProjection targetProjection;
	protected Map<String, String> options;
	
	
	
	public final SuchijidoData parse(File mapFile, GeographicProjection worldProjection, Map<String, String> options)
			throws Exception {

		synchronized (this) {
			this.mapFile = mapFile;
			this.worldProjection = worldProjection;
			this.options = options;
			this.targetProjection = this.getTargetProjection();
			return getResult();
		}
	}



	public final SuchijidoData parse(File mapFile, Map<String, String> options) throws Exception {
		return this.parse(mapFile, new EquirectangularProjection(), options);
	}



	public final SuchijidoData parse(File mapFile) throws Exception {
		return this.parse(mapFile, new EquirectangularProjection(), Collections.emptyMap());
	}



	protected abstract SuchijidoData getResult() throws IOException;



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
		return SuchijidoUtils.getProjectionFromMapName(fileName.substring(0, fileName.length() - 4));
	}
	
	
	
	public Korea2010BeltProjection getTargetProjection() {
		return getProjFromFileName(this.mapFile);
	}
	
}
