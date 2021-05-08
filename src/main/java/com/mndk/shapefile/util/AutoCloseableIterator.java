package com.mndk.shapefile.util;

import java.io.Closeable;
import java.util.Iterator;

public interface AutoCloseableIterator<T> extends Iterator<T>, Iterable<T>, Closeable, AutoCloseable {}
