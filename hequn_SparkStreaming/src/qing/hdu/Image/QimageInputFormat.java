package qing.hdu.Image;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class QimageInputFormat<K, V> extends FileInputFormat<K, V> {

	@Override
	public RecordReader<K,V> createRecordReader(InputSplit arg0,
			TaskAttemptContext arg1) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		return new QimageRecordReader<K,V>();
	}

	//�Ƿ���������һ���ļ����з�Ƭ,ͼ����Ϊ�������룬����Ƭ��ֱ��returnΪfalse.
	@Override
	protected boolean isSplitable(JobContext context, Path filename) {
		// TODO Auto-generated method stub
		return false;
	}

}
