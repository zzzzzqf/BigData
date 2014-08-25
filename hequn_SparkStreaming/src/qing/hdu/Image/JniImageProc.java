package qing.hdu.Image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Date;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

public class JniImageProc {

	// JNI Functions
	private static native byte[] JNI_Koutu(byte[] src, byte[] ref, int width,
			int height, int channels, int bit_depth, int color_type);

	private static native float[] JNI_IPVH(byte[] src, float[] cabli,
			int ViewNum, int PointNum);

	private static native Point3D[] JNI_Color_IBVH(byte[] src, byte[] srcR,byte[] srcG,byte[] srcB,float[] cabli);
	
	public static native String show(String str);

	
	// Java Functions
	public static byte[] Java_Koutu(byte[] src, byte[] ref) {

		int width = 1024;
		int height = 768;
		int channels = 3;
		int bit_depth = 8;
		int color_type = 2;
		return JNI_Koutu(src, ref, width, height, channels, bit_depth,
				color_type);
	}

	public static float[] Java_IPVH(byte[] src, float[] calic) {
		int ViewNum = 20;
		int PointNum = 400000;
		return JNI_IPVH(src, calic, ViewNum, PointNum);
	}

	public static Point3D[] Java_Color_IBVH(byte[] src, byte[] srcR,byte[] srcG,byte[] srcB,float[] cabli){
		return JNI_Color_IBVH(src, srcR,srcG,srcB, cabli);
	}
	
	static {
		try {
			NativeUtils.loadLibraryFromJar("/lib/libkoutu.so");
			//NativeUtils.loadLibraryFromJar("/lib/libImageTool.so");
		} catch (IOException e) {
			e.printStackTrace(); // This is probably not the best way to handle
									// exception :-)
		}
	}

	public static void main(String[] args) throws IOException {
	}
}
