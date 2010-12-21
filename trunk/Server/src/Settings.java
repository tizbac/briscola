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
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
public class Settings {
	public String host;
	public String username;
	public String password;
	public String database;
	public int bindport;
	public String bindaddress;
	public Settings() {
		File f = new File("settings.xml");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		Document doc = null;
		try {
			doc = db.parse(f);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element rootele = doc.getDocumentElement();
		Node dbnode = rootele.getElementsByTagName("db").item(0);
		Element dbelement = (Element)dbnode;
		host = dbelement.getAttribute("host");
		username = dbelement.getAttribute("username");
		password = dbelement.getAttribute("password");
		database = dbelement.getAttribute("database");
		Node netnode = rootele.getElementsByTagName("net").item(0);
		Element netelement = (Element)netnode;
		bindaddress = netelement.getAttribute("bindaddress");
		bindport = Integer.parseInt(netelement.getAttribute("port"));
	}
}
