package json.topojson.geom;

public abstract class Object {

	Integer id;
	protected String type;
	protected java.lang.Object properties;

	public void setId(int iId){
		id = iId;
	}
	
	public void setProperties(java.lang.Object iProperties){
		properties = iProperties;
	}
	
	public abstract int findMaxArcIndex();
	
}
