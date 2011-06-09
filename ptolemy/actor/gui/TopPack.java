/* Top-level window for Ptolemy models with a menubar and status bar.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.actor.gui;

import ptolemy.gui.Top;

//////////////////////////////////////////////////////////////////////////
//// TopPack

/**
 An interface that allows alternate pack() methods to be called from
 TableauFrame which allows for functionality such as alternate menu systems
 in Vergil.

 @author Chad Berkley
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.AcceptedRating Red (berkley)
 @Pt.ProposedRating Red (berkley)
 */
public interface TopPack {

    /**
     * Pack the Top JFrame.
     * This method overrides the regular pack() call.
     * @param top The Top to pack.
     * @param alreadyCalled  True if pack has already been called.
     */
    public void pack(Top top, boolean alreadyCalled);

    /**
     * Get an object.
     * This method allows the overrider to pass an object back to the calling object.
     * @param identifier The object to get.
     * @return The object found.
     */
    public Object getObject(Object identifier);
}
