package json.geojson;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;

import json.algorithm.Jenks;
import json.converter.csv.CSVReader;
import json.geojson.objects.Bounding;
import json.geojson.objects.Bounding.Tile;
import json.geojson.objects.Shape;
import json.graphic.Colorifier;
import json.graphic.Display;
import json.tools.EntryImp;
import json.topojson.algorithm.ArcMap;
import json.topojson.geom.GeometryCollection;
import json.topojson.geom.Object;
import json.topojson.geom.sub.Entity;



public class FeatureCollection extends Shape {

	public int _shapeType;

	public Bounding _bnd;

	public TreeMap<Integer,Feature> _shapes;

   public transient HashMap<String, java.lang.Object> _meta_properties;

	public FeatureCollection(){
		_shapes = new TreeMap<Integer,Feature>();
		_meta_properties = new HashMap<String, java.lang.Object>();
	}


	public String toJson(){

		StringBuffer aBuffer = new StringBuffer();

		aBuffer.append("{ \"type\": \"FeatureCollection\", \"features\": [");

		Feature[] aValues = new Feature[_shapes.values().size()];
		aValues = _shapes.values().toArray(aValues);
		int max = aValues.length;
		for (int i=0; i<max/*aValues.length*/; i++) {
			aBuffer.append(aValues[i].toJson());
			if (i!=max-1) aBuffer.append(",");
		}

		aBuffer.append("]}");

		return aBuffer.toString();

	}

	
	// Divide a feature collection in standard tiles based onn openstreetmap definition
	
	public static class TileElement {
		
			public int x;
			public int y;
			public int zoom;
			
			TileElement(int x, int y, int zoom){
				this.x = x;
				this.y = y;
				this.zoom = zoom;
			}
		
	}
	
	public Vector<TileElement>  dimTiles(int iZoom){
		
		Vector<TileElement> aResult  = new Vector<TileElement>();
		
		Tile aMinTile = Bounding.getTileNumber(_bnd.miny, /* lon*/_bnd.minx,  iZoom);
		Tile aMaxTile = Bounding.getTileNumber( _bnd.maxy, /*lon*/_bnd.maxx, iZoom);
		
		System.out.println("Max:"+_bnd.maxx+","+_bnd.maxy);
		System.out.println("Min:"+_bnd.minx+","+_bnd.miny);
		
		int aN = aMaxTile.x-aMinTile.x;
		int aM = aMaxTile.y-aMinTile.y;
		
		System.out.println("N:"+aN);
		System.out.println("M:"+aM);
		
		int aSN = aN<0?-1:+1;
		int aSM = aM<0?-1:+1;
		
		aN = Math.abs(aN)+1;
		aM = Math.abs(aM)+1;
		
		FeatureCollection[][] aDividedResult = new FeatureCollection[aN][aM];

		for (int i=0; i<aN; i++) {

			for (int j=0; j<aM; j++) {

				int x = aMinTile.x+aSN*i;
				int y = aMinTile.y+aSM*j;
				
				aResult.add(new TileElement(x,y,iZoom));
				
			}	
		
		}	
			
		return aResult;
		
	}
	
	public FeatureCollection processTileElement(TileElement iTileE){
	
		FeatureCollection aGroupRecord = new FeatureCollection();

		Bounding aBnd = Bounding.tile2boundingBox(iTileE.x , iTileE.y , iTileE.zoom);
		aGroupRecord._bnd = aBnd;
		
		//System.out.println(aBnd.toJson());

		for (Entry<Integer,Feature> aERec:_shapes.entrySet()) {
			if (aERec.getValue().partlyIn(aBnd) /*&& (!aAlreadySelected.contains(aERec.getKey()))*/) {
				aGroupRecord._shapes.put(aERec.getKey(), (Feature) aERec.getValue());
			}
		}

		aGroupRecord._meta_properties.put("x", new Integer(iTileE.x));
		aGroupRecord._meta_properties.put("y", new Integer(iTileE.y));	
	
		return aGroupRecord;
		
	}
	
