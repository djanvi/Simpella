package core;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class ServerThread extends Thread
{
	int id;
	short tcpport;
	int remoteport;
	String cip;
	String chostname;

	ServerThread (short tcpport)
	{
		this.tcpport= tcpport;
	}
	public void run()
	{
		@SuppressWarnings("unused")
		int id=0;
		GlobalVar.myconnectionPort=this.tcpport;
		ServerSocket tcpSocket=null;		
		try
		{
			tcpSocket=new ServerSocket(tcpport);
		}
		catch(Exception e)
		{

			e.printStackTrace();
		}

		Random numGenerator= new Random();
		
		while(true)
		{
			try
			{	
				Socket incomingSocket=null;
				incomingSocket=tcpSocket.accept();
				id ++;

				//Handshake
				/*InputStreamReader inputStream = new InputStreamReader(incomingSocket.getInputStream());
				BufferedReader in = new BufferedReader(inputStream);

				PrintWriter out = new PrintWriter(incomingSocket.getOutputStream(),true);
				 
				//OutputStreamWriter out=new OutputStreamWriter(incomingSocket.getOutputStream()); 

				ObjectInputStream in = new ObjectInputStream(incomingSocket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(incomingSocket.getOutputStream());
				 */
				// Generate Random Number for connection IDs
				
				
				ConnectionFormat entry = new ConnectionFormat();
				entry.hostname = incomingSocket.getInetAddress().getCanonicalHostName().toString();
				entry.ipAddress=incomingSocket.getInetAddress();				
				entry.localPort= incomingSocket.getLocalPort();
				entry.remotePort=incomingSocket.getPort();
				entry.inStream = new ObjectInputStream(incomingSocket.getInputStream());
				entry.outStream = new ObjectOutputStream(incomingSocket.getOutputStream());
				
				int connID = numGenerator.nextInt();
				GlobalVar.inConn.put(connID, entry);
				//System.out.println("Entry made to incomingConnections: "+ );

				WrapThis obj;
				String incomingMsg;
				do{
					obj = (WrapThis) GlobalVar.inConn.get(connID).inStream.readObject();
					incomingMsg = obj.element;
					//System.out.println("Got "+incomingMsg+" from client");

					if(GlobalVar.inConn.size()>3)
					{//To DO-wrap this
						WrapThis sendThis = new WrapThis();
						sendThis.element = GlobalVar.handshakeMsg3;
						GlobalVar.inConn.get(connID).outStream.writeObject(sendThis);
						GlobalVar.inConn.remove(connID);
						
					}
					else if(incomingMsg.contentEquals(GlobalVar.handshakeMsg1))
					{
						WrapThis sendThis = new WrapThis();
						sendThis.element = GlobalVar.handshakeMsg2;
						GlobalVar.inConn.get(connID).outStream.writeObject(sendThis);
						GlobalVar.inConn.get(connID).outStream.flush();
						System.out.println("Sending "+GlobalVar.handshakeMsg2+" to client");
					}
					else if(incomingMsg.contentEquals(GlobalVar.handshakeMsg2)){
						System.out.print("Handshake successful. I am now connected to client\n simpella>");

						//Operations.saveInfo(incomingSocket,"in");
						WorkerThread workerThread =new WorkerThread(connID);
						workerThread.start();
						
					}
					else if(incomingMsg.contentEquals(GlobalVar.closeConnection)){
						//in.close();
						//out.close();
						
					}
								
				}
				while(!incomingMsg.contentEquals(GlobalVar.closeConnection));
			}	
			catch(Exception e){System.out.println("Couldnot listen to port");}
		}

	}

}

