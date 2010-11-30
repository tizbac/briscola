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
public class GiocatoreBriscola {
	Vector<Carta> inmano;
	Vector<Carta> prese;
	int id;
	Partita part;
	String nome;
	Player pl;
	public class ActionNotPermittedException extends Exception
	{
		
		
		
	}
	public GiocatoreBriscola(Partita inst,String n,int id_,Player _pl)
	{
		part = inst;
		nome = n;
		id = id_;
		pl = _pl;
		inmano = new Vector<Carta>();
		prese = new Vector<Carta>();
	}
	public void SendPoints()
	{
		pl.SendLine("P "+id+" "+punti());
	}
	synchronized public void pescacarta() throws ActionNotPermittedException
	{
		Carta c;
		if ( inmano.size() == 3)
			throw new ActionNotPermittedException();
		if ( part.mazzo.size() == 0 && part.regnante.possessore == -1)
		{
			c = part.regnante;
			part.regnante.possessore = id;
		}
		else if ( part.mazzo.size() > 0)
		{
			c = part.mazzo.get(0);
			part.mazzo.remove(0);
		}else
		{
			return;
			
		}
		c.possessore = id;
		
		inmano.add(c);
		part.SendUpdateHand(id);
		part.SendUpdatecardcounts();
	}
	public int punti()
	{
		int p = 0;
		for ( int i = 0; i < prese.size(); i++)
			p += prese.get(i).punti;
		return p;
	}
	synchronized public void prenditutto() throws ActionNotPermittedException
	{
		if ( part.tavolo.size() != part.giocatori.size() )
			throw new ActionNotPermittedException();
		while (part.tavolo.size() > 0)
		{
			prese.add(part.tavolo.get(0));
			part.tavolo.remove(0);
			
		}
		part.SendTableTaken(this);
		SendPoints();
		part.SendUpdateTable();
		/*pescacarta();
		if ( id == 0)
			part.giocatori.get(1).pescacarta();
		else
			part.giocatori.get(0).pescacarta();*/
		
	}
	synchronized public void butta(int pos) throws ActionNotPermittedException
	{
		if ( part.turno != id)
			throw new ActionNotPermittedException();
		Carta c = inmano.get(pos);
		
		inmano.remove(pos);
		part.tavolo.add(c);
		part.turno++;
		part.turno = part.turno % part.giocatori.size();
		part.SendNP();
		part.SendUpdateHand(id);
		part.SendUpdateTable();
		part.SendUpdatecardcounts();
		part.cartebuttate++;
		if ( part.cartebuttate == part.giocatori.size() )// Tutti hanno giocato
		{
			part.eseguipresa();
		}
	}
}
