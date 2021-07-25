package com.mndk.scjd2mc.core.triangulator.cdt;


/**
 * IndexEdge connecting two vertices: vertex with smaller index is always first.<br>
 * Note: hash IndexEdge is specialized at the bottom
 * @author artem-ogre
 */
public class IndexEdge {

	public int v1, v2;
	
	public IndexEdge(int iV1, int iV2) {
		this.v1 = Math.min(iV1, iV2); this.v2 = Math.max(iV1, iV2);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IndexEdge) {
			IndexEdge e = (IndexEdge) obj;
			return e.v1 == v1 && e.v2 == v2;
		}
		return false;
	}
	
}
