# Below is the copyright agreement for the Ptolemy II system.
#
# Copyright (c) 2006-2011 The Regents of the University of California.
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
# Makefile for tank_top

# default target when "make" is run w/o arguments
all: tank_top.rom

# compile game_of_life.c into game_of_life.o
game_of_life.o: game_of_life.c game_of_life.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega16 -I. game_of_life.c -o game_of_life.o

# compile tank_top.c into tank_top.o
tank_top.o: tank_top.c tank_top.h
	avr-gcc -c -g -O3 -Wall -mmcu=atmega16 -I. tank_top.c -o tank_top.o

# link up tank_top.o and game_of_life.o into tank_top.elf
tank_top.elf: tank_top.o game_of_life.o
	avr-gcc tank_top.o game_of_life.o -Wl,-Map=tank_top.map,--cref -mmcu=atmega16 -o tank_top.elf

# copy ROM (FLASH) object out of tank_top.elf into tank_top.rom
tank_top.rom: tank_top.elf
	avr-objcopy -O ihex tank_top.elf tank_top.rom

# command to clean up junk (no source files) (invoked by running "make clean")
clean:
	rm -f *.o *.rom *.elf *.map *~

# command to program chip (invoked by running "make install")
install:
	uisp -v=1 -dprog=stk500 -dserial=/dev/cu.USA19H1b1P1.1 -dpart=ATmega16 --erase --upload --verify if="tank_top.rom"

# command to set oscillator fuse bits on chip to 8Mhz oscillator (invoked by running "make osc")
# default oscillator setting is 1Mhz, so this will make your chip run a lot faster
# NOTE! - if you change the oscillator setting of your chip to 8Mhz, you must change the F_CPU variable
# in game_of_life.h to 8000000UL!!
osc:
	uisp -v=2 -dprog=stk500 -dserial=/dev/cu.USA19H1b1P1.1 -dpart=ATmega16 --wr_fuse_l=0xe4

# command to disable JTAGEN fuse bit (invoked by "make unTAG")
# NOTE! - when JTAGEN fuse bit is enabled, pins c2-c5 do not work as I/0
unTAG:
	uisp -v=2 -dprog=stk500 -dserial=/dev/cu.USA19H1b1P1.1 -dpart=atmega16 --wr_fuse_h=0xd9

test:
	gcc game_of_life.c tank_top.c
