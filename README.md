# KVectorMap2Minecraft

The mod that imports Korean vector map files (such as .ngi and .shp) to Minecraft, using the world's Terra++ projection.

## Info
### Required mods
 * [Terra++](https://www.curseforge.com/minecraft/mc-mods/terraplusplus)
   * [CubicChunks](https://www.curseforge.com/minecraft/mc-mods/opencubicchunks)
   * [CubicWorldGen](https://www.curseforge.com/minecraft/mc-mods/cubicworldgen)
 * [WorldEdit](https://www.curseforge.com/minecraft/mc-mods/worldedit)

### Supported file types
 * .ngi
 * .shp ( & .dbf)

### Terrain generation algorithm
*Modified* Fast delaunay triangulation
* [Original Github source](https://github.com/mapbox/delaunator)
* [FDT implementation](src/main/java/com/mndk/kvm2m/core/util/delaunator/FastDelaunayTriangulator.java)
* [Modified FDT implementation](src/main/java/com/mndk/kvm2m/core/util/delaunator/DelaunayTriangulationTerrainGenerator.java)

## Usage
1. Download vector map data files [from one of those portals](#vector-map-data-portals)
2. Put downloaded files at:
    * Singleplayer: `.../.minecraft/kvecmap_files/`
    * Multiplayer: `/path/to/server/file/kvecmap_files/`
3. Select the region you want the mod to generate with WorldEdit wand.
4. Type the command `/gen<extension_name>map <data_file_name> ...`

### Vector map data portals
 * [국토정보플랫폼 국토정보맵 (NGII Interactive Map)](http://map.ngii.go.kr/ms/map/NlipMap.do) (For .ngi, .zip (.shp & .dbf) files)

## Copyright
All of the korean map data and their format are copyrighted by [국토지리정보원 (National Geographic Information Institute)](https://www.ngii.go.kr/).

## Screenshot

![Reference screenshot](docs/screenshot0.png)
