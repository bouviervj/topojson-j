import graphics.CensusColorifier;

import java.awt.Color;
import java.io.IOException;

import json.converter.csv.merger.Merger;
import json.converter.csv.merger.Merger.MergeStep;
import json.geojson.FeatureCollection;
import json.graphic.Display;
import json.topojson.algorithm.ArcMap;
import json.topojson.api.TopojsonApi;
import json.topojson.topology.Topology;


public class testFillPolygon {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		Display aDisplay = new Display(800, 800);
		aDisplay.start();
		aDisplay.clear();

		String aYear = "2010";

		String[][] aFilter = {{"STATEA", "250" }, {"GJOIN"+aYear, ".+"}};
		
		Merger aMerger = new Merger();
		aMerger.addStep(new MergeStep("GISJOIN","%s","./data/nhgis0002_ts_tract.csv", "NHGISCODE","%s"));
		
		FeatureCollection aFeat = TopojsonApi.shpToGeojsonFeatureCollection("./data/US_tract_2010.shp", "esri:102003", aFilter,  aMerger);
		
		ArcMap aMap = TopojsonApi.joinCollection(aFeat);
		
		Topology[][] aRes = TopojsonApi.tileFeatureCollectionToTopojson(aFeat , aMap,  6, "MA");

		int n = 0;
		int m = 1;
		
		aDisplay.setBound(aRes[n][m]._bnd);

		CensusColorifier aColorifier = new CensusColorifier(aRes[n][m],"AV0AA125");
		
		aRes[n][m].fill(aDisplay,aColorifier);
		//aRes[n][m].draw(aDisplay);
		aDisplay.render();

		
	}

}
