#!/bin/sh
if [ "$REV" == "" ]; then
  REV=0
  
fi
qmake
rm Makefile_32
sudo rm -rf rootfs/*
cp Makefile Makefile_32
sed -i 's:/usr/local:/usr:g' Makefile_32

sed -i 's/CFLAGS\s*=\(.*\)/CFLAGS=\1 \-m32/' Makefile_32
sed -i 's/CXXFLAGS\s*=\(.*\)/CXXFLAGS=\1 \-m32/' Makefile_32
sed -i 's/LFLAGS\s*=\(.*\)/LFLAGS=\1 \-m32 \-L\/usr\/lib32/' Makefile_32

make -f Makefile_32 clean
make -f Makefile_32 -j4
sudo INSTALL_ROOT=$(pwd)/rootfs make -f Makefile_32 install
( cat dct | sed -e 's/Architecture\:\s*\S*/Architecture: i386/' | sed -e "s/Version:.*/Version: 0\\.1\\.${REV}/") > debian/control
ln -s $(pwd)/debian $(pwd)/rootfs/DEBIAN
dpkg -b ./rootfs Briscolav0.1_rev${REV}_i386.deb

rm Makefile_64
sudo rm -rf rootfs/*
cp Makefile Makefile_64
sed -i 's:/usr/local:/usr:g' Makefile_64
INSTALL_ROOT=$(pwd)/rootfs
make -f Makefile_64 clean
make -f Makefile_64 -j4
sudo INSTALL_ROOT=$(pwd)/rootfs make -f Makefile_64 install
( cat dct | sed -e 's/Architecture\:\s*\S*/Architecture: amd64/' | sed -e "s/Version:.*/Version: 0\\.1\\.${REV}/" ) > debian/control
ln -s $(pwd)/debian $(pwd)/rootfs/DEBIAN
dpkg -b ./rootfs Briscolav0.1_rev${REV}_amd64.deb