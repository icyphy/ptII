demodir = @bindir@/demo
demo_PROGRAMS = scheduler

common_srcdir = @top_srcdir@/c/common
common_builddir = @top_builddir@/c/common

AM_CFLAGS = -I$(common_srcdir) -Werror -Wall -std=c99 -O2
LDADD = $(common_builddir)/libp1000_utils.a
AM_LDFLAGS = -lpthread

scheduler_SOURCES = \
	$(common_srcdir)/p1000_utils.h \
	$(common_srcdir)/ptpHwP1000LinuxDr.h \
	actor.c \
	bidir_list.c \
	event.c \
	int_token.c \
	main.c \
	port.c \
	scheduler.c \
	token.c \
	typed_port.c \
	types.c \
	types_init.c \
	actor.h \
	bidir_list.h \
	event.h \
	int_token.h \
	main.h \
	port.h \
	scheduler.h \
	token.h \
	typed_port.h \
	types.h \
	types_init.h
