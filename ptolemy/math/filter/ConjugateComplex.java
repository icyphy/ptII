/* A complex conjugate data type.

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

*/

package ptolemy.math.filter;

import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// ConjugateComplex 
/**
  A complex conjugate data type.  This class contain two complex number,
  which form a complex conjugate pair.  This class ensures the pair is always
  conjugate of each other.  
  <p>
  @author: William Wu (wbwu@eecs.berkeley.edu)
  @version: $id$
  @date: 11/21/98
*/ 

public class ConjugateComplex {

    /**
     * Constructor.  Constructor takes a Complex data, saves it,
     * then creates and saves its conjugate.
     */
    public ConjugateComplex(Complex indata){
        value = indata;
        conj = value.conjugate();
    } 

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Changed value of the complex conjugate pair.  Given a new complex
     * value, saves it, then creates and saves its complex conjugate value.
     * <p>
     * @param indata new complex value.
     */ 
    public void setValue(Complex indata){
        value = indata;
        conj = value.conjugate();
    } 

    /**
     * Get one of the complex value from the pair.
     * @return one complex value in the conjugate pair.
     */
    public Complex getValue(){
        return value;
    }
    
    /**
     * Get the other one of the complex value from the pair.
     * @return one complex value in the conjugate pair.
     */
    public Complex getConjValue(){
        return conj;
    }
   
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
 
    private Complex value;
    private Complex conj;

}
