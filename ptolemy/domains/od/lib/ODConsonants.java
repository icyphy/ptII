/* 

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.od.lib;

import ptolemy.domains.od.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ODConsonants
/** 


@author John S. Davis II
@version %W%	%G%
*/
public class ODConsonants extends ODStringSource {

    /** 
     */
    public ODConsonants(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _consonants = new LinkedList();
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this actor.
     */
    public LinkedList setUpStrings() {
        
        _consonants.insertLast( new StringTime( 0.0, "H" ) );
        
        _consonants.insertLast( new StringTime( 1.0, "l" ) );
        
        _consonants.insertLast( new StringTime( 1.5, "l" ) );
        
        _consonants.insertLast( new StringTime( 4.0, "T" ) );
        
        _consonants.insertLast( new StringTime( 4.5, "h" ) );
        
        _consonants.insertLast( new StringTime( 6.0, "P" ) );
        
        _consonants.insertLast( new StringTime( 6.5, "t" ) );
        
        _consonants.insertLast( new StringTime( 7.5, "l" ) );
        
        _consonants.insertLast( new StringTime( 8.5, "m" ) );
        
        _consonants.insertLast( new StringTime( 9.0, "y" ) );
        
        _consonants.insertLast( new StringTime( 10.5, "D" ) );
        
        _consonants.insertLast( new StringTime( 11.5, "D" ) );
        
        _consonants.insertLast( new StringTime( 12.5, "m" ) );
        
        _consonants.insertLast( new StringTime( 14.0, "n" ) );
        
        _consonants.insertLast( new StringTime( 5000.0, ";" ) );
        
        // Up to "Hello!! The" run
        
        return _consonants;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private LinkedList _consonants;
    
}




















