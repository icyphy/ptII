# Makefile org.terraswarm Global Data Plane sources
#
# @Version: $Id$
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

DIRS = apps js

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
	Gdp10Library.java \
	GdpUtilities.java \
	ep_malloc_functions.java \
	ep_stat_to_string.java

OTHER_FILES_TO_BE_JARED = \
	gdp.patch

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass 
	@echo "See the apps directory for demos."
install: jclass jars

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = #src

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS) \
	src \
	'EP_DBG$$ByReference.class' \
	'EP_DBG$$ByValue.class' \
	'EP_STAT$$ByReference.class' \
	'EP_STAT$$ByValue.class' \
	'EP_TIME_SPEC$$ByReference.class' \
	'EP_TIME_SPEC$$ByValue.class' \
	'Gdp10Library$$EP_STAT2.class' \
	'Gdp10Library$$EP_STAT_HANDLE.class' \
	'Gdp10Library$$EP_STAT_HANDLER_FUNCP.class' \
	'Gdp10Library$$EP_STAT_callback.class' \
	'Gdp10Library$$EP_THR_COND.class' \
	'Gdp10Library$$EP_THR_MUTEX.class' \
	'Gdp10Library$$EP_THR_RWLOCK.class' \
	'Gdp10Library$$FILE.class' \
	'Gdp10Library$$gdp_buf_t.class' \
	'Gdp10Library$$gdp_event_t.class' \
	'Gdp10Library$$gdp_gcl_sub_cbfunc_t.class' \
	'Gdp10Library$$gdp_gcl_t.class' \
	'Gdp10Library$$gdp_iomode_t.class' \
	'ep_stat_to_string$$ByReference.class' \
	'ep_stat_to_string$$ByValue.class'

JCLASS = $(JSRCS:%.java=%.class)

KRUFT = .cpp *.c *.choices _jnaerator.macros.cpp _jnaerator.rawParsed.cpp _jnaerator.normalizedParsed.cpp

# See https://code.google.com/p/jnaerator/
# Be sure to get the -shaded.jar and avoid a message about
#    Exception in thread "main" java.lang.NoClassDefFoundError: com/ochafik/lang/jnaerator/parser/Visitor
JNAERATOR_JAR = jnaerator-0.12-shaded.jar

all: jclass
install: jclass jars

GDP_SRC = src/gdp

GDP_H_FILES = \
	$(GDP_SRC)/gdp/gdp.h \
	$(GDP_SRC)/gdp/gdp_buf.h \
	$(GDP_SRC)/ep/ep.h \
	$(GDP_SRC)/ep/ep_dbg.h \
	$(GDP_SRC)/ep/ep_thr.h

# Present under Linux, but not MacOS.
#PTHREADTYPES_H = /usr/include/bits/pthreadtypes.h
#PTHREADTYPES_H = /usr/include/sys/_types.h

# Unfortunately, we need pthread.h because gdp_datum uses it if
# -DEP_OSCF_USE_PTHREADS=0 is not defined

#PTHREAD_H_FILES = \
#	/usr/include/pthread.h \
#	$(PTHREADTYPES_H)

#STD=	-DEP_OSCF_USE_PTHREADS=0

LIBEVENT_VERSION = 2.0.21-stable
LIBEVENT=libevent-$(LIBEVENT_VERSION)
LIBEVENT_SRC=src/$(LIBEVENT)

LIBEVENT_H_FILES = \
	$(LIBEVENT_SRC)/include/event2/buffer.h


# Rebuild the jar file
# We define gdp_datum_t and gdp_datum as void * so that JNAerator does not generate gdp_datum.java, which would refer to pthreads.
jna: $(JNAERATOR_JAR) src/gdp $(LIBEVENT_SRC)
	java -jar $(JNAERATOR_JAR) -runtime JNA -Dgdp_datum_t="void *" -Dgdp_datum="void *" -library gdp.0.2 -I $(GDP_SRC) $(GDP_H_FILES) $(PTHREAD_H_FILES) -o $(PTII) -library event2 -I $(LIBEVENT2_SRC)/include $(LIBEVENT_H_FILES) -package org.terraswarm.gdp  -v  -noJar -noComp
	# Fix up some issues:
	for file in *.java; do \
	echo "Fixing $$file"; \
	sed \
	    -e 's@NativeSize\([^T]\)@NativeSizeT\1@g' \
	    -e 's@import com.ochafik.lang.jnaerator.runtime.NativeSizeT;@import org.ptolemy.fmi.NativeSizeT; //Use NativeSizeT intead of NativeSize.  Fixed in makefile@' \
	    -e 's@\(import com.ochafik.lang.jnaerator.runtime.\)@// \1 // Commented out by cxh in makefile@' \
	    -e 's@public static final Pointer EP_GEN_DEADBEEF = (Pointer)0xDEADBEEF;@public static final Pointer EP_GEN_DEADBEEF = new Pointer(0xDEADBEEF); // Fixed by cxh in makefile.@' \
	    -e 's@\(public static final GlobalInt\)@// \1 // Fixed by cxh in makefile.@' \
		$$file > /tmp/gdp.fix; \
	mv /tmp/gdp.fix $$file; \
        done

