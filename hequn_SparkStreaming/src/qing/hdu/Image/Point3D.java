package qing.hdu.Image;

class Point3D{
	
public float a;
public float b;
public float c;
public float x;
public float y;
public float z;
public int rr;
public int gg;
public int bb;
			
/*	Point3D(float a,float b,float c,float x,float y,float z,int rr , int gg ,int bb)
			{
				this.a = a;
				this.b = b;
				this.c = c;
				this.x = x;
				this.y = y;
				this.z = z;
				this.rr = rr;
				this.gg = gg;
				this.bb = bb;
			}
	*/		
	@Override
	public String toString(){
			return "a = " + a + "b = " + b + "c = " + c + "x = " + x + "y = " + y + "z = " + z + "rr = " + rr + "gg = " + gg + "bb = " +bb ;	
		}

};
