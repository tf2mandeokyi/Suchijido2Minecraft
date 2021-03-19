package com.mndk.ngiparser.ngi;

import java.util.Map;

import com.mndk.ngiparser.nda.NdaDataColumn;
import com.mndk.ngiparser.ngi.element.NgiLineElement;
import com.mndk.ngiparser.ngi.element.NgiPointElement;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;
import com.mndk.ngiparser.ngi.element.NgiTextElement;

public class NgiHeader {
    public int version;
    public int dimensions;
    public Boundary bound;
    public Map<Integer, NgiPointElement.Attr> pointAttributes;
    public Map<Integer, NgiLineElement.Attr> lineAttributes;
    public Map<Integer, NgiPolygonElement.Attr> polygonAttributes;
    public Map<Integer, NgiTextElement.Attr> textAttributes;
    
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
