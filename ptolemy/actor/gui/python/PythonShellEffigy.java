/* A representative of an Python shell.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.gui.python;

import java.net.URL;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// PythonShellEffigy

/**
 A representative of an Python expression shell.

 @author Edward A. Lee, Christopher Hylands
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (janneck)
 */
public class PythonShellEffigy extends Effigy {
    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public PythonShellEffigy(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Create a new effigy in the given container with the given name.
     *  @param container The container that contains this effigy.
     *  @param name The name of this effigy.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public PythonShellEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. This calls the
     *  base class and then clones the associated model.
     *  @param workspace The workspace for the new effigy.
     *  @return A new effigy.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PythonShellEffigy newObject = (PythonShellEffigy) super
                .clone(workspace);

        if (_model != null) {
            newObject._model = (NamedObj) _model.clone(new Workspace());
        }

        return newObject;
    }

    /** Return the model used to store variables.
     *  @return A model.
     */
    public NamedObj getModel() {
        return _model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Initialization.
    private void _init() {
        _model = new NamedObj();

        try {
            _model.setName("Python");
            identifier.setExpression("Python Evaluator");
        } catch (KernelException ex) {
            throw new InternalErrorException(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A model used to store variables. */
    private NamedObj _model;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory for creating new Ptolemy effigies.
     */
    public static class ShellFactory extends PtolemyEffigy.Factory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this entity.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public ShellFactory(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return true, indicating that this effigy factory is
         *  capable of creating an effigy without a URL being specified.
         *  @return True.
         */
        @Override
        public boolean canCreateBlankEffigy() {
            return true;
        }

        /** If the <i>input</i> URL is null, then
         *  create a blank effigy; otherwise, return null.
         *  This effigy is not capable of reading a file.
         *  The blank effigy will have a new model associated with it.
         *  @param container The container for the effigy.
         *  @param base The base for relative file references, or null if
         *   there are no relative file references.
         *  @param input The input URL.
         *  @return A new instance of PythonShellEffigy, or null if the URL
         *   is not null.
         *  @exception Exception If there is some failure.
         *   is malformed in some way.
         */
        @Override
        public Effigy createEffigy(CompositeEntity container, URL base,
                URL input) throws Exception {
            return new PythonShellEffigy(container,
                    container.uniqueName("effigy"));
        }
    }
}
