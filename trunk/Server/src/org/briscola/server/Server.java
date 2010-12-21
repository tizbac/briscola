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
import java.sql.*;
import java.util.concurrent.*;

public class Server extends Thread {
	private ServerSocket mainSock;
	int idcount;
	int gameidcount = 0;
	private Lock playerlistlock = new Lock();
	UpdateThreadRunnable updth;
	private ConcurrentHashMap<Integer, Player> players;
	private Vector<Integer> loggedinaccounts;
	public HashMap<Integer,Gioco> games = new HashMap<Integer,Gioco>();
	Connection conn = null;
	String loginquery = "SELECT id,nickname,gameswon,gameslost FROM players WHERE nickname = ? AND password = ? LIMIT 1";
	String updateaccountquery = "UPDATE players SET gameswon = ? , gameslost = ? WHERE id = ? LIMIT 1";
	String pingquery = "SELECT 1";
	PreparedStatement pingstat;
	PreparedStatement loginstat;
	PreparedStatement updatestat;
	int pingtimer = 100;
	Server()
	{
		Settings setts = new Settings();
		loggedinaccounts = new Vector<Integer>();
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			
			e1.printStackTrace();
			System.exit(1);
		}
		try {
			String url = String.format("jdbc:mysql://%s/%s", setts.host,setts.database);
			conn = DriverManager.getConnection("jdbc:mysql://localhost/briscola",setts.username,setts.password);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			System.err.println("Cannot connect to database.\n");
			System.exit(1);
			
			e1.printStackTrace();
		}
		try {
			loginstat = conn.prepareStatement(loginquery);
			updatestat = conn.prepareStatement(updateaccountquery);
			pingstat = conn.prepareStatement(pingquery);
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			System.exit(1);
		}
		
