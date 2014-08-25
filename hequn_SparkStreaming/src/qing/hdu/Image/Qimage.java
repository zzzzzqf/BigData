package qing.hdu.Image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class Qimage implements Writable, java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int fileLength;
	//这里保存了抠图后的结果
	private byte[] data;
	public int qing;
	private String imagename;
	private String frame;
	private String camid;
	private float[] pointcloud;
	private byte[] R;
	private byte[] G;
	private byte[] B;
	private Point3D colorPointCloud[]; 
	//保存了原图片
	private byte[] src;
	private int srcLength;

	public Qimage() {
		byte[] bydata = new byte[10];
		data = bydata;
		pointcloud = null;
	}

	public Qimage(byte[] data, byte[] name) {
		this.fileLength = data.length;
		this.data = data;
		if (name.length < 255) {
			String filename = new String(name);
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
			
			Util.log(imagename + " had received, length is " + fileLength + " camid is " + camid + " frame is " + frame);
		}
		else
			Util.log(imagename + " filename's length is " + name.length);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		int size;
		// qing = in.readInt();
		// System.out.println("Qimage::readFields() qing = " + qing);
		imagename = in.readUTF();
		frame = in.readUTF();
		camid = in.readUTF();
		size = in.readInt();
		// System.out.println("Qimage::readFields() size = " + size);
		if (size >= 0) {
			byte[] indata = new byte[(int) size];
			in.readFully(indata, 0, (int) size);
			data = indata;
		} else {
			size = 1;
			byte[] indata = new byte[(int) size];
			indata[0] = 0;
			data = indata;
		}
		fileLength = size;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		// out.writeInt(qing);
		// System.out.println("Qimage::write() qing = " + qing);
		out.writeBytes(imagename);
		out.writeBytes(frame);
		out.writeBytes(camid);
		out.writeInt(fileLength);
		// System.out.println("Qimage::write() fileLength = " + fileLength);
		out.write(data);
	}

	public byte[] getData() {
		return data;
	}

	public void setDate(byte[] data) {
		this.data = data;
	}

	public int getImageSize() {
		return this.fileLength;
	}

	public void setImageSize(int size) {
		this.fileLength = size;
	}

	public String getImagename() {
		return imagename.toString();
	}

	public String getFrame() {
		return frame.toString();
	}

	public String getCamid() {
		return camid.toString();
	}

	public float[] getPointCloud() {
		return pointcloud;
	}

	public void setPointCloud(float[] pc) {
		this.pointcloud = pc;
	}

	public Qimage add(Qimage other) {
/*
		System.out.println(imagename + " + " + other.getImagename()
				+ " is added.");
		System.out
				.println(imagename + " length is " + fileLength + " , "
						+ other.getImagename() + " length is "
						+ other.getData().length);*/
		int preLength = this.fileLength;
		this.fileLength = this.fileLength + other.getImageSize();
		// 用Array.copy的方法。
		byte[] newData = new byte[fileLength];
		System.arraycopy(this.data, 0, newData, 0, preLength);
		System.arraycopy(other.data, 0, newData, preLength,other.getImageSize());
		this.data = newData;
		
		//add RGB
		preLength = this.srcLength;
		this.srcLength = preLength + other.getSrcImageSize();
		byte[] newR = new byte[srcLength/3];
		byte[] newG = new byte[srcLength/3];
		byte[] newB = new byte[srcLength/3];
		System.arraycopy(this.R, 0, newR, 0, preLength/3);
		System.arraycopy(this.G, 0, newG, 0, preLength/3);
		System.arraycopy(this.B, 0, newB, 0, preLength/3);
		System.arraycopy(other.R, 0, newR, preLength/3,other.getSrcImageSize()/3);
		System.arraycopy(other.G, 0, newG, preLength/3,other.getSrcImageSize()/3);
		System.arraycopy(other.B, 0, newB, preLength/3,other.getSrcImageSize()/3);
		this.R = newR;
		this.G = newG;
		this.B = newB;
		
		return this;
	}

	public Qimage koutu() throws IOException {

		System.out.println("value.getImagename = " + imagename
				+ " , start to koutu.");

		ByteArrayInputStream input = new ByteArrayInputStream(data); // 创建接受的字节流
		DataInputStream datainput = new DataInputStream(input); // 字节流转化为参数需要DataInput格式

		BufferedImage image_src = ImageIO.read(datainput); // 转为BufferedImage结构，image是图像的字节流，因为ImageIO.read()函数不接受字节流
		int width = image_src.getWidth();
		int height = image_src.getHeight();
		byte[] src = new byte[width * height * 3];
		int index = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image_src.getRGB(i, j);
				Color c = new Color(rgb);
				int r = c.getRed();
				int g = c.getGreen();
				int b = c.getBlue();
				src[index] = (byte) (r);
				src[index + 1] = (byte) (g);
				src[index + 2] = (byte) (b);
				index = index + 3;
			}
		}

		// 读入背景图片
		Configuration job = new Configuration();
		// 将类似"MSingle_1_119.png"这样的图片文件名中取出相机号1
		// String str = key.getCamid();
		// String sub = str.substring(str.indexOf('_')+1,str.length());
		// String frame = sub.substring(0,sub.indexOf("_"));
		Path file = new Path(QimageProc.HdfsURL + "/Qimage/bg/bg_" + camid
				+ ".png");
		FileSystem fs = file.getFileSystem(job);
		FSDataInputStream bgfile = fs.open(file);
		BufferedImage image_ref = ImageIO.read(bgfile);
		int width1 = image_ref.getWidth();
		int height1 = image_ref.getHeight();
		byte[] ref = new byte[width1 * height1 * 3];
		index = 0;
		for (int i = 0; i < width1; i++) {
			for (int j = 0; j < height1; j++) {
				int rgb = image_ref.getRGB(i, j);
				Color c = new Color(rgb);
				int r = c.getRed();
				int g = c.getGreen();
				int b = c.getBlue();
				ref[index] = (byte) (r);
				ref[index + 1] = (byte) (g);
				ref[index + 2] = (byte) (b);
				index = index + 3;
			}
		}
		bgfile.close();

		byte[] res = new byte[width * height];
		res = JniImageProc.Java_Koutu(src, ref);

		BufferedImage grayImage = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = res[i * height + j];
				grayImage.setRGB(i, j, rgb);
			}
		}

		// System.out.println("Map::map() grayImage.getWidth = " +
		// grayImage.getWidth());
		// System.out.println("Map::map() grayImage.getHeight = " +
		// grayImage.getHeight());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		boolean flag = ImageIO.write(grayImage, QimageProc.houzhui, out); // 将图像中的image字节流以bmp的格式输出到out字节流中。
		byte[] b = out.toByteArray();
		setImageSize(b.length);
		setDate(b);

		input.close();
		datainput.close();
		out.close();
		return this;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "image name is " + this.getImagename() + ", data length is "
				+ fileLength;
	}

	public void setSrcImage(byte[] src){
	//	this.src = new byte[src.length];
	//	this.src = src;
		int index = 0;
		R = new byte[src.length/3];
		G = new byte[src.length/3];
		B = new byte[src.length/3];
		for(int i=0;i<src.length/3;i++){
			R[i] = src[index];
			G[i] = src[index+1];
			B[i] = src[index+2];
			index = index + 3;
		}
		Util.log("Qimage::setSrcImage() src.length is "  + src.length);
	}
	
	public void setSrcImageSize(int length){
		srcLength = length;
	}
	
	public int getSrcImageSize()
	{
		return srcLength;
	}
	
	public byte[] getR(){
		return R;
	}
	
	public byte[] getG(){
		return G;
	}
	
	public byte[] getB(){
		return B;
	}
	
	public void setColorPointCloud(Point3D[] points){
		this.colorPointCloud = new Point3D[points.length];
		this.colorPointCloud = points;
	}
	
	public Point3D[] getColorPointCloud(){
		return colorPointCloud;
	}
}
