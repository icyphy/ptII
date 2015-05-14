rm -rf jniTofmu.so
gcc -o jniTofmu.so -g -shared -I/home/thierry/opt/jdk1.7.0_51/include -I/home/thierry/opt/jdk1.7.0_51/include/linux/ jniTofmu.c -lc -std=c99 -D_GNU_SOURCE -fPIC