		idcount = 0;
		try {
			mainSock = new ServerSocket();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			mainSock.setReuseAddress(true);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			
			mainSock.bind(new InetSocketAddress(setts.bindaddress, setts.bindport));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Cannot bind socket to port "+setts.bindport);
			System.exit(1);
		}
		players = new ConcurrentHashMap<Integer, Player>();
		updth = new UpdateThreadRunnable();
		updth.start();
	}
	public class RemoveClientException extends Exception
	{
		private int clientid;
		public RemoveClientException(int _clientid) {
			// TODO Auto-generated constructor stub
			clientid = _clientid;
		}
		public String getError()
		{
			return String.format("Failed to remove client %d", clientid);
		}
		
	}
	public class UpdateThreadRunnable extends Thread
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			long diff = 100;
			long updatestarttime;
			long updateendtime;
			while ( true )
			{
				updatestarttime = System.currentTimeMillis();
				try {
					playerlistlock.lock();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Collection<Player> coll = players.values();
				Iterator<Player> itr = coll.iterator();
				while(itr.hasNext())
					itr.next().Update((int)diff);
				playerlistlock.unlock();
				updateendtime = System.currentTimeMillis();
				if ( updateendtime-updatestarttime < 100 )
					try {
						Thread.sleep(100-(updateendtime-updatestarttime), 0);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.err.println("Interrupt on update thread");
						break;
					}
				if ( pingtimer > diff )
					pingtimer -= diff;
				else
				{
					try {
						System.out.println("Pinging MYSQL connection to keep alive");
						pingstat.execute();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					pingtimer = 60000;
					
				}
				diff = System.currentTimeMillis()-updatestarttime;
			}
		}
		
		
		
		
	}
	public void run()
	{
		while ( true )
		{
			idcount++;
			Socket sock;
			try {
				sock = mainSock.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("ERROR: Cannot accept connection!");
				e.printStackTrace();
				break;
			}
			System.out.printf("New client connected from %s",sock.getInetAddress().toString());
			try {
				playerlistlock.lock();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			players.put(idcount, new Player(sock,idcount,this));
			playerlistlock.unlock();
		}
		
	}
	public void RemovePlayer(int id,boolean lock) throws RemoveClientException
	{
		System.out.printf("Removing client %d\n", id);
		
		if ( lock ){
			
		
			try {
				playerlistlock.lock();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		try{
			if ( ! players.containsKey(id) )
				throw new RemoveClientException(id);
			boolean sendlogout = false;
			if ( players.get(id).info != null)
			{
				sendlogout = true;
				try {
					updatestat.setInt(1, players.get(id).info.gameswon );
					updatestat.setInt(2, players.get(id).info.gameslost );
					updatestat.setInt(3, players.get(id).info.dbid );
					updatestat.execute();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				loggedinaccounts.removeElement(players.get(id).info.dbid);
				try{
				if ( players.get(id).currgame != -1 )
				{
					if ( games.get(players.get(id).currgame).master == players.get(id) )
						CloseGame(players.get(id).currgame);
					else
						games.get(players.get(id).currgame).RemPlayer(players.get(id));
				}
				}catch ( Exception e)
				{
					e.printStackTrace();
					
				}
				
			}
			players.remove(id);
			if ( sendlogout )
				BroadCastToLoggedIn(String.format("REMPLAYER %d",id));
		}finally{
			if ( lock )
				playerlistlock.unlock();
			
		}
	}
	public class AlreadyLoggedInException extends Exception
	{
		
		
	}
	public PlayerInfo LoginPlayer(Player pl,String username , String password ) throws AlreadyLoggedInException
	{
		System.out.println("Login "+pl+" "+username+" "+password);
		try {
			loginstat.setString(1, username);
			loginstat.setString(2, password);
			ResultSet R = loginstat.executeQuery();
			if (!R.next())
				return null;
			else
			{
				PlayerInfo info = new PlayerInfo();
				info.nickname = R.getString(2);
				info.gameswon = R.getInt(3);
				info.gameslost = R.getInt(4);
				info.dbid = R.getInt(1);
				if ( !loggedinaccounts.contains(info.dbid))
				{
					loggedinaccounts.add(info.dbid);
					BroadCastToLoggedIn(String.format("ADDPLAYER %d %s %d %d",pl._id,info.nickname,info.gameswon,info.gameslost));
					return info;
				}else{
					throw new AlreadyLoggedInException();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public void BroadCastToLoggedIn(String line)
	{
		Collection<Player> coll = players.values();
		Iterator<Player> itr = coll.iterator();
		while(itr.hasNext())
		{
			Player pl = itr.next();
			if ( pl.info != null )
				pl.SendLine(line);
		}
	}
	public void BroadCastToAll(String line)
	{
		Collection<Player> coll = players.values();
		Iterator<Player> itr = coll.iterator();
		while(itr.hasNext())
		{
			Player pl = itr.next();
			pl.SendLine(line);
		}
	}
	public void SendState(Player player) {
		Collection<Player> coll = players.values();
		Iterator<Player> itr = coll.iterator();
		while(itr.hasNext())
		{
			Player pl = itr.next();
			if ( pl != player && pl.info != null)
			{
				player.SendLine(String.format("ADDPLAYER %d %s %d %d",pl._id,pl.info.nickname,pl.info.gameswon,pl.info.gameslost));
			}
		}
		Collection<Gioco> coll2 = games.values();
		Iterator<Gioco> itr2 = coll2.iterator();
		while(itr2.hasNext())
		{
			Gioco g = itr2.next();
			g.SendCurrentState(player);
		}
		
	}
	public void OpenGame(Player master,int numplayers,String desc)
	{
		if (master.currgame != -1)
		{
			master.SendError("You are already hosting a game!");
			return;
		}
		gameidcount++;
		if ( numplayers != 2 && numplayers != 4 )
		{
			master.SendError("Invalid player count!");
			return;
		}
		games.put(gameidcount,new Gioco(this,master,numplayers,gameidcount,desc));
		master.currgame = gameidcount;
	}
	public void CloseGame(int id)
	{
		BroadCastToLoggedIn(games.get(id).ForgeGameClosed());
		games.get(id).master.currgame = -1;
		games.get(id).OnClose();
		games.remove(id);
		
		
	}
	static String join(String[] s, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     for ( int i = 0; i < s.length; i++)
	     {
	    	builder.append(s[i]);
	    	if ( i+1 != s.length )
	    		builder.append(delimiter);
	    	 
	     }
	     return builder.toString();
	 }
}
