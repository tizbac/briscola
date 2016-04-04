#include "opengamedialog.h"
#include "ui_opengamedialog.h"
#include "briscola.h"
OpenGameDialog * ogdlg = NULL;
OpenGameDialog::OpenGameDialog(QWidget *parent) :
    QDialog(parent),
    ui(new Ui::OpenGameDialog)
{
    ui->setupUi(this);
    ogdlg = this;
}

OpenGameDialog::~OpenGameDialog()
{
    delete ui;
}

void OpenGameDialog::on_buttonBox_accepted()
{
    hide();
    unsigned int players = ui->comboBox->currentText().toUInt();
    QString desc = ui->lineEdit->text();
    briscola->sock->write((QString()+"OG "+QString().sprintf("%d",players)+" "+desc+"\n").toLatin1());
}

void OpenGameDialog::on_buttonBox_rejected()
{
    hide();
}
