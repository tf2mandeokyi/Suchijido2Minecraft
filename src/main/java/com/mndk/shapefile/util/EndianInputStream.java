package com.mndk.shapefile.util;

import java.io.IOException;
import java.io.InputStream;

public class EndianInputStream extends InputStream {

	private final InputStream parent;
	
	public EndianInputStream(InputStream parent) {
		this.parent = parent;
	}
	
	@Override
	public int read() throws IOException {
		return parent.read();
	}
	
	public short readShortBig() throws IOException {
		return (short) ((read() << 8) + read());
	}

	public int readIntBig() throws IOException {
		return (read() << 24) + (read() << 16) + (read() << 8) + read();
	}

	public int readIntLittle() throws IOException {
		return read() + (read() << 8) + (read() << 16) + (read() << 24);
	}

	public long readLongLittle() throws IOException {
		return read() + (read() << 8) + (read() << 16) + (read() << 24) + ((long) read() << 32) + ((long) read() << 40) + ((long) read() << 48) + ((long) read() << 56);
	}

	public double readDoubleLittle() throws IOException {
		return Double.longBitsToDouble(readLongLittle());
	}

}
