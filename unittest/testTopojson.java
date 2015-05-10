import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;


import json.converter.csv.CSVReader;
import json.converter.shp.ShpFileReader;
import json.geojson.FeatureCollection;
import json.graphic.Display;
import json.tools.Compress;
import json.tools.Toolbox;
import json.topojson.algorithm.ArcMap;
import json.topojson.api.TopojsonApi;
import json.topojson.geom.sub.Entity;
import json.topojson.topology.Topology;

import org.junit.Test;

import com.google.gson.Gson;



public class testTopojson {

	@Test
	public void test() throws IOException {

		for (int i=0; i<6; i++) {
			int aVal = (int) Math.pow(10, i);
			TopojsonApi.shpToTopojsonFile("./data/MA.shp", "nad83:2001",
					"./web/topojson_"+aVal+".json", 
					"MA", 
					aVal, 
					4, 
					true);		
		}

	}
	
	@Test
	public void testAssociation() throws IOException {

			String[][] aFilter = {{"STATEA", "25" }};
			
			CSVReader aExtReader = new CSVReader("./data/US_NHGIS_2000.csv");
			aExtReader.read();
			ShpFileReader aReader = new ShpFileReader("./data/US.shp", "esri:102003", aFilter);
			//aReader.mergeWithAssociation("GISJOIN",aExtReader, "GISJOIN");
			aReader.read();
			
			Display aDisplay = new Display(1024, 600);
			aDisplay.start();
			aDisplay.clear();

			FeatureCollection aFeat = aReader.getGroupRecord();
			
			ArcMap aMap = TopojsonApi.joinCollection(aFeat);
			
			Topology[][] aRes = TopojsonApi.tileFeatureCollectionToTopojson(aFeat , aMap,  6,
					"MA");

			aDisplay.setBound(aRes[0][0]._bnd);


			aRes[5][5].draw(aDisplay);
			aDisplay.render();
		
	}

	@Test
	public void testDecompress() throws IOException {

		String iJsonC = TopojsonApi.shpToTopojson("./data/MA.shp", "nad83:2001",
				"MA", 
				10, 
				4, 
				true);

		String iJson;
		try {
			iJson = Compress.decompressB64(iJsonC);
			System.out.println(iJson);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	@Test
	public void createAnim() throws IOException {


		ShpFileReader aReader = new ShpFileReader("./data/US.shp", "esri:102003");
		aReader.read();

		FeatureCollection aCollection = aReader.getGroupRecord();

		List<Entity> aEntities = aCollection.extract();
		Entity.join(aEntities);

		ArcMap aMap = new ArcMap();
		for (Entity aEntity:aEntities){
			aEntity.cut(aMap);
		}

		File aDir = new File("./web/anim");
		aDir.mkdirs();

		Display aDisplay = new Display(1024, 600);
		aDisplay.setBound(aCollection.getMergedBound());
		aDisplay.start();

		Topology aTopology = new Topology();

		aTopology.addObject("MA" , aCollection.toTopology());

		int aStep = (5000-10)/30;
		for (int i=0; i<30; i++){

			aTopology.setArcs(aMap);

			aTopology.simplify(10/*+i*aStep*/);

			aDisplay.clear();
			aTopology.draw(aDisplay);
			aDisplay.render();
			aDisplay.saveImage(aDir.getAbsolutePath()+File.separator+"anim"+String.format("%03d", i)+".png");

		}

	}

	@Test
	public void testTile() throws IOException {

		Display aDisplay = new Display(1024, 600);
		aDisplay.start();
		aDisplay.clear();

		FeatureCollection aFeat = TopojsonApi.shpToGeojsonFeatureCollection("./data/MA.shp", "esri:102003");
		
		ArcMap aMap = TopojsonApi.joinCollection(aFeat);
		
		Topology[][] aRes = TopojsonApi.tileFeatureCollectionToTopojson(aFeat , aMap,  14,
				"MA");

		aDisplay.setBound(aRes[0][0]._bnd);


		aRes[5][5].draw(aDisplay);
		aDisplay.render();

	}	

}
