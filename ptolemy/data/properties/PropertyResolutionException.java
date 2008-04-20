/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.data.properties;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

public class PropertyResolutionException extends IllegalActionException {

    public PropertyResolutionException(String message) {
        super(null, null, null, message);
    }

    public PropertyResolutionException(String message, Throwable cause) {
        super(null, null, cause, message);
    }

    public PropertyResolutionException(Throwable cause) {
        super(null, null, cause, null);
    }

    public PropertyResolutionException(Nameable object1, String detail) {
        super(object1, null, null, detail);
    }

    public PropertyResolutionException(Nameable object1, Throwable cause) {
        super(object1, null, cause, "");
    }

    public PropertyResolutionException(Nameable object1, Throwable cause,
            String detail) {
        super(object1, null, cause, detail);
    }
}
