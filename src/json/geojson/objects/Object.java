package json.geojson.objects;


import java.awt.Color;

import json.JsonInterface;
import json.graphic.Colorifier;
import json.graphic.Display;


public abstract class Object implements JsonInterface {

	public abstract boolean partlyIn(Bounding iBnd);
	
	//public abstract void draw(Display iDisp);
	
	public abstract void draw(Display iDisplay, Color iColor);
	
	public abstract void fill(Display iDisplay, Colorifier iColor);
	
	public abstract Object clone();
	
}
