# See http://www.kaffe.org/pipermail/kaffe/2002-May/091305.html 
# export LD_LIBRARY_PATH=.
# export PATH=/usr/local/kaffe/bin:$PATH
# export CLASSPATH=.:/usr/local/kaffe/share/kaffe/Klasses.jar
# javac Test.java
# kaffeh -jni Test
# gcc -Wall -I/usr/local/kaffe/include/kaffe/ -c test_jni.c
# gcc -shared -Wl,-soname,libtest.so -o libtest.so test_jni.o -lpthread

include $(PTII)/mk/ptII.mk

all:
	javac Test.java
	javah -jni Test
	gcc -Wall $(PTJNI_SHAREDLIBRARY_CFLAG) \
		"-I$(PTJAVA_HOME)/../include" \
		"-I$(PTJAVA_HOME)/../include/$(PTJNI_ARCHITECTURE)" \
		 -c test_jni.c
	gcc -shared $(PTJNI_SHAREDLIBRARY_LDFLAG)  test_jni.o -fno-exceptions \
		-o ${PTJNI_SHAREDLIBRARY_PREFIX}test.${PTJNI_SHAREDLIBRARY_SUFFIX} \
		-lpthread -lc 
	LD_LIBRARY_PATH=`pwd` PATH=".:${PATH}" java -classpath . Main
