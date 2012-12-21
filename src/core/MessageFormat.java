package core;

import java.io.Serializable;
import java.util.HashMap;

class MessageFormat implements Serializable
{
   
	private static final long serialVersionUID = 1L;
	byte[] MessageID=new byte[16]; //0-15th
    byte MessageType;//16th
    byte MessageTTL;//17th
    byte MessageHops;//18th
    byte[] MessagePayloadLength;//=new byte[4];//19-22nd
    //byte[] MessagePayload;//
   
    

    public static String retMsgType(byte code){
    	
    	HashMap<Integer,String> typeSet = new HashMap<Integer, String>();
    	typeSet.put(0,"Ping");
    	typeSet.put(1,"Pong");
    	typeSet.put(-128,"Query");
    	typeSet.put(-127,"QueryHit");
    	
    	return typeSet.get(code);    	
    }
}