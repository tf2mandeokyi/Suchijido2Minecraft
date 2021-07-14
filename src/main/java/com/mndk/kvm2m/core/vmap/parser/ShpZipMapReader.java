package com.mndk.kvm2m.core.vmap.parser;

import com.mndk.kvm2m.core.projection.Korea2010BeltProjection;
import com.mndk.kvm2m.core.util.file.DirectoryManager;
import com.mndk.kvm2m.core.util.file.ZipManager;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.VMapElementType;
import com.mndk.kvm2m.core.vmap.VMapParserException;
import com.mndk.kvm2m.core.vmap.VMapReaderResult;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.elem.line.VMapContour;
import com.mndk.kvm2m.core.vmap.elem.line.VMapPolyline;
import com.mndk.kvm2m.core.vmap.elem.line.VMapWall;
import com.mndk.kvm2m.core.vmap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vmap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapBuilding;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;
import com.mndk.kvm2m.mod.KVectorMap2MinecraftMod;
import com.mndk.shapefile.ShpDbfDataIterator;
import com.mndk.shapefile.ShpDbfRecord;
import com.mndk.shapefile.dbf.DBaseField;
import com.mndk.shapefile.shp.ShapeVector;
import com.mndk.shapefile.shp.ShapefileRecord;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;



public class ShpZipMapReader extends VMapReader {


	@Override
	protected VMapReaderResult getResult() throws IOException {
		
		VMapReaderResult result = new VMapReaderResult();
		
		Throwable throwable = null;
		
		// Set temporary destination directory
		String mapFilePath = mapFile.getAbsolutePath();
		Korea2010BeltProjection projection = getProjFromFileName(mapFile);
		if(projection == null) throw new VMapParserException("Invalid projection!");
		File zipDestination = new File(mapFilePath.substring(0, mapFilePath.lastIndexOf(".zip")) + "/");
		System.out.println(zipDestination);
		if(zipDestination.isDirectory()) {
			throw new VMapParserException(zipDestination.getAbsolutePath() + " directory already exists.");
		}

		boolean ignored;

		try {
			
			// Extract all files in map file
			ignored = zipDestination.mkdir();
			ZipManager.extractZipFile(mapFile, zipDestination);
			
			File[] shapeFiles = zipDestination.listFiles((dir, name) -> name.endsWith(".shp"));
			assert shapeFiles != null;
			for(File shapeFile : shapeFiles) {
				String filePath = shapeFile.getAbsolutePath();
				filePath = filePath.substring(0, filePath.length() - 4);
				String fileName = new File(filePath).getName();
				VMapLayer elementLayer = fromShpFile(filePath, fileName);
				result.addElement(elementLayer);
			}
		} catch(Throwable t) {
			throwable = t;
		}

		// I could use "finally" though
		DirectoryManager.deleteDirectory(zipDestination);
		
		if(throwable != null) {
			if(throwable instanceof IOException) {
				throw (IOException) throwable;
			}
			else {
				throw new RuntimeException(throwable);
			}
		}
		
		return result;
		
	}
	
	
	
	private VMapLayer fromShpFile(String filePath, String fileName) throws IOException {
		
		VMapElementType type = VMapElementType.fromLayerName(fileName);

		try(ShpDbfDataIterator iterator = new ShpDbfDataIterator(filePath, Charset.forName("cp949"))) {

			DBaseField[] fields = iterator.getDBaseHeader().fields;
			String[] columns = new String[fields.length];
			for (int i = 0; i < fields.length; ++i) {
				columns[i] = fields[i].name;
			}

			VMapLayer layer = new VMapLayer(type, columns);

			// int i = 0;
			for (ShpDbfRecord record : iterator) {
				try {
					VMapElement element = fromElement(layer, record);
					if (element == null) continue;
					layer.add(element);
					// i++;
				} catch(Exception e) {
					KVectorMap2MinecraftMod.logger.error("Error occured while parsing layer " + type + ": " + e.getMessage());
				}
			}

			return layer;
		}
	}
	
	
	
	private VMapElement fromElement(VMapLayer layer, ShpDbfRecord record) throws Exception {
		if(record.shape instanceof ShapefileRecord.Polygon) {
			return fromPolygon(layer, record);
		}
		else if(record.shape instanceof ShapefileRecord.PolyLine) {
			return fromLine(layer, record);
		}
		else if(record.shape instanceof ShapefileRecord.Point) {
			return fromPoint(layer, record);
		}
		return null;
	}
	
	
	
	private VMapElement fromPolygon(VMapLayer layer, ShpDbfRecord record) {
		ShapefileRecord.Polygon shape = (ShapefileRecord.Polygon) record.shape;
		ShapeVector[][] points = shape.points;
		Vector2DH[][] vertexList = new Vector2DH[shape.points.length][];
		
		for(int j = 0; j < points.length; ++j) {
			int size = points[j].length;
			vertexList[j] = new Vector2DH[size];
			
			for(int i = 0; i < size; ++i) {
				ShapeVector vector = points[j][i];
				vertexList[j][i] = this.targetProjToWorldProjCoord(vector.x, vector.y);
			}
		}
		
		if(layer.getType() == VMapElementType.건물) {
			if(options.containsKey("gen-building-shells")) { 
				return new VMapBuilding(layer, vertexList, record.dBase.data);
			} else {
				return new VMapPolyline(layer, vertexList, record.dBase.data, true);
			}
		}
		else { return new VMapPolygon(layer, vertexList, record.dBase.data, true); }
	}
	

	
	private VMapPolyline fromLine(VMapLayer layer, ShpDbfRecord record) {
		ShapefileRecord.PolyLine shape = (ShapefileRecord.PolyLine) record.shape;
		ShapeVector[][] points = shape.points;
		Vector2DH[][] vertexList = new Vector2DH[shape.points.length][];
		
		for(int j = 0; j < points.length; ++j) {
			int size = points[j].length;
			vertexList[j] = new Vector2DH[size];
			
			for(int i = 0; i < size; ++i) {
				ShapeVector vector = points[j][i];
				vertexList[j][i] = this.targetProjToWorldProjCoord(vector.x, vector.y);
			}
		}
		
		if(layer.getType() == VMapElementType.등고선) { return new VMapContour(layer, vertexList[0], record.dBase.data); }
		else if(layer.getType() == VMapElementType.옹벽) { return new VMapWall(layer, vertexList, record.dBase.data, false); }
		else { return new VMapPolyline(layer, vertexList, record.dBase.data, false); }
	}
	
	
	
	private VMapPoint fromPoint(VMapLayer layer, ShpDbfRecord record) throws Exception {
		ShapefileRecord.Point shape = (ShapefileRecord.Point) record.shape;
		Vector2DH vpoint = this.targetProjToWorldProjCoord(shape.vector.x, shape.vector.y);
		
		if(layer.getType() == VMapElementType.표고점) { return new VMapElevationPoint(layer, vpoint, record.dBase.data); }
		else { return new VMapPoint(layer, vpoint, record.dBase.data); }
	}

}
