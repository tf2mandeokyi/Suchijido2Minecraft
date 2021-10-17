package com.mndk.scjd2mc.core.scjd;

import com.mndk.scjd2mc.core.projection.Proj4jProjection;
import com.mndk.scjd2mc.core.triangulator.TerrainTriangulator;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxInteger;
import com.mndk.scjd2mc.core.util.shape.Triangle;
import com.mndk.scjd2mc.core.util.shape.TriangleList;
import lombok.Getter;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdalconst.gdalconst;

import java.io.File;

public class SuchijidoFile extends SuchijidoData {

    @Getter private final String mapIndex;
    @Getter private final Proj4jProjection projection;

    public SuchijidoFile(File file) {
        super();
        this.projection = ScjdIndexManager.getProjectionFromMapName(file.getName());
        this.mapIndex = ScjdIndexManager.getMapIndexFromFileName(file.getName());
    }

    public void extractGeoTiff(Driver geoTiffDriver, File directory, SuchijidoFile... additionalFiles) {
        BoundingBoxInteger bbox = this.boundingBox.toMaximumBoundingBoxInteger();
        int xsize = bbox.xmax - bbox.xmin + 1, zsize = bbox.zmax - bbox.zmin + 1;

        File file = new File(directory, this.mapIndex + ".tif");

        Dataset dataset = geoTiffDriver.Create(file.getAbsolutePath(), xsize, zsize, 1, gdalconst.GDT_Float32);
        Band band = dataset.GetRasterBand(1);

        SuchijidoData combined = new SuchijidoData();
        combined.append(this);
        for(SuchijidoFile dataFile : additionalFiles) combined.append((dataFile));
        TriangleList interpolatedResult = TerrainTriangulator.useCDT(combined);

        for(Triangle t : interpolatedResult) {
            for(int z = t.minZ; z <= t.maxZ; ++z)  for(int x = t.minX; x <= t.maxX; ++x) {
                if(t.contains(x + .5, z + .5) == null || !bbox.isPointInside(x, z)) continue;
                float height = (float) t.interpolateY(x + .5, z + .5);
                band.WriteRaster(x - bbox.xmin, z - bbox.zmin, 1, 1, new float[] { height });
            }
        }

        dataset.SetGeoTransform(new double[] { bbox.xmin, 1, 0, bbox.zmin, 0, 1 });
        dataset.SetProjection(this.projection.toWellKnownText());
        dataset.delete();
    }


    private static final int[][] surroundings = {
            { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { -1, 0 }, { -1, 1 }
    };
    public String[] getSurroundingIndexes() {
        int[] myPos = ScjdIndexManager.getTilePosition(this.mapIndex);
        int myScale = ScjdIndexManager.getTileScale(this.mapIndex);
        String[] result = new String[surroundings.length];
        for(int i = 0 ; i < surroundings.length; ++i) {
            result[i] = ScjdIndexManager.getTileIndex(
                    new int[] { myPos[0] + surroundings[i][0], myPos[1] + surroundings[i][1] }, myScale);
        }
        return result;
    }

}
