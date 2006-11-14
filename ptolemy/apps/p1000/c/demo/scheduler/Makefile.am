demodir = @bindir@/demo
demo_PROGRAMS = scheduler

common_srcdir = @top_srcdir@/c/common
common_builddir = @top_builddir@/c/common
AM_CFLAGS = -I$(common_srcdir)
LDADD = $(common_builddir)/libp1000_utils.a
AM_LDFLAGS = -lpthread

scheduler_SOURCES = \
	scheduler.c \
	scheduler.h \
	type_defs.h \
	type_impls.c \
	type_uids.h
