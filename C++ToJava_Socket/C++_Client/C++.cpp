

//��ͼƬ���ڴ���ѹ����JPEG�ļ���ʽ�����͵�java��

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

	//����ļ�����
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
	//�ȷ���һ��int �ı�����int��ֵ���ļ���С
	len = send(sockClient,(char *)&p,len,0);
	//len = send(sockClient,ch,4,0);

	//����ļ���
	OutputMessage(param.name);

	//����ļ�������
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
