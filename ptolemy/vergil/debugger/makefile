ME =		ptolemy/vergil/debugger

DIRS= 		

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
#PTPACKAGE = 	debugger
#PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = Breakpoint.java \
	ExecState.java \
	Watchers.java \
	ActorWatcher.java \
	DebuggingListener.java \
	DbgController.java \
	DbgDirector.java \
	DebuggerModule.java \
	domains/sdf/SDFDbgDirector.java \
	MMI/DisplayList.java \
	MMI/BrkptEditor.java \
	MMI/DebuggerUI.java \
	Pdb.java 

EXTRA_SRCS = \
	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass $(PTCLASSJAR)

install: all

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk