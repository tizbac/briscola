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
import java.util.Vector;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;


public class Match {
	Vector<Card> deck;
	Vector<BriscolaPlayer> players;
	Vector<Card> table;
	Card regnante;
	int turn;
	boolean game_ended;
	ReentrantLock tablelock;
	Server srv;
	int usedcards = 0;
	public int id;
	public Match(Vector<Card> match_deck,Player[] match_players,int _id,Server _srv)
	{
		id = _id;
		srv = _srv;
		game_ended = false;
		tablelock = new ReentrantLock();
		players = new Vector<BriscolaPlayer>();
		table = new Vector<Card>();
		for ( int i = 0; i < match_players.length; i++ )
		{
			players.add( new BriscolaPlayer(this,match_players[i].info.nickname,i,match_players[i]));
		}
		deck = (Vector<Card>) match_deck.clone();
		for ( int i = 0; i < players.size(); i++)
		{
			try {
				players.get(i).drawcard();
				players.get(i).drawcard();
				players.get(i).drawcard();
			}catch ( BriscolaPlayer.ActionNotPermittedException e)
			{
				System.err.printf("Impossibile dare le carte al giocatore %s",match_players[i].info.nickname);
			}
			SendUpdateHand(i);
		}
		regnante = deck.get(0);
		deck.remove(0);
		SendR();
		turn = 0;
		SendNP();
		SendGameIDs();
		SendUpdatecardcounts();
		
	}
	public void Broadcast(String line)
	{
		for ( int i = 0; i < players.size(); i++)
		{
			if ( players.get(i).pl != null )
				players.get(i).pl.SendLine(line);
		}
	}
	public void Broadcastexc(String line,Player pl)
	{
		for ( int i = 0; i < players.size(); i++)
		{
			if ( !( players.get(i).pl == pl) )
				players.get(i).pl.SendLine(line);
		}
	}
	public void ReplacePlayer(int plindex, Player newplayer)
	{
		BriscolaPlayer gioc = players.get(plindex);
		gioc.pl = newplayer;
		srv.games.get(id).AddPlayer(newplayer);
		SendUpdateHand(plindex);
		SendUpdateTable();
		SendUpdatecardcounts();
		SendR();
		SendNP();
		SendPlayerChanged(plindex, newplayer);
		SendGameIDs();
		UpdateScore();
		newplayer.SendLine("GS "+id);
	}
	public void SendUpdateHand(int plindex)
	{
		Player p = players.get(plindex).pl;
		if ( p == null )
			return;
		BriscolaPlayer gioc = players.get(plindex);
		StringBuffer buf = new StringBuffer();
		buf.append("HAND ");
		for (int i = 0; i < gioc.hand.size(); i++)
			buf.append(gioc.hand.get(i).protocolrepr()+ (i+1 < gioc.hand.size() ? " " : ""));
		p.SendLine(buf.toString());
	}
	public void SendUpdateTable()
	{
		
		StringBuffer buf = new StringBuffer();
		buf.append("T ");
		for (int i = 0; i < table.size(); i++)
			buf.append(table.get(i).protocolrepr()+ (i+1 < table.size() ? " " : ""));
		Broadcast(buf.toString());
		
	}
	public void SendR()
	{
		Broadcast("R "+regnante.protocolrepr());
	}
	public void SendPlayerChanged(int plindex,Player newplayer)
	{
		Broadcast("PC "+plindex+" "+newplayer._id);
		
	}
	public void SendNP()//Turno
	{
		if ( players.get(turn).pl != null ) // Il turno verrà inviato al rejoin del giocatore nel caso è null
			Broadcast("NP "+players.get(turn).pl._id);
	}
	public void SendTableTaken(BriscolaPlayer gioc)
	{
		if ( gioc.pl != null )
			Broadcast("TT "+gioc.pl._id);
		else
		{
			for ( int i = 0; i < players.size(); i++ )//Manda l'animazione delle carte a un giocatore qualsiasi presente
			{
				if ( players.get(i).pl != null )
				{
					Broadcast("TT "+players.get(i).pl._id);
					break;
				}
			}
			
			
		}
	}
	public void SendGameIDs()
	{
		
		for ( int i = 0; i < players.size(); i++ )
		{
			if ( players.get(i).pl != null )
				Broadcast("PID "+players.get(i).pl._id+" "+i);
		}
	}
	public void SendUpdatecardcounts()
	{
		for ( int b = 0; b < players.size(); b++)
		{
			for ( int i = 0; i < players.size(); i++)
			{
				int srvid = players.get(i).id;
				if ( i != b && players.get(b).pl != null )
					players.get(b).pl.SendLine("CN "+srvid+" "+players.get(i).hand.size());
			}
		}
		Broadcast("DN "+deck.size());
	}
	public void UpdateScore()
	{
		for ( int b = 0; b < players.size(); b++)
		{
			if ( players.get(b).pl != null )
				Broadcast("SC "+players.get(b).pl._id+" "+players.get(b).points());
		}
	}
	synchronized public int dotake()
	{
		int x = calculate_take();// Quello che prende tutto
		for ( int i = 0; i < table.size(); i++)
		{
			Card c = table.get(i);
			players.get(x).taken.add(c);
			
			
		}
		SendTableTaken(players.get(x));
		table.clear();
		SendUpdateTable();
		for ( int i = 0; i < players.size(); i++)
		{
			try {
				players.get(i).drawcard();
			} catch (BriscolaPlayer.ActionNotPermittedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		turn = x;
		SendNP();
		usedcards = 0;
		int tot = 0;
		int maxpoints = -1;
		int maxplayer =-1;
		
		for ( int i = 0; i < players.size(); i++)
		{
			tot += players.get(i).hand.size();
			if ( players.get(i).points() > maxpoints )
			{
				maxpoints = players.get(i).points();
				maxplayer = i;
				
			}
		}
		UpdateScore();
		if ( tot == 0 )
		{
			Broadcast("WON "+players.get(maxplayer).pl._id);
			players.get(maxplayer).pl.WonGame(this,players.get(maxplayer).points());
		
			for ( int i = 0; i < players.size(); i++)
			{
				if ( i != maxplayer && players.get(i).pl != null)
					players.get(i).pl.LostGame(this,players.get(i).points());
			}
		}
		return x;
	}
	public int strongest_briscola()
	{
		int forzamax = -1;
		int cartamax = -1;
		for ( int i = 0; i < table.size(); i++ )
		{
			if ( table.get(i).seed == regnante.seed && table.get(i).strength > forzamax )
			{
				forzamax = table.get(i).strength;
				cartamax = i;
			}
		}
		return cartamax;
	}
	public int strongest_card(int seed)
	{
		int s_max = -1;
		int c_max = -1;
		for ( int i = 0; i < table.size(); i++ )
		{
			if ( table.get(i).seed == seed && table.get(i).strength > s_max )
			{
				s_max = table.get(i).strength;
				c_max = i;
			}
		}
		return c_max;
	}
	public int calculate_take()
	{
		//Trova chi deve prendere le carte buttate
		if ( table.size() == 0) // Non dovrebbe mai succedere
			return 0;
		int main_seed = table.get(0).seed;
		int bpf = strongest_briscola(); // Briscola più forte
		int cpf = strongest_card(main_seed); // Carta più forte
		assert ( cpf != -1 );
		if ( bpf > -1 )
			return table.get(bpf).owner;
		return table.get(cpf).owner;
	}
	public int GetFirstEmptySlot() {
		for ( int i = 0; i < players.size(); i++)
		{
			System.out.println("giocatori.get(i).pl ="+players.get(i).pl);
			System.out.println("srv.games.get(id) ="+srv.games.get(id));
			if ( !srv.games.get(id).players.contains(players.get(i).pl))
				return i;
			
		}
		return -1;
	}
	public void UnLinkPlayer(Player pl) {
		for ( int i = 0; i < players.size(); i++)
		{
			if ( players.get(i).pl == pl )
				players.get(i).pl = null;
		
		}
		
	}
}
