#ifndef OPENGAMEDIALOG_H
#define OPENGAMEDIALOG_H

#include <QDialog>

namespace Ui {
    class OpenGameDialog;
}

class OpenGameDialog : public QDialog
{
    Q_OBJECT

public:
    explicit OpenGameDialog(QWidget *parent = 0);
    ~OpenGameDialog();

private:
    Ui::OpenGameDialog *ui;

private slots:
    void on_buttonBox_rejected();
    void on_buttonBox_accepted();
};
extern OpenGameDialog * ogdlg;
#endif // OPENGAMEDIALOG_H
