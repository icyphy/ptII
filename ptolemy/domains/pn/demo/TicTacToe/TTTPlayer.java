/* Represents the computer in a game of TicTacToe

 Copyright (c) 1997-1999 The Regents of the University of California.
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

package ptolemy.domains.pn.demo.TicTacToe;

import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;

import java.util.Enumeration;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// TTTPlayer
/** 
A multiple input double precision adder.
Input and output are DoubleMatrixTokens.

@author Mudit Goel
@version $Id$
*/
public class TTTPlayer extends AtomicActor {

    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public TTTPlayer(CompositeActor container, String name)
	    throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new IOPort(this, "input", true, false);
        output = new IOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the matrix input and determine the position to be played.
     */
    public void fire() throws IllegalActionException {
        int[][] moves = ((IntMatrixToken)input.get(0)).intMatrix();
	_decideMove(moves);
	output.broadcast(new IntToken(_row));
	output.broadcast(new IntToken(_col));
    }                

    /** Initialize and set the move variables */
    public void initialize() throws IllegalActionException {
	super.initialize();
	_row = -1;
	_col = -1;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  //// 

    // The input port 
    public IOPort input;
    // The output port 
    public IOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /**Make a decision on the move
     */
    private void _decideMove(int[][] moves) {
	boolean done = false;
	for (int i = 0; i < 3; i++ ) {
	    //FIXME One of them might be -1 or +1, decreasing the number!!
	    //Check for row sum
	    if (moves[i][0] + moves[i][1] + moves[i][2] == -2) {
		//Comp on verge of victory
		//So go ahead and win!!
		for (int j = 0; j < 3; j++) {
		    if (moves[i][j] == 0) {
			_col = j;
			_row = i;
			i = 3;
			j = 3;
			done = true;
		    } 
		}
	    } //Check for column sum
	    else if (moves[0][i] + moves[1][i] + moves[2][i] == -2) {
		//Comp on verge of victory
		//So go ahead and win!!
		//Else player on verge of victory- Block him
		for (int j = 0; j < 3; j++) {
		    if (moves[j][i] == 0) {
			_row = j;
			_col = i;
			i = 3;
			j = 3;
			done = true;
		    }
		}
	    } //Check for diagonal
	    else if (moves[0][2] + moves[1][1] + moves[2][0] == -2) {
		for (int j = 0; j < 3; j++) {
		    if (moves[j][2-j] == 0) {
			_row = j;
			_col = 2-j;
			i = 3;
			j = 3;
			done = true;
		    }
		}
	    } 
	    else if (moves[0][0] + moves[1][1] + moves[2][2] == -2) {
		for (int j = 0; j < 3; j++) {
		    if (moves[j][j] == 0) {
			_row = j;
			_col = j;
			i = 3;
			j = 3;
			done = true;
		    }
		}
	    }
	}
	if (done) return;
	else {
	    for (int i = 0; i < 2; i++) {
		if (moves[i][0] + moves[i][1] + moves[i][2] == 2) {
		    //Player on verge of victory.
		    //So go ahead and block him!!
		    for (int j = 0; j < 3; j++) {
			if (moves[i][j] == 0) {
			    _col = j;
			    _row = i;
			    i = 3;
			    j = 3;
			    done = true;
			} 
		    }
		} //Check for column sum
		else if (moves[0][i] + moves[1][i] + moves[2][i] == 2) {
		    //Comp on verge of victory
		    //So go ahead and win!!
		    //Else player on verge of victory- Block him
		    for (int j = 0; j < 3; j++) {
			if (moves[j][i] == 0) {
			    _row = j;
			    _col = i;
			    i = 3;
			    j = 3;
			    done = true;
			}
		    }
		}
		else if (moves[0][2] + moves[1][1] + moves[2][0] == 2) {
		    for (int j = 0; j < 3; j++) {
			if (moves[j][2-j] == 0) {
			    _row = j;
			    _col = 2-j;
			    i = 3;
			    j = 3;
			    done = true;
			}
		    }
		} 
		else if (moves[0][0] + moves[1][1] + moves[2][2] == 2) {
		    for (int j = 0; j < 3; j++) {
			if (moves[j][j] == 0) {
			    _row = j;
			    _col = j;
			    i = 3;
			    j = 3;
			    done = true;
			}
		    }
		} 	
	    }
	}
	if (done) return;
	else {
	    //else devise a strategy
	    if (moves[0][0] == 0) {
		_row = 0;
		_col = 0;
	    } else if (moves[0][2] == 0) {
		_row = 0;
		_col = 2;
	    } else if (moves[2][0] == 0) {
		_row = 2;
		_col = 0;
	    } else if (moves[2][2] == 0) {
		_row = 2;
		_col = 2;
	    } else { //Hopefully no more combos
		for (int i = 0; i < 3; i++ ) {
		    for (int j = 0; j < 3; j++) {
			if (moves[i][j] == 0) {
			    _row = i;
			    _col = j;
			}
		    }
		}
	    }   
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  //// 
    int _row = -1;
    int _col = -1;
}

