package com.mndk.kvm2m.core.vmap;

import java.io.IOException;

public class VMapParserException extends IOException {
	public VMapParserException(String message) {
		super(message);
	}
	public VMapParserException(Throwable t) {
		super(t);
	}
}
