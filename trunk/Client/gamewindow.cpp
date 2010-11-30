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

#include <iostream>
#include <QGraphicsView>
#include <QMessageBox>

GameWindow::GameWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::GameWindow)
{
    ui->setupUi(this);
    setWindowFlags( Qt::CustomizeWindowHint | Qt::WindowTitleHint | Qt::Window);
    hide();
    v = new QGraphicsView(this);

    ui->horizontalLayout_2->addWidget(v);
    w = 800;
    h = 600;
    td = new QTimer();
    td->setSingleShot(true);
    td->setInterval(2000);
    td->stop();
    connect(td,SIGNAL(timeout()),this,SLOT(delayedanim()));
    v->setScene(new QGraphicsScene());
    v->setSceneRect(0,0,w,h);

    QGraphicsScene * scene = v->scene();


    mazzo = new QParallelAnimationGroup();
    v->show();
#ifdef WIN32
    QPixmap cartapx("carte\\retro.jpg");
#else
    QPixmap cartapx("carte/retro.jpg");
#endif
    cardh = cartapx.height();
    cardw = cartapx.width();
    Pixmap * cartagi = new Pixmap(cartapx.scaled(QSize(w/7,h/4),Qt::KeepAspectRatio),-1);
    scene->addItem(cartagi);
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
       // std::cout << carta->width() << std::endl;
        carta = carta.scaled(QSize(w/7,h/4),Qt::KeepAspectRatio);
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

    }
  //  cartagi->show();
   // scene.addItem(cartagi);

    v->setCacheMode(QGraphicsView::CacheBackground);
    v->setRenderHints(QPainter::Antialiasing | QPainter::SmoothPixmapTransform);
    v->setViewportUpdateMode(QGraphicsView::BoundingRectViewportUpdate);


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
        ui->lblturno->setText(briscola->pllist->GetPlayer(briscola->GetGame()->turno)->name);
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
