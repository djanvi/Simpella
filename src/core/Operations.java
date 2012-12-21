package core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

@SuppressWarnings("unused")
public class Operations 
{
	/*
	public static void saveInfo(Socket incomingSocket, String type)
	{GlobalVar gv= new GlobalVar();
		String cip=incomingSocket.getInetAddress().toString();
		String chname=incomingSocket.getInetAddress().getCanonicalHostName().toString();
		String localPort= Integer.toString(incomingSocket.getLocalPort());
		String remotePort=Integer.toString(incomingSocket.getPort());


		List<String> tempList = new ArrayList<String>();		

		tempList.add(0, ""+GlobalVar.count);
		tempList.add(1, cip);
		tempList.add(2, chname);
		tempList.add(3, localPort);
		tempList.add(4, remotePort);

		if(type.contentEquals("out"))
		{
			GlobalVar.outgoingConnections.add(gv.outgoingConnID , tempList);
			gv.outgoingConnID++;
		}
		else
		{	GlobalVar.incomingConnections.add(gv.incomingConnID, tempList);
			gv.incomingConnID++;
		}

		GlobalVar.count++;	
		//count = alternateDS.size();


	}
	 */
	public static  void showOutgoing()
	{		
		System.out.println("ConnectionID |  IPAddress        | HostName         |LocalPort  |  RemotePort |");
		System.out.println("-------------------------------------------------------------------------------");
		//System.out.println(alternateDS);
		//alternateDS.get(0).
		for(int connID : GlobalVar.outConn.keySet())
		{
			System.out.print(connID+"		|"+GlobalVar.outConn.get(connID).ipAddress+"     |"+
					GlobalVar.outConn.get(connID).hostname+"     |"+GlobalVar.outConn.get(connID).localPort+"     |"+
					GlobalVar.outConn.get(connID).remotePort);			
			System.out.println();
		}
	}

	public static void showIncoming()
	{		
		System.out.println("ConnectionID |  IPAddress        | HostName         |LocalPort  |  RemotePort |");
		System.out.println("-------------------------------------------------------------------------------");
		//System.out.println(alternateDS);
		//alternateDS.get(0).
		for(int connID : GlobalVar.inConn.keySet())
		{
			System.out.print(connID+"		|"+GlobalVar.inConn.get(connID).ipAddress+"     |"+
					GlobalVar.inConn.get(connID).hostname+"     |"+GlobalVar.inConn.get(connID).localPort+"     |"+
					GlobalVar.inConn.get(connID).remotePort);			
			System.out.println();
		}
	}

