
SHELL=/bin/sh
CLASSPATH=jtools

JAVAC=javac
JAVADOC=javadoc
JAVA=java
TEMP=/usr/tmp

export CLASSPATH

default: all_jtools all_ctools core_classes tinyvm_emul

# make the JVM image as well.. This requires the Hitachi gcc backend.
all: default lejos_bin

release:
	make clean
	rm -rf apidocs
	make all
	export TINYVM_VERSION=lejos_`cat VERSION`; make dir_and_zip

dir_and_zip:
	rm -rf ${TEMP}/${TINYVM_VERSION}
	mkdir ${TEMP}/${TINYVM_VERSION}
	cp -r . ${TEMP}/${TINYVM_VERSION}
	cd ${TEMP}/${TINYVM_VERSION}; make remove_useless_files
	cd ${TEMP}; tar cvf ${TINYVM_VERSION}.tar ${TINYVM_VERSION}; gzip ${TINYVM_VERSION}.tar
	diff bin/lejos.srec ${TEMP}/${TINYVM_VERSION}/bin/lejos.srec

remove_useless_files:
	rm -rf `find . -name 'CVS'`
	rm -rf `find . -name '*.o'`
	rm -rf `find . -name '*.class'`
	rm -rf `find . -name '*.tvm'`
	rm -rf `find . -name '*.bin'`
	rm -rf `find . -name '*.exe'`
	rm -rf `find . -name '*.core'`
	rm -rf `find . -name '*.jar'`
	rm -rf `find . -name '*.lst'`
	rm -rf `find . -name 'core'`

release_win:
	make clean
	rm -rf apidocs
	make all
	export TINYVM_VERSION=lejos_win32_`cat VERSION`; make dir_and_zip_win

dir_and_zip_win:
	rm -rf ${TEMP}/lejos
	mkdir ${TEMP}/lejos
	cp -r . ${TEMP}/lejos
	cd ${TEMP}/lejos; make remove_useless_files_win
	cp /bin/cygwin1.dll ${TEMP}/lejos/bin
	cd ${TEMP}; jar cvf ${TINYVM_VERSION}.zip lejos
	diff bin/lejos.srec ${TEMP}/lejos/bin/lejos.srec

remove_useless_files_win:
	rm -rf `find . -name 'CVS'`
	rm -rf `find . -name '*.o'`
	rm -rf `find . -name '*.class'`
	rm -rf `find . -name '*.tvm'`
	rm -rf `find . -name '*.bin'`
	rm -rf `find . -name '*.core'`
	rm -rf `find . -name '*.lst'`
	rm -rf `find . -name 'core'`
	/bin/strip `find . -name '*.exe'`

check_release:
	@echo TINYVM_HOME=${TINYVM_HOME}
	@echo Location of lejosc=`which lejosc`
	@echo Location of lejos=`which lejos`
	make
	cd regression; ./run.sh

# The java tools that compile class files into an executable image.
all_jtools: java_tools generated_files java_loader
	cd jtools; jar cf ../lib/jtools.jar `find . -name '*.class'`

java_tools:
	@echo "====> Making tools"
	${JAVAC} jtools/js/tools/*.java

generated_files: common/classes.db common/signatures.db
	${JAVA} -Dtinyvm.home="." js.tools.GenerateConstants

java_loader:
	@echo "====> Making loader/linker (lejos)"
	${JAVAC} jtools/js/tinyvm/*.java

# The uploading software.
all_ctools:
	cd tools; make

lejos_bin:
	@echo "====> Making leJOS RCX binary (lejos.srec)"
	cd rcx_impl; make

# The emulator.
tinyvm_emul:
	@echo "====> Making leJOS Unix binaries (lejos, for emulation)"
	cd unix_impl; make

# The java classes for the JVM.
core_classes:
	${JAVAC} -classpath classes `find classes -name '*.java'`
	cd classes; jar cf ../lib/classes.jar `find . -name '*.class'`

javadoc:
	if [ ! -d apidocs ]; then mkdir apidocs; fi
	${JAVADOC} -author -d apidocs -sourcepath classes java.io java.lang java.util josx.platform.rcx josx.util

clean:
	rm -rf `find . -name '*.class'`
	rm -rf `find . -name 'core'`
	rm -rf `find . -name '*.o'`
	rm -rf `find . -name '*~'`

cleaner: clean
	rm -rf `find . -name '*.srec'`






