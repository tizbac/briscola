#ifndef PLAYERLIST_H
#define PLAYERLIST_H

#include <QTableWidget>
#include <QAbstractItemModel>
class Player
{
public:
    QString name;
    unsigned int id;
    int gameswon;
    int gameslost;
};
Q_DECLARE_METATYPE( Player* );
class PlayerList : public QAbstractItemModel
{
public:
    PlayerList(QObject *parent);
    ~PlayerList(){}
    std::map<unsigned int,QString> playerid2name;
    std::map<QString,unsigned int> name2playerid;
    std::map<unsigned int,Player*> players;
    std::map<Player*,int> playerlistindexes;
    QList<Player*> playerlist;
    QModelIndex index( int row, int col, const QModelIndex& parent = QModelIndex() ) const;
        QModelIndex parent( const QModelIndex& child ) const;
        virtual int columnCount( const QModelIndex& parent = QModelIndex() ) const;

        int rowCount( const QModelIndex& parent = QModelIndex() ) const;
        virtual QVariant data( const QModelIndex& index, int role ) const;
        virtual QVariant headerData( int col, Qt::Orientation, int role ) const;
    void Pulisci();
    bool hasChildren(const QModelIndex &parent) const;
    void AddPlayer(unsigned int id,QString name,int gameswon , int gameslost);
    void UpdatePlayer(unsigned int id,int gameswon , int gameslost);
    void RemovePlayer(unsigned int id);
    Player * GetPlayer(unsigned int id);
};

#endif // PLAYERLIST_H
