#include "scoredisplay.h"
#include "briscola.h"
#include <iostream>
ScoreDisplay::ScoreDisplay(QObject *parent) :
    QAbstractItemModel(parent)
{
}


bool ScoreDisplay::hasChildren(const QModelIndex &parent) const
{
    if ( parent.row() == -1)
        return true;
    return false;
}
QVariant ScoreDisplay::data(const QModelIndex &index, int role) const
{
    if ( role == Qt::DisplayRole ) {
        switch ( index.column())
        {
        case 0:
            {
                int r = index.row();
                int playerid = players[r];
                Player * pl = briscola->pllist->GetPlayer(playerid);
                if (!pl)
                    return QString("#Offline#");
                return pl->name;
            }
        case 1:
            {
                return QString().sprintf("%d",score[index.row()] );
            }
        }
    }
     if (role == Qt::BackgroundRole) {
         if ( score[index.row()] > 60 && briscola->GetGame()->players.size() == 2 )
             return QColor(200,200,255);
     }
    return QVariant();
}
QVariant ScoreDisplay::headerData(int section, Qt::Orientation orientation, int role) const
{
     if ( role == Qt::DisplayRole ) {
        switch ( section ) {
        case 0://Nick
            return tr("Giocatore");
        case 1://Punti
            return tr("Punti");
        default: break;

        }
     }
    return QVariant();

}
int ScoreDisplay::rowCount(const QModelIndex &parent) const
{
    //std::cout << "rowCount( row=" << parent.row() << " col=" << parent.column() << ")" << std::endl;
    if ( parent.row() == -1 )
        return players.size();
    return 0;
}
int ScoreDisplay::columnCount(const QModelIndex &parent) const
{
    return 2;
}
QModelIndex ScoreDisplay::index(int row, int column, const QModelIndex &parent)
             const
 {
     if (!hasIndex(row, column, parent))
         return QModelIndex();
     return createIndex(row, column);

 }
QModelIndex ScoreDisplay::parent( const QModelIndex& /*child*/ ) const {
   return QModelIndex();
}
void ScoreDisplay::Pulisci()
{
    players.clear();
    player_row.clear();
    score.clear();
    reset();
}
void ScoreDisplay::SetScore(unsigned int playerid, int p_score)
{

    if ( player_row.find(playerid) != player_row.end())
    {
        score[player_row[playerid]] = p_score;
        dataChanged(index(player_row[playerid],1),index(player_row[playerid],1));
        std::cout << stdmaprepr<unsigned int ,int >(player_row) << std::endl;
        std::cout << "Updating score entry for " << playerid << std::endl;
        return;
    }
    std::cout << "Adding score entry for " << playerid << std::endl;
    beginInsertRows(QModelIndex(),players.size(),players.size());
    player_row.insert(std::pair<unsigned int ,int >(playerid,players.size()));
    score.append(p_score);
    players.append(playerid);
    endInsertRows();


}
void ScoreDisplay::SwapPlayer(unsigned int oldplayerid, int newplayerid)
{
    if ( player_row.find(oldplayerid) == player_row.end())
    {
        std::cerr << __PRETTY_FUNCTION__ << " : oldplayerid ! exist" << std::endl;
        if ( player_row.find(newplayerid) == player_row.end())
            SetScore(newplayerid,0);
       return;
    }
    int r = player_row[oldplayerid];
    player_row.erase(oldplayerid);
    player_row.insert(std::pair<unsigned int ,int >(newplayerid,r));
    players[r] = newplayerid;
    dataChanged(index(r,0),index(r,1));
}
