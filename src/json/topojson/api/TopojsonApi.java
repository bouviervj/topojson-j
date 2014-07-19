package json.topojson.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import json.converter.shp.ShpFileReader;
import json.geojson.FeatureCollection;
import json.tools.Compress;
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
	
	public static Topology shpToTopology(String iFileName, String iTopoName, int iKink, int iQuantizeDigit, boolean iCompress ) throws FileNotFoundException {
		
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
	
	public static String shpToTopojson(String iFileName, String iTopoName, int iKink, int iQuantizeDigit, boolean iCompress ) throws FileNotFoundException {
		
		ShpFileReader aReader = new ShpFileReader(iFileName);
		aReader.read();
		
		FeatureCollection aCollection = aReader.getGroupRecord();
		
		List<Entity> aEntities = aCollection.extract();
		Entity.join(aEntities);
		
		ArcMap aMap = new ArcMap();
		for (Entity aEntity:aEntities){
			aEntity.cut(aMap);
		}
	
		Topology aTopology = new Topology();
		
		aTopology.addObject(iTopoName , aCollection.toTopology());
		
		aTopology.setArcs(aMap);
		
		if (iKink>0)aTopology.simplify(iKink);
		if (iQuantizeDigit>0) aTopology.quantize(iQuantizeDigit);
		
		Gson aGson = new Gson();
		String aJson = aGson.toJson(aTopology);
		
		if (iCompress) {
			return Compress.compressB64(aJson);
		} 
		return aJson;
		
	}
	
	public static void shpToTopojsonFile(String iFileNameInput, String iFileOuput, String iTopoName, int iKink, int iQuantizeDigit, boolean iCompress ) throws FileNotFoundException {
		
		String aJson = shpToTopojson( iFileNameInput , iTopoName, iKink,  iQuantizeDigit, iCompress );
		FileOutputStream aStream;
		try {
			aStream = new FileOutputStream(new File(iFileOuput));
			aStream.write(aJson.getBytes());
			aStream.flush();
			aStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
		
}
