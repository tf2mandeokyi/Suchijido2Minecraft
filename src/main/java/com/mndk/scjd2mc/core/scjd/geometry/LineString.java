package com.mndk.scjd2mc.core.scjd.geometry;

import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.util.LineGenerator;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.FlatRegion;
import lombok.Getter;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineString extends GeometryShape<Vector2DH[]> {


    @Getter private final boolean closed;


    public LineString(Vector2DH[] shape, boolean closed) {
        super(shape, ElementGeometryType.LINESTRING);
        this.closed = closed;
    }


    public LineString(Vector2DH[] shape) {
        this(shape, false);
    }


    public int size() {
        return shape.length;
    }


    @Override
    protected BoundingBoxDouble shapeToBoundingBox(Vector2DH[] shape) {
        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxX = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
        for(Vector2DH v : shape) {
            if(v.x < minX) minX = v.x;
            if(v.x > maxX) maxX = v.x;
            if(v.z < minZ) minZ = v.z;
            if(v.z > maxZ) maxZ = v.z;
        }
        return new BoundingBoxDouble(minX, minZ, maxX, maxZ);
    }


    public static void generateOutline(
            Vector2DH[] line, boolean closed, @Nonnull ElementStyleSelector.ScjdElementStyle[] styles, FlatRegion region,
            World world, TriangleList triangles) {

        for(ElementStyleSelector.ScjdElementStyle style : styles) {
            if(style == null) continue; if(style.state == null) continue;

            LineGenerator lineGenerator = new LineGenerator.TerrainLine(
                    (x, z) -> (int) Math.round(triangles.interpolateHeight(x, z)) + style.y,
                    world, region, style.state
            );

            for (int i = 0; i < line.length - 1; ++i) {
                lineGenerator.generate(line[i], line[i + 1]);
            }
            if (closed && !line[0].equalsXZ(line[line.length - 1])) {
                lineGenerator.generate(line[line.length - 1], line[0]);
            }
        }
    }


    @Override
    public void generateBlocks(
            @Nonnull ElementStyleSelector.ScjdElementStyle[] styles, FlatRegion region, World world, TriangleList triangles) {

        generateOutline(shape, this.closed, styles, region, world, triangles);
    }


    @Override
    public Object toSerializableCoordinates() {
        List<List<Double>> result = new ArrayList<>();
        for(Vector2DH v : this.shape) {
            result.add(v.toRoundDoubleList());
        }
        return result;
    }


    @Override
    protected void coordinatesToStream(DataOutputStream stream) throws IOException {
        stream.writeInt(shape.length);
        for(Vector2DH v : shape) {
            stream.writeDouble(v.x);
            stream.writeDouble(v.z);
        }
    }


    public static LineString streamToCoordinates(DataInputStream stream, GeographicProjection projection)
            throws IOException, OutOfProjectionBoundsException {
        int size = stream.readInt();
        Vector2DH[] result = new Vector2DH[size];
        for(int i = 0; i < size; ++i) {
            result[i] = readVectorFromStream(stream, projection);
        }
        return new LineString(result, result[size - 1].equalsXZ(result[0]));
    }

}
