package qing.hdu.Image;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PointCloudOutputFormat<K, V> extends FileOutputFormat<K,V> {

	@Override
	public RecordWriter<K, V> getRecordWriter(TaskAttemptContext context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		Configuration conf = context.getConfiguration();
        Path outputPath = getOutputPath(context);
        FileSystem fs = outputPath.getFileSystem(conf);
		return new PointCloudRecordWriter<K,V>(fs,outputPath);
	}

}
