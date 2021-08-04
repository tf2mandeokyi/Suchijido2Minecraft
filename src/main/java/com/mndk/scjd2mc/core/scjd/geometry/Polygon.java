package com.mndk.scjd2mc.core.scjd.geometry;

import com.mndk.scjd2mc.core.scjd.SuchijidoUtils;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.math.VectorMath;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxInteger;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.FlatRegion;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Polygon extends GeometryShape<LineString[]> {


    public Polygon(LineString... shape) {
        super(shape, ElementGeometryType.POLYGON);
    }


    public int size() {
        return shape.length;
    }


    public boolean containsPoint(Vector2DH point) {

        boolean result = false;

        for (LineString vertex : shape) {
            boolean inside = false;
            Vector2DH[] points = vertex.getShape();
            for (int i = 0, j = points.length - 1; i < points.length; j = i++) {
                if (VectorMath.checkRayXIntersection(point, points[i], points[j])) {
                    inside = !inside;
                }
            }
            if (inside) {
                result = !result;
            }
        }
        return result;
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

        for(LineString line : shape) {
            line.generateBlocks(styles, region, world, triangles);
        }

        BoundingBoxInteger box = boundingBox.toMaximumBoundingBoxInteger().and(new BoundingBoxInteger(region));
        if(!box.isValid()) return;

        for(ElementStyleSelector.ScjdElementStyle style : styles) {
            if(style == null) continue; if(style.state == null) continue;

            for(int z = box.zmin; z <= box.zmax; ++z) {
                for(int x = box.xmin; x <= box.xmax; ++x) {
                    if(!region.contains(new Vector(x, region.getMinimumY(), z)) || !this.containsPoint(new Vector2DH(x+.5, z+.5))) continue;

                    int y = (int) Math.round(triangles.interpolateHeight(x, z)) + style.y;

                    SuchijidoUtils.setBlock(world, new BlockPos(x, y, z), style.state);
                }
            }
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


    public static Polygon streamToCoordinates(DataInputStream stream, GeographicProjection projection)
            throws IOException, OutOfProjectionBoundsException {
        int size = stream.readInt();
        LineString[] result = new LineString[size];
        for(int i = 0; i < size; ++i) {
            result[i] = LineString.streamToCoordinates(stream, projection);
        }
        return new Polygon(result);
    }

}
