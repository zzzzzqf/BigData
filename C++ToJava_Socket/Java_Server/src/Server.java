import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class Server {

	private int port;
	public static boolean flag = false;
	public static boolean Terminal = true;
	public static boolean suspend = false;
	private ListenThread listenThread = null;
	private List<HandleThread> handleThread_list = null;
	private Path path = null;
	private FileSystem fs = null;
	private String localpath = null;
	private int count = 0;
	private boolean END = false;

	Server(int listenPort) {
		this.port = listenPort;
	}

	public Server(int listenPort, String path) {
		// TODO Auto-generated constructor stub
		this.port = listenPort;
		this.localpath = path;
	}

	public Server(int listenPort, FileSystem fs, Path path) {
		// TODO Auto-generated constructor stub
		this.port = listenPort;
		this.path = path;
		this.fs = fs;
	}

	// 鍚姩鏈嶅姟绔�
	public void start() {
		if (listenThread == null) {
			Util.log("Server is starting, port is " + port);
			listenThread = new ListenThread();
			listenThread.start();
		}
	}

	private class ListenThread extends Thread {

		@Override
		public void run() {
			try {
				ServerSocket server = new ServerSocket(port);
				// END = true 鍏抽棴鐩戝惉绾跨▼
				while (!END) {
					Socket socket = server.accept();
					Util.log(socket.getRemoteSocketAddress()
							+ " connection is established.");
					new HandleThread(socket).start();
				}
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class HandleThread extends Thread {

		private Socket socket;

		HandleThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {

				DataInputStream is = new DataInputStream(socket.getInputStream());
				DataOutputStream os = new DataOutputStream(socket.getOutputStream());
				byte[] buf = new byte[4096];
				String respond = "file received.";
				int len = 0;
				//接受参数结构体的大小
				len = is.read(buf, 0, 4);
				int param_length = b2i(buf);
				//接受参数结构体并解析
				len = is.read(buf, 0, param_length);
				QingParams imageParams = ByteToParams(buf, param_length);
				//创建图片文件
				Path filepath = new Path(path + "/" + imageParams.name);
				byte [] rgbs = new byte[imageParams.filelength];
				FSDataOutputStream fileStream = null;
				if (fs.exists(filepath))
					fs.delete(filepath, true);
				fileStream = fs.create(filepath);

				int reclen = 0;
				int count = 0;
				while (reclen < imageParams.filelength) {
					len = is.read(buf);
					if (len == -1) {
						Util.log("reveiced data error.");
						break;
					}
				/*	if (len < 1024) {
						count++;
					}		*/	
					
					
				//	System.arraycopy(buf, 0, rgbs, reclen, len);
					//这里重大错误！！！
					//不能这么写fileStream.write(buf); 
					//如果buf接收到数据不到1024个，那么fileStream也会写入buf的长度，1024个字符，这样就会造成了
					//写入hdfs的文件比原始要大，有时候正确，有时候又不正确了。蛋疼！！
					fileStream.write(buf,0,len);
					reclen += len;
				}
				Util.log(imageParams.name + " has received " + reclen + " !!!");
				//Util.BMPWriter(fileStream,rgbs,imageParams.height,imageParams.width);
				Util.log(imageParams.toString());
				// os.write(respond.getBytes());
				socket.close();
				is.close();
				os.close();
				fileStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static byte[] i2b(int i) {
		return new byte[] { (byte) ((i >> 24) & 0xFF),
				(byte) ((i >> 16) & 0xFF), (byte) ((i >> 8) & 0xFF),
				(byte) (i & 0xFF) };
	}

	public static int b2i(byte[] b) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i] & 0x000000FF) << shift;
		}
		return value;
	}

	public void close() {
		END = true;
		if (listenThread != null) {
			Util.log("Server is Shuttind down....");
			listenThread.interrupt();
			listenThread = null;
		}
	}

	public void print(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			System.out.print(b[i] + " ");
		}
		System.out.println();
	}

	public class QingParams {

		public int filelength;
		public String name;
		public int namelength;
		public int total;
		public int width;
		public int height;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			if (this == null)
				return "QingParams is null.";
			return "filename = " + name + " , filelength = " + filelength;
		}

	}

	public QingParams ByteToParams(byte[] b, int len) {
		
	//	Util.printBinaryByteArray(b,len);
		
		QingParams imageParams = new QingParams();
		imageParams.total = b2i(b);
		
		byte temp[] = new byte[4];
		System.arraycopy(b, 4, temp, 0, 4);
		imageParams.filelength = b2i(temp);
		
		temp = new byte[4];
		System.arraycopy(b, 8, temp, 0, 4);
		imageParams.width = b2i(temp);		

		temp = new byte[4];
		System.arraycopy(b, 12, temp, 0, 4);
		imageParams.height = b2i(temp);
				
		temp = new byte[4];
		System.arraycopy(b, 16, temp, 0, 4);
		imageParams.namelength = b2i(temp);
		
		temp = new byte[imageParams.namelength];
		System.arraycopy(b, 20, temp, 0, imageParams.namelength);
		imageParams.name = new String(temp);
		
		Util.log("QingParams.total is " + imageParams.total);
		Util.log("QingParams.filelength is " + imageParams.filelength);
		Util.log("QingParams.width is " + imageParams.width);
		Util.log("QingParams.height is " + imageParams.height);
		Util.log("QingParams.name is " + imageParams.name);

		return imageParams;
	}

}
