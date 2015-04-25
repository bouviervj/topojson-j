package graphics;

import java.awt.Color;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;

import json.algorithm.Jenks;
import json.graphic.Colorifier;
import json.topojson.topology.Topology;

import json.topojson.geom.GeometryCollection;
import json.topojson.geom.Object;

public class CensusColorifier extends  Colorifier {

	String _param;
	double[] _ranges;
	Topology _topo;

	public CensusColorifier(Topology iTopo, String iParam) {

		_topo = iTopo;
		_param = iParam;

		init();

	}

	public void init(){

		for (Object aObject:_topo.objects.values()){
			System.out.println("Type:"+aObject.getClass());

			if (aObject instanceof GeometryCollection){

				GeometryCollection aCollect = (GeometryCollection) aObject;

				TreeSet<Double> aArrayProp = new TreeSet<Double>();

				// At first level we should have the list of polygons with their properties
				for (Object aIObject:aCollect.geometries){

					java.lang.Object aProps  = aIObject.getProperties();
					if (aProps instanceof HashMap<?,?>) {

						HashMap<String,java.lang.Object> aData = (HashMap<String,java.lang.Object>) aProps;

						/* Simple display */
						for (String aKey:aData.keySet()){
							System.out.print(aKey+":"+aData.get(aKey).toString()+" ");
						}
						System.out.println();

						java.lang.Object toconvert = aData.get(_param);
						System.out.println("Param:"+_param+" value:"+aData.get(_param));

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
						return new Color(Colors[i]);
					}
				}
			
			} catch (java.lang.NumberFormatException e){
				// Unable to decode this string
			}

		}

		return new Color(0x00AFAFAF);
	}

}
