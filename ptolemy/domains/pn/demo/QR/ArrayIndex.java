/* Class defining methods to store and retrieve values in a flexible way.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.domains.pn.demo.QR;

import java.lang.*;
import java.util.*;

/**

This class defines an associative array and methods to store and
retrieve data in the array.

@author Bart Kienhuis
@version $Id$
*/


public class ArrayIndex {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
 
    /** Construct an ArrayIndex object. */
    public ArrayIndex() {
	super();
    }

    /** Create a key for an 1 dimensional index reference. This key is
        used to store and retrieve values in the associate array. 
        @param in_1 an index value.
        @return a key representing the index vector.
    */
    public String atKey(int i_1 ) {
	String key =  "(" + i_1 + ")";
	return key;
    }

    /** Create a key for an 2 dimensional index reference. This key is
        used to store and retrieve values in the associate array. 
        @param in_1 an index value.
        @param in_2 an index value.
        @return a key representing the index vector.
    */
    public String atKey(int i_1, int i_2 ) {
	String key =  "(" + i_1 + "," + i_2 + ")";
	return key;
    }

    /** Create a key for an 3 dimensional index reference. This key is
        used to store and retrieve values in the associate array. 
        @param in_1 an index value.
        @param in_2 an index value.
        @param in_3 an index value.
        @return a key representing the index vector.
    */
    public String atKey(int i_1, int i_2, int i_3 ) {
	String key =  "(" + i_1 + "," + i_2 + "," + i_3 +  ")";
	return key;
    }

    /** Create a key for an 4 dimensional index reference. This key is
        used to store and retrieve values in the associate array. 
        @param in_1 an index value.
        @param in_2 an index value.
        @param in_3 an index value.
        @param in_4 an index value.
        @return a key representing the index vector.
    */
    public String atKey(int i_1, int i_2, int i_3, int i_4 ) {
	String key =  "(" + i_1 + "," + i_2 + "," + i_3 + "," + i_4
	    +")";
	return key;
    }

    /** Create a key for an 5 dimensional index reference. This key is
        used to store and retrieve values in the associate array. 
        @param in_1 an index value.
        @param in_2 an index value.
        @param in_3 an index value.
        @param in_4 an index value.
        @param in_5 an index value.
        @return a key representing the index vector.
    */
    public String atKey(int i_1, int i_2, int i_3, int i_4, int i_5 ) {
	String key =  "(" + i_1 + "," + i_2 + "," + i_3 + "," + i_4
	    + "," + i_5 + ")";
	return key;
    }
    
    /** Create a key for an 6 dimensional index reference. This key is
        used to store and retrieve values in the associate array. 
        @param in_1 an index value.
        @param in_2 an index value.
        @param in_3 an index value.
        @param in_4 an index value.
        @param in_5 an index value.
        @param in_6 an index value.
        @return a key representing the index vector.
    */
    public String atKey(int i_1, int i_2, int i_3, int i_4, int i_5,
            int i_6 ) {
	String key =  "(" + i_1 + "," + i_2 + "," + i_3 + "," + i_4
	    + "," + i_5 + "," + i_6  + ")";
	return key;
    }

    /** Retrieve a value from the associate array using the supplied key.
        @param aKey the key.
        @return the stored value.
        @throws Exception if value stored in the associative array does not 
        exits.
    */
    public double retrieve(String aKey ) {
	Double value = (Double) _map.get( aKey );
	if ( value == null ) {
	    throw new Error(" --- NULL Value retrieved for key " + aKey );
	}
	return value.doubleValue();
    }

    /** Store a data value at a particular location given by the key string. 
        @param aValue the value.
        @param aKey the key.
    */
    public void store(double aValue, String aKey) {
	_map.put( aKey, new Double(aValue) );
    }

    /** Read in a matrix with a given name and store it into a
     *  associative data structure. The associate data structure make
     *  the access of the data simple. Instead of reading from a file
     *  system, the method reads from the source matrix supplied by
     *  SourceMatrix class. This make the access of the matrix
     *  possible inside a Applet.
     *
     *  @param file The filename.
     */
    public void ReadMatrix(String filename ) {
	if ( filename == "U_1000x16" ) {
	    for (int i = 0; i < 500; i++) {
		for (int j = 0; j < 16; j++) {
		    String key = atKey(i+1, j+1);
		    _map.put(key, new Double( x_0.sourcematrix_0[i][j] ));
		}
	    }
	} else {
	    // CREATE matrix Zeros64x64
	    for (int i = 0; i < 64; i++) {
		for (int j = 0; j < 64; j++) {
		    String key = atKey(i, j);
                    _map.put(key, new Double( 0.0 ));
		}
	    }

	}
    }

    /** Write the matrix stored in the associate array with a given
        name. Currently not further implemented.
        @param filename the filename.
    */
    public void WriteMatrix(String filename ) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A reference to the SourceMatrix used to initialize the
        Associate Array.*/
    private SourceMatrix x_0;

    /** Associative Array that is used to store and retrieve data. */
    private Map _map = new HashMap();

    
}
