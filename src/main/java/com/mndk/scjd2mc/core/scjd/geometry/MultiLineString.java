package com.mndk.scjd2mc.core.scjd.geometry;

import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.FlatRegion;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiLineString extends GeometryShape<LineString[]> {

    public MultiLineString(LineString[] shape) {
        super(shape, ElementGeometryType.MULTILINESTRING);
    }


    public int size() {
        return shape.length;
    }


    @Override
    protected BoundingBoxDouble shapeToBoundingBox(LineString[] shape) {
        BoundingBoxDouble result = shape[0].boundingBox;
        for(int i = 1; i < shape.length; ++i) {
            result = result.or(shape[i].boundingBox);
        }
        return result;
    }


    @Override
    public void generateBlocks(
            @Nonnull ElementStyleSelector.ScjdElementStyle[] styles, FlatRegion region, World world, TriangleList triangles) {

        for(LineString l : shape) {
            l.generateBlocks(styles, region, world, triangles);
        }
    }


    @Override
    public Object toSerializableCoordinates() {
        List<Object> result = new ArrayList<>();
        for(LineString lineString : shape) {
            result.add(lineString.toSerializableCoordinates());
        }
        return result;
    }


    @Override
    protected void coordinatesToStream(DataOutputStream stream) throws IOException {
        stream.writeInt(shape.length);
        for(LineString lineString : shape) {
            lineString.coordinatesToStream(stream);
        }
    }


    public static MultiLineString streamToCoordinates(DataInputStream stream, GeographicProjection projection)
            throws IOException, OutOfProjectionBoundsException {
        int size = stream.readInt();
        LineString[] result = new LineString[size];
        for(int i = 0; i < size; ++i) {
            result[i] = LineString.streamToCoordinates(stream, projection);
        }
        return new MultiLineString(result);
    }

}
