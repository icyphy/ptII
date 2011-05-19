ME =		ptolemy/plot/img

DIRS = 		

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	img
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = 

OTHER_FILES_TO_BE_JARED = \
	fill.gif \
	format.gif \
	print.gif \
	reset.gif

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

all: 
install: jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
