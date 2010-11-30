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
#include "playerlist.h"
#include <QApplication>
#include <iostream>
PlayerList::PlayerList(QWidget * __parent) : QTableWidget(__parent)
{
    setColumnCount(3);
    QTableWidgetItem *qtablewidgetitem4 = new QTableWidgetItem();
    qtablewidgetitem4->setText(QApplication::translate("Briscola", "Nick", 0, QApplication::UnicodeUTF8));
    QTableWidgetItem *qtablewidgetitem5 = new QTableWidgetItem();
    qtablewidgetitem5->setText(QApplication::translate("Briscola", "PV", 0, QApplication::UnicodeUTF8));
    QTableWidgetItem *qtablewidgetitem6 = new QTableWidgetItem();
    qtablewidgetitem6->setText(QApplication::translate("Briscola", "PP", 0, QApplication::UnicodeUTF8));
    setHorizontalHeaderItem(0,qtablewidgetitem4);
    setHorizontalHeaderItem(1,qtablewidgetitem5);
    setHorizontalHeaderItem(2,qtablewidgetitem6);
    _parent = __parent;
}
void PlayerList::Pulisci()
{
    clearContents();
    playerid2name.clear();
    name2playerid.clear();
    for ( std::map<unsigned int,Player*>::iterator it = players.begin(); it != players.end(); it++)
        delete (*it).second;
    players.clear();
    setRowCount(0);
}
void PlayerList::AddPlayer(unsigned int id, QString name, int gameswon, int gameslost)
{
    if ( playerid2name.find(id) != playerid2name.end() )
        return;
    int rowindex = rowCount();
    setRowCount(rowCount()+1);
    Player * pl = new Player();
    pl->id = id;
    pl->name = name;
    pl->gameslost = gameslost;
    pl->gameswon = gameswon;
    QTableWidgetItem * prototype = new QTableWidgetItem();
    prototype->setFlags(Qt::ItemIsSelectable|Qt::ItemIsEnabled);

    playerid2name.insert(std::pair<unsigned int,QString>(id,name));
    name2playerid.insert(std::pair<QString,unsigned int>(name,id));
    players.insert(std::pair<unsigned int,Player*>(id,pl));
    QTableWidgetItem * itm = prototype->clone();
    itm->setText(name);
    setItem(rowindex,0,itm);
    itm = prototype->clone();
    itm->setText(QString().sprintf("%d",gameswon));
    setItem(rowindex,1,itm);
    itm = prototype->clone();
    itm->setText(QString().sprintf("%d",gameslost));
    setItem(rowindex,2,itm);
    delete prototype;
}
void PlayerList::RemovePlayer(unsigned int id)
{
    if ( playerid2name.find(id) == playerid2name.end() )
        return;
    QString name = playerid2name[id];
    QList<QTableWidgetItem*> itm = findItems(name,Qt::MatchExactly);
    removeRow(itm[0]->row());
    name2playerid.erase(name);
    playerid2name.erase(id);
    delete players[id];
    players.erase(id);
}
Player * PlayerList::GetPlayer(unsigned int id)
{
    if ( players.find(id) == players.end() )
    {
        std::cerr << "Player " << id << " does not exist(GetPlayer())" << std::endl;
        return NULL;
    }
    return players[id];
}
