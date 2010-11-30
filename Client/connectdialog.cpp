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
#include "connectdialog.h"
#include "ui_connectdialog.h"
#include "briscola.h"
Connectdialog::Connectdialog(QWidget *parent) :
    QDialog(parent),
    ui(new Ui::Connectdialog)
{
    ui->setupUi(this);
}

Connectdialog::~Connectdialog()
{
    delete ui;
}

void Connectdialog::on_buttonBox_accepted()
{
    this->hide();
    briscola->host = ui->txthost->text();
    briscola->username = ui->lineEdit->text();
    briscola->password = ui->lineEdit_2->text();
    briscola->ConnectToServer();
}

void Connectdialog::on_buttonBox_rejected()
{
    this->hide();
}
