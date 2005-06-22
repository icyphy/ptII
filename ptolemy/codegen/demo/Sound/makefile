# Makefile for Ptolemy II SDL codegen demo
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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

# This demo uses SDL the Simple DirectMedia Layer that we can use for
# audio code generation http://www.libsdl.org
# See http://embedded.eecs.berkeley.edu/elab/sdl.htm

ME =		ptolemy/codegen/demo/Sound

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	Sound
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS =


OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED) \


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all:  audioPlayer.exe

#jclass $(PTCLASSJAR)
#	@echo "To run all the demos, run 'make demo'"
#	@echo "To run the applet only, run 'make demo_applet'"
#	@echo "To run the application only, run 'make demo_application'"

install: all

# Run the demo in all its different formats
# Run demo_application first so that we generate Sound.xml
demo: demo_applet demo_application demo_ptolemy demo_vergil

demo_applet: $(PTCLASSJAR)
	CLASSPATH="$(CLASSPATH)" appletviewer Sound.htm

demo_application: $(PTCLASSJAR)
	CLASSPATH="$(CLASSPATH)" \
	 "$(JAVA)" ptolemy.actor.gui.CompositeActorApplication \
		-class ptolemy.domains.sdf.demo.Sound.Sound

demo_ptolemy:
	"$(PTII)/bin/ptolemy" Sound.xml

demo_vergil:
	"$(PTII)/bin/vergil" Sound.xml


# or, try
# gcc loopwave.c -o loopwave.exe `/usr/local/bin/sdl-config --cflags --libs`

SDL_DIRECTORY=/usr/local
SDL_INCLUDES=-I$(SDL_DIRECTORY)/include/SDL

SDL_LIBS=-L$(SDL_DIRECTORY)/lib -lmingw32 -lSDLmain -lSDL -lSDLmain \
		-mwindows -mno-cygwin

# Our sample code
audioPlayer.exe: audioPlayer.c
	$(PTCC) $(SDL_INCLUDES) $^ \
		$(SDL_LIBS) \
		-o $@

# sample code from libsdl
loopwave.exe: loopwave.c
	$(PTCC) $(SDL_INCLUDES) $^ \
		$(SDL_LIBS) \
		-o $@
	@echo "To demo this code, run:     make loopwave_demo"

loopwave_demo:
	./loopwave.exe $(PTII)/ptolemy/actor/lib/javasound/voice.wav 

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
