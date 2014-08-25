package qing.hdu.Image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

public class QimageProc implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6727722622098696833L;
	private Qimage value;
	private Qimage bg_img;
	private String key;
	public static final String HdfsURL = "hdfs://192.168.178.92:9000";
	public static final String houzhui = QimageRecordWriter.houzhui;
	public static final String image = "image";
	public static final String txt = "txt";
	public static  String outFlag = txt;
	private Hashtable<String, Qimage> bgmap;

	QimageProc() {

	}

	// ���뽫Ҫ�����ͼ�� �����gary()����
	QimageProc(Qimage value) {
		this.value = value;
	}

	// ���뽫Ҫ�����ͼƬ���ֺ�ͼƬ�����koutu(),koutu(Qimage bg)����������ʹ��
	QimageProc(String key, Qimage value) {
		this.key = key;
		this.value = value;
	}

	QimageProc(String key, Qimage value, Qimage bg_img) {
		this.key = key;
		this.value = value;
		this.bg_img = bg_img;
	}

	/*
	 * public void setImage(Qkey key, Qimage value) { // this.key = key; //
	 * this.value = value; }
	 */
	// ��ͼƬת�Ҷ�
	public Qimage gary() throws IOException {

		ByteArrayInputStream input = new ByteArrayInputStream(value.getData()); // �������ܵ��ֽ���
		DataInputStream datainput = new DataInputStream(input); // �ֽ���ת��Ϊ������ҪDataInput��ʽ

		BufferedImage image = ImageIO.read(datainput); // תΪBufferedImage�ṹ��image��ͼ����ֽ�������ΪImageIO.read()�����������ֽ���

		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage grayImage = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_BINARY);// TYPE_BYTE_GRAY�ҶȻ�
												// TYPE_BYTE_GRAY��ֵ��
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				grayImage.setRGB(i, j, rgb);
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		boolean flag = ImageIO.write(grayImage, houzhui, out); // ��ͼ���е�image�ֽ�����bmp�ĸ�ʽ�����out�ֽ����С�
		byte[] b = out.toByteArray();
		value.setImageSize(b.length);
		value.setDate(b);

		input.close();
		datainput.close();
		out.close();
		return value;
	}

	// �������ͼƬ���п�ͼ������ͼƬ��key����hdfs�϶�ȡ
	public Qimage koutu(String OutFlag) throws IOException {

	//	Util.log(value.getImagename() + " start to koutu.");
		if (OutFlag != null) 
			outFlag = OutFlag;
		ByteArrayInputStream input = new ByteArrayInputStream(value.getData()); // �������ܵ��ֽ���
		DataInputStream datainput = new DataInputStream(input); // �ֽ���ת��Ϊ������ҪDataInput��ʽ

		BufferedImage image_src = ImageIO.read(datainput); // תΪBufferedImage�ṹ��image��ͼ����ֽ�������ΪImageIO.read()�����������ֽ���
		Util.log("Qimage::koutu() " + value.getImagename() + "'s length is " + value.getImageSize());
		int width = image_src.getWidth();
		int height = image_src.getHeight();
		byte[] src = new byte[width * height * 3];
		int index = 0;
		// System.out.println(value.getImagename() + " koutu()  height= " +
		// height);
		// System.out.println(value.getImagename() + " koutu()  width= " +
		// width);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int rgb = image_src.getRGB(j, i); // �����i��j˳����Ǻ������ѭ����Ӧ�������˰��л��ǰ�������ȡ���ص�
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
		
		value.setSrcImage(src);
		value.setSrcImageSize(src.length);
		
		// ���뱳��ͼƬ
		Configuration job = new Configuration();
		// ������"MSingle_1_119.png"������ͼƬ�ļ�����ȡ�������1
		Path file = new Path(HdfsURL + "/Qimage/bg/bg_" + value.getCamid()+ ".png");
		FileSystem fs = file.getFileSystem(job);
		FSDataInputStream bgfile = fs.open(file);
		BufferedImage image_ref = ImageIO.read(bgfile);
		int width1 = image_ref.getWidth();
		int height1 = image_ref.getHeight();
		byte[] ref = new byte[width1 * height1 * 3];
		index = 0;
		for (int i = 0; i < height1; i++) {
			for (int j = 0; j < width1; j++) {
				int rgb = image_ref.getRGB(j, i);
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

		if (outFlag.equalsIgnoreCase(image)) {
			BufferedImage grayImage = new BufferedImage(width, height,
					BufferedImage.TYPE_BYTE_GRAY);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					int rgb = res[i * width + j];
					grayImage.setRGB(j, i, rgb);
				}
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// ��ͼ���е�image�ֽ�����bmp�ĸ�ʽ�����out�ֽ����С�
			boolean flag = ImageIO.write(grayImage, houzhui, out);
			byte[] b = out.toByteArray();
			value.setImageSize(b.length);
			value.setDate(b);
			out.close();
			
		} else if (outFlag.equalsIgnoreCase(txt)) {
			
			value.setImageSize(res.length);
			value.setDate(res);
		}

		input.close();
		datainput.close();
		Util.log(value.getImagename() + " finished koutu.");
		return value;
	}

	// �������ͼƬ���п�ͼ������ͼƬ�����βΣ�����ͼƬ�����spark��broadcast��
	public Qimage koutu(Qimage bg) throws IOException {

		// System.out.println(this.key + " start to koutu");
		// System.out.println("bg.length = " + bg.getData().length);
		ByteArrayInputStream input = new ByteArrayInputStream(value.getData()); // �������ܵ��ֽ���
		DataInputStream datainput = new DataInputStream(input); // �ֽ���ת��Ϊ������ҪDataInput��ʽ

		BufferedImage image_src = ImageIO.read(datainput); // תΪBufferedImage�ṹ��image��ͼ����ֽ�������ΪImageIO.read()�����������ֽ���
		if (image_src == null)
			System.out.println("image_src is null");
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

		// ���뱳��ͼƬ
		ByteArrayInputStream bginput = new ByteArrayInputStream(bg.getData()); // �������ܵ��ֽ���
		DataInputStream bgdatainput = new DataInputStream(bginput); // �ֽ���ת��Ϊ������ҪDataInput��ʽ

		BufferedImage image_ref = ImageIO.read(bgdatainput); // תΪBufferedImage�ṹ��image��ͼ����ֽ�������ΪImageIO.read()�����������ֽ���
		if (image_ref == null)
			System.out.println("image_ref is null");
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

		byte[] res = new byte[width * height];
		res = JniImageProc.Java_Koutu(src, ref);

		// System.out.println(this.key + " koutu is over.");

		BufferedImage grayImage = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = res[i * height + j];
				grayImage.setRGB(i, j, rgb);
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		boolean flag = ImageIO.write(grayImage, houzhui, out); // ��ͼ���е�image�ֽ�����bmp�ĸ�ʽ�����out�ֽ����С�
		byte[] b = out.toByteArray();
		value.setImageSize(b.length);
		value.setDate(b);

		input.close();
		datainput.close();
		bginput.close();
		bgdatainput.close();
		out.close();
		return value;
	}

	public void setBgmap(Hashtable<String, Qimage> map) {
		bgmap = map;
		System.out.println("Qimage::setBgmap map.size = " + map.size());
		Iterator i = bgmap.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();

			String key = (String) entry.getKey();
			Qimage value = (Qimage) entry.getValue();
			System.out.println(key + " :" + value.getData().length);
		}
	}

	public Qimage IPVH() {
		Util.log("QimageProc::IPVH() value.getImagename() = " + value.getImagename() +" ,value.length = "+ value.getImageSize());
		float[] res = JniImageProc.Java_IPVH(value.getData(),
				DoubleToFloat(new CalibMatrix().calib));
		value.setPointCloud(res);
		return value;
	}

	public float[] DoubleToFloat(double[] a) {
		float[] b = new float[a.length];
		for (int i = 0; i < a.length; i++)
			b[i] = (float) a[i];
		return b;
	}
	
	
	public Qimage ColorIBVH(){
		Util.log("QimageProc::ColorIBVH() value.getImagename() = " + value.getImagename() +" ,value.length = "+ value.getImageSize());
		Point3D[] res = JniImageProc.Java_Color_IBVH(value.getData(),value.getR(),value.getG(),value.getB(),DoubleToFloat(new CalibMatrix().calib));
		value.setColorPointCloud(res);
		return value;
	}
}
