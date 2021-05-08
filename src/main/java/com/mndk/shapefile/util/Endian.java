package com.mndk.shapefile.util;

import java.io.IOException;
import java.io.InputStream;

public class Endian {

	public static short readShortBig(InputStream is) throws IOException {
		return (short) ((is.read() << 8) + is.read());
	}

	public static int readIntegerBig(InputStream is) throws IOException {
		return (is.read() << 24) + (is.read() << 16) + (is.read() << 8) + is.read();
	}

	public static int readIntegerLittle(InputStream is) throws IOException {
		return is.read() + (is.read() << 8) + (is.read() << 16) + (is.read() << 24);
	}

	public static long readLongLittle(InputStream is) throws IOException {
		return is.read() + (is.read() << 8) + (is.read() << 16) + (is.read() << 24) + ((long) is.read() << 32) + ((long) is.read() << 40) + ((long) is.read() << 48) + ((long) is.read() << 56);
	}

	public static double readDoubleLittle(InputStream is) throws IOException {
		return Double.longBitsToDouble(readLongLittle(is));
	}

}
