package com.mndk.ngiparser.ngi;

import java.util.Map;

import com.mndk.ngiparser.ngi.element.NgiLineElement;
import com.mndk.ngiparser.ngi.element.NgiPointElement;
import com.mndk.ngiparser.ngi.element.NgiPolygonElement;

public class NgiHeader {
    public int version;
    public int dimensions;
    public Boundary bound;
    public Map<Integer, NgiPointElement.Attr> pointAttributes;
    public Map<Integer, NgiLineElement.Attr> lineAttributes;
    public Map<Integer, NgiPolygonElement.Attr> polygonAttributes;

    public NgiHeader() { }

    public static class Boundary {
        public double[] min, max;
        public Boundary(double[] min, double[] max) {
            if(min.length != max.length) throw new NgiParseException("Boundary min.dimension != max.dimension");
            this.min = min; this.max = max;
        }
    }
}
