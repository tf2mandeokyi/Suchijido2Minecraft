package com.mndk.kvm2m.core.vectorparser;

@Deprecated
public class DxfMapParser {

	/*@SuppressWarnings("unchecked")
	public static VMapParserResult parse(File mapFile) throws ParseException, FileNotFoundException {

		VMapParserResult result = new VMapParserResult();

		String fileName = mapFile.getName();
		if(!FilenameUtils.isExtension(fileName, "dxf")) return null;
		Grs80Projection projection = VMapUtils.getProjectionFromMapId(fileName.substring(0, fileName.length() - 4));
		Parser parser = ParserBuilder.createDefaultParser();
		parser.parse(
				new FileInputStream(mapFile),
				DXFParser.DEFAULT_ENCODING
		);
		DXFDocument doc = parser.getDocument();
		Iterator<DXFLayer> layers = doc.getDXFLayerIterator();

		while(layers.hasNext()) {
			DXFLayer layer = layers.next();
			if(layer.hasDXFEntities("LWPOLYLINE")) {
				List<DXFLWPolyline> polylines = layer.getDXFEntities("LWPOLYLINE");
				for(DXFLWPolyline polyline : polylines) {
					fromDXFLWPolyline(layer, polyline, projection, result);
				}
			}
			if(layer.hasDXFEntities("POINT")) {
				List<DXFPoint> points = layer.getDXFEntities("POINT");
				for(DXFPoint point : points) {
					fromDXFPoint(layer, point, projection, result);
				}
			}
		}

		return result;

	}

	
	
	public static void fromDXFLWPolyline(DXFLayer layer, DXFLWPolyline polyline, Grs80Projection projection, VMapParserResult result) {
		
		String layerName = layer.getName();
		VMapElementType type = VMapElementType.getTypeFromLayerName(layerName);
		
		if(type == VMapElementType.등고선) {
			
			VMapContour contour = new VMapContour(polyline, projection);
			for(Vector2DH[] va : contour.getVertexList()) for(Vector2DH v : va) {
				result.getElevationPoints().add(v.withHeight(contour.getElevation()));
			}
			
		}
		else if(type == VMapElementType.도곽선) {
			result.setBoundary(new VMapPolyline(polyline, projection, VMapElementType.도곽선));
		}
		else {
			result.addElement(new VMapPolyline(polyline, projection, type));
		}
	}

	
	
	public static void fromDXFPoint(DXFLayer layer, DXFPoint point, Grs80Projection projection, VMapParserResult result) {
		
		String layerName = layer.getName();
		VMapElementType type = VMapElementType.getTypeFromLayerName(layerName);
		
		if(type == VMapElementType.표고점) {
			result.getElevationPoints().add(new VMapElevationPoint(point, projection).toVector());
		}
		else {
			result.addElement(new VMapPoint(point, projection, type));
		}
	}*/
	
}
