package json.converter.csv.merger;

import java.util.Vector;

import json.converter.csv.CSVReader;

public class Merger {

	Vector<MergeStep> _vect = new Vector<MergeStep>();
	
	public static class MergeStep {
		
		    String _col1;
		    String _format1;
		    String _col2;
		    String _format2;
		    
		    CSVReader _reader;
		    
		    boolean _join;
		
			public MergeStep(String iCol1, String iFormat1, String iFileName, String iCol2, String iFormat2){
					_col1 = iCol1;
					_format1 = iFormat1;
					_reader = new CSVReader(iFileName);
					_reader.read();
					_col2 = iCol2;
					_format2 = iFormat2;
					_join = false;
			}
			
			public MergeStep(String iCol1, String iFormat1, String iFileName, String iCol2, String iFormat2, boolean iJoin){
					_col1 = iCol1;
					_format1 = iFormat1;
					_reader = new CSVReader(iFileName);
					_reader.read();
					_col2 = iCol2;
					_format2 = iFormat2;
					_join = iJoin;
			}
			
			public void process(CSVReader iReader){
					iReader.merge(_col1, _format1, _reader, _col2, _format2, _join);
			}
		
	}
	
	public Merger(){
		_vect = new Vector<MergeStep>();
	}
	
	public void addStep(MergeStep iStep){
		_vect.add(iStep);
	}

	public void process(CSVReader iReader){
		
		for (MergeStep aStep:_vect){
			aStep.process(iReader);
		}
		
	}
	
}
