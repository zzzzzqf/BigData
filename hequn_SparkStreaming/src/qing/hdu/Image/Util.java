package qing.hdu.Image;

import java.io.File;
import java.text.SimpleDateFormat;

public class Util {
	public static String getTime() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd  HH:mm:ss  ");
		String date = sDateFormat.format(new java.util.Date());
		return date;
	}
	
	public static String getOutputTime() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"___yyyy-MM-dd_HH_mm_ss");
		String date = sDateFormat.format(new java.util.Date());
		return date;
	}
	
	public static void log(String info){
		System.out.println(getTime() + info);
	}

}
