package com.mndk.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.mndk.shapefile.shp.ShapefileDataIterator;
import com.mndk.shapefile.shp.ShapefileRecord;

/*
 * Reasons why I made this instead of using gt-shapefile:
 *  - It's too big. (Makes the mod up to 12~13 MB)
 *  - gt-shapefile somehow doesn't work ;(
 */
public class ShpDbfParser {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		try (
				InputStream stream = new FileInputStream(new File("test/37612030_/N3A_B0010000.shp"));
				ShapefileDataIterator it = new ShapefileDataIterator(stream, Charset.forName("cp949"))
		) {

			for(ShapefileRecord record : it) {
				System.out.println(record);
			}
			
		}
		
		/*try (
				InputStream stream = new FileInputStream(new File("test/37612030_/N3A_B0010000.dbf"));
				DBaseDataIterator it = new DBaseDataIterator(stream, Charset.forName("cp949"))
		) {

			for(DBaseRecord record : it) {
				System.out.println(record);
			}
			
		}*/
	}
	
}
