/* Test class for DebugEvent

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.kernel.util.test;

import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// TestDebugEvent

/**
 Class used to test DebugEvent

 @author  Christopher Hylands
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 */
public class TestDebugEvent implements DebugEvent {
    /**
     * Create a new firing event with the given source, actor, and type.
     */
    public TestDebugEvent(NamedObj source) {
        _source = source;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the director that activated this event.
     */
    @Override
    public NamedObj getSource() {
        return _source;
    }

    /**
     * Return the the name of the source
     */
    @Override
    public String toString() {
        return _source.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private NamedObj _source;
}
