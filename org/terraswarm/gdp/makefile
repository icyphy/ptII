# Makefile org.terraswarm Global Data Plane sources
#
# @Version: $Id: makefile 69602 2014-07-30 14:20:15Z cxh $
# @Author: Christopher Brooks
#
# @Copyright (c) 2014 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

DIRS = apps

# Root of Ptolemy II directory
ROOT =		../../..

# $(ME) is used in ptdir.mk
ME =		org.terraswarm.gdp

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JNA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	gdp
#PTCLASSJAR =	$(PTPACKAGE).jar
#PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =

JSRCS = \
	EP_DBG.java \
	EP_STAT.java \
	EP_TIME_SPEC.java \
	Event2Library.java \
	Gdp10Library.java \
	ep_stat_to_string.java \
	evbuffer_cb_info.java \
	evbuffer_ptr.java \
	gdp_datum.java

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

all: suball
install: subinstall
# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS)

JCLASS = $(JSRCS:%.java=%.class)

KRUFT = .cpp *.c *.choices


JNAERATOR_JAR = jnaerator-0.12-20140626.234513-58-shaded.jar

all: jclass
install: jclass jars

GDP_SRC = src/gdp

GDP_H_FILES = \
	$(GDP_SRC)/gdp/gdp.h \
	$(GDP_SRC)/ep/ep.h \
	$(GDP_SRC)/ep/ep_dbg.h

LIBEVENT_VERSION = 2.0.21-stable
LIBEVENT=libevent-$(LIBEVENT_VERSION)
LIBEVENT_SRC=src/$(LIBEVENT)


LIBEVENT_H_FILES = \
	$(LIBEVENT_SRC)/include/event2/buffer.h

# Rebuild the jar file
jna: $(JNAERATOR_JAR) src/gdp
	java -jar $(JNAERATOR_JAR) -runtime JNA -library gdp.1.0 -I $(GDP_SRC) $(GDP_H_FILES) -o $(PTII) -package org.terraswarm.gdp  -v  -noJar -noComp
	java -jar $(JNAERATOR_JAR) -runtime JNA -library event2 -I $(LIBEVENT2_SRC)/include $(LIBEVENT_H_FILES) -o $(PTII) -package org.terraswarm.gdp  -v  -noJar -noComp
	# Fix up some issues:
	for file in *.java; do \
	echo "Fixing $$file"; \
	sed -e 's@NativeSize@NativeSizeT@g' \
	    -e 's@import com.ochafik.lang.jnaerator.runtime.NativeSizeT;@import org.ptolemy.fmi.NativeSizeT; //Use NativeSizeT intead of NativeSize@' \
	    -e 's@import com.ochafik.lang.jnaerator.runtime.globals.GlobalInt;@// import com.ochafik.lang.jnaerator.runtime.globals.GlobalInt; // Fixed by cxh in makefile.@' \
	    -e 's@public static final Pointer EP_GEN_DEADBEEF = (Pointer)0xDEADBEEF;@public static final Pointer EP_GEN_DEADBEEF = new Pointer(0xDEADBEEF); // Fixed by cxh in makefile.@' \
	    -e 's@\(public static final GlobalInt __EpDbgCurGen = new GlobalInt(Gdp[0-9]*Library.JNA_NATIVE_LIB, "__EpDbgCurGen");\)@//\1@' \
	    -e 's@\(.*NativeSizeTByReference.*\)@// \1 // Commented out by makefile because NativeSizeTByReference is not in jna.@' \
		$$file > /tmp/gdp.fix; \
	mv /tmp/gdp.fix $$file; \
        done


# libevent:
# "The libevent API provides a mechanism to execute a callback function
# when a specific event occurs on a file descriptor or after a timeout
# has been reached. Furthermore, libevent also support callbacks due
# to signals or regular timeouts."
#
# We have a local copy so that we can run JNAeator
#
# Run "make libevent" to get the libevent source.
libevent: $(LIBEVENT_SRC)
$(LIBEVENT_SRC):
	# Github is too stupid to handle wget, so you will need to go to http://libevent.org, download it and thenrun
	# cd src; tar -zxf $(LIBEVENT).tar.gz

update:
	cd src/gdp; git pull

# Run "make gdp" to get the gdp source.
gdp: $(GDP_SRC)

$(GDP_SRC):
	# You will need a password for this, ask Eric.
	@if [! -d src ]; then mkdir src; echo "making the src/ directory."; fi
	cd src; git clone https://repo.eecs.berkeley.edu/git/projects/swarmlab/gdp.git

jnaerator-0.12-20140626.234513-58-shaded.jar:
	wget --no-check-certificate https://oss.sonatype.org/content/groups/public/com/nativelibs4java/jnaerator/0.12-SNAPSHOT/jnaerator-0.12-20140626.234513-58-shaded.jar


# jnaerator-0.11-shaded.jar does not generate getFieldOrder(), which is needed in more recent versions of JNA.
#
#jnaerator-0.11-shaded.jar:
#	wget http://search.maven.org/remotecontent?filepath=com/nativelibs4java/jnaerator/0.11/jnaerator-0.11-shaded.jar
#	mv 'remotecontent?filepath=com%2Fnativelibs4java%2Fjnaerator%2F0.11%2Fjnaerator-0.11-shaded.jar' $@





# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
