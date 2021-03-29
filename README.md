# KVectorMap2Minecraft

The mod that imports Korean vector map files (such as ~~.dxf~~ and .ngi) to Minecraft, using BTE Dymaxion projection.

## Required mods
 * [TerraPlusPlus](https://github.com/BuildTheEarth/terraplusplus)
 * [WorldEdit](https://github.com/EngineHub/WorldEdit)

## Supported file types
 * .ngi

## Terrain generation algorithm
Fast delaunay triangulation ([Original source](https://github.com/mapbox/delaunator), [Java implementation](src/main/java/com/mndk/kvm2m/core/util/delaunator/FastDelaunayTriangulator.java))

## Copyright

All of the korean map data and their format are copyrighted by [국토지리정보원 (National Geographic Information Institute)](https://www.ngii.go.kr/).<br>
You can get their data at:
 * [국토정보플랫폼 국토정보맵 (NGII Interactive Map)](http://map.ngii.go.kr/ms/map/NlipMap.do) (For ~~.dxf~~, .ngi, .zip files)
 * [국가공간정보포털 (National Spatial Data Infrastructure Portal)](http://data.nsdi.go.kr/organization/a05016) (For ~~.dxf~~ files)

## Screenshot

![Reference screenshot](docs/screenshot0.png)
