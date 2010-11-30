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
public class Carta {
	int seme;
	int punti;
	String nome;
	
	int forza;
	int possessore;
	int id;
	Carta(int seme_,int punti_,String nome_,int forza_,int i)
	{
		seme = seme_;
		punti = punti_;
		nome = nome_;
		forza = forza_;
		possessore = -1;
		id = i;
	}
	public String protocolrepr()
	{
		return ""+id;
	}
}
