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
/**
 * Class to hold player data required while a player is online
 * @author Tiziano Bacocco
 *
 */
public class PlayerInfo {
	/** The count of games won since registration */
	public int gameswon;
	/** The count of games lost since registration */
	public int gameslost;
	/** The login name of the player */
	public String nickname;
	/** The id on the database , it will be used when saving data */
	public int dbid;
	PlayerInfo()
	{
		
	}
}
