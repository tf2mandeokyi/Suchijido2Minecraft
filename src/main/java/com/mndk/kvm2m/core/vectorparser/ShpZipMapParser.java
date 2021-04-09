package com.mndk.kvm2m.core.vectorparser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.util.delaunator.FastDelaunayTriangulator;
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
		
		try {
			if(!FileDataStoreFinder.getAvailableFileExtentions().contains(".shp")) {
				throw new VMapParserException("Shapefile (.shp) extension is not available!");
			}
			
			// Extract all files in map file
			zipDestination.mkdir();			
			ZipManager.extractZipFile(mapFile, zipDestination);
			
			File[] shapeFiles = zipDestination.listFiles((dir, name) -> name.endsWith(".shp"));
			for(File shapeFile : shapeFiles) {
				// System.out.println("Checking file \"" + shapeFile + "\"");
				
				FileDataStore dataStore = FileDataStoreFinder.getDataStore(shapeFile);
				if(dataStore == null) {
					// System.out.println("  ! DataStore not found.");
					continue;
				}
				if(dataStore instanceof ShapefileDataStore) {
					((ShapefileDataStore) dataStore).setCharset(Charset.forName("MS949"));
				}
				String[] typeNames = dataStore.getTypeNames();
				
				for(String typeName : typeNames) {

					// System.out.println("  Checking typeName \"" + typeName + "\"");
					
					SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
					SimpleFeatureCollection collection = featureSource.getFeatures();
					SimpleFeatureType schema = collection.getSchema();
					try (FeatureIterator<SimpleFeature> iterator = collection.features()) {
						
						int attributeCount = schema.getAttributeCount();
						String[] columnArray = new String[attributeCount];
						for(int i = 0; i < attributeCount; ++i) {
							columnArray[i] = schema.getDescriptor(i).getLocalName();
						}
						
						VMapElementType type = VMapElementType.getTypeFromLayerName(typeName.substring(4));
						VMapElementLayer layer = new VMapElementLayer(type, columnArray);
						
						// System.out.println("    Found iterator: n(attribute) = " + attributeCount + ", type = " + type);
						
						// int total = 0;
						while(iterator.hasNext()) {
							SimpleFeature feature = iterator.next();
							VMapElement element = fromFeature(layer, feature, typeName, projection, worldProjection);
							if(element == null) continue;
							layer.add(element);
							extractElevationPoints(element, result.getElevationPoints());
							// ++total;
						}
						// System.out.println("    - Total size: " + total);
						
						result.addElement(layer);
					}
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
		VMapParserResult result = new ShpZipMapParser().parse(new File("test/.asdf/37612030.zip"), BTE);
		System.out.println(result.getElevationPoints().size());
		FastDelaunayTriangulator.from(result.getElevationPoints());
	}
}
