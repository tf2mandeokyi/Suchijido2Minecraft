package com.mndk.scjdmc.util;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.util.file.DirectoryManager;
import lombok.RequiredArgsConstructor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public record TppTileCoordinate(int x, int y) {


    private static final int SCALE = 64;
    private static final double INV_SCALE = 1 / (double) SCALE;


    public BoundingBox getBoundingBox() {
        double minX = x * INV_SCALE, minY = y * INV_SCALE;
        return new ReferencedEnvelope(minX, minX + INV_SCALE, minY, minY + INV_SCALE, Constants.CRS84);
    }


    public Geometry getTileGeometry(double tileBuffer) {
        return getTileGeometry(x, y, x, y, tileBuffer);
    }


    private static Geometry getTileGeometry(int tileMinX, int tileMinY, int tileMaxX, int tileMaxY, double tileBuffer) {
        double minX = (tileMinX - tileBuffer) * INV_SCALE, minY = (tileMinY - tileBuffer) * INV_SCALE;
        double maxX = (tileMaxX + tileBuffer + 1) * INV_SCALE, maxY = (tileMaxY + tileBuffer + 1) * INV_SCALE;
        Coordinate[] coordinates = new Coordinate[]{
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
        if (createParents) DirectoryManager.createFolder(result.getParentFile());
        return result;
    }


    public File getFolderLocation(File tppDatasetFolder, boolean createFolder) throws IOException {
        File result = Paths.get(tppDatasetFolder.getAbsolutePath(), "tile", x + "", y + "").toFile();
        if (createFolder) DirectoryManager.createFolder(result);
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
    public String toString() {
        return "(" + x + ", " + y + ")";
    }


    public static TppTileCoordinateMinMax getTileCoordinateMinMax(BoundingBox boundingBox, double buffer) {
        double minX = boundingBox.getMinX(), maxX = boundingBox.getMaxX();
        double minY = boundingBox.getMinY(), maxY = boundingBox.getMaxY();

        return new TppTileCoordinateMinMax(
                new TppTileCoordinate((int) Math.floor(minX * SCALE - buffer), (int) Math.floor(minY * SCALE - buffer)),
                new TppTileCoordinate((int) Math.floor(maxX * SCALE + buffer), (int) Math.floor(maxY * SCALE + buffer))
        );
    }


    public static Set<TppTileCoordinate> getBoundingBoxIntersections(BoundingBox boundingBox, double buffer) {
        TppTileCoordinateMinMax minMax = getTileCoordinateMinMax(boundingBox, buffer);
        Set<TppTileCoordinate> result = new HashSet<>();
        for (int y = minMax.getMinY(); y <= minMax.getMaxY(); y++) {
            for (int x = minMax.getMinX(); x <= minMax.getMaxX(); x++) {
                result.add(new TppTileCoordinate(x, y));
            }
        }

        return result;
    }


    public static Set<TppTileCoordinate> getFeatureGeometryIntersections(SimpleFeature feature, double buffer) {
        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        return getBoundingBoxIntersections(feature.getBounds(), buffer).stream()
                .filter(coord -> geometry.intersects(coord.getTileGeometry(buffer)))
                .collect(Collectors.toSet());
    }


    public static Map<TppTileCoordinate, Geometry> divideFeatureGeometryToTiles(
            SimpleFeature feature, double featureBuffer, double tileBuffer
    ) {
        TppTileCoordinateMinMax minMax = getTileCoordinateMinMax(feature.getBounds(), tileBuffer);
        Geometry featureGeometry = (Geometry) feature.getDefaultGeometry();
        if (featureBuffer != 0) featureGeometry.buffer(Constants.POLYGON_BUFFER_EPSILON);

        Map<TppTileCoordinate, Geometry> result = new HashMap<>();
        divideGeometryToTiles(
                result, featureGeometry,
                minMax.getMinX(), minMax.getMinY(), minMax.getMaxX(), minMax.getMaxY(),
                tileBuffer
        );
        return result;
    }


    private static void divideGeometryToTiles(
            Map<TppTileCoordinate, Geometry> result, Geometry victim,
            int tileMinX, int tileMinY, int tileMaxX, int tileMaxY, double tileBuffer
    ) {
        if (!victim.isValid()) GeometryFixer.fix(victim);

        Geometry tileGeometry = getTileGeometry(tileMinX, tileMinY, tileMaxX, tileMaxY, tileBuffer)
                .buffer(Constants.POLYGON_BUFFER_EPSILON);
        Geometry intersection = victim.intersection(tileGeometry);
        if (intersection.isEmpty()) return;

        int xCount = tileMaxX - tileMinX + 1, yCount = tileMaxY - tileMinY + 1;
        int X2 = xCount / 2, Y2 = yCount / 2;

        if (xCount == 1 && yCount == 1) {
            result.put(new TppTileCoordinate(tileMinX, tileMinY), intersection);
        } else if (xCount == 1) {
            divideGeometryToTiles(result, intersection,
                    tileMinX, tileMinY, tileMaxX, tileMinY + Y2 - 1,
                    tileBuffer
            );
            divideGeometryToTiles(result, intersection,
                    tileMinX, tileMinY + Y2, tileMaxX, tileMaxY,
                    tileBuffer
            );
        } else if (yCount == 1) {
            divideGeometryToTiles(result, intersection,
                    tileMinX, tileMinY, tileMinX + X2 - 1, tileMaxY,
                    tileBuffer
            );
            divideGeometryToTiles(result, intersection,
                    tileMinX + X2, tileMinY, tileMaxX, tileMaxY,
                    tileBuffer
            );
        } else {
            divideGeometryToTiles(result, intersection,
                    tileMinX, tileMinY, tileMinX + X2 - 1, tileMinY + Y2 - 1,
                    tileBuffer
            );
            divideGeometryToTiles(result, intersection,
                    tileMinX + X2, tileMinY, tileMaxX, tileMinY + Y2 - 1,
                    tileBuffer
            );
            divideGeometryToTiles(result, intersection,
                    tileMinX, tileMinY + Y2, tileMinX + X2 - 1, tileMaxY,
                    tileBuffer
            );
            divideGeometryToTiles(result, intersection,
                    tileMinX + X2, tileMinY + Y2, tileMaxX, tileMaxY,
                    tileBuffer
            );
        }
    }


    public static Set<TppTileCoordinate> getAvailableFolderCoordinates(File tppDatasetFolder) {

        Set<TppTileCoordinate> result = new HashSet<>();

        File[] xFolders = new File(tppDatasetFolder, "tile").listFiles(File::isDirectory);
        assert xFolders != null;
        for (File xFolder : xFolders) {
            int x = Integer.parseInt(xFolder.getName());

            File[] yFolders = xFolder.listFiles(File::isDirectory);
            assert yFolders != null;
            for (File yFolder : yFolders) {
                int y = Integer.parseInt(yFolder.getName());
                result.add(new TppTileCoordinate(x, y));
            }
        }
        return result;
    }


    @RequiredArgsConstructor
    public static class TppTileCoordinateMinMax {
        public final TppTileCoordinate min, max;

        public int getMinX() {
            return min.x;
        }

        public int getMinY() {
            return min.y;
        }

        public int getMaxX() {
            return max.x;
        }

        public int getMaxY() {
            return max.y;
        }
    }

}
