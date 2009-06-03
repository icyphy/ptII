/*

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
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// DesignPatternImporter

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DesignPatternImporter extends Attribute
        implements GTAttribute, ValueListener {

    public DesignPatternImporter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        designPatternFile = new FileParameter(this, "designPatternFile");
        designPatternFile.addValueListener(this);
    }

    public void attributeChanged(Settable settable) {
        update();
    }

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
                throw new InternalErrorException(this, e, "Unable to obtain " +
                        "value for parameter " + parameter.getName());
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
            model = parser.parse(baseDirectory == null ? null :
                baseDirectory.toURL(), value, reader);
        } catch (Exception e) {
            throw new InternalErrorException(this, e, "Unable to read design " +
                    "pattern from file \"" + value + "\".");
        }

        final String moml = action.getMoml(model, null);

        parser.reset();

        final NamedObj container = getContainer();
        final UndoStackAttribute undoStack;
        try {
            undoStack = new UndoStackAttribute(container, container.uniqueName(
                    "_undoStack"));
            undoStack.moveToFirst();
        } catch (KernelException e) {
            // This should not happen.
            throw new InternalErrorException(this, e, "Unable to create " +
                    "empty undo stack.");
        }

        boolean isModified = MoMLParser.isModified();
        final MoMLContentFilter filter = new MoMLContentFilter();
        parser.setContext(container);
        parser.setUndoable(true);
        try {
            MoMLParser.addMoMLFilter(filter);
            parser.parse(moml);
        } catch (Exception e) {
            throw new InternalErrorException(this, e, "Unable to apply the " +
                    "design pattern in file \"" + value + "\".");
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
                final TransformationAttribute attribute =
                    (TransformationAttribute) after;
                attribute.addExecutionListener(new ExecutionListener() {
                    public void executionError(Manager manager,
                            Throwable throwable) {
                    }
                    public void executionFinished(Manager manager) {
                    }
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

    public void valueChanged(Settable settable) {
        update();
    }

    public FileParameter designPatternFile;

    private UndoStackAttribute _lastUndoStack;

    private HashMap<String, Token> _lastValues = new HashMap<String, Token>();

    private class MoMLContentFilter implements MoMLFilter {

        public String filterAttributeValue(NamedObj container, String element,
                String attributeName, String attributeValue, String xmlFile) {
            return attributeValue;
        }

        public void filterEndElement(NamedObj container, String elementName,
                StringBuffer currentCharData, String xmlFile) throws Exception {
            if (container != null && !"group".equals(elementName)) {
                NamedObj context = getContainer();
                NamedObj parent = container;
                while (parent != null && parent != context) {
                    parent = parent.getContainer();
                }
                if (parent == context && container != context) {
                    container.setPersistent(false);
                }
            }
        }
    }
}