	public FeatureCollection[][] groupGridDivide(int iZoom/*int iN, int iM*/){

		Tile aMinTile = Bounding.getTileNumber(_bnd.miny, /* lon*/_bnd.minx,  iZoom);
		Tile aMaxTile = Bounding.getTileNumber( _bnd.maxy, /*lon*/_bnd.maxx, iZoom);
		
		System.out.println("Max:"+_bnd.maxx+","+_bnd.maxy);
		System.out.println("Min:"+_bnd.minx+","+_bnd.miny);
		
		int aN = aMaxTile.x-aMinTile.x;
		int aM = aMaxTile.y-aMinTile.y;
		
		System.out.println("N:"+aN);
		System.out.println("M:"+aM);
		
		int aSN = aN<0?-1:+1;
		int aSM = aM<0?-1:+1;
		
		aN = Math.abs(aN)+1;
		aM = Math.abs(aM)+1;
		
		FeatureCollection[][] aDividedResult = new FeatureCollection[aN][aM];

		for (int i=0; i<aN; i++) {

			for (int j=0; j<aM; j++) {

				int x = aMinTile.x+aSN*i;
				int y = aMinTile.y+aSM*j;
				
				FeatureCollection aGroupRecord = new FeatureCollection();

				Bounding aBnd = Bounding.tile2boundingBox(x , y , iZoom);
				aGroupRecord._bnd = aBnd;
				
				//System.out.println(aBnd.toJson());

				for (Entry<Integer,Feature> aERec:_shapes.entrySet()) {
					if (aERec.getValue().partlyIn(aBnd) /*&& (!aAlreadySelected.contains(aERec.getKey()))*/) {
						aGroupRecord._shapes.put(aERec.getKey(), (Feature) aERec.getValue());
					}
				}

				aDividedResult[i][j] = aGroupRecord;
				aGroupRecord._meta_properties.put("x", new Integer(x));
				aGroupRecord._meta_properties.put("y", new Integer(y));

			}

		}

		return aDividedResult;

	}

	@Override
	public boolean partlyIn(Bounding iBnd) {
		for (Shape aShape:_shapes.values()){
			if (aShape.partlyIn(iBnd)) return true;
		}
		return false;
	}

	@Override
	public Bounding getBounding() {
		// TODO Auto-generated method stub
		return _bnd;
	}
	
	@Override
	public List<Entity> extract() {
		Vector<Entity> aEntities = new Vector<Entity>();
		for (Shape aShape:_shapes.values()) {
			aEntities.addAll(aShape.extract());
		}
		return aEntities;
	}

	public Bounding getMergedBound(){
		
		if (_shapes.size()>0) {
			Bounding aSt = _shapes.firstEntry().getValue().getBounding();
			for (Shape aShape:_shapes.values()) {
				aSt.merge(aShape.getBounding());
			}
			return aSt;
		}
		
		return null;
	}

	@Override
	public int[] arcs() {
		int[] aAll= {};
		for (Shape aShape:_shapes.values()) {
			int[] aArcs = aShape.arcs();
			aAll = ArrayUtils.addAll(aAll, aArcs);
		}
		return aAll;
	}

	@Override
	public Object toTopology() {
		GeometryCollection aGeoCol = new GeometryCollection();
		for (Shape aShape:_shapes.values()) {
			aGeoCol.addGeometry(aShape.toTopology());
		}
		return aGeoCol;
	}

	@Override
	public json.geojson.objects.Object clone() {

		FeatureCollection aCollection = new FeatureCollection();
		aCollection._shapeType = _shapeType;
		aCollection._bnd = _bnd.clone();
		
		aCollection._meta_properties = _meta_properties;

		for (Entry<Integer,Feature> aEnt:_shapes.entrySet()) {

			aCollection._shapes.put(aEnt.getKey(), (Feature) aEnt.getValue().clone());
		}

		return aCollection;
	}

	@Override
	public void rebuildIndexes(ArcMap iMap) {
		for (Feature aEnt:_shapes.values()) {
			aEnt.rebuildIndexes(iMap);
		}
	}

	@Override
	public void draw(Display iDisplay, Color iColor) {
		for (Feature aEnt:_shapes.values()) {
			aEnt.draw(iDisplay, iColor);
		}
	}

	@Override
	public void fill(Display iDisplay, Colorifier iColor) {
		for (Feature aEnt:_shapes.values()) {
			aEnt.fill(iDisplay, iColor);
		}
	}

}
