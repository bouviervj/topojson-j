package json.geojson.objects;

import java.awt.Color;
import java.awt.Rectangle;
import java.nio.ByteBuffer;

import json.graphic.Display;

public class Bounding extends Object  {

	public double minx, miny, maxx, maxy;
	transient public int hash;
	transient public Rectangle _rect;

	public Bounding(double Xmin, double Ymin, double Xmax, double Ymax){

		minx = Xmin;
		miny = Ymin;
		maxx = Xmax;
		maxy = Ymax;
		
		double swap;
		if (minx>maxx) {
			swap = minx;
			minx = maxx;
			maxx=swap;
		}
		
		if (miny>maxy) {
			swap = miny;
			miny = maxy;
			maxy=swap;
		}

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

	public double getRatioXY(){
		return (maxx-minx)/(maxy-miny);
	}

	public static boolean intersectRect(Bounding b1, Bounding b2, double iScale) {

		/*
		if (b1.minx < b2.maxx && b1.maxx > b2.minx &&
			    b1.miny < b2.maxy && b1.maxy > b2.miny) return true;
		
		return false;
		*/
		
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

	public static class Tile {

		public int x;
		public int y;
		public int zoom;

		public Tile(int x, int y, int zoom){
			this.x = x;
			this.y = y;
			this.zoom = zoom;
		}

	}

	public static Tile getTileNumber(final double lat, final double lon, final int zoom) {
		int xtile = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) ) ;
		int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
		if (xtile < 0)
			xtile=0;
		if (xtile >= (1<<zoom))
			xtile=((1<<zoom)-1);
		if (ytile < 0)
			ytile=0;
		if (ytile >= (1<<zoom))
			ytile=((1<<zoom)-1);
		return new Tile(xtile, ytile, zoom);
	}

	public static double tile2lon(int x, int z) {
		return x / Math.pow(2.0, z) * 360.0 - 180;
	}

	public static double tile2lat(int y, int z) {
		double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
		return Math.toDegrees(Math.atan(Math.sinh(n)));
	}

	public static Bounding tile2boundingBox(final int x, final int y, final int zoom) {
	    return new Bounding( tile2lon(x, zoom), tile2lat(y, zoom), tile2lon(x + 1, zoom), tile2lat(y + 1, zoom)  );
	    /*bb.north = tile2lat(y, zoom);
	    bb.south = tile2lat(y + 1, zoom);
	    bb.west = tile2lon(x, zoom);
	    bb.east = tile2lon(x + 1, zoom);
	    return bb;*/
	 }

	@Override
	public void draw(Display iDisp) {
		iDisp.drawLine(minx, miny, minx, maxy, Color.red);
		iDisp.drawLine(maxx, miny, minx, maxy, Color.red);
		iDisp.drawLine(maxx, maxy, minx, maxy, Color.red);
		iDisp.drawLine(maxx, maxy, minx, miny, Color.red);
	}


}
