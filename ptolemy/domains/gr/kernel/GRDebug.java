/* GR domain class for internal debugging

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.kernel;

import javax.swing.JOptionPane;

/**
@author C. Fong
@version $Id$
@since Ptolemy II 1.0
*/
public class GRDebug {

    public GRDebug(boolean debugOn) {
        _debugOn = debugOn;
    }

    public static final void print(Object object) {
        if (_debugOn) {
            System.out.print(object.toString());
        }
    }

    public static final void printStackTrace() {
        if (_debugOn) {
            try {
                throw new Exception("printStackTrace()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static final void println(Object object) {
        if (_debugOn) {
            System.out.println(object.toString());
        }
    }

    public static final void prompt(String string) {
        if (_debugOn) {
            JOptionPane.showMessageDialog(
                    null, string,
                    "MessageDialog", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static boolean _debugOn = false;
}
