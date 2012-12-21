package core;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

public class ConnectionFormat {
	InetAddress ipAddress;
	String hostname;
	int localPort , remotePort;
	ObjectOutputStream outStream;
	ObjectInputStream inStream;
	int noOfPacketsReceived=0;
	int noOfPacketsSent=0;
	int noOfBytesReceived=0;
	int noOfBytesSent=0;
	
	
}
