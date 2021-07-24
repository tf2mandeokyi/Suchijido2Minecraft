package com.mndk.shapefile;

import com.mndk.shapefile.dbf.DBaseDataIterator;
import com.mndk.shapefile.dbf.DBaseHeader;
import com.mndk.shapefile.shp.ShapefileDataIterator;
import com.mndk.shapefile.shp.ShapefileHeader;
import com.mndk.shapefile.util.AutoCloseableIterator;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import sun.misc.Cleaner;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/*
 * Reasons why I made this instead of using gt-shapefile:
 *  - It's too big. (Makes the mod up to 12~13 MB)
 *  - gt-shapefile somehow doesn't work ;(
 */
public class ShpDbfDataIterator implements AutoCloseableIterator<ShpDbfRecord> {

	
	private final ShapefileDataIterator shpIterator;
	private final DBaseDataIterator dBaseIterator;

	private final FileChannel shpChannel, dbfChannel;
	private final MappedByteBuffer shpBuffer, dbfBuffer;
	public final boolean containsDbfFile;
	
	
	
	public ShpDbfDataIterator(String filePath, Charset charset) throws IOException {
		this.shpChannel = FileChannel.open(Paths.get(filePath + ".shp"), StandardOpenOption.READ);
		this.shpBuffer = shpChannel.map(FileChannel.MapMode.READ_ONLY, 0, shpChannel.size());
		InputStream shpInputStream = new ByteBufferBackedInputStream(this.shpBuffer);
		this.shpIterator = new ShapefileDataIterator(shpInputStream, charset);

		File dBaseFile = new File(filePath + ".dbf");
		if(dBaseFile.exists()) {
			this.dbfChannel = FileChannel.open(dBaseFile.toPath(), StandardOpenOption.READ);
			this.dbfBuffer = dbfChannel.map(FileChannel.MapMode.READ_ONLY, 0, dbfChannel.size());
			InputStream dbfInputStream = new ByteBufferBackedInputStream(this.dbfBuffer);
			this.dBaseIterator = new DBaseDataIterator(dbfInputStream, charset);
			this.containsDbfFile = true;
		}
		else {
			this.dbfChannel = null;
			this.dbfBuffer = null;
			this.dBaseIterator = null;
			this.containsDbfFile = false;
		}
		
	}

	
	
	public ShapefileHeader getShapefileHeader() {
		return shpIterator.getHeader();
	}
	
	
	
	public DBaseHeader getDBaseHeader() {
		return dBaseIterator.getHeader();
	}
	
	
	
	@Override
	public boolean hasNext() {
		return shpIterator.hasNext() && (containsDbfFile || dBaseIterator.hasNext());
	}

	
	
	@Override
	public ShpDbfRecord next() {
		return new ShpDbfRecord(shpIterator.next(), containsDbfFile ? dBaseIterator.next() : null);
	}

	
	
	@Override
	public void close() throws IOException {
		Cleaner cleaner;
		if((cleaner = ((sun.nio.ch.DirectBuffer) shpBuffer).cleaner()) != null) cleaner.clean();
		shpIterator.close();
		shpChannel.close();
		if(containsDbfFile) {
			if((cleaner = ((sun.nio.ch.DirectBuffer) dbfBuffer).cleaner()) != null) cleaner.clean();
			dBaseIterator.close();
			dbfChannel.close();
		}
	}

	
	
	@Override @Nonnull
	public Iterator<ShpDbfRecord> iterator() {
		return this;
	}
	
}
