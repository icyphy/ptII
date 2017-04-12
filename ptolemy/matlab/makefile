# Makefile for Java Ptolemy matlab interface
# Based on standard Ptolemy makefiles
#
# @Version: $Id$
# @Author: Zoltan Kemenczy
#
# This directory contains the matlab engine interface
#
# @Copyright (c) 1997-2014 The Regents of the University of California and
# Research in Motion Limited.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA  OR RESEARCH IN MOTION
# LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
# OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND
# ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA OR
# RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
# SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA  AND RESEARCH IN MOTION LIMITED
# HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

ME =		ptolemy/matlab

DIRS =		demo test

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	matlab
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	Engine.java \
	Expression.java

OTHER_FILES_TO_BE_JARED = \
	matlab-copyright.htm \
	matlab.xml \
	matlab.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	test \
	ptmatlab.cc

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'Engine$$ConversionParameters.class' \
	demo \
	doc

JCLASS = $(JSRCS:%.java=%.class)

# 'make fast' also build PTLIB, so be sure that PTLIB is set to
# the name of the library
PTLIB=$(PTJNI_SHAREDLIBRARY_PREFIX)ptmatlab.$(PTJNI_SHAREDLIBRARY_SUFFIX)

# Files to remove with 'make clean'
KRUFT = \
	*.jnilib \
	*.jar

all: jclass

jclass: $(PTLIB)


# MAJOR_OS_NAME is set by configure to the major name of the OS
# Under all versions of Windows, MAJOR_OS_NAME = Windows
PTPACKAGE_NATIVE_JAR =	$(PTPACKAGE)$(MAJOR_OS_NAME).jar
install: jclass jars $(PTPACKAGE).jar $(PTPACKAGE_NATIVE_JAR) $(PTLIB)
	@echo "'$(PTPACKAGE).jar' requires gcc, so we install it in \$$PTII/lib"
	@echo "for hosts that might not have gcc installed and for Web Start."
	cp $(PTPACKAGE).jar $(PTPACKAGE_NATIVE_JAR) $(ROOT)/lib
	cp $(PTLIB) $(ROOT)/lib

clean: ptmatlab_clean

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

#--- Specialized part of the makefile: ---

# Under Windows, if cl fails, you may need to set INCLUDE and LIB
# export VCINSTALLDIR="C:/Program Files/Microsoft Visual Studio .NET 2003"
# export MSVCDir=${VCINSTALLDIR}/VC7
# export INCLUDE="${MSVCDir}/ATLMFC/INCLUDE;${MSVCDir}/INCLUDE;${MSVCDir}/PlatformSDK/include/prerelease;${SVCDir}/PlatformSDK/include;C:/Program Files/Microsoft Visual Studio .NET 2003/SDK/v1.1/include;${INCLUDE}"
# export LIB="${MSVCDir}/ATLMFC/LIB;${MSVCDir}/LIB;${MSVCDir}/PlatformSDK/lib/prerelease;${MSVCDir}/PlatformSDK/lib;${FrameworkSDKDir}/lib;${LIB}"

# $(PTMATLAB_cC) is set to cl or gcc by configure in $PTII/mk/ptII.mk
$(PTLIB): ptmatlab.cc Engine.class
	if [ "`uname -s`" = "Darwin" ]; then \
	    if [ -d "$(MATLAB_LIBDIR)" -a -d "$(MATLAB_64LIBDIR)" ]; then \
		echo "# Making 32 and 64 bit Universal Library $(MAC_UNIVERSAL_LIB) and then copying to $(PTLIB)"; \
	        $(MAKE) $(MAC_UNIVERSAL_LIB); \
	        mv $(MAC_UNIVERSAL_LIB) $@; \
            else \
	        if [ -d "$(MATLAB_LIBDIR)" ]; then \
		    echo "# Making $(PTLIB_32) and then copying to $(PTLIB)"; \
		    $(MAKE) $(PTLIB_32); \
	            mv $(PTLIB_32) $@; \
                else \
                    if [ -d "$(MATLAB_64LIBDIR)" ]; then \
			echo "# Big bucks! Congrats!! making $(PTLIB_64) and copying to $(PTLIB)"; \
	                $(MAKE)  $(PTLIB_64); \
	                mv $(PTLIB_64) $@; \
                    else \
			echo "# MATLAB_LIBDIR '$(MATLAB_LIBDIR)' and/or MATLAB_64LIBDIR '$(MATLAB_64LIBDIR)' don't exist, try rerunning configure."; \
			exit 1; \
                    fi; \
                fi; \
	    fi; \
	    echo "# The file command reports that $@ is: "; \
	    file $@; \
        else \
	    CLASSPATH="$(CLASSPATH)" "$(JAVAH)" -jni -o ptmatlab.h ptolemy.matlab.impl.impl.mathworks.MathworksEngine; \
	    "$(PTMATLAB_CC)" $(PTMATLAB_CC_FLAGS) \
		"-I$(MATLAB_DIR)/extern/include" \
		"-I$(PTJNI_INCLUDE)" \
		"-I$(PTJAVA_HOME)/../include/$(PTJNI_ARCHITECTURE)" \
		$(PTMATLAB_LD_ARGS); \
        fi

