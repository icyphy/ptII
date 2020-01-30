#!/bin/bash  
# Install CERTI 

CERTI_INSTALL=$HOME/pthla/hla1516e/certi-tools
MYCERTI=$CERTI_INSTALL/share/scripts/myCERTI_env.sh
if [ -f "$MYCERTI" ]; then
    echo "$0: $MYCERTI found, no need to build CERTI."
    echo "In your shell, invoke \"source $MYCERTI\" to set up.";
    exit
fi

SRC=$HOME/pthla/hla1516e
if [ ! -d $SRC ]; then
    mkdir -p $SRC
fi    

CERTI_SRC=$SRC/certi4-0.0
if [ ! -f $CERTI_SRC ]; then

    # Master does not have yet the new data types of HLA 1516-2010 Evolved
    # git clone https://git.savannah.nongnu.org/git/certi.git $CERTI_SRC
    # This branch bellow has implemented the new data types (HLA 1516-2010 Evolved) and compiles under macos
#git clone -b dev-scalian/alaine-macos-compatibility https://git.savannah.nongnu.org/git/certi.git $CERTI_SRC
    # If the command above does not work, try:
    git clone https://git.savannah.nongnu.org/git/certi.git $CERTI_SRC
    git checkout dev-scalian/alaine-compatibility-mac
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
        ;;
    *)
      echo "You may need to install cmake, flex and bison"
      ;;
esac

# Put the build outside the source code
cd $SRC
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

