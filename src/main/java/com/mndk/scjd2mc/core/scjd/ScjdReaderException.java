package com.mndk.scjd2mc.core.scjd;

import java.io.IOException;

public class ScjdReaderException extends IOException {
	public ScjdReaderException(String message) {
		super(message);
	}
	public ScjdReaderException(Throwable t) {
		super(t);
	}
}
