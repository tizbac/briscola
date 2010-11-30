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
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
public class Gioco {
	public Vector<Carta> carte = new Vector<Carta>();
	public Vector<Player> players = new Vector<Player>();
	Server srv;
	int id;
	String desc;
	int numplayers;
	Player master;
	Partita p = null;
	boolean avviato = false;
	public Gioco(Server _srv,Player _master,int _numplayers,int _id,String _desc) {
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
        			carte.add(new Carta(s,11,"Asso",10,s*10+0));
        			break;
        		case 3:
        			carte.add(new Carta(s,10,"Tre",9,s*10+2));
        			break;
        		case 2:
        			carte.add(new Carta(s,0,"Due",1,s*10+1));
        			break;
        		case 4:
        			carte.add(new Carta(s,0,"Quattro",2,s*10+3));
        			break;	
        		case 5:
        			carte.add(new Carta(s,0,"Cinque",3,s*10+4));
        			break;
        		case 6:
        			carte.add(new Carta(s,0,"Sei",4,s*10+5));
        			break;
        		case 7:
        			carte.add(new Carta(s,0,"Sette",5,s*10+6));
        			break;
        		case 8:
        			carte.add(new Carta(s,2,"Fante",6,s*10+7));
        			break;
        		case 9:
        			carte.add(new Carta(s,3,"Cavallo",7,s*10+8));
        			break;
        		case 10:
        			carte.add(new Carta(s,4,"Re",8,s*10+9));
        			break;
        		
        		}
        		x++;
        		
        	}
        }
    	Random rnd = new Random();
    	for ( int i = 0; i < carte.size(); i++)
    	{
    		int i1;
    		int i2;
    		i1 = rnd.nextInt(carte.size()-1);
    		i2 = rnd.nextInt(carte.size()-1);
    		Carta app;
    		app = carte.get(i1);
    		carte.set(i1, carte.get(i2));
    		carte.set(i2, app);
    		
    		
    	}
    	players.add(master);
    	srv.BroadCastToLoggedIn(ForgeGameOpened());
    	for ( int i = 0; i < players.size(); i++)
    		srv.BroadCastToLoggedIn(ForgePlayerJoined(players.get(i)));
    	
	}
	String ForgeGameOpened()
	{
		return String.format("GO %d %d %d %s", id,master._id,numplayers,desc);
	}
	String ForgeGameClosed()
	{
		return String.format("GC %d", id);
	}
	String ForgePlayerJoined(Player p)
	{
		return String.format("JG %d %d",id,p._id);
	}
	String ForgePlayerLeft(Player p)
	{
		return String.format("LG %d %d",id,p._id);
	}
	void OnClose()
	{
		for ( int i = 0; i < players.size(); i++ )
			players.get(i).currgame = -1;
		
	}
	void SendCurrentState(Player pdest)
	{
		pdest.SendLine(ForgeGameOpened());
		for ( int i = 0; i < players.size(); i++)
			pdest.SendLine(ForgePlayerJoined(players.get(i)));
	}
	void AddPlayer(Player p)
	{
		srv.BroadCastToLoggedIn(ForgePlayerJoined(p));
		players.add(p);
		p.currgame = id;
	}
	void RemPlayer(Player p)
	{
		srv.BroadCastToLoggedIn(ForgePlayerLeft(p));
		players.removeElement(p);
		p.currgame = -1;
	}
	void AvviaPartita()
	{
		avviato = true;
		Player giocatori_s[] = new Player[players.size()];
		players.toArray(giocatori_s);
		p = new Partita(carte, giocatori_s, id,srv);
		p.Broadcast("GS "+p.id);//Game started
	}
	GiocatoreBriscola GetGamePlayer(Player pl)
	{
		for ( int i = 0; i < players.size(); i++)
		{
			if ( players.get(i) == pl )
				return p.giocatori.get(i);
		}
		return null;
	}
	
	
}
