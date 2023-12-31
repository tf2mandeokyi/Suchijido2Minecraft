package com.mndk.scjdmc.util.math;

import org.locationtech.jts.geom.Coordinate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public class Vector2DH {

	public final double x, height, z;

	public Vector2DH(double x, double height, double z) {
		this.x = x; this.height = height; this.z = z;
	}
	
	public Vector2DH(double x, double z) {
		this(x, 0, z);
	}

	public Vector2DH(Coordinate coordinate, double height) {
		this(coordinate.x, height, coordinate.y);
	}

	public Vector2DH(InputStream is) throws IOException {
		this(
				ByteBuffer.wrap(is.readNBytes(8)).getDouble(),
				ByteBuffer.wrap(is.readNBytes(8)).getDouble(),
				ByteBuffer.wrap(is.readNBytes(8)).getDouble()
		);
	}

	public byte[] toByteArray() {
		byte[] bytes = new byte[24];
		ByteBuffer.wrap(bytes, 0, 8).putDouble(x);
		ByteBuffer.wrap(bytes, 8, 8).putDouble(height);
		ByteBuffer.wrap(bytes, 16, 8).putDouble(z);
		return bytes;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + height + ", " + z + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Vector2DH v)) return false;
		return x == v.x && height == v.height && z == v.z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, height, z);
	}

	public boolean equalsXZ(Vector2DH v) {
		return x == v.x && z == v.z;
	}
	
}
