
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;

import json.converter.csv.CSVReader;
import json.converter.shp.ShpFileReader;
import json.geojson.FeatureCollection;
import json.geojson.objects.Bounding;
import json.graphic.BasicColorifier;
import json.graphic.Display;
import json.graphic.JenksColorifierGeojson;
import json.topojson.algorithm.ArcMap;
import json.topojson.api.TopojsonApi;
import json.topojson.topology.Topology;


public class testAssociation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String aYear = "2010";
		
		String[][] aFilter = {{"STATEA", "500" }, {"GJOIN"+aYear, ".+"}};

		try {

			CSVReader aExtReader = new CSVReader("./data/nhgis0002_ts_tract.csv");
			aExtReader.read();
			ShpFileReader aReader = new ShpFileReader("./data/US_tract_2010.shp", "esri:102003", aFilter);
			aReader.mergeWithAssociation("GISJOIN",aExtReader, "NHGISCODE");
			aReader.read();

			Display _display;
			
			FeatureCollection aFeat = aReader.getGroupRecord();
		
			Bounding aBound = aFeat.getMergedBound();
			
			System.out.println("Shapes size:"+aFeat._shapes.size());
			
			int x = 1024;
			_display = new Display(x, (int) (x/aBound.getRatioXY()));
			_display.start();
			
			_display.setBound(aBound);
			
			aFeat.fill(_display, new JenksColorifierGeojson(aFeat,"AV0AA125"));
			aFeat.draw(_display, Color.white);
			
			_display.saveImage("./data/STATE_"+aFilter[0][1]+"_"+aYear+"_high.png");
			
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
