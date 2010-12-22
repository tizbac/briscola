#ifndef BRISCOLA_H
#define BRISCOLA_H

#include <QMainWindow>
#include <QtNetwork/QTcpSocket>
#include "playerlist.h"
#include "gamelist.h"
#include <sstream>
#include <map>
namespace Ui {
    class Briscola;
}
template <typename K, typename V> inline std::string stdmaprepr(std::map<K,V> & m)
{
    std::stringstream ss;
    ss << "{ ";
    typedef std::map<K, V> maptype;
    for ( typename maptype::iterator it = m.begin(); it != m.end(); it++)
    {
        if ( it != m.begin())
            ss << ",";
        ss << (*it).first << " : " << (*it).second;

    }
    ss << " }";
    return ss.str();



}


class Briscola : public QMainWindow
{
    Q_OBJECT

public:
    explicit Briscola(QWidget *parent = 0);
    ~Briscola();
    QTcpSocket * sock;
    void ConnectToServer();
    QString host;
    QString username;
    QString password;
    QString recvbuf;
    unsigned int mygameid;
    unsigned int myid;
    GameList * glist;
    PlayerList * pllist;
    Game * GetGame();

private:
    Ui::Briscola *ui;
    void HandleCommand( QStringList args );
    void DoLogin();

    std::map<unsigned int,unsigned int> battleid2row;

    std::map<unsigned int,unsigned int> row2battleid;
private slots:
    void on_actionInformazioni_triggered();
    void on_actionCrea_partita_triggered();
    void on_treeView_activated(QModelIndex index);
    void on_actionDisconnect_triggered();
    void on_actionConnect_to_server_triggered();
    void on_data_recv();
    void on_disconnected();
};
extern Briscola * briscola;
#endif // BRISCOLA_H
