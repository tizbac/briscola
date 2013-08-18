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
public class BriscolaPlayer {
	Vector<Card> hand;
	Vector<Card> taken;
	int id;
	Match match;
	String name;
	Player pl;
	public class ActionNotPermittedException extends Exception
	{
		
		
		
	}
	public BriscolaPlayer(Match inst,String n,int id_,Player _pl)
	{
		match = inst;
		name = n;
		id = id_;
		pl = _pl;
		hand = new Vector<Card>();
		taken = new Vector<Card>();
	}
	public void SendPoints()
	{
		pl.SendLine("P "+id+" "+points());
	}
	synchronized public void drawcard() throws ActionNotPermittedException
	{
		Card c;
		if ( hand.size() == 3)
			throw new ActionNotPermittedException();
		if ( match.deck.size() == 0 && match.regnante.owner == -1)
		{
			c = match.regnante;
			match.regnante.owner = id;
		}
		else if ( match.deck.size() > 0)
		{
			c = match.deck.get(0);
			match.deck.remove(0);
		}else
		{
			return;
			
		}
		c.owner = id;
		
		hand.add(c);
		match.SendUpdateHand(id);
		match.SendUpdatecardcounts();
	}
	public int points()
	{
		int p = 0;
		for ( int i = 0; i < taken.size(); i++)
			p += taken.get(i).points;
		return p;
	}
	synchronized public void takeall() throws ActionNotPermittedException
	{
		if ( match.table.size() != match.players.size() )
			throw new ActionNotPermittedException();
		while (match.table.size() > 0)
		{
			taken.add(match.table.get(0));
			match.table.remove(0);
			
		}
		match.SendTableTaken(this);
		SendPoints();
		match.SendUpdateTable();
		/*pescacarta();
		if ( id == 0)
			part.giocatori.get(1).pescacarta();
		else
			part.giocatori.get(0).pescacarta();*/
		
	}
	synchronized public void throw_card(int pos) throws ActionNotPermittedException
	{
		if ( match.turn != id)
			throw new ActionNotPermittedException();
		Card c = hand.get(pos);
		
		hand.remove(pos);
		match.table.add(c);
		match.turn++;
		match.turn = match.turn % match.players.size();
		match.SendNP();
		match.SendUpdateHand(id);
		match.SendUpdateTable();
		match.SendUpdatecardcounts();
		match.usedcards++;
		if ( match.usedcards == match.players.size() )// Tutti hanno giocato
		{
			match.dotake();
		}
	}
}
