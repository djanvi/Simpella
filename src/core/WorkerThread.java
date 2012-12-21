package core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class WorkerThread extends Thread
{
	int connID;
	WorkerThread(int connID)
	{
		this.connID = connID;
	}
	public void run()
	{
		try{
			//System.out.println(" Reached in worker thread");
			ArrayList<String> listofMsgIDs = new ArrayList<String>();
			ArrayList<String> listofrecievedQueryIDs = new ArrayList<String>();
			while(true){
				MessageFormat receivedMsg  = (MessageFormat) GlobalVar.inConn.get(connID).inStream.readObject();
			//	String msgType = MessageFormat.retMsgType(receivedMsg.MessageType);
				GlobalVar.inConn.get(connID).noOfPacketsReceived++;
				
				if(receivedMsg.MessageType == 0) // PING
				{	 
					GlobalVar.inConn.get(connID).noOfBytesReceived=GlobalVar.inConn.get(connID).noOfBytesReceived+23;
					
					String msgID="";
				//convert msg id (bytes) to string
				for(int i=0;i<15;i++)
					 msgID=msgID+receivedMsg.MessageID[i];
					
					//check rtable if it contains this ping, if not,then add entry into rtable, call forwardPing(pingmsg,connid),
					if(!GlobalVar.rTable.values().contains(msgID))
					{	  
						
						listofMsgIDs.add(msgID);
						GlobalVar.rTable.put(connID,listofMsgIDs );
						
						if(receivedMsg.MessageTTL==0)
						{
							//do nothing		
						}
						else
						{
							Operations.forwardPing(receivedMsg,connID);
						}
					}
					Operations.sendPong(connID,receivedMsg.MessageID,"incoming");
				}
				else if(receivedMsg.MessageType == 1)//PONG
				{	
					GlobalVar.inConn.get(connID).noOfBytesReceived=GlobalVar.inConn.get(connID).noOfBytesReceived+37;
					
					PongMessage receivedPong = (PongMessage) receivedMsg;
						int pingID=0;
						String type="";
						String msgID="";
						
					//is it a reply to a ping i sent,if yes, save info, no then find it is reply to whose ping and forward ping to that node
						//convert msg id (bytes) to string
						for(int i=0;i<15;i++)
							 msgID=msgID+receivedPong.MessageID[i];  
								
					if(GlobalVar.mysentPings.containsKey(msgID) )             /*it is a reply to my own ping*/
					{
						//save info from PONG
						
						
						PongInformation obj = new PongInformation();
						obj.pongMessageID=receivedPong.MessageID;
						obj.ipSender=receivedPong.ipAddress;
						obj.portSender=receivedPong.port;
						obj.numberOfFilesShared=receivedPong.numberOfFilesShared;
						obj.numberOfKBShared=receivedPong.numberOfKBShared;
						GlobalVar.pongInfo.add(obj);
						System.out.println("Saved info from pong");
					}
					else																										//find it is reply to whose ping is it and forward pong to it 
					{
						ArrayList<String> tempList = new ArrayList<String>();
							for(int key: GlobalVar.rTable.keySet())
							{
								tempList=GlobalVar.rTable.get(key);
								for(String s:tempList)
								{
									if(s.contentEquals(msgID))
									{
										pingID=key;
									}
								}
							}	
						
					//check whether this connID is in its incoming connections or outgoing connections table
						if(GlobalVar.inConn.containsKey(pingID))
							type="incoming";
						else if(GlobalVar.outConn.containsKey(pingID))
							type="outgoing";
					
						
						Operations.forwardPong(pingID,receivedPong,type);
						System.out.println("PONG forwarded");
					}
					
				}
				else if(receivedMsg.MessageType == -128) //QUERY
				{   //String query="";
					
					
					String msgID="";
					String words[];
					 ArrayList<ArrayList<String>> queryResultSet = new ArrayList<ArrayList<String>>();
					
					QueryMessage receivedQuery = (QueryMessage) receivedMsg;
					String query=receivedQuery.querySearchString;
					//Search my shared files that is myfilesinfo table if it contains the query string
					//if yes then query hit
					GlobalVar.inConn.get(connID).noOfBytesReceived=GlobalVar.inConn.get(connID).noOfBytesReceived+23+query.getBytes().length;
					
					if(receivedQuery.MessageTTL==0)
					{						//do nothing
					}else
					{
						Operations.forwardQuery(receivedQuery,connID);	
					}
					//convert query (bytes) to string
				/*	for(int i=0;i<receivedQuery.querySearchString.length;i++)
						 query=query+receivedQuery.querySearchString[i]; */ 
					//convert msg id (bytes) to string
					for(int i=0;i<15;i++)
					msgID=msgID+receivedMsg.MessageID[i];
					
					listofrecievedQueryIDs.add(msgID);
					GlobalVar.receivedQueries.put(connID,listofrecievedQueryIDs );
					
					words=query.split(" ");
					
					for(String word:words)
					{
						//ArrayList<String> tempList;
						for(ArrayList<String> fileInfo: GlobalVar.filesInfo)
						{
							if(fileInfo.get(0).contains(word))
							{
								ArrayList<String> result = new ArrayList<String>() ;
								//Save in result set: name size and index and sendQueryHit
								///int fileIndex=GlobalVar.filesInfo.indexOf(fileInfo);
								String fileName=fileInfo.get(0);
								String fileSize= fileInfo.get(1);
								//result.add(0, Integer.toString(fileIndex));
								result.add(fileName);
								result.add(fileSize);
															
								queryResultSet.add(result);
								
							}
								
						}
					}	
					
					Operations.sendQueryHit(connID,receivedQuery.MessageID,queryResultSet,"incoming");				
				}
				else if(receivedMsg.MessageType == -127) //QUERY HIT
				{
					QueryHitMessage receivedQueryHit = (QueryHitMessage) receivedMsg;
					int queryID=0;
					String type="";
					String msgID="";
					GlobalVar.inConn.get(connID).noOfBytesReceived=GlobalVar.inConn.get(connID).noOfBytesReceived+23;
					//System.out.println("Received Query Hit from my incoming connection with connID:"+connID);
				
					//is it a reply to a query i sent,if yes, display info ,
					//no then find it is reply to whose query and forward  to that node
					//convert msg id (bytes) to string
					for(int i=0;i<15;i++)
						 msgID=msgID+receivedQueryHit.MessageID[i];  
							
				if(GlobalVar.mysentQueries.containsKey(msgID) )             /*it is a reply to my own query*/
				{
					//display results
					
					
					GlobalVar.noOfResponses=GlobalVar.noOfResponses+receivedQueryHit.queryResultSet.size();
					/*for(byte each : receivedQueryHit.ipAddress)
						ip=ip+each;
					for(byte each : receivedQueryHit.port)
						port=port+each;
					*/
					for(ArrayList<String> eachHit : receivedQueryHit.queryResultSet){
						eachHit.add( receivedQueryHit.ipAddress);
						eachHit.add( receivedQueryHit.port);
						GlobalVar.findResults.put(GlobalVar.resultsCounter,eachHit);
						GlobalVar.resultsCounter++;
						
						GlobalVar.listResults.put(GlobalVar.listresultsCounter,eachHit);
						GlobalVar.listresultsCounter++;
						
						
					//	System.out.println("Results  saved in findresults table");			
				}
				}
				else																										//find it is reply to whose query is it and forward  to it 
				{
					//ArrayList<String> listofQueryids = new ArrayList<String>();
						for(int key: GlobalVar.receivedQueries.keySet())
						{
							ArrayList<String> listofQueryids=GlobalVar.rTable.get(key);
							
							for(String s:listofQueryids)
							{
								if(s.contentEquals(msgID))
								{
									queryID=key;
								}
							}
						}	
					
				//check whether this connID is in its incoming connections or outgoing connections table
					if(GlobalVar.inConn.containsKey(queryID))
						type="incoming";
					else if(GlobalVar.outConn.containsKey(queryID))
						type="outgoing";
				
					
					Operations.forwardQueryHit(queryID,receivedQueryHit,type);
					//System.out.println("QueryHit forwarded");
				}
				}

			}


			/*
			obj = (WrapThis) GlobalVar.inConn.get(connID).inStream.readObject();
			incomingMsg = obj.element; 
			System.out.println ("Client sent: " + incomingMsg); 

			//see info of this socket.Since it is server.that means this socket is for its incoming connections

			String cip=incomingSocket.getInetAddress().toString();
			System.out.println("ip of client is:"+cip);

			 * String chname=incomingSocket.getInetAddress().getCanonicalHostName().toString();
			String localPort= Integer.toString(incomingSocket.getLocalPort());
			String remotePort=Integer.toString(incomingSocket.getPort());
			 */

			/* ObjectInputStream in = (ObjectInputStream) incomingSocket.getInputStream();

			 //PrintWriter out = new PrintWriter(incomingSocket.getOutputStream(),true);

			 WrapThis obj;
				String incomingMsg;

					obj = (WrapThis) in.readObject();
					incomingMsg = obj.element; 


			if((incomingMsg!= null))
			{
							/*System.out.println("Echoing" +" "+ inputLine+ " to IP " + csocket.getInetAddress());
				System.out.println("Type= TCP");
				out.println(inputLine);*/
			//}



			/*out.close(); 
			in.close();*/
		}catch(Exception e){e.printStackTrace();}

	}

}
