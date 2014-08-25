package qing.hdu.Image
import org.apache.spark.streaming.dstream.NetworkReceiver
import org.apache.spark.storage.StorageLevel
import java.net.Socket
import java.io.BufferedReader
import java.io.InputStreamReader
import scala.collection.mutable.ArrayBuffer
import java.io.OutputStream
import java.io.DataOutputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat

class QimageSocketReceiver(host: String, port: Int) extends NetworkReceiver[(String, Qimage)] {
  protected lazy val blocksGenerator: BlockGenerator =
    new BlockGenerator(StorageLevel.MEMORY_ONLY_SER_2)

  protected def onStart() = {
    blocksGenerator.start()
    val socket = new Socket(host, port)
    println("socket is created....")

    val inputStream = socket.getInputStream()
    val min_len = 2048
    val buffer = new Array[Byte](min_len)
    val LENGTH_UINT = new Array[Byte](4)
    val FILE_BEGIN = new Array[Byte](8)

    while (true) {
      inputStream.read(FILE_BEGIN)
      if (FILE_BEGIN.sum == 0) { //起始标志
    //    println(getTime + "received 00000000..")

        if (inputStream.read(LENGTH_UINT) != -1) { //文件名字长度
          
          val filename_length = b2i(LENGTH_UINT)
        //  println(getTime + "onStart::filename_length() is " + filename_length + ", bytes[] = " + LENGTH_UINT(0) + " " + LENGTH_UINT(1) + " " + LENGTH_UINT(2) + " " + LENGTH_UINT(3))
          val filename = new Array[Byte](filename_length)
          inputStream.read(filename, 0, filename_length)

/*          if (filename.length > 0 && filename.length < 255) {
            print(getTime + "filename[] = ")
            for (i <- filename)
              print(i + " ")
            println
          }
  */        
          inputStream.read(LENGTH_UINT) //文件长度
          val length = b2i(LENGTH_UINT)
          println(getTime + "onStart::file_length() is " + length + ", bytes[] = " + LENGTH_UINT(0) + " " + LENGTH_UINT(1) + " " + LENGTH_UINT(2) + " " + LENGTH_UINT(3))

          
 /*         
          
         /***************************   这段代码有问题~ 2014-8-10 ************************************/
         
          var Current_length = 0
          var data = Array[Byte]()
          var size = 0
          while (Current_length < length) {
            if (length - Current_length >= min_len) {
              size = min_len
            } else {
              size = length - Current_length
            //  println(getTime + "last size = " + size) 
            }
            //  println("onStart::size is " + size)
            size = inputStream.read(buffer, 0, size)
            //data.write(buffer, 0, size)
            Current_length = Current_length + size
      //      if(size < min_len)
        //      println(getTime + " size < min_len is " + size)
            data = data ++ buffer.take(size)
          }
          
          /***************************   这段代码有问题~ 2014-8-10 见下面的代码 ************************************/
 */         
          
          
          /*
           * 这里是我尝试修改的，，，没有验证过 2014-8-10
           */
          var Current_length = 0
          var data = Array[Byte]()
          var size = 0
          while (Current_length < length) {
            size = inputStream.read(buffer, 0, min_len)
            Current_length = Current_length + size
            data = data ++ buffer.take(size)
          }
          
                 
          
          if (data.length != length) {
            println("received error data!!!, data.length = " + data.length + ", Current_length = " + Current_length)
         //   println
          } else {
            val image = new Qimage(data, filename)
            blocksGenerator += (image.getFrame(), image)
         //   println(getTime + "one blocksGenerator is over...")
            println
          }
        } //文件读取结束       
      } //起始表示结束            
    } //while循环 
  //  println(getTime + "TextSocketReceiver::onStart() is over...")
  }

  protected def onStop() {
    blocksGenerator.stop()
  }

  def b2i(byte: Array[Byte]): Int = {
    var value = 0;
    for (i <- 0 to 3) {
      val shift = (4 - 1 - i) * 8;
      value += (byte(i) & 0x000000FF) << shift;
    }
    value
  }

  def i2b(value: Int): Array[Byte] = {
    val byte = Array[Byte](
      ((value >> 24) & 0xFF).toByte,
      ((value >> 16) & 0xFF).toByte,
      ((value >> 8) & 0xFF).toByte,
      (value & 0xFF).toByte)
    byte
  }

  def getTime(): String = {
    val sDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss  ");
    val date = sDateFormat.format(new java.util.Date());
    date
  }
}