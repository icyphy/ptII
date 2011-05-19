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
#ifndef F_CPU
#define F_CPU 8000000UL
#endif /* F_CPU */
//WinAVR 20040404 does not have util/delay.h, so _delay_ms might not be defined
//#include <avr/delay.h>

#define _DELAY_MS
#include <util/delay.h>

//MACROS FOR AVR ATmega16
#if defined(DDRA)
#define row0_output DDRA|= _BV(PA0)
#define row1_output DDRA|= _BV(PA1)
#define row2_output DDRA|= _BV(PA2)
#define row3_output DDRA|= _BV(PA3)
#define row4_output DDRA|= _BV(PA4)
#define row5_output DDRA|= _BV(PA5)
#define row6_output DDRA|= _BV(PA6)
#define row7_output DDRA|= _BV(PA7)
#endif /* DDRA */

#ifndef row0_output
// For the Arduino
int ledPin =13;
#endif /* row0_output */

#if defined(PORTA)
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
#endif /* PORTA */

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

#if defined(PORTC)
#define col0_high PORTC|= _BV(PC0)
#define col1_high PORTC|= _BV(PC1)
#define col2_high PORTC|= _BV(PC2)
#define col3_high PORTC|= _BV(PC3)
#define col4_high PORTC|= _BV(PC4)
#endif /* PORTC */

#if defined(PORTB)
#define col5_high PORTB|= _BV(PB4)
#define col6_high PORTB|= _BV(PB3)
#define col7_high PORTB|= _BV(PB2)
#define col8_high PORTB|= _BV(PB1)
#define col9_high PORTB|= _BV(PB0)
#endif /* PORTB */

#if defined(PORTC)
#define col0_low PORTC &= ~_BV(PC0)
#define col1_low PORTC &= ~_BV(PC1)
#define col2_low PORTC &= ~_BV(PC2)
#define col3_low PORTC &= ~_BV(PC3)
#define col4_low PORTC &= ~_BV(PC4)
#endif /* PORTC */

#if defined(PORTB)
#define col5_low PORTB &= ~_BV(PB4)
#define col6_low PORTB &= ~_BV(PB3)
#define col7_low PORTB &= ~_BV(PB2)
#define col8_low PORTB &= ~_BV(PB1)
#define col9_low PORTB &= ~_BV(PB0)
#endif /* PORTB */
#endif /* __AVR__ */

/* End of LEDMatrix defines */

/* turns off all the LEDs in the array */
void all_on (void)
{
#ifdef __AVR__
        //rows low
#if defined(row0_low)
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
#endif /* row0_low */
#endif /* __AVR__ */
}

/* turns on all the LEDs in the array */
void all_off (void)
{
#ifdef __AVR__
#if defined(row0_high)
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
#endif /* row0_high */
#endif /* __AVR__ */
}

//initializes the pins to be used for the display as outputs
void initialize_tank_pins_as_output (void) {
#ifdef __AVR__
#if defined(row0_output)
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
#else /* row0_output */

    pinMode(ledPin, OUTPUT);
#endif /* row0_output */
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
#ifdef row0_high
        row0_high;
#else
        digitalWrite(ledPin, HIGH);
#endif
        break;
    case 1:
#ifdef row1_high
        row1_high;
#endif
        break;
    case 2:
#ifdef row2_high
        row2_high;
#endif
        break;
    case 3:
#ifdef row3_high
        row3_high;
#endif
        break;
    case 4:
#ifdef row4_high
        row4_high;
#endif
        break;
    case 5:
#ifdef row5_high
        row5_high;
#endif
        break;
    case 6:
#ifdef row6_high
        row6_high;
#endif
        break;
    case 7:
#ifdef row7_high
        row7_high;
#endif
        break;
    }
    switch ($ref(column)) {
    case 0:
#ifdef col0_low
        col0_low;
#else
        digitalWrite(ledPin, LOW);
#endif
        break;
    case 1:
#ifdef col1_low
        col1_low;
#endif
        break;
    case 2:
#ifdef col2_low
        col2_low;
#endif
        break;
    case 3:
#ifdef col3_low
        col3_low;
#endif
        break;
    case 4:
#ifdef col4_low
        col4_low;
#endif
        break;
    case 5:
#ifdef col5_low
        col5_low;
#endif
        break;
    case 6:
#ifdef col6_low
        col6_low;
#endif
        break;
    case 7:
#ifdef col7_low
        col7_low;
#endif
        break;
    case 8:
#ifdef col8_low
        col8_low;
#endif
        break;
    case 9:
#ifdef col9_low
        col9_low;
#endif
        break;
    }

#ifdef _DELAY_MS
        _delay_ms(1000);
#endif

        all_on();
}
#endif /* ! __AVR__ */

/**/