	public static void startHandshake(String hostname, int connID) throws NumberFormatException, UnknownHostException, IOException, ClassNotFoundException
	{


		String[] serventSplit=hostname.split(":");
		String destIP=serventSplit[0];			//to do for host name
		String destPort=serventSplit[1];

		Socket outgoingSocket=new Socket(InetAddress.getByName(destIP),Integer.parseInt(destPort));
		ConnectionFormat entry = new ConnectionFormat();

		entry.hostname = InetAddress.getByName(destIP).getCanonicalHostName().toString();
		entry.ipAddress=InetAddress.getByName(destIP);				
		entry.localPort= outgoingSocket.getLocalPort();
		entry.remotePort=outgoingSocket.getPort();
		entry.outStream = new ObjectOutputStream(outgoingSocket.getOutputStream());
		entry.inStream = new ObjectInputStream(outgoingSocket.getInputStream());

		GlobalVar.outConn.put(connID, entry);
		/*	ObjectOutputStream out = new ObjectOutputStream(gv.outgoingSocket[gv.outgoingConnID].getOutputStream());
		ObjectInputStream in = new ObjectInputStream(gv.outgoingSocket[gv.outgoingConnID].getInputStream());
		PrintWriter out = new PrintWriter(GlobalVar.outgoingSocket[GlobalVar.connID].getOutputStream(), true);*/

		System.out.println("Sending "+GlobalVar.handshakeMsg1+" to server");

		WrapThis start = new WrapThis();
		start.element = GlobalVar.handshakeMsg1;
		GlobalVar.outConn.get(connID).outStream.writeObject(start);
		GlobalVar.outConn.get(connID).noOfPacketsSent++;
		
		WrapThis startReply = (WrapThis) GlobalVar.outConn.get(connID).inStream.readObject();
		GlobalVar.outConn.get(connID).noOfPacketsReceived++;
		
		String fromServer= startReply.element;

		System.out.println("Received "+fromServer+" from server");
		if(fromServer.contentEquals(GlobalVar.handshakeMsg2))
		{
			System.out.println("Sending "+GlobalVar.handshakeMsg2+" to server");
			WrapThis msg2 = new WrapThis();
			msg2.element = GlobalVar.handshakeMsg2;
			GlobalVar.outConn.get(connID).outStream.writeObject(msg2);
			GlobalVar.outConn.get(connID).noOfPacketsSent++;
			

			WrapThis end = new WrapThis();
			end.element = GlobalVar.closeConnection;
			GlobalVar.outConn.get(connID).outStream.writeObject(end);
			GlobalVar.outConn.get(connID).noOfPacketsSent++;
			GlobalVar.outConn.get(connID).outStream.flush();
			System.out.println("Handshake finished");

			/* A separate thread for further processing of this connection*/			
			ClientThread clientThread =new ClientThread(connID);
			//ClientThread clientT =new ClientThread();
			clientThread.start();
			//System.out.println("Client Thread with ID:"+ connID+" has been started");

		}
		else
		{
			System.out.println("Handshake failed.");
		}

	}

	public static boolean initiatePing() throws IOException
	{
		String p="";
		MessageFormat pingMsg= new MessageFormat();
		pingMsg.MessageID=createGUID();
		pingMsg.MessageType=(byte)0x00;
		pingMsg.MessageTTL=GlobalVar.TTL;
		pingMsg.MessageHops=GlobalVar.Hop;
		pingMsg.MessagePayloadLength=new byte[]{0,0,0,0};

		//convert msg id (bytes) to string
		for(int i=0;i<15;i++)
			p=p+pingMsg.MessageID[i];  
		//put an entry in table mysentPings
		GlobalVar.mysentPings.put(p,pingMsg);


		// send to corresponding Clients
		for (int connID : GlobalVar.inConn.keySet())
			{GlobalVar.inConn.get(connID).outStream.writeObject(pingMsg);
			GlobalVar.inConn.get(connID).noOfPacketsSent++;
			GlobalVar.inConn.get(connID).noOfBytesSent=GlobalVar.inConn.get(connID).noOfBytesSent+23;

			}
		// send to corresponding Workers
		for (int connID : GlobalVar.outConn.keySet())
		{	GlobalVar.outConn.get(connID).outStream.writeObject(pingMsg);
			GlobalVar.outConn.get(connID).noOfPacketsSent++;
			GlobalVar.outConn.get(connID).noOfBytesSent=GlobalVar.outConn.get(connID).noOfBytesSent+23;

		}

		return true;
	}
	public static boolean forwardPing(MessageFormat forwardMsg,int avoid) throws IOException
	{
		int newTTL =forwardMsg.MessageTTL-1;
		int newHops=forwardMsg.MessageHops+1;
		forwardMsg.MessageTTL=(byte) ( newTTL);
		forwardMsg.MessageHops=(byte) ( newHops);

		// send to corresponding Clients
		for (int connID : GlobalVar.inConn.keySet())
		{
			if(connID!=avoid)
			{	GlobalVar.inConn.get(connID).outStream.writeObject(forwardMsg);
				GlobalVar.inConn.get(connID).noOfPacketsSent++;
				GlobalVar.inConn.get(connID).noOfBytesSent=GlobalVar.inConn.get(connID).noOfBytesSent+23;

			}
		}
		// send to corresponding Workers
		for (int connID : GlobalVar.outConn.keySet())
		{
			if(connID!=avoid)
				GlobalVar.outConn.get(connID).outStream.writeObject(forwardMsg);
				GlobalVar.outConn.get(connID).noOfPacketsSent++;
				GlobalVar.outConn.get(connID).noOfBytesSent=GlobalVar.outConn.get(connID).noOfBytesSent+23;

		}
		return true;
	}

