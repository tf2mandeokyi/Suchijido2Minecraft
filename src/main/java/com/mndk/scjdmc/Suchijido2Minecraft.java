package com.mndk.scjdmc;

import com.mndk.scjdmc.cdtlib.Triangulation;

import java.io.File;

public class Suchijido2Minecraft {


    // Select mode (show debug UI if "--debug-ui" option provided)
    // Mode 0: Index files
    // Mode 1: Area + Index files
    // Mode A: OSM elements
    // Mode B: Contour rasterization

    // Mode 0: Index files
    // 1. Read indexed .zip files in a selected directory
    // 2. Pick one indexed .zip file, and extract it into a temporal folder
    // A. OSM elements
    //    3. Select all osm-convertible .shp files in a temporal folder,
    //        and convert them into a single geojson file named "<index>.json"
    //    4. Repeat 2~3 for every .zip files in a selected directory
    //    5. For each T++ tile index, put all intersecting suchijido indexed geojson files' contents in the
    //        corresponding t++ geojson tile
    // B. Contour rasterization
    //    3. Convert contours & elevation points from .shp to a single geojson file in a temporal folder,
    //        and then delete the temporal folder
    //    4. Convert the geojson file into .tif imagery data, using Contour rasterization
    //    5. Repeat 2~4 for every .zip files in a selected directory
    //    6. (Future) With qgis library, combine all .tif files into a single elevation dataset

    // Mode 1: Area + Index files
    // 1. Read every area files' area data
    // 2. Find every index map not prefectly filled. Include them if it exists in another folder, else notify
    // 3. Move every element in area file into its according T++ index geojson file

    public static void main(String[] args) throws Throwable {

        // Test
        Triangulation triangulation = new Triangulation();

        ScjdConversionWorkingDirectory workingDirectory = new ScjdConversionWorkingDirectory(
                new File("D:\\gis\\gradle_scjd2tpp_test")
        );
        workingDirectory.setDebug(true);
//        workingDirectory.relocateAllAreas();
//        workingDirectory.doCoastlineRelocation();
        workingDirectory.combineGeoJsonFiles();
    }
}
