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


public class Partita {
	Vector<Carta> mazzo;
	Vector<GiocatoreBriscola> giocatori;
	Vector<Carta> tavolo;
	Carta regnante;
	int turno;
	boolean finita;
	ReentrantLock locktavolo;
	Server srv;
	int cartebuttate = 0;
	public int id;
	public Partita(Vector<Carta> carte,Player[] giocatori_s,int _id,Server _srv)
	{
		id = _id;
		srv = _srv;
		finita = false;
		locktavolo = new ReentrantLock();
		giocatori = new Vector<GiocatoreBriscola>();
		tavolo = new Vector<Carta>();
		for ( int i = 0; i < giocatori_s.length; i++ )
		{
			giocatori.add( new GiocatoreBriscola(this,giocatori_s[i].info.nickname,i,giocatori_s[i]));
		}
		mazzo = (Vector<Carta>) carte.clone();
		for ( int i = 0; i < giocatori.size(); i++)
		{
			try {
				giocatori.get(i).pescacarta();
				giocatori.get(i).pescacarta();
				giocatori.get(i).pescacarta();
			}catch ( GiocatoreBriscola.ActionNotPermittedException e)
			{
				System.err.printf("Impossibile dare le carte al giocatore %s",giocatori_s[i].info.nickname);
			}
			SendUpdateHand(i);
		}
		regnante = mazzo.get(0);
		mazzo.remove(0);
		SendR();
		turno = 0;
		SendNP();
		SendGameIDs();
		SendUpdatecardcounts();
		
	}
	public void Broadcast(String line)
	{
		for ( int i = 0; i < giocatori.size(); i++)
		{
			if ( giocatori.get(i).pl != null )
				giocatori.get(i).pl.SendLine(line);
		}
	}
	public void Broadcastexc(String line,Player pl)
	{
		for ( int i = 0; i < giocatori.size(); i++)
		{
			if ( !( giocatori.get(i).pl == pl) )
				giocatori.get(i).pl.SendLine(line);
		}
	}
	public void ReplacePlayer(int plindex, Player newplayer)
	{
		GiocatoreBriscola gioc = giocatori.get(plindex);
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
		Player p = giocatori.get(plindex).pl;
		if ( p == null )
			return;
		GiocatoreBriscola gioc = giocatori.get(plindex);
		StringBuffer buf = new StringBuffer();
		buf.append("HAND ");
		for (int i = 0; i < gioc.inmano.size(); i++)
			buf.append(gioc.inmano.get(i).protocolrepr()+ (i+1 < gioc.inmano.size() ? " " : ""));
		p.SendLine(buf.toString());
	}
	public void SendUpdateTable()
	{
		
		StringBuffer buf = new StringBuffer();
		buf.append("T ");
		for (int i = 0; i < tavolo.size(); i++)
			buf.append(tavolo.get(i).protocolrepr()+ (i+1 < tavolo.size() ? " " : ""));
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
		if ( giocatori.get(turno).pl != null ) // Il turno verrà inviato al rejoin del giocatore nel caso è null
			Broadcast("NP "+giocatori.get(turno).pl._id);
	}
	public void SendTableTaken(GiocatoreBriscola gioc)
	{
		if ( gioc.pl != null )
			Broadcast("TT "+gioc.pl._id);
		else
		{
			for ( int i = 0; i < giocatori.size(); i++ )//Manda l'animazione delle carte a un giocatore qualsiasi presente
			{
				if ( giocatori.get(i).pl != null )
				{
					Broadcast("TT "+giocatori.get(i).pl._id);
					break;
				}
			}
			
			
		}
	}
	public void SendGameIDs()
	{
		
		for ( int i = 0; i < giocatori.size(); i++ )
		{
			if ( giocatori.get(i).pl != null )
				Broadcast("PID "+giocatori.get(i).pl._id+" "+i);
		}
	}
	public void SendUpdatecardcounts()
	{
		for ( int b = 0; b < giocatori.size(); b++)
		{
			for ( int i = 0; i < giocatori.size(); i++)
			{
				int srvid = giocatori.get(i).id;
				if ( i != b && giocatori.get(b).pl != null )
					giocatori.get(b).pl.SendLine("CN "+srvid+" "+giocatori.get(i).inmano.size());
			}
		}
		Broadcast("DN "+mazzo.size());
	}
	public void UpdateScore()
	{
		for ( int b = 0; b < giocatori.size(); b++)
		{
			if ( giocatori.get(b).pl != null )
				Broadcast("SC "+giocatori.get(b).pl._id+" "+giocatori.get(b).punti());
		}
	}
	synchronized public int eseguipresa()
	{
		int x = calcolapresa();// Quello che prende tutto
		for ( int i = 0; i < tavolo.size(); i++)
		{
			Carta c = tavolo.get(i);
			giocatori.get(x).prese.add(c);
			
			
		}
		SendTableTaken(giocatori.get(x));
		tavolo.clear();
		SendUpdateTable();
		for ( int i = 0; i < giocatori.size(); i++)
		{
			try {
				giocatori.get(i).pescacarta();
			} catch (GiocatoreBriscola.ActionNotPermittedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		turno = x;
		SendNP();
		cartebuttate = 0;
		int tot = 0;
		int maxpunti = -1;
		int maxgioc =-1;
		
		for ( int i = 0; i < giocatori.size(); i++)
		{
			tot += giocatori.get(i).inmano.size();
			if ( giocatori.get(i).punti() > maxpunti )
			{
				maxpunti = giocatori.get(i).punti();
				maxgioc = i;
				
			}
		}
		UpdateScore();
		if ( tot == 0 )
		{
			Broadcast("WON "+giocatori.get(maxgioc).pl._id);
			giocatori.get(maxgioc).pl.WonGame();
		}
		for ( int i = 0; i < giocatori.size(); i++)
		{
			if ( i != maxgioc && giocatori.get(i).pl != null)
				giocatori.get(i).pl.LostGame();
		}
		return x;
	}
	public int briscolapiuforte()
	{
		int forzamax = -1;
		int cartamax = -1;
		for ( int i = 0; i < tavolo.size(); i++ )
		{
			if ( tavolo.get(i).seme == regnante.seme && tavolo.get(i).forza > forzamax )
			{
				forzamax = tavolo.get(i).forza;
				cartamax = i;
			}
		}
		return cartamax;
	}
	public int cartapiuforte(int seme)
	{
		int forzamax = -1;
		int cartamax = -1;
		for ( int i = 0; i < tavolo.size(); i++ )
		{
			if ( tavolo.get(i).seme == seme && tavolo.get(i).forza > forzamax )
			{
				forzamax = tavolo.get(i).forza;
				cartamax = i;
			}
		}
		return cartamax;
	}
	public int calcolapresa()
	{
		//Trova chi deve prendere le carte buttate
		if ( tavolo.size() == 0) // Non dovrebbe mai succedere
			return 0;
		int semeprincipale = tavolo.get(0).seme;
		int bpf = briscolapiuforte(); // Briscola più forte
		int cpf = cartapiuforte(semeprincipale); // Carta più forte
		assert ( cpf != -1 );
		if ( bpf > -1 )
			return tavolo.get(bpf).possessore;
		return tavolo.get(cpf).possessore;
	}
	public int GetFirstEmptySlot() {
		for ( int i = 0; i < giocatori.size(); i++)
		{
			System.out.println("giocatori.get(i).pl ="+giocatori.get(i).pl);
			System.out.println("srv.games.get(id) ="+srv.games.get(id));
			if ( !srv.games.get(id).players.contains(giocatori.get(i).pl))
				return i;
			
		}
		return -1;
	}
	public void UnLinkPlayer(Player pl) {
		for ( int i = 0; i < giocatori.size(); i++)
		{
			if ( giocatori.get(i).pl == pl )
				giocatori.get(i).pl = null;
		
		}
		
	}
}