	public static byte[] createGUID(){
		byte[] GUID=new byte[16];
		GUID[7]=(byte)0xff;
		GUID[15]=(byte)0x00;
		int min =-127;
		int max=128;
		Random gen = new Random();
		for(int i=0;i<15;i++)
		{
			if(i!=7)
			{
				int random=gen.nextInt(max-min+1)+min;
				GUID[i]=(byte)random;
			}
		}

		return GUID;
	}

	public static boolean sendPong(int connID,byte[] messageID,String type) throws IOException
	{
		GlobalVar gv= new GlobalVar();
		//calculate no of kb share
		float nokb=0;
		for(ArrayList<String> entry: GlobalVar.filesInfo)
		{
			nokb=nokb+Float.parseFloat(entry.get(1));
		}
		
		int nof=GlobalVar.filesInfo.size();
		PongMessage pongMsg= new PongMessage();
		pongMsg.MessageID=messageID;
		pongMsg.MessageType=(byte)0x01;
		pongMsg.MessageTTL=GlobalVar.TTL;
		pongMsg.MessageHops=GlobalVar.Hop;
		pongMsg.MessagePayloadLength=ByteBuffer.allocate(4).putInt(14).array();

		pongMsg.port=ByteBuffer.allocate(2).putShort(GlobalVar.myconnectionPort).array();
		pongMsg.ipAddress=gv.myIPAddress.getBytes();
		//pongMsg.numberOfFilesShared=ByteBuffer.allocate(4).putInt(nof).array();
		//pongMsg.numberOfKBShared=ByteBuffer.allocate(4).putInt(nokb).array();
		pongMsg.numberOfFilesShared=nof;
		pongMsg.numberOfKBShared=nokb;

		
		// send to corresponding this client
		if(type.contentEquals("incoming"))
		{	GlobalVar.inConn.get(connID).outStream.writeObject(pongMsg);
		GlobalVar.inConn.get(connID).noOfPacketsSent++;
		GlobalVar.inConn.get(connID).noOfBytesSent=GlobalVar.inConn.get(connID).noOfBytesSent+37;

		}
		else
		{	GlobalVar.outConn.get(connID).outStream.writeObject(pongMsg);
			GlobalVar.outConn.get(connID).noOfPacketsSent++;
			GlobalVar.outConn.get(connID).noOfBytesSent=GlobalVar.outConn.get(connID).noOfBytesSent+37;

		}	
		return true;

	}

	public static boolean forwardPong(int connID,MessageFormat forwardPong, String type) throws IOException
	{
		int newTTL =forwardPong.MessageTTL-1;
		int newHops=forwardPong.MessageHops+1;
		forwardPong.MessageTTL=(byte) ( newTTL);
		forwardPong.MessageHops=(byte) ( newHops);

		if(type.contentEquals("incoming"))
		{GlobalVar.inConn.get(connID).outStream.writeObject(forwardPong);
		GlobalVar.inConn.get(connID).noOfPacketsSent++;
		GlobalVar.inConn.get(connID).noOfBytesSent=GlobalVar.inConn.get(connID).noOfBytesSent+37;
}
		else
			{GlobalVar.outConn.get(connID).outStream.writeObject(forwardPong);
			GlobalVar.outConn.get(connID).noOfPacketsSent++;
			GlobalVar.outConn.get(connID).noOfBytesSent=GlobalVar.outConn.get(connID).noOfBytesSent+37;
}


		return true;
	}

