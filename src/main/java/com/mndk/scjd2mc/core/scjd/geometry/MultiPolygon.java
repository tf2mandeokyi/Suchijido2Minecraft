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

public class MultiPolygon extends GeometryShape<Polygon[]> {


    public MultiPolygon(Polygon[] shape) {
        super(shape, ElementGeometryType.MULTIPOLYGON);
    }


    public int size() {
        return shape.length;
    }


    @Override
    protected BoundingBoxDouble shapeToBoundingBox(Polygon[] shape) {
        BoundingBoxDouble result = shape[0].boundingBox;
        for(int i = 1; i < shape.length; ++i) {
            result = result.or(shape[i].boundingBox);
        }
        return result;
    }


    @Override
    public void generateBlocks(
            @Nonnull ElementStyleSelector.ScjdElementStyle[] styles, FlatRegion region, World world, TriangleList triangles) {

        for(Polygon p : shape) {
            p.generateBlocks(styles, region, world, triangles);
        }
    }


    @Override
    public Object toSerializableCoordinates() {
        List<Object> result = new ArrayList<>();
        for(Polygon polygon : shape) {
            result.add(polygon.toSerializableCoordinates());
        }
        return result;
    }


    @Override
    protected void coordinatesToStream(DataOutputStream stream) throws IOException {
        stream.writeInt(shape.length);
        for(Polygon polygon : shape) {
            polygon.coordinatesToStream(stream);
        }
    }


    public static MultiPolygon streamToCoordinates(DataInputStream stream, GeographicProjection projection)
            throws IOException, OutOfProjectionBoundsException {
        int size = stream.readInt();
        Polygon[] result = new Polygon[size];
        for(int i = 0; i < size; ++i) {
            result[i] = Polygon.streamToCoordinates(stream, projection);
        }
        return new MultiPolygon(result);
    }
}
