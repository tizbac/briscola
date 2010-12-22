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
#include "gamewindow.h"
#include "ui_gamewindow.h"
#include "briscola.h"
#include <QTime>
#include <iostream>
#include <QGraphicsView>
#include <QMessageBox>
#include <sstream>
#include <QScrollBar>
GameWindow::GameWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::GameWindow)
{
    ui->setupUi(this);
    setWindowFlags( Qt::CustomizeWindowHint | Qt::WindowTitleHint | Qt::Window);
    hide();
    score = new ScoreDisplay(this);
    ui->scorelist->setModel(score);

    v = ui->graphicsView;
    w = 800;
    h = 600;
    td = new QTimer();
    td->setSingleShot(true);
    td->setInterval(2000);
    td->stop();
    connect(td,SIGNAL(timeout()),this,SLOT(delayedanim()));
    v->setGeometry(v->x(),v->y(),800,600);
    //QGraphicsRectItem * testcoord = new QGraphicsRectItem(w/7,h/4,64.0,64.0);

    QGraphicsScene * scene = v->scene();
    if ( !scene )
    {
        scene = new QGraphicsScene(v);
        scene->setSceneRect(v->sceneRect());
        v->setScene(scene);
    }
   // scene->addItem(testcoord);
    mazzo = new QParallelAnimationGroup();
    v->show();
#ifdef WIN32
    QPixmap cartapx("carte\\retro.png");
#else
    QPixmap cartapx("carte/retro.png");
#endif
    cardh = cartapx.height();
    cardw = cartapx.width();
    if ( cardh == 0 || cardw == 0 )
    {
        QMessageBox::critical(this,"Errore","Dimensione carte errata!",QMessageBox::Ok,QMessageBox::NoButton);
        abort();

    }
   /* Pixmap * cartagi = new Pixmap(cartapx.scaled(QSize(w/7,h/4),Qt::KeepAspectRatio),-1);
    cartagi->setOffset(0,0);
scene->addItem(cartagi);
    v->scale(1,1);
    cartagi->setPos(0,0);*/
    QGraphicsPixmapItem * cartagi = scene->addPixmap(cartapx.scaled(QSize(w/7,h/4),Qt::KeepAspectRatio));

    cartagi->setPos(w/7,h/4);
    //std::cout << cartagi->pixmap().width() << std::endl;
   for ( int i = 0; i < 40; i++ )
    {
#ifdef WIN32
        QPixmap carta(QString().sprintf("carte\\%d.png",i));
        if ( carta.isNull() )
        {
            int ch = QMessageBox::critical(this,"Errore!",QString().sprintf("Impossibile caricare carte/%d.png",i),QMessageBox::Ok,QMessageBox::Abort);
            if ( ch == QMessageBox::Abort )
                abort();
        }
#else

        QPixmap carta(QString().sprintf("carte/%d.png",i));
        if ( carta.isNull() )
        {
            int ch = QMessageBox::critical(this,"Errore!",QString().sprintf("Impossibile caricare carte/%d.png",i),QMessageBox::Ok,QMessageBox::Abort);
            if ( ch == QMessageBox::Abort )
                abort();
        }
#endif

        carta = carta.scaled(QSize(w/7,h/4),Qt::KeepAspectRatio);
        //std::cout << carta.width() << " "<< carta.height() << std::endl;
        Pixmap * c = new Pixmap(carta,i);

        if ( c == 0x0 )
            abort();
        c->setZValue(i);
        carte.push_back(c);

        scene->addItem(c);

        c->hide();

        QPropertyAnimation * ani = new QPropertyAnimation(c,"pos");
       /* ani->setDuration(2000);
        ani->setTargetObject(c);
        std::cout << ani->targetObject() << std::endl;
        ani->setStartValue(QPointF(rand()%w,rand()%h));
        ani->setEndValue(QPointF(w/5,h/2));
        ani->setEasingCurve(QEasingCurve::InOutBack);
        mazzo->addAnimation(ani);*/
        animazioni.push_back(ani);
        AnimateCard(i,0,0,cartagi->pos().x(),cartagi->pos().y());
     /*    std::stringstream ss;
    ss << "W=" << v->sceneRect().width() << " H=" << v->sceneRect().height() << " C1=" << v->sceneRect().left() << " C2=" << v->sceneRect().top() << std::endl;
   QMessageBox::information(this,"Debug",QString(ss.str().c_str()),QMessageBox::Ok,QMessageBox::NoButton);
*/
    }
  //  cartagi->show();
   // scene.addItem(cartagi);




}
int pposx(unsigned int pid)
{
    QList<QPointF> edges;
    edges.push_back(QPointF(400,800));
    edges.push_back(QPointF(-200,300));
    edges.push_back(QPointF(400,-200));
    edges.push_back(QPointF(1000,300));
    Game * g = briscola->GetGame();
    int meindex = 0;
    std::map<unsigned int,unsigned int> reversegameids;
    for ( int i = 0; i < g->players.size(); i++ )
    {
        if ( g->players[i] == briscola->myid )
        {
            meindex = g->gameids[g->players[i]];

        }
        reversegameids[g->gameids[g->players[i]]] = g->players[i];
    }



    for ( std::map<unsigned int,unsigned int>::iterator it = g->gameids.begin(); it != g->gameids.end(); it++ )
    {
        if ((*it).first == pid )
            return int(edges[((*it).second-meindex)%4].x());
    }
    return int(edges[0].x());

}
int pposy(unsigned int pid)
{
    QList<QPointF> edges;
    edges.push_back(QPointF(400,800));
    edges.push_back(QPointF(-200,300));
    edges.push_back(QPointF(400,-200));
    edges.push_back(QPointF(1000,300));
    Game * g = briscola->GetGame();
    int meindex = 0;
    std::map<unsigned int,unsigned int> reversegameids;
    for ( int i = 0; i < g->players.size(); i++ )
    {
        if ( g->players[i] == briscola->myid )
        {
            meindex = g->gameids[g->players[i]];

        }
        reversegameids[g->gameids[g->players[i]]] = g->players[i];
    }



    for ( std::map<unsigned int,unsigned int>::iterator it = g->gameids.begin(); it != g->gameids.end(); it++ )
    {
        if ((*it).first == pid )
            return int(edges[((*it).second-meindex)%4].y());
    }
    return int(edges[0].y());
}
void GameWindow::AppendMessage(unsigned int color, QString text, unsigned int flags)
{

    QString timestamp = QTime::currentTime().toString("[HH:mm:ss] ");
    ui->textEdit->textCursor().movePosition(QTextCursor::End);
    ui->textEdit->insertHtml(QString("<p><font color=#0000ff>")+Qt::escape(timestamp)+"</font> ");
    ui->textEdit->insertHtml(QString().sprintf("<font color=#%06x>%s%s%s%s%s%s%s</font><br></p>",
                                               color,flags & BOLD ? "<b>" : "",
                                               flags & ITALIC ? "<i>" : "",
                                               flags & UNDERLINE ? "<u>" : "",
                                               text.toStdString().c_str(),
                                               flags & BOLD ? "</b>" : "",
                                               flags & ITALIC ? "</i>" : "",
                                               flags & UNDERLINE ? "</u>" : ""
                                               ));
    QScrollBar *sb = ui->textEdit->verticalScrollBar();
    sb->setValue(sb->maximum());
}

