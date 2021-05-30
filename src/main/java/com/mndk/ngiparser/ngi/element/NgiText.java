package com.mndk.ngiparser.ngi.element;

import com.mndk.ngiparser.ngi.NgiLayer;
import com.mndk.ngiparser.ngi.gattr.NgiTextGAttribute;
import com.mndk.ngiparser.ngi.vertex.NgiVector;

public class NgiText extends NgiRecord<NgiTextGAttribute> {

	public NgiText(NgiLayer parent) {
		super(parent);
	}
	
	public String text;
	public NgiVector position;

}