	public static void showPongInfo()
	{	// to be fixed.
		String ipAddress="";
		System.out.println("Port |  IP Addres |    Number of files shared | Number of Kilobytes shared");
		for(PongInformation ponginfo:GlobalVar.pongInfo)
		{
			//byte[] to short
			ByteBuffer bb = ByteBuffer.wrap(ponginfo.portSender);
			bb.order(ByteOrder.BIG_ENDIAN);
			short port = bb.getShort();
			//byte to string
			for(int i=0;i<4;i++)
				ipAddress=ipAddress+ponginfo.ipSender[i];

			//ByteBuffer bb2 = ByteBuffer.wrap(ponginfo.numberOfFilesShared);
			//bb2.order(ByteOrder.BIG_ENDIAN);
			//int nof=bb2.getInt();
			int nof= ponginfo.numberOfFilesShared;
			
			//ByteBuffer bb3 = ByteBuffer.wrap(ponginfo.numberOfKBShared);
			//bb3.order(ByteOrder.BIG_ENDIAN);
			//double nokb=bb3.getInt();
			float nokb=ponginfo.numberOfKBShared;
			
			System.out.print(port+ "  "+ ipAddress+"  "+nof+"  "+nokb );

		}

	}

	public static void shareDirectory(String directory )
	{ 
		File newSharedDirectory= null;

		if(!directory.contains("/"))
		{ 
			//user is referring to the relative directory
			newSharedDirectory = new File(GlobalVar.sharedDirectory+"/"+directory); 
		}
		else if(directory.contains("/")==true)
		{
			//user is referring to some new directory
			newSharedDirectory = new File(directory); 
		}

		if(newSharedDirectory.isDirectory()==true)
		{
			GlobalVar.sharedDirectory=newSharedDirectory.getPath();
			System.out.println("Sharing "+GlobalVar.sharedDirectory);
			Operations.saveFilesInfo(GlobalVar.sharedDirectory);
		}
		else
		{
			//directory does not exist
			System.out.println(" Directory does not exist. ");
		}

	}

	public static void scan() throws IOException
	{
		System.out.println("Scanning all the files from " +GlobalVar.sharedDirectory );

		//File f = new File(GlobalVar.sharedDirectory);
		File currentDirectory = new File(GlobalVar.sharedDirectory); 
		File[] filesInSharedDir=currentDirectory.listFiles();

		int nofiles =0;
		float fileSize=0;
		for (File f : filesInSharedDir)
		{		if (f.isFile())
			nofiles=nofiles+1;

		FileInputStream fin=new FileInputStream(f);
		FileChannel fc_fin = fin.getChannel();
		fileSize=fileSize+(float)(fc_fin.size()/1024.0);

		}
		System.out.println("Scanned "+nofiles+" files and "+fileSize+" bytes");
	}

	public static void saveFilesInfo(String sharedDirectory)
	{
		File currentDirectory = new File(GlobalVar.sharedDirectory); 
		File[] filesInSharedDir=currentDirectory.listFiles();

		for (File f : filesInSharedDir)
		{
			if (f.isDirectory() == true) {
				saveFilesInfo(f.toString());
			} else {
				try {

					String str_file= f.toString();	
					int index=str_file.lastIndexOf('/');
					String fileName= str_file.substring((index+1),str_file.length());
					ArrayList<String> tempList = new ArrayList<String>();

					try {
						FileInputStream fin=new FileInputStream(f);
						FileChannel fc_fin = fin.getChannel();
						float fileSize=(float)(fc_fin.size()/1024.0);

						tempList.add(0,fileName);
						tempList.add(1,String.valueOf(fileSize));
						GlobalVar.filesInfo.add(tempList);
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();}

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(e);
				}
			}
		}
	}

	public static void showFilesInfo()
	{
		System.out.println("FileIndex |" + "File name |" + "Size");
		for(ArrayList<String> file: GlobalVar.filesInfo)
		{
			System.out.println(file.get(0)+" | "+file.get(1));
		}
	}

