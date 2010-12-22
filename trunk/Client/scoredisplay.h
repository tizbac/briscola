#ifndef SCOREDISPLAY_H
#define SCOREDISPLAY_H

#include <QAbstractItemModel>
#include <map>
class ScoreDisplay : public QAbstractItemModel
{
    Q_OBJECT
public:
    explicit ScoreDisplay(QObject *parent = 0);
    QModelIndex index( int row, int col, const QModelIndex& parent = QModelIndex() ) const;
    QModelIndex parent( const QModelIndex& child ) const;
    virtual int columnCount( const QModelIndex& parent = QModelIndex() ) const;

    int rowCount( const QModelIndex& parent = QModelIndex() ) const;
    virtual QVariant data( const QModelIndex& index, int role ) const;
    virtual QVariant headerData( int col, Qt::Orientation, int role ) const;
    void Pulisci();
    bool hasChildren(const QModelIndex &parent) const;
    void SetScore(unsigned int playerid,int p_score);
    void SwapPlayer(unsigned int oldplayerid,int newplayerid);
signals:

public slots:

private:
    QList<unsigned int> players;
    QList<int> score;
    std::map<unsigned int,int> player_row;
};

#endif // SCOREDISPLAY_H
