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
/**
 * Class representing the whole Server instance, it handles incoming connections and global player list updates
 * @author Tiziano Bacocco
 *
 */
public class Server extends Thread {
	/**The server socket , usually bound to port 3112 */
	private ServerSocket mainSock;
	/**Progressive id counter used to generate runtime-unique IDs*/
	int idcount;
	/**Progressive game id counter , used to generate runtime-unique game IDs*/
	int gameidcount = 0;
	/**
	 * Since connections are accepted on another thread , this is required to avoid that a player is added while an update is taking place
	 */
	private Lock playerlistlock = new Lock();
	/** Runnable implementation of the thread used to update Players and games */
	UpdateThreadRunnable updth;
	/** Thread-Safe Associative array holding ID-Player pairs */
	private ConcurrentHashMap<Integer, Player> players;
	/** A vector holding currently online accounts, it is used to login of already connected user */
	private Vector<Integer> loggedinaccounts;
	/** An associative array holding GameID-Game pairs @see Game*/
	public HashMap<Integer,Game> games = new HashMap<Integer,Game>();
	/** Connection to MYSQL Database , it'll be initialized later */
	Connection conn = null;
	/** Query used to check if password is correct and then to load all player data */
	String loginquery = "SELECT ID,user_login,user_pass FROM wp_users WHERE user_login = ? LIMIT 1";
	/** Query used to add a new record on game history table, this data will be used later to do statistical analysis */
	String gamehistory_add_query = "INSERT INTO history (player,playercount,won,points) VALUES ( ?,?,?,?)";
	/** Query used to set account statistics */
	String updateaccountquery = "UPDATE players SET gameswon = ? , gameslost = ? WHERE id = ? LIMIT 1";
	/** Query used to keep alive connection to database server */
	String pingquery = "SELECT 1";
	PreparedStatement pingstat;
	PreparedStatement loginstat;
	PreparedStatement updatestat;
	PreparedStatement historystat;
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
			conn = DriverManager.getConnection("jdbc:mysql://localhost/"+setts.database,setts.username,setts.password);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.err.println("Cannot connect to database.\n");
			System.exit(1);
			

		}
		try {
			loginstat = conn.prepareStatement(loginquery);
			//updatestat = conn.prepareStatement(updateaccountquery);
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
	/**
	 * Exception raised when there's a failure removing a client
	 * @author Tiziano Bacocco
	 *
	 */
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
	/**
	 * Thread used to update clients ( receive data , timeout and send data )
	 * @author Tiziano Bacocco
	 *
	 */
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
	/**
	 * Entry point of the thread used to accept incoming connections
	 */
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
	/**
	 * Removes a player from list and notifies all authenticated clients
	 * @param id The identifier of the client to remove
	 * @param lock true if the operations is being done from a thread other than acceptor thread or if the lock is not already taken
	 * @throws RemoveClientException
	 */
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
				/*try {
					updatestat.setInt(1, players.get(id).info.gameswon );
					updatestat.setInt(2, players.get(id).info.gameslost );
					updatestat.setInt(3, players.get(id).info.dbid );
					updatestat.execute();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
				loggedinaccounts.removeElement(players.get(id).info.dbid);
				try{
				if ( players.get(id).currgame != -1 )
				{
					if ( games.get(players.get(id).currgame).master == players.get(id) )
						CloseGame(players.get(id).currgame);
					else
						games.get(players.get(id).currgame).RemovePlayer(players.get(id));
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
	/**
	 * Exception raised when a player is already connected
	 * @author Tiziano Bacocco
	 *
	 */
	public class AlreadyLoggedInException extends Exception
	{
		
		
	}
	public void AddHistory(Player pl,int players, boolean won, int points)
	{
		try {
			historystat.setLong(1, pl.info.dbid);
			historystat.setLong(2, players);
			historystat.setBoolean(3, won);
			historystat.setLong(4, points);
			historystat.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Login a player to the server with provided username and password
	 * @param pl The client to be authenticated
	 * @param username Username of the client
	 * @param password MD5 hash of the password
	 * @return non-null if login succeeds
	 * @throws AlreadyLoggedInException
	 */
	public PlayerInfo LoginPlayer(Player pl,String username , String password ) throws AlreadyLoggedInException
	{
		//System.out.println("Login "+pl+" "+username+" "+password);
		try {
			loginstat.setString(1, username);
			ResultSet R = loginstat.executeQuery();
			if (!R.next())
				return null;
			else
			{
				String passhash = R.getString("user_pass");
				PasswordHasher ph = new PasswordHasher();
				System.out.println("Check against "+passhash);
				if ( !ph.isMatch(password, passhash) )
					return null;
				PlayerInfo info = new PlayerInfo();
				info.nickname = R.getString("user_login");
				info.gameswon = 0;
				info.gameslost = 0;
				info.dbid = R.getInt("ID");
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
	/**
	 * Send a command to all authenticated clients
	 * @param line The command to be sent
	 */
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
	/**
	 * Send a command to all ( non authenticated too ) clients
	 * @param line The command to be sent
	 */
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
	/**
	 * Send current player and game lists to a new client that just authenticated
	 * @param player Player that needs initial sync
	 */
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
		Collection<Game> coll2 = games.values();
		Iterator<Game> itr2 = coll2.iterator();
		while(itr2.hasNext())
		{
			Game g = itr2.next();
			g.SendCurrentState(player);
		}
		
	}
	/**
	 * Try to open a game with a master
	 * @param master The player who creates the game, if he disconnects , the game will close
	 * @param numplayers can be 2 or 4 for now
	 * @param desc The title of the game , provided by client
	 */
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
		games.put(gameidcount,new Game(this,master,numplayers,gameidcount,desc));
		master.currgame = gameidcount;
	}
	/**
	 * Close an existing game
	 * @param id The identifier of the game to close
	 */
	public void CloseGame(int id)
	{
		BroadCastToLoggedIn(games.get(id).ForgeGameClosed());
		games.get(id).master.currgame = -1;
		games.get(id).OnClose();
		games.remove(id);
		
		
	}
	/**
	 * General purpose function that takes an array and concatenates the elements putting the specified separator between them
	 * @param s The string array to concatenate
	 * @param delimiter The delimiter to but between elements
	 * @return The resulting string after concatenation
	 */
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
