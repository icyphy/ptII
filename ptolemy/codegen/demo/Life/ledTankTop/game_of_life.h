/*********************************************
* Author: Leah Buechley
* Filename: game_of_life.h
* Chip: ATmega16
* Date: 3/30/2006
* Purpose:
*	This program was written for a wearable LED tank top.
*	More information in game_of_life.c and at: 
*	http://www.cs.colorado.edu/~buechley/diy/diy_tank.html
* Copyright information: http://www.gnu.org/copyleft/gpl.html

Copyright (C) 2006 Leah Buechley

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

*********************************************/
#ifdef __AVR__

//MACROS FOR AVR ATmega16
#define row0_output DDRB|= _BV(PB1)
#define row1_output DDRB|= _BV(PB2)
#define row2_output DDRB|= _BV(PB3)
#define row3_output DDRB|= _BV(PB4)
#define row4_output DDRB|= _BV(PB5)
#define row5_output DDRB|= _BV(PB6)

#define row0_high PORTB|= _BV(PB1)
#define row1_high PORTB|= _BV(PB2)
#define row2_high PORTB|= _BV(PB3)
#define row3_high PORTB|= _BV(PB4)
#define row4_high PORTB|= _BV(PB5)
#define row5_high PORTB|= _BV(PB6)

#define row0_low PORTB &= ~_BV(PB1)
#define row1_low PORTB &= ~_BV(PB2)
#define row2_low PORTB &= ~_BV(PB3)
#define row3_low PORTB &= ~_BV(PB4)
#define row4_low PORTB &= ~_BV(PB5)
#define row5_low PORTB &= ~_BV(PB6)

#define col0_output DDRB|= _BV(PB0)
#define col1_output DDRA|= _BV(PA0)
#define col2_output DDRA|= _BV(PA1)
#define col3_output DDRA|= _BV(PA2)
#define col4_output DDRA|= _BV(PA3)
#define col5_output DDRA|= _BV(PA4)
#define col6_output DDRA|= _BV(PA5)
#define col7_output DDRA|= _BV(PA6)
#define col8_output DDRA|= _BV(PA7)
#define col9_output DDRC|= _BV(PC7)
#define col10_output DDRC|= _BV(PC6)
#define col11_output DDRC|= _BV(PC5)
#define col12_output DDRC|= _BV(PC4)
#define col13_output DDRC|= _BV(PC3)

#define col0_high PORTB|= _BV(PB0)
#define col1_high PORTA|= _BV(PA0)
#define col2_high PORTA|= _BV(PA1)
#define col3_high PORTA|= _BV(PA2)
#define col4_high PORTA|= _BV(PA3)
#define col5_high PORTA|= _BV(PA4)
#define col6_high PORTA|= _BV(PA5)
#define col7_high PORTA|= _BV(PA6)
#define col8_high PORTA|= _BV(PA7)
#define col9_high PORTC|= _BV(PC7)
#define col10_high PORTC|= _BV(PC6)
#define col11_high PORTC|= _BV(PC5)
#define col12_high PORTC|= _BV(PC4)
#define col13_high PORTC|= _BV(PC3)

#define col0_low PORTB &= ~_BV(PB0)
#define col1_low PORTA &= ~_BV(PA0)
#define col2_low PORTA &= ~_BV(PA1)
#define col3_low PORTA &= ~_BV(PA2)
#define col4_low PORTA &= ~_BV(PA3)
#define col5_low PORTA &= ~_BV(PA4)
#define col6_low PORTA &= ~_BV(PA5)
#define col7_low PORTA &= ~_BV(PA6)
#define col8_low PORTA &= ~_BV(PA7)
#define col9_low PORTC &= ~_BV(PC7)
#define col10_low PORTC &= ~_BV(PC6)
#define col11_low PORTC &= ~_BV(PC5)
#define col12_low PORTC &= ~_BV(PC4)
#define col13_low PORTC &= ~_BV(PC3)
#else /* __AVR__ */ 

#define row0_output
#define row1_output
#define row2_output
#define row3_output
#define row4_output
#define row5_output

#define row0_high
#define row1_high
#define row2_high
#define row3_high
#define row4_high
#define row5_high

#define row0_low
#define row1_low
#define row2_low
#define row3_low
#define row4_low
#define row5_low

#define col0_output
#define col1_output
#define col2_output
#define col3_output
#define col4_output
#define col5_output
#define col6_output
#define col7_output
#define col8_output
#define col9_output
#define col10_output
#define col11_output
#define col12_output
#define col13_output

#define col0_high
#define col1_high
#define col2_high
#define col3_high
#define col4_high
#define col5_high
#define col6_high
#define col7_high
#define col8_high
#define col9_high
#define col10_high
#define col11_high
#define col12_high
#define col13_high

#define col0_low
#define col1_low
#define col2_low
#define col3_low
#define col4_low
#define col5_low
#define col6_low
#define col7_low
#define col8_low
#define col9_low
#define col10_low
#define col11_low
#define col12_low
#define col13_low

#endif /* __AVR__ */ 

//initialization functions
void initialize_tank_pins_as_output (void);

//basic LED array display functions
void row_column_display(unsigned char i, unsigned char j);
void column_display (unsigned char j);
void all_on (void);
void all_off (void);
void loop_lights (void);

//Game of Life (GOL) cellular automaton functions
void zero_array (void);
void display_array (void);
void reset_array(void);
void gol (void);
void glider (unsigned char i, unsigned char j);
void blinker (unsigned char i, unsigned char j);
void r_pentomino (unsigned char i, unsigned char j);
