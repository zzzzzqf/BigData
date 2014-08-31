import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.hadoop.fs.FSDataOutputStream;

public class Util {
	

	
	public static String getTime() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd  HH:mm:ss  ");
		String date = sDateFormat.format(new java.util.Date());
		return date;
	}

	public static void log(String info) {
		if (info == null)
			System.out.println();
		else
			System.out.println(getTime() + info);
	}

	public static ArrayList<File> getListFiles(Object obj) {
		File directory = null;
		if (obj instanceof File) {
			directory = (File) obj;
		} else {
			directory = new File(obj.toString());
		}
		ArrayList<File> files = new ArrayList<File>();
		if (directory.isFile()) {
			files.add(directory);
			return files;
		} else if (directory.isDirectory()) {
			File[] fileArr = directory.listFiles();
			for (int i = 0; i < fileArr.length; i++) {
				File fileOne = fileArr[i];
				files.addAll(getListFiles(fileOne));
			}
		}
		return files;
	}

	public static byte[] StringToByteArray(String str) {
		byte[] temp = new byte[100];
		try {
			temp = str.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}

	public static String ByteArrayToString(byte[] bAttr, int maxLen) {
		int index = 0;
		while (index < bAttr.length && index < maxLen) {
			if (bAttr[index] == 0) {
				break;
			}
			index++;
		}
		byte[] tmp = new byte[index];
		System.arraycopy(bAttr, 0, tmp, 0, index);
		String str = null;
		try {
			str = new String(tmp, "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;

	}

	public static void printHexString(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			System.out.print(hex.toUpperCase() + " ");
		}
		System.out.println();
	}

	static void printBinaryByteArray(byte[] b,int len){
		byte[] bb = new byte[len];
		System.arraycopy(b, 0, bb, 0, len);
		for(byte bbb:bb){
			printBinaryByte(bbb);
		}
	}
	
	static void printBinaryByte(byte b) {
		for (int i = 7; i >= 0; --i) {
			if (((1 << i) & b) == 0) {
				System.out.print("0");
			} else {
				System.out.print("1");
			}
		}
		System.out.println();
	}
	
	
	public static void BMPWriter(FSDataOutputStream fileStream , byte[] rgb , int h , int w) throws IOException{
		int width = w;
		int height = h;
		int fheader = 14;
		int infoheader = 40;
		int board = 0;
		int offset = fheader + infoheader + board;
		int length = width * height * 3 + offset;
		short frame = 1;
		short deep = 24;
		int fbl = 3800;
		
			fileStream.write('B');
			fileStream.write('M');// 1格式头
			wInt(fileStream, length);// 2-3文件大小
			wInt(fileStream, 0);// 4-5保留
			wInt(fileStream, offset);// 6-7偏移量
			wInt(fileStream, infoheader);// 8-9头信息
			wInt(fileStream, width);// 10-11宽
			wInt(fileStream, height);// 12-13高
			wShort(fileStream, frame);// 14 = 1帧数
			wShort(fileStream, deep);// 15 = 24位数
			wInt(fileStream, 0);// 16-17压缩
			wInt(fileStream, 4);// 18-19 size
			wInt(fileStream, fbl);// 20-21水平分辨率
			wInt(fileStream, fbl);// 22-23垂直分辨率
			wInt(fileStream, 0);// 24-25颜色索引 0为所有
			wInt(fileStream, 0);// 26-27重要颜色索引 0为所有
			// wInt(0);//28-35
			// wInt(0);
			// wInt(0);
			// wInt(0);//28-35彩色板
		/*	byte [] reverseRGB = new byte[width * height * 3];
			int index = 0 ;
			int total = width * height;
			for(int i = 0;i<height;i++){
				for(int j =0; j< width; j++){
					reverseRGB[i*height]
				}
				reverseRGB[index] = rgb[total*3-index-3];
				reverseRGB[index+1] = rgb[total*3-index-2];
				reverseRGB[index+2] = rgb[total*3-index-1];
				index = index +3;
			}
			fileStream.write(reverseRGB);*/
			fileStream.write(rgb);
			
		}
	
	
	public static void JpegWriter(FSDataOutputStream fileStream , byte[] rgb ){
		
	}
	
	public static void wInt(DataOutputStream fileStream, int i) throws IOException {
		fileStream.write(i);
		fileStream.write(i >> 8);
		fileStream.write(i >> 16);
		fileStream.write(i >> 24);
	}

	public static void wShort(DataOutputStream fileStream, short i)
			throws IOException {
		fileStream.write(i);
		fileStream.write(i >> 8);
	}
}
