AUTOMAKE_OPTIONS = foreign
ACLOCAL_AMFLAGS = -I m4

#Doesn't work recursively?
#AM_CPPFLAGS = -I$(top_srcdir)/fish-lib -I.

SUBDIRS = fish-lib sequential pthreads mpi openmp

bin_PROGRAMS = aquarium
aquarium_SOURCES = aquarium.in
noinst_PROGRAMS = aquarium

aquarium:	aquarium.in
	sed -e 's,[@]PYTHON[@],@PYTHON@,g' < $(srcdir)/aquarium.in > aquarium
	chmod +x aquarium
