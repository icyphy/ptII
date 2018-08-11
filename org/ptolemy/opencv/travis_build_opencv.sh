#!/bin/bash
# Build OpenCV
# From: https://github.com/nebgnahz/cv-rs/blob/master/.ci/travis_build_opencv.sh
#
# .travis.yml should look like:
#
# before_install:
#  - sudo -E ./.ci/travis_build_opencv.sh
#
# cache:
#  timeout: 1000
#  directories:
#    - $HOME/usr/installed-version
#    - $HOME/usr/include
#   - $HOME/usr/lib

set -eux -o pipefail

OPENCV_VERSION=${OPENCV_VERSION:-3.4.1}
URL=https://github.com/opencv/opencv/archive/$OPENCV_VERSION.zip
URL_CONTRIB=https://github.com/opencv/opencv_contrib/archive/$OPENCV_VERSION.zip
SRC=$HOME/src
OPENCV_BUILD=$SRC/opencv-$OPENCV_VERSION/build
OPENCV_CONTRIB=$SRC/opencv_contrib-$OPENCV_VERSION/modules
INSTALL_PREFIX=$PTII/vendors/opencv
INSTALL_FLAG=$INSTALL_PREFIX/share/OpenCV/java


# Avoid "Package libdc1394-22-dev is not available, but is referred to by another package."
# See https://github.com/travis-ci/travis-ci/issues/5221
sudo apt-get update 

# Install shared libraries necessary for compilation and runtime.  We need liblapack.so.3 etc.
sudo apt-get install -y cmake pkg-config ninja-build zlib1g-dev libjpeg8-dev libtiff5-dev libopenexr-dev libavcodec-dev libavformat-dev libswscale-dev libv4l-dev libdc1394-22-dev libxine2-dev libgphoto2-dev libgtk2.0-dev libtbb-dev libeigen3-dev libblas-dev liblapack-dev liblapacke-dev libatlas-base-dev libhdf5-dev libprotobuf-dev libgflags-dev libgoogle-glog-dev

# The packages below did not work for me under Ubuntu 17.x:
# sudo apt-get install -y libjasper-dev libpng12-dev libgstreamer0.10-dev libgstreamer-plugins-base0.10-dev

