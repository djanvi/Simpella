package core;

class QueryMessage extends MessageFormat
{
    /*Since Query messages are broadcast to many nodes, the total size of the message SHOULD not be
    larger than 256 bytes. Servents should drop Query messages larger that 256 bytes, and MUST drop
    Query messages with payload larger than 4 kB.*/
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	byte[] queryMinimumSpeed=new byte[2];//0-1st
    //byte[] querySearchString=new byte[231];//2-258th
    String querySearchString;
}
