#ifndef GAMEWINDOW_H
#define GAMEWINDOW_H
#include "gamelist.h"
#include <QMainWindow>
#include <QGraphicsScene>
#include <QParallelAnimationGroup>
#include <QGraphicsPixmapItem>
#include <QPropertyAnimation>
#include <QParallelAnimationGroup>
#include <QTimer>
#include <iostream>
#include "briscola.h"
namespace Ui {
    class GameWindow;
}
class Pixmap : public QObject, public QGraphicsPixmapItem
{
    Q_OBJECT
    Q_PROPERTY(QPointF pos READ pos WRITE setPos)
public:
    Pixmap(const QPixmap &pix,int idcarta)
        : QObject(), QGraphicsPixmapItem(pix)
    {
        setCacheMode(DeviceCoordinateCache);
        cid = idcarta;
    }
    int cid;
    void mousePressEvent ( QGraphicsSceneMouseEvent * event )
    {
        std::cout << "Mouse press " << cid << std::endl;
        Game * g = briscola->GetGame();

        if ( g )
        {
            if ( g->started && g->turno == briscola->myid )
            {
                for ( int i = 0; i < g->hand.size(); i++ )
                {
                    if ( cid == g->hand[i] )
                    {
                        briscola->sock->write(QString().sprintf("UC %d\n",i).toAscii());
                        break;
                    }

                }
            }

        }

    }
};


class GameWindow : public QMainWindow
{
    Q_OBJECT

public:
    int cardh,cardw;
    explicit GameWindow(QWidget *parent = 0);
    void OnGameJoin(bool ishost,Game * game);
    void OnUpdate();
    void OnGameLeft(unsigned int playerid);
    void OnTableTaken(unsigned int playerid);
    void AnimateCard(unsigned int index,float sx , float sy , float dx , float dy);
    void AnimateCardDelayed(unsigned int index, float sx, float sy, float dx, float dy);
    void UpdateStatus();
    void ShowCard(unsigned int index);
    void HideCard(unsigned int index);
    void UpdatePlayers();
    void OnGameStarted();
    ~GameWindow();

private:
    Ui::GameWindow *ui;
    int h,w;
    QList<Pixmap*> carte;
    QList<QPropertyAnimation*> animazioni;
    QList<QPropertyAnimation*> animazioni_d;
    QTimer * td;
    QPixmap *cartagirata;
    QParallelAnimationGroup * mazzo;
    QGraphicsView* v;
private slots:
    void on_GameWindow_destroyed();
    void on_pushButton_2_clicked();
    void on_pushButton_clicked();
    void delayedanim();
};
extern GameWindow * gamewindow;
#endif // GAMEWINDOW_H