	public static boolean initiateQuery(String query) throws IOException
	{
		GlobalVar.findResults = new HashMap<Integer, ArrayList<String>>();
		GlobalVar.noOfResponses=0;
		GlobalVar.resultsCounter=1;
		if(query.getBytes().length>254)
		{
			System.out.println("Size of query is too large. enter smaller queries");
			return false;
		}
		else
		{
			String p="";

			QueryMessage queryMsg= new QueryMessage();
			queryMsg.queryMinimumSpeed=new byte[]{0,0};
			//queryMsg.querySearchString= query.getBytes("UTF-16BE");
			queryMsg.querySearchString= query;
			queryMsg.MessageID=createGUID();
			queryMsg.MessageType=(byte)0x80;
			queryMsg.MessageTTL=GlobalVar.TTL;
			queryMsg.MessageHops=GlobalVar.Hop;
			queryMsg.MessagePayloadLength=ByteBuffer.allocate(4).putInt((queryMsg.queryMinimumSpeed.length+queryMsg.querySearchString.length())).array();

			//convert msg id (bytes) to string
			for(int i=0;i<15;i++)
				p=p+queryMsg.MessageID[i];

			//put an entry in table mysentQueries
			GlobalVar.mysentQueries.put(p,query);

			// send to corresponding Clients
			for (int connID : GlobalVar.inConn.keySet())
			{	GlobalVar.inConn.get(connID).outStream.writeObject(queryMsg);
			GlobalVar.inConn.get(connID).noOfPacketsSent++;
			GlobalVar.inConn.get(connID).noOfBytesSent=GlobalVar.inConn.get(connID).noOfBytesSent+23;
			
			}
			// send to corresponding Workers
			for (int connID : GlobalVar.outConn.keySet())
			{	GlobalVar.outConn.get(connID).outStream.writeObject(queryMsg);
			GlobalVar.outConn.get(connID).noOfPacketsSent++;
			GlobalVar.outConn.get(connID).noOfBytesSent=GlobalVar.outConn.get(connID).noOfBytesSent+23;

			}
			//call showfindresults() somewhere
			
			System.out.println("Searching Simpella network for : " +query +"......");
			
			new java.util.Timer().schedule( 
					new java.util.TimerTask() {
						@Override
						public void run() {
							showFindResults();
						}
					}, 
					6000 
					);



			return true;
		}

	}
	public static boolean forwardQuery(QueryMessage forwardMsg,int avoid) throws IOException
	{
		int newTTL =forwardMsg.MessageTTL-1;
		int newHops=forwardMsg.MessageHops+1;
		forwardMsg.MessageTTL=(byte) ( newTTL);
		forwardMsg.MessageHops=(byte) ( newHops);

		// send to corresponding Clients
		for (int connID : GlobalVar.inConn.keySet())
		{
			if(connID!=avoid)
			{	GlobalVar.inConn.get(connID).outStream.writeObject(forwardMsg);
			GlobalVar.inConn.get(connID).noOfPacketsSent++;
			GlobalVar.inConn.get(connID).noOfBytesSent=GlobalVar.inConn.get(connID).noOfBytesSent+23;

			}
		}
		// send to corresponding Workers
		for (int connID : GlobalVar.outConn.keySet())
		{
			if(connID!=avoid)
			{	GlobalVar.outConn.get(connID).outStream.writeObject(forwardMsg);
			GlobalVar.outConn.get(connID).noOfPacketsSent++;
			GlobalVar.outConn.get(connID).noOfBytesSent=GlobalVar.outConn.get(connID).noOfBytesSent+23;

			}
		}
		return true;
	}

