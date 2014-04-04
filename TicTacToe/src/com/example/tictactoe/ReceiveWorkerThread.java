package com.example.tictactoe;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import android.os.Message;



public class ReceiveWorkerThread extends Thread{
	//private DatagramPacket rxPacket;
	private DatagramSocket socket;
	
	public ReceiveWorkerThread(DatagramSocket socket){
		this.socket = socket;
	}
	public void run(){
		// receive the server's response
			while(true){
				byte[] buf = new byte[512];
				DatagramPacket rxPacket = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(rxPacket);
					Message msg = MainActivity.handler.obtainMessage();
					String payload = new String(rxPacket.getData(), 0,
							rxPacket.getLength());
					if(payload.startsWith("ERROR")){
						MainActivity.handler.obtainMessage(MainActivity.MSG_ERROR, payload.toString()).sendToTarget();
					}
					//just get the message, but handler doesn't do anything critical
					else if(payload.startsWith("POLL")){
						MainActivity.handler.obtainMessage(MainActivity.MSG_POLL, payload.toString()).sendToTarget();
					}
					else if(payload.startsWith("REGISTERED")){
						MainActivity.handler.obtainMessage(MainActivity.MSG_REGISTER, payload.toString()).sendToTarget();
					}
					else if(payload.startsWith("You joined")){
						MainActivity.handler.obtainMessage(MainActivity.MSG_JOIN, payload.toString()).sendToTarget();
					}
					else if(payload.startsWith("TOO MANY")){
						MainActivity.handler.obtainMessage(MainActivity.MSG_TOOMANY, payload.toString()).sendToTarget();
					}
					
					else if(payload.startsWith("MESSAGE")){
						MainActivity.handler.obtainMessage(MainActivity.MSG_SEND, payload.toString()).sendToTarget();
					}
					else if(payload.startsWith("READY")){
						MainActivity.handler.obtainMessage(MainActivity.MSG_READY, payload.toString()).sendToTarget();
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
		}		
		
	}
}
