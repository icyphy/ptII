#ALL= display gps ins plant server_controller server_plant giottoc/examples/fcode/circular_buffers_giotto.o giottoc/examples/fcode/socket.o
CFLAGS = -Wall -c

LIB = -lpthread

OBJECTS = tmrt.o 

ALL = $(OBJECTS)

all: $(ALL)

clean:
	rm -f *.exe *~ $(ALL)

tmrt.o: tmrt.c tmrt.h schedule.h
	gcc $(CFLAGS) tmrt.c

