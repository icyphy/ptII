ME =		ptolemy/vergil/debugger

DIRS=

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	debugger
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = Breakpoint.java \
	BreakpointEditor.java \
	ActorWatcher.java \
	DebugController.java \
	DebuggerFrame.java \
	DebuggerTableau.java \
	DisplayList.java

EXTRA_SRCS = \
	$(JSRCS) $(DIRS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'BreakpointEditor$$1.class' \
	'BreakpointEditor$$2.class' \
	'BreakpointEditor$$3.class' \
	'BreakpointEditor$$4.class' \
	'BreakpointEditor$$5.class' \
	'DebuggerFrame$$1.class' \
	'DebuggerFrame$$2.class' \
	'DebuggerFrame$$3.class' \
	'DebuggerFrame$$4.class' \
	'DebuggerFrame$$5.class' \
	'DebuggerFrame$$6.class' \
	'DebuggerFrame$$7.class' \
	'DebuggerFrame$$8.class' \
	'DebuggerFrame$$9.class' \
	'DebuggerFrame$$10.class' \
	'DebuggerFrame$$11.class' \
	'DebuggerFrame$$12.class' \
	'DebuggerFrame$$13.class' \
	'DebuggerFrame$$14.class' \
	'DebuggerFrame$$15.class' \
	'DebuggerTableau$$Factory.class' \
	'DisplayList$$1.class' \
	'DisplayList$$2.class'

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
