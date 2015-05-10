package json.graphic;

import java.awt.Color;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;

import json.algorithm.Jenks;
import json.geojson.Feature;
import json.geojson.FeatureCollection;
import json.topojson.topology.Topology;

import json.topojson.geom.GeometryCollection;
import json.topojson.geom.Object;

public class JenksColorifierGeojson extends  Colorifier {

	String _param;
	double[] _ranges;
	FeatureCollection _collection;

	public JenksColorifierGeojson(FeatureCollection iCollection, String iParam) {

		_collection = iCollection;
		_param = iParam;

		init();

	}

	public void init(){
		
		TreeSet<Double> aArrayProp = new TreeSet<Double>();

		for (Feature aFeature:_collection._shapes.values()){

			java.lang.Object aProp  = aFeature.getProperty(_param);
			if (aProp instanceof String ) {

				java.lang.Object toconvert =aProp;
				
				if (toconvert!=null) {
					try {
						aArrayProp.add(new Double((String) toconvert));
					} catch (java.lang.NumberFormatException e){
						// Unable to decode this string
					}
				}

			}

		}
		
		_ranges = Jenks.computeJenks(5, ArrayUtils.toPrimitive(aArrayProp.toArray(new Double[aArrayProp.size()])));

		for (double aValue:_ranges){
			System.out.println("Range:"+aValue);
		}

	}

	int[] Colors = {
			0xedf8fb,
			0xb2e2e2,
			0x66c2a4,
			0x2ca25f,
			0x006d2c
	};

	@Override
	public Color getColor(java.lang.Object properties) {

		if (properties  instanceof HashMap<?,?>) {

			HashMap<String,java.lang.Object> aProp = (HashMap<String,java.lang.Object>) properties;

			try {

				double value = new Double((String) aProp.get(_param));

				for (int i=0;i<Colors.length; i++) {
					if ((_ranges[i]<=value) && (_ranges[i+1]>value)){
						
						float R = (Colors[i]>>16 & 0xFF)/256.0f;
						float G = (Colors[i]>>8 & 0xFF)/256.0f;
						float B = (Colors[i] & 0xFF)/256.0f;
						
						return new Color( R , G, B, 0.5f );
					}
				}

			} catch (java.lang.NumberFormatException e){
				// Unable to decode this string
			}

		}

		return new Color(0x00AFAFAF);
	}

}
