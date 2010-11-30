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

#include <QtGui/QApplication>
#include "briscola.h"
#include "connectdialog.h"
#include "gamewindow.h"
#include "opengamedialog.h"
#include "dialog.h"
Connectdialog * cdlg = NULL;
GameWindow * gamewindow = NULL;
int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    Briscola *w = new Briscola;
    cdlg = new Connectdialog(w);
    gamewindow = new GameWindow(w);
    w->show();
    new OpenGameDialog();
    new AboutDialog();
    int cod = a.exec();
    delete gamewindow;
    delete cdlg;
    delete w;
    return cod;
}
