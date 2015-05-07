package json.converter.csv.builder;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;

import json.converter.csv.CSVReader;

public class ColumnBuilder {

		String _name;
		Vector<Token> _tokens; 
	
		public static class Token {
			
			public enum TYPE {
				INTEGER,
				STRING
			};
			
			String _format;
			TYPE _type;
			int _key;
			
			public Token(String iFormat, TYPE iType, int iIndexKey ){
				_format = iFormat;
				_type = iType;
				_key = iIndexKey;
			}
			
			public String format(String[] iRow){
				
				String aVal =  iRow[_key];
				switch (_type) {
					case INTEGER: return String.format(_format,new Integer(aVal.equals("")?"0":aVal)); 
					case STRING: return String.format(_format,aVal); 
				}
				return "";
				
			}
			
		}
		
		public ColumnBuilder(String iName){
			_name = iName;
			_tokens = new Vector<Token>();
		}
	
		public void addToken(Token iToken){
			_tokens.add(iToken);
		}
		
		public String format(String[] iRow){
			StringBuffer aBuffer = new StringBuffer();
			for (Token tok:_tokens){
				aBuffer.append(tok.format(iRow));
			}
			return aBuffer.toString();
		}
		
		public void build(CSVReader iReader){
			iReader._header.add(_name);
			TreeMap<Integer,String[]> aNew = new TreeMap<Integer,String[]>();
			for (Entry<Integer,String[]> aRowEnt:iReader._data.entrySet()){
				String[] newLine = ArrayUtils.add(aRowEnt.getValue(), format(aRowEnt.getValue()));
				aNew.put(aRowEnt.getKey(),newLine);
			}
			iReader._data = aNew;
		}
		

}
