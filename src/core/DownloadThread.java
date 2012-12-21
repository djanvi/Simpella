package core;
import java.io.File;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class DownloadThread extends Thread implements Runnable 
{
	int downloadPort;
	public static String FileName;
	public static String sendThis;
	
	DownloadThread(int downloadport)
	{
		this.downloadPort = downloadport;

	}

	public void run()
	{
		try {
			
			ServerSocket downloadsocket = new ServerSocket(downloadPort);
			Socket acceptSocket = downloadsocket.accept();
				System.out.println("Connection received");
			ObjectInputStream in = new ObjectInputStream(acceptSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(acceptSocket.getOutputStream());
			
			WrapThis obj; 
			String incomingMsg;
			obj = (WrapThis) in.readObject();
			incomingMsg = obj.element;
			System.out.println("Connection received 2");
			///incomingMsg = (String) in.readObject();
							GlobalVar gv= new GlobalVar();
			
				System.out.println("Received msg1 from client");
				String HostSplit[] = incomingMsg.split("Host:");
				String aftrhostnm = HostSplit[1];
				String Hostnm[] = aftrhostnm.split(":");
				String HostIP = Hostnm[0];
				String Address = gv.myIPAddress;
				
				String [] splitString = incomingMsg.split("\\s*[/|HTTP]+\\s*");
				String FileName = splitString[4];
				String contentLength="";
				
				for(ArrayList<String> fileinfo: GlobalVar.filesInfo)
				{
					if (fileinfo.get(0).contentEquals(FileName))
					{
						contentLength=fileinfo.get(1);
					}
						
				}
			
				//File check = new File(pathname)
			File check = new File(GlobalVar.sharedDirectory);
			File FiletobeRead = new File(check, FileName);
		
			
			
			if(HostIP.contentEquals(Address) && FiletobeRead.exists())
				{  	
					WrapThis sendThis = new WrapThis();
					sendThis.element = GlobalVar.Msg2+contentLength+"\r\n\r\n";					
					out.writeObject(sendThis);	
					System.out.println("Sent msg2 to client");
					Operations.sendFile(FiletobeRead,acceptSocket);
					
					//acceptSocket.close();

				}
			else
			{
				String replyToClient="HTTP/1.1 503 File not found.\r\n\r\n";
				out.writeObject(replyToClient);
				//out.writeObject.flush();
				//out.writeObject.close();
			}
			
		} catch ( ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
}	















