#include <Winsock2.h>
#include <stdio.h>
#include <iostream>
#include <string>
using namespace std;

class QingParams  
{
public:
	QingParams()
	{
		width = 1024;
		height = 768;
		filelength = width * height *3;
		char *filename = "default.image";
		namelength = strlen(filename);
		strcpy(name,filename);
		total = (namelength + 24);
	}

	QingParams(int length,char * filename)
	{
		width = 1024;
		height = 768;
		filelength = length;
		namelength = strlen(filename);
		strcpy(name,filename);
		total = (namelength + 24);
	}

	~QingParams()
	{
		//delete[] name;
		cout<<"desconstructed called.";
	}
	
	void SetWidth(int w)
	{
		width = w;
	}

	void SetHeight(int h)
	{
		height = h;
	}

	int GetStructSize()
	{
		return total;
	}

	void print()
	{
		cout<<"total is "<<total<<endl;
		cout<<"filelength is "<<filelength<<endl;
		cout<<"width is "<<width<<endl;
		cout<<"height is "<<height<<endl;
		cout<<"name is "<<name<<endl;
		cout<<"namelength is "<<namelength<<endl;
	}

	void converToInternetType()
	{
		total = htonl(total);
		filelength = htonl(filelength);
		namelength = htonl(namelength);
		width = htonl(width);
		height = htonl(height);
	}

	void converToHostType()
	{	
		total = ntohl(total);
		filelength = ntohl(filelength);
		namelength = ntohl(namelength);
		width = ntohl(width);
		height = ntohl(height);
	}
private:
	int total;
	int filelength;
	int width;
	int height;
	int namelength;
	char name[255];
};