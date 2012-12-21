package core;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class DownloadClientThread extends Thread implements Runnable
{
	int fileIndex;
	DownloadClientThread(int fileIndex)
	{
		this.fileIndex=fileIndex;
	}


	public void run()
	{
		try {
			ArrayList<String> fileDownload=GlobalVar.findResults.get(fileIndex);
			String fileName=fileDownload.get(0);
			String ipAddress=fileDownload.get(2);
			String downloadPort=fileDownload.get(3);
			String fileSize=fileDownload.get(1);
		

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



		} catch ( ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


	}



	}


