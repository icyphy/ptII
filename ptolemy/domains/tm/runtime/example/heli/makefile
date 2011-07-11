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
ALL= display gps ins plant server_controller server_plant
#giottoc/examples/fcode/circular_buffers_giotto.o giottoc/examples/fcode/socket.o

ALL += ../../tmrt.o gps_isr.o ins_isr.o app.o ports.o filter.o statefb.o actuator.o controller

INCLUDES = ../../

CFLAGS=-Wall -c

all: $(ALL)

clean:
	rm -f *.o *~ $(ALL)

actuator.o: actuator.c actuator.h ports.c ports.h
	gcc $(CFLAGS) $< -o $@

app.o: app.c app.h
	gcc $(CFLAGS) $< -o $@

circular_buffers_plant.o: circular_buffers.c circular_buffers.h
	gcc -DPLANT $(CFLAGS) $< -o $@

circular_buffers_controller.o: circular_buffers.c circular_buffers.h
	gcc -DCONTROLLER $(CFLAGS) $< -o $@

#giottoc/examples/fcode/circular_buffers_giotto.o: circular_buffers.c circular_buffers.h
#	gcc -DCONTROLLER $(CFLAGS) $< -o $@

derivatives.o: derivatives.c derivatives.h
	gcc -DPLANT $(CFLAGS) $< -o $@

display.o: display.c circular_buffers.h display.h ins.h plant.h return.h socket.h
	gcc -DPLANT $(CFLAGS) $< -o $@

display: display.o circular_buffers_plant.o socket.o
	gcc display.o circular_buffers_plant.o socket.o -o $@

filter.o: filter.c filter.h ports.h
	gcc $(CFLAGS) $< -o $@

gps.o: gps.c gps.h circular_buffers.h controller.h plant.h return.h socket.h
	gcc -DPLANT $(CFLAGS) $< -o $@

gps: gps.o circular_buffers_plant.o socket.o
	gcc gps.o circular_buffers_plant.o socket.o -o $@

gps_isr.o: gps_isr.c gps_isr.h ports.h ports.c gps.c gps.h return.h socket.h
	gcc $(CFLAGS) $< -o $@


ins.o: ins.c circular_buffers.h controller.h plant.h return.h socket.h
	gcc -DPLANT $(CFLAGS) $< -o $@

ins: ins.o circular_buffers_plant.o socket.o
	gcc ins.o circular_buffers_plant.o socket.o -o $@

ins_isr.o: ins_isr.c ins_isr.h ports.h ports.c ins.c ins.h return.h socket.h
	gcc $(CFLAGS) $< -o $@

nrutil.o: nrutil.c nrutil.h
	gcc -DPLANT $(CFLAGS) $< -o $@

plant.o: plant.c circular_buffers.h derivatives.h plant.h return.h rk4.h
	gcc -DPLANT $(CFLAGS) $< -o $@

plant: plant.o circular_buffers_plant.o derivatives.o rk4.o nrutil.o
	gcc plant.o circular_buffers_plant.o derivatives.o rk4.o nrutil.o -lm -o $@

ports.o: ports.c ports.h
	gcc -I $(INCLUDES) $(CFLAGS) $< -o $@

rk4.o: rk4.c rk4.h nrutil.h
	gcc -DPLANT $(CFLAGS) $< -o $@

server_controller.o: server_controller.c circular_buffers.h controller.h ins.h gps.h return.h socket.h
	gcc -DCONTROLLER $(CFLAGS) $< -o $@

server_controller: server_controller.o circular_buffers_controller.o socket.o
	gcc server_controller.o circular_buffers_controller.o socket.o -o $@

server_plant.o: server_plant.c actuators.h circular_buffers.h plant.h return.h socket.h
	gcc -DPLANT $(CFLAGS) $< -o $@

server_plant: server_plant.o circular_buffers_plant.o socket.o
	gcc server_plant.o circular_buffers_plant.o socket.o -o $@

socket.o: socket.c socket.h
	gcc $(CFLAGS) $< -o $@

statefb.o: statefb.c statefb.h ports.c ports.h
	gcc $(CFLAGS) $< -o $@

../../tmrt.o: ../../tmrt.c ../../tmrt.h
	gcc $(CFLAGS) $< -o $@

controller: app.o gps_isr.o ins_isr.o filter.o statefb.o actuator.o ports.o socket.o 
	gcc app.o gps_isr.o ins_isr.o filter.o statefb.o actuator.o ports.o socket.o ../../tmrt.o  -lpthread -lm -o $@

#giottoc/examples/fcode/socket.o: socket.c socket.h
#	gcc $(CFLAGS) $< -o $@
