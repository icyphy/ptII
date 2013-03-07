/* Load Icons

 Copyright (c) 2006-2013 The Regents of the University of California.
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
package ptolemy.moml;

import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// IconLoader

/**
 Interface for loading icons.

 @see MoMLParser
 @author Edward A. Lee, Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public interface IconLoader {

    // This interface is used by Kepler so that the createHierarchy
    // command brings up the proper icon.
    // See kepler/gui/src/org/kepler/gui/KeplerGraphFrame.java

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Load an icon for a class in a particular context.
     *  @param className The name of the class for which the icon is
     *  to be loaded.
     *  @param context The context in which the icon is loaded.
     *  @return true if the icon was successfully loaded.
     *  @exception Exception If there is a problem adding
     *  the icon.
     *  @see ptolemy.moml.MoMLParser#getIconLoader()
     *  @see ptolemy.moml.MoMLParser#setIconLoader(IconLoader)
     */
    public boolean loadIconForClass(String className, NamedObj context)
            throws Exception;
}
