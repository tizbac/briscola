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
#include "briscola.h"
#include "ui_briscola.h"
#include "connectdialog.h"
#include <iostream>
#include <QCryptographicHash>
#include <QMessageBox>
#include <QTextCodec>
#include <map>
#include "gamewindow.h"
#include "opengamedialog.h"
#include "dialog.h"
Briscola * briscola = NULL;
#define BRISCOLAVERSION "1"

Briscola::Briscola(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::Briscola)
{
    briscola = this;
    ui->setupUi(this);
    glist = new GameList();
    ui->treeView->setModel(glist);
    sock = new QTcpSocket();
    mygameid = 0;
    connect(sock,SIGNAL(readyRead()),this,SLOT(on_data_recv()));
    connect(sock,SIGNAL(error(QAbstractSocket::SocketError)),this,SLOT(on_disconnected()));
    connect(sock,SIGNAL(disconnected()),this,SLOT(on_disconnected()));
    pllist = new PlayerList(this);
    ui->playerlistview->setModel(pllist);
}

Briscola::~Briscola()
{
    delete ui;
}

void Briscola::on_actionConnect_to_server_triggered()
{
    cdlg->show();
}
void Briscola::ConnectToServer()
{
    sock->abort();
    sock->connectToHost(host,3112);
}
void Briscola::on_data_recv()
{

while ( sock->canReadLine() )
{
    QString line = QString::fromUtf8(sock->readLine()).replace("\r","").replace("\n","");
    std::cerr << line.toLocal8Bit().data() << "-"<< std::endl;
    QStringList args = line.split(" ",QString::KeepEmptyParts);
    if ( args.size() > 0 )
    {
        HandleCommand(args);



    }
}
}
void Briscola::HandleCommand(QStringList args)
{
    if ( args[0] == "BSRV" && args.size() == 2 )
    {
        QString serverversion = args[1];
        //ui->lblstatus->setText("Connesso : Host "+host);
        ui->lblstatus->setText("Login...");
        DoLogin();
    }else if ( args[0] == "E" )
    {
        args.removeFirst();
        if ( args.size() > 0 )
        {
            QString txt = args.join(" ");
            QMessageBox::critical(this,"Errore",txt,QMessageBox::Ok,QMessageBox::NoButton);
        }

    }else if ( args[0] == "LOGINOK" )
    {
        ui->lblstatus->setText("Login eseguito con successo");
        myid = args[1].toUInt();
        pllist->AddPlayer(myid,username,args[2].toUInt(),args[3].toUInt());
    }else if ( args[0] == "ADDPLAYER" )
    {
        pllist->AddPlayer(args[1].toUInt(),args[2],args[3].toUInt(),args[4].toUInt());

    }else if ( args[0] == "REMPLAYER" )
    {
        pllist->RemovePlayer(args[1].toUInt());
    }else if ( args[0] == "GO" )
    {
        glist->GameOpened(args[1].toUInt(),args[2].toUInt(),args[4],args[3].toInt());
    }else if ( args[0] == "GC" )
    {
        glist->GameClosed(args[1].toUInt());
        if ( args[1].toUInt() == mygameid )
            gamewindow->OnGameLeft(myid);
    }else if ( args[0] == "JG" )
    {
        glist->PlayerJoined(args[1].toUInt(),args[2].toUInt());
        std::cout << "MyID=" << myid << std::endl;
        if ( GetGame())
            gamewindow->UpdatePlayers();

        if ( args[2].toUInt() == myid )
        {
            //Deve aprire la finestra di gioco
            std::cout << " \"" << args[1].toStdString() << "\" -> INT: " << args[1].toUInt() << std::endl;
            mygameid = args[1].toUInt();

            gamewindow->OnGameJoin(GetGame()->hostid == myid,GetGame());
            gamewindow->UpdatePlayers();
        }

    }else if ( args[0] == "LG" )
    {
        glist->PlayerLeft(args[1].toUInt(),args[2].toUInt());
        if ( GetGame())
            gamewindow->UpdatePlayers();
        if ( args[2].toUInt() == myid )
        {
            //Deve chiudere la finestra di gioco
            gamewindow->OnGameLeft(myid);
            mygameid = -1;


        }

    }else if ( args[0] == "HAND" )
    {
        if (!GetGame())
        {
            std::cerr << "Errore GetGame()=NULL" << std::endl;

        }
        GetGame()->hand.clear();
        for ( int i = 1; i < args.size(); i++)
            GetGame()->hand.push_back(args[i].toUInt());
        gamewindow->OnUpdate();
    }else if ( args[0] == "T" )
    {
        if (!GetGame())
        {
            std::cerr << "Errore GetGame()=NULL" << std::endl;

        }
        GetGame()->tavolo.clear();
        if ( args[1].trimmed().length() != 0)
        {
            for ( int i = 1; i < args.size(); i++)
                GetGame()->tavolo.push_back(args[i].toUInt());
        }
        gamewindow->OnUpdate();



    }else if ( args[0] == "R" )
    {
        GetGame()->regnante = args[1].toUInt();
        gamewindow->OnUpdate();



    }else if ( args[0] == "NP" )
    {
        GetGame()->turnoprecedente = GetGame()->turno;
        GetGame()->turno = args[1].toUInt();
       // std::cout << "TURNO: " << args[1].toUInt() << std::endl;
        gamewindow->UpdateStatus();
    }else if ( args[0] == "GS" )
    {
        glist->OnGameStart(args[1].toUInt());
        if ( args[1].toUInt() == mygameid )
        {
            gamewindow->OnGameStarted();
        }
    }
    else if ( args[0] == "TT" )
    {
        gamewindow->OnTableTaken(args[1].toUInt());
    }else if ( args[0] == "PID" )
    {
        int replacedplayer = -1;
        for ( std::map<unsigned int,unsigned int>::iterator it = GetGame()->gameids.begin(); it != GetGame()->gameids.end(); it++ )
        {
            if ( (*it).second == args[2].toUInt())
            {
                std::cout << "Sostituzione giocatore "<< (*it).first << std::endl;
                replacedplayer = (*it).first;
                GetGame()->gameids.erase(it);

                break;
            }
        }
        GetGame()->gameids[args[1].toUInt()] = args[2].toUInt();
        if ( replacedplayer > -1)
            gamewindow->score->SwapPlayer(replacedplayer,args[1].toUInt());
        gamewindow->UpdatePlayers();
    }else if ( args[0] == "DN" )
    {
         GetGame()->ncarte = args[1].toUInt();
         gamewindow->UpdateStatus();
    }else if ( args[0] == "WON" )
    {
        if (!GetGame())
            return;
        QString res;
        if ( args[1].toUInt() == myid )
            res = "Hai vinto!";
        else
            res = "Hai perso.";
        QMessageBox::information(gamewindow,"Esito del gioco",res,QMessageBox::Ok,QMessageBox::NoButton);
    }else if ( args[0] == "PING" )
        sock->write(QByteArray("PONG\n"));
     else if ( args[0] == "GAMECHAT" )
     {
        unsigned int playerid = args[1].toUInt();
        args.removeFirst();
        args.removeFirst();
        QString txt = args.join(" ");
        gamewindow->OnChatMessage(playerid,txt);
     }else if ( args[0] == "SC" )
     {
        unsigned int playerid = args[1].toUInt();
        int score = args[2].toInt();
        gamewindow->score->SetScore(playerid,score);

     }else if ( args[0] == "UPDATEPLAYER")
     {
        unsigned int playerid = args[1].toUInt();
        unsigned int won = args[2].toUInt();
        unsigned int lost = args[3].toUInt();
        pllist->UpdatePlayer(playerid,won,lost);
     }


}
Game * Briscola::GetGame()
{
    //std::cout << "GetGame() mygameid=" << mygameid << std::endl;
    return glist->id2game[mygameid];
}

void Briscola::DoLogin()
{
    QCryptographicHash passhash(QCryptographicHash::Md5);
    passhash.addData(password.toAscii());
    QByteArray digest = passhash.result();
    QString finalhash = QString(digest.toHex());
    QString version(BRISCOLAVERSION);
    QString cmd = "LOGIN "+username+" "+finalhash+" "+version+"\n";
    sock->write(cmd.toAscii());
}

void Briscola::on_disconnected()
{
    pllist->Pulisci();
    glist->OnDisconnect();
    gamewindow->OnGameLeft(myid);
    ui->lblstatus->setText("Disconnesso.");
}

void Briscola::on_actionDisconnect_triggered()
{
    sock->abort();
}

void Briscola::on_treeView_activated(QModelIndex index)
{
    unsigned int gameid = glist->games[index.row()]->id;
    sock->write(QString().sprintf("JG %d\n",gameid).toAscii());
}

void Briscola::on_actionCrea_partita_triggered()
{
    ogdlg->show();
}

void Briscola::on_actionInformazioni_triggered()
{
    adlg->show();
}
