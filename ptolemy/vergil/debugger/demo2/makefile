ME =		ptolemy/vergil/debugger/demo2/

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
#PTPACKAGE = 	demo2
#PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = ../Breakpoint.java \
	../BrkptList.java \
	../ExecState.java \
	MyRamp.java \
	../DbgController.java \
	../DbgDirector.java \
	../MMI/DebuggerUI.java \
	../Pdb.java \
	../domains/sdf/SDFDbgDirector.java \
	MyCompositeActor.java \
	ClusteredGraph.java 

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
	@echo "To run the application only, run 'make demo_application'"
install: all

demo_application: $(PTCLASSJAR)
	CLASSPATH=$(CLASSPATH) $(JAVA) ptolemy.vergil.debugger.demo2.ClusteredGraph

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk