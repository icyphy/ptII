AM_CPPFLAGS = -I$(top_srcdir)/fish-lib
LDADD = -L../fish-lib/ -lfish

if COND_HAVE_OPENMP
bin_PROGRAMS = fish
fish_SOURCES = fish.c ../fish-lib/fish.h
fish_CFLAGS = $(CFLAGS) $(OPENMP_CFLAGS)
fish_LDFLAGS = $(LDFLAGS) $(OPENMP_CFLAGS)
noinst_PROGRAMS = fish
endif