if [ ! -d $INSTALL_FLAG ]; then
    echo "$0: $INSTALL_FLAG does not exist or is not a directory."
    # Because set -e was invoked, ls will return non-zero if the
    # directory does not exist.  So we check that it exists first
    if [ ! -d $INSTALL_PREFIX ]; then
        echo "$0: $INSTALL_PREFIX does not exist or is not a directory."
    else
        ls -R $INSTALL_PREFIX
    fi
    if [ ! -d $OPENCV_BUILD ]; then
        echo "$0: $OPENCV_BUILD  does not exist or is not a directory, so we download files and create the directory."
	OPENCV_TAR=/tmp/opencv-${OPENCV_VERSION}.tar.gz
	if [ ! -f $OPENCV_TAR ]; then
            echo "$0: Downloading $OPENCV_TAR"
	    wget -O $OPENCV_TAR https://github.com/opencv/opencv/archive/${OPENCV_VERSION}.tar.gz
	fi
	OPENCV_CONTRIB_TAR=/tmp/opencv_contrib-${OPENCV_VERSION}.tar.gz
	if [ ! -f $OPENCV_CONTRIB_TAR ]; then
            echo "$0: Downloading $OPENCV_CONTRIB_TAR"
	    wget -O /tmp/opencv_contrib-${OPENCV_VERSION}.tar.gz https://github.com/opencv/opencv_contrib/archive/${OPENCV_VERSION}.tar.gz
	fi

	if [ ! -d $SRC ]; then
	    mkdir $SRC
	fi

	(cd $SRC; tar -zxf $OPENCV_TAR)
	(cd $SRC; tar -zxf $OPENCV_CONTRIB_TAR)

        mkdir -p $OPENCV_BUILD
    fi

    pushd $OPENCV_BUILD

    # CUDA configuration
    # cmake \
    #     -D WITH_CUDA=ON \
    #     -D BUILD_EXAMPLES=OFF \
    #     -D BUILD_TESTS=OFF \
    #     -D BUILD_PERF_TESTS=OFF  \
    #     -D BUILD_opencv_java=OFF \
    #     -D BUILD_opencv_python=OFF \
    #     -D BUILD_opencv_python2=OFF \
    #     -D BUILD_opencv_python3=OFF \
    #     -D OPENCV_EXTRA_MODULES_PATH=$OPENCV_CONTRIB \
    #     -D CMAKE_INSTALL_PREFIX=$INSTALL_PREFIX \
    #     -D CMAKE_BUILD_TYPE=Release \
    #     -D CUDA_ARCH_BIN=5.2 \
    #     -D CUDA_ARCH_PTX="" \
    #     ..


    # OpenCV Java configuration.
    cmake -Wno-dev \
      -DBUILD_DOCS:BOOL=OFF \
      -DBUILD_EXAMPLES:BOOL=OFF \
      -DBUILD_PACKAGE:BOOL=OFF \
      -DBUILD_PERF_TESTS:BOOL=OFF \
      -DBUILD_TESTS:BOOL=OFF \
      -DBUILD_WITH_DEBUG_INFO:BOOL=OFF \
      -DBUILD_ITT:BOOL=OFF \
      -DCV_TRACE:BOOL=OFF \
      -DENABLE_PYLINT:BOOL=OFF \
      -DWITH_CUDA:BOOL=OFF \
      -DWITH_CUBLAS:BOOL=OFF \
      -DWITH_CUFFT:BOOL=OFF \
      -DWITH_NVCUVID:BOOL=OFF \
      -DWITH_ITT:BOOL=OFF \
      -DWITH_MATLAB:BOOL=OFF \
      -DWITH_OPENCL:BOOL=OFF \
      -DWITH_VTK:BOOL=OFF \
      -DBUILD_opencv_apps:BOOL=OFF \
      -DBUILD_opencv_cudaarithm:BOOL=OFF \
      -DBUILD_opencv_cudabgsegm:BOOL=OFF \
      -DBUILD_opencv_cudacodec:BOOL=OFF \
      -DBUILD_opencv_cudafeatures2d:BOOL=OFF \
      -DBUILD_opencv_cudafilters:BOOL=OFF \
      -DBUILD_opencv_cudaimgproc:BOOL=OFF \
      -DBUILD_opencv_cudalegacy:BOOL=OFF \
      -DBUILD_opencv_cudaobjdetect:BOOL=OFF \
      -DBUILD_opencv_cudaoptflow:BOOL=OFF \
      -DBUILD_opencv_cudastereo:BOOL=OFF \
      -DBUILD_opencv_cudawarping:BOOL=OFF \
      -DBUILD_opencv_cudev:BOOL=OFF \
      -DBUILD_opencv_java:BOOL=ON \
      -DBUILD_opencv_js:BOOL=OFF \
      -DBUILD_opencv_python2:BOOL=OFF \
      -DBUILD_opencv_python3:BOOL=OFF \
      -DBUILD_opencv_ts:BOOL=OFF \
      -DBUILD_opencv_viz:BOOL=OFF \
      -DBUILD_opencv_world:BOOL=OFF \
      -DBUILD_opencv_contrib_world:BOOL=OFF \
      -DBUILD_opencv_matlab:BOOL=OFF \
      -DBUILD_opencv_ccalib:BOOL=OFF \
      -DBUILD_opencv_cvv:BOOL=OFF \
      -DBUILD_opencv_hdf:BOOL=OFF \
      -DBUILD_opencv_sfm:BOOL=OFF \
      -DBUILD_opencv_structured_light:BOOL=OFF \
      -DBUILD_opencv_surface_matching:BOOL=OFF \
      -DCMAKE_BUILD_TYPE:STRING=Release \
      -DCMAKE_INSTALL_PREFIX:PATH=$INSTALL_PREFIX \
      -DOPENCV_ENABLE_NONFREE:BOOL=ON \
      -DOPENCV_EXTRA_MODULES_PATH:PATH=$OPENCV_CONTRIB $OPENCV_BUILD/..
              
    make install && sudo mkdir -p "$(dirname "$INSTALL_FLAG")" && sudo touch "$INSTALL_FLAG";
    popd
    touch $HOME/fresh-cache

fi

sudo cp -r $INSTALL_PREFIX/include/* /usr/local/include/
sudo cp -r $INSTALL_PREFIX/lib/* /usr/local/lib/
if [ ! -d /usr/lib/jni ]; then
    sudo mkdir /usr/lib/jni
fi
sudo cp $INSTALL_PREFIX/share/OpenCV/java/*so /usr/lib/jni/
sudo mkdir -p /usr/share/OpenCV/java/
sudo cp $INSTALL_PREFIX/share/OpenCV/java/*  /usr/share/OpenCV/java/
sudo sh -c "echo \"$INSTALL_PREFIX/lib\" > /etc/ld.so.conf.d/opencv.conf"
sudo sh -c "echo \"$INSTALL_PREFIX/share/OpenCV/java\" > /etc/ld.so.conf.d/opencv-java.conf"
sudo ldconfig
