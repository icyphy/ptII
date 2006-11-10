demodir = @bindir@/demo
demo_PROGRAMS = get_time

commondir = @top_srcdir@/c/common
AM_CFLAGS = -I$(commondir)
LDADD = $(commondir)/libp1000_utils.a

get_time_SOURCES = \
	$(commondir)/ptpHwP1000LinuxDr.h \
	$(commondir)/p1000_utils.h \
	get_time.c
