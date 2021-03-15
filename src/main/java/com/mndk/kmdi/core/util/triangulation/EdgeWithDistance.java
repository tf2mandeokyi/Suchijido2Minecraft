package com.mndk.kmdi.core.util.triangulation;

public class EdgeWithDistance implements Comparable<EdgeWithDistance> {
	
	public Edge edge;
    public double distance;

    public EdgeWithDistance(Edge edge, double distance) {
        this.edge = edge;
        this.distance = distance;
    }

    @Override
    public int compareTo(EdgeWithDistance o) {
        return Double.compare(this.distance, o.distance);
    }
}
