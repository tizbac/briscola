#-------------------------------------------------
#
# Project created by QtCreator 2010-11-23T21:10:57
#
#-------------------------------------------------

QT       += core gui network

TARGET = Briscola
TEMPLATE = app


SOURCES += main.cpp\
        briscola.cpp \
    connectdialog.cpp \
    playerlist.cpp \
    gamelist.cpp \
    gamewindow.cpp \
    opengamedialog.cpp \
    dialog.cpp \
    scoredisplay.cpp

HEADERS  += briscola.h \
    connectdialog.h \
    playerlist.h \
    gamelist.h \
    gamewindow.h \
    opengamedialog.h \
    dialog.h \
    scoredisplay.h

FORMS    += briscola.ui \
    connectdialog.ui \
    gamewindow.ui \
    opengamedialog.ui \
    dialog.ui
target.path = /usr/local/bin
carte.path = /usr/local/share/games/Briscola/carte
carte.files = ./carte/*
menu.path = /usr/local/share/menu
menu.files = ./menu/briscola
icona.path = /usr/local/share/pixmaps
icona.files = ./menu/briscola.xpm
deskfile.path = /usr/local/share/applications
deskfile.files = ./menu/briscola.desktop
INSTALLS += carte
INSTALLS += target
INSTALLS += icona
INSTALLS += menu
INSTALLS += deskfile
QMAKESPEC=linux-g++-32
