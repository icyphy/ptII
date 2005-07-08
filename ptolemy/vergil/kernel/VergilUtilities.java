/* Utilities used by Vergil.

 Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.vergil.kernel;

import javax.swing.UIManager;

import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// VergilUtilities

/**
 Utilities used by Vergil.

 @author Christopher Hylands Brooks
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class VergilUtilities {
    /** Instances of this class cannot be created.
     */
    private VergilUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if we are running under MacOS look and feel or
     *  if the ptolemy.ptII.MacOS property is defined.
     *  To define ptolemy.ptII.MacOS, invoke Vergil with
     *  java -Dptolemy.ptII.MacOS=true -classpath $PTII ptolemy.vergil.VergilApplication
     *  Or, under Cygwin:
     *  <pre>
     *  export JAVAFLAGS=-Dptolemy.ptII.MacOS=true
     *  $PTII/bin/vergil
     *  </pre>
     *  If the ptolemy.ptII.MacOS property is set to true, this method
     *  prints the message "ptolemy.ptII.MacOS = true property detected".
     *
     *  @return True if the look and feel starts with "Mac OS" or the
     *  ptolemy.ptII.MacOS property is set to true.
     */
    public static boolean macOSLookAndFeel() {
        // Dan Higgins writes:
        // "Apple suggests in their tech note 2042 (http://developer.apple.com/
        // documentation/Java/Conceptual/Java131Development/x_platform/
        // chapter_5_section_5.html) that the statement
        //
        // System.getProperty("mrj.version")
        //
        // should be used to detect a mac, with any non-null result
        // indicating that the machine is a mac. This approach is
        // independent of the string value which may change."
        // However, calling getProperty will likely fail in applets
        // or within the sandbox, so we use this method instead.
        try {
            if (StringUtilities.getProperty("ptolemy.ptII.MacOS")
                    .equals("true")) {
                System.out.println("ptolemy.ptII.MacOS = "
                        + "true property detected");
                return true;
            }
        } catch (SecurityException ex) {
            if (!_printedSecurityExceptionMessage) {
                _printedSecurityExceptionMessage = true;
                System.out.println("Warning: Failed to get the "
                        + "ptolemy.ptII.MacOS property "
                        + "(-sandbox always causes this)");
            }
        }

        return UIManager.getLookAndFeel().getName().startsWith("Mac OS");
    }

    // True if we have printed the securityExceptionMessage;
    private static boolean _printedSecurityExceptionMessage = false;
}
