/* A class containing all the information passed from a PNRDDecider to
   its parent PNRDDecider and to PNGlobalController.

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

//////////////////////////////////////////////////////////////////////////
//// PNRDInfo
/** 
A class containing all the information passed from a PNRDDecider to
its parent PNRDDecider and to PNGlobalController.

If left and right are both null, tree stops spliting.  In this case, all the
paremeters are computed based on the local filter.

If left or right is not null, this is not a leaf node.  Then rate, distortion
are the combined rate and distortion based on the coding of the subtree.
And quantizerId and codeBookEntry should not be used.

@author Yuhong Xiong
@(#)PNRDInfo.java	1.9 09/13/98
*/

package ptolemy.domains.pn.lib;

public class PNRDInfo {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    
    public int depth = -1;	// depth in the tree
    public int nodeId = -1;	// no position, ranges 0 - (2^depth - 1)
    public int rate = -1;	// best rate for coding the subtree starting
				// from me
    public double distortion = -1;   // distortion under the above rate
    public int quantizerId = -1;     // just for globalController
    public int[] codeBookEntry = null; // just for globalController

    public PNRDInfo left = null;
    public PNRDInfo right = null;
}


