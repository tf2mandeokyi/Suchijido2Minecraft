package com.mndk.scjd2mc.core.scjd.geometry;

import com.mndk.scjd2mc.core.scjd.SuchijidoUtils;
import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.FlatRegion;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Point extends GeometryShape<Vector2DH> {


    public Point(Vector2DH shape) {
        super(shape, ElementGeometryType.POINT);
    }


    @Override
    protected BoundingBoxDouble shapeToBoundingBox(Vector2DH shape) {
        return new BoundingBoxDouble(shape.x, shape.z, shape.x, shape.z);
    }


    @Override
    public void generateBlocks(
            @Nonnull ElementStyleSelector.ScjdElementStyle[] styles, FlatRegion region, World world, TriangleList triangles) {

        for(ElementStyleSelector.ScjdElementStyle style : styles) {
            if(style == null) return; if(style.state == null) return;

            if(region.contains(shape.withHeight(region.getMinimumY()).toIntegerWorldEditVector())) {

                Vector2DH p = shape.withHeight(triangles.interpolateHeight((int) Math.floor(shape.x), (int) Math.floor(shape.z)) + style.y);
                SuchijidoUtils.setBlock(world, new BlockPos(p.x, p.height, p.z), style.state);
            }
        }
    }


    @Override
    public Object toSerializableCoordinates() {
        return shape.toRoundDoubleList();
    }

    @Override
    protected void coordinatesToStream(DataOutputStream stream) throws IOException {
        stream.writeDouble(shape.x);
        stream.writeDouble(shape.z);
    }

    public static Point streamToCoordinates(DataInputStream stream, GeographicProjection projection)
            throws IOException, OutOfProjectionBoundsException {
        return new Point(readVectorFromStream(stream, projection));
    }

}
