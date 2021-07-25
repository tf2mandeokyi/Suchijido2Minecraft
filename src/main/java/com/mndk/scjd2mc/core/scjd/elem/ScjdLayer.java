package com.mndk.scjd2mc.core.scjd.elem;

import com.mndk.scjd2mc.core.scjd.type.ElementDataType;

import java.util.ArrayList;
import java.util.List;

public class ScjdLayer extends ArrayList<ScjdElement> implements Comparable<ScjdLayer> {

	private final ElementDataType type;
	@Deprecated
	private final String[] dataColumns;
	
	public ScjdLayer(List<ScjdElement> elements, ElementDataType type, String[] dataColumns) {
		super(elements);
		this.type = type;
		this.dataColumns = dataColumns;
	}
	
	public ScjdLayer(ElementDataType type, String[] dataColumns) {
		super();
		this.type = type;
		this.dataColumns = dataColumns;
	}
	
	public ElementDataType getType() {
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
	public int compareTo(ScjdLayer o) {
		if(this.type.getPriority() != o.type.getPriority()) return this.type.getPriority() - o.type.getPriority();
		else return this.getType().compareTo(o.getType());
	}
	
}
