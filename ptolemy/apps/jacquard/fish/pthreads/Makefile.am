CC = @PTHREAD_CC@
AM_CPPFLAGS = -I$(top_srcdir)/fish-lib @PTHREAD_CFLAGS@
LDADD = -L../fish-lib -lfish @PTHREAD_CFLAGS@ @PTHREAD_LIBS@

if COND_HAVE_PTHREADS
bin_PROGRAMS = fish
fish_SOURCES = fish.c pthread_barrier.c pthread_barrier.h ../fish-lib/fish.h
noinst_PROGRAMS = fish
endif
