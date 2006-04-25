# Makefile.am for testing the test CPP programs
#

bin_PROGRAMS = PDEtest

PDEtest_SOURCES = Scheduler.cpp Token.cpp  simpleModel.cpp

#PDEtest_CPPFLAGS = -I/usr/local/escher/EOCP/src/vendors/STLPort/stlport

PDEtest_LDADD = -lpthread

PDEtestdistdir = .
PDEtestdir = .
#PDEtest_HEADERS = *.h 
PDEtest_HEADERS = Clock.h Merge.h actor.h event.h port.h token.h Display.h Time.h dependencylink.h eventqueue.h scheduler.h typedport.h ptpHwP1000LinuxDr.h

ACLOCAL = @ACLOCAL@
ACLOCAL_AMFLAGS = -I m4

