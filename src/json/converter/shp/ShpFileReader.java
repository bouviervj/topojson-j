package json.converter.shp;


import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import json.converter.csv.CSVReader;
import json.converter.csv.merger.Merger;
import json.converter.dbf.DBFExtractor;
import json.geojson.Feature;
import json.geojson.FeatureCollection;
import json.geojson.objects.Bounding;
import json.geojson.objects.Polygon;
import json.geojson.objects.Shape;
import json.tools.Toolbox;


public class ShpFileReader {

	static final String[] _typeNames = {
		"Null Shape",
		"Point",
		"",
		"PolyLine",
		"",
		"Polygon",
		"",
		"",
		"MultiPoint"
	};

	String _filename;
	String _basename;
	String _dbfname;
	DataInputStream _stream;
	String[][] _filter; // Use to filter features in a SHP file: list of  key regexp to match according to the DBF file
	CSVReader _assoc_reader;

	/*
	int _shapeType;

	double _Xmin,_Xmax;
	double _Ymin,_Ymax;
	double _Zmin,_Zmax;
	double _Mmin,_Mmax;

	TreeMap<Integer,Record> _shapes;
	 */
	FeatureCollection _groupRecord;

	public ShpFileReader(String iFileName, String iCoordinateSystem){

		_filename = iFileName;

		Toolbox.setCoordinateSystem(iCoordinateSystem);
		
		readAssociation();
		
	}

	public ShpFileReader(String iFileName, String iCoordinateSystem, String[][] iFilter) {

		_filename = iFileName;
		_filter = iFilter;

		Toolbox.setCoordinateSystem(iCoordinateSystem);
		
		readAssociation();
		
	}

	String giveShapeName(int iShapeType){
		return _typeNames[iShapeType];
	}

	public void readAssociation(){

		_basename = _filename.replaceFirst("[.][^.]+$", "");

		_dbfname = _basename + ".dbf";

		System.out.println("Tries to find :"+_dbfname);
		File aDBF = new File(_dbfname);
		if (aDBF.exists()) { // We have a DBF file associated 

			System.out.println("Associate DBF File:"+_dbfname);
			DBFExtractor.extractDBFDataToCSV(_dbfname, _basename+".csv");

			CSVReader aReader = new CSVReader(_basename+".csv");
			aReader.read();

			_assoc_reader = aReader;

		}

	}
	
	public void mergeWithAssociation(Merger iMerger){
		
		if (_assoc_reader!=null) {
				iMerger.process(_assoc_reader);
		}
		
	}	
	
	public void mergeWithAssociation(String iColumn1, CSVReader aReader, String iColumn2){
		
		System.out.println("Trying to merge association ...");
		if (_assoc_reader!=null) {
				System.out.println("Assoc reader present ...");
				_assoc_reader.merge(iColumn1, "%s", aReader, iColumn2, "%s");
			
		}
		
	}

