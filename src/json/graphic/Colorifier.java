package json.graphic;

import java.awt.Color;
import java.util.HashMap;

import json.geojson.FeatureCollection;
import json.topojson.topology.Topology;

public abstract class Colorifier {

	public Colorifier(){
	}
	
	public abstract Color getColor(Object  properties);

}
