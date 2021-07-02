package com.mndk.shapefile.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ShapefileCustomInputStream extends InputStream {

	private final InputStream parent;
	private final Charset charset;
	
	public ShapefileCustomInputStream(InputStream parent, Charset charset) {
		this.parent = parent;
		this.charset = charset;
	}
	
	@Override
	public int read() throws IOException {
		return parent.read();
	}
	
	public byte[] readBytes(int byteCount) throws IOException {
		byte[] result = new byte[byteCount];
		for(int i = 0; i < byteCount; i++) {
			result[i] = (byte) read();
		}
		return result;
	}
	
	public String readString(int byteCount) throws IOException {
		return new String(readBytes(byteCount), charset);
	}
	
	public short readShortBig() throws IOException {
		return (short) ((read() << 8) + read());
	}
	
	public short readShortLittle() throws IOException {
		return (short) (read() + (read() << 8));
	}

	public int readIntBig() throws IOException {
		return (read() << 24) + (read() << 16) + (read() << 8) + read();
	}

	public int readIntLittle() throws IOException {
		return read() + (read() << 8) + (read() << 16) + (read() << 24);
	}

	public long readLongLittle() throws IOException {
		return read() + ((long) read() << 8) + ((long) read() << 16) + ((long) read() << 24) + ((long) read() << 32) + ((long) read() << 40) + ((long) read() << 48) + ((long) read() << 56);
	}

	public double readDoubleLittle() throws IOException {
		return Double.longBitsToDouble(readLongLittle());
	}
	
	@Override
	public void close() throws IOException {
		parent.close();
		super.close();
	}

}
