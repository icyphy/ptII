/* Top-level window for Ptolemy models with a menubar and status bar.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.InternalErrorException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// PtolemyFrame
/**
This is a top-level window for Ptolemy models with a menubar and status bar.
Derived classes should add components to the content pane using a
line like:
<pre>
    getContentPane().add(component, BorderLayout.CENTER);
</pre>

@author Edward A. Lee
@version $Id$
*/
public abstract class PtolemyFrame extends TableauFrame {

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @param model The model to put in this frame, or null if none.
     */
    public PtolemyFrame(CompositeEntity model) {
        this(model, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param model The model to put in this frame, or null if none.
     *  @param tableau The tableau responsible for this frame, or null if none.
     */
    public PtolemyFrame(CompositeEntity model, Tableau tableau) {
        super(tableau);
        setModel(model);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the associated model.
     *  @return The associated model.
     */
    public CompositeEntity getModel() {
        return _model;
    }

    /** Set the associated model.
     *  @param model The associated model.
     */
    public void setModel(CompositeEntity model) {
        _model = model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        try {
            URL doc = getClass().getClassLoader().getResource(
                    "ptolemy/configs/doc/basicHelp.htm");
            getConfiguration().openModel(null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            _about();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The model that this window controls, if any.
    private CompositeEntity _model;
}
