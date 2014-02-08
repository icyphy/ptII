/*********************************************
* Author: Leah Buechley
* Filename: game_of_life.c
* Chip: ATmega16
* Date: 3/30/2006
* Purpose:
*        This program implements the Game of Life on a 6 x 14 LED
*        array. It was written for a wearable LED tank top.
*        More information at:
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

*********************************************/

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
//WinAVR 20040404 does not have util/delay.h, so _delay_ms is not defined
//#include <avr/delay.h>

#define _DELAY_MS
#include <util/delay.h>

#endif /*__AVR__*/
#include "game_of_life.h"

/* constants and variables defining the size and properties
   of the LED array and corresponding Game of Life (GOL) array.
   the GOL array will hold information about the current and
   next state for each cell in the GOL array/grid.
*/
//const unsigned char number_rows = 6;
//const unsigned char number_columns = 14;
//const unsigned char size_of_array = 84;
//volatile unsigned char array [6][14];

const unsigned char number_rows = 8;
const unsigned char number_columns = 10;
const unsigned char size_of_array = 80;
volatile unsigned char array [8][10];

/* constants for resetting the values of the GOL array */
const unsigned char on_next_on = 3;                        //current state is on and next state is on
const unsigned char off_next_on = 2;                //current state is off and next state is on
const unsigned char on_next_off = 1;                //current state is on and next state is off (default on state)
const unsigned char off_next_off = 0;                //current state is off and next state is off (default off state)


/******************************************************************************************/
/*  initialization function */
/******************************************************************************************/

//initializes the pins to be used for the display as outputs
void initialize_tank_pins_as_output (void)
{
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
}


/******************************************************************************************/
/*  functions for row-column display */
/******************************************************************************************/

/* given a row value (i), and a column value (j), turns on the LED
   at position i,j in the LED array for 1ms */
void row_column_display (unsigned char i, unsigned char j)
{
#ifndef __AVR__
    printf("%d %d\n", i, j);
#else
        if (i==0)
                row0_high;
        else
                if (i==1)
                        row1_high;
        else
                if (i==2)
                        row2_high;
        else
                if (i==3)
                        row3_high;
        else
                if (i==4)
                        row4_high;
        else
                if (i==5)
                        row5_high;
        else
                if (i==6)
                        row6_high;
        else
                if (i==7)
                        row7_high;

        if (j==0)
                col0_low;
        else
                if (j==1)
                        col1_low;
        else
                if (j==2)
                        col2_low;
        else
                if (j==3)
                        col3_low;
        else
                if (j==4)
                        col4_low;
        else
                if (j==5)
                        col5_low;
        else
                if (j==6)
                        col6_low;
        else
                if (j==7)
                        col7_low;
        else
                if (j==8)
                        col8_low;
        else
                if (j==9)
                        col9_low;
#endif

#ifdef _DELAY_MS
        _delay_ms(1000);
#endif
        all_on ();
}

/* given a column value (j), turns on the LEDs in column
   j in the LED array for 5ms */
void column_display (unsigned char j)
{

        row0_low;
        row1_low;
        row2_low;
        row3_low;
        row4_low;
        row5_low;
        row6_low;
        row7_low;

        if (j==0)
                col0_high;
        else
                if (j==1)
                        col1_high;
        else
                if (j==2)
                        col2_high;
        else
                if (j==3)
                        col3_high;
        else
                if (j==4)
                        col4_high;
        else
                if (j==5)
                        col5_high;
        else
                if (j==6)
                        col6_high;
        else
                if (j==7)
                        col7_high;
        else
                if (j==8)
                        col8_high;
        else
                if (j==9)
                        col9_high;

#ifdef _DELAY_MS
        _delay_ms(5);
#endif

        all_off ();
}

/******************************************************************************************/
/*  functions useful for troubleshooting display */
/******************************************************************************************/

/* turns on all the LEDs in the array */
void all_on (void)
{
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
}

/* turns off all the LEDs in the array */
void all_off (void)
{
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
}

/* loops through all of the LEDs in the array turning
   each one on in turn */
void loop_lights (void)
{
        unsigned char x,y;
        int z;

        for(x=0;x<number_rows;x++)
        {
                y=0;
                for (y=0;y<number_columns;y++)
                {
                 //  for (z=0;z<10000;z++)
                                row_column_display(x,y);
#ifdef _DELAY_MS
                                _delay_ms(5000);
#endif
                }
        }
}


/******************************************************************************************/
/*  functions for the Game of Life cellular automaton */
/******************************************************************************************/

/* set each element in the array to zero*/
void zero_array (void)
{
        volatile int i=0;
        volatile int j=0;
        while (i<number_rows)
        {
                j=0;
                while (j<number_columns)
                {
                        array[i][j] = 0;
                        j++;
                }
                i++;
        }
}

