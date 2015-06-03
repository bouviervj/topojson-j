package json.graphic;

import java.awt.Color;

import json.graphic.Colorifier;
import json.topojson.topology.Topology;

public class BasicColorifier extends Colorifier  {

	Color _color;
	
	public BasicColorifier(Color iColor) {
		_color = iColor;
	}
	
	@Override
	public Color getColor(Object properties) {
		// TODO Auto-generated method stub
		return _color;
	}

	@Override
	public double[] getClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
