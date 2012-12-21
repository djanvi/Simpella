package core;

class PongMessage extends MessageFormat
{
	private static final long serialVersionUID = 1L;
	byte[] port=new byte[2];//0-1st
    byte[] ipAddress=new byte[4];//2-5th
    int numberOfFilesShared;//6-9th
    float numberOfKBShared;//10-13th
    
}