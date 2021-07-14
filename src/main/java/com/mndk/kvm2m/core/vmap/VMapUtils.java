package com.mndk.kvm2m.core.vmap;

import com.mndk.kvm2m.core.projection.Korea2010BeltProjection;
import com.mndk.kvm2m.core.projection.Projections;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.line.VMapLineString;
import com.mndk.kvm2m.core.vmap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;
import com.mndk.kvm2m.mod.event.ServerTickRepeater;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VMapUtils {



	private static final Pattern generalMapIdPattern = Pattern.compile("^\\(.{4}\\)수치지도_(\\d+)");
	// private static final DecimalFormat decimalFormat = new DecimalFormat("#.0000000");



	public static Korea2010BeltProjection getProjectionFromMapName(String fileName) {

		Matcher matcher = generalMapIdPattern.matcher(fileName);
		if(matcher.find()) {
			fileName = matcher.group(1);
		}

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



	public static byte[] generateGeometryDataBytes(VMapElement element) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		if(element instanceof VMapPoint) {
			Vector2DH v = ((VMapPoint) element).getPosition();

			dos.writeByte(VMapElementGeomType.POINT.ordinal());
			dos.writeDouble(v.x);
			dos.writeDouble(v.z);
		}
		else {
			if (element instanceof VMapPolygon) {
				dos.writeByte(VMapElementGeomType.POLYGON.ordinal());
			}
			else if(element instanceof VMapLineString) {
				dos.writeByte(VMapElementGeomType.LINESTRING.ordinal());
			}
			else {
				return new byte[] { (byte) VMapElementGeomType.NULL.ordinal() };
			}
			Vector2DH[][] lines = ((VMapLineString) element).getVertexList();
			dos.writeInt(lines.length);
			for (Vector2DH[] line : lines) {
				dos.writeInt(line.length);
				for (Vector2DH point : line) {
					dos.writeDouble(point.x);
					dos.writeDouble(point.z);
				}
			}
		}

		return bos.toByteArray();
	}


	/**
	 * @return Object part: Either a class of point (Vector2DH) or a list of lines (Vector2DH[][])
	 */
	public static VMapGeometryPayload<?> parseGeometryDataString(InputStream geometryStream) throws IOException {
		DataInputStream dis = new DataInputStream(geometryStream);
		int firstByte = dis.readByte();
		VMapElementGeomType type = VMapElementGeomType.values()[firstByte];

		switch(type) {
			case POINT:
				return new VMapGeometryPayload<>(
						VMapElementGeomType.POINT,
						new Vector2DH(dis.readDouble(), dis.readDouble())
				);
			case LINESTRING:
			case POLYGON:
				int lineCount = dis.readInt();
				Vector2DH[][] result = new Vector2DH[lineCount][];
				for(int i = 0; i < lineCount; ++i) {
					int pointCount = dis.readInt();
					result[i] = new Vector2DH[pointCount];
					for(int j = 0; j < pointCount; ++j) {
						result[i][j] = new Vector2DH(dis.readDouble(), dis.readDouble());
					}
				}
				return new VMapGeometryPayload<>(type, result);
			default:
				return new VMapGeometryPayload<>(VMapElementGeomType.NULL, null);
		}
	}


}