void GameWindow::OnGameStarted()
{

    ui->pushButton->setEnabled(false);
    briscola->GetGame()->started = true;
    OnUpdate();
    UpdatePlayers();
}
void GameWindow::UpdateStatus()
{
    UpdatePlayers();
    ui->lbldn->setText(QString().sprintf("%d",briscola->GetGame()->ncarte));
    if ( briscola->GetGame()->turno >= 0 )
    {
        Player * pl = briscola->pllist->GetPlayer(briscola->GetGame()->turno);
        if (!pl)
           ui->lblturno->setText("#Offline#");
        else
            ui->lblturno->setText(pl->name);
    }
}
void GameWindow::UpdatePlayers()
{
    Game * g = briscola->GetGame();
    int meindex = 0;
    if ( !g->started )
    {
        ui->lblp->setText("---");
        ui->lblp_2->setText("---");
        ui->lblp_3->setText("---");
        ui->lblp_4->setText("---");
        std::vector<unsigned int> other;
        for ( int i = 0; i < g->numplayers; i++ )
        {
            if ( i >= g->players.size() )
                continue;
            if ( g->players[i] == briscola->myid )
            {
                meindex = i;
                Player * pl = briscola->pllist->GetPlayer(g->players[i]);
                ui->lblp->setText(pl->name);
            }else{
                other.push_back(g->players[i]);
            }
        }
        int c1 = 0;

        for ( std::vector<unsigned int>::iterator it = other.begin(); it != other.end(); it++ )
        {
            Player * pl = briscola->pllist->GetPlayer(*it);

            switch (c1)
            {
            case 0:
                ui->lblp_2->setText(pl->name);
                break;
            case 1:
                ui->lblp_3->setText(pl->name);
                break;
            case 2:
                ui->lblp_4->setText(pl->name);
                break;
            }
            c1++;

        }
    }else{
        ui->lblp->setText("---");
        ui->lblp_2->setText("---");
        ui->lblp_3->setText("---");
        ui->lblp_4->setText("---");
        std::map<unsigned int,unsigned int> reversegameids;
        for ( int i = 0; i < g->players.size(); i++ )
        {
            if ( g->players[i] == briscola->myid )
            {
                meindex = g->gameids[g->players[i]];

            }
            reversegameids[g->gameids[g->players[i]]] = g->players[i];
        }
        QList <unsigned int> players2;
        for ( int i = 0; i < g->players.size(); i++ )
        {
            players2.push_back(g->players[i]);
        }

        for ( std::map<unsigned int,unsigned int>::iterator it = g->gameids.begin(); it != g->gameids.end(); it++ )
        {
            Player * pl = briscola->pllist->GetPlayer((*it).first);
            if ( pl )
            {
                switch ( ((*it).second-meindex)%4 )
                {
                case 0:
                    ui->lblp->setText(pl->name);
                    break;
                case 1:
                    if ( players2.contains( (*it).first ))
                        ui->lblp_2->setText(pl->name);
                    else
                        ui->lblp_2->setText(pl->name+"(Lasciato la partita)");
                    break;
                case 2:
                    if ( players2.contains( (*it).first ))
                        ui->lblp_3->setText(pl->name);
                    else
                        ui->lblp_3->setText(pl->name+"(Lasciato la partita)");
                    break;
                case 3:
                    if ( players2.contains( (*it).first ))
                        ui->lblp_4->setText(pl->name);
                    else
                        ui->lblp_4->setText(pl->name+"(Lasciato la partita)");
                    break;
                }
             }
        }



    }


}

