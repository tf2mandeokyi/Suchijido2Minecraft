# KVectorMap2Minecraft

The mod that imports Korean vector map files (such as ~~.dxf~~ and .ngi) to Minecraft, using BTE Dymaxion projection.

## Info
### Required mods
 * [TerraPlusPlus](https://github.com/BuildTheEarth/terraplusplus)
 * [WorldEdit](https://github.com/EngineHub/WorldEdit)

### Supported file types
 * .ngi

### Terrain generation algorithm
Fast delaunay triangulation ([Original source](https://github.com/mapbox/delaunator), [Java implementation](src/main/java/com/mndk/kvm2m/core/util/delaunator/FastDelaunayTriangulator.java))

## Usage
1. Download vector map data files [from one of those portals](#vector-map-data-portals)
2. Put downloaded files at:
    * Singleplayer: `.../.minecraft/saves/<world save file>/kvecmap_files/`
    * Multiplayer: `/path/to/server_file/<world folder>/kvecmap_files/`
3. Select the region you want the mod to generate with WorldEdit wand.
4. Type the command `/gen<extension_name>map <data_file_name> ...`

### Vector map data portals
 * [국토정보플랫폼 국토정보맵 (NGII Interactive Map)](http://map.ngii.go.kr/ms/map/NlipMap.do) (For ~~.dxf~~, .ngi, .zip files)
 * [국가공간정보포털 (National Spatial Data Infrastructure Portal)](http://data.nsdi.go.kr/organization/a05016) (For ~~.dxf~~ files)

## Copyright
All of the korean map data and their format are copyrighted by [국토지리정보원 (National Geographic Information Institute)](https://www.ngii.go.kr/).

## Screenshot

![Reference screenshot](docs/screenshot0.png)
