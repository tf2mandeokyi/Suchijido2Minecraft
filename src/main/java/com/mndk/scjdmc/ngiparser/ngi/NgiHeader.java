package com.mndk.scjdmc.ngiparser.ngi;

import com.mndk.scjdmc.ngiparser.nda.NdaDataColumn;
import com.mndk.scjdmc.ngiparser.ngi.gattr.NgiLineGAttribute;
import com.mndk.scjdmc.ngiparser.ngi.gattr.NgiPointGAttribute;
import com.mndk.scjdmc.ngiparser.ngi.gattr.NgiRegionGAttribute;
import com.mndk.scjdmc.ngiparser.ngi.gattr.NgiTextGAttribute;

import java.util.Map;

public class NgiHeader {
	public int version;
	public int dimensions;
	public Boundary bound;
	public Map<Integer, NgiPointGAttribute> symbolGAttrs;
	public Map<Integer, NgiLineGAttribute> lineGAttrs;
	public Map<Integer, NgiRegionGAttribute> regionGAttrs;
	public Map<Integer, NgiTextGAttribute> textGAttrs;
	
	public NdaDataColumn[] columns;

	public NgiHeader() { }
	
	public int getColumnIndex(String name) {
		for(int i = 0; i < columns.length; ++i) {
			if(columns[i].name.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public static class Boundary {
		public double[] min, max;
		public Boundary(double[] min, double[] max) {
			if(min.length != max.length) throw new NgiSyntaxErrorException("Boundary min.dimension != max.dimension");
			this.min = min; this.max = max;
		}
	}
}
