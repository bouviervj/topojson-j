package json.converter.csv.filter;

import java.util.Vector;

import json.converter.csv.CSVReader;
import json.converter.csv.merger.Merger.MergeStep;

public class Filter {

	Vector<FilterStep> _vect = new Vector<FilterStep>();

	public static class FilterStep {
		
		String _column;
		String _value;
		
		public FilterStep(String iColumn, String iValue){
				_column = iColumn;
				_value = iValue;
		}
		
		public void process(CSVReader iReader){
				iReader.filter(_column,_value);
		}
	
}

	public Filter(){
		_vect = new Vector<FilterStep>();
	}
	
	public void addStep(FilterStep iStep){
		_vect.add(iStep);
	}
	
	public void process(CSVReader iReader){
		
		for (FilterStep aStep:_vect){
			aStep.process(iReader);
		}
		
	}
	
}
