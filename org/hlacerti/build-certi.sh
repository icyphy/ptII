#!/bin/bash
# Install CERTI and JCerti


SRC=$HOME/src
if [ ! -d $SRC ]; then
    mkdir -p $SRC
fi    

CERTI_SRC=$SRC/CERTI-3.5.1-Source
if [ ! -f $CERTI_SRC ]; then
    CERTI_TAR=/tmp/certi.tar.gz
    if [ ! -f $CERTI_TAR ]; then
        wget -O $CERTI_TAR http://download.savannah.gnu.org/releases/certi/CERTI-3.5.1-Source.tar.gz
    fi
    echo "$0: untaring $CERTI_TAR in $SRC"
    (cd $SRC; tar -zxf $CERTI_TAR);
fi
    
cd $CERTI_SRC
mkdir build
cd build
echo "$0: running cmake in `pwd`"
cmake -DCMAKE_INSTALL_PREFIX=$PTII/vendors/certi $CERTI_SRC
make
mkdir $PTII/vendors/certi
make install

# No need to build and install jcerti, we have $PTII/lib/jcerti.jar
# JCERTI_SRC=$SRC/jcerti
# if [ ! -f $JCERTI_SRC ]; then
#     JCERTI_ZIP=/tmp/jcerti.zip
#     if [ ! -f $JCERTI_ZIP ]; then
#         wget -O $JCERTI_ZIP http://download.savannah.gnu.org/releases/certi/jcerti-1_0_0-src.zip
#     fi
#     echo "$0: unzipping $JCERTI_ZIP in $SRC"
#     (cd $SRC; unzip -qu /tmp/jcerti.zip)
# fi

# cd $JSRC_SRC
# ant
