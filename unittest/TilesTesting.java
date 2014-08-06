import java.io.FileNotFoundException;

import json.geojson.FeatureCollection;
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
	
	
	public TilesTesting(String iFileName, int iN, int iM){
		
		_display = new Display(1024, 600);
		_display.start();
		_display.clear();
		_display.setDisplayListener(this);

		_N = iN;
		_M = iM;
		
		_C_N = iN/2;
		_C_M = iM/2;
		
		try {
			
			FeatureCollection aFeat = TopojsonApi.shpToGeojsonFeatureCollection(iFileName);
			
			ArcMap aMap = TopojsonApi.joinCollection(aFeat);
			
			_res = TopojsonApi.tileFeatureCollectionToTopojson(aFeat, aMap,
					_N,_M,
					"MA"
					);
			
			view();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void view(){
		
		_display.clear();
		_display.setBound(_res[_C_N][_C_M]._bnd);
		_res[_C_N][_C_M].simplify(10);
		_res[_C_N][_C_M].draw(_display);
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
		
		new TilesTesting("./data/MA.shp", 16, 16);
		
	}

}
