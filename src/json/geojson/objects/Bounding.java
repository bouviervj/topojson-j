package json.geojson.objects;

import java.awt.Rectangle;
import java.nio.ByteBuffer;

public class Bounding {

	public double minx, miny, maxx, maxy;
	transient public int hash;
	transient public Rectangle _rect;
	
	public Bounding(double Xmin, double Ymin, double Xmax, double Ymax){
	
		minx = Xmin;
		miny = Ymin;
		maxx = Xmax;
		maxy = Ymax;
		
		hash = ByteBuffer.allocate(4*8).putDouble(minx).putDouble(miny)
									   .putDouble(maxx).putDouble(maxx).hashCode();

	}

	@Override
	public int hashCode(){
		return hash;
	}
	
	public boolean in(double X, double Y){
		return (X>minx) && (Y>miny) && (X<maxx) && (Y<maxy);
	}
	
	public static boolean intersectRect(Bounding b1, Bounding b2, double iScale) {
		
		double vx = (b1.maxx-b1.minx)*(iScale-1.0);
		double vy = (b1.maxy-b1.miny)*(iScale-1.0);
		
		double Xmax = b1.maxx+vx;
		double Xmin = b1.minx-vx;
		double Ymax = b1.maxy+vy;
		double Ymin = b1.miny-vy;
		
		return !((b2.minx > (Xmax)) || 
		           (b2.maxx < (Xmin)) || 
		           (b2.miny > (Ymax)) ||
		           (b2.maxy < (Ymin)));
	}
	
	public void scale(double iScale){
		
		double vx = (maxx-minx)*(iScale-1.0);
		double vy = (maxy-miny)*(iScale-1.0);
		
		maxx = maxx+vx;
		minx = minx-vx;
		maxy = maxy+vy;
		miny = miny-vy;
		
	}
	
	public boolean partlyIn(Bounding iBnd){
		return /*iBnd.in(_Xmin, _Ymin) || iBnd.in(_Xmax, _Ymax)
			|| iBnd.in(_Xmax, _Ymin) || iBnd.in(_Xmin, _Ymax)*/ intersectRect(this, iBnd, 1.0);	   
	}
	
	public boolean partlyIn(Bounding iBnd, double iScale){
		return /*iBnd.in(_Xmin, _Ymin) || iBnd.in(_Xmax, _Ymax)
			|| iBnd.in(_Xmax, _Ymin) || iBnd.in(_Xmin, _Ymax)*/ intersectRect(this, iBnd, iScale);	   
	}
	
	public String toJson(){
		return "{ \"minx\" : "+minx+" , \"miny\" : "+miny+" , \"maxx\" : "+maxx+ " , \"maxy\" : "+maxy+" }";
	}
	
	public void merge(Bounding bnd){
		if (bnd.maxx>maxx) maxx = bnd.maxx;
		if (bnd.maxy>maxy) maxy = bnd.maxy;
		if (bnd.minx<minx) minx = bnd.minx;
		if (bnd.miny<miny) miny = bnd.miny;
	}
	
	@Override
	public Bounding clone(){
		return new Bounding(minx,miny,maxx,maxy);
	}
	
}
