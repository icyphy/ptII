.phone: all

SHELL=bash
CLASSFILES=$(addsuffix .class, $(basename $(wildcard *.java)))

SUBDIR = util

all: $(CLASSFILES)

%.class: %.java
	javac -classpath ../../.. -source 1.4 $<

clean:
	$(RM) *.class
