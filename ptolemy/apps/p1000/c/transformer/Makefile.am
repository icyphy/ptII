SUBDIRS = . test

AM_YFLAGS = -d
AM_CFLAGS = -Werror -Wall -Wno-unused-function -O2

lib_LIBRARIES = libpct.a
libpct_a_SOURCES = \
	pct_c_parser.y \
	pct_c_lexer.l \
	pct_util.h \
	pct_util_misc.c \
	pct_util_string.c \
	pct_util_visitor.c
include_HEADERS = pct_c_parser.h pct_util.h