Name "Briscola"
OutFile "Briscola_v0.1_rev5_setup.exe"
InstallDir $PROGRAMFILES\Briscola
DirText "Il gioco di Briscola online verrà installato sul tuo pc, scegli una directory"
Page license
Page directory
Page components
Page instfiles

Section "Client"
  SetOutPath $INSTDIR
  File Briscola.exe
  File libgcc_s_dw2-1.dll
  File QtGui4.dll
  File QtSvg4.dll
  File QtNetwork4.dll
  File QtXml4.dll
  File QtCore4.dll
  File mingwm10.dll
  SetOutPath $INSTDIR\carte
  File carte\0.png
  File carte\10.png
  File carte\11.png
  File carte\12.png
  File carte\13.png
  File carte\14.png
  File carte\15.png
  File carte\16.png
  File carte\17.png
  File carte\18.png
  File carte\19.png
  File carte\1.png
  File carte\20.png
  File carte\21.png
  File carte\22.png
  File carte\23.png
  File carte\24.png
  File carte\25.png
  File carte\26.png
  File carte\27.png
  File carte\28.png
  File carte\29.png
  File carte\2.png
  File carte\30.png
  File carte\31.png
  File carte\32.png
  File carte\33.png
  File carte\34.png
  File carte\35.png
  File carte\36.png
  File carte\37.png
  File carte\38.png
  File carte\39.png
  File carte\3.png
  File carte\4.png
  File carte\5.png
  File carte\6.png
  File carte\7.png
  File carte\8.png
  File carte\9.png
  File carte\retro.jpg
  SetOutPath $INSTDIR
  CreateDirectory "$SMPROGRAMS\Briscola Online"
  CreateShortCut "$SMPROGRAMS\Briscola Online\Briscola.lnk" "$INSTDIR\Briscola.exe"
  WriteUninstaller $INSTDIR\Uninstall.exe
  CreateShortCut "$SMPROGRAMS\Briscola Online\Disinstalla Briscola Online.lnk" "$INSTDIR\Uninstall.exe"
SectionEnd
Section "Uninstall"
	Delete $INSTDIR\Briscola.exe
	Delete $INSTDIR\libgcc_s_dw2-1.dll
	Delete $INSTDIR\QtGui4.dll
	Delete $INSTDIR\QtSvg4.dll
	Delete $INSTDIR\QtNetwork4.dll
	Delete $INSTDIR\QtCore4.dll
	Delete $INSTDIR\QtXml4.dll
	Delete $INSTDIR\mingwm10.dll
	Delete $INSTDIR\Uninstall.exe
	Delete $INSTDIR\carte\10.png
	Delete $INSTDIR\carte\11.png
	Delete $INSTDIR\carte\12.png
	Delete $INSTDIR\carte\13.png
	Delete $INSTDIR\carte\14.png
	Delete $INSTDIR\carte\15.png
	Delete $INSTDIR\carte\16.png
	Delete $INSTDIR\carte\17.png
	Delete $INSTDIR\carte\18.png
	Delete $INSTDIR\carte\19.png
	Delete $INSTDIR\carte\1.png
	Delete $INSTDIR\carte\20.png
	Delete $INSTDIR\carte\21.png
	Delete $INSTDIR\carte\22.png
	Delete $INSTDIR\carte\23.png
	Delete $INSTDIR\carte\24.png
	Delete $INSTDIR\carte\25.png
	Delete $INSTDIR\carte\26.png
	Delete $INSTDIR\carte\27.png
	Delete $INSTDIR\carte\28.png
	Delete $INSTDIR\carte\29.png
	Delete $INSTDIR\carte\2.png
	Delete $INSTDIR\carte\30.png
	Delete $INSTDIR\carte\31.png
	Delete $INSTDIR\carte\32.png
	Delete $INSTDIR\carte\33.png
	Delete $INSTDIR\carte\34.png
	Delete $INSTDIR\carte\35.png
	Delete $INSTDIR\carte\36.png
	Delete $INSTDIR\carte\37.png
	Delete $INSTDIR\carte\38.png
	Delete $INSTDIR\carte\39.png
	Delete $INSTDIR\carte\3.png
	Delete $INSTDIR\carte\4.png
	Delete $INSTDIR\carte\5.png
	Delete $INSTDIR\carte\6.png
	Delete $INSTDIR\carte\7.png
	Delete $INSTDIR\carte\8.png
	Delete $INSTDIR\carte\9.png
	Delete $INSTDIR\carte\retro.jpg
	Delete "$SMPROGRAMS\Briscola Online\Briscola.lnk"
	RMDir "$SMPROGRAMS\Briscola Online"
	RMDir $INSTDIR\carte
	RMDir $INSTDIR
SectionEnd