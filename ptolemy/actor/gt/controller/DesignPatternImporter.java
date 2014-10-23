/* An attribute that specifies the location of a design pattern and populates
   the container with that design pattern automatically.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import java.io.Reader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import ptolemy.actor.DesignPatternGetMoMLAction;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.gt.GTAttribute;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.MoMLFilterSimple;

///////////////////////////////////////////////////////////////////
//// DesignPatternImporter

/**
 An attribute that specifies the location of a design pattern and
 populates the container with that design pattern automatically. This
 attribute has the same effect of importing a design pattern into the
 current model with the menu item in the File menu of the Ptolemy
 environment, but it automatically imports the specified design
 pattern and does not require the model user to manually import it.


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DesignPatternImporter extends Attribute implements GTAttribute,
ValueListener {

    /** Construct an attribute with the given name contained by the
     *  specified entity. The container argument must not be null, or
     *  a NullPointerException will be thrown.  This attribute will
     *  use the workspace of the container for synchronization and
     *  version counts.  If the name argument is null, then the name
     *  is set to the empty string.  Increment the version of the
     *  workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of
     *   an acceptable class for the container, or if the name
     *   contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DesignPatternImporter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        designPatternFile = new FileParameter(this, "designPatternFile");
        designPatternFile.addValueListener(this);
    }

    /** Update the design pattern.
     *
     *  @param settable The attribute changed.
     */
    public void attributeChanged(Settable settable) {
        update();
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DesignPatternImporter newObject = (DesignPatternImporter) super
                .clone(workspace);
        newObject._lastValues = new HashMap<String, Token>();
        return newObject;
    }

    /** Set the container of this importer, and update the new
     *  container if it is not null.
     *
     *  @param container The new container.
     *  @exception IllegalActionException If thrown by the superclass.
     *  @exception NameDuplicationException If thrown by the superclass.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        NamedObj oldContainer = getContainer();
        try {
            if (oldContainer != null && _lastUndoStack != null) {
                _lastUndoStack.undo();
            }
        } catch (Exception e) {
            throw new InternalErrorException(this, e,
                    "Unable to undo previous updates.");
        } finally {
            _lastUndoStack = null;
        }
        super.setContainer(container);
        if (container != null) {
            update();
        }
    }

    /** Update the container of this importer with the design
     *  pattern. If a design pattern is previously added to the
     *  container, the importer first tries to undo the importation
     *  before importing the new design pattern.
     */
    public void update() {
        List<Parameter> parameters = attributeList(Parameter.class);
        HashMap<String, Token> table = new HashMap<String, Token>();
        String value = null;
        DesignPatternGetMoMLAction action = new DesignPatternGetMoMLAction();
        for (Parameter parameter : parameters) {
            try {
                Token token = parameter.getToken();
                table.put(parameter.getName(), token);
                if (parameter == designPatternFile) {
                    value = ((StringToken) token).stringValue();
                } else {
                    action.overrideParameter(parameter.getName(),
                            parameter.getExpression());
                }
            } catch (IllegalActionException e) {
                throw new InternalErrorException(this, e, "Unable to obtain "
                        + "value for parameter " + parameter.getName());
            }
        }
        if (table.equals(_lastValues)) {
            return;
        } else {
            _lastValues = table;
        }

        if (_lastUndoStack != null) {
            try {
                _lastUndoStack.undo();
            } catch (Exception e) {
                throw new InternalErrorException(this, e,
                        "Unable to undo previous updates.");
            } finally {
                _lastUndoStack = null;
            }
        }

        if (value == null || value.equals("")) {
            return;
        }

        final MoMLParser parser = new MoMLParser();
        NamedObj model;
        try {
            Reader reader = designPatternFile.openForReading();
            URI baseDirectory = designPatternFile.getBaseDirectory();
            model = parser.parse(
                    baseDirectory == null ? null : baseDirectory.toURL(),
                            value, reader);
        } catch (Exception e) {
            throw new InternalErrorException(this, e, "Unable to read design "
                    + "pattern from file \"" + value + "\".");
        }

        final String moml = action.getMoml(model, null);

        parser.reset();

        final NamedObj container = getContainer();
        final UndoStackAttribute undoStack;
        try {
            undoStack = new UndoStackAttribute(container,
                    container.uniqueName("_undoStack"));
            undoStack.moveToFirst();
        } catch (KernelException e) {
            // This should not happen.
            throw new InternalErrorException(this, e, "Unable to create "
                    + "empty undo stack.");
        }

        boolean isModified = MoMLParser.isModified();
        final MoMLContentFilter filter = new MoMLContentFilter();
        parser.setContext(container);
        parser.setUndoable(true);
        try {
            MoMLParser.addMoMLFilter(filter);
            parser.parse(moml);
        } catch (Exception e) {
            throw new InternalErrorException(this, e, "Unable to apply the "
                    + "design pattern in file \"" + value + "\".");
        } finally {
            MoMLParser.getMoMLFilters().remove(filter);
            MoMLParser.setModified(isModified);

            _lastUndoStack = undoStack;
            try {
                undoStack.setContainer(null);
            } catch (KernelException e) {
                // Can't happen.
            }

            Attribute after = container.getAttribute("After");
            if (after instanceof TransformationAttribute) {
                final TransformationAttribute attribute = (TransformationAttribute) after;
                attribute.addExecutionListener(new ExecutionListener() {
                    @Override
                    public void executionError(Manager manager,
                            Throwable throwable) {
                    }

                    @Override
                    public void executionFinished(Manager manager) {
                    }

                    @Override
                    public void managerStateChanged(Manager manager) {
                        if (manager.getState() == Manager.PREINITIALIZING) {
                            MoMLParser.addMoMLFilter(filter);
                            _isModified = MoMLParser.isModified();
                            try {
                                _lastUndoStack.setContainer(container);
                                _lastUndoStack.moveToFirst();
                            } catch (KernelException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else if (manager.getState() == Manager.IDLE) {
                            MoMLParser.getMoMLFilters().remove(filter);
                            MoMLParser.setModified(_isModified);

                            _lastUndoStack = undoStack;
                            try {
                                undoStack.setContainer(null);
                            } catch (KernelException e) {
                                // Can't happen.
                            }
                            attribute.removeExecutionListener(this);
                        }
                    }

                    private boolean _isModified;
                });
            }
        }
    }

    /** React to change of an attribute and update the container of this
     *  importer.
     *
     *  @param settable The attribute changed.
     */
    @Override
    public void valueChanged(Settable settable) {
        update();
    }

    /** The design pattern file.
     */
    public FileParameter designPatternFile;

    /** The last undo stack.
     */
    private UndoStackAttribute _lastUndoStack;

    /** The last values of the parameters to this importer, used to
     *  test whether attributes are changed and whether the container
     *  needs to be updated.
     */
    private HashMap<String, Token> _lastValues = new HashMap<String, Token>();

    ///////////////////////////////////////////////////////////////////
    //// MoMLContentFilter

    /**
     A moml filter that sets each created object to be non-persistent.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class MoMLContentFilter extends MoMLFilterSimple {

        /** Return the value of the attribute.
         *
         *  @param container  The container for XML element.
         *  @param element The XML element name.
         *  @param attributeName The name of the attribute.
         *  @param attributeValue The value of the attribute.
         *  @param xmlFile The file currently being parsed.
         *  @return The value of the attribute.
         */
        @Override
        public String filterAttributeValue(NamedObj container, String element,
                String attributeName, String attributeValue, String xmlFile) {
            return attributeValue;
        }

        /** Set the created element to be non-persistent.
         *
         *  @param container The object defined by the element that this
         *   is the end of.
         *  @param elementName The element name.
         *  @param currentCharData The character data, which appears
         *   only in the doc and configure elements
         *  @param xmlFile The file currently being parsed.
         *  @exception Exception Not thrown in this class.
         */
        @Override
        public void filterEndElement(NamedObj container, String elementName,
                StringBuffer currentCharData, String xmlFile) throws Exception {
            if (container != null && !"group".equals(elementName)) {
                NamedObj context = getContainer();
                NamedObj parent = container;
                while (parent != null && parent != context) {
                    parent = parent.getContainer();
                }
                if (parent == context && container != context) {
                    // tfeng: This does not work with actor classes in
                    //   the pattern, because instances are not
                    //   persistent either, and will be lost after
                    //   saving.  container.setDerivedLevel(1);
                    if (container.attributeList(PersistenceAttribute.class)
                            .isEmpty()) {
                        new PersistenceAttribute(container,
                                container.uniqueName("persistenceAttribute"));
                    }
                }
            }
        }
    }
}
