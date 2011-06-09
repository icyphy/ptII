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
