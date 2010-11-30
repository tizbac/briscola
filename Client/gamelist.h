#ifndef GAMELIST_H
#define GAMELIST_H
#include <QTreeView>
#include <QStandardItemModel>
class Game
{
public:
    Game()
    {
        started = false;
        turno = -1;
        regnante = -1;
        ncarte = 0;
    }

    std::vector<unsigned int> players;
    int numplayers;
    QString desc;
    int hostid;
    int index;
    int id;
    int ncarte;
    std::vector<unsigned int> cardcount; // Max 4 players
    int turno;
    int turnoprecedente;
    std::vector<unsigned int> hand;
    std::vector<unsigned int> tavolo;
    std::vector<unsigned int> prese;
    std::vector<unsigned int> preseda;
    std::map<unsigned int,unsigned int> gameids;
    bool started;
    int regnante;
};

class GameList : public QStandardItemModel
{
public:
    GameList();
    void GameOpened(unsigned int id,unsigned int plid,QString desc, int numplayers);
    void GameClosed(unsigned int id);
    void PlayerJoined(unsigned int gid,unsigned int plid);
    void PlayerLeft(unsigned int gid,unsigned int plid);
    void OnGameStart(unsigned int gid);
    void OnDisconnect();
   /* int rowCount ( const QModelIndex & parent = QModelIndex() ) const;
    QVariant data(const QModelIndex &index, int role) const;*/
    std::vector<Game*> games;
    std::map<unsigned int,Game*> id2game;
};

#endif // GAMELIST_H