# This target contains old sed rules that were used at various points
# oldSedRules:
# 	    -e 's@\(.*CharByReference.*\)@// \1@' \
# 	    -e 's@\(public static final GlobalPointerType\)@// \1@' \
# 	    -e 's@\(public static final GlobalStruct\)@// \1@' \
# 	    -e 's@\(public static final Pointer PTHREAD_CANCELED = (Pointer)-1;\)@// \1@' \
# 	    -e 's@\(.*NativeSizeTByReference.*\)@// \1 // Commented out by makefile because NativeSizeTByReference is not in jna.@' \
# 	    -e 's@\(.*time_tPtr1.*\)@// \1@' \
# 	    -e 's@\(.*time_t1.*\)@// \1@' \
# 	    -e 's@\(.*tmPtr1.*\)@// \1@' \
# 	    -e 's#\(.*@Deprecated.*\)#// \1#' \
# 	    -e 's@\(.*(Pointer)1.*\)@// \1@' \
# 	    -e 's@public static final int EP_STAT_MAX_DETAIL = (int)((1 << 42) - 1);@public static final long /* Fixed by makefile, was int, Coverity warned about bad shift. */ EP_STAT_MAX_DETAIL = (long)((1 << 42) - 1);@' \
# 	    -e 's@public static final int _EP_STAT_SEVSHIFT = (int)((10 + 8) + 11);@public static final int _EP_STAT_SEVSHIFT = (int)((42 + 8) + 11); // Fixed by makefile, see http://www.terraswarm.org/swarmos/wiki/Main/GDPJavaInterface#ReaderTestDoesNotExit@' \



# Not needed, but we leave it here for now.
# fix_gdp_datum:
# 	@echo "gdp_datum.java will need some edits because the JNAerator does not understand bitfields."
# 	sed -e 's@abstract@/* abstract Fixed in makefile.*/@' \
#             -e 's@return Arrays.asList("mutex", "next", "recno", "ts", "dlen", "dbuf");@return Arrays.asList("mutex", "next", "inuse" /*Fixed in makefile.*/, "recno", "ts", "dlen", "dbuf");@' \
# 	    -e 's@\(/\*\* Conversion Error : inuse:1 (This runtime does not support bit fields : JNA (please use BridJ instead)) \*/\)@ \1 public byte inuse;@' \
# 	    -e 's@mutex, gdp_datum.ByReference next, long recno@mutex, gdp_datum.ByReference next, byte inuse /*Fixed in makefile*/,long recno@' \
# 	    -e 's@this.next = next;@this.next = next; this.inuse = inuse; /*Fixed in makefile. */@' \
# 		gdp_datum.java > /tmp/gdp.fix
# 	cp /tmp/gdp.fix gdp_datum.java

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
	@if [ ! -d $(LIBEVENT_SRC) ]; then \
		echo "Github is too stupid to handle wget, so you will need to go to http://libevent.org, download it and then run"; \
		echo " cd src; tar -zxf ~/Downloads/$(LIBEVENT).tar.gz; cd .."; \
		echo "Then rerun make."; \
		exit 2; \
	else \
		echo "$(LIBEVENT_SRC) exists!"; \
	fi

update:
	cd src/gdp; git pull

# Run "make gdp" to get the gdp source.
gdp: $(GDP_SRC)

$(GDP_SRC):
	# You will need a password for this, ask Eric.
	@if [ ! -d src ]; then mkdir src; echo "making the src/ directory."; fi
	cd src; git clone https://$(USER)@repo.eecs.berkeley.edu/git/projects/swarmlab/gdp.git; cd gdp; $(MAKE)
	@echo "If the above fails, read src/gdp/README and install dependencies."

#patch_gdp:
#	@echo "Patching the gdp so that /usr/local/include is searched for event2 and so that .dylib is created."
#	cd src/gdp; patch -p1 < ../../gdp.patch
#	cd src/gdp; $(MAKE)

$(JNAERATOR_JAR):
	wget 'http://search.maven.org/remotecontent?filepath=com/nativelibs4java/jnaerator/0.12/$(JNAERATOR_JAR)' -O $@

# jnaerator-0.11-shaded.jar does not generate getFieldOrder(), which is needed in more recent versions of JNA.
#
#jnaerator-0.11-shaded.jar:
#	wget http://search.maven.org/remotecontent?filepath=com/nativelibs4java/jnaerator/0.11/jnaerator-0.11-shaded.jar
#	mv 'remotecontent?filepath=com%2Fnativelibs4java%2Fjnaerator%2F0.11%2Fjnaerator-0.11-shaded.jar' $@

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