	public void readHeader(){

		try {

			_groupRecord = new FeatureCollection();

			int aFileCode = _stream.readInt();

			System.out.println("File code :"+aFileCode);
			if (aFileCode!=0x0000270a) {
				System.out.println("File not a shp one.");
				return;
			}

			byte[] aUnused = new byte[5*4];
			_stream.read(aUnused);

			int aSize = _stream.readInt();
			System.out.println("File size :"+aSize*2);

			byte[] aIBuffer = new byte[4];
			_stream.read(aIBuffer);
			int aVersion = Toolbox.little2big(aIBuffer);
			System.out.println("Version "+aVersion);

			_stream.read(aIBuffer);
			_groupRecord._shapeType = Toolbox.little2big(aIBuffer);
			System.out.println("Shape Type : "+giveShapeName(_groupRecord._shapeType));


			_groupRecord._bnd = new Bounding(0,0,0,0);
			byte[] aDBuffer = new byte[8];
			// Bounding
			// XMin
			_stream.read(aDBuffer);
			_groupRecord._bnd.minx = Toolbox.getDoubleFromByte(aDBuffer);


			// YMin
			_stream.read(aDBuffer);
			_groupRecord._bnd.miny = Toolbox.getDoubleFromByte(aDBuffer);


			Point2D.Double aRes = Toolbox.convertLatLong(_groupRecord._bnd.minx, _groupRecord._bnd.miny);
			_groupRecord._bnd.minx = aRes.x;
			_groupRecord._bnd.miny = aRes.y;

			// XMax
			_stream.read(aDBuffer);
			_groupRecord._bnd.maxx = Toolbox.getDoubleFromByte(aDBuffer);


			// YMax
			_stream.read(aDBuffer);
			_groupRecord._bnd.maxy = Toolbox.getDoubleFromByte(aDBuffer);


			aRes = Toolbox.convertLatLong(_groupRecord._bnd.maxx, _groupRecord._bnd.maxy);
			_groupRecord._bnd.maxx = aRes.x;
			_groupRecord._bnd.maxy = aRes.y;

			/*
			double swap;
			if (_groupRecord._bnd._Xmin>_groupRecord._bnd._Xmax) {
				swap = _groupRecord._bnd._Xmin;
				_groupRecord._bnd._Xmin = _groupRecord._bnd._Xmax;
				_groupRecord._bnd._Xmax = swap;
			}*/

			System.out.println("xmin : "+_groupRecord._bnd.minx);
			System.out.println("ymin : "+_groupRecord._bnd.miny);
			System.out.println("xmax : "+_groupRecord._bnd.maxx);
			System.out.println("ymax : "+_groupRecord._bnd.maxy);

			// ZMin
			_stream.read(aDBuffer);
			//_groupRecord._Zmin = Toolbox.getDoubleFromByte(aDBuffer);
			//System.out.println("zmin : "+_groupRecord._Zmin);

			// ZMax
			_stream.read(aDBuffer);
			//_groupRecord._Zmax = Toolbox.getDoubleFromByte(aDBuffer);
			//System.out.println("zmax : "+_groupRecord._Zmax);

			// MMin
			_stream.read(aDBuffer);
			//_groupRecord._Mmin = Toolbox.getDoubleFromByte(aDBuffer);
			//System.out.println("mmin : "+_groupRecord._Mmin);

			// MMax
			_stream.read(aDBuffer);
			//_groupRecord._Mmax = Toolbox.getDoubleFromByte(aDBuffer);
			//System.out.println("mmax : "+_groupRecord._Mmax);


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void readRecords(){

		// here reading records

		try {
			while (_stream.available()!=0) {
				readRecord();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean  applyFilter(int iRecordNumber, CSVReader iReader, int iRow){

		if (_filter!=null) {
			
			for (String[] aFilter:_filter) {
				
				int aIndex= iReader._header.indexOf(aFilter[0]);
				String aData = iReader._data.get(iRow)[aIndex];
				if (aData==null) return false;
				if (!Pattern.matches(aFilter[1], aData)) {
					return false;
				}

			}

		}

		return true;
	}

	public void readRecord(){

		try {

			// Here reading record header
			int aRecordNumber = _stream.readInt();
			
			String[] prop = (_assoc_reader!=null?_assoc_reader.get(aRecordNumber):null);
			boolean filter = (prop!=null?applyFilter(aRecordNumber,_assoc_reader, aRecordNumber):false);
			
			int aRecordSize = _stream.readInt(); // a better implementation will skip those bytes if filter = false
			
			if (filter) {
				
				//System.out.println("Record# : "+aRecordNumber);
				//System.out.println("Prop : "+prop);

				int a1 = _stream.available();
				
				
				byte[] aIBuffer = new byte[4];
				_stream.read(aIBuffer);
				int aShapeType = Toolbox.little2big(aIBuffer);
				//System.out.println("Record Shape Type : "+giveShapeName(aShapeType));
	
				Shape aReadShape = null;
				switch (aShapeType) {
					case 5 : { //Polygons
						aReadShape = Polygon.readPolygon(_stream);
					}
				}
				//System.out.println("Pos 2: "+_stream.available());
				//System.out.println("Readen: "+(a1-_stream.available()));
				
				
				//System.out.println("Record # : "+aRecordNumber);
				
				Feature aFeature = new Feature(aRecordNumber, aReadShape);
				
				if (prop!=null) {
					for (int i=0; i<prop.length; i++) {
						aFeature.addProperty(_assoc_reader._header.get(i), prop[i]);
					}
				}
				
				_groupRecord._shapes.put(new Integer(aRecordNumber), aFeature);
				
			} else {
				
				_stream.skip(aRecordSize*2);
				
			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void read() throws IOException{

		try {

			_stream = new DataInputStream(new FileInputStream(new File(_filename)));

			//readAssociation(); readen in constructor to be able to merge data with association

			readHeader();

			readRecords();

			_stream.close();

		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}


	}

	public String toJson(){
		return _groupRecord.toJson();
	}

	public FeatureCollection getGroupRecord(){
		return _groupRecord;
	}

}