	//sendQueryHit
	public static boolean sendQueryHit(int connID,byte[] messageID,ArrayList<ArrayList<String>> queryResultSet,String type) throws IOException
	{
		GlobalVar gv= new GlobalVar();

		short nof=900;
		short nokb=300;
		QueryHitMessage queryHitMessage= new QueryHitMessage();
		queryHitMessage.MessageID=messageID;
		queryHitMessage.MessageType=(byte)0x81;
		queryHitMessage.MessageTTL=GlobalVar.TTL;
		queryHitMessage.MessageHops=GlobalVar.Hop;
		//queryHitMessage.MessagePayloadLength=ByteBuffer.allocate(4).putInt(14).array();
		queryHitMessage.numberOfHits=(byte)queryResultSet.size();
		//queryHitMessage.port=ByteBuffer.allocate(2).putShort(GlobalVar.mydownloadPort).array();
		//queryHitMessage.ipAddress=gv.myIPAddress.getBytes();
		queryHitMessage.port=Short.toString(GlobalVar.mydownloadPort);
		queryHitMessage.ipAddress=gv.myIPAddress;
		queryHitMessage.speed=ByteBuffer.allocate(4).putInt(14).array();
		queryHitMessage.queryResultSet=queryResultSet;
		//queryHitMessage.serventID=

		// send to corresponding this client
		if(type.contentEquals("incoming"))
		{	GlobalVar.inConn.get(connID).outStream.writeObject(queryHitMessage);
		GlobalVar.inConn.get(connID).noOfPacketsSent++;
		GlobalVar.inConn.get(connID).noOfBytesSent=GlobalVar.inConn.get(connID).noOfBytesSent+23;

		}
		else
			{GlobalVar.outConn.get(connID).outStream.writeObject(queryHitMessage);
			GlobalVar.outConn.get(connID).noOfPacketsSent++;
			GlobalVar.outConn.get(connID).noOfBytesSent=GlobalVar.outConn.get(connID).noOfBytesSent+23;

			}
		return true;

	}


	public static boolean forwardQueryHit(int connID,MessageFormat forwardQueryHit, String type) throws IOException
	{
		int newTTL =forwardQueryHit.MessageTTL-1;
		int newHops=forwardQueryHit.MessageHops+1;
		forwardQueryHit.MessageTTL=(byte) ( newTTL);
		forwardQueryHit.MessageHops=(byte) ( newHops);

		if(type.contentEquals("incoming"))
		{	GlobalVar.inConn.get(connID).outStream.writeObject(forwardQueryHit);
		GlobalVar.inConn.get(connID).noOfPacketsSent++;
		GlobalVar.inConn.get(connID).noOfBytesSent=GlobalVar.inConn.get(connID).noOfBytesSent+23;

		}else
		{	GlobalVar.outConn.get(connID).outStream.writeObject(forwardQueryHit);
		GlobalVar.outConn.get(connID).noOfPacketsSent++;
		GlobalVar.outConn.get(connID).noOfBytesSent=GlobalVar.outConn.get(connID).noOfBytesSent+23;

		}


		return true;
	}

	public static void showFindResults()
	{
		System.out.println(GlobalVar.noOfResponses+" responses received");
		System.out.println("----------------------------------");
		System.out.println("File index        | File Name         | File Size       | IP Address         | Port     ");
		
		for(int key: GlobalVar.findResults.keySet())
		{
			ArrayList<String> eachHit=GlobalVar.findResults.get(key);
			String result=key+"		";
			for (String s: eachHit)
				result=result+s+"		";
			System.out.println(result);


		}


	}
	
