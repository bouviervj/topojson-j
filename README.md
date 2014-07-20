topojson-j
==========

Topojson library for Java, based on topojson [specifications](https://github.com/topojson/topojson-specification).
The base project reads .shp files, can convert them in Geojson files or Topojson files.
Added a functionality to compress generated files with LZ4 lib. 

*Example*
```java
// Reading a shp file and writing it as compressed topojson
TopojsonApi.shpToTopojsonFile("./data/MA.shp", 
										  "./web/topojson.json", 
										  "MA", 
										  1000, 
										  4, 
										  true);
```

*Base algorithm*
```java
// Extracting a FeatureCollection from shp file
FeatureCollection aCollection = shpToGeojsonFeatureCollection(iFileName);
		
// Then process the algorithm to create Topojson data
List<Entity> aEntities = aCollection.extract();
Entity.join(aEntities);
		
ArcMap aMap = new ArcMap();
for (Entity aEntity:aEntities){
	aEntity.cut(aMap);
}

Topology aTopology = new Topology();
aTopology.addObject( "MA" , aCollection.toTopology());
aTopology.setArcs(aMap);

aTopology.simplify( 1000 );
aTopology.quantize( 4 );
```
