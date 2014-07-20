package json.geojson.objects;


import json.JsonInterface;
import json.graphic.Display;


public abstract class Object implements JsonInterface {

	public abstract boolean partlyIn(Bounding iBnd);
	
	public abstract void draw(Display iDisp);
	
	public abstract Object clone();
	
}
