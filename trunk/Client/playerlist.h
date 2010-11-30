#ifndef PLAYERLIST_H
#define PLAYERLIST_H

#include <QTableWidget>
class Player
{
public:
    QString name;
    unsigned int id;
    int gameswon;
    int gameslost;
};

class PlayerList : public QTableWidget
{
public:
    QWidget * _parent;
    std::map<unsigned int,QString> playerid2name;
    std::map<QString,unsigned int> name2playerid;
    std::map<unsigned int,Player*> players;
    void Pulisci();
    void AddPlayer(unsigned int id,QString name,int gameswon , int gameslost);
    void RemovePlayer(unsigned int id);
    Player * GetPlayer(unsigned int id);
    PlayerList(QWidget * parent);
};

#endif // PLAYERLIST_H
