

//将图片在内存中压缩成JPEG文件格式，发送到java端

UINT CDragonFlyCam::ThreadSendImage( LPVOID lpParams )
{
	ImageParams param = *((ImageParams*) lpParams);
	SOCKET sockClient = socket (AF_INET, SOCK_STREAM, 0);
	SOCKADDR_IN addrSrv;
	addrSrv.sin_addr.S_un.S_addr = inet_addr("192.168.178.141");
	addrSrv.sin_family = AF_INET;
	addrSrv.sin_port = htons(9999); 
	connect(sockClient,(SOCKADDR *)&addrSrv,sizeof(SOCKADDR));

	IplImage* dst;
	CvSize size;
	size.width = param.img->width;
	size.height = param.img->height;
	dst = cvCreateImageHeader(size, IPL_DEPTH_8U, 3);
	dst->imageData =  new(nothrow) char[size.width*size.height*3];
	cvCvtColor(param.img, dst, CV_BayerBG2BGR);


	char str[100];
	int filelength = dst->imageSize;

	//输出文件长度
	memset(str,0,100);
	sprintf(str,"%d",filelength);
	OutputMessage(str);

	int length = htonl((int)filelength);  
	char buf[1024];
	int len = 0;

	char ch[4];
	memcpy(ch,&filelength,4);
	QingParams p(filelength,param.name);
	p.SetHeight(param.img->height);
	p.SetWidth(param.img->width);
	len = htonl(p.GetStructSize());
	len = send(sockClient,(char *)&len,sizeof(p.GetStructSize()),0);


	len = p.GetStructSize();
	p.converToInternetType();
	//先发送一个int 的变量，int的值是文件大小
	len = send(sockClient,(char *)&p,len,0);
	//len = send(sockClient,ch,4,0);

	//输出文件名
	OutputMessage(param.name);

	//输出文件名长度
	memset(str,0,100);
	sprintf(str,"%s%d","filename length is ",len);
	OutputMessage(str);

	char *index = dst->imageData;
	int total = 0;
	char recvBuf[100];
	//	total = send(sockClient,index,filelength,0);
	char  a[255] = "start sending...";
	strcat(a,param.name );
	//OutputMessage(a);
	TRACE("%s%s\n",param.name,", start sending...") ;
	for(int i=0;i<filelength;i+=1024)
	{
		//	memcpy(buf,index+i,1024);
		len = send(sockClient,index+i,1024,0);
		if(len < 1024)
		{
			memset(str,0,100);
			sprintf(str,"%s%d","send len < 1024 ,len is  ",len);
			OutputMessage(str);
			if(len == -1)
				break;
		}
		total = total + len;
	}	

	TRACE("%s%s\n",param.name,", over sending...") ;
	char  b[255] = "over sending...";
	strcat(b,param.name );
	//OutputMessage(b);
	//cvReleaseImage(&(param->img));
	if(dst->imageData)
		delete [] dst->imageData;
	cvReleaseImage(&(param.img));
	cvReleaseImage(&dst);
	//delete param;
	closesocket(sockClient);
	memset(str,0,100);
	sprintf(str,"%s%d","file send over,send bytes: " ,total);
	OutputMessage(str);
	return 1;
}
