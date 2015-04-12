package json.graphic;

import java.awt.Color;
import java.util.HashMap;

import json.topojson.topology.Topology;

public abstract class Colorifier {

	public Topology _topo;
	
	public Colorifier(Topology iTopo){
		_topo = iTopo;
	}
	
	public abstract Color getColor(HashMap<String,java.lang.Object> properties);

}
