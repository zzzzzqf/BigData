import java.io.IOException;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class Main {
	 public static void main(String[] args) throws IOException {		 
		 Configuration conf = new Configuration();
			conf.addResource(new Path("E:\\eclipse-2.3.0\\hadoop-2.3.0\\etc\\hadoop\\hdfs-site.xml"));
			conf.addResource(new Path("E:\\eclipse-2.3.0\\hadoop-2.3.0\\etc\\hadoop\\mapred-site.xml"));
			conf.addResource(new Path("E:\\eclipse-2.3.0\\hadoop-2.3.0\\etc\\hadoop\\core-site.xml"));
			conf.addResource(new Path("E:\\eclipse-2.3.0\\hadoop-2.3.0\\etc\\hadoop\\yarn-site.xml"));
			
			Path path = new Path("/Qimage/hdfs/"); 
			FileSystem fs = path.getFileSystem(conf);
		 
		// String savepath = "C:\\Users\\Administrator\\Desktop\\123";
		 Server ser = new Server(9999,fs,path);
		 ser.start();
		 //输入0，关闭服务端
		 Scanner sc = new Scanner( System.in );
			if(sc.nextInt() == 0)
				ser.close();
	 }
	 
}