void GameWindow::OnUpdate()
{
    for (int i = 0; i < 40; i++ )
    {
        HideCard(i);


    }
    bool regnanteusato = false;
    QList <unsigned int> cartemie;
    for (int i = 0; i < briscola->GetGame()->hand.size() ; i++)
    {
        int carta = briscola->GetGame()->hand[i];
        ShowCard(carta);
        if ( carta == briscola->GetGame()->regnante )
            regnanteusato = true;
       float currxpos = (float)(carte[carta]->x());
       float currypos = (float)(carte[carta]->y());
       cartemie.push_back(carta);
        AnimateCard(carta,currxpos,currypos,200+(cardw+20)*i,600-cardh);
    }
    for (int i = 0; i < briscola->GetGame()->tavolo.size(); i++)
    {
        int carta = briscola->GetGame()->tavolo[i];
        ShowCard(carta);
        if ( carta == briscola->GetGame()->regnante )
            regnanteusato = true;
        float currxpos = (float)(carte[carta]->x());
        float currypos = (float)(carte[carta]->y());
        if ( cartemie.contains(carta) )
            AnimateCard(carta,currxpos,currypos,200+(cardw+20)*i,400-cardh);
        else
            AnimateCard(carta,pposx(briscola->GetGame()->turnoprecedente),pposy(briscola->GetGame()->turnoprecedente),200+(cardw+20)*i,400-cardh);
    }
    for (int i = 0; i < briscola->GetGame()->prese.size(); i++)
    {
        int carta = briscola->GetGame()->prese[i];
        ShowCard(carta);
        if ( carta == briscola->GetGame()->regnante )
            regnanteusato = true;
        float currxpos = (float)(carte[carta]->x());
        float currypos = (float)(carte[carta]->y());
        AnimateCardDelayed(carta,currxpos,currypos,pposx(briscola->GetGame()->preseda[i]),pposy(briscola->GetGame()->preseda[i]));


    }
    if ( briscola->GetGame()->regnante != -1 && !regnanteusato)
    {
        ShowCard(briscola->GetGame()->regnante);
        float currxpos = (float)(carte[briscola->GetGame()->regnante]->x());
        float currypos = (float)(carte[briscola->GetGame()->regnante]->y());
        AnimateCard(briscola->GetGame()->regnante,currxpos,currypos,10,400-cardh);
    }

}

