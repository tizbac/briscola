#ifndef BRISCOLA_H
#define BRISCOLA_H

#include <QMainWindow>
#include <QtNetwork/QTcpSocket>
#include "playerlist.h"
#include "gamelist.h"
#include <map>
namespace Ui {
    class Briscola;
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
