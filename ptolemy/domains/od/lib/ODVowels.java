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
//// ODVowels
/** 


@author John S. Davis II
@version @(#)ODVowels.java	1.1	11/12/98
*/
public class ODVowels extends ODStringSource {

    /** 
     */
    public ODVowels(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _vowels = new LinkedList();
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this actor.
     */
    public LinkedList setUpStrings() {
        
        _vowels.insertLast( new StringTime( 0.5, "e" ) );
        
        _vowels.insertLast( new StringTime( 2.0, "o" ) );
        
        _vowels.insertLast( new StringTime( 5.0, "e" ) );
        
        _vowels.insertLast( new StringTime( 7.0, "o" ) );
        
        _vowels.insertLast( new StringTime( 8.0, "e" ) );
        
        _vowels.insertLast( new StringTime( 10.0, "O" ) );
        
        _vowels.insertLast( new StringTime( 12.0, "o" ) );
        
        _vowels.insertLast( new StringTime( 13.0, "a" ) );
        
        _vowels.insertLast( new StringTime( 13.5, "i" ) );
        
        _vowels.insertLast( new StringTime( 15.0, "i" ) );
        
        _vowels.insertLast( new StringTime( 17.0, "o" ) );
        
        _vowels.insertLast( new StringTime( 19.0, "e" ) );
        
        _vowels.insertLast( new StringTime( 20.0, "e" ) );
        
        // _vowels.insertLast( new StringTime( 5000.0, ";" ) );
        
        // Up to "Hello!! The Ptolemy Domain is complete."
        
        return _vowels;
    }
    
    public void wrapup() throws IllegalActionException {
        // System.out.println("ODVowel current time = "+getCurrentTime() );
        super.wrapup();
    }
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private LinkedList _vowels;
    
}




















