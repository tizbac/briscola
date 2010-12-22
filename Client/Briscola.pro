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