/* for each array element that is = 1, turns on the corresponding LED */
void display_array (void)
{
        volatile unsigned char i=0;
        volatile unsigned char j=0;
        volatile unsigned char dummy;
#ifdef __AVR__
        volatile unsigned char dummyCount=250;
#else
        volatile unsigned char dummyCount=1;
#endif

        for (dummy=0;dummy<dummyCount; dummy++)
        {

                i=0;
                while (i<number_rows)
                {
                        j=0;
                        while (j<number_columns) {
#ifdef __AVR__
                            if (array[i][j] == 1) {
                                row_column_display(i,j);
                            }
#else
#ifdef GOL_DISPLAY
                            printf("%d", array[i][j]);
#endif
#endif
                            j++;
                        }
#ifndef __AVR__
#ifdef GOL_DISPLAY
                        printf("\n");
#endif
#endif

                        i++;
                }
#ifndef __AVR__
#ifdef GOL_DISPLAY
                printf("\n");
#endif
#endif
        }
}

/* sets array to reflect next state of GOL iteration.
   see GOL function for context. */
void reset_array (void)
{
        volatile unsigned char i=0;
        volatile unsigned char j=0;
        while (i<number_rows)
        {
                j=0;
                while (j<number_columns)
                {
                        if ((array[i][j] == on_next_on) | (array[i][j] == off_next_on))
                                array[i][j] = 1;
                        else
                                array[i][j] = 0;
                        j++;
                }
                i++;
        }
}

/* computes 1 iteration of the game of life */
void gol (void)
{
        volatile unsigned char i=0;
        volatile unsigned char j=0;
        volatile unsigned char number_neighbors=0;

        //variables to hold boundary condition information
        volatile unsigned char i_minus1=0;
        volatile unsigned char i_plus1=0;
        volatile unsigned char j_minus1=0;
        volatile unsigned char j_plus1=0;
        /* note: the boundaries of the shirt are the top and bottom
                         rows and the seam where the first and last
                         columns meet.  we will treat the shirt
                         as a torus, meaning that the cells on the top row
                         will be neighbors to the cells on the bottom and vice versa.
                         likewise, the cells in the first and last columns will
                         be neighbors.
        */

        //display current state
        display_array ();

        //compute next state
        while (i<number_rows)
        {
                //compute boundary condition information
                i_plus1 = i+1;
                i_minus1 = i-1;
                if (i==number_rows-1)
                        //  o x o
                        //  o o o
                        //  o n o
                        i_plus1 = 0;
                if (i==0)
                        //  o n o
                        //  o o o
                        //  o x o
                        i_minus1 = number_rows-1;
                j=0;
                while (j<number_columns)
                {
                        j_plus1 = j+1;
                        j_minus1 = j-1;
                        if (j==number_columns-1)
                                //  o o o
                                //  n o x
                                //  o o o
                                j_plus1 = 0;
                        if (j==0)
                                //  o o o
                                //  x o n
                                //  o o o
                                j_minus1 = number_columns-1;

                        //compute number of neighbors for each cell
                        number_neighbors = 0;
                        if ((array[i_minus1][j_minus1]==1) | (array[i_minus1][j_minus1]==on_next_on))
                                number_neighbors++;
                        if ((array[i_minus1][j] == 1) | (array[i_minus1][j] == on_next_on))
                                number_neighbors++;
                        if ((array[i_minus1][j_plus1] == 1) | (array[i_minus1][j_plus1] == on_next_on))
                                number_neighbors++;
                        if ((array[i][j_minus1] == 1) | (array[i][j_minus1] == on_next_on))
                                number_neighbors++;
                        if ((array[i][j_plus1] == 1) | (array[i][j_plus1] == on_next_on))
                                number_neighbors++;
                        if ((array[i_plus1][j_minus1] == 1) | (array[i_plus1][j_minus1] == on_next_on))
                                number_neighbors++;
                        if ((array[i_plus1][j] == 1) | (array[i_plus1][j] == on_next_on))
                                number_neighbors++;
                        if ((array[i_plus1][j_plus1] == 1) | (array[i_plus1][j_plus1] == on_next_on))
                                number_neighbors++;

                        //compute rule
                        if (array[i][j] == 1)
                        {
                                if ((number_neighbors == 2) | (number_neighbors == 3))
                                        array[i][j]=on_next_on;                        //current state is on and next state is on
                        }
                        else
                                if (number_neighbors == 3)
                                        array[i][j]=off_next_on;                //current state is off and next state is on
                        j++;
                }
                i++;
        }

        //reset array to reflect next state
        reset_array();
}

void glider (unsigned char i, unsigned char j)
{
        if (i<(number_rows + 2) && j<(number_columns + 2))
        {
                array[i][j]=1;
                array[i+1][j]=1;
                array[i+2][j]=1;
                array[i+2][j+1]=1;
                array[i+1][j+2]=1;
        }
}

void blinker (unsigned char i, unsigned char j)
{
        if (i<(number_rows + 2) && j<number_columns)
        {
                array[i][j]=1;
                array[i+1][j]=1;
                array[i+2][j]=1;
        }
}

void r_pentomino (unsigned char i, unsigned char j)
{
        if (i<(number_rows + 2) && j<(number_columns + 2))
        {
                array[i][j+1]=1;
                array[i+1][j]=1;
                array[i+1][j+1]=1;
                array[i+2][j+1]=1;
                array[i+2][j+2]=1;
        }
}
