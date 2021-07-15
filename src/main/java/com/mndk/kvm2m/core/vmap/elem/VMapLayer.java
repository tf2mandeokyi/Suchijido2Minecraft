package com.mndk.kvm2m.core.vmap.elem;

import com.mndk.kvm2m.core.vmap.VMapElementDataType;

import java.util.ArrayList;
import java.util.List;

public class VMapLayer extends ArrayList<VMapElement> implements Comparable<VMapLayer> {

	private final VMapElementDataType type;
	@Deprecated
	private final String[] dataColumns;
	
	public VMapLayer(List<VMapElement> elements, VMapElementDataType type, String[] dataColumns) {
		super(elements);
		this.type = type;
		this.dataColumns = dataColumns;
	}
	
	public VMapLayer(VMapElementDataType type, String[] dataColumns) {
		super();
		this.type = type;
		this.dataColumns = dataColumns;
	}
	
	public VMapElementDataType getType() {
		return this.type;
	}
	
	public int getDataColumnIndex(String columnName) {
		columnName = columnName.replaceAll("\\([^()]+\\)", "");
		for(int i = 0; i < this.dataColumns.length; ++i) {
			if(columnName.equals(this.dataColumns[i].replaceAll("\\([^()]+\\)", ""))) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int compareTo(VMapLayer o) {
		if(this.type.getPriority() != o.type.getPriority()) return this.type.getPriority() - o.type.getPriority();
		else return this.getType().compareTo(o.getType());
	}
	
}
