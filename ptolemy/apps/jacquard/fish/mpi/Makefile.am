AM_CPPFLAGS = -I$(top_srcdir)/fish-lib -I.
LDADD = -L../fish-lib/ -lfish

if COND_HAVE_MPI
bin_PROGRAMS = fish
fish_SOURCES = fish.c ../fish-lib/fish.h mpi-fish.h
noinst_PROGRAMS = fish

if VAMPIR_ACTIVE
bin_PROGRAMS += fish_vt
fish_vt_SOURCES = fish.c ../fish-lib/fish.h mpi-fish.h
fish_vt_CPPFLAGS = -DTRACE_WITH_VAMPIR
noinst_PROGRAMS += fish_vt
endif

endif


EXTRA_DIST=lltemplate

fish:	fish.c ../fish-lib/fish.h
	$(MPICC) $(DEFS) $(DEFAULT_INCLUDES) $(INCLUDES) \
		$(AM_CPPFLAGS) $(CPPFLAGS) $(AM_CFLAGS) $(CFLAGS) \
		-o $@ $(srcdir)/fish.c \
		$(AM_LDFLAGS) $(LDFLAGS) $(LDADD) $(LIBS)

fish_vt:	fish.c ../fish-lib/fish.h
	$(MPICC) $(DEFS) $(DEFAULT_INCLUDES) $(INCLUDES) $(VTINC) \
		$(AM_CPPFLAGS) $(CPPFLAGS) $(AM_CFLAGS) $(CFLAGS) \
		-o $@ $(srcdir)/fish.c \
		$(VTLIB) $(AM_LDFLAGS) $(LDFLAGS) $(LDADD) $(LIBS)

