demodir = @bindir@/demo
demo_PROGRAMS = sensor_actuator

common_srcdir = @top_srcdir@/c/common
common_builddir = @top_builddir@/c/common

AM_CFLAGS = -I$(common_srcdir) -Werror -Wall -std=c99 -O2
LDADD = $(common_builddir)/libp1000_utils.a
AM_LDFLAGS = -lpthread

sensor_actuator_SOURCES = \
	$(common_srcdir)/ptpHwP1000LinuxDr.h \
	$(common_srcdir)/p1000_utils.h \
	PTIDES.h \
	PTIDES.c \
	EnDecode.c 
