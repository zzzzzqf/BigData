package qing.hdu.Image;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class PointCloudRecordWriter<K, V> extends RecordWriter<K, V> {

	private Path outputPath = null;
	private FileSystem fs = null;
	public static final String houzhui = ".xyz";

	public PointCloudRecordWriter(FileSystem fs, Path outputPath) {
		// TODO Auto-generated constructor stub
		this.fs = fs;
		this.outputPath = outputPath;
	}

	@Override
	public void close(TaskAttemptContext arg0) throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub
		//fs.close();
	}

	@Override
	public void write(K key, V value) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		if (!(key == null && key instanceof NullWritable)) {
			// 如果key不为空者输出key
			if (!(value == null && value instanceof NullWritable)) {
				// 如果value不为空则输出value
				if ((Object) value instanceof Qimage) {
					Qimage off = (Qimage) value;
					float data[] = off.getPointCloud();
					if (data.length >= 2) {
						Util.log("QimageRecordWriter::write() key = "+ off.getFrame());
						// 创建文件
						FSDataOutputStream fileStream = null;
						Path filePath = new Path(outputPath + "/" + off.getFrame()+ Util.getOutputTime()  + houzhui);
						fileStream = fs.create(filePath, false);
						// fileStream.write(data);
						int len = data.length;
						for (int i = 1; i <= len; ++i) {
							fileStream.write(String.valueOf(data[i - 1]).getBytes());
							if (i % 6 != 0)
								fileStream.write(" ".getBytes());
							else
								fileStream.write("\r\n".getBytes());
						}
						fileStream.flush();
						fileStream.close();
					} else {
						System.out.println("QimageRecordWriter::write() key = "
								+ key + ", But data is null!");
					}
				} else {
					System.out.println("fileStream had been created.");
				}

			}
		}
	}

}
