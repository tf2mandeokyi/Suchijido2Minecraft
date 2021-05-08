package com.mndk.shapefile.dbf;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;

import com.mndk.shapefile.util.ShapefileCustomInputStream;

public class DBaseDataIterator implements Iterator<DBaseRecord>, Iterable<DBaseRecord>, Closeable, AutoCloseable {
	
	private final ShapefileCustomInputStream is;
	private final DBaseHeader header;
	private int index;
	
	public DBaseDataIterator(InputStream input, Charset charset) throws IOException {
		
		this.is = new ShapefileCustomInputStream(input, charset);
		
		this.header = new DBaseHeader(is);
		
		this.index = 0;
		
	}

	@Override
	public boolean hasNext() {
		return index != header.recordCount - 1;
	}

	@Override
	public DBaseRecord next() {
		try {
			DBaseRecord result = new DBaseRecord(header, is);
			index++;
			return result;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

	@Override
	public Iterator<DBaseRecord> iterator() {
		return this;
	}
	
}