	public static void startDownload(int fileIndex) throws IOException, ClassNotFoundException
	{
		ArrayList<String> fileDownload=GlobalVar.findResults.get(fileIndex);
		String fileName=fileDownload.get(0);
		String ipAddress=fileDownload.get(2);
		String downloadPort=fileDownload.get(3);
		String fileSize=fileDownload.get(1);
		System.out.println("file to download:"+fileName);
		
		//connect to download server-send http msg1
		
		Socket clientDownload = new Socket(InetAddress.getByName(ipAddress),Integer.parseInt(downloadPort));
		
		ObjectOutputStream out = new ObjectOutputStream(clientDownload.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(clientDownload.getInputStream());
		// write msg1;
		
		String getMessage1=GlobalVar.Msg1+fileIndex+"/"+fileName+" HTTP/1.1\r\nUser - Agent: Simpella \r\nHost:"+ipAddress+":"+downloadPort+"\r\nConnection : Keep -Alive \r\nRange: bytes = 0 -\r\n\r\n";
				
			
		
		WrapThis start = new WrapThis();					
		start.element = getMessage1;
		out.writeObject(start);
		System.out.println("Sent msg1 to server");
																		
		WrapThis startReply = (WrapThis) in.readObject();
		String replyFromServer= startReply.element;
																		
		//read reply from server
		
		StringTokenizer st=new StringTokenizer(replyFromServer);
		st.nextToken("\n");
		st.nextToken("\n");
		st.nextToken("\n");
		st.nextToken(":");
		String contentLength=st.nextToken("\r").trim();
		//System.out.println("content length "+contentLength);
		
		if(replyFromServer.contains("200"))
		{
			DownloadStatsofFile dsf=new DownloadStatsofFile();
			dsf.downloadedAmount=0;
			dsf.fileName=fileName;
			dsf.downloadingIndex=fileIndex;
			dsf.totalFileSize=Float.parseFloat(fileSize);
			dsf.senderIP=ipAddress;
			dsf.portNumber=downloadPort;
			GlobalVar.downloadStats.put(dsf.fileName, dsf);
			
			//BufferedInputStream brin=new BufferedInputStream(in);
			//BufferedOutputStream bufOutStream_toFile=null;
			//FileOutputStream fOutStream=null;
		
			Operations.receiveFile(fileName,new File(GlobalVar.sharedDirectory+"/"+fileName),clientDownload);
				
			}
		else if(replyFromServer.contains("503"))
		{
			System.out.println("Downloading error from server...");
			return ;
		}
		
		
		
	}
	
	
	public static void sendFile(File file,Socket acceptSocket) throws IOException, ClassNotFoundException {
		
		byte[] buffer = new byte[1024];
		//OutputStream os =  acceptSocket.getOutputStream();
		
		FileInputStream 	fileInputStream = new FileInputStream(file);
		BufferedInputStream brin=new BufferedInputStream(fileInputStream);
		
		ObjectOutputStream obj_out=new ObjectOutputStream(acceptSocket.getOutputStream());
		
		
	/*	BufferedOutputStream out = new BufferedOutputStream(acceptSocket.getOutputStream(), 1024);
		FileInputStream in = new FileInputStream(file);
		*/
		
		int i = 0;
		i = brin.read(buffer);
		
		int bytecount=0;
		while ( i!= -1) 
		{
			bytecount = bytecount + 1024;
			obj_out.write(buffer);
			obj_out.flush();
		}

		System.out.println("Bytes Sent :" + bytecount);


	//	out.close();
	//	in.close();

	}
	
	public static void receiveFile(String fileName,File file,Socket clientDownload) throws IOException, ClassNotFoundException {
		FileOutputStream inFile=null; 
		BufferedInputStream in2=null;
		BufferedOutputStream bufOutStream=null;
		try{  
		   inFile= new FileOutputStream(file);
		   in2 = new BufferedInputStream(clientDownload.getInputStream(), 1024);
		   bufOutStream= new BufferedOutputStream(inFile);
	    
	   }
	   catch(FileNotFoundException e){
		   System.out.println("Choose some shared directory by 'share' command\nSimpella >");
	   }
	   
	   byte[] b = new byte[1024];
	   int size = 0;
	   int bytcount = 1024;
	   size = in2.read(b);
		System.out.println("Downloading:"+fileName);
	DownloadStatsofFile dsf= new DownloadStatsofFile();
	   while (size!= -1) {
	      //bytcount = bytcount + 1024;
	    	dsf=GlobalVar.downloadStats.get(fileName);
	    	bufOutStream.write(b);
	    	size = in2.read(b);	
	    	bufOutStream.flush();
			

			dsf.downloadedAmount=dsf.downloadedAmount+b.length;
			if(b.length<1024){System.out.println("buffer.length "+b.length);}
			GlobalVar.downloadStats.put(fileName, dsf);
			
			
	    }
	    
	   
	  }

	public static void showDownloadStats()
	{
		//GlobalVar.downloadStats.get(key);
		
	}
	
	
	public static void showlistResults()
	{
		
		System.out.println("----------------------------------");
		System.out.println("File index        | File Name         | File Size       | IP Address         | Port     ");
		
		for(int key: GlobalVar.listResults.keySet())
		{
			ArrayList<String> eachHit=GlobalVar.listResults.get(key);
			String result=key+"		";
			for (String s: eachHit)
				result=result+s+"		";
			System.out.println(result);


		}
	}
	
	public static void clearListResults(String fileNo)
	{
		if(fileNo.contentEquals("all"))
		{
			GlobalVar.listResults.clear();
			Operations.showlistResults();
			
			
		}
		else
		{
			int index= Integer.parseInt(fileNo);
			GlobalVar.listResults.remove(index);
			Operations.showlistResults();
			
		}
	}
	
	public static void handleInfoCommand(String command)
	{
		String commandsplit[];
		commandsplit=command.split(" ");
		
		if(commandsplit.length==1)
		{
			System.out.println("Invalid arguments to the 'info' command.");
			return;
		}
		
		String arguement =commandsplit[1];
		if(arguement.equals("h"))
		{
			System.out.println("HOST STATS:");
			System.out.println("----------");
			
			
			int numberOfHosts=0;
			int numberOfFiles=0;
			float totalSize=0;
		
			for(PongInformation entry: GlobalVar.pongInfo )
			{
				numberOfHosts=numberOfHosts+1;
				numberOfFiles=numberOfFiles+entry.numberOfFilesShared;
				totalSize=totalSize+entry.numberOfKBShared;
				
			}
			
			System.out.println("Hosts: "+numberOfHosts+ " Files: "+ (float)numberOfFiles/1000.0 +"K   Size: "+totalSize+"K");
			
			return;
			
			
		}
		else if(arguement.equals("s"))
		{
			System.out.println("SHARE STATS:");
			System.out.println("-----------");
			System.out.println("Num Shared: "+GlobalVar.filesInfo.size());
			float totalSize=0;
			for (ArrayList<String> eachentry:GlobalVar.filesInfo)
			{
				totalSize=totalSize+Integer.parseInt(eachentry.get(1));
			}
			
			System.out.println("Size Shared: "+totalSize);
			
			
		}
		else if(arguement.equals("q"))
		{
			int numberOfQreceived=0;
			
			
			for(int key:GlobalVar.receivedQueries.keySet())
			{
				for(String eachentry:GlobalVar.receivedQueries.get(key))
				{
					numberOfQreceived=numberOfQreceived+1;
				}
			}
			
			System.out.println("QUERY STATS:");
			System.out.println("------------");
			System.out.println("Queries: "+numberOfQreceived+"   Responses Sent: "+GlobalVar.noOfResponses);
			
		}
		else if(arguement.equals("c"))
		{
			System.out.println("CONNECTION STATS");
			System.out.println("-----------------");
			int i=0;
			int counter=1;
			
			for(int key:GlobalVar.outConn.keySet())
			{
				ConnectionFormat object=GlobalVar.outConn.get(key);
				System.out.println(i+1+" "+object.ipAddress+":"+object.remotePort+"   "+"Packs: "+object.noOfPacketsSent+":"+object.noOfPacketsReceived+"  "+"Bytes: "+object.noOfBytesSent+":"+object.noOfBytesReceived);
						
			}
		
		}
		
		
		else if(arguement.equals("d"))
		{
			System.out.println("DOWNLOAD STATS");
			System.out.println("---------------");
			int i=0;
			
					
			
			for(String key:GlobalVar.downloadStats.keySet())
			{
				DownloadStatsofFile object=GlobalVar.downloadStats.get(key);
				float percent=(object.downloadedAmount/(object.totalFileSize*1024.0f))*100.0f;
				System.out.println(i+1+" "+object.senderIP+"  "+"\t"+percent+"%\t"+object.downloadedAmount/(1024.0f)+"K/"+object.totalFileSize+"K");
				System.out.println(object.fileName);
			}
		}

		else if(arguement.equals("n"))
		{
			System.out.println("NET STATS:");
			System.out.println("----------");
			
			int totalMessagesReceived=0;
			int totalMessagesSent=0;
			int totalBytesReceived=0;
			int totalBytesSent=0;
			
			
			
		}
		
		
		
		
		
		
		
		
		
	}
	


}




