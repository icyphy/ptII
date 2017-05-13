# Makefile for Nashorn-based Ptolemy II JavaScript modules
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2014-2016 The Regents of the University of California.
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

ME =		ptolemy/actor/lib/jjs/modules

# PTBROWSER_DIR is set to browser if the Vertx jars are found by configure.
# PTCAMERAS_DIR is set to cameras if the webcam-capture*.jar and other jars are found by configure.
# PTEVENTBUS_DIR is set to eventbus if the Vertx jars are found by configure.
# PTGDP_DIR is set to gdp if $PTII/lib/gdp*.jar was found by configure
# PTHTTPCLIENT_DIR is set to httpClient if the Vertx jars are found by configure.
# PTHTTPCLIENT_DIR is set to httpServer if the Vertx jars are found by configure.
# PTIMUSENSOR_DIR is set to IMUSensor if the Java Communications API (RXTX) jars are found by configure.
# PTPAHO_MQTT_DIR is set to localStorage if the $PTII/lib/org.eclipse.paho.client.mqttv3.jar is found by configure.
# PTSERIAL_DIR is set to serial if the Java Communication API is found by configure.
# PTUDPSOCKET_DIR is set to udpSocket if the Vertx jars are found by configure.
# PTVERTX_SOCKET_DIR is set to socket if the Vertx jars are found by configure.
# PTWEBSOCKET_DIR is set to webSocket if the Vertx jars are found by configure.
# PTXBEE_DIR is set to xbee if the XBee Java Interface was found
DIRS = 		audio $(PTBROWSER_DIR) buffer $(PTCAMERAS_DIR) contextAware contextAwareTest crypto dataConverter discovery dsp $(PTEVENTBUS_DIR) $(PTGDP_DIR) $(PTHTTPCLIENT_DIR) $(PTHTTPSERVER) imageDisplay $(PTIMUSENSOR_DIR) iotAuth localStorage mapManager motionDetector $(PTPAHO_MQTT_DIRS) objectAssign os process $(PTSERIAL_DIR) shell speechRecognition testing $(PTUDPSOCKET_DIR) $(PTVERTX_SOCKET_DIR) $(PTWEBSOCKET_DIR) $(PTXBEE_DIR)

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTOAUTHCLIENT_JAR)$(CLASSPATHSEPARATOR)$(PTOAUTHCOMMON_JAR)$(CLASSPATHSEPARATOR)$(PTSOCKETIO_JAR)$(CLASSPATHSEPARATOR)$(PTVERTX_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	modules
#PTCLASSJAR = 	$(PTPACKAGE).jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS =

# Don't include these possibly optional files.
#	localStorage/localStorage.jar
#	mqtt/mqtt.jar
#	webSocket/webSocket.jar

# Keep this list alphabetized.
JSRCS =

OTHER_FILES_TO_BE_JARED =

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	IMUSensor aprilTags audio browser buffer cameras contextAware contextAwareTest crypto dataConverter discovery dsp eventbus gdp httpClient httpServer imageFilters iotAuth localStorage mapManager motionDetector mqtt objectAssign serial shell socket testing udpSocket webSocket webSocketClient webSocketServer xbee #test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	demo.jar \
	$(PTCLASSJAR) 

JCLASS = $(JSRCS:%.java=%.class)


all: suball #jclass
install: subjars jars #jclass

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
