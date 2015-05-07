

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;

import json.converter.csv.CSVReader;
import json.converter.shp.ShpFileReader;
import json.geojson.FeatureCollection;
import json.geojson.objects.Bounding;
import json.graphic.BasicColorifier;
import json.graphic.Display;
import json.graphic.DisplayListener;
import json.topojson.algorithm.ArcMap;
import json.topojson.api.TopojsonApi;
import json.topojson.topology.Topology;


public class TilesTesting implements DisplayListener {

	Topology[][] _res;
	int _N, _C_N;
	int _M, _C_M;
	Display _display;


	public TilesTesting(String iFileName,int iZoom){

		_display = new Display(640, 640);
		_display.start();
		_display.clear();
		_display.setDisplayListener(this);


		try {

			//FeatureCollection aFeat = TopojsonApi.shpToGeojsonFeatureCollection(iFileName);

			String aYear = "2010";

			String[][] aFilter = {{"STATEA", "250" }, {"GJOIN"+aYear, ".+"}};

			CSVReader aExtReader = new CSVReader("./data/nhgis0002_ts_tract.csv");
			aExtReader.read();
			ShpFileReader aReader = new ShpFileReader("./data/US_tract_2010.shp", "esri:102003",  aFilter);
			aReader.mergeWithAssociation("GISJOIN",aExtReader, "NHGISCODE");
			aReader.read();

			Display _display;

			FeatureCollection aFeat = aReader.getGroupRecord();

			Bounding aBound = aFeat.getMergedBound();
			
			aFeat._bnd = aBound;
			
		//	System.out.println("Max:"+aBound.maxx+","+aBound.maxy);
		//	System.out.println("Min:"+aBound.minx+","+aBound.miny);

			ArcMap aMap = TopojsonApi.joinCollection(aFeat);

			_res = TopojsonApi.tileFeatureCollectionToTopojson(aFeat, aMap,
					iZoom,
					"MA"
					);
			
			_N = _res.length;
			_M = _res[0].length;

			_C_N = _res.length/2;
			_C_M =  _res[0].length/2;

			view();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void view(){

		_display.clear();
		
		System.out.println(_res[_C_N][_C_M]._bnd.toJson());
		
		_display.setBound(_res[_C_N][_C_M]._bnd);
		
		//_res[_C_N][_C_M].simplify(10);
		_res[_C_N][_C_M].fill(_display, new  BasicColorifier(Color.green));
		_res[_C_N][_C_M].draw(_display);
		//_res[_C_N][_C_M]._bnd.draw(_display);
		
		_display.render();

	}

	@Override
	public void up() {
		_C_M++;
		if (_C_M>=_M) _C_M = _M-1;
		view();
	}

	@Override
	public void down() {
		_C_M--;
		if (_C_M<0) _C_M = 0;
		view();
	}

	@Override
	public void left() {
		_C_N--;
		if (_C_N<0) _C_N = 0;
		view();
	}

	@Override
	public void right() {
		_C_N++;
		if (_C_N>_N) _C_N = _N-1;
		view();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new TilesTesting("", 7);

	}

}
