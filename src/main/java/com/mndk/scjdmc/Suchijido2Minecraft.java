package com.mndk.scjdmc;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.IntStream;

public class Suchijido2Minecraft {

    public static void main(String[] args) throws Throwable {
        ScjdConversionWorkingDirectory workingDirectory = new ScjdConversionWorkingDirectory(
                new File("D:\\gis\\gradle_scjd2tpp_test")
        );
        workingDirectory.setDebug(true);

        System.out.print("""
Operations
 | 1. Area relocation
 | 2. Area coastline relocation
 | 3. T++ geojson file combination
 | 4. Terrain test

Select operation number(s) [ex: "1", "23" ]:\s""");

        Scanner sc = new Scanner(System.in);
        String operationString = sc.nextLine();
        Set<Integer> operations = new HashSet<>();
        IntStream.range(0, operationString.length())
                .mapToObj(operationString::charAt)
                .map(String::valueOf)
                .map(Integer::parseInt)
                .forEach(operations::add);

        if(operations.contains(1)) workingDirectory.relocateAllAreas();
        if(operations.contains(2)) workingDirectory.doCoastlineRelocation();
        if(operations.contains(3)) workingDirectory.convertScjdGeoJsonToOsmGeoJson();
        if(operations.contains(4)) workingDirectory.terrainTest();
    }
}
