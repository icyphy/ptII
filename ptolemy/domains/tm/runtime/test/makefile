# Below is the copyright agreement for the Ptolemy II system.
#
# Copyright (c) 2002-2011 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the above
# copyright notice and the following two paragraphs appear in all copies
# of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# Ptolemy II includes the work of others, to see those copyrights, follow
# the copyright link on the splash page or see copyright.htm.
#ALL= display gps ins plant server_controller server_plant giottoc/examples/fcode/circular_buffers_giotto.o giottoc/examples/fcode/socket.o

CINCLUDE = ..

CFLAGS = -I $(CINCLUDE) -Wall -c

LIB = -lpthread

OBJECTS = ports.o \
	actor1.o \
	actor2.o \
	isr.o \
	app.o \
	../tmrt.o

ALL = $(OBJECTS) test

all: $(ALL)

clean:
	rm -f *.exe *~ $(ALL)

ports.o: ports.c ports.h
	gcc $(CFLAGS) $<

actor1.o: actor1.c actor1.h
	gcc $(CFLAGS) $<

actor2.o: actor2.c actor2.h
	gcc $(CFLAGS) $<

isr.o: isr.c isr.h
	gcc $(CFLAGS) $<
app.o: app.c app.h
	gcc $(CFLAGS) $<
test: $(OBJECTS)
	gcc $(OBJECTS) $(LIB) -o test
