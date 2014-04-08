package com.example.tictactoe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
	protected static final String serverAddress = "10.66.196.232";
	//protected static final String serverAddress = "54.186.235.124";
	protected static final int serverPort = 20000;
	protected static final int MaxSize = 512;
	public String group;
	public String Letter;
	public int ID;
	public int count = 0;
	public boolean pressTwice;
	public String winner = "None";
	public DatagramSocket socket;
	public boolean startGame = false;
	
	public boolean turn = true;
	public int turnToPlay = 1;
	
	
	public Button button11;
	public Button button12;
	public Button button13;
	public Button button21;
	public Button button22;
	public Button button23;
	public Button button31;
	public Button button32;
	public Button button33;
	public Button start;
	public Button poll;
	public EditText text;
	
	public static Handler handler;
	
	public static final int MSG_REGISTER = 1;
	public static final int MSG_SEND = 2;
	public static final int MSG_JOIN = 3;
	public static final int MSG_POLL = 4;
	public static final int MSG_ERROR = 5;
	public static final int MSG_TOOMANY = 6;
	public static final int MSG_READY = 7;
	
	public String result;
	public InetSocketAddress serverSocketAddress = 
			new InetSocketAddress(MainActivity.serverAddress, MainActivity.serverPort);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		button11 = (Button)findViewById(R.id.button1);
		button12 = (Button)findViewById(R.id.button2);
		button13 = (Button)findViewById(R.id.button3);
		button21 = (Button)findViewById(R.id.button4);
		button22 = (Button)findViewById(R.id.button5);
		button23 = (Button)findViewById(R.id.button6);
		button31 = (Button)findViewById(R.id.button7);
		button32 = (Button)findViewById(R.id.button8);
		button33 = (Button)findViewById(R.id.button9);
		start = (Button)findViewById(R.id.button10);
		//poll = (Button)findViewById(R.id.button11);
		text = (EditText)findViewById(R.id.editText1);
		
		button11.setEnabled(false);
		button12.setEnabled(false);
		button13.setEnabled(false);
		button21.setEnabled(false);
		button22.setEnabled(false);
		button23.setEnabled(false);
		button31.setEnabled(false);
		button32.setEnabled(false);
		button33.setEnabled(false);
		//poll.setEnabled(false);
		
		socket = null;
		try{
			socket = new DatagramSocket();
		}catch (IOException e) {
			// we jump out here if there's an error
			e.printStackTrace();
		}
		new ReceiveWorkerThread(socket).start();
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// a message is received from the worker thread thread
				
				if(msg.what == MSG_SEND) {
					// it's a result string
					result = (String)msg.obj;
					System.out.println("Im here to receive message------------!!");
					//using result message to decide what to do
					if(result.startsWith("MESSAGE")){
						System.out.println("in MSG_SEND--------------");
					
						String [] tokens = result.split(" ");
						String [] msgTokens = result.split(":");
						String message = msgTokens[1].trim();
						int msgID = Integer.parseInt(tokens[3]);
						System.out.println("------im the msg ID!!!!"+msgID);
						System.out.println("------im the msgeeeeee!!!!"+message);
					
						if(msgID != ID){
							//turn = true;
							//String turnToPlay = message.substring(0,1);
							if(message.startsWith("Win")){
								CharSequence text = message.substring(3)+" won";
								Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
								toast.show();
							}
							else{
								String sym = message.substring(0,1);
								String rowCol = message.substring(1);
								changeButtonTexts(sym,rowCol);
								enableAllButtons(true);
								System.out.println(checkStatus()+"check status in MSG_SEND----------");
							}
						}
						else if(msgID == ID){
							enableAllButtons(false);
						}
						System.out.println("print count before checking status!!----------" + count);
						System.out.println("print status------------"+checkStatus());
						
						if(checkStatus()){
							//poll.setEnabled(false);
							enableAllButtons(false);
							CharSequence text = winner+" won";
							Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
							toast.show();
						}
					}
					else{
						CharSequence text = "Error about receiving message";
						Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
						toast.show();
					}	
				}
				else if(msg.what == MSG_ERROR){
					result = (String)msg.obj;
					CharSequence text = result;
					Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
					toast.show();
				}
				else if(msg.what == MSG_REGISTER){
					/*System.out.println("In register!!!---------");
					result = (String)msg.obj;
					String [] tokens = result.split(":");
					ID = Integer.parseInt(tokens[1].trim());
					
					CharSequence text = result;
					Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
					toast.show();*/
					System.out.println("In register!!!---------");
					result = (String)msg.obj;
					String [] tokens = result.split(":");
					ID = Integer.parseInt(tokens[1].trim());
					System.out.println("ID="+ID);
					CharSequence text = result;
					Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
					toast.show();
					joinTask jt = new joinTask();
					jt.execute();
				}
				
				else if(msg.what == MSG_JOIN) {
					result = (String)msg.obj;
					if(result.startsWith("You joined")){
						System.out.println("Im in you dog joined");
						enableAllButtons(true);
						//poll.setEnabled(true);
						start.setEnabled(false);
					} /*else {
						poll.setEnabled(true);
						start.setEnabled(false);
					}*/
					CharSequence text = "Success!";
					Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
					toast.show();
				}
				else if (msg.what == MSG_TOOMANY){
					result = (String)msg.obj;
					CharSequence text = result;
					Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
					toast.show();
				}
				else if (msg.what == MSG_READY){
					result = (String)msg.obj;
					startGame = true;
					CharSequence text = "Game is ready";
					Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
					toast.show();	
					
				}
			}										
		};
	
	}
	private void changeButtonTexts(String letter, String rowCol) {
		if(rowCol.startsWith("11")){
			button11.setText(letter);
		} else if(rowCol.startsWith("12")){
			button12.setText(letter);
		}else if(rowCol.startsWith("13")){
			button13.setText(letter);
		}else if(rowCol.startsWith("21")){
			button21.setText(letter);
		}else if(rowCol.startsWith("22")){
			button22.setText(letter);
		}else if(rowCol.startsWith("23")){
			button23.setText(letter);
		}else if(rowCol.startsWith("31")){
			button31.setText(letter);
		}else if(rowCol.startsWith("32")){
			button32.setText(letter);
		}else if(rowCol.startsWith("33")){
			button33.setText(letter);
		}else { 
			CharSequence text = "illegal move error";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	
	public void enableAllButtons(boolean b){
		button11.setEnabled(b);
		button12.setEnabled(b);
		button13.setEnabled(b);
		button21.setEnabled(b);
		button22.setEnabled(b);
		button23.setEnabled(b);
		button31.setEnabled(b);
		button32.setEnabled(b);
		button33.setEnabled(b);
	}
	
	public void startClick(View V){
		final String line = text.getText().toString();
		if(line == ""){
			CharSequence text = "Please Enter Text";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		} else { 
			String[] token = line.split(",");
			group = token[0];
			Letter = token[1];
			
			startTask st = new startTask();
			//joinTask jt = new joinTask();
			//legalTask lt = new legalTask();
			
			st.execute("");
			//lt.execute("");
			//jt.execute("");
				
			System.out.println("Game start!");
		}
		
	}
	
	private class startTask extends AsyncTask<String, Void, String>{
		
		@Override
		protected String doInBackground(String... params) {
			String rval = null;
			try{									 
				// send "REGISTER" to the server
				String command = "REGISTER";
				
				DatagramPacket txPacket = new DatagramPacket(command.getBytes(),
						command.length(), serverSocketAddress);
				// send the packet through the socket to the server
				socket.send(txPacket);
				
			} catch (IOException e) {
				// we jump out here if there's an error
				e.printStackTrace();
			} 
			return rval;
		
		}

	}
	
	private class joinTask extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			try{
				String join = "JOIN "+Integer.toString(ID)+" "+group;
			
			DatagramPacket txPacket = new DatagramPacket(join.getBytes(),
					join.length(), serverSocketAddress);
			// send the packet through the socket to the server
			socket.send(txPacket);
			}
			catch (IOException e) {
				// we jump out here if there's an error
				e.printStackTrace();
			} 

			return null;
		}
		
	}
	
	private class legalTask extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			try{
				String join = "Legal " +group;
			
			DatagramPacket txPacket = new DatagramPacket(join.getBytes(),
					join.length(), serverSocketAddress);
			// send the packet through the socket to the server
			socket.send(txPacket);
			}
			catch (IOException e) {
				// we jump out here if there's an error
				e.printStackTrace();
			} 

			return null;
		}
		
	}
	
