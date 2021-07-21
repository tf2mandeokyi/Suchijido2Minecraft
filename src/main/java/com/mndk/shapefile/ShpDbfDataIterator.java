package com.mndk.shapefile;

import com.mndk.shapefile.dbf.DBaseDataIterator;
import com.mndk.shapefile.dbf.DBaseHeader;
import com.mndk.shapefile.shp.ShapefileDataIterator;
import com.mndk.shapefile.shp.ShapefileHeader;
import com.mndk.shapefile.util.AutoCloseableIterator;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;

/*
 * Reasons why I made this instead of using gt-shapefile:
 *  - It's too big. (Makes the mod up to 12~13 MB)
 *  - gt-shapefile somehow doesn't work ;(
 */
public class ShpDbfDataIterator implements AutoCloseableIterator<ShpDbfRecord> {

	
	private final ShapefileDataIterator shpIterator;
	private final DBaseDataIterator dBaseIterator;
	
	
	
	public ShpDbfDataIterator(String filePath, Charset charset) throws IOException {
		
		this.shpIterator = new ShapefileDataIterator(new FileInputStream(filePath + ".shp"), charset);
		
		File dBaseFile = new File(filePath + ".dbf");
		this.dBaseIterator = dBaseFile.exists() ? new DBaseDataIterator(new FileInputStream(dBaseFile), charset) : null;
		
	}

	
	
	public ShapefileHeader getShapefileHeader() {
		return shpIterator.getHeader();
	}
	
	
	
	public DBaseHeader getDBaseHeader() {
		return dBaseIterator.getHeader();
	}
	
	
	
	@Override
	public boolean hasNext() {
		return shpIterator.hasNext() && (dBaseIterator == null || dBaseIterator.hasNext());
	}

	
	
	@Override
	public ShpDbfRecord next() {
		return new ShpDbfRecord(shpIterator.next(), dBaseIterator.next());
	}

	
	
	@Override
	public void close() throws IOException {
		shpIterator.close();
		dBaseIterator.close();
	}

	
	
	@Override @Nonnull
	public Iterator<ShpDbfRecord> iterator() {
		return this;
	}
	
}
