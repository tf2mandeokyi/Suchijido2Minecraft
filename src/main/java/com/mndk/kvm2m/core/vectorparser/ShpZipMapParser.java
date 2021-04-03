package com.mndk.kvm2m.core.vectorparser;

public class ShpZipMapParser /*extends VMapParser*/ {


	/*
	public VMapParserResult parse(File mapFile) throws IOException {
		
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
			// Read all shp files with File#getFilesWithExtension or smth
			File[] shpFiles = zipDestination.listFiles((dir, name) -> name.endsWith(".shp"));
			for(File file : shpFiles) {
				Map<String, String> connect = new HashMap<>();
				connect.put("url", file.toURI().toString());
				connect.put("charset", "MS949");
				
				DataStore dataStore = DataStoreFinder.getDataStore(connect);
				String[] typeNames = dataStore.getTypeNames();
				
				for(String typeName : typeNames) {
					System.out.println("Reading content " + typeName);
	
					FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
					FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures();
					FeatureIterator<SimpleFeature> iterator = collection.features();
					
					fromFeatureIterator(iterator, typeName, projection, result.getElevationPoints());
				}
			}
		} finally {
			DirectoryManager.deleteDirectory(zipDestination);
		}
		
		
		return result;
		
	}
	
	
	
	private static VMapElementLayer fromFeatureIterator(FeatureIterator<SimpleFeature> iterator, String typeName, Grs80Projection projection, List<Vector2DH> elevPoints) {
		VMapElementType type = VMapElementType.getTypeFromLayerName(typeName.substring(4));
		VMapElementLayer layer = new VMapElementLayer(type);
		
		try {
			while(iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				VMapElement element = fromFeature(layer, feature, typeName, projection);
				if(element == null) continue;
				layer.add(element);
				extractElevationPoints(element, elevPoints);
			}
		} finally {
			iterator.close();
		}
		
		return layer;
	}
	
	
	
	private static VMapElement fromFeature(VMapElementLayer layer, SimpleFeature feature, String typeName, Grs80Projection projection) {
		GeometryAttribute sourceGeometryAttribute = feature.getDefaultGeometryProperty();
		IBlockState state = VMapBlockSelector.getBlockState(ngiElement, type);
		int elevation = VMapBlockSelector.getAdditionalHeight(ngiElement, type);
		
		Object the_geom = feature.getAttribute("the_geom");
		if(the_geom instanceof MultiPolygon) {
			return fromMultiPolygon(layer, (MultiPolygon) the_geom, projection);
		}
		else if(the_geom instanceof MultiLineString) {
			return null;
		}
		else if(the_geom instanceof Point) {
			return null;
		}
		else return null;
		
	}
	
	
	
	private static VMapElement fromMultiPolygon(
			VMapElementLayer layer, 
			MultiPolygon polygon, 
			IBlockState state, 
			int elevation, 
			Grs80Projection projection
	) {
		Coordinate[] coordinates = polygon.getCoordinates();
		int n;
		Vector2DH[] vertexList = new Vector2DH[n = coordinates.length];
		for(int i = 0; i < n; ++i) {
			Coordinate coordinate = coordinates[i];
			vertexList[i] = projectGrs80CoordToBteCoord(projection, coordinate.x, coordinate.y);
		}
		
		if(layer.getType() == VMapElementType.건물) { return new VMapBuilding(layer, vertexList, state, elevation); }
		else { return new VMapPolyline(layer, vertexList, state, elevation, true); }
	}
	
	
	
	public static void main(String[] args) throws IOException {
		new ShpZipMapParser().parse(new File("test/37612030.zip"));
	}*/
}
