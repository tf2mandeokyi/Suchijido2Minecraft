package com.mndk.scjd2mc.core.scjd.elem;

import com.mndk.scjd2mc.core.scjd.type.ElementDataType;

import java.util.ArrayList;
import java.util.List;

public class ScjdLayer extends ArrayList<ScjdElement<?>> implements Comparable<ScjdLayer> {

	private final ElementDataType type;
	
	public ScjdLayer(List<ScjdElement<?>> elements, ElementDataType type) {
		super(elements);
		this.type = type;
	}
	
	public ScjdLayer(ElementDataType type) {
		super();
		this.type = type;
	}
	
	public ElementDataType getType() {
		return this.type;
	}
	
	public int getDataColumnIndex(String columnName) {
		return this.type.getColumns().getIndexByName(columnName);
	}

	@Override
	public int compareTo(ScjdLayer o) {
		if(this.type.getPriority() != o.type.getPriority()) return this.type.getPriority() - o.type.getPriority();
		else return this.getType().compareTo(o.getType());
	}
	
}
