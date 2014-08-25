package qing.hdu.Image;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class QimageRecordReader<K, V> extends RecordReader<K,V> {
	
	public static int count = 0;
	private K key = null;
	private V value = null;
	private FSDataInputStream filein = null;
	private boolean isFileEnd ;
	private int fileLength;
	private String imagename;
	private String frame;
	private String camid;
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub	
		if(filein!=null)
			filein.close();
	}

	@Override
	public K getCurrentKey() throws IOException, InterruptedException {
		// TODO Auto-generated method stub	
		return key;
	}

	@Override
	public V getCurrentValue() throws IOException, InterruptedException {
		// TODO Auto-generated method stub	
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		if(isFileEnd) return 1.f;
		else return 0;
	}

	@Override
	public void initialize(InputSplit input, TaskAttemptContext context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub

		FileSplit split = (FileSplit) input;
		fileLength = (int) split.getLength();
		Configuration job = context.getConfiguration();
		Path file = split.getPath();
		FileSystem fs = file.getFileSystem(job);
		
		filein = fs.open(file);
		String filename = file.getName();
		
		imagename = filename.substring(0, filename.indexOf('.'));
		
		String[] strarray=imagename.split("_"); 
		if(strarray.length <= 2){
			camid = strarray[1];
			frame = "0000";
		}
		else{
			camid = strarray[1];
			frame = strarray[2];
		}
	/*	String sub = imagename.substring(imagename.indexOf('_') + 1, imagename.length());
		// MSingle_0_119
		//CamData_17_17_2014_6_11_15I47I6.bmp
		if (sub.indexOf("_") > 0) {
			camid = sub.substring(0, sub.indexOf("_"));
			frame = sub.substring(sub.indexOf("_") + 1, sub.length());
		} else {
			// bg_10
			camid = sub.substring(0, sub.length());
			frame = "0000";
		}
		*/
		key = (K) frame;  //获取文件的名字，不包括后缀。如abc.txt,那么key=abc
		//key = (K) new Qkey(filename.substring(0, filename.indexOf('.')));
		value = (V) new Qimage();
		isFileEnd = false;
		
//		System.out.println("key = " + key +" , InputSplit.length = " + input.getLength() 
//				+ " , FileSplit.length = " + fileLength);
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
	
		//如读到文件End，则返回。
		if(isFileEnd)
			return false;
		
		byte data[] = new byte[filein.available()];	 //创建一个字节类型的数组，用来接收整个图像文件
		filein.readFully(0, data, 0, fileLength);
			
		//Util.log("QimageRecordReader::nextKeyValue() fileLength = " + fileLength);
		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(byteOut);
		//int a =1;
		//dataOut.writeInt(a);
		dataOut.writeUTF(imagename);
		dataOut.writeUTF(frame);
		dataOut.writeUTF(camid);
		////
		dataOut.writeInt(fileLength); //将文件的大小传入进去
		dataOut.write(data);
		
		ByteArrayInputStream input = new ByteArrayInputStream(byteOut.toByteArray());  //创建接受的字节流
		DataInputStream datainput = new DataInputStream(input);  //字节流转化为参数需要DataInput格式

		((Qimage)value).readFields(new DataInputStream(datainput));
	
		isFileEnd = true;
		return true;
	}

}
