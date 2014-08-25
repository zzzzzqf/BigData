package qing.hdu.Image;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class ColorPointCloudRecordWriter<K, V> extends RecordWriter<K, V> {

	private Path outputPath = null;
	private FileSystem fs = null;
	public static final String houzhui = ".ply";
	
	public ColorPointCloudRecordWriter(FileSystem fs, Path outputPath){
		this.fs = fs;
		this.outputPath = outputPath;
	}
	
	@Override
	public void close(TaskAttemptContext arg0) throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(K key, V value) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		if (!(key == null && key instanceof NullWritable)) {
			// 如果key不为空者输出key
			if (!(value == null && value instanceof NullWritable)) {
				// 如果value不为空则输出value
				if ((Object) value instanceof Qimage) {
					Qimage ply = (Qimage) value;
					Point3D data[] = ply.getColorPointCloud();
					if (data.length >= 2) {
						Util.log("ColorPointCloudRecordWriter::write() key = "+ ply.getFrame());
						// 创建文件
						FSDataOutputStream fileStream = null;
						Path filePath = new Path(outputPath + "/" + ply.getFrame() + houzhui);
						fileStream = fs.create(filePath, false);
						// fileStream.write(data);
						String head = "ply\r\n"
								+ "format ascii 1.0\r\n"
								+ "comment VCGLIB generated\r\n"
								+ "element vertex " + data.length
								+ "\r\nproperty float x\r\n"
								+ "property float y\r\n"
								+ "property float z\r\n"
								+ "property float nx\r\n"
								+ "property float ny\r\n"
								+ "property float nz\r\n"
								+ "property uchar red\r\n"
								+ "property uchar green\r\n"
								+ "property uchar blue\r\n"
								+ "element face 0\r\n"
								+ "property list uchar int vertex_indices\r\n"
								+ "end_header\r\n";
						fileStream.write(head.getBytes());
						int len = data.length;
						for (int i = 1; i <= len; ++i) {
							fileStream.write(String.valueOf(data[i - 1].a).getBytes());
							fileStream.write(" ".getBytes());
							
							fileStream.write(String.valueOf(data[i - 1].b).getBytes());
							fileStream.write(" ".getBytes());
							
							fileStream.write(String.valueOf(data[i - 1].c).getBytes());
							fileStream.write(" ".getBytes());
							
							fileStream.write(String.valueOf(data[i - 1].x).getBytes());
							fileStream.write(" ".getBytes());
							
							fileStream.write(String.valueOf(data[i - 1].y).getBytes());
							fileStream.write(" ".getBytes());
							
							fileStream.write(String.valueOf(data[i - 1].z).getBytes());
							fileStream.write(" ".getBytes());
							
							fileStream.write(String.valueOf(data[i - 1].rr).getBytes());
							fileStream.write(" ".getBytes());
							
							fileStream.write(String.valueOf(data[i - 1].gg).getBytes());
							fileStream.write(" ".getBytes());
							
							fileStream.write(String.valueOf(data[i - 1].bb).getBytes());
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
