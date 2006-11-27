/***preinitBlock***/

/** LEDMatrix Portion has the following copyright */
/**
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

*/
#ifdef __AVR__

//MACROS FOR AVR ATmega16
#define row0_output DDRB|= _BV(PB1)
#define row1_output DDRB|= _BV(PB2)
#define row2_output DDRB|= _BV(PB3)
#define row3_output DDRB|= _BV(PB4)
#define row4_output DDRB|= _BV(PB5)
#define row5_output DDRB|= _BV(PB6)
#define row6_output DDRB|= _BV(PB7)
#define row8_output DDRB|= _BV(PB8)

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
#endif /* __AVR__ */ 

/* End of LEDMatrix defines */

/**/

/***fireBlock***/
#ifndef __AVR__
/* Machines that don't have the hardware just print 0 and 1. */
if ($ref(column) == 0) {
    printf("\n");
}
if ($ref(row) == 0 && $ref(column) == 0) {
    printf("\n");
}
if ($ref(control)) { 
    printf("1");
} else {
    printf("0");
}

#else /* __AVR__ */ 
/* LED Tank Top Code from
 *   http://craftzine.com/01/led
 *   http://www.cs.colorado.edu/~buechley/diy/diy_tank.html
 */ 
if ($ref(control)) { 
    switch ($ref(row)) {
    case 0:
        row0_low;
        break;
    case 1:
        row1_low;
        break;
    case 2:
        row2_low;
        break;        
    case 3:
        row3_low;
        break;
    case 4:
        row4_low;
        break;
    case 5:
        row5_low;
        break;        
    case 6:
        row6_low;
        break;
    case 7:
        row7_low;
        break;
    case 8:
        row8_low;
        break;        
    }
    switch ($ref(column)) {
    case 0:
        col0_high;
        break;
    case 1:
        col1_high;
        break;
    case 2:
        col2_high;
        break;        
    case 3:
        col3_high;
        break;
    case 4:
        col4_high;
        break;
    case 5:
        col5_high;
        break;        
    case 6:
        col6_high;
        break;
    case 7:
        col7_high;
        break;
    case 8:
        col8_high;
        break;        
    case 8:
        col9_high;
        break;
    case 10:
        col10_high;
        break;        
    case 11:
        col11_high;
        break;
    case 12:
        col12_high;
        break;
    case 13:
        col13_high;
        break;        
    }
	
#ifdef _DELAY_MS
	_delay_ms(1);
#endif	
        /* turns off all the LEDs in the array */
	//rows high
	row0_high;
	row1_high;
	row2_high;
	row3_high;
	row4_high;
	row5_high;
	
	//columns low
	col0_low;
	col1_low;
	col2_low;
	col3_low;
	col4_low;
	col5_low;
	col6_low;
	col7_low;
	col8_low;
	col9_low;
	col10_low;
	col11_low;
	col12_low;
	col13_low;
#endif /* __AVR__ */ 

}
/**/
