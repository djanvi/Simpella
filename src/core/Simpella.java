package core;


import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@SuppressWarnings("unused")
public class Simpella
{
	static final int packetsize=1024;

	Socket[] clientsocket = new Socket[7];

	public static void main(String args[])throws Exception
	{
		Simpella sc = new Simpella();
		Random numGenerator= new Random();
		String command;


		GlobalVar.myconnectionPort=Short.parseShort(args[0]);
		GlobalVar.mydownloadPort = Short.parseShort(args[1]);
		int connID;



		if (args.length<2)
		{
			System.out.println("Default ports will be Used");
			GlobalVar.myconnectionPort = 6346;
			GlobalVar.mydownloadPort=5635;
			ServerThread serverThread =new ServerThread(GlobalVar.myconnectionPort);
			serverThread.start();
			DownloadThread downloadThread =new DownloadThread(GlobalVar.mydownloadPort);
			downloadThread.start();	
		}
		else
		{

			ServerThread serverThread =new ServerThread(GlobalVar.myconnectionPort);
			serverThread.start();
			DownloadThread downloadThread =new DownloadThread(GlobalVar.mydownloadPort);
			downloadThread.start();	
		}

		Socket s= new Socket("www.google.com",80);
		
		System.out.println("Local IP:" + s.getLocalAddress().getHostAddress());
		System.out.println("Simpella Port:" + GlobalVar.myconnectionPort );
		System.out.println("Downloading Port:" + GlobalVar.mydownloadPort );
		System.out.println("Simpella Version 0.6 ");

		while(true)
		{

			System.out.print("simpella>");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			command=in.readLine();
			String[] temp;
			/*if(command.equals("info"))
		{
			Socket s= new Socket("www.google.com",80);
			System.out.println("IPAddress  TCP port   DownloadPort");
			System.out.println(s.getLocalAddress().getHostAddress()+" "+GlobalVar.myconnectionPort+ " " + GlobalVar.mydownloadPort);
			s.close();
		}*/
			
			 if(command.contains("open"))
			{
				connID = numGenerator.nextInt();
				temp=command.split(" ");
				Operations.startHandshake(temp[1],connID);
			}
			else if(command.contains("update"))
			{
				Operations.initiatePing();
			}

			/*else if(command.contains("Monitor")|| (command.contains("monitor")))
			{

			}*/
			else if (command.contains("share"))
			{
				temp=command.split(" ");
				//System.out.println("Sharing" + new File("").getAbsolutePath());
				Operations.shareDirectory(temp[1]);
			}
			else if(command.contentEquals("Scan") || command.contentEquals("scan"))
			{
				Operations.scan();
			}
			else if(command.contains("exit") || command.contentEquals("Exit") || command.contentEquals("quit") || command.contentEquals("Quit"))
			{
				System.out.println("Bye!!");
				System.exit(0);
			}
			else if( (command.contentEquals("bye")  || command.contentEquals("Bye")))
			{
				System.out.print("Sorry wrong command to leave .... try using Exit/Quit");
			}
			/*else if(command.contains("ponginfo"))
			{
				Operations.showPongInfo();
			}
			else if(command.contains("showfileinfo"))
			{
				Operations.showFilesInfo();
			}*/
			else if(command.contains("find"))
			{
				temp=command.split(" ",2);
				Operations.initiateQuery(temp[1]);
			}
			else if(command.contains("download"))
			{
				temp=command.split(" ");
				DownloadClientThread downloadclientThread =new DownloadClientThread(Integer.parseInt(temp[1]));
				downloadclientThread.start();	
				
			}
			else if(command.contains("list"))
			{
				Operations.showlistResults();

			}
			else if(command.contains("clear"))
			{
				temp = command.split(" ");

				if(temp.length==1){
					Operations.clearListResults("all");
				}
				else{
					Operations.clearListResults(temp[1]);}

			}
			else if(command.contains("info"))
			{
				Operations.handleInfoCommand(command);

			}
			else
			{
				System.out.println("unknown command");
			}
		}

	}



}