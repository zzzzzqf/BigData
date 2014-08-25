package qing.hdu.Image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class QimageRecordWriter<K, V> extends RecordWriter<K, V> {

	private static final String utf8 = "UTF-8";
	private static final byte[] newline;
	public static final String houzhui = "bmp";
	static {
		try {
			newline = "\n".getBytes(utf8);
		} catch (UnsupportedEncodingException uee) {
			throw new IllegalArgumentException("can't find " + utf8
					+ " encoding");
		}
	}

	// private FSDataOutputStream fileStream = null;
	private Path outputPath = null;
	private FileSystem fs = null;

	public QimageRecordWriter(FileSystem fs, Path outputPath) {
		// TODO Auto-generated constructor stub

		this.fs = fs;
		this.outputPath = outputPath;
	}

	@Override
	public void close(TaskAttemptContext arg0) throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub
	}

	@Override
	public void write(K key, V value) throws IOException,
	// public synchronized void write(K key, V value) throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub

		if (!(key == null && key instanceof NullWritable)) {
			// 如果key不为空者输出key
			if (!(value == null && value instanceof NullWritable)) {
				// 如果value不为空则输出value
				if ((Object) value instanceof Qimage) {
					Qimage img = (Qimage) value;
					// Qkey qkey = (Qkey) key;
					byte data[] = img.getData();
					if (data.length >= 2) {
						Util.log("QimageRecordWriter::write() key = "+ img.getImagename());
						// 创建文件
						FSDataOutputStream fileStream = null;
						if (QimageProc.outFlag.equalsIgnoreCase(QimageProc.image)) {
							Path filePath = new Path(outputPath + "/"+ img.getFrame() + "/" + img.getImagename() + Util.getOutputTime() + ".bmp");
							fileStream = fs.create(filePath, false);
							fileStream.write(data);
						} else if (QimageProc.outFlag.equalsIgnoreCase(QimageProc.txt)) {
							Path filePath = new Path(outputPath + "/"+ img.getFrame() + "/" + img.getImagename()+ Util.getOutputTime() + ".txt");
							fileStream = fs.create(filePath, false);
							// 向hdfs上写入转化好的文件,byte转化为int，byte的范围自动转化
							for (int i = 0; i < data.length; i++) {
								fileStream.write(String.valueOf((int) data[i] & 0xFF).getBytes());
								fileStream.write(" ".getBytes());
							}
						}
						fileStream.flush();
						fileStream.close();
					} else {
						System.out.println("QimageRecordWriter::write() key = " + key + ", But data is null!");
					}

				} else {
					System.out.println("fileStream had been created.");
				}
			}

		}
	}

}