PTLIB_32 = $(PTJNI_SHAREDLIBRARY_PREFIX)ptmatlab32.$(PTJNI_SHAREDLIBRARY_SUFFIX)
PTLIB_64 = $(PTJNI_SHAREDLIBRARY_PREFIX)ptmatlab64.$(PTJNI_SHAREDLIBRARY_SUFFIX)
$(PTLIB_32): ptmatlab.cc Engine.class
	CLASSPATH="$(CLASSPATH)" "$(JAVAH)" -jni -o ptmatlab.h ptolemy.matlab.impl.impl.mathworks.MathworksEngine
	"$(PTMATLAB_CC)" $(PTMATLAB_CC_FLAGS) \
		"-I$(MATLAB_DIR)/extern/include" \
		"-I$(PTJNI_INCLUDE)" \
		"-I$(PTJAVA_HOME)/../include/$(PTJNI_ARCHITECTURE)" \
		$(PTMATLAB_LD_ARGS)

$(PTLIB_64): ptmatlab.cc Engine.class
	CLASSPATH="$(CLASSPATH)" "$(JAVAH)" -jni -o ptmatlab.h ptolemy.matlab.impl.impl.mathworks.MathworksEngine
	"$(PTMATLAB_CC)" $(PTMATLAB_CC_FLAGS) -m64 \
		"-I$(MATLAB_DIR)/extern/include" \
		"-I$(PTJNI_INCLUDE)" \
		"-I$(PTJAVA_HOME)/../include/$(PTJNI_ARCHITECTURE)" \
		$(PTMATLAB_64LD_ARGS);

MAC_UNIVERSAL_LIB = $(PTJNI_SHAREDLIBRARY_PREFIX)ptmatlabUniversal.$(PTJNI_SHAREDLIBRARY_SUFFIX)
mac_universal_lib: $(MAC_UNIVERSAL_LIB)
$(MAC_UNIVERSAL_LIB): $(PTLIB_32) $(PTLIB_64)
	@echo "# Creating $@ from $(PTLIB_32) $(PTLIB_64)"
	lipo -create -arch i386 $(PTLIB_32) -arch x86_64 $(PTLIB_64) -o $@

# Rule to build under Microsoft Visual C
$(PTLIB)_msvc: ptmatlab.cc Engine.class
	CLASSPATH="$(CLASSPATH)" "$(JAVAH)" -jni -o ptmatlab.h ptolemy.matlab.impl.impl.mathworks.MathworksEngine
	cl \
		"-I$(MATLAB_DIR)/extern/include" \
		"-I$(PTJAVA_HOME)/../include" \
		"-I$(PTJAVA_HOME)/../include/win32" \
		/Tpptmatlab.cc \
		-Fe$@ \
		/link /dll \
		/libpath:\""$(MATLAB_DIR)/extern/lib/win32/microsoft"\" \
		libeng.lib libmx.lib




# Build using gcc
# Note that the Matlab interface does not work under Cygwin with gcc-3.3.3.
#
# $(PTCC) is set to gcc by configure in $PTII/mk/ptII.mk
# -fno-exceptions  - see note at bottom
# $(PTJNI_NO_CYGWIN) is sometimes set to -mno-cygwin and causes
# "Invalid access to memory location" errors
ptlib_gcc: $(PTLIB)_gcc
$(PTLIB)_gcc: ptmatlab.cc Engine.class
	CLASSPATH="$(CLASSPATH)" "$(JAVAH)" -jni -o ptmatlab.h ptolemy.matlab.impl.impl.mathworks.MathworksEngine
	$(PTCC) -shared \
		"-I$(MATLAB_DIR)/extern/include" \
		"-I$(PTJAVA_HOME)/../include" \
		"-I$(PTJAVA_HOME)/../include/$(PTJNI_ARCHITECTURE)" \
		$< \
		-fno-exceptions \
		-o $@ \
		"-L$(MATLAB_LIBDIR)" $(PTJNI_SHAREDLIBRARY_LDFLAG) -lc -leng -lmx

# Jar file for Web Start that contains the dll
$(PTPACKAGE_NATIVE_JAR): $(PTLIB)
	rm -rf $(PTJAR_TMPDIR) $@
	mkdir $(PTJAR_TMPDIR)
	-cp $< $(PTJAR_TMPDIR)
	@echo "Creating $@"
	(cd $(PTJAR_TMPDIR); "$(JAR)" -cvf tmp.jar .)
	mv $(PTJAR_TMPDIR)/tmp.jar $@
	rm -rf $(PTJAR_TMPDIR)


ptmatlab_clean:
	rm -f ptmatlab.h *.o $(PTLIB)

# Note: -fno-exceptions was added to remove the exception handling frames,
# which for gcc 3.2-3 (prerelease) was introducing an unresolved
# gcc_personality_v0 external, nowhere to be found within the cygwin libraries
# and object files...