private class winTask extends AsyncTask<String, Void, String>{
		
		@Override
		protected void onPreExecute() {}
		
		@Override
		protected String doInBackground(String... arg0) {
			//the string of the button
			//turn = false;
			String move = arg0[0];
			String rval = null;
			try {
				System.out.println("ID ---------"+ID);
				String command = "SEND "+ID +" "+group+" "+ "Win"+winner;
				DatagramPacket txPacket = new DatagramPacket(command.getBytes(),
						command.length(), serverSocketAddress);
				// send the packet through the socket to the server
				socket.send(txPacket);
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return rval;
		}
	}
	
	private class task extends AsyncTask<String, Void, String>{
		
		@Override
		protected void onPreExecute() {}
		
		@Override
		protected String doInBackground(String... arg0) {
			//the string of the button
			//turn = false;
			String move = arg0[0];
			String rval = null;
			try {
				System.out.println("ID ---------"+ID);
				String command = "SEND "+ID +" "+group+" "+ Letter+move;
				DatagramPacket txPacket = new DatagramPacket(command.getBytes(),
						command.length(), serverSocketAddress);
				// send the packet through the socket to the server
				socket.send(txPacket);
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return rval;
		}

		@Override
		protected void onPostExecute(String result) {
				
				//enableAllButtons(false);
				
				if(checkStatus()){
					winTask t = new winTask();
					t.execute("");
					CharSequence text = winner+" won";
					Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
					toast.show();
				} 

		}
	}	
	
	
	public void button11Click(View V) {
		if(((String) button11.getText()).startsWith("__")){
			count++;
			button11.setText(Letter);
			task t = new task(); 
			t.execute("11");
		} else{
			CharSequence text = "Button already used";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	public void button12Click(View V) {
		if(((String) button12.getText()).startsWith("__")){
			count++;
			button12.setText(Letter);
			task t = new task(); 
			t.execute("12");
		} else{
			CharSequence text = "Button already used";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	public void button13Click(View V) {
		if(((String) button13.getText()).startsWith("__")){
			count++;
			button13.setText(Letter);
			task t = new task(); 
			t.execute("13");
		} else{
			CharSequence text = "Button already used";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	public void button21Click(View V) {
		if(((String) button21.getText()).startsWith("__")){
			count++;
			button21.setText(Letter);
			task t = new task(); 
			t.execute("21");
		} else{
			CharSequence text = "Button already used";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	public void button22Click(View V) {
		if(((String) button22.getText()).startsWith("__")){
			count++;
			button22.setText(Letter);
			task t = new task(); 
			t.execute("22");
		} else{
			CharSequence text = "Button already used";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	public void button23Click(View V) {
		if(((String) button23.getText()).startsWith("__")){
			count++;
			button23.setText(Letter);
			task t = new task(); 
			t.execute("23");
		} else{
			CharSequence text = "Button already used";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	public void button31Click(View V) {
		if(((String) button31.getText()).startsWith("__")){
			count++;
			button31.setText(Letter);
			task t = new task(); 
			t.execute("31");
		} else{
			CharSequence text = "Button already used";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	public void button32Click(View V) {
		if(((String) button32.getText()).startsWith("__")){
			count++;
			button32.setText(Letter);
			task t = new task(); 
			t.execute("32");
		} else{
			CharSequence text = "Button already used";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	public void button33Click(View V) {
		if(((String) button33.getText()).startsWith("__")){
			count++;
			button33.setText(Letter);
			task t = new task(); 
			t.execute("33");
		} else{
			CharSequence text = "Button already used";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	

	
	//check if there is a winner on board, if there is one, set his symbol to winner string;
	public boolean checkStatus(){
		System.out.println("CHECK");
		if(count >= 3){
			System.out.println("CHECKING");
			if(!((String) button11.getText()).startsWith("__") && (button11.getText().equals(button12.getText())) && (button11.getText().equals(button13.getText()))){
				winner = (String) button11.getText();
				return true;
			} else if(!((String) button21.getText()).startsWith("__") &&(button21.getText().equals(button22.getText())) && (button21.getText().equals(button23.getText()))){
				winner = (String) button21.getText();
				return true;
			} else if(!((String) button31.getText()).startsWith("__") &&(button31.getText().equals(button32.getText())) && (button31.getText().equals(button33.getText()))){
				winner = (String) button31.getText();
				return true;
			} else if(!((String) button11.getText()).startsWith("__") &&(button11.getText().equals(button21.getText()))&& (button11.getText().equals(button31.getText()))){
				winner = (String) button21.getText();
				return true;
			} else if(!((String) button22.getText()).startsWith("__") &&(button22.getText().equals(button12.getText())) && (button22.getText().equals(button32.getText()))){
				winner = (String) button22.getText();
				return true;
			} else if(!((String) button13.getText()).startsWith("__") &&(button13.getText().equals(button33.getText())) && (button23.getText().equals(button13.getText()))){
				winner = (String) button13.getText();
				return true;
			} else if(!((String) button22.getText()).startsWith("__") &&(button11.getText().equals(button22.getText())) && (button11.getText().equals(button33.getText()))){
				winner = (String) button22.getText();
				return true;
			} else if(!((String) button13.getText()).startsWith("__") &&(button13.getText().equals(button22.getText())) && (button22.getText().equals(button31.getText()))){
				winner = (String) button22.getText();
				return true;
			} else if(count == 9){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
