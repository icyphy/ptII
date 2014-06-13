# fmusim makefile for Mac OS X
# TODO: Create a proper makefile

EXEC = main # name of the executable
CC = gcc # C compiler
INCDIR = includes # include directory

# Build master algorithm
all: $(EXEC)

clean:
	rm -f $(EXEC)
	rm -rf *.dSYM
	rm -f *.o

# Helper sources
SHARED_SRCS = sim_support.c

CPP_SRCS = \
	parser/XmlElement.cpp \
	parser/XmlParser.cpp \
	parser/XmlParserCApi.cpp

# Main target
TARGET = main.c

# Dependencies
DEPS = \
	$(INCDIR)/fmi.h \
	$(INCDIR)/fmiFunctions.h \
	$(INCDIR)/fmiFunctionTypes.h \
	$(INCDIR)/fmiTypesPlatform.h \
	parser/XmlElement.h \
	parser/XmlParser.h \
	parser/XmlParserCApi.h \
	parser/XmlParserException.h

# Set CFLAGS to -m32 to build for linux32
CFLAGS= -g -Wall -DFMI_COSIMULATION -DSTANDALONE_XML_PARSER -DLIBXML_STATIC $(INCFLAGS)
INCFLAGS= -I$(INCDIR) -Iparser/libxml -Iparser
LIBFLAGS= -lexpat -ldl -lxml2

CXX=c++

# Build target
%.o: %.c $(DEPS)
	$(CC) -o $@ -c $< $(CFLAGS) $(SHARED_SRCS)

main: main.o sim_support.o	
	$(CXX) -o $@ $^ $(CPP_SRCS) $(CFLAGS) $(LIBFLAGS)
	
#	cp $(EXEC) ../bin/

#../bin/:
#	if [ ! -d ../bin ]; then \
#		echo "Creating ../bin/"; \
#		mkdir ../bin/; \
#	fi
