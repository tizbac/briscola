/*  Briscola game
    Copyright (C) 2010  Tiziano Bacocco

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
package org.briscola.server;
import java.io.*;
import java.net.*;
import java.util.*;



//import Server.RemoveClientException;
public class Player {
	int _id;
	private Socket s;
	private Server srv;
	private Vector<String> sendq;
	private Lock sendq_lock;
	OutputStreamWriter out;
	BufferedReader in;
	StringBuffer recvbuf;
	PlayerInfo info;
	int currgame = -1;
	int sendpingtimer = 20000;
	long lastpacket = System.currentTimeMillis();
	/**
	 *  Creates a player
	 * @param sock The socket connected to the client
	 * @param id The identifier to assign to the player , must be unique on runtime
	 * @param srv_ The server instance
	 */
	Player(Socket sock,int id,Server srv_)
	{
		s = sock;
		try {
			s.setSoTimeout(1);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		_id = id;
		info = null;
		srv = srv_;
		sendq = new Vector<String>();
		sendq_lock = new Lock();
		recvbuf = new StringBuffer();
		try {
			out = new OutputStreamWriter(s.getOutputStream(),"UTF8");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in = new BufferedReader( new InputStreamReader(s.getInputStream(),"UTF8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SendLine("BSRV v0.1");
	}
	/**
	 * Updates the player
	 * This method flushes the send queue, receives incoming data from client , checks for timeout and if timeout occurred it kills the connection
	 * @param diff The time since last tick , in milliseconds
	 */
	public void Update(int diff)
	{
		Flush();
		ReceiveData();
		if ( sendpingtimer >= diff )
			sendpingtimer -= diff;
		else
		{
			SendLine("PING");
			sendpingtimer = 20000;
		}
		if ( System.currentTimeMillis() - lastpacket > 40000 )
			Kill();
	}
	/**
	 * Try to read data from socket and stop when timeout occurs, timeout is set to 1 ms so it won't block much
	 * TODO: Needs use of socket polling using NIO
	 */
	private void ReceiveData()
	{
		char buffer[] = new char[1024];
		int bytesread = -1;
		
		try {
			bytesread = in.read(buffer);
		}catch ( SocketTimeoutException e ) {
			return;
		}catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			Kill();
		} 
		if ( bytesread < 1 )
		{
			Kill();
			return;
		}
		recvbuf.append(buffer,0,bytesread);
		int indx = recvbuf.indexOf("\n");

		if ( indx > 0 )
		{
			String line = recvbuf.substring(0, indx);
			line = line.replaceAll("\r", "");
			System.out.println("Recvd: "+line);
			String other = recvbuf.substring(indx+1);
			recvbuf = new StringBuffer(other);
			String args[] = line.split(" ");
			if ( args.length > 0 )
			{

				String params[];
				if ( args.length > 1 )
					params = new String[args.length-1];
				else
					params = new String[1];
				for ( int i = 1; i < args.length; i++)
					params[i-1] = args[i];
				HandleCommand(args[0], params);
			}
		}else
		{
			String other = recvbuf.substring(1);
			recvbuf = new StringBuffer(other);
		}
	}
	/**
	 * Called when a command has been received
	 * @param command The name of the command
	 * @param args The array containing the parameters
	 */
	public void HandleCommand(String command , String[] args)
	{
		lastpacket = System.currentTimeMillis();
		try{
			String cmdupper = command.toUpperCase();
			System.out.printf("CMD: %s %d \n", command,command.length());
			System.out.flush();
			if ( info == null )
			{
				if ( cmdupper.equals("LOGIN") )
				{
					HandleLogin(args);

				}else{
					SendError("Command '"+command+"' does not exist! ("+args+")");

				}
			}else{
				if ( cmdupper.equals("LOGOUT") )
					Kill();
				else if ( cmdupper.equals("OG" ) ) // Open game [numpl] [desc]
				{
					srv.OpenGame(this, Integer.parseInt(args[0]), args.length > 1 ? args[1] : "(Nessuna)");
				}else if ( cmdupper.equals("LG" ) ) // Leave game [id]
				{
					int gid = Integer.parseInt(args[0]);
					Game g = srv.games.get(gid);
					if ( g.master == this )
						srv.CloseGame(gid);
					else
						g.RemovePlayer(this);
				}else if ( cmdupper.equals("JG" ) ) // Join game [id]
				{
					if ( currgame != -1 )
					{
						SendError("You are already in another game");
						return;
					}
					int gid = Integer.parseInt(args[0]);
					Game g = srv.games.get(gid);
					if ( g.players.size() < g.numplayers )
						if ( g.started == false )
							g.AddPlayer(this);
						else
							g.p.ReplacePlayer(g.p.GetFirstEmptySlot(), this);
					else
						SendError("Game is full");
	
				}else if ( cmdupper.equals("START"))
				{
					if ( currgame == -1 )
					{
						SendError("You are not in a game");
						return;
					}
					Game g = srv.games.get(currgame);
					if ( g.master != this )
					{
						SendError("You are not the game host");
						return;
					}
					if ( g.p != null )
					{
						SendError("The game is already started");
						return;
					}
					if ( g.players.size() == g.numplayers )
						g.StartGame();
					else
						SendError("No enough players to start game");
				}else if ( cmdupper.equals("TC")) // Take card from deck
				{
					if ( currgame == -1 )
					{
						SendError("You are not in a game");
						return;
					}
					Game g = srv.games.get(currgame);
					BriscolaPlayer gioc = g.GetGamePlayer(this);
					gioc.drawcard();
				}else if ( cmdupper.equals("UC")){ //Use card
					if ( currgame == -1 )
					{
						SendError("You are not in a game");
						return;
					}
					Game g = srv.games.get(currgame);
					BriscolaPlayer gioc = g.GetGamePlayer(this);
					try{
						gioc.throw_card(Integer.parseInt(args[0]));
					}catch ( BriscolaPlayer.ActionNotPermittedException e )
					{
						SendError("Action not allowed!");
					}
				}else if ( cmdupper.equals("PONG"))
				{
					
					
				}else if ( cmdupper.equals("GAMECHAT") )
				{
					if ( currgame == -1 )
					{
						SendError("You are not in a game");
						return;
					}
					Game g = srv.games.get(currgame);
					g.OnGameChat(this, Server.join(args, " "));
				}
					else{
					SendError("Command '"+command+"' does not exist! ("+args+")");
	
				}
			}
		}catch ( Exception e)
		{
			e.printStackTrace();
			try {
				out.write("E Server-side exception while processing your command!.\n");
				out.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Kill();

		}
	}
	/**
	 * Handles LOGIN command
	 * @param args
	 */
	private void HandleLogin(String[] args) {
		if ( args.length < 2 )
		{
			SendError("Invalid login parameters");
			return;
		}
		String clientversion = args.length > 2 ? args[2] : "";
		if ( ! clientversion.equals("1") )
		{
			SendErrorFast("Versione del client non valida , scaricare la nuova versione da http://code.google.come/p/briscola");
			Kill();
			return;
		}
		try {
			info = srv.LoginPlayer(this,args[0], args[1]);
		} catch (Server.AlreadyLoggedInException e) {
			SendErrorFast("Login failed( Already logged in!).");
			Kill();
			return;
		}
		if ( info == null )
		{
			SendErrorFast("Login failed( Invalid username/password).");
			Kill();
			return;
		}else{
			SendLine("LOGINOK "+_id+" "+info.gameswon+" "+info.gameslost+" "+info.dbid);
			srv.SendState(this);
		}
	}
	/**
	 * Forcibly removes this client from the server
	 */
	public void Kill()
	{
		System.out.println("Killing client "+_id);
		try {
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			srv.RemovePlayer(_id,false);
		} catch (Server.RemoveClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * Try to send all messages on the send queue
	 * TODO: Needs use of NIO to avoid possible blocking operations
	 */
	private void Flush() 
	{
		try {
			try {
				sendq_lock.lock();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while ( sendq.size() > 0 )
			{
				try {
					out.write(sendq.get(0));
					out.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Kill();
					break;
				}
				sendq.remove(0);
			}
		}finally{
			sendq_lock.unlock();
		}
	}
	/**
	 * Sends a command to this client
	 * @param line The command to be sent
	 */
	public void SendLine(String line)
	{
		try {
			sendq_lock.lock();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendq.add(line+"\n");
		sendq_lock.unlock();
	}
	/**
	 * Sends an error message to the client
	 * @param error The message
	 */
	public void SendError(String error)
	{
		SendLine("E "+error);
	}
	/**
	 * Sends an error message to the client without respecting send queue, used when forcing disconnect
	 * @param error The message
	 */
	public void SendErrorFast(String error)
	{
		try {
			out.write("E "+error+"\n");
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Called when the player just won a game, it updates stats on other clients
	 */
	public void WonGame() {
		info.gameswon++;
		srv.BroadCastToLoggedIn("UPDATEPLAYER "+_id+" "+info.gameswon+" "+info.gameslost);
		
	}
	/**
	 * Called when the player just lost a game, it updates stats on other clients
	 */
	public void LostGame() {
		info.gameslost++;
		srv.BroadCastToLoggedIn("UPDATEPLAYER "+_id+" "+info.gameswon+" "+info.gameslost);
	}
}
