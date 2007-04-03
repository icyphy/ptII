/* Utilities for MoML files

 Copyright (c) 2007 The Regents of the University of California.
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


import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.UndefinedConstantOrIdentifierException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.UserActorLibrary;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ExpertParameter;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.UndefinedConstantOrIdentifierException;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.undo.RedoChangeRequest;
import ptolemy.kernel.undo.UndoChangeRequest;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.IconLoader;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.MoMLUndoEntry;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// MoMLUtilties

/**
 Utilities for operating on MoML files.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class MoMLUtilities {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check for problems in the moml to be copied.  If there are
     *  missing variables references, search for the variables and
     *  return MoML definitions for any found variables.
     *  @param momlToBeChecked The MoML string to be checked.
     *  @param container The container in which the string is to be checked.
     *  @return MoML to be inserted before the momlToBeChecked
     *  @exception IllegalActionException If there is a problem parsing
     *  the string, or validating a variable.
     */
    public static String checkCopy(String momlToBeChecked,
            NamedObj container) throws IllegalActionException {
        StringWriter variableBuffer = new StringWriter();
        Workspace workspace = new Workspace("copyWorkspace");
        MoMLParser parser = new MoMLParser(workspace);
        TypedCompositeActor parsed = null;
        try {
            parsed = (TypedCompositeActor) parser.parse(
                    "<entity name=\"auto\" class=\"ptolemy.actor.TypedCompositeActor\">" +
                    momlToBeChecked
                    + "</entity>");
        } catch (Exception ex) {
            throw new IllegalActionException(container, ex,
                    "Failed to parse.");
        }
        // FIXME: what about classes?
        Iterator entities = parsed.allAtomicEntityList().iterator();
        while (entities.hasNext()) {
            Entity entity = (Entity) entities.next();
            Enumeration attributes = entity.getAttributes();
            while (attributes.hasMoreElements()) {
                Attribute attribute = (Attribute) attributes.nextElement();
                if (attribute instanceof Variable) {
                    Variable variable = (Variable) attribute;
                    variable.validate();
                    try {
                        variable.getToken();
                    } catch (IllegalActionException ex) {
                        if (ex.getCause() instanceof
                                UndefinedConstantOrIdentifierException) {
                            UndefinedConstantOrIdentifierException cause = (UndefinedConstantOrIdentifierException)ex.getCause();

                            // Find the variable in the object we are
                            // copying

                            // Get the name of the variable without the
                            // .auto.
                            String variableName = variable.getFullName().substring(variable.toplevel().getName().length()+2);
                                    
                            Attribute masterAttribute = container.getAttribute(variableName);

                            if (masterAttribute instanceof Variable) {
                                Variable masterVariable = (Variable)masterAttribute;
                                ParserScope parserScope = masterVariable.getParserScope();
                                if (parserScope instanceof ModelScope) {
                                    if (masterVariable != null) {
                                        Variable node = masterVariable.getVariable(cause.nodeName());
                                        if (node == null) {
                                            ex.printStackTrace();
                                        }

                                        try {
                                            variableBuffer.append(node.exportMoML());
                                        } catch (Throwable ex2) {

                                            // Ignore and hope the
                                            // user pastes into a
                                            // location where the
                                            // variable is defined
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return variableBuffer.toString();
    }
}
