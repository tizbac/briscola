#ifndef CONNECTDIALOG_H
#define CONNECTDIALOG_H

#include <QDialog>

namespace Ui {
    class Connectdialog;
}

class Connectdialog : public QDialog
{
    Q_OBJECT

public:
    explicit Connectdialog(QWidget *parent = 0);
    ~Connectdialog();

private:
    Ui::Connectdialog *ui;

private slots:
    void on_buttonBox_rejected();
    void on_buttonBox_accepted();
};
extern Connectdialog * cdlg;
#endif // CONNECTDIALOG_H
