/* Discrete Time (DT) domain class for internal debugging

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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.dt.kernel;

import java.util.*;
import javax.swing.*;

public class DTDebug {

    public DTDebug(boolean debugOn) {
        _debugOn = debugOn;
    }
    
    
    public void println(Object obj) {
        if (_debugOn) {
            System.out.println(obj.toString());
        }
    }
    public void print(Object obj) {
        if (_debugOn) {
            System.out.print(obj.toString());
        }
    }
    
    public void prompt(String str) {
        if (_debugOn) {
            JOptionPane.showMessageDialog(null,str,"MessageDialog",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void printStackTrace() {
        if (_debugOn) {
            try {
		        throw new Exception("printStackTrace()");
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
    }
 
    private boolean _debugOn = false;
}
