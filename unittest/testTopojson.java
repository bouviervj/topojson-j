import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import javax.imageio.ImageIO;


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
	public void test() throws FileNotFoundException {

		for (int i=0; i<6; i++) {
			int aVal = (int) Math.pow(10, i);
			TopojsonApi.shpToTopojsonFile("./data/MA.shp", 
					"./web/topojson_"+aVal+".json", 
					"MA", 
					aVal, 
					4, 
					true);		
		}

	}

	@Test
	public void testDecompress() throws FileNotFoundException {

		String iJsonC = TopojsonApi.shpToTopojson("./data/MA.shp", 
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
	public void createAnim() throws FileNotFoundException {


		ShpFileReader aReader = new ShpFileReader("./data/MA.shp");
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

			aTopology.simplify(10+i*aStep);

			aDisplay.clear();
			aTopology.draw(aDisplay);
			aDisplay.render();
			aDisplay.saveImage(aDir.getAbsolutePath()+File.separator+"anim"+String.format("%03d", i)+".png");

		}

	}

	@Test
	public void testTile() throws FileNotFoundException {

		Display aDisplay = new Display(1024, 600);
		aDisplay.start();
		aDisplay.clear();

		FeatureCollection aFeat = TopojsonApi.shpToGeojsonFeatureCollection("./data/MA.shp");
		
		Topology[][] aRes = TopojsonApi.tileFeatureCollectionToTopojson(aFeat , 8,8,
				"MA", 
				10);

		aDisplay.setBound(aRes[0][0]._bnd);


		aRes[5][5].draw(aDisplay);
		aDisplay.render();

	}	

}
