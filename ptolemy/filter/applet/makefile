# Makefile for ptFilter.Filter package 
#
# @Authors: William Wu
#
# @Version: @(#)makefile        1.52 03/02/98
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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
#                                               PT_COPYRIGHT_VERSION_2
#                                               COPYRIGHTENDKEY
# $Id$ %S%
# Root of the Java directory

ROOT =          /users/ptdesign/tycho

# Get configuration info
CONFIG =        $(ROOT)/mk/tycho.mk
include $(CONFIG)
 
JPACKAGE =      ptolemy.filter.applet 
JVERSION =      1.0
JDIST =         $(JPACKAGE)$(JVERSION)
JZIP =          filter/$(JPACKAGE).zip
 
DIRS =  demo #test
 
CLASSPATH =     ../../..
# Order matters here.
 
# Sources that compile under JDK1.0.2 and later
JSRCS = \
       FiltApplet.java 

JCLASS = $(JSRCS:%.java=%.class)
 
all: sources jclass $(JZIP)
	chmod a+x pxgraph testpxgraph
install: sources jclass javadocs $(JZIP) 
	chmod a+x pxgraph testpxgraph
docs: javadocs
 
depend:
	@echo "no dependencies in this directory"

include $(ROOT)/mk/tycommon.mk
 

