package json.topojson.geom;

import java.util.Vector;

public class GeometryCollection extends Object{

	Vector<Object> geometries;
	
	public GeometryCollection(){
		type = "GeometryCollection"; 
		geometries = new Vector<Object	>();
	}

	public void addGeometry(Object iObject){
		geometries.add(iObject);
	}

	@Override
	public int findMaxArcIndex() {
		int max = -1;
		for (Object object:geometries) {
			
			int index = object.findMaxArcIndex();
			if (index>max) {
				max = index;
			}
		
		}
		return max;
	}
	
}
