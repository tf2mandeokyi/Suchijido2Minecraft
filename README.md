# Suchijido2Minecraft

The mod that imports Korean National Geographic Data Map files (also known as "수치지도" made by [National Geographic Information Institute, 국토지리정보원](https://www.ngii.go.kr/)) to Minecraft via [Terra++](https://www.curseforge.com/minecraft/mc-mods/terraplusplus)'s terrain generating feature.

## Info

### Supported file types
 - .ngi
   * [Parser Implementation](src/main/java/com/mndk/ngiparser/NgiParser.java)
 - .shp & .dbf
   * [Parser Implementation](src/main/java/com/mndk/shapefile/ShpDbfDataIterator.java)
 - ~~.dxf (Removed)~~

### Terrain generation algorithm
 - Constraint Delaunay Triangulation (CDT)
   * [Original Github source](https://github.com/artem-ogre/CDT)
   * [Implementation](src/main/java/com/mndk/scjd2mc/core/triangulator/cdt/ConstraintDelaunayTriangulator.java)
 - ~~(Deprecated) Fast Delaunay Triangulation (FDT)~~
   * ~~[Original Github source](https://github.com/mapbox/delaunator)~~
   * ~~[Implementation](src/main/java/com/mndk/scjd2mc/core/triangulator/fdt/FastDelaunayTriangulator.java)~~

## Usage

## Copyright

## Screenshot
![Reference screenshot](docs/screenshot0.png)
