import java.io.FileNotFoundException;
import java.io.IOException;

import json.geojson.FeatureCollection;
import json.graphic.Display;
import json.topojson.algorithm.ArcMap;
import json.topojson.api.TopojsonApi;
import json.topojson.topology.Topology;


public class TilesExplosion {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
			
		Topology[][] _res;
		int _N, _C_N;
		int _M, _C_M;
		Display _display;
		
		_display = new Display(1024, 600);
		_display.start();
		_display.clear();
		//_display.setDisplayListener(this);
	
		try {
			
			FeatureCollection aFeat = TopojsonApi.shpToGeojsonFeatureCollection("./data/MA.shp", "esri:102003");
			
			aFeat._bnd.scale(1.9);
			_display.setBound(aFeat._bnd);
			
			ArcMap aMap = TopojsonApi.joinCollection(aFeat);
			
			_res = TopojsonApi.tileFeatureCollectionToTopojson(aFeat, aMap,
					10,
					"MA"
					);
			
			int iN = _res.length;
			int iM = _res[0].length;
			
			_N = iN;
			_M = iM;
			
			_C_N = iN/2;
			_C_M = iM/2;
			
			double CX=(aFeat._bnd.maxx+aFeat._bnd.minx)/2;
			double CY=(aFeat._bnd.maxy+aFeat._bnd.miny)/2;
			
			for (int i=0;i<_N; i++) {
				
				for (int j=0; j<_M; j++) {
					
					Topology aTopo = _res[i][j];
					aTopo.simplify(10000);
					
					double CPX = (aTopo._bnd.maxx+aTopo._bnd.minx)/2;
					double CPY = (aTopo._bnd.maxy+aTopo._bnd.miny)/2;
					
					double fact = 1.3;
					double Vx = (CPX-CX)*fact;
					double Vy = (CPY-CY)*fact;
					
					_res[i][j].draw(Vx,Vy, _display);
					
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
