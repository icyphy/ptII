/* The Filter class

 Copyright (c) 1998 The Regents of the University of California.
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
*/

package ptolemy.math.filter;

import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// Filter
/** 
The Filter class is an abstract class from which the AnalogFilter and 
DigitalFilter are derived.  This class contains the shared methods of 
AnalogFilter and DigitalFilter.
@author  David Teng (davteng@hkn.eecs.berkeley.edu)
@version %W%	%G%
*/

public abstract class Filter {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // Constants describe the various types of digital filter
    public final static int BLANK = 0;
    public final static int IIR = 1;
    public final static int FIRWIN = 2;
    public final static int FIROPT = 3;
    public final static int FIRFS = 4;
 
    // Constants describe the approximation method for (IIR) filter
    public final static int BUTTERWORTH = 1; 
    public final static int CHEBYSHEV1 = 2; 
    public final static int CHEBYSHEV2 = 3; 
    public final static int ELLIPTICAL = 4; 
 
    // Constants describe the frequency band type
    public final static int LOWPASS = 1; 
    public final static int HIGHPASS = 2; 
    public final static int BANDPASS = 3; 
    public final static int BANDSTOP = 4; 
 
    // Constants describe the analog to digital transfer method
    public final static int BILINEAR = 1; 
    public final static int IMPULSEINVAR = 2; 
    public final static int MATCHZ = 3; 
    
}










