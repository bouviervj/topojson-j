package json.geojson;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import json.geojson.objects.Shape;
import json.graphic.Display;
import json.tools.EntryImp;
import json.topojson.algorithm.ArcMap;
import json.topojson.geom.GeometryCollection;
import json.topojson.geom.Object;
import json.topojson.geom.sub.Entity;



public class FeatureCollection extends Shape {

	public int _shapeType;

	public Bounding _bnd;

	/*
	public double _Xmin,_Xmax;
	public double _Ymin,_Ymax;
	 */
	/*
	public double _Zmin,_Zmax;
	public double _Mmin,_Mmax;
	 */

	public TreeMap<Integer,Feature> _shapes;

	HashMap<String, EntryImp<Double,Double>> _minmax_properties;
	HashMap<String, List<Double>> _minmax_properties_series;
	HashMap<String, HashMap<String,String>> _meta_properties;

	public FeatureCollection(){
		_shapes = new TreeMap<Integer,Feature>();
		_meta_properties = new HashMap<String, HashMap<String,String>>();
	}

	public void mergeMinMaxProperties(String[] iProperties){

		if (iProperties.length>0){
			EntryImp<Double,Double> aEntry = _minmax_properties.get(iProperties[0]);

			if (aEntry!=null) {

				for (int i=1; i<iProperties.length; i++) {

					EntryImp<Double,Double> aEntC = _minmax_properties.get(iProperties[i]);
					if (aEntC.getKey()<aEntry.getKey()) aEntry.setKey(aEntC.getKey());
					if (aEntC.getValue()>aEntry.getValue()) aEntry.setValue(aEntC.getValue());

				}

				for (int i=0; i<iProperties.length; i++) {
					_minmax_properties.put(iProperties[i], aEntry);
				}

			} else {

				System.err.println("Unable to find property:"+iProperties[0]);

			}

			List<Double> aSerie = _minmax_properties_series.get(iProperties[0]);
			for (int i=1; i<iProperties.length; i++) {

				List<Double> aList = _minmax_properties_series.get(iProperties[i]);
				aSerie.addAll(aList);

			}
			Collections.sort(aSerie);
			for (int i=0; i<iProperties.length; i++) {
				_minmax_properties_series.put(iProperties[i], aSerie);
			}

		}		

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

	public String toJsonMinMaxProperties(String[] aDrop){

		StringBuffer aBuf = new StringBuffer();

		HashSet<String> aToRemove = new HashSet<String>();
		for (String aD:aDrop){
			aToRemove.add(aD);
		}

		aBuf.append("{");

		String[] aKeys = new String[_minmax_properties.size()];
		aKeys = _minmax_properties.keySet().toArray(aKeys);

		boolean first = true;
		for (int i=0; i<aKeys.length; i++) {

			String aKey = aKeys[i].replace("\"", "");
			
			if (!aToRemove.contains(aKey)) {

				if (!first) {
					aBuf.append(",");
				}

				aBuf.append("\"");
				aBuf.append(aKey);
				aBuf.append("\" : {");

				aBuf.append("\"min\" : ");
				aBuf.append(_minmax_properties.get(aKeys[i]).getKey());
				aBuf.append(" , \"max\" : ");
				aBuf.append(_minmax_properties.get(aKeys[i]).getValue());

				HashMap<String,String> aMap = _meta_properties.get(aKeys[i]);
				if (aMap.size()>0) {

					for (Entry<String,String> aEnt:aMap.entrySet()) {
						aBuf.append(",");
						aBuf.append("\""+aEnt.getKey()+"\" : \"");
						aBuf.append(aEnt.getValue());
						aBuf.append("\"");
					}

				}
				aBuf.append(", \"serie\" : [ ");
				
				// comput Jenks
				
				List<Double> aList = _minmax_properties_series.get(aKeys[i]);
				double[] aSerie = new double[aList.size()];
				for (int j=0; j<aList.size(); j++) {
					aSerie[j] = aList.get(j);
					//aBuf.append(String.format("%.2f", aList.get(j)));
					//if (j!=aList.size()-1) aBuf.append(",");
				}
				
				double[] aResult = Jenks.computeJenks(5, aSerie);
				
				//List<Double> aList = _minmax_properties_series.get(aKeys[i]);
				for (int j=0; j<aResult.length; j++) {
					aBuf.append(String.format("%.2f", aResult[j]));
					if (j!=aResult.length-1) aBuf.append(",");
				}
				aBuf.append("] ");
				aBuf.append("}");

				first = false;
			}

		}

		aBuf.append("}");

		return aBuf.toString();

	}

	public FeatureCollection[][] groupGridDivide(int iN, int iM){

		FeatureCollection[][] aDividedResult = new FeatureCollection[iN][iM];
		//HashSet<Integer> aAlreadySelected = new HashSet<Integer>();

		double xStep = (_bnd.maxx-_bnd.minx)/iN;
		double yStep = (_bnd.maxy-_bnd.miny)/iM;

		for (int i=0; i<iN; i++) {

			double aXmin = _bnd.minx + i*xStep;
			double aXmax = _bnd.minx + (i+1)*xStep;

			for (int j=0; j<iM; j++) {

				FeatureCollection aGroupRecord = new FeatureCollection();

				double aYmin = _bnd.miny + j*yStep;
				double aYmax = _bnd.miny + (j+1)*yStep;

				Bounding aBnd = new Bounding(aXmin,aYmin,aXmax,aYmax);

				aGroupRecord._bnd = aBnd;

				for (Entry<Integer,Feature> aERec:_shapes.entrySet()) {
					if (aERec.getValue().partlyIn(aBnd) /*&& (!aAlreadySelected.contains(aERec.getKey()))*/) {
						aGroupRecord._shapes.put(aERec.getKey(), (Feature) aERec.getValue());
						//aAlreadySelected.add(aERec.getKey());
					}
				}

				aDividedResult[i][j] = aGroupRecord;

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

	public void merge(CSVReader aReader, String iCol, String[] iAccepted, String[] iUnits, String[] iTitles){

		_minmax_properties = new HashMap<String, EntryImp<Double,Double>>();
		_minmax_properties_series = new HashMap<String, List<Double>>();

		HashSet<String> aSet = new HashSet<String>();
		for (int i=0; i<iAccepted.length; i++){
			aSet.add(iAccepted[i]);

			HashMap<String, String > aMap = new HashMap<String,String>();
			aMap.put("unit", iUnits[i]);
			aMap.put("title", iTitles[i]);

			_meta_properties.put(iAccepted[i], aMap);
		}

		for (Entry<Integer,TreeMap<String,String>> aEntry : aReader._data.entrySet()){

			String aValue = aEntry.getValue().get(iCol);
			Integer aIntValue = new Integer(aValue);

			Feature aRecord = _shapes.get(aIntValue);
			if (aRecord!=null) {


				for (Entry<String,String> aKV : aEntry.getValue().entrySet()){


					if ((!aKV.getKey().equals(iCol)) && (aSet.contains(aKV.getKey()))
							&& ( !aKV.getValue().equals(""))) {

						// Here we have selected values
						// We want to build max min values for these properties
						try {
							double aDValue = Double.valueOf(aKV.getValue());
							EntryImp<Double,Double> aMM = _minmax_properties.get(aKV.getKey());
							if (aMM!=null) {

								if (aMM.getKey()>aDValue) {
									aMM.setKey(aDValue);
								}

								if (aMM.getValue()<aDValue) {
									aMM.setValue(aDValue);
								}

							} else {

								_minmax_properties.put(aKV.getKey(), new EntryImp<Double,Double>(aDValue,aDValue));

							}

							List<Double> aList = _minmax_properties_series.get(aKV.getKey());
							if (aList==null) {
								aList = new ArrayList<Double>();
								_minmax_properties_series.put(aKV.getKey(), aList);
							}
							aList.add(aDValue);

						} catch (java.lang.NumberFormatException e){
							// unable to convert this data
						}

						String iKey = aKV.getKey().replace("\"", ""); // prevent inserting " in keys
						aRecord.addProperty(iKey, aKV.getValue());

					}

				}

			}

		}

		for (String aKey:_minmax_properties_series.keySet()) {

			List<Double> aList = _minmax_properties_series.get(aKey);
			Collections.sort(aList); // sort all lists

		}

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
		Bounding aSt = _shapes.firstEntry().getValue().getBounding();
		for (Shape aShape:_shapes.values()) {
			aSt.merge(aShape.getBounding());
		}
		return aSt;
	}


	@Override
	public void draw(Display iDisp) {
		for (Shape aShape:_shapes.values()) {
			aShape.draw(iDisp);
		}
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
		aCollection._meta_properties = new HashMap<String,HashMap<String,String>>(aCollection._meta_properties);

		if (_minmax_properties!=null) {
			aCollection._minmax_properties = new HashMap<String, EntryImp<Double,Double>>(_minmax_properties);
		}

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

}
