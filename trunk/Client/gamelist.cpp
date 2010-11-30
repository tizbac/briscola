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
#include "gamelist.h"
#include "briscola.h"
GameList::GameList() : QStandardItemModel()
{
    QStandardItem * item = new QStandardItem("Descrizione");
    setHorizontalHeaderItem(0,item);
    item = new QStandardItem("Host");
    setHorizontalHeaderItem(1,item);
    item = new QStandardItem("Giocatori");
    setHorizontalHeaderItem(2,item);
    //QListWidgetItem * root = new QListWidgetItem(v,"Partite");
}

/*QVariant GameList::data(const QModelIndex &index, int role) const
{
    if (!index.isValid())
        return QVariant();
    int row = index.row();
    if ( row >= games.size() )
        return QVariant();
    switch ( index.column() )
    {
    case 0:
        return QVariant(games[row]->desc);
    case 1:
        return QVariant();
    case 2:
        return QVariant(games[row]->numplayers);
    }
    return QVariant();
}*/
/*int GameList::rowCount(const QModelIndex &parent) const
{
    return games.size();
}*/
void GameList::OnGameStart(unsigned int gid)
{
    if ( id2game.find(gid) == id2game.end())
        return;
    Game * g = id2game[gid];
    g->started = true;


}

void GameList::GameOpened(unsigned int id,unsigned int hostid, QString desc, int numplayers)
{
    if ( id2game.find(id) != id2game.end())
        return;
    Game * g = new Game();
    g->desc = desc;
    g->hostid = hostid;
    g->numplayers = numplayers;
    //g->players.push_back(g->hostid);
    id2game[id] = g;
    g->index = games.size();
    g->id = id;
    games.push_back(g);

    insertRow(games.size()-1);
    QStandardItem * item = new QStandardItem(desc);
    item->setFlags(Qt::ItemIsEnabled|Qt::ItemIsSelectable);
    setItem(games.size()-1,0,item);
    Player * host = briscola->pllist->GetPlayer(games[games.size()-1]->hostid);
    if ( host )
        item = new QStandardItem(host->name);
    else
        item = new QStandardItem("Errore interno.");
    item->setFlags(Qt::ItemIsEnabled|Qt::ItemIsSelectable);
    setItem(games.size()-1,1,item);
    item = new QStandardItem(QString().sprintf("%d/%d",0,numplayers));
    item->setFlags(Qt::ItemIsEnabled|Qt::ItemIsSelectable);
    setItem(games.size()-1,2,item);
}
void GameList::GameClosed(unsigned int id)
{
    if ( id2game.find(id) == id2game.end())
        return;
    Game * ptr = id2game[id];
    delete ptr;
    id2game.erase(id);
    std::vector<Game*> newgamelist;
    for ( int i = 0; i < games.size(); i++)
    {
        if ( ! (games[i] == ptr) )
        {
            games[i]->index = newgamelist.size();
            newgamelist.push_back(games[i]);
        }
        else
            removeRow(i);
    }
    games = newgamelist;

}
void GameList::PlayerJoined(unsigned int gid, unsigned int plid)
{
    Game * partita = id2game[gid];
    partita->players.push_back(plid);
    if (!partita)
        return;
    QStandardItem * item = new QStandardItem(QString().sprintf("%d/%d",partita->players.size(), partita->numplayers));
    item->setFlags(Qt::ItemIsEnabled|Qt::ItemIsSelectable);
    setItem(partita->index,2,item);
}

void GameList::PlayerLeft(unsigned int gid, unsigned int plid)
{
    Game * partita = id2game[gid];
    if (!partita)
        return;
    std::vector<unsigned int> newplayerlist;
    for ( int i = 0; i < partita->players.size(); i++)
    {
        if ( ! ( partita->players[i] == plid ))
            newplayerlist.push_back(partita->players[i]);
    }
    partita->players = newplayerlist;
    QStandardItem * item = new QStandardItem(QString().sprintf("%d/%d",partita->players.size(), partita->numplayers));
    item->setFlags(Qt::ItemIsEnabled|Qt::ItemIsSelectable);
    setItem(partita->index,2,item);

}
void GameList::OnDisconnect()
{
    while ( games.size() > 0)
        GameClosed(games[0]->id);
}
