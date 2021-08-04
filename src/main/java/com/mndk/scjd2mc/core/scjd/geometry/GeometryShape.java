package com.mndk.scjd2mc.core.scjd.geometry;

import com.mndk.scjd2mc.core.scjd.type.ElementGeometryType;
import com.mndk.scjd2mc.core.scjd.type.ElementStyleSelector;
import com.mndk.scjd2mc.core.util.math.Vector2DH;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import com.sk89q.worldedit.regions.FlatRegion;
import lombok.Getter;
import lombok.ToString;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Getter @ToString
public abstract class GeometryShape<T> {


    protected final BoundingBoxDouble boundingBox;
    private final ElementGeometryType type;
    protected final T shape;


    public GeometryShape(T shape, ElementGeometryType type) {
        this.shape = shape;
        this.type = type;
        this.boundingBox = this.shapeToBoundingBox(shape);
    }


    protected abstract BoundingBoxDouble shapeToBoundingBox(final T shape);


    public abstract void generateBlocks(
            @Nonnull ElementStyleSelector.ScjdElementStyle[] styles, FlatRegion region, World world, TriangleList triangles);


    public abstract Object toSerializableCoordinates();


    public final void toGeometryBytes(DataOutputStream stream) throws IOException {
        stream.write(type.ordinal());
        this.coordinatesToStream(stream);
    }
    protected abstract void coordinatesToStream(DataOutputStream stream) throws IOException;


    public static GeometryShape<?> fromGeometryBytes(DataInputStream stream, GeographicProjection projection)
            throws IOException, OutOfProjectionBoundsException {

        int temp = stream.read();
        switch(temp) {
            case 1: return Point.streamToCoordinates(stream, projection);
            case 2: return LineString.streamToCoordinates(stream, projection);
            case 3: return MultiLineString.streamToCoordinates(stream, projection);
            case 4: return Polygon.streamToCoordinates(stream, projection);
            case 5: return MultiPolygon.streamToCoordinates(stream, projection);
            default: throw new IOException("Illegal shape enum type : " + temp);
        }
    }


    protected static Vector2DH readVectorFromStream(DataInputStream dis, GeographicProjection projection)
            throws IOException, OutOfProjectionBoundsException {

        Vector2DH parsedPoint = new Vector2DH(dis.readDouble(), dis.readDouble());
        double[] projResult = projection.fromGeo(parsedPoint.x, parsedPoint.z);
        return new Vector2DH(projResult[0], projResult[1]);
    }

}
