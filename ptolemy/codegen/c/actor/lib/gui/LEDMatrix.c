/***preinitBlock***/

/** LEDMatrix Portion has the following copyright */
/**
* Author: Leah Buechley
* Filename: game_of_life.h
* Chip: ATmega16
* Date: 3/30/2006
* Purpose:
*        This program was written for a wearable LED tank top.
*        More information in game_of_life.c and at: 
*        http://www.cs.colorado.edu/~buechley/diy/diy_tank.html
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

#include <avr/io.h>
/* F_CU defines the clock speed of your chip
   if you change the clock speed of your chip you must change the definition of F_CPU!!!
   for 8Mhz speed use:
   #define F_CPU 8000000UL
   for 1Mhz speed (chip is shipped at 1Mhz) use:
   #define F_CPU 1000000UL
*/
#define F_CPU 8000000UL
//WinAVR 20040404 does not have util/delay.h, so _delay_ms might not be defined
//#include <avr/delay.h>

#define _DELAY_MS
#include <util/delay.h>

//MACROS FOR AVR ATmega16
#define row0_output DDRA|= _BV(PA0)
#define row1_output DDRA|= _BV(PA1)
#define row2_output DDRA|= _BV(PA2)
#define row3_output DDRA|= _BV(PA3)
#define row4_output DDRA|= _BV(PA4)
#define row5_output DDRA|= _BV(PA5)
#define row6_output DDRA|= _BV(PA6)
#define row7_output DDRA|= _BV(PA7)

#define row0_high PORTA|= _BV(PA0)
#define row1_high PORTA|= _BV(PA1)
#define row2_high PORTA|= _BV(PA2)
#define row3_high PORTA|= _BV(PA3)
#define row4_high PORTA|= _BV(PA4)
#define row5_high PORTA|= _BV(PA5)
#define row6_high PORTA|= _BV(PA6)
#define row7_high PORTA|= _BV(PA7)

#define row0_low PORTA &= ~_BV(PA0)
#define row1_low PORTA &= ~_BV(PA1)
#define row2_low PORTA &= ~_BV(PA2)
#define row3_low PORTA &= ~_BV(PA3)
#define row4_low PORTA &= ~_BV(PA4)
#define row5_low PORTA &= ~_BV(PA5)
#define row6_low PORTA &= ~_BV(PA6)
#define row7_low PORTA &= ~_BV(PA7)

#define col0_output DDRC|= _BV(PC0)
#define col1_output DDRC|= _BV(PC1)
#define col2_output DDRC|= _BV(PC2)
#define col3_output DDRC|= _BV(PC3)
#define col4_output DDRC|= _BV(PC4)

#define col5_output DDRB|= _BV(PB4)
#define col6_output DDRB|= _BV(PB3)
#define col7_output DDRB|= _BV(PB2)
#define col8_output DDRB|= _BV(PB1)
#define col9_output DDRB|= _BV(PB0)

#define col0_high PORTC|= _BV(PC0)
#define col1_high PORTC|= _BV(PC1)
#define col2_high PORTC|= _BV(PC2)
#define col3_high PORTC|= _BV(PC3)
#define col4_high PORTC|= _BV(PC4)

#define col5_high PORTB|= _BV(PB4)
#define col6_high PORTB|= _BV(PB3)
#define col7_high PORTB|= _BV(PB2)
#define col8_high PORTB|= _BV(PB1)
#define col9_high PORTB|= _BV(PB0)

#define col0_low PORTC &= ~_BV(PC0)
#define col1_low PORTC &= ~_BV(PC1)
#define col2_low PORTC &= ~_BV(PC2)
#define col3_low PORTC &= ~_BV(PC3)
#define col4_low PORTC &= ~_BV(PC4)

#define col5_low PORTB &= ~_BV(PB4)
#define col6_low PORTB &= ~_BV(PB3)
#define col7_low PORTB &= ~_BV(PB2)
#define col8_low PORTB &= ~_BV(PB1)
#define col9_low PORTB &= ~_BV(PB0)
#endif /* __AVR__ */ 

/* End of LEDMatrix defines */

/* turns off all the LEDs in the array */
void all_on (void)
{
#ifdef __AVR__
        //rows low
        row0_low;
        row1_low;
        row2_low;
        row3_low;
        row4_low;
        row5_low;
        row6_low;
        row7_low;
        
        //columns high
        col0_high;
        col1_high;
        col2_high;
        col3_high;
        col4_high;
        col5_high;
        col6_high;
        col7_high;
        col8_high;
        col9_high;
#endif /* __AVR__ */ 
}

/* turns on all the LEDs in the array */
void all_off (void)
{
#ifdef __AVR__
        //rows high
        row0_high;
        row1_high;
        row2_high;
        row3_high;
        row4_high;
        row5_high;
        row6_high;
        row7_high;
        
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
#endif /* __AVR__ */ 
}

//initializes the pins to be used for the display as outputs
void initialize_tank_pins_as_output (void) {
#ifdef __AVR__
    row0_output;
    row1_output;
    row2_output;
    row3_output;
    row4_output;
    row5_output;
    row6_output;
    row7_output;
    
    col0_output;
    col1_output;
    col2_output;
    col3_output;
    col4_output;
    col5_output;
    col6_output;
    col7_output;
    col8_output;
    col9_output;

    all_off();
#endif
}
/**/

/***initBlock***/
initialize_tank_pins_as_output();
/**/

/***fireBlock***/
#ifndef __AVR__
/* Machines that don't have the hardware just print 0 and 1. */
if ($ref(row) == 0) {
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

#else /* !  __AVR__ */ 
/* LED Tank Top Code from
 *   http://craftzine.com/01/led
 *   http://www.cs.colorado.edu/~buechley/diy/diy_tank.html
 */ 
if ($ref(control)) { 
    switch ($ref(row)) {
    case 0:
        row0_high;
        break;
    case 1:
        row1_high;
        break;
    case 2:
        row2_high;
        break;        
    case 3:
        row3_high;
        break;
    case 4:
        row4_high;
        break;
    case 5:
        row5_high;
        break;        
    case 6:
        row6_high;
        break;
    case 7:
        row7_high;
        break;
    }
    switch ($ref(column)) {
    case 0:
        col0_low;
        break;
    case 1:
        col1_low;
        break;
    case 2:
        col2_low;
        break;        
    case 3:
        col3_low;
        break;
    case 4:
        col4_low;
        break;
    case 5:
        col5_low;
        break;        
    case 6:
        col6_low;
        break;
    case 7:
        col7_low;
        break;
    case 8:
        col8_low;
        break;
    case 9:
        col9_low;
        break;
    }

#ifdef _DELAY_MS
        _delay_ms(1000);
#endif        

        all_on();
}
#endif /* ! __AVR__ */ 

/**/
