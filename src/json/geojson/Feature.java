package json.geojson;


import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import json.geojson.objects.Bounding;
import json.geojson.objects.Object;
import json.geojson.objects.Shape;
import json.graphic.BasicColorifier;
import json.graphic.Colorifier;
import json.graphic.Display;
import json.topojson.algorithm.ArcMap;
import json.topojson.geom.sub.Entity;



public class Feature extends Shape {

	int _recordId;
	Shape _shape;
	HashMap<String,java.lang.Object> _properties;
	
	public Feature(int iRecordId, Shape iShape){
		
		_recordId = iRecordId;
		_shape = iShape;
		_properties = new HashMap<String,java.lang.Object>();
		_properties.put("bound", _shape.getBounding());
		
	}
	
	public void addProperty(String iName, String iData){
		_properties.put(iName, iData);
	}

	public HashMap<String,java.lang.Object>  getProperties(){
		return _properties;
	}
	
	public java.lang.Object getProperty(String iName){
		return _properties.get(iName);
	}
	
	public String valid(String iData){
		
		
		try {
			Float afloat = Float.parseFloat(iData);
			return String.format("%f",afloat);
		} catch (NumberFormatException aEx ) {
			System.err.println("Unable to parse float");
		}
		
		try {
			Integer aInt = Integer.parseInt(iData);
			return String.format("%d",aInt);
		} catch (NumberFormatException aEx ) {
			System.err.println("Unabme to parse int");
		}
		
		return "\""+iData.replaceAll("\"", "")+"\"";
		
	}
	
	@Override
	public String toJson() {
		
		StringBuffer aBuffer = new StringBuffer();
		
		aBuffer.append("{ \"type\": \"Feature\", \"id\" : \""+_recordId+"\", \"geometry\":");
		aBuffer.append(_shape.toJson());
		aBuffer.append(", \"properties\" : {");
		aBuffer.append(" \"bound\" :");
		aBuffer.append(_shape.getBounding().toJson());
		
		if (!_properties.isEmpty()) {
			
			aBuffer.append(", ");
			
			java.lang.Object[] aKeys = _properties.keySet().toArray();
			for (int i=0; i<aKeys.length; i++) {
				aBuffer.append("\"");
				aBuffer.append(((String) aKeys[i]).replace("\"", ""));
				aBuffer.append("\" :");
				aBuffer.append(valid((String)_properties.get((String) aKeys[i])));
				if (i!=aKeys.length-1) {
					aBuffer.append(",");
				}
				
			}
			
		}
		
		aBuffer.append("}");
		aBuffer.append("}");
		
		return aBuffer.toString();
	}

	@Override
	public boolean partlyIn(Bounding iBnd) {
		// TODO Auto-generated method stub
		return _shape.partlyIn(iBnd);
	}

	@Override
	public Bounding getBounding() {
		// TODO Auto-generated method stub
		return _shape.getBounding();
	}

	@Override
	public List<Entity> extract() {
		return _shape.extract();
	}

	@Override
	public void draw(Display iDisp, Color iColor) {
		// TODO Auto-generated method stub
		_shape.draw(iDisp,iColor);
	}

	@Override
	public int[] arcs() {
		// TODO Auto-generated method stub
		return _shape.arcs();
	}

	@Override
	public json.topojson.geom.Object toTopology() {
		json.topojson.geom.Object aObj = _shape.toTopology();
		aObj.setId(_recordId);
		aObj.setProperties(_properties);
		return aObj;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		Feature aFeat =  new Feature(_recordId, (Shape) _shape.clone());
		if (_properties!=null) {
			aFeat._properties = new HashMap<String,java.lang.Object>(_properties);
		}
		
		return (Feature) aFeat;
	}

	@Override
	public void rebuildIndexes(ArcMap iMap) {
		_shape.rebuildIndexes(iMap);
	}

	@Override
	public void fill(Display iDisplay, Colorifier iColor) {
		Color aColor = iColor.getColor(_properties);
		_shape.fill(iDisplay, new BasicColorifier(aColor));
	}
	
}
