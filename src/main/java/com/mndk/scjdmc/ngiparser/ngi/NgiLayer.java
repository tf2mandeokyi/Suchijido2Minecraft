package com.mndk.scjdmc.ngiparser.ngi;

import com.mndk.scjdmc.ngiparser.ngi.element.NgiRecord;

import java.util.Map;

public class NgiLayer {

	public String name;
	public NgiHeader header;
	public Map<Integer, NgiRecord<?>> data;

}