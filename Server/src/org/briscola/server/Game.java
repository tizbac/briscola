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
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
public class Game {
	public Vector<Card> cards = new Vector<Card>();
	public Vector<Player> players = new Vector<Player>();
	Server srv;
	int id;
	String desc;
	int numplayers;
	Player master;
	Match p = null;
	boolean started = false;
	public Game(Server _srv,Player _master,int _numplayers,int _id,String _desc) {
		srv = _srv;
		int x = 0 ;
		id = _id;
		desc = _desc;
		numplayers = _numplayers;
		master = _master;
    	for ( int s = 0; s < 4; s++)
        {
        	for ( int i = 1; i < 11; i++)
        	{
        		switch (i)
        		{
        		case 1:
        			cards.add(new Card(s,11,"Asso",10,s*10+0));
        			break;
        		case 3:
        			cards.add(new Card(s,10,"Tre",9,s*10+2));
        			break;
        		case 2:
        			cards.add(new Card(s,0,"Due",1,s*10+1));
        			break;
        		case 4:
        			cards.add(new Card(s,0,"Quattro",2,s*10+3));
        			break;	
        		case 5:
        			cards.add(new Card(s,0,"Cinque",3,s*10+4));
        			break;
        		case 6:
        			cards.add(new Card(s,0,"Sei",4,s*10+5));
        			break;
        		case 7:
        			cards.add(new Card(s,0,"Sette",5,s*10+6));
        			break;
        		case 8:
        			cards.add(new Card(s,2,"Fante",6,s*10+7));
        			break;
        		case 9:
        			cards.add(new Card(s,3,"Cavallo",7,s*10+8));
        			break;
        		case 10:
        			cards.add(new Card(s,4,"Re",8,s*10+9));
        			break;
        		
        		}
        		x++;
        		
        	}
        }
    	Random rnd = new Random();
    	for ( int i = 0; i < cards.size(); i++)
    	{
    		int i1;
    		int i2;
    		i1 = rnd.nextInt(cards.size()-1);
    		i2 = rnd.nextInt(cards.size()-1);
    		Card app;
    		app = cards.get(i1);
    		cards.set(i1, cards.get(i2));
    		cards.set(i2, app);
    		
    		
    	}
    	players.add(master);
    	srv.BroadCastToLoggedIn(ForgeGameOpened());
    	for ( int i = 0; i < players.size(); i++)
    		srv.BroadCastToLoggedIn(ForgePlayerJoined(players.get(i)));
    	
	}
	/**
	 * Returns the string containing the command to make clients aware that this game has been opened
	 * @return the needed command to notify game opening
	 * @see String
	 */
	String ForgeGameOpened()
	{
		return String.format("GO %d %d %d %s", id,master._id,numplayers,desc);
	}
	/**
	 * Returns the string containing the command to make clients aware that the game has been closed
	 * @return the needed command to notify game close
	 * @see String
	 */
	String ForgeGameClosed()
	{
		return String.format("GC %d", id);
	}
	/**
	 * Returns the string containing the command to make clients aware that a player joined this game
	 * @param p The Player who joined the game
	 * @return the needed command to notify game join
	 * @see String
	 */
	String ForgePlayerJoined(Player p)
	{
		return String.format("JG %d %d",id,p._id);
	}
	/**
	 * Returns the string containing the command to make clients aware that a player left this game
	 * @param p The Player who left the game
	 * @return the needed command to notify player leaving the game
	 * @see String
	 */
	String ForgePlayerLeft(Player p)
	{
		return String.format("LG %d %d",id,p._id);
	}
	/**
	 * Removes references to this game from players
	 */
	void OnClose()
	{
		for ( int i = 0; i < players.size(); i++ )
			players.get(i).currgame = -1;
		
	}
	/**
	 * Sends the current game state to a new player that just joined the game
	 * @param pdest The player that needs initial sync
	 */
	void SendCurrentState(Player pdest)
	{
		pdest.SendLine(ForgeGameOpened());
		for ( int i = 0; i < players.size(); i++)
			pdest.SendLine(ForgePlayerJoined(players.get(i)));
	}
	/**
	 * Adds a player to the game and broadcasts to the whole server the event
	 * @param p The player who just joined
	 */
	void AddPlayer(Player p)
	{
		srv.BroadCastToLoggedIn(ForgePlayerJoined(p));
		players.add(p);
		p.currgame = id;
	}
	/**
	 * Sends a command only to the players of this game
	 * @param msg Command to be sent
	 */
	void BroadCast(String msg)
	{
		for ( int i = 0; i < players.size(); i++ )
			players.get(i).SendLine(msg);
		
	}
	/**
	 * Removes a player from the game , broadcasting the event to the whole server, it will also remove references
	 * @param pl Player to be removed
	 */
	void RemovePlayer(Player pl)
	{
		srv.BroadCastToLoggedIn(ForgePlayerLeft(pl));
		players.removeElement(pl);
		pl.currgame = -1;
		if ( p != null )
		{
			p.UnLinkPlayer(pl);
			
			
		}
	}
	/**
	 * Launches the game by creating the actual Match and broadcasts to the players inside the game the event
	 */
	void StartGame()
	{
		started = true;
		Player players_s[] = new Player[players.size()];
		players.toArray(players_s);
		p = new Match(cards, players_s, id,srv);
		p.Broadcast("GS "+p.id);//Game started
	}
	/**
	 * Handles the very simple chat service of this game, it takes a message from a client and it will broadcast it to all players of the game
	 * @param pl The player who sent the chat message
	 * @param msg The message
	 */
	void OnGameChat(Player pl , String msg)
	{
		BroadCast(String.format("GAMECHAT %d %s", pl._id,msg));
	}
	/**
	 * Retrieves a BriscolaPlayer from a generic logged in account
	 * @param pl The generic logged in account
	 * @return The BriscolaPlayer linked to the Player , null if not found in this game
	 * @see BriscolaPlayer
	 */
	BriscolaPlayer GetGamePlayer(Player pl)
	{
		for ( int i = 0; i < players.size(); i++)
		{
			if ( players.get(i) == pl )
				return p.players.get(i);
		}
		return null;
	}
	
	
}
