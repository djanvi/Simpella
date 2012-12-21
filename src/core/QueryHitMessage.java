package core;

import java.util.ArrayList;

class QueryHitMessage extends MessageFormat
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	byte  numberOfHits;//=new byte[1];    //0th
   // byte[]  port=new byte[2];            //1-2nd
  //  byte[]  ipAddress=new byte[4];    //3-6th
	String ipAddress;
	String port;
    byte[]  speed=new byte[4];        //7-10th
    byte[] serventID=new byte[16];
    ArrayList<ArrayList<String>> queryResultSet = new ArrayList<ArrayList<String>>();
    
    
    
}