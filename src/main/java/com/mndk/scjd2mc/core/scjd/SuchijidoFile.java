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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SuchijidoFile extends SuchijidoData {

    @Getter private final String mapIndex;
    @Getter private final Proj4jProjection projection;

    public SuchijidoFile(File file) {
        super();
        this.projection = SuchijidoUtils.getProjectionFromMapName(file.getName());
        this.mapIndex = SuchijidoUtils.getMapIndexFromFileName(file.getName());
    }

    public void extractGeoTiff(Driver geoTiffDriver, File directory) {
        BoundingBoxInteger bbox = this.boundingBox.toMaximumBoundingBoxInteger();
        int xsize = bbox.xmax - bbox.xmin + 1, zsize = bbox.zmax - bbox.zmin + 1;

        File file = new File(directory, this.mapIndex + ".tiff");

        Dataset dataset = geoTiffDriver.Create(file.getAbsolutePath(), xsize, zsize, 1, gdalconst.GDT_Float32);
        Band band = dataset.GetRasterBand(1);
        BufferedImage image = new BufferedImage(xsize, zsize, BufferedImage.TYPE_INT_RGB);

        TriangleList interpolatedResult = TerrainTriangulator.generateTerrain(this);

        for(Triangle t : interpolatedResult) {
            for(int z = t.minZ; z <= t.maxZ; ++z)  for(int x = t.minX; x <= t.maxX; ++x) {
                if(t.contains(x + .5, z + .5) == null || !bbox.isPointInside(x, z)) continue;

                float height = (float) t.interpolateY(x + .5, z + .5);

                band.WriteRaster(x - bbox.xmin, z - bbox.zmin, 1, 1, new float[] { height });
            }
        }

        try {
            ImageIO.write(image, "png", new File(directory, this.mapIndex + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataset.SetGeoTransform(new double[] { bbox.xmin, 1, 0, bbox.zmin, 0, 1 });
        dataset.SetProjection(this.projection.toWellKnownText());
        dataset.delete();
    }

}
