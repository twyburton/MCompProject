package uk.ac.ncl.burton.twyb.PK.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

import uk.ac.ncl.burton.twyb.utils.NetworkUtils;

public abstract class NetworkConnection implements NetworkInterface, Runnable{

	/*
	 *  Messages consist of a status byte followed by the byte length of the message with the message attached at the end.
	 *  
	 *  status[1] messageLength[4] message[n] 
	 *  
	 *  Use Little Endian for integers in messages.
	 *  
	 *  
	 */
	
	protected String connectionType = "ERROR";
	
	private boolean blockingMode = false;
	public void setBlockingMode(boolean mode){
		blockingMode = mode;
	}
	public boolean isBlockingMode(){
		return blockingMode;
	}
	
	private Vector<String> messageList = new Vector<String>();
	private Vector<String> messageListToSend = new Vector<String>();
	
	public String receiveMessage(){
	
		if( !blockingMode ){
			if( messageList.size() > 0 ){
				return messageList.remove(0);
			}
			return null;
		} else {
			while( true ){
				if( messageList.size() > 0 ){
					return messageList.remove(0);
				}
			}
		}
		
	}
	
	public boolean sendMessage(String msg){
		
		if( messageListToSend.size() >= NetworkConfig.MAX_MESSAGES_IN_QUEUE ){
			return false;
		}
		
		messageListToSend.addElement(msg);
		
		return true;
	}
	
	/**
	 * Used for stopping the connection loop. Should not be used to check if the network connection is open.
	 */
	boolean networkRunning = true;
	public void stop(){
		networkRunning = false;
	}
	
	protected int connection( Socket socket ){
		
		try {
			
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			
			while( true ){
				
				// Stop Network
				if( !networkRunning ){
					out.write(0);
					break;
				}
				
				
				// Write to out
				if( messageListToSend.size() > 0 ){
					if( NetworkConfig.LOG_CORE ) System.out.println("[" + connectionType + "] Sending message...");
					
					String msg = messageListToSend.remove(0);
					byte[] msgLength = NetworkUtils.my_int_to_bb_le(msg.length());
					
					out.write(1);
					out.write(msgLength);
					out.write(msg.getBytes());
					
					if( NetworkConfig.LOG_CORE ) System.out.println("[" + connectionType + "] Message sent");
				}
				
				
				// Read from in
				if( in.available() > 0 ){
					
					if( NetworkConfig.LOG_CORE ) System.out.println("[" + connectionType + "] Receiving message...");
					
					byte status = (byte) in.read();
					
					if( status == 1 ){
					
						byte[] msgLength = new byte[4];
						in.read(msgLength, 0, 4);
						int msg_length = NetworkUtils.my_bb_to_int_le(msgLength);
						
						byte[] msg = new byte[msg_length];
						in.read(msg, 0, msg_length);
						
						String message = "";
						for( int i = 0 ; i < msg.length; i++){
							message += (char)msg[i];
						}
						
						messageList.add(message);
						
						if( NetworkConfig.LOG_CORE ) System.out.println("[" + connectionType + "] Message received");
						
					} else if (status == 0){
						networkRunning = false;
						break;
					}
						
				}
				
				// Sleep
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			return 0;
			
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		
		
		
	}
	
	protected boolean connection_closed = false;
	public boolean isConnectionClosed(){
		return connection_closed;
	}
}
