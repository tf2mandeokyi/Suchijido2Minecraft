package com.mndk.kvm2m.core.vmap;

import java.io.IOException;

public class VMapReaderException extends IOException {
	public VMapReaderException(String message) {
		super(message);
	}
	public VMapReaderException(Throwable t) {
		super(t);
	}
}
