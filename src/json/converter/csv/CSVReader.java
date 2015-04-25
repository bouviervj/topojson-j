package json.converter.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

import json.tools.EntryImp;

public class CSVReader  implements SortedMap<Integer, LinkedHashMap<String,String>> {

	//String _filename;
	protected InputStream  _instream;
	protected BufferedReader _reader;
	String[] _header = null;

	public TreeMap<Integer, LinkedHashMap<String,String>> _data;

	public CSVReader(String iFileName){

		try {
			_instream = new FileInputStream(new File(iFileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//_filename = iFileName;
		_data = new TreeMap<Integer,LinkedHashMap<String,String>>();
	}

	public CSVReader(InputStream  iStream){

		_instream = iStream;
		//_filename = iFileName;
		_data = new TreeMap<Integer,LinkedHashMap<String,String>>();
	}

	public void readHeader(){

		try {

			String aHeader = _reader.readLine();

			_header = CSVReader.readCSVLine(aHeader);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void readData(){

		try {
			int count = 1;
			String line;
			while ((line = _reader.readLine()) != null) {

				String[] data;
				try {
					data = CSVReader.readCSVLine(line);

					LinkedHashMap<String,String> aTupple = new LinkedHashMap<String,String>();

					aTupple.put("rowid", String.format("%d", count) );

					if (_header==null) {

						for (int i=0; i<data.length;i++) {
							aTupple.put("c"+i, data[i].trim());
						}

					} else {

						for (int i=0; i<_header.length;i++) {
							if (i<data.length) {
								aTupple.put(_header[i].trim(), data[i].trim());
							}
						}

					}

					_data.put(count,aTupple);


				} catch (Exception e) { // Doing best effort
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				count++;

			}

			_reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void read(boolean readHeader){

		try {

			_reader = new BufferedReader(new InputStreamReader(_instream));

			if (readHeader) readHeader();

			readData();

			_reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void read(){
		read(true);
	}

	public void readWithoutHeader(){
		read(false);
	}

	public String toString(){
		StringBuffer aBuf = new StringBuffer();

		for (LinkedHashMap<String,String> aTuple:_data.values()) {
			aBuf.append(aTuple);
			aBuf.append("\n");
		}
		return aBuf.toString();
	}

	public void merge(String iCol1, String iFormat1, CSVReader iReader, String iCol2, String iFormat2) {
		merge(iCol1, iFormat1, iReader, iCol2, iFormat2, false);
	}
	
	public void merge(String iCol1, String iFormat1, CSVReader iReader, String iCol2, String iFormat2, boolean iJoin) {

		HashMap<String,Vector<EntryImp<String,Integer>>> aIndex2 = new HashMap<String,Vector<EntryImp<String,Integer>>>();
		for (Entry<Integer,LinkedHashMap<String,String>> entrySet: iReader._data.entrySet()){

			String aValue  = entrySet.getValue().get(iCol2);

			if (aValue.length()>1) { // else empty row, will be eliminated
				String aHashedValue = String.format(iFormat2,aValue);

				//System.out.println("Registering:"+ aHashedValue);

				if (!aIndex2.containsKey(aHashedValue)) {
					aIndex2.put(aHashedValue, new Vector<EntryImp<String,Integer>>());
				} 
				aIndex2.get(aHashedValue).add(new EntryImp<String,Integer>(aHashedValue,entrySet.getKey()));

				if (aIndex2.get(aHashedValue).size()>1) {
					System.err.println(aHashedValue+" ("+iCol2+") hash overlap:"+aIndex2.get(aHashedValue)+ " Value:"+ entrySet.getValue() );
				}
			}

		}
		
		TreeMap<Integer, LinkedHashMap<String,String>> newData = new TreeMap<Integer, LinkedHashMap<String,String>>();

		for (Entry<Integer,LinkedHashMap<String,String>> entrySet: _data.entrySet()){

			String aValueToFind = String.format(iFormat1, entrySet.getValue().get(iCol1));

			Vector<EntryImp<String,Integer>> aIndex = aIndex2.get(aValueToFind);
			if (aIndex!=null) {

				for (int i=0; i<aIndex.size(); i++){

					if (aValueToFind.equals(aIndex.get(i).getKey())) {
						LinkedHashMap<String,String> toMerge = iReader._data.get(aIndex.get(i).getValue());
						
						for (Entry<String,String> aTuple:toMerge.entrySet()) {

							if (!aTuple.getKey().equals("rowid")) {
								entrySet.getValue().put(aTuple.getKey(), aTuple.getValue());
							}
						
						}
						
					}
					
					newData.put(entrySet.getKey(), entrySet.getValue());
				} 

			}  else { // If no match we complete the line anyway // TODO factorize algorithm
				
				if (!iJoin) {
					LinkedHashMap<String,String> aToAdd = iReader._data.firstEntry().getValue();
					for (String aKey:aToAdd.keySet()) {
						if (!aKey.equals("rowid")) {
							entrySet.getValue().put(aKey,"");
						}
					}
					newData.put(entrySet.getKey(), entrySet.getValue());
				}
				
			}
			
		}
		
		_data = newData;

	}

	public static String[] readCSVLine(String iLine) throws Exception{

		Vector<String> aGroup = new Vector<String>();

		boolean escape_mode = false;
		int escape_count = 0;
		String accum = "";
		for (int i=0; i<iLine.length(); i++) {

			char c = iLine.charAt(i);
			switch (c) {
			case '"':{
				if (escape_mode){
					escape_mode = false;
					escape_count++;
				} else {
					escape_mode = true;
					escape_count++;
				}
				break;
			}
			case ',': {
				if (escape_mode) {
					accum+=c;
				} else {
					if (escape_count%2>0) throw new Exception("CSV Parse exception: invalid escape sequence char["+i+"]:"+iLine);
					aGroup.add(accum);
					escape_count = 0;
					accum="";
				}
				break;
			}
			default: accum+=c; break;
			}

		}

		aGroup.add(accum);

		return aGroup.toArray(new String[aGroup.size()]);

	}

	LinkedHashMap<String,String> getRow(int aRow){
		return _data.get(aRow);
	}

	@Override
	public void clear() {
		_data.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return _data.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return  _data.containsValue(value);
	}

	@Override
	public LinkedHashMap<String, String> get(Object key) {
		// TODO Auto-generated method stub
		return  _data.get(key);
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return _data.isEmpty();
	}

	@Override
	public LinkedHashMap<String, String> put(Integer key,
			LinkedHashMap<String, String> value) {
		// TODO Auto-generated method stub
		return _data.put(key, value);
	}

	@Override
	public void putAll(
			Map<? extends Integer, ? extends LinkedHashMap<String, String>> m) {
		// TODO Auto-generated method stub
		_data.putAll(m);
	}

	@Override
	public LinkedHashMap<String, String> remove(Object key) {
		// TODO Auto-generated method stub
		return _data.remove(key);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return _data.size();
	}

	@Override
	public Comparator<? super Integer> comparator() {
		// TODO Auto-generated method stub
		return _data.comparator();
	}

	@Override
	public Set<java.util.Map.Entry<Integer, LinkedHashMap<String, String>>> entrySet() {
		// TODO Auto-generated method stub
		return _data.entrySet();
	}

	@Override
	public Integer firstKey() {
		// TODO Auto-generated method stub
		return _data.firstKey();
	}

	@Override
	public SortedMap<Integer, LinkedHashMap<String, String>> headMap(Integer toKey) {
		// TODO Auto-generated method stub
		return _data.headMap(toKey);
	}

	@Override
	public Set<Integer> keySet() {
		// TODO Auto-generated method stub
		return _data.keySet();
	}

	@Override
	public Integer lastKey() {
		// TODO Auto-generated method stub
		return _data.lastKey();
	}

	@Override
	public SortedMap<Integer, LinkedHashMap<String, String>> subMap(
			Integer fromKey, Integer toKey) {
		// TODO Auto-generated method stub
		return _data.subMap(fromKey, toKey);
	}

	@Override
	public SortedMap<Integer, LinkedHashMap<String, String>> tailMap(
			Integer fromKey) {
		// TODO Auto-generated method stub
		return _data.tailMap(fromKey);
	}

	@Override
	public Collection<LinkedHashMap<String, String>> values() {
		// TODO Auto-generated method stub
		return _data.values();
	}

	public String buildCSV(TreeMap<Integer, LinkedHashMap<String,String>> iData){

		StringBuffer aBuffer = new StringBuffer();
		// Builds the header
		LinkedHashMap<String,String> aHeaderLine = iData.firstEntry().getValue();
		for(Iterator<String> i = aHeaderLine.keySet().iterator(); i.hasNext(); ) {
			aBuffer.append(i.next());
			if (i.hasNext()) aBuffer.append(",");
		}
		aBuffer.append("\n");

		for (LinkedHashMap<String,String> aLine:iData.values()){

			for (Iterator<String> i = aLine.keySet().iterator(); i.hasNext(); ){
				
				String aValue = aLine.get(i.next());
				if (aValue.contains(",")){
					aBuffer.append("\"");
					aBuffer.append(aValue);
					aBuffer.append("\"");
				} else {
					aBuffer.append(aValue);
				}
				if (i.hasNext()) aBuffer.append(",");
			}
			aBuffer.append("\n");

		}

		return aBuffer.toString();
	}

	public void write(String iFileName){

		String aCSVData = buildCSV(_data);

		FileOutputStream out;
		try {
			out = new FileOutputStream(iFileName);
			out.write(aCSVData.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}

