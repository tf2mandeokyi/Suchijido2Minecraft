package com.mndk.ngiparser.ngi;

import java.util.Map;

import com.mndk.ngiparser.ngi.element.NgiRecord;

public class NgiLayer {

	public String name;
	public NgiHeader header;
	public Map<Integer, NgiRecord<?>> data;

}