package com.mndk.scjdmc.util;

import com.mndk.scjdmc.Constants;
import com.mndk.scjdmc.util.file.DirectoryManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor @Getter
public class TppTileCoordinate {

    private static final int SCALE = 64;
    private static final double INV_SCALE = 1 / (double) SCALE;

    private final int x, y;

    public BoundingBox getBoundingBox() {
        double minX = x * INV_SCALE, minY = y * INV_SCALE;
        return new ReferencedEnvelope(minX, minX + INV_SCALE, minY, minY + INV_SCALE, Constants.CRS84);
    }

    public File getJsonLocation(File datasetFolder, boolean createParents) throws IOException {
        File result = Paths.get(datasetFolder.getAbsolutePath(), "tile", x + "", y + ".json").toFile();
        if(createParents) DirectoryManager.createFolder(result.getParentFile());
        return result;
    }

    public File getFolderLocation(File datasetFolder, boolean createFolder) throws IOException {
        File result = Paths.get(datasetFolder.getAbsolutePath(), "tile", x + "", y + "").toFile();
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

    public static Set<TppTileCoordinate> getBBoxContainingCoordinates(BoundingBox boundingBox) {
        double minX = boundingBox.getMinX(), maxX = boundingBox.getMaxX();
        double minY = boundingBox.getMinY(), maxY = boundingBox.getMaxY();

        Set<TppTileCoordinate> result = new HashSet<>();
        int tileMinX = (int) Math.floor(minX * SCALE), tileMaxX = (int) Math.floor(maxX * SCALE);
        int tileMinY = (int) Math.floor(minY * SCALE), tileMaxY = (int) Math.floor(maxY * SCALE);
        for(int y = tileMinY; y <= tileMaxY; y++) for(int x = tileMinX; x <= tileMaxX; x++) {
            result.add(new TppTileCoordinate(x, y));
        }

        return result;
    }
}
