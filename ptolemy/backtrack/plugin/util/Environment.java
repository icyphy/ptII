/* 

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.plugin.util;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import ptolemy.backtrack.plugin.EclipsePlugin;
import ptolemy.backtrack.plugin.preferences.PreferenceConstants;

//////////////////////////////////////////////////////////////////////////
//// Environment
/**
 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Environment {

    public static String getPtolemyHome() {
        return getPtolemyHome(null);
    }
    
    public static String getPtolemyHome(Shell shell) {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String PTII = store.getString(PreferenceConstants.PTII);
        boolean valid = true;
        
        if (PTII == null || PTII.equals(""))
            valid = false;
        
        if (!valid && shell != null) {
            MessageDialog.openError(shell,
                    "Ptolemy II Environment Error",
                    "Ptolemy home is invalid.\n" +
                    "Please set it in Ptolemy -> Options.");
            return null;
        } else
            return PTII;
    }
    
}
