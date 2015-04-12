package json.converter.dbf;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.cfb.jdbf.DBFReader;
import org.cfb.jdbf.JDBFException;

public class DBFExtractor {

	public static void extractDBFDataToCSV(String iFileName, String iCSVName) {
		
		try {

			DBFReader dbfreader = new DBFReader(iFileName);

			PrintWriter writer = new PrintWriter(iCSVName);
			
			StringBuffer aBuffer = new StringBuffer();
			for (int i=0; i<dbfreader.getFieldCount(); i++) {
				aBuffer.append(dbfreader.getField(i).getName()+(i+1==dbfreader.getFieldCount()?"":","));
			}
			writer.println(aBuffer.toString());
			
			for(int i = 0; dbfreader.hasNextRecord(); i++)
			{
				Object aobj[];

				aBuffer.delete(0, aBuffer.length());
				
				aobj = dbfreader.nextRecord(Charset.forName("GBK"));

				for (int j=0; j<aobj.length; j++)
					aBuffer.append(aobj[j]+(j+1==aobj.length?"":","));
				
				writer.println(aBuffer.toString());
			}

			writer.close();

		} catch (JDBFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
}
