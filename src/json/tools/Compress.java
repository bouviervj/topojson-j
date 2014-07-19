package json.tools;

import java.nio.charset.StandardCharsets;

import javax.xml.bind.DatatypeConverter;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Decompressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4UnknownSizeDecompressor;

public class Compress {

	public static String compressB64(String iStr){
	
		byte[] aInputData = iStr.getBytes(StandardCharsets.ISO_8859_1);
		
		LZ4Factory factory = LZ4Factory.fastestInstance();
		
		LZ4Compressor compressor = factory.fastCompressor();
		int maxCompressedLength = compressor.maxCompressedLength(aInputData.length);
		byte[] compressed = new byte[maxCompressedLength];
		int compressedLength = compressor.compress(aInputData, 0, aInputData.length, compressed, 0, maxCompressedLength);
	
		byte[] compressedFinal = new byte[compressedLength];
		System.arraycopy(compressed, 0, compressedFinal, 0, compressedLength);
		
		return DatatypeConverter.printBase64Binary(compressedFinal);
		
	}
	
	public static String decompressB64(String iStr){
		
	
		byte[] aInputData = DatatypeConverter.parseBase64Binary(iStr);
		
		LZ4Factory factory = LZ4Factory.fastestInstance();
		
		int primaryLength = aInputData.length*5; 
		byte[] aOutputData = new byte[primaryLength]; 
		
		LZ4UnknownSizeDecompressor decompressor = factory.unknwonSizeDecompressor();
		int decompressed = decompressor.decompress(aInputData, 
												   0, aInputData.length, 
												   aOutputData, 0);
				
		return new String(aOutputData,StandardCharsets.ISO_8859_1);
		
	}
	
}
