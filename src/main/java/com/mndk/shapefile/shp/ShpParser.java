package com.mndk.shapefile.shp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.projection.Proj4jProjection;

public class ShpParser {
	
	public static void read(BufferedReader reader) throws IOException {
		ShpHeader header = getHeader(reader);
	}
	
	private static ShpHeader getHeader(BufferedReader reader) throws IOException {
		
		// bad code
		
		ShpHeader header = new ShpHeader();
		header.fileCode = readIntegerBig(reader);
		/*int unused1 = */ readIntegerBig(reader);
		/*int unused2 = */ readIntegerBig(reader);
		/*int unused3 = */ readIntegerBig(reader);
		/*int unused4 = */ readIntegerBig(reader);
		/*int unused5 = */ readIntegerBig(reader);
		header.fileLength = readIntegerBig(reader);
		header.version = readIntegerLittle(reader);
		header.type = ShapeType.getType(readIntegerLittle(reader));
		double xmin = readDoubleLittle(reader);
		double ymin = readDoubleLittle(reader);
		double xmax = readDoubleLittle(reader);
		double ymax = readDoubleLittle(reader);
		double zmin = readDoubleLittle(reader);
		double zmax = readDoubleLittle(reader);
		double mmin = readDoubleLittle(reader);
		double mmax = readDoubleLittle(reader);
		header.boundingBox = new ShpBoundingBox(xmin, ymin, zmin, mmin, xmax, ymax, zmax, mmax);
		return header;
	}
	
	private static int readIntegerBig(BufferedReader reader) throws IOException {
		return (reader.read() << 24) + (reader.read() << 16) + (reader.read() << 8) + reader.read();
	}
	
	private static int readIntegerLittle(BufferedReader reader) throws IOException {
		return reader.read() + (reader.read() << 8) + (reader.read() << 16) + (reader.read() << 24);
	}
	
	private static long readLongLittle(BufferedReader reader) throws IOException {
		return reader.read() + (reader.read() << 8) + (reader.read() << 16) + (reader.read() << 24) + ((long) reader.read() << 32) + ((long) reader.read() << 40) + ((long) reader.read() << 48) + ((long) reader.read() << 56);
	}
	
	private static double readDoubleLittle(BufferedReader reader) throws IOException {
		return Double.longBitsToDouble(readLongLittle(reader));
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("test/37612030_/N3A_A0010000.shp")));
		ShpHeader header = getHeader(reader);
		Proj4jProjection projection = new Proj4jProjection("", new String[] {
				"+proj=tmerc", "+lat_0=38", "+lon_0=127", "+k=1", "+x_0=200000", "+y_0=600000", "+ellps=GRS80", "+units=m", "+no_defs"
		});
		System.out.println(Arrays.toString(projection.toGeo(header.boundingBox.xmin, header.boundingBox.ymin)));
	}
	
}
