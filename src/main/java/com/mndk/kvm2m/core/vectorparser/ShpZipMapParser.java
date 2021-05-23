package com.mndk.kvm2m.core.vectorparser;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.projection.Projections;
import com.mndk.kvm2m.core.triangulator.TerrainTriangulator;
import com.mndk.kvm2m.core.util.file.DirectoryManager;
import com.mndk.kvm2m.core.util.file.ZipManager;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.VMapElementType;
import com.mndk.kvm2m.core.vmap.VMapParserException;
import com.mndk.kvm2m.core.vmap.VMapParserResult;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapElementLayer;
import com.mndk.kvm2m.core.vmap.elem.line.VMapContour;
import com.mndk.kvm2m.core.vmap.elem.line.VMapPolyline;
import com.mndk.kvm2m.core.vmap.elem.line.VMapWall;
import com.mndk.kvm2m.core.vmap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vmap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapBuilding;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;
import com.mndk.shapefile.ShpDbfDataIterator;
import com.mndk.shapefile.ShpDbfRecord;
import com.mndk.shapefile.dbf.DBaseField;
import com.mndk.shapefile.shp.ShapeVector;
import com.mndk.shapefile.shp.ShapefileRecord;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;



public class ShpZipMapParser extends VMapParser {


	@Override
	protected VMapParserResult getResult() throws IOException {
		
		VMapParserResult result = new VMapParserResult();
		
		Throwable throwable = null;
		
		// Set temporary destination directory
		String mapFilePath = mapFile.getAbsolutePath();
		Grs80Projection projection = getProjFromFileName(mapFile);
		if(projection == null) throw new VMapParserException("Invalid projection!");
		File zipDestination = new File(mapFilePath.substring(0, mapFilePath.lastIndexOf(".zip")) + "/");
		if(zipDestination.exists()) {
			throw new VMapParserException(zipDestination.getAbsolutePath() + " already exists.");
		}
		
		try {
			
			// Extract all files in map file
			zipDestination.mkdir();			
			ZipManager.extractZipFile(mapFile, zipDestination);
			
			File[] shapeFiles = zipDestination.listFiles((dir, name) -> name.endsWith(".shp"));
			for(File shapeFile : shapeFiles) {
				String filePath = shapeFile.getAbsolutePath();
				filePath = filePath.substring(0, filePath.length() - 4);
				String fileName = new File(filePath).getName();
				VMapElementLayer elementLayer = fromShpFile(filePath, fileName);
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
	
	
	
	private VMapElementLayer fromShpFile(String filePath, String fileName) throws FileNotFoundException, IOException {
		
		VMapElementType type = VMapElementType.fromLayerName(fileName.substring(4));
		ShpDbfDataIterator iterator = new ShpDbfDataIterator(filePath, Charset.forName("cp949"));
		
		DBaseField[] fields = iterator.getDBaseHeader().fields;
		String[] columns = new String[fields.length];
		for(int i = 0; i < fields.length; ++i) {
			columns[i] = fields[i].name;
		}
		
		VMapElementLayer layer = new VMapElementLayer(type, columns);
		
		// int i = 0;
		for(ShpDbfRecord record : iterator) {
			VMapElement element = fromElement(layer, record);
			if(element == null) continue;
			layer.add(element);
			// i++;
		}
		
		iterator.close();
		
		return layer;
	}
	
	
	
	private VMapElement fromElement(VMapElementLayer layer, ShpDbfRecord record) {
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
	
	
	
	private VMapElement fromPolygon(VMapElementLayer layer, ShpDbfRecord record) {
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
	

	
	private VMapPolyline fromLine(VMapElementLayer layer, ShpDbfRecord record) {
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
	
	
	
	private VMapPoint fromPoint(VMapElementLayer layer, ShpDbfRecord record) {
		ShapefileRecord.Point shape = (ShapefileRecord.Point) record.shape;
		Vector2DH vpoint = this.targetProjToWorldProjCoord(shape.x, shape.y);
		
		if(layer.getType() == VMapElementType.표고점) { return new VMapElevationPoint(layer, vpoint, record.dBase.data); }
		else { return new VMapPoint(layer, vpoint, record.dBase.data); }
	}
	
	
	
	public static void main(String[] args) throws IOException {
		
		/*
		String BTE_GEN_JSON =
				"{" +
					"\"projection\":\"bteairocean\"," +
					"\"orentation\":\"upright\"," +
					"\"scaleX\":7318261.522857145," +
					"\"scaleY\":7318261.522857145" +
				"}";
		GeographicProjection BTE = EarthGeneratorSettings.parse(BTE_GEN_JSON).projection();
		*/
		
		GeographicProjection proj = Projections.GRS80_WEST;
		Map<String, String> emptyOption = Collections.emptyMap();
		VMapParser parser = new ShpZipMapParser();
		
		VMapParserResult result = parser.parse(new File("test/37612030.zip"), proj, emptyOption);
		
		System.out.println(result.getLayer(VMapElementType.등고선));
		
		System.out.println(TerrainTriangulator.generate(result).size());
	}
}
