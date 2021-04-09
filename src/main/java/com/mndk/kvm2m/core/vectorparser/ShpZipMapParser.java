package com.mndk.kvm2m.core.vectorparser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.file.DirectoryManager;
import com.mndk.kvm2m.core.util.file.ZipManager;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vectormap.VMapElementType;
import com.mndk.kvm2m.core.vectormap.VMapParserException;
import com.mndk.kvm2m.core.vectormap.VMapParserResult;
import com.mndk.kvm2m.core.vectormap.elem.VMapElement;
import com.mndk.kvm2m.core.vectormap.elem.VMapElementLayer;
import com.mndk.kvm2m.core.vectormap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vectormap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapBuilding;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapContour;
import com.mndk.kvm2m.core.vectormap.elem.poly.VMapPolyline;

import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

public class ShpZipMapParser extends VMapParser {


	@Override
	public VMapParserResult parse(File mapFile, GeographicProjection worldProjection) throws IOException {
		
		VMapParserResult result = new VMapParserResult();
	
		// Set temporary destination directory
		String mapFilePath = mapFile.getAbsolutePath();
		Grs80Projection projection = getProjFromFileName(mapFile);
		if(projection == null) throw new VMapParserException("Invalid projection!");
		File zipDestination = new File(mapFilePath.substring(0, mapFilePath.lastIndexOf(".zip")) + "/");
		if(zipDestination.exists()) {
			throw new VMapParserException(zipDestination.getAbsolutePath() + " already exists.");
		}
		zipDestination.mkdir();
		
		// Extract all files in map file
		ZipManager.extractZipFile(mapFile, zipDestination);
		
		try {
			File[] shpFiles = zipDestination.listFiles((dir, name) -> name.endsWith(".shp"));
			for(File file : shpFiles) {
				Map<String, String> connect = new HashMap<>();
				connect.put("url", file.toURI().toString());
				connect.put("charset", "MS949");
				
				DataStore dataStore = DataStoreFinder.getDataStore(connect);
				if(dataStore == null) continue;
				String[] typeNames = dataStore.getTypeNames();
				
				for(String typeName : typeNames) {
					FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
					FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures();
					FeatureIterator<SimpleFeature> iterator = collection.features();
					SimpleFeatureType featureType = collection.getSchema();
					
					int attributeCount = featureType.getAttributeCount();
					String[] columnArray = new String[attributeCount];
					for(int i = 0; i < attributeCount; ++i) {
						columnArray[i] = featureType.getDescriptor(i).getLocalName();
					}
					
					VMapElementType type = VMapElementType.getTypeFromLayerName(typeName.substring(4));
					VMapElementLayer layer = new VMapElementLayer(type, columnArray);
					
					try {
						while(iterator.hasNext()) {
							SimpleFeature feature = iterator.next();
							VMapElement element = fromFeature(layer, feature, typeName, projection, worldProjection);
							if(element == null) continue;
							layer.add(element);
							extractElevationPoints(element, result.getElevationPoints());
						}
					} finally {
						iterator.close();
					}
					
					result.addElement(layer);
				}
			}
		} finally {
			DirectoryManager.deleteDirectory(zipDestination);
		}
		
		
		return result;
		
	}
	
	
	
	private static VMapElement fromFeature(
			VMapElementLayer layer,
			SimpleFeature feature,
			String typeName,
			Grs80Projection projection,
			GeographicProjection worldProjection
	) {
		int attributeCount = feature.getAttributeCount();
		Object[] rowData = new Object[attributeCount];
		for(int i = 0; i < attributeCount; ++i) {
			rowData[i] = feature.getAttribute(i);
		}
		Object the_geom = feature.getAttribute("the_geom");
		if(the_geom instanceof MultiPolygon) {
			return fromMultiPolygon(layer, (MultiPolygon) the_geom, rowData, projection, worldProjection);
		}
		else if(the_geom instanceof MultiLineString) {
			return fromMultiLineString(layer, (MultiLineString) the_geom, rowData, projection, worldProjection);
		}
		else if(the_geom instanceof Point) {
			return fromPoint(layer, (Point) the_geom, rowData, projection, worldProjection);
		}
		else return null;
		
	}
	
	
	
	private static VMapElement fromMultiPolygon(
			VMapElementLayer layer, 
			MultiPolygon polygon, 
			Object[] rowData,
			Grs80Projection projection,
			GeographicProjection worldProjection
	) {
		Coordinate[] coordinates = polygon.getCoordinates();
		int n;
		Vector2DH[] vertexList = new Vector2DH[n = coordinates.length];
		for(int i = 0; i < n; ++i) {
			Coordinate coordinate = coordinates[i];
			vertexList[i] = projectGrs80CoordToWorldCoord(projection, worldProjection, coordinate.x, coordinate.y);
		}
		
		if(layer.getType() == VMapElementType.건물) { return new VMapBuilding(layer, new Vector2DH[][] { vertexList }, rowData); }
		else { return new VMapPolyline(layer, vertexList, rowData, true); }
	}
	
	
	
	private static VMapElement fromMultiLineString(
			VMapElementLayer layer, 
			MultiLineString lineString, 
			Object[] rowData,
			Grs80Projection projection,
			GeographicProjection worldProjection
	) {
		Coordinate[] coordinates = lineString.getCoordinates();
		int n;
		Vector2DH[] vertexList = new Vector2DH[n = coordinates.length];
		for(int i = 0; i < n; ++i) {
			Coordinate coordinate = coordinates[i];
			vertexList[i] = projectGrs80CoordToWorldCoord(projection, worldProjection, coordinate.x, coordinate.y);
		}
		
		if(layer.getType() == VMapElementType.등고선) { return new VMapContour(layer, vertexList, rowData); }
		else { return new VMapPolyline(layer, vertexList, rowData, false); }
	}
	
	
	
	private static VMapElement fromPoint(
			VMapElementLayer layer, 
			Point point, 
			Object[] rowData,
			Grs80Projection projection,
			GeographicProjection worldProjection
	) {
		Coordinate coordinates = point.getCoordinate();
		Vector2DH vpoint = projectGrs80CoordToWorldCoord(projection, worldProjection, coordinates.x, coordinates.y);
		
		if(layer.getType() == VMapElementType.표고점) { return new VMapElevationPoint(layer, vpoint, rowData); }
		else { return new VMapPoint(layer, vpoint, rowData); }
	}
	
	
	
	public static void main(String[] args) throws IOException {
		
		String BTE_GEN_JSON =
				"{" +
					"\"projection\":\"bteairocean\"," +
					"\"orentation\":\"upright\"," +
					"\"scaleX\":7318261.522857145," +
					"\"scaleY\":7318261.522857145" +
				"}";
		GeographicProjection BTE = EarthGeneratorSettings.parse(BTE_GEN_JSON).projection();
		
		VMapParserResult result = new ShpZipMapParser().parse(new File("test/37612030.zip"), BTE);
		for(Vector2DH v : result.getElevationPoints()) {
			System.out.println(v);
		}
	}
}
