/* Utilities for the P1000 device, not including the device driver.

 Copyright (c) 1997-2005 The Regents of the University of California.
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

 @author Thomas Huining Feng, Yang Zhao
 
 */

#include <math.h>
#include "p1000_utils.h"

/**
 * Encode the time, given by seconds and nano-seconds within a second, into a
 * hardware time structure.
 * 
 * @param hwTime Reference to the hardware time structure to store the result.
 * @param secs Number of seconds.
 * @param nsec Number of nano-seconds within a second.
 * @see decodeHwNsec(const FPGA_TIME *, unsigned int*, unsigned int*)
 */
void encodeHwNsec(FPGA_TIME* hwTime, const unsigned int secs,
	const unsigned int nsec) {
	static double nsecToHw;
	nsecToHw = pow(2,30) / 1e9;

    if (hwTime) {
    	hwTime->hwNsec = (unsigned int) (nsec * nsecToHw);
    	hwTime->secs = secs;
	}
}

/**
 * Decode the time, given by the hardware time structure, and return the number
 * of seconds and nano-seconds within a second.
 * 
 * @param hwTime Reference to the hardware time structure.
 * @param secs Number of seconds.
 * @param nsec Number of nano-seconds within a second.
 * @see encodeHwNsec(FPGA_TIME*, const unsigned int, const unsigned int)
 */
void decodeHwNsec(const FPGA_TIME* hwTime, unsigned int* secs,
	unsigned int* nsec) {
	static double hwToNsec;
	hwToNsec = 1e9 / pow(2,30);

	if (hwTime) {
		*nsec = (unsigned int) ((hwTime->hwNsec & 0x7fffffff) * hwToNsec);
		*secs = hwTime->secs;
		if ( hwTime->hwNsec & 0x80000000) {
			// Must add 10 nsec
			*nsec += 10;
			if (*nsec >= 1000000000) {
				*secs = *secs + 1;
				*nsec = *nsec - 1000000000;
			}
		}
	}
}
