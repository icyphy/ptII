# Example Makefile
#
# Be sure to define CROSS_COMPILE first. See setup.sh script

CC             = $(CROSS_COMPILE)gcc
C++            = $(CROSS_COMPILE)g++
AR             = $(CROSS_COMPILE)ar
AS             = $(CROSS_COMPILE)as
LD             = $(CROSS_COMPILE)ld
NM             = $(CROSS_COMPILE)nm
STRIP          = $(CROSS_COMPILE)strip
CPP            = $(CC) -E


all: fpgaLoad timeTrigger

fpgaLoad: fpgaLoad.cpp
	$(C++) -o $(@) $<

timeTrigger: timeTrigger.cpp
	$(C++) -o $(@) $<
