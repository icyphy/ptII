/* A JavaSpace client that reads stock prices and print out on screen.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jspaces.test;

import ptolemy.data.DoubleToken;
import ptolemy.actor.lib.jspaces.TokenEntry;
import ptolemy.actor.lib.jspaces.IndexEntry;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;


import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

//////////////////////////////////////////////////////////////////////////
//// TestReader
/**
Clean all IndexEntry and TokenEntry in the java space.

@author Jie Liu
@version $Id$
*/

public class SpaceCleaner {
    public static void main(String[] args) {
        try {
            TokenEntry allToken = new TokenEntry();
            
            JavaSpace space = SpaceFinder.getSpace();
            
            IndexEntry allIndex = new IndexEntry();

	    while(true) {
                IndexEntry index = 
                    (IndexEntry)space.takeIfExists(
                            allIndex, null, Long.MAX_VALUE);
                if(index == null) {
                    break;
                } else {
                    System.out.println("Taken from JavaSpaces IndexEntry: " +
                            index.name + " " + index.type +" "+ 
                            index.getPosition());
                }
            }
             while(true) {
                TokenEntry token = 
                    (TokenEntry)space.takeIfExists(
                            allToken, null, Long.MAX_VALUE);
                if(token == null) {
                    break;
                } else {
                    System.out.println("Taken from JavaSpaces TokenEntry: " +
                            token.name + " " + token.serialNumber);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

