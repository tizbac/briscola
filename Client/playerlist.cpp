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
#include <QTreeView>
PlayerList::PlayerList(QObject * __parent) : QAbstractItemModel(__parent)
{
    qRegisterMetaType<Player*>( "Player" );
}
void PlayerList::Pulisci()
{
    beginResetModel();
    playerid2name.clear();
    name2playerid.clear();
    playerlistindexes.clear();
    playerlist.clear();
    players.clear();
    endResetModel();
    for ( std::map<unsigned int,Player*>::iterator it = players.begin(); it != players.end(); it++)
        delete (*it).second;

}
bool PlayerList::hasChildren(const QModelIndex &parent) const
{
    if ( parent.row() == -1)
        return true;
    return false;
}
QVariant PlayerList::data(const QModelIndex &index, int role) const
{
    if ( role == Qt::UserRole ) {
        QVariant a;
        a.setValue<Player*>(playerlist[index.row()]);
        return a;
     }
      if ( role == Qt::DisplayRole ) {
    switch ( index.column() )
    {
    case 0://Nick
        return playerlist[index.row()]->name;
    case 1://Partite vinte
        return QString().sprintf("%d",playerlist[index.row()]->gameswon);
    case 2://Partite perse
        return QString().sprintf("%d",playerlist[index.row()]->gameslost);

    }}

    return QVariant();




}

void PlayerList::AddPlayer(unsigned int id, QString name, int gameswon, int gameslost)
{
    if ( playerid2name.find(id) != playerid2name.end() )
        return;
    Player * pl = new Player();
    pl->id = id;
    pl->name = name;
    pl->gameslost = gameslost;
    pl->gameswon = gameswon;
    QTableWidgetItem * prototype = new QTableWidgetItem();
    prototype->setFlags(Qt::ItemIsSelectable|Qt::ItemIsEnabled);

    playerid2name.insert(std::pair<unsigned int,QString>(id,name));
    name2playerid.insert(std::pair<QString,unsigned int>(name,id));
    playerlistindexes.insert(std::pair<Player*,int>(pl,playerlist.size()));
    players.insert(std::pair<unsigned int,Player*>(id,pl));
    beginInsertRows(QModelIndex(),playerlist.size(),playerlist.size());
    playerlist.append(pl);
    endInsertRows();

}
QVariant PlayerList::headerData(int section, Qt::Orientation orientation, int role) const
{
     if ( role == Qt::DisplayRole ) {
        switch ( section ) {
        case 0://Nick
            return tr("Nick");
        case 1://Partite vinte
            return tr("Partite Vinte");
        case 2://Partite perse
            return tr("Partite Perse");
        default: break;

        }
     }
    return QVariant();

}
int PlayerList::rowCount(const QModelIndex &parent) const
{
    //std::cout << "rowCount( row=" << parent.row() << " col=" << parent.column() << ")" << std::endl;
    if ( parent.row() == -1 )
        return playerlist.size();
    return 0;
}
int PlayerList::columnCount(const QModelIndex &parent) const
{
    return 3;

}
QModelIndex PlayerList::index(int row, int column, const QModelIndex &parent)
             const
 {
     if (!hasIndex(row, column, parent))
         return QModelIndex();
     return createIndex(row, column);

 }
/*Qt::ItemFlags PlayerList::flags(const QModelIndex &index) const
 {
     if (!index.isValid())
         return 0;

     return Qt::ItemIsEnabled | Qt::ItemIsSelectable;
 }*/
 QModelIndex PlayerList::parent( const QModelIndex& /*child*/ ) const {
    return QModelIndex();
 }
void PlayerList::RemovePlayer(unsigned int id)
{
    if ( playerid2name.find(id) == playerid2name.end() )
        return;
    QString name = playerid2name[id];
    name2playerid.erase(name);
    playerid2name.erase(id);
    int index = playerlistindexes[players[id]];

    beginRemoveRows(QModelIndex(),index,index);
    playerlist.removeOne(players[id]);
    endRemoveRows();
    players.erase(id);

    playerlistindexes.erase(players[id]);
    delete players[id];
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
void PlayerList::UpdatePlayer(unsigned int id, int gameswon, int gameslost)
{
    Player * pl = GetPlayer(id);
    int r = playerlistindexes[pl];
    if (!pl)
        return;
    pl->gameswon = gameswon;
    pl->gameslost = gameslost;
    dataChanged(index(r,1),index(r,2));
}
