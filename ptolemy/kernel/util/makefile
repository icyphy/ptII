# Makefile for the Java classes used to implement the Ptolemy kernel
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997-2012 The Regents of the University of California.
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

ME = 		ptolemy/kernel/util

DIRS = 		test

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Flags to pass to javadoc. (Override value in ptII.mk)
JDOCFLAGS = 	-author -version -public $(JDOCBREAKITERATOR) $(JDOCMEMORY) $(JDOCTAG)

# Used to build jar files
PTPACKAGE = 	util
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	AbstractSettableAttribute.java \
	Attribute.java \
	BasicModelErrorHandler.java \
	Changeable.java \
	ChangeListener.java \
	ChangeRequest.java \
	Configurable.java \
	ConfigurableAttribute.java \
	CrossRefList.java \
	DebugEvent.java \
	Debuggable.java \
	DebugListener.java \
	Decorator.java \
	DecoratedAttributes.java \
	Derivable.java \
	DropTargetHandler.java \
	ExceptionHandler.java \
	Flowable.java \
	HierarchyListener.java \
	IllegalActionException.java \
	Instantiable.java \
	InternalErrorException.java \
	InvalidStateException.java \
	KernelException.java \
	KernelRuntimeException.java \
	LazyComposite.java \
	Locatable.java \
	Location.java \
	ModelErrorHandler.java \
	MoMLExportable.java \
	Moveable.java \
	Nameable.java \
	NamedList.java \
	NamedObj.java \
	NameDuplicationException.java \
	NoSuchItemException.java \
	PtolemyThread.java \
	RecorderListener.java \
	RelativeLocation.java \
	Settable.java \
	Singleton.java \
	SingletonAttribute.java \
	SingletonConfigurableAttribute.java \
	StreamChangeListener.java \
	StreamListener.java \
	StringAttribute.java \
	TransientSingletonConfigurableAttribute.java \
	ValueListener.java \
	Workspace.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'CrossRefList$$1.class' \
	'CrossRefList$$CrossRef.class' \
	'CrossRefList$$CrossRefEnumeration.class' \
	'NamedList$$1.class' \
	'NamedList$$NamedListEnumeration.class' \
	'NamedObj$$1.class' \
	'NamedObj$$SerializableObject.class' \
	'NamedObj$$MoMLInfo.class' \
	'NamedObj$$ContainedObjectsIterator.class' \
	'Settable$$Visibility.class' \
	'Workspace$$1.class' \
	'Workspace$$AccessRecord.class' \
	'Workspace$$ReadDepth.class' \
	$(PTCLASSJAR)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