void GameWindow::OnGameJoin(bool ishost, Game *game)
{

    show();
    score->Pulisci();
    ui->textEdit->clear();
    ui->textEdit->setHtml("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\">"
                          "<html><head><style type=\"text/css\">"
                          "p, li { white-space: pre-wrap; }"
                          "</style></head><body>");
    AppendMessage(0x007700,"Entrato nella partita",BOLD);
    ui->pushButton->setEnabled(ishost);
    for ( int i = 0; i < 40; i++ )
    {
        HideCard(i);
        AnimateCard(i,0,0,w/7,h/4);
    }
    Game * g = briscola->GetGame();
    g->tavolo.clear();
    g->hand.clear();
    g->regnante = -1;
    g->prese.clear();
    g->preseda.clear();
    g->started = false;
    OnUpdate();
    //briscola->mygameid = game->id;
    //mazzo->start();
}
void GameWindow::OnTableTaken(unsigned int playerid)
{
    for ( int i = 0; i < briscola->GetGame()->tavolo.size(); i++)
    {
        int carta = briscola->GetGame()->tavolo[i];
        briscola->GetGame()->prese.push_back(carta);
        briscola->GetGame()->preseda.push_back(playerid);
    }

}

void GameWindow::OnGameLeft(unsigned int playerid)
{
    if ( playerid == briscola->myid )
        hide();

}
void GameWindow::AnimateCard(unsigned int index, float sx, float sy, float dx, float dy)
{
    if ( animazioni[index]->state() == QAbstractAnimation::Running )
    {

        QVariant endval = animazioni[index]->endValue();
        ((Pixmap*)animazioni[index]->targetObject())->setPos(endval.toPointF().x(),endval.toPointF().y());
    }
    animazioni[index]->stop();
    animazioni[index]->setEasingCurve(QEasingCurve::OutCubic);

    animazioni[index]->setStartValue(QPointF(sx,sy));
    animazioni[index]->setEndValue(QPointF(dx,dy));
    animazioni[index]->setDuration(500);
    animazioni[index]->start();
}
void GameWindow::AnimateCardDelayed(unsigned int index, float sx, float sy, float dx, float dy)
{
    if ( animazioni[index]->state() == QAbstractAnimation::Running )
    {
        animazioni[index]->stop();
        QVariant endval = animazioni[index]->endValue();
        ((Pixmap*)animazioni[index]->targetObject())->setPos(endval.toPointF().x(),endval.toPointF().y());
    }

    animazioni[index]->setEasingCurve(QEasingCurve::OutCubic);
    animazioni[index]->setStartValue(QPointF(sx,sy));
    animazioni[index]->setEndValue(QPointF(dx,dy));
    animazioni[index]->setDuration(1000);
    animazioni_d.push_back(animazioni[index]);
    td->start();
}
void GameWindow::HideCard(unsigned int index)
{
    carte[index]->hide();

}
void GameWindow::ShowCard(unsigned int index)
{
    carte[index]->show();

}
GameWindow::~GameWindow()
{
    delete ui;
    while ( carte.size() > 0 )
    {
        delete carte.first();
        carte.pop_front();
    }
    //delete scene;
}

void GameWindow::on_pushButton_clicked()
{
    briscola->sock->write(QString().sprintf("START\n").toAscii());
}

void GameWindow::on_pushButton_2_clicked()
{
    briscola->sock->write(QString().sprintf("LG %d\n",briscola->mygameid).toAscii());
}

void GameWindow::on_GameWindow_destroyed()
{

}
void GameWindow::delayedanim()
{
    while ( animazioni_d.size() > 0)
    {
        animazioni_d.first()->start();
        animazioni_d.pop_front();
    }

}

void GameWindow::on_lineEdit_editingFinished()
{

}
QString htmlutf8escape(QString orig_)
{
    QString orig = Qt::escape(orig_);
    QString res = "";
    for ( int i = 0; i < orig.length(); i++)
    {
        if ( orig[i].unicode() > 127 )// Se il carattere è al di fuori della tabella ASCII standard è necessario fargli l'escape
        {
            res.append(QString("&#")+QString().sprintf("%u",orig[i].unicode())+QString(";"));
        }else
            res.append(orig[i]);
    }
    return res;
}

void GameWindow::OnChatMessage(unsigned int player, QString message)
{
    QString playername = briscola->pllist->GetPlayer(player)->name;
    AppendMessage(0x000000,htmlutf8escape(QString("<%1> %2").arg(playername,message)),0);
}

void GameWindow::on_lineEdit_returnPressed()
{
    QString msg = ui->lineEdit->text();
    ui->lineEdit->setText("");
    if ( msg.trimmed().length() == 0 )
        return;
    std::cout << "Send message: " << (QString("GAMECHAT ")+msg+"\n").toLocal8Bit().data() << std::endl;
    briscola->sock->write((QString("GAMECHAT ")+msg+"\n").toUtf8());
}
