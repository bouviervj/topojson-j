package json.topojson.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.TreeSet;

import javax.xml.bind.DatatypeConverter;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import json.converter.shp.ShpFileReader;
import json.geojson.FeatureCollection;
import json.tools.Compress;
import json.tools.Toolbox;
import json.topojson.algorithm.ArcMap;
import json.topojson.geom.sub.Entity;
import json.topojson.topology.Topology;

import com.google.gson.Gson;

public class TopojsonApi {

	public static FeatureCollection shpToGeojsonFeatureCollection(String iFileName) throws FileNotFoundException {
		
		ShpFileReader aReader = new ShpFileReader(iFileName);
		aReader.read();
		
		return aReader.getGroupRecord();
		
	}
	
	public static Topology shpToTopology(String iFileName, String iTopoName ) throws FileNotFoundException {
		
		FeatureCollection aFeat = shpToGeojsonFeatureCollection(iFileName);
		
		List<Entity> aEntities = aFeat.extract();
		Entity.join(aEntities);
		
		ArcMap aMap = new ArcMap();
		for (Entity aEntity:aEntities){
			aEntity.cut(aMap);
		}
	
		Topology aTopology = new Topology();
		
		aTopology.addObject(iTopoName , aFeat.toTopology());
		
		aTopology.setArcs(aMap);
	
		return aTopology;
		
	}
	
	public static String getJson(Topology iTopology, boolean iCompress){
		
		Gson aGson = new Gson();
		String aJson = aGson.toJson(iTopology);
		
		if (iCompress) {
			return Compress.compressB64(aJson);
		} 
		return aJson;
		
	}
	
	public static String shpToTopojson(String iFileName, String iTopoName, int iKink, int iQuantizeDigit, boolean iCompress ) throws FileNotFoundException {
		
		Topology aTopology = shpToTopology(iFileName, iTopoName );
		
		if (iKink>0)aTopology.simplify(iKink);
		if (iQuantizeDigit>0) aTopology.quantize(iQuantizeDigit);
		
		return getJson(aTopology,iCompress);
		
	}
	
	public static void shpToTopojsonFile(String iFileNameInput, String iFileOuput, String iTopoName, int iKink, int iQuantizeDigit, boolean iCompress ) throws FileNotFoundException {
		
		String aJson = shpToTopojson( iFileNameInput , iTopoName, iKink,  iQuantizeDigit, iCompress );
		Toolbox.writeFile(iFileOuput, aJson);
		
	}
	
	public static Topology[][] tileFeatureCollectionToTopojson(FeatureCollection iCollection, int iN, int iM, String iTopoName, int iKink) throws FileNotFoundException{
		
		FeatureCollection aFeat = iCollection;//shpToGeojsonFeatureCollection(iFileNameInput);
		
		List<Entity> aEntities = aFeat.extract();
		Entity.join(aEntities);
		
		// at that point we record arcs in the map
		// if we're doing that now it's to make tiles 
		// match together. Some arcs may have been build 
		// differently , then may have been reduced !=
		ArcMap aMap = new ArcMap();
		for (Entity aEntity:aEntities){
			aEntity.cut(aMap);
		}
		
		// Here the grid contains feature collections with references 
		// to same arcs indexes i.e. indexes are not following
		// All the work consists in rebuilding own arc maps and indexes
		FeatureCollection[][] aGrid = aFeat.groupGridDivide(iN, iM);
		
		// build result array
		Topology[][] aResult = new Topology[iN][];
		for (int i=0; i<iN; i++) {
		
			aResult[i] = new Topology[iM];
			
			for (int j=0; j<iM; j++) {
				
				// this collection have now independent entities
				// i.e we can change entity leafs without impacting 
				// other FeatureCollection in the grid
				FeatureCollection aCollection = (FeatureCollection) aGrid[i][j].clone();
				
				// arcs gives all arcs we have to draw to get the tile display
				int[] aArcs = aCollection.arcs();
				TreeSet<Integer> aSet = new TreeSet<Integer>();
				for (int aArc:aArcs){
					aSet.add(aArc);
				}
				// gives the unicity
				
				// this is the new map to rebuild entity references
				ArcMap aNewMap = aMap.rebuild(aSet.toArray(new Integer[aSet.size()]));
				aCollection.rebuildIndexes(aNewMap);
				
				// The collection is ready , and also the new map
				Topology aTopology = new Topology();
				
				aTopology.addObject(iTopoName , aCollection.toTopology());
				
				aTopology.setArcs(aNewMap);
				
				aTopology.setBound(aCollection.getBounding());
				
				if (iKink!=0) aTopology.simplify(iKink); // destructive
				
				aResult[i][j] = aTopology;
				
			}
			
		}
		
		return aResult;
		
	}
	
		
}
