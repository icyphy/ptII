/* Check that all the variables are defined in a piece of MoML.

Copyright (c) 2007-2014 The Regents of the University of California.
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

import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.UndefinedConstantOrIdentifierException;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MoMLUtilties

/**
   Check that all the variables are defined in a piece of MoML.

   @author Christopher Brooks
   @version $Id$
   @since Ptolemy II 6.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public class MoMLVariableChecker {

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
    public String checkCopy(String momlToBeChecked, NamedObj container)
            throws IllegalActionException {

        return checkCopy(momlToBeChecked, container, false);
    }

    /** Check for problems in the moml to be copied.  If there are
     *  missing variable references, search for the variables and
     *  return MoML definitions for any found variables.
     *  @param momlToBeChecked The MoML string to be checked.
     *  @param container The container in which the string is to be checked.
     *  @param hideVariables If true, add MoML that will make all the found
     *   variables hidden from the user interface when they are copied.
     *  @return MoML to be inserted before the momlToBeChecked
     *  @exception IllegalActionException If there is a problem parsing
     *  the string, or validating a variable.
     */
    public String checkCopy(String momlToBeChecked, NamedObj container,
            boolean hideVariables) throws IllegalActionException {

        _variableBuffer = new StringWriter();
        Workspace workspace = new Workspace("copyWorkspace");
        MoMLParser parser = new MoMLParser(workspace);
        TypedCompositeActor parsedContainer = null;

        // Attempt to parse the moml.  If we fail, check the exception
        // for a missing variable.  If a missing variable is found
        // add it to the moml and reparse.
        boolean doParse = true;
        while (doParse) {
            ErrorHandler handler = MoMLParser.getErrorHandler();
            MoMLParser.setErrorHandler(null);
            try {
                // Parse the momlToBeChecked.
                parsedContainer = (TypedCompositeActor) parser
                        .parse("<entity name=\"auto\" class=\"ptolemy.actor.TypedCompositeActor\">"
                                + _variableBuffer.toString()
                                + momlToBeChecked
                                + "</entity>");
                doParse = false;
            } catch (MissingClassException ex1) {
                try {
                    doParse = _findMissingClass(ex1, container, parsedContainer);
                } catch (Exception ex1a) {
                    return _variableBuffer.toString();
                }
            } catch (IllegalActionException ex2) {
                try {
                    doParse = _findUndefinedConstantsOrIdentifiers(ex2,
                            container, parsedContainer, hideVariables);

                } catch (Throwable throwable) {
                    return _variableBuffer.toString();
                }
            } catch (Exception ex3) {
                throw new IllegalActionException(container, ex3,
                        "Failed to parse contents of copy buffer.");
            } finally {
                MoMLParser.setErrorHandler(handler);
            }
        }

        // Iterate through all the entities and attributes, find the attributes
        // that are variables, validate the variables and look for
        // errors.
        if (parsedContainer != null) {
            // parsedContainer might be null if we failed to parse because
            // of a missing class
            Iterator entities = parsedContainer.deepEntityList().iterator();
            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();
                List<Attribute> entityAttributes = new LinkedList<Attribute>(
                        entity.attributeList());
                for (Attribute attribute : entityAttributes) {
                    _recursiveFindUndefinedConstantsOrIdentifiesInAttribute(
                            attribute, container, parsedContainer,
                            hideVariables);
                }
            }

            List<Attribute> allAttributes = new LinkedList<Attribute>(
                    parsedContainer.attributeList());
            for (Attribute attribute : allAttributes) {
                _recursiveFindUndefinedConstantsOrIdentifiesInAttribute(
                        attribute, container, parsedContainer, hideVariables);
            }
        }

        return _variableBuffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Recursively search through an attribute and its contained attributes to
     *  find any unresolved references to other attributes.
     *  @param attribute The attribute to be traversed.
     *  @param container The original container of the attribute.
     *  @param parsedContainer The temporary container from which the new copied
     *   unresolved attributes will be generated.
     *  @param hideVariables If true, add MoML that will make all the found
     *   variables hidden from the user interface when they are copied.
     *  @exception IllegalActionException If there is a problem parsing
     *   an attribute, or validating a variable.
     */
    private void _recursiveFindUndefinedConstantsOrIdentifiesInAttribute(
            Attribute attribute, NamedObj container,
            TypedCompositeActor parsedContainer, boolean hideVariables)
                    throws IllegalActionException {

        if (attribute instanceof Variable) {
            Variable variable = (Variable) attribute;

            boolean doGetToken = true;
            while (doGetToken) {
                doGetToken = false;
                try {
                    variable.getToken();
                } catch (IllegalActionException ex) {
                    doGetToken = _findUndefinedConstantsOrIdentifiers(ex,
                            container, parsedContainer, hideVariables);
                }
            }
            ;
        }

        // Parse all the StringAttributes and Parameters and
        // look for missing things.  Note that Expression.expression
        // is a StringAttribute, so we pick that up here.
        if (attribute instanceof AbstractSettableAttribute) {
            AbstractSettableAttribute settable = (AbstractSettableAttribute) attribute;
            PtParser ptParser = new PtParser();
            ASTPtRootNode parseTree = null;
            try {
                parseTree = ptParser
                        .generateParseTree(settable.getExpression());
            } catch (Throwable throwable) {
                // Skip things we can't parse, like StringAttributes
                // that are docs.

                // FIXME: we could be smarter here and look
                // for Expression.expression and only parse
                // it.  However, this would mean that this
                // class then dependend on
                // actor.lib.Expression.  A better design
                // would be to have a marker interface
                // implemented by Expression.expression and
                // search for that interface.

            }

            if (parseTree != null) {
                ParseTreeFreeVariableCollector variableCollector = new ParseTreeFreeVariableCollector();
                Set set = variableCollector.collectFreeVariables(parseTree,
                        /*scope*/null);
                for (Iterator elements = set.iterator(); elements.hasNext();) {
                    String name = (String) elements.next();

                    // Look for the variable in parsedContainer
                    if (parsedContainer.getAttribute(name) == null) {
                        _findUndefinedConstantsOrIdentifiers(name, name,
                                container, parsedContainer, hideVariables);
                    }
                }
            }
        }

        List<Attribute> containedAttributes = attribute.attributeList();
        for (Attribute containedAttribute : containedAttributes) {
            _recursiveFindUndefinedConstantsOrIdentifiesInAttribute(
                    containedAttribute, container, parsedContainer,
                    hideVariables);
        }
    }

    /** Given a MissingClassException, find missing classes.
     *  @param exception The MissingClassException that contains the class
     *   that needs to be found.
     *  @param container The original container of the elements being copied.
     *  @param parsedContainer The temporary container from which the new copied
     *   unresolved attributes will be generated.
     *  @return true if the outer parse should be rerun, false otherwise.
     */
    private boolean _findMissingClass(MissingClassException exception,
            NamedObj container, TypedCompositeActor parsedContainer) {

        // True if we should rerun the outer parse
        boolean doRerun = false;

        if (container instanceof CompositeEntity) {
            Iterator containedClasses = ((CompositeEntity) container)
                    .classDefinitionList().iterator();

            while (containedClasses.hasNext()) {
                NamedObj containedObject = (NamedObj) containedClasses.next();
                String missingClassName = exception.missingClassName();
                if (missingClassName.equals(containedObject.getName())
                        || missingClassName.startsWith(".")
                        && missingClassName.substring(1).equals(
                                containedObject.getName())) {
                    try {
                        String moml = containedObject.exportMoML()
                                .replaceFirst("<class",
                                        "<class createIfNecessary=\"true\"");

                        MoMLChangeRequest change = new MoMLChangeRequest(
                                parsedContainer, parsedContainer, moml);

                        if (parsedContainer != null) {
                            // If we are parsing the moml for the first
                            // time, then the parsedContainer might be null.
                            parsedContainer.requestChange(change);
                        }
                        _variableBuffer.append(moml);
                        // Rerun the parse in case there are other problems.
                        doRerun = true;
                    } catch (Throwable ex2) {
                        // Ignore and hope the user pastes into a
                        // location where the variable is defined
                    }
                }
            }
        }
        return doRerun;
    }

    /** Given an UndefinedConstantOrIdentifierException, find
     *  missing variables.
     *  @param exception The UndefinedConstantOrIdentifierException that
     *   contains the identifier that needs to be found.
     *  @param container The original container of the elements being copied.
     *  @param parsedContainer The temporary container from which the new copied
     *   unresolved attributes will be generated.
     *  @param hideVariables If true, add MoML that will make all the found
     *   variables hidden from the user interface when they are copied.
     *  @return true if the outer parse should be rerun, false otherwise.
     *  @exception IllegalActionException If there is a problem finding
     *   the undefined constants or identifiers.
     */
    private boolean _findUndefinedConstantsOrIdentifiers(
            IllegalActionException exception, NamedObj container,
            TypedCompositeActor parsedContainer, boolean hideVariables)
                    throws IllegalActionException {
        // True if we should rerun the outer parse or getToken

        // Ok, we have a variable that might have an appropriate
        // undefined constant or identifier.

        // If the current exception is appropriate, or its cause is
        // appropriate, then we look for the missing variable.  If the
        // exception or its cause does not have the node name, then we
        // can't do anything.

        UndefinedConstantOrIdentifierException idException = null;
        if (exception instanceof UndefinedConstantOrIdentifierException) {
            idException = (UndefinedConstantOrIdentifierException) exception;
        } else {
            Throwable cause = exception.getCause();
            while (cause instanceof IllegalActionException) {
                if (cause instanceof UndefinedConstantOrIdentifierException) {
                    idException = (UndefinedConstantOrIdentifierException) cause;
                    break;
                }
                cause = ((IllegalActionException) cause).getCause();
            }
        }

        if (idException == null) {
            // The exception or the cause was not an
            // UndefinedConstantOrIdentifierException, so we cannot do
            // anything.
            return false;
        }

        // We have an exception that has the name of the missing
        // variable.

        // Find the variable in the object we are copying.

        // Get the name of the variable without the .auto.
        String variableName = exception
                .getNameable1()
                .getFullName()
                .substring(
                        ((NamedObj) exception.getNameable1()).toplevel()
                        .getName().length() + 2);

        return _findUndefinedConstantsOrIdentifiers(variableName,
                idException.nodeName(), container, parsedContainer,
                hideVariables);
    }

    /** Find the missing variables referred to by the given variable name and
     *  add MoML code to generate them in the _variableBuffer.
     *  @param variableName The name of the variable which is the root from
     *   which any missing references should be found.
     *  @param nodeName The name of the missing constant or identifier.
     *  @param container The original container of the elements being copied.
     *  @param parsedContainer The temporary container from which the new copied
     *   unresolved attributes will be generated.
     *  @param hideVariables If true, add MoML that will make all the found
     *   variables hidden from the user interface when they are copied.
     *  @return true if the outer parse should be rerun, false otherwise.
     *  @exception IllegalActionException If there is a problem finding
     *   the undefined constants or identifiers or generating the MoML code for
     *   the _variableBuffer.
     */
    private boolean _findUndefinedConstantsOrIdentifiers(String variableName,
            String nodeName, NamedObj container,
            TypedCompositeActor parsedContainer, boolean hideVariables)
                    throws IllegalActionException {
        boolean doRerun = false;

        Attribute masterAttribute = container.getAttribute(variableName);
        if (masterAttribute == null) {
            // Needed to find Parameters that are up scope
            NamedObj searchContainer = container;
            while (searchContainer != null && masterAttribute == null) {
                masterAttribute = searchContainer.getAttribute(variableName);
                searchContainer = searchContainer.getContainer();
            }
        }
        if (masterAttribute instanceof Variable) {
            Variable masterVariable = (Variable) masterAttribute;
            ParserScope parserScope = masterVariable.getParserScope();
            if (parserScope instanceof ModelScope) {
                Variable node = masterVariable.getVariable(nodeName);

                if (node == null) {
                    // Needed when we are copying a composite that contains
                    // an Expression that refers to an upscope Parameter.
                    node = masterVariable;
                }

                if (node == _previousNode) {
                    // We've already seen this node, so stop
                    // looping through the getToken() loop.
                    return false;
                }
                _previousNode = node;

                try {
                    String moml = node.exportMoML().replaceFirst("<property",
                            "<property createIfNecessary=\"true\"");

                    if (hideVariables) {
                        moml = _insertHiddenMoMLTagIntoProperty(moml);
                    }

                    // Insert the new variable so that other
                    // variables may use it.

                    MoMLChangeRequest change = new MoMLChangeRequest(
                            parsedContainer, parsedContainer, moml);

                    if (parsedContainer != null) {
                        // If we are parsing the moml for the first
                        // time, then the parsedContainer might be null.
                        parsedContainer.requestChange(change);
                    }
                    _variableBuffer.append(moml);

                    // Rerun the getToken() call in case there are
                    // other problem variables.
                    doRerun = true;
                } catch (Throwable ex2) {
                    // Ignore and hope the user pastes into a
                    // location where the variable is defined
                }
            }
        }
        return doRerun;
    }

    /** Given a MoML string that represents an attribute, insert a MoML tag
     *  that will hide this attribute from the user interface.
     *  @param moml The original MoML string that should represent a Ptolemy
     *   attribute with the &lt;property&gt;&lt;/property&gt; tag.
     *  @return A new MoML string with the hidden attribute tag inserted into
     *   the original string.
     */
    private String _insertHiddenMoMLTagIntoProperty(String moml) {
        String hiddenMoML = "<property name=\"style\" class=\"ptolemy.actor.gui.style.HiddenStyle\"></property>";
        return moml.replaceFirst("</property>", hiddenMoML + "</property>");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The previous node for which we searched.  We keep track of
     *  this to avoid infinite loops.
     */
    private Variable _previousNode;

    /** The moml of any missing variables we have found thus far. */
    private StringWriter _variableBuffer;
}
