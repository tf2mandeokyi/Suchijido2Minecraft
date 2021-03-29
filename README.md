# KVectorMap2Minecraft

The mod that imports Korean vector map files (such as ~~.dxf~~ and .ngi) to Minecraft, using BTE Dymaxion projection.

![Reference screenshot](docs/screenshot0.png)

## Supported file types
 * .ngi

## Terrain generation algorithm
 * [Fast delaunay triangulation](https://github.com/mapbox/delaunator)

## Copyright

All of the korean map data and their format are copyrighted by [국토지리정보원 (National Geographic Information Institute)](https://www.ngii.go.kr/).<br>
You can get their data at:
 * [국토정보플랫폼 국토정보맵 (NGII's Interactive Map)](http://map.ngii.go.kr/ms/map/NlipMap.do) (For ~~.dxf~~, .ngi, .zip files)
 * [국가공간정보포털 (National Spatial Data Infrastructure Portal)](http://data.nsdi.go.kr/organization/a05016) (For ~~.dxf~~ files)
