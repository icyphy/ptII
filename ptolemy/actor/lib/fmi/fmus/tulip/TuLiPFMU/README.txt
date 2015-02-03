ptolemy/actor/lib/fmi/fmus/tulip/TuLiPFMU/README.txt

$Id$

This uses ecos (https://github.com/ifa-ethz/ecos) to solve optimization problem. It is under GPLv3 license.

To download and install ecos:

git clone https://github.com/ifa-ethz/ecos
cd ecos
make
sudo cp libecos.a /usr/local/lib
sudo mkdir /usr/local/include/ecos
sudo cp include/*.h /usr/local/include/ecos

sudo mkdir -p /usr/local/include/ecos/external/amd/include
sudo mkdir -p /usr/local/include/ecos/external/ldl/include
sudo mkdir /usr/local/include/ecos/external/SuiteSparse_config

sudo cp external/amd/include/* /usr/local/include/ecos/external/amd/include/
sudo cp external/ldl/include/ldl.h /usr/local/include/ecos/external/ldl/include/
sudo cp external/SuiteSparse_config/SuiteSparse_config.h /usr/local/include/ecos/external/SuiteSparse_config


Then, build the FMU:
cd $PTII/ptolemy/actor/lib/fmi/fmus/tulip/TuLiPFMU/
make
$PTII/bin/vergil TuLiPFMU.xml
