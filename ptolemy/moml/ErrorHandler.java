/* Handle a MoML Parsing Error

 Copyright (c) 2000-2001 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.moml;


//////////////////////////////////////////////////////////////////////////
//// ErrorHandler
/**
@author Christopher Hylands, Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public interface ErrorHandler {

    /** Handle an error that occurred while parsing the specified
     *  text. To prevent the parser from continuing to parse the
     *  MoML, implementations of this method should throw an exception.
     *  To prevent the parser from continuing, and also prevent if from
     *  reporting the error (say, if the error has already been reported),
     *  then an implementation of this method should throw CancelException.
     *  @param text The text that caused the error.
     *  @param exception The exception that was thrown.
     *  @param parser The MoMLParser where the error occurred
     */
    public void handleError(String text, Exception exception,
            MoMLParser parser) throws Exception;
}

