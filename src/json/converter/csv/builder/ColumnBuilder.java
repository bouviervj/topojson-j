package json.converter.csv.builder;

import java.util.LinkedHashMap;
import java.util.Vector;

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
			String _key;
			
			public Token(String iFormat, TYPE iType, String iKey ){
				_format = iFormat;
				_type = iType;
				_key = iKey;
			}
			
			public String format(LinkedHashMap<String,String> iRow){
				
				String aVal =  iRow.get(_key);
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
		
		public String format(LinkedHashMap<String,String> iRow){
			StringBuffer aBuffer = new StringBuffer();
			for (Token tok:_tokens){
				aBuffer.append(tok.format(iRow));
			}
			return aBuffer.toString();
		}
		
		public void build(CSVReader iReader){
			for (LinkedHashMap<String, String> aRow:iReader._data.values()){
				aRow.put(_name, format(aRow));
			}
		}
		

}
