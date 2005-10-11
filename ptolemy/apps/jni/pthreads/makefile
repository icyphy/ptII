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
	gcc -Wall \
		"-I$(PTJAVA_HOME)/../include" \
		"-I$(PTJAVA_HOME)/../include/$(PTJNI_ARCHITECTURE)" \
		 -c test_jni.c
	gcc -shared test_jni.o -fno-exceptions \
		-o ${PTJNI_SHAREDLIBRARY_PREFIX}test.${PTJNI_SHAREDLIBRARY_SUFFIX} \
		-lpthread
	PATH=".;${PATH}" java -verbose -Xcheck:jni -classpath . Test