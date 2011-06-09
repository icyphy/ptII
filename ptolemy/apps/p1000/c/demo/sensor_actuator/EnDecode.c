/** Utilities for the type infrastructure.

 Copyright (c) 1997-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 @author Slobodan Matic
 @Parts of this code that serves to interface the hardware is copied from
 previous written code samples from Agilent

 */

#include "ptpHwP1000LinuxDr.h"

// --------------------------------------------------------------------------
// Encode nsec into the HW form
void encodeHwNsec( FPGA_TIME *hwTime,
        const unsigned int secs,
        const unsigned int nsec)
{
    static double nsecToHw = pow(2,30) / 1e9;

    if (hwTime)
        {
            hwTime->hwNsec = (unsigned int) ( nsec * nsecToHw); // 0.93132257461548);
            hwTime->secs = secs;
        }
}

// Decode sec, nsec from the HW form. Deal with even and odd values
void decodeHwNsec(
        const FPGA_TIME *hwTime,
        unsigned int *secs,
        unsigned int *nsec)
{
    static double hwToNsec = 1e9 / pow(2,30);

    if (hwTime)
        {

            *nsec = (unsigned int) ( (hwTime->hwNsec & 0x7fffffff) * hwToNsec); //1.073741824);
    *secs = hwTime->secs;
    if ( hwTime->hwNsec & 0x80000000)
        {
            // Must add 10 nsec		//ADDING 10 NSEC?
            *nsec += 10;
            if (*nsec >= 1000000000)
                {
                    *secs = *secs + 1;
                    *nsec = *nsec - 1000000000;
                }

        }
	}

}
