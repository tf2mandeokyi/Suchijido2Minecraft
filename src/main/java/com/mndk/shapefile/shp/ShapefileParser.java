package com.mndk.shapefile.shp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.mndk.shapefile.util.Endian;

public class ShapefileParser {
	
	public static ShapefileData read(InputStream is) throws IOException {
		
		// Read shape file header
		ShapefileHeader header = new ShapefileHeader(is);
		List<ShapeRecord> records = new ArrayList<>();
		
		// Read records
		int fileLength = header.fileLength;
		
		// File length includes its header (50 * 16-bit word) - set 50 as an initial to the total length
		int totalLength = 50;
		
		while(totalLength < fileLength) {
			
			int recordNumber = Endian.readIntegerBig(is);
			int recordLength = Endian.readIntegerBig(is);
			
			ShapeRecord record = ShapeRecord.from(recordNumber, recordLength, is);
			records.add(record);
			
			// Record length includes its header (4 * 16-bit word) - add 4 to the total length
			totalLength += recordLength + 4;
		}
		
		return new ShapefileData(header, records);
		
	}
	
	/*
	public static void main(String[] args) throws FileNotFoundException, IOException {
		try (InputStream reader = new FileInputStream(new File("test/37612030_/N3A_A0010000.shp"))) {
			ShapefileData result = read(reader);
			
			Proj4jProjection projection = new Proj4jProjection("", new String[] {
					"+proj=tmerc", "+lat_0=38", "+lon_0=127", "+k=1", "+x_0=200000", "+y_0=600000", "+ellps=GRS80", "+units=m", "+no_defs"
			});
			System.out.println(result.records.size());
			for(ShapeRecord record : result.records.subList(0, 1)) {
				if(record instanceof ShapeRecord.Polygon) {
					ShapeRecord.Polygon polygon = (ShapeRecord.Polygon) record;
					ShapeVector[][] points = polygon.points;
					System.out.println("P");
					for(ShapeVector[] v1 : points) {
						System.out.println("  [");
						for(ShapeVector v : v1) {
							double[] projected = projection.toGeo(v.x, v.y);
							System.out.println("    (" + projected[1] + "," + projected[0] + ")");
						}
						System.out.println("  ]");
					}
						
				}
			}
			
		}
	}
	*/
}
