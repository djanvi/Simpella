package core;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class GlobalVar {
	/*
	static List<List<String>> outgoingConnections = new ArrayList<List<String>>();
	static List<List<String>> incomingConnections = new ArrayList<List<String>>();	
	*/
	GlobalVar() throws UnknownHostException, IOException  
	{
		Socket s= new Socket("www.google.com",80);
		this.myIPAddress=s.getLocalAddress().getHostAddress();
		s.close();
	}
	 
	static final int TTL=7;
	static final int Hop=0;
	// Routing Table Structure , recently received pings
	static Map<Integer, ArrayList<String>> rTable = new LinkedHashMap<Integer, ArrayList<String>>(160);	
	
	// Routing Table Structure , recently received queries
	static Map<Integer, ArrayList<String>> receivedQueries = new LinkedHashMap<Integer, ArrayList<String>>(160);
	
	
	
	// Main Data Structure used for saving state of connections in Simpella
	static HashMap<Integer, ConnectionFormat> outConn = new HashMap<Integer, ConnectionFormat>();
	static HashMap<Integer, ConnectionFormat> inConn = new HashMap<Integer, ConnectionFormat>();
	
	static HashMap<String,MessageFormat> mysentPings = new HashMap<String,MessageFormat>();
	static HashMap<String,String> mysentQueries = new HashMap<String,String>();
	static List<ArrayList<String>> filesInfo = new ArrayList<ArrayList<String>>();
	
	
	//Data structure for search results
	static HashMap<Integer,ArrayList<String>>	findResults; 
	static HashMap<Integer,ArrayList<String>>	listResults= new HashMap<Integer,ArrayList<String>>();
	static int resultsCounter;
	static int listresultsCounter=1;
	
	// Data structure for storing PONG info
	static List<PongInformation> pongInfo = new ArrayList<PongInformation>();
	static String sharedDirectory=new File("").getAbsolutePath();
	
	String myIPAddress;
	static short myconnectionPort;
	static short mydownloadPort;
	static int count = 1,same=0;
	int incomingConnID=0;
	int outgoingConnID=0;
	static String handshakeMsg1="SIMPELLA CONNECT/0.6\r\n";
	static String handshakeMsg2="SIMPELLA/0.6 200 OK\r\n";
	static String handshakeMsg3="SIMPELLA/0.6 503 Maximum nuber of connections reached.Sorry!\r\n";
	static String closeConnection="SIMPELLA/0.6 CLOSE";
	Socket[] outgoingSocket = new Socket[3];
	static int noOfResponses=0;

	
	//File Download
	static int  num = 1, numb=0;
	static String Msg1 = "GET /get/";
	static String Msg2 = "HTTP/1.1 200 OK\r\nServer : Simpella 0.6 \r\nContent-type: application/binary\r\nContent-length:";
	static String Msg3 = "HTTP/1.1 503 File not found.\r\n";
	
	static HashMap<String,DownloadStatsofFile> downloadStats = new HashMap<String,DownloadStatsofFile>();
	
	

}
