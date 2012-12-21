package core;

import java.awt.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unused")
public class ClientThread extends Thread
{
	int connID;

	ClientThread(int connID)
	{
		this.connID = connID;
	}
	public void run()
	{
		try{


			//System.out.println(" Reached in client thread with connID: "+connID);
			ArrayList<String> listofMsgIDs = new ArrayList<String>();
			ArrayList<String> listofrecievedQueryIDs = new ArrayList<String>();

			///Send PING
			//Send ping to  this connID's outstream

			while(true){
				MessageFormat receivedMsg  = (MessageFormat) GlobalVar.outConn.get(connID).inStream.readObject();
				//String msgType = MessageFormat.retMsgType(receivedMsg.MessageType);
				GlobalVar.outConn.get(connID).noOfPacketsReceived++;

				if(receivedMsg.MessageType == 0) // PING
				{	 
					GlobalVar.outConn.get(connID).noOfBytesReceived=GlobalVar.outConn.get(connID).noOfBytesReceived+23;
					String msgID="";
				//convert msg id (bytes) to string
				for(int i=0;i<15;i++)
					msgID=msgID+receivedMsg.MessageID[i];

				//check rtable if it contains this ping, if not,then add entry into rtable, call forwardPing(pingmsg,connid),
				if(!GlobalVar.rTable.values().contains(msgID))   //check
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
				Operations.sendPong(connID,receivedMsg.MessageID,"outgoing");
				}
				else if(receivedMsg.MessageType == 1)//PONG
				{	PongMessage receivedPong = (PongMessage) receivedMsg;
				int pingID=0;
				String type="";
				String msgID="";
				GlobalVar.outConn.get(connID).noOfBytesReceived=GlobalVar.outConn.get(connID).noOfBytesReceived+37;
				
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
					//System.out.println("Saved info from pong");
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
					//System.out.println("PONG forwarded");
				}

				}
				else if(receivedMsg.MessageType == -128) //QUERY
				{  // String query="";
					String msgID="";
					String words[];
					ArrayList<ArrayList<String>> queryResultSet = new ArrayList<ArrayList<String>>();

					QueryMessage receivedQuery = (QueryMessage) receivedMsg;
					String query=receivedQuery.querySearchString;
					//Search my shared files that is myfilesinfo table if it contains the query string
					//if yes then query hit
					GlobalVar.outConn.get(connID).noOfBytesReceived=GlobalVar.outConn.get(connID).noOfBytesReceived+23+query.getBytes().length;
					
					if(receivedQuery.MessageTTL==0)
					{						//do nothing
					}else
					{
						Operations.forwardQuery(receivedQuery,connID);	
					}

					//convert query (bytes) to string
					/*	for(int i=0;i<receivedQuery.querySearchString.length;i++)
						 query=query+receivedQuery.querySearchString[i];  
					 */


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
								//Save in result set: name size and index and sendQueryHit
								//int fileIndex=GlobalVar.filesInfo.indexOf(fileInfo);
								ArrayList<String> result = new ArrayList<String>() ;
								//result.add(0, fileIndex+"");
								String fileName=fileInfo.get(0);
								String fileSize= fileInfo.get(1);
								//result.add(0, Integer.toString(fileIndex));
								result.add(fileName);
								result.add(fileSize);

								queryResultSet.add(result);

							}

						}
					}	

					Operations.sendQueryHit(connID,receivedQuery.MessageID,queryResultSet,"outgoing");				
				}
				else if(receivedMsg.MessageType == -127)   //Query Hit
				{
					QueryHitMessage receivedQueryHit = (QueryHitMessage) receivedMsg;
					int queryID=0;
					String type="";
					String msgID="";

					System.out.println("Received Query Hit from my outgoing connection with");
					GlobalVar.outConn.get(connID).noOfBytesReceived=GlobalVar.outConn.get(connID).noOfBytesReceived+23;
					
					//is it a reply to a query i sent,if yes, display info ,
					//no then find it is reply to whose query and forward  to that node
					//convert msg id (bytes) to string
					for(int i=0;i<15;i++)
						msgID=msgID+receivedQueryHit.MessageID[i];  

					if(GlobalVar.mysentQueries.containsKey(msgID) )             /*it is a reply to my own query*/
					{
						//save results for display

						GlobalVar.noOfResponses=GlobalVar.noOfResponses+receivedQueryHit.queryResultSet.size();
						/*	for(byte each : receivedQueryHit.ipAddress)
						ip=ip+each;
					for(byte each : receivedQueryHit.port)
						port=port+each;*/

						for(int index=0; index< receivedQueryHit.queryResultSet.size() ; index++){
							ArrayList<String> eachHit =receivedQueryHit.queryResultSet.get(index);
						//	System.out.println("In Query Hit: "+eachHit);
							
							eachHit.add(receivedQueryHit.ipAddress);
							eachHit.add(receivedQueryHit.port);
							GlobalVar.findResults.put(GlobalVar.resultsCounter,eachHit);
							GlobalVar.resultsCounter++;
							
							GlobalVar.listResults.put(GlobalVar.listresultsCounter,eachHit);
							GlobalVar.listresultsCounter++;
							
						}						

						System.out.println("Results  saved in findresults table");
						//GlobalVar.findResults.put(GlobalVar.resultsCounter,receivedQueryHit.queryResultSet);
						//GlobalVar.resultsCounter++;

					}
					else																										//find it is reply to whose query is it and forward  to it 
					{
						ArrayList<String> listofQueryids = new ArrayList<String>();
						for(int key: GlobalVar.receivedQueries.keySet())
						{
							listofQueryids=GlobalVar.rTable.get(key);
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
						System.out.println("QueryHit forwarded");
					}
				}

			}


			//see info of this socket.Since it is client.that means this socket is for its outgoing connections
			/*String cip=outgoingSocket.getInetAddress().toString();
			System.out.println("ip of server is:"+cip);

			/*	String chname=outgoingSocket.getInetAddress().getCanonicalHostName().toString();
			String localPort= Integer.toString(outgoingSocket.getLocalPort());
			String remotePort=Integer.toString(outgoingSocket.getPort());


			BufferedReader in = new BufferedReader(new InputStreamReader(outgoingSocket.getInputStream()));
			ObjectOutputStream out = (ObjectOutputStream) outgoingSocket.getOutputStream();

			WrapThis sendThis = new WrapThis();
			sendThis.element = "Hi from client";
			out.writeObject(sendThis);

			/*String inputLine = in.readLine();
			if((inputLine!= null))
			{
				System.out.println ("Client sent: " + inputLine); 
				System.out.println("Echoing" +" "+ inputLine+ " to IP " + outgoingSocket.getInetAddress());
				System.out.println("Type= TCP");
				out.println(inputLine);
			}
			out.close(); 
			in.close();*/
		}catch(Exception e){e.printStackTrace();}
	}

}
