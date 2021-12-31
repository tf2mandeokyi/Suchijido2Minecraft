package com.mndk.scjdmc.ngiparser.ngi.element;

import com.mndk.scjdmc.ngiparser.ngi.NgiLayer;
import com.mndk.scjdmc.ngiparser.ngi.gattr.NgiTextGAttribute;
import com.mndk.scjdmc.ngiparser.ngi.vertex.NgiVector;

public class NgiText extends NgiRecord<NgiTextGAttribute> {

	public NgiText(NgiLayer parent) {
		super(parent);
	}
	
	public String text;
	public NgiVector position;

}
