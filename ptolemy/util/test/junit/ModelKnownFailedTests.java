/* Base class for tests that use models in the auto/knownFailedTests directory.

   Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.util.test.junit;

import java.io.IOException;

///////////////////////////////////////////////////////////////////
//// ModelKnownFailedTests
/**
 * Base class for tests that use models in the auto/knownFailedTests directory.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ModelKnownFailedTests extends ModelTests {

    /**
     * Return a two dimensional array of arrays of strings that name
     * the model to be executed. If auto/knownFailedTests/ does not
     * exist, or does not contain files that end with .xml or .moml,
     * return a list with one element that contains a special string.
     *
     * @return The List of model names in auto/
     * @exception IOException If there is a problem accessing the auto/ directory.
     */
    @Override
    public Object[] modelValues() throws IOException {
        return modelValues("auto/knownFailedTests/",
                THERE_ARE_NO_KNOWN_FAILED_TESTS);
    }
}
