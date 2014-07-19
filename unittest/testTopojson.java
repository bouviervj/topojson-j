import static org.junit.Assert.*;

import java.io.FileNotFoundException;


import json.tools.Compress;
import json.topojson.api.TopojsonApi;
import org.junit.Test;



public class testTopojson {

	@Test
	public void test() throws FileNotFoundException {
		
		for (int i=0; i<6; i++) {
			int aVal = (int) Math.pow(10, i);
			TopojsonApi.shpToTopojsonFile("./data/MA.shp", 
										  "./web/topojson_"+aVal+".json", 
										  "MA", 
										  aVal, 
										  4, 
										  true);		
		}
		
	}

	@Test
	public void testDecompress() throws FileNotFoundException {
		
		String iJsonC = TopojsonApi.shpToTopojson("./data/MA.shp", 
				  "MA", 
				  10, 
				  4, 
				  true);
		
		String iJson;
		try {
			iJson = Compress.decompressB64(iJsonC);
			System.out.println(iJson);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
}
