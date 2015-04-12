package graphics;

import java.awt.Color;
import java.util.HashMap;

import json.graphic.Colorifier;
import json.topojson.topology.Topology;

import json.topojson.geom.GeometryCollection;
import json.topojson.geom.Object;

public class CensusColorifier extends  Colorifier {

	public CensusColorifier(Topology iTopo) {
		
		super(iTopo);
		init();

	}

	public void init(){
		
		for (Object aObject:_topo.objects.values()){
			System.out.println("Type:"+aObject.getClass());
		
			if (aObject instanceof GeometryCollection){
			
				GeometryCollection aCollect = (GeometryCollection) aObject;
				
				// At first level we should have the list of polygons with their properties
				for (Object aIObject:aCollect.geometries){
					
						java.lang.Object aProps  = aIObject.getProperties();
						if (aProps instanceof HashMap<?,?>) {
							
							HashMap<String,String> aData = (HashMap<String,String>) aProps;
							for (String aKey:aData.keySet()){
								System.out.print(aKey+":"+aData.get(aKey).toString()+" ");
							}
							System.out.println();
							
						}
					
				}
				
			}
			
		}
		
	}

	@Override
	public Color getColor(HashMap<String, java.lang.Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

}
