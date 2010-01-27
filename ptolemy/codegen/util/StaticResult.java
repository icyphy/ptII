/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.util;

/**
   A dynamic result.
   Used the PartialResult interface is used in DE code generation.

   @author Man-Kit Leung
   @version $Id$
   @since Ptolemy II 8.0
   @Pt.ProposedRating Red (mankit)
   @Pt.AcceptedRating Red (mankit)
 */
public class StaticResult implements PartialResult {

    /** Create a dynamic result object.
     *  @param result The result to be stored.
     */
    public StaticResult(Object result) {
        _result = result;
    }

    /** Return the result.
     *  @return the result.
     */
    public Object getResult() {
        return _result;
    }

    /** Return true if static.
     *  In this class, false is always returned because this
     *  result is dynamic, not static.
     *  @return Always return false, indicating that this 
     *  result is dynamic.
     */
    public boolean isStatic() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The result that is stored. */
    private Object _result;
}
