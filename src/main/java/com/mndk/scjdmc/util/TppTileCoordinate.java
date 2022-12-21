package com.mndk.scjdmc.util;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.util.file.DirectoryManager;
import lombok.RequiredArgsConstructor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TppTileCoordinate {

    private static final int SCALE = 64;
    private static final double INV_SCALE = 1 / (double) SCALE;

    public final int x, y;


    public BoundingBox getBoundingBox() {
        double minX = x * INV_SCALE, minY = y * INV_SCALE;
        return new ReferencedEnvelope(minX, minX + INV_SCALE, minY, minY + INV_SCALE, Constants.CRS84);
    }


    public Geometry getTileGeometry() {
        return getTileGeometry(x, y, x, y);
    }


    private static Geometry getTileGeometry(int tileMinX, int tileMinY, int tileMaxX, int tileMaxY) {
        double minX =  tileMinX    * INV_SCALE, minY =  tileMinY    * INV_SCALE;
        double maxX = (tileMaxX+1) * INV_SCALE, maxY = (tileMaxY+1) * INV_SCALE;
        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(minX, minY), new Coordinate(maxX, minY),
                new Coordinate(maxX, maxY), new Coordinate(minX, maxY),
                new Coordinate(minX, minY)
        };
        LinearRing linearRing = new LinearRing(
                Constants.GEOMETRY_FACTORY.getCoordinateSequenceFactory().create(coordinates),
                Constants.GEOMETRY_FACTORY
        );
        return new Polygon(linearRing, new LinearRing[0], Constants.GEOMETRY_FACTORY);
    }


    public File getJsonLocation(File tppDatasetFolder, boolean createParents) throws IOException {
        File result = Paths.get(tppDatasetFolder.getAbsolutePath(), "tile", x + "", y + ".json").toFile();
        if(createParents) DirectoryManager.createFolder(result.getParentFile());
        return result;
    }


    public File getFolderLocation(File tppDatasetFolder, boolean createFolder) throws IOException {
        File result = Paths.get(tppDatasetFolder.getAbsolutePath(), "tile", x + "", y + "").toFile();
        if(createFolder) DirectoryManager.createFolder(result);
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TppTileCoordinate that = (TppTileCoordinate) o;
        return x == that.x && y == that.y;
    }


    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }


    public static TppTileCoordinateMinMax getTileCoordinateMinMax(BoundingBox boundingBox) {
        double minX = boundingBox.getMinX(), maxX = boundingBox.getMaxX();
        double minY = boundingBox.getMinY(), maxY = boundingBox.getMaxY();

        return new TppTileCoordinateMinMax(
                new TppTileCoordinate((int) Math.floor(minX * SCALE), (int) Math.floor(minY * SCALE)),
                new TppTileCoordinate((int) Math.floor(maxX * SCALE), (int) Math.floor(maxY * SCALE))
        );
    }


    public static Set<TppTileCoordinate> getBoundingBoxIntersections(BoundingBox boundingBox) {
        TppTileCoordinateMinMax minMax = getTileCoordinateMinMax(boundingBox);
        Set<TppTileCoordinate> result = new HashSet<>();
        for(int y = minMax.getMinY(); y <= minMax.getMaxY(); y++) {
            for(int x = minMax.getMinX(); x <= minMax.getMaxX(); x++) {
                result.add(new TppTileCoordinate(x, y));
            }
        }

        return result;
    }


    public static Set<TppTileCoordinate> getFeatureGeometryIntersections(SimpleFeature feature) {
        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        return getBoundingBoxIntersections(feature.getBounds()).stream()
                .filter(coord -> geometry.intersects(coord.getTileGeometry()))
                .collect(Collectors.toSet());
    }


    public static Map<TppTileCoordinate, Geometry> divideFeatureGeometryToTiles(
            SimpleFeature feature, boolean bufferFeatureGeometry
    ) {
        TppTileCoordinateMinMax minMax = getTileCoordinateMinMax(feature.getBounds());
        Geometry featureGeometry = (Geometry) feature.getDefaultGeometry();
        if(bufferFeatureGeometry) featureGeometry.buffer(Constants.POLYGON_BUFFER_EPSILON);

        Map<TppTileCoordinate, Geometry> result = new HashMap<>();
        divideGeometryToTiles(
                result, featureGeometry,
                minMax.getMinX(), minMax.getMinY(), minMax.getMaxX(), minMax.getMaxY()
        );
        return result;
    }


    private static void divideGeometryToTiles(
            Map<TppTileCoordinate, Geometry> result, Geometry victim,
            int tileMinX, int tileMinY, int tileMaxX, int tileMaxY
    ) {
        if(!victim.isValid()) victim = victim.buffer(0);

        Geometry tileGeometry = getTileGeometry(tileMinX, tileMinY, tileMaxX, tileMaxY)
                .buffer(Constants.POLYGON_BUFFER_EPSILON);
        Geometry intersection = victim.intersection(tileGeometry);
        if(intersection.isEmpty()) return;

        int xCount = tileMaxX - tileMinX + 1, yCount = tileMaxY - tileMinY + 1;
        int X2 = xCount / 2, Y2 = yCount / 2;

        if(xCount == 1 && yCount == 1) {
            result.put(new TppTileCoordinate(tileMinX, tileMinY), intersection);
        }
        else if(xCount == 1) {
            divideGeometryToTiles(result, intersection,
                    tileMinX, tileMinY,
                    tileMaxX, tileMinY + Y2 - 1
            );
            divideGeometryToTiles(result, intersection,
                    tileMinX, tileMinY + Y2,
                    tileMaxX, tileMaxY
            );
        }
        else if(yCount == 1) {
            divideGeometryToTiles(result, intersection,
                    tileMinX, tileMinY,
                    tileMinX + X2 - 1, tileMaxY
            );
            divideGeometryToTiles(result, intersection,
                    tileMinX + X2, tileMinY,
                    tileMaxX, tileMaxY
            );
        }
        else {
            divideGeometryToTiles(result, intersection,
                    tileMinX, tileMinY,
                    tileMinX + X2 - 1, tileMinY + Y2 - 1
            );
            divideGeometryToTiles(result, intersection,
                    tileMinX + X2, tileMinY,
                    tileMaxX, tileMinY + Y2 - 1
            );
            divideGeometryToTiles(result, intersection,
                    tileMinX, tileMinY + Y2,
                    tileMinX + X2 - 1, tileMaxY
            );
            divideGeometryToTiles(result, intersection,
                    tileMinX + X2, tileMinY + Y2,
                    tileMaxX, tileMaxY
            );
        }
    }


    @RequiredArgsConstructor
    public static class TppTileCoordinateMinMax {
        public final TppTileCoordinate min, max;
        public int getMinX() { return min.x; }
        public int getMinY() { return min.y; }
        public int getMaxX() { return max.x; }
        public int getMaxY() { return max.y; }
    }

}
