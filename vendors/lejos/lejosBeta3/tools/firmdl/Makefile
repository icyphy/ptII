# Makefile for firmdl3

# The contents of this file are subject to the Mozilla Public License
# Version 1.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
# http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS IS"
# basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
# License for the specific language governing rights and limitations
# under the License.
#
# The Original Code is Firmdl code, released October 3, 1998.
#
# The Initial Developer of the Original Code is Kekoa Proudfoot.
# Portions created by Kekoa Proudfoot are Copyright (C) 1998, 1999
# Kekoa Proudfoot. All Rights Reserved.

#
# 2000.03.12 - Paolo Masetti <paolo.masetti@itlug.org>
#
#	- Conditional make based on $OSTYPE variable for Windows NT (cygnwin32)
#

#include ../Makefile.common

CC=gcc
CFLAGS =

ifndef OSTYPE
	OSTYPE=$(MSDOS)
endif

ifneq (,$(findstring $(OSTYPE),cygwin32 CYGWIN_NT-4.0 WindowsNT Windows_NT))
	EXT=.exe
endif

all: fastdl.h ../../bin/lejosfirmdl$(EXT)

mkimg$(EXT): mkimg.o srec.o
	$(CC) $^ -o $@ $(CFLAGS)

../../bin/lejosfirmdl$(EXT): firmdl.o srec.o rcx_comm.o util.o
	$(CC) $^ -o $@ $(CFLAGS)

fastdl.h: mkimg$(EXT) fastdl.srec
	./mkimg fastdl.srec > fastdl.h

clean:
	rm -f *.o *~ *.bak fastdl.h mkimg$(EXT) ../../bin/firmdl3$(EXT)
