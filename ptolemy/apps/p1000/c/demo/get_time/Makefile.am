demodir = @bindir@/demo
demo_PROGRAMS = get_time

common_srcdir = @top_srcdir@/c/common
common_builddir = @top_builddir@/c/common

AM_CFLAGS = -I$(common_srcdir) -Wall -Werror -std=c99 -O2

LDADD = $(common_builddir)/libp1000_utils.a

get_time_SOURCES = \
	$(common_srcdir)/ptpHwP1000LinuxDr.h \
	$(common_srcdir)/p1000_utils.h \
	get_time.c
