#!/bin/bash
# Install CERTI 

CERTI_INSTALL=$HOME/pthla/certi-tools
MYCERTI=$CERTI_INSTALL/share/scripts/myCERTI_env.sh
if [ -f "$MYCERTI" ]; then
    echo "$0: $MYCERTI found, no need to build CERTI."
    echo "In your shell, invoke \"source $MYCERTI\" to set up.";
    exit
fi

SRC=$HOME/pthla
if [ ! -d $SRC ]; then
    mkdir -p $SRC
fi    

# CERTI_SRC=$SRC/CERTI-3.5.1-Source
# if [ ! -f $CERTI_SRC ]; then
#     CERTI_TAR=/tmp/certi.tar.gz
#     if [ ! -f $CERTI_TAR ]; then
#         wget -O $CERTI_TAR http://download.savannah.gnu.org/releases/certi/CERTI-3.5.1-Source.tar.gz
#     fi
#     echo "$0: untaring $CERTI_TAR in $SRC"
#     (cd $SRC; tar -zxf $CERTI_TAR);
# fi
    
CERTI_SRC=$SRC/certi-4.0.0
if [ ! -f $CERTI_SRC ]; then
    # No longer need this branch (as of Oct. 2018).
    # git clone -b br_jbch_4.0.0 https://git.savannah.nongnu.org/git/certi.git $CERTI_SRC
    git clone https://git.savannah.nongnu.org/git/certi.git $CERTI_SRC

    if [ ! -f $CERTI_SRC ]; then
        echo "$0: Hmm.  $CERTI_SRC was not created by running"
        echo "   git clone https://git.savannah.nongnu.org/git/certi.git $CERTI_SRC"
        echo "   If the error message above is "
        echo "   'SSL certificate problem: certificate has expired'"
        echo "   then see https://savannah.gnu.org/maintenance/SavannahTLSInfo/"
        echo "   and https://www.mail-archive.com/savannah-hackers@gnu.org/msg22507.html"
        echo "   Under macOS 10.14, as root consider editing /etc/ssl/cert.pem"
        echo "   and removing the DST Root CA X3 cert."
        echo "   See https://stackoverflow.com/questions/69387175/git-for-windows-ssl-certificate-problem-certificate-has-expired/69396425#69396425"
        exit 2
    fi        
    echo "Patching $CERTI_SRC/CMakeLists.txt by moving a double quote and avoiding \"clang: error: no such file or directory: ';-flat_namespace'\" "
    echo "See https://savannah.nongnu.org/bugs/index.php?53964"

    sed -e 's/SET (CMAKE_SHARED_LINKER_FLAGS \${CMAKE_SHARED_LINKER_FLAGS_INIT} "-flat_namespace -undefined suppress"/SET (CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS_INIT} -flat_namespace -undefined suppress"/' \
        -e 's/SET (CMAKE_MODULE_LINKER_FLAGS \${CMAKE_MODULE_LINKER_FLAGS_INIT} "-flat_namespace -undefined suppress"/SET (CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS_INIT} -flat_namespace -undefined suppress"/' \
        $CERTI_SRC/CMakeLists.txt > $CERTI_SRC/CMakeLists.txt.tmp

    diff $CERTI_SRC/CMakeLists.txt $CERTI_SRC/CMakeLists.txt.tmp
    cp $CERTI_SRC/CMakeLists.txt.tmp  $CERTI_SRC/CMakeLists.txt
    echo ""
fi

OS=`uname -s`
case $OS in
    *Linux*)
        echo "Installing packages"
        sudo apt-get install -y cmake flex bison
        # CERTI-3.5.1 fails to compile with gcc-5, so try clang.
        # See https://lists.gnu.org/archive/html/certi-devel/2015-10/msg00007.html
        # sudo apt-get install -y clang-3.6
        # CMAKE_FLAGS="-DCMAKE_C_COMPILER=clang-3.6 -DCMAKE_CXX_COMPILER=clang-3.6"
        ;;

    Darwin)
        # https://savannah.nongnu.org/bugs/index.php?53620
        CMAKE_FLAGS=-DFORCE_NO_X11=ON

        echo "Fixing for Darwin, see https://savannah.nongnu.org/bugs/index.php?53592"
        sed 's/#include <features.h>//' $CERTI_SRC/libHLA/SemaphorePosix.hh > $CERTI_SRC/libHLA/SemaphorePosix.hh.tmp 
        diff $CERTI_SRC/libHLA/SemaphorePosix.hh $CERTI_SRC/libHLA/SemaphorePosix.hh.tmp 
        mv $CERTI_SRC/libHLA/SemaphorePosix.hh.tmp $CERTI_SRC/libHLA/SemaphorePosix.hh
        echo ""
        ;;
    *)
      echo "You may need to install cmake, flex and bison"
      ;;
esac


cd $CERTI_SRC
mkdir build
cd build
echo "$0: running cmake in `pwd`"
cmake -DCMAKE_INSTALL_PREFIX=$CERTI_INSTALL $CMAKE_FLAGS $CERTI_SRC

echo "$0: running make in `pwd`"
make

mkdir -p $CERTI_INSTALL

echo "$0: running make install in `pwd`"
make install

case $OS in
    Darwin)
        echo "$0: Darwin: Create links for shared libraries in /usr/local/lib"
        if [ ! -d "$CERTI_INSTALL/lib" ]; then
            mkdir $CERTI_INSTALL/lib
        fi 
        files=`(cd $CERTI_INSTALL/lib; ls -1 lib*)`
        for file in $files
        do                    
            if [ -f /usr/local/lib/$file ]; then
                echo " "
                echo "There are shared libraries in /usr/local/lib from another CERTI installation, consider removing them with:"
                echo " "
                echo "  sudo sh -c \"cd /usr/local/lib; rm `(cd $CERTI_INSTALL/lib; ls -1 lib* | awk '{printf("%s ", $1)} END{printf("\n")}')` \" "
                echo " "
                break;
            fi
        done
        echo "Recent macOS releases have a hard time with shared libraries, so create links in /usr/local/lib by running:"
        echo " "
        echo "  sudo sh -c \"cd /usr/local/lib; ln -s $CERTI_INSTALL/lib/* .\""
    ;;
esac

echo " "
echo "To set the CERTI environment variables under bash, do:"
echo " "
echo "   source $CERTI_INSTALL/share/scripts/myCERTI_env.sh"

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
