package uk.ac.ncl.burton.twyb.PK.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
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
	
	/**
	 * String stating the connection type. This is only used for logging.
	 */
	protected String connectionType = "ERROR";
	
	
	private UUID networkId = UUID.randomUUID();
	public UUID getNetworkId(){
		return networkId;
	}
	
	/**
	 * Boolean value for whether to use blocking mode.
	 * 
	 * If blocking mode is true, then the receiveMessage() will block until a message is received
	 */
	private boolean blockingMode = false;
	/**
	 * Set blocking mode to mode
	 * @param mode boolean value for whether blocking mode should be enabled
	 */
	public void setBlockingMode(boolean mode){
		blockingMode = mode;
	}
	/**
	 * Check if blocking mode is enabled
	 * @return returns true if blocking mode is enabled else false
	 */
	public boolean isBlockingMode(){
		return blockingMode;
	}
	
	/**
	 * Received message queue
	 */
	private Vector<String> messageList = new Vector<String>();
	/**
	 * Messages to send queue
	 */
	private Vector<String> messageListToSend = new Vector<String>();
	
	/**
	 * Retrieves a received message.
	 * 
	 * In non-blocking mode null is returned if there are no messages available.
	 * In blocking mode the method will block until a message is available.
	 */
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
	
	/**
	 * Adds a message to the send queue.
	 */
	public boolean sendMessage(String msg){
		
		if( messageListToSend.size() >= NetworkConfig.MAX_MESSAGES_IN_QUEUE && NetworkConfig.MAX_MESSAGES_IN_QUEUE > 0 ){
			return false;
		}
		
		messageListToSend.addElement(msg);
		
		return true;
	}
	
	/**
	 * Used for stopping the connection loop. Should not be used to check if the network connection is open.
	 */
	boolean networkShouldRun = true;
	public void stop(){
		networkShouldRun = false;
	}
	
	/**
	 * The method for running the network.
	 * @param socket
	 * @return status value. 0: non-exceptional stop.
	 */
	protected int connection( Socket socket ){
		
		try {
			
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			
			while( true ){
				
				// Stop Network
				if( !networkShouldRun ){
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
						networkShouldRun = false;
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