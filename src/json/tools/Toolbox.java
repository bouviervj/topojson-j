package json.tools;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

public class Toolbox {

	static Projection sProj;
	
	static {
		
		sProj = ProjectionFactory.getNamedPROJ4CoordinateSystem("esri:102003");
	
	}
	
	// TODO change this as not thread safe
	public static void setCoordinateSystem(String iCoordinate){
		sProj = ProjectionFactory.getNamedPROJ4CoordinateSystem(iCoordinate);
	}
	
	public static Point2D.Double convertLatLong(double X, double Y){
		
		Point2D.Double aSrc = new Point2D.Double(X, Y);
		Point2D.Double aDst = new Point2D.Double();
		
		sProj.inverseTransform(aSrc, aDst);
		//aDst.x = aSrc.x;
		//aDst.y = aSrc.y;
		
		return aDst;
		
	}
	
	public static int little2big(byte[] iBytes) {
		return ByteBuffer.wrap(iBytes).order(ByteOrder.LITTLE_ENDIAN ).getInt();
	}

	public static double getDoubleFromByte(byte[] aDoubleBuffer){
		return ByteBuffer.wrap(aDoubleBuffer).order(ByteOrder.LITTLE_ENDIAN ).getDouble();
	}
	
	public static void writeFile(String iFile, String iData){
		
		FileOutputStream aStream;
		try {
			aStream = new FileOutputStream(new File(iFile));
			aStream.write(iData.getBytes());
			aStream.flush();
			aStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}
