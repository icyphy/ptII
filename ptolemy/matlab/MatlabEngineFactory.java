/* Matlab Engine Interface

 Copyright (c) 1998-2003 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (zkemenczy@rim.net)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.matlab;

//////////////////////////////////////////////////////////////////////////
//// MatlabEngineFactory
/*
Create a matlab engine that is compatible with the available
implementation.  This class allows other classes to interface with
matlab without depending directly on the matlab library, which may or
may not exist, depending on whether or not matlab is installed.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class MatlabEngineFactory {
    public static MatlabEngineInterface createEngine() {
        // FIXME: reflect the correct implementation.
        return new Engine();
    }
}
