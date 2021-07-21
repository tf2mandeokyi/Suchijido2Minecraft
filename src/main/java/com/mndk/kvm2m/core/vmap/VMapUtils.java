package com.mndk.kvm2m.core.vmap;

import com.mndk.kvm2m.core.projection.Korea2010BeltProjection;
import com.mndk.kvm2m.core.projection.Projections;
import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.elem.line.VMapContour;
import com.mndk.kvm2m.core.vmap.elem.line.VMapLineString;
import com.mndk.kvm2m.core.vmap.elem.line.VMapWall;
import com.mndk.kvm2m.core.vmap.elem.point.VMapElevationPoint;
import com.mndk.kvm2m.core.vmap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapBuilding;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;
import com.mndk.kvm2m.db.common.TableColumns;
import com.mndk.kvm2m.mod.event.ServerTickRepeater;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VMapUtils {



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
			VMapLineString lineString = (VMapLineString) element;
			Vector2DH[][] lines = lineString.getVertexList();
			dos.writeInt(lines.length);
			for (Vector2DH[] line : lines) {
				dos.writeInt(lineString.isClosed() ? line.length + 1 : line.length);
				for (Vector2DH point : line) {
					dos.writeDouble(point.x);
					dos.writeDouble(point.z);
				}
				if(lineString.isClosed()) {
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
	public static VMapGeometryPayload.Record<?> parseGeometryDataString(
			InputStream geometryStream, GeographicProjection projection) throws IOException, OutOfProjectionBoundsException {

		DataInputStream dis = new DataInputStream(geometryStream);
		int firstByte = dis.readByte();
		VMapElementGeomType type = VMapElementGeomType.values()[firstByte];

		switch(type) {
			case POINT:
				return new VMapGeometryPayload.Record<>(
						VMapElementGeomType.POINT, readVector(dis, projection)
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
				return new VMapGeometryPayload.Record<>(type, result);
			default:
				return new VMapGeometryPayload.Record<>(VMapElementGeomType.NULL, null);
		}
	}



	private static Vector2DH readVector(DataInputStream dis, GeographicProjection projection)
			throws IOException, OutOfProjectionBoundsException {

		Vector2DH parsedPoint = new Vector2DH(dis.readDouble(), dis.readDouble());
		double[] projResult = projection.fromGeo(parsedPoint.x, parsedPoint.z);
		return new Vector2DH(projResult[0], projResult[1]);
	}



	public static VMapReaderResult combineVMapPayloads(
			VMapGeometryPayload geometryPayload,
			VMapDataPayload dataPayload,
			Map<String, String> options
	) throws Exception {

		VMapReaderResult result = new VMapReaderResult();

		for(Map.Entry<Integer, VMapGeometryPayload.Record<?>> entry : geometryPayload.entrySet()) {
			int id = entry.getKey();
			VMapGeometryPayload.Record<?> geometryRecord = entry.getValue();
			VMapDataPayload.Record dataRecord = dataPayload.get(id);

			if(dataRecord == null) continue;

			Object[] dataRow = dataRecord.getDataRow();

			VMapElementGeomType geometryType = geometryRecord.getType();
			VMapElementDataType dataType = dataRecord.getType();

			VMapLayer layer = result.getLayer(dataType);
			if(layer == null) {
				TableColumns columns = dataType.getColumns();
				String[] stringColumns = new String[columns.getLength()];
				for(int i = 0; i < stringColumns.length; ++i) {
					stringColumns[i] = columns.get(i).getCategoryName();
				}
				result.addLayer(layer = new VMapLayer(dataType, stringColumns));
			}

			VMapElement element;
			Vector2DH[][] vertexList;

			switch(geometryType) {
				case POINT:
					Vector2DH point = (Vector2DH) geometryRecord.getGeometryData();
					if(dataType == VMapElementDataType.표고점) {
						element = new VMapElevationPoint(layer, point, dataRow);
					}
					else {
						element = new VMapPoint(layer, point, dataRow);
					}
					break;
				case LINESTRING:
					vertexList = (Vector2DH[][]) geometryRecord.getGeometryData();
					if(layer.getType() == VMapElementDataType.등고선) {
						element = new VMapContour(layer, vertexList[0], dataRow);
					}
					else if(layer.getType() == VMapElementDataType.옹벽) {
						element = new VMapWall(layer, vertexList, dataRow, false);
					}
					else {
						element = new VMapLineString(layer, vertexList, dataRow, false);
					}
					break;
				case POLYGON:
					vertexList = (Vector2DH[][]) geometryRecord.getGeometryData();

					if(layer.getType() == VMapElementDataType.건물) {
						if(options.containsKey("gen-building-shells")) {
							element = new VMapBuilding(layer, vertexList, dataRow);
						} else {
							element = new VMapLineString(layer, vertexList, dataRow, false);
						}
					}
					else {
						element = new VMapPolygon(layer, vertexList, dataRow, true);
					}
					break;
				default: continue;
			}

			layer.add(element);

		}

		return result;

	}


}
