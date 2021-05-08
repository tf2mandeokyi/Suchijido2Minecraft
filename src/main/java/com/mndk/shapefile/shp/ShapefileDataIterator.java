package com.mndk.shapefile.shp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;

import com.mndk.shapefile.util.AutoCloseableIterator;
import com.mndk.shapefile.util.ShapefileCustomInputStream;

public class ShapefileDataIterator implements AutoCloseableIterator<ShapefileRecord> {
	
	
	private final ShapefileCustomInputStream is;
	private final ShapefileHeader header;
	private final int contentLength;
	private int totalLength;
	
	
	
	public ShapefileDataIterator(InputStream input, Charset charset) throws IOException {
		
		this.is = new ShapefileCustomInputStream(input, charset);
		
		this.header = new ShapefileHeader(is);

		this.contentLength = header.fileLength;
		
		// File length includes its header (50 * 16-bit word) - set 50 as an initial to the total length
		this.totalLength = 50;
		
	}

	
	
	@Override
	public boolean hasNext() {
		return totalLength < contentLength;
	}

	
	
	@Override
	public ShapefileRecord next() {
		try {
			int recordNumber = is.readIntBig();
			int recordLength = is.readIntBig();
			
			ShapefileRecord record = ShapefileRecord.from(recordNumber, recordLength, is);
			
			// Record length includes its header (4 * 16-bit word) - add 4 to the total length
			totalLength += recordLength + 4;
			
			return record;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	

	@Override
	public void close() throws IOException {
		is.close();
	}

	
	
	@Override
	public Iterator<ShapefileRecord> iterator() {
		return this;
	}
	
}
