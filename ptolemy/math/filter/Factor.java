/* A factor type class.

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

import ptolemy.math.*;

//////////////////////////////////////////////////////////////////////////
//// Factor
/** 
  This is the factor class.  It is the component that will be used to build
  LTI transfer function.  This is an abstract class to be derived by RealFactor
  class and ComplexFactor class.  It provide some abstract methods that will 
  be shared among these two classes. 
<p> 
@author  William Wu (wbwu@eecs.berkeley.edu)
@version %W%	%G%
*/

public abstract class Factor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return all the poles of this factor.  This method should be 
     * overwritten in the derived class.
     * @return array of Complex
     */	
    public abstract Complex[] getPoles();

    /** Return all the zeroes of this factor.  This method should be 
     * overwritten in the derived class.
     * @return array of Complex
     */	
    public abstract Complex[] getZeroes();

    /** Check if this factor contains the given pole.  This method should be 
     * overwritten in the derived class.
     * @param pole the given pole to be checked on.
     * @return boolean value indicating if the given pole is part of this 
     *         factor.
     */	
    public abstract boolean ifPole(Complex pole);

    /** Check if this factor contains the given zero.  This method should be 
     * overwritten in the derived class.
     * @param zero the given zero to be checked on.
     * @return boolean value indicating if the given pole is part of this factor
     */	
    public abstract boolean ifZero(Complex zero);

    /** Move the given pole to the given value.  This method should be 
     * overwritten in the derived class.
     * @param pole the given pole to be moved
     * @param real destination's real value
     * @param imag destination's imaginary value
     */	
    public abstract void movePole(Complex pole, double real, double imag);
    /** Move the given zero to the given value.  This method should be 
     * overwritten in the derived class.
     * @param zero the given zero to be moved
     * @param real destination's real value
     * @param imag destination's imaginary value
     */	
    public abstract void moveZero(Complex zero, double real, double imag);


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Solve the pole/zero of this factor 
    private abstract void _solvePoleZero();
    
}



