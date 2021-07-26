package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.projection.Korea2010BeltProjection;
import com.mndk.scjd2mc.core.projection.Projections;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.scjd.elem.ScjdElement;
import com.mndk.scjd2mc.core.scjd.elem.line.ScjdLineString;
import com.mndk.scjd2mc.core.scjd.elem.point.ScjdPoint;
import com.mndk.scjd2mc.core.scjd.elem.poly.ScjdPolygon;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.scjd2mc.mod.event.ServerTickRepeater;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuchijidoUtils {



	private static final Pattern generalMapIdPattern = Pattern.compile("^\\(.{4}\\)수치지도_(\\d+)_");



	public static String getMapIndexFromFileName(String fileName) {
		Matcher matcher = generalMapIdPattern.matcher(fileName);
		if(matcher.find()) {
			return matcher.group(1);
		}
		return fileName;
	}



	public static Korea2010BeltProjection getProjectionFromMapName(String fileName) {

		fileName = getMapIndexFromFileName(fileName);
		char number = fileName.charAt(2);

		if(number == '5') {
			return Projections.KOREA2010_WEST;
		} else if(number == '6' || number == '7') {
			return Projections.KOREA2010_CENTRAL;
		} else if(number == '8' || number == '9') {
			return Projections.KOREA2010_EAST;
		} else if(number == '0' || number == '1'){
			return Projections.KOREA2010_EASTSEA;
		}
		return null;
	}



	public static void setBlock(World world, BlockPos pos, IBlockState state) {
		ServerTickRepeater.addTask(new ServerTickRepeater.BlockTask(world, pos, state));
	}



	public static byte[] generateGeometryDataBytes(ScjdElement element) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		if(element instanceof ScjdPoint) {
			Vector2DH v = ((ScjdPoint) element).getPosition();

			dos.writeByte(ElementGeometryType.POINT.ordinal());
			dos.writeDouble(v.x);
			dos.writeDouble(v.z);
		}
		else {
			if (element instanceof ScjdPolygon) {
				dos.writeByte(ElementGeometryType.POLYGON.ordinal());
			}
			else if(element instanceof ScjdLineString) {
				dos.writeByte(ElementGeometryType.LINESTRING.ordinal());
			}
			else {
				return new byte[] { (byte) ElementGeometryType.NULL.ordinal() };
			}
			ScjdLineString lineString = (ScjdLineString) element;
			Vector2DH[][] lines = lineString.getVertexList();
			dos.writeInt(lines.length);
			for (Vector2DH[] line : lines) {
				boolean closed = lineString.isClosed();
				if(closed) {
					if(line[line.length - 1].equalsXZ(line[0])) {
						closed = false;
					}
				}
				dos.writeInt(closed ? line.length + 1 : line.length);
				for (Vector2DH point : line) {
					dos.writeDouble(point.x);
					dos.writeDouble(point.z);
				}
				if(closed) {
					dos.writeDouble(line[0].x);
					dos.writeDouble(line[0].z);
				}
			}
		}

		return bos.toByteArray();
	}



	/**
	 * @return Object part: Either a class of point (Vector2DH) or a list of lines (Vector2DH[][])
	 */
	public static ScjdDataPayload.Geometry.Record<?> parseBinaryGeometryDataString(
			InputStream geometryStream, GeographicProjection projection) throws IOException, OutOfProjectionBoundsException {

		DataInputStream dis = new DataInputStream(geometryStream);
		int firstByte = dis.readByte();
		ElementGeometryType type = ElementGeometryType.values()[firstByte];

		switch(type) {
			case POINT:
				return new ScjdDataPayload.Geometry.Record<>(
						ElementGeometryType.POINT, readVector(dis, projection)
				);
			case LINESTRING:
			case POLYGON:
				int lineCount = dis.readInt();
				Vector2DH[][] result = new Vector2DH[lineCount][];
				for(int i = 0; i < lineCount; ++i) {
					int pointCount = dis.readInt();
					result[i] = new Vector2DH[pointCount];
					for(int j = 0; j < pointCount; ++j) {
						result[i][j] = readVector(dis, projection);
					}
				}
				return new ScjdDataPayload.Geometry.Record<>(type, result);
			default:
				return new ScjdDataPayload.Geometry.Record<>(ElementGeometryType.NULL, null);
		}
	}



	private static Vector2DH readVector(DataInputStream dis, GeographicProjection projection)
			throws IOException, OutOfProjectionBoundsException {

		Vector2DH parsedPoint = new Vector2DH(dis.readDouble(), dis.readDouble());
		double[] projResult = projection.fromGeo(parsedPoint.x, parsedPoint.z);
		return new Vector2DH(projResult[0], projResult[1]);
	}


}
