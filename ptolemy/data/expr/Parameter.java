/* Parameter is a subclass of Variable with support for strings.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Parameter
/**
Parameter extends Variable with support for string-valued variables that
makes these friendlier at the user interface level. In particular,
after calling setStringMode(true), then when setting the value of
this parameter, the string that you pass to setExpression(String)
is taken to be literally the value of the instance of StringToken
that represents the value of this parameter. It is not necessary
to enclose it in quotation marks (and indeed, if you do, the quotation
marks will become part of the value of the string).  In addition,
the type of this parameter will be set to string.
<p>
In string mode, the value passed to setExpression(String) may contain
references to other variables in scope using the syntax $id,
${id} or $(id).  The first case only works if the id consists
only of alphanumeric characters and/or underscore, and if the
character immediately following the id is not one of these.
To get a simple dollar sign, use $$.
<p>
In addition, this class supports an annotation that specifies
choices for values.  A user interface can use this to present a
choice dialog that offers the specified values.  This is typically
used when a particular set of choices make sense.  The values can
be any expression, or if used in conjunction with string mode,
any string.
<p>
By default, an instance of Parameter, unlike Variable, is persistent.
<p>
By convention, an instance of NamedObj has a set of attributes,
some of which are visible to users and some of which are not.
When a user interface presents these attributes
for editing, it presents only those that are visible.
By default, an instance of Parameter is visible, as indicated by the fact
that it returns FULL in its getVisibility() method.  This can be overridden
by calling setVisibility().

@author Neil Smyth, Edward A. Lee, Xiaojun Liu
@version $Id$
@since Ptolemy II 0.2

@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token

*/

public class Parameter extends Variable {

    /** Construct a parameter in the default workspace with an empty
     *  string as its name. The parameter is added to the list of
     *  objects in the workspace.
     *  Increment the version number of the workspace.
     */
    public Parameter() {
        super();
        setVisibility(Settable.FULL);
        setPersistent(true);
    }

    /** Construct a parameter in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the parameter.
     */
    public Parameter(Workspace workspace) {
        super(workspace);
        setVisibility(Settable.FULL);
        setPersistent(true);
    }

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public Parameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setVisibility(Settable.FULL);
        setPersistent(true);
    }

    /** Construct a Parameter with the given container, name, and Token.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  @param container The container.
     *  @param name The name.
     *  @param token The Token contained by this Parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an parameter already in the container.
     */
    public Parameter(NamedObj container, String name, ptolemy.data.Token token)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, token);
        setVisibility(Settable.FULL);
        setPersistent(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a choice.
     *  @param choice A choice to offer to the user.
     *  @see #removeChoice(String)
     */
    public void addChoice(String choice) {
        if (_choices == null) {
            _choices = new ArrayList();
        }
        _choices.add(choice);
    }

    /** Write a MoML description of this object, unless this object is
     *  not persistent. MoML is an XML modeling markup language.
     *  In this class, the object is identified by the "property"
     *  element, with "name" and "class" (XML) attributes.
     *  The body of the element, between the "&lt;property&gt;"
     *  and "&lt;/property&gt;", is written using
     *  the _exportMoMLContents() protected method, so that derived classes
     *  can override that method alone to alter only how the contents
     *  of this object are described.
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     *  @see #isPersistent()
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (!isPersistent()) {
            return;
        }
        String value = getExpression();
        String valueTerm = "";
        if (value != null && !value.equals("")) {
            valueTerm = " value=\"" +
                StringUtilities.escapeForXML(value) + "\"";
        }

        output.write(_getIndentPrefix(depth)
                + "<"
                + getMoMLInfo().elementName
                + " name=\""
                + name
                + "\" class=\""
                + getMoMLInfo().className
                + "\""
                + valueTerm
                + ">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</"
                + getMoMLInfo().elementName + ">\n");
    }
    
    /** Get choices.
     *  @return An array of choices, or null if there are none.
     *  @see #addChoice(String)
     */
    public String[] getChoices() {
        if (_choices == null || _choices.size() == 0) {
            return null;
        } else {
            return (String [])_choices.toArray(new String[_choices.size()]);
        }
    }
    
    /** Override the base class to remove the enclosing quotation marks
     *  and reverse the escaping of quotation marks and backslashes that
     *  is done in setExpression() if this parameter is in string mode.
     *  @return The expression used to set the value of this parameter.
     *  @see #setExpression(String)
     */
    public String getExpression() {
        // NOTE: The following never returns null.
        String value = super.getExpression();
        if (isStringMode()) {
            String trimmed = value.trim();
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                value = trimmed.substring(1, trimmed.length()-1);
            }
            // NOTE: To get a backslash in a regexp, need two backslashes.
            // To get two backslashes in a string, you need four.
            // Hence the second substitution looks really weird.
            // It is not clear to me why we need eight in the replacement,
            // but it works.
            value = value.replaceAll("\\\\\"", "\"").replaceAll("\\\\\\\\", "\\\\");
        }
        return value;
    }
    
    /** Override the base class so that if this parameter is in string
     *  mode and the superclass method return null, then modify the
     *  return value to be a StringToken with an empty string.
     *  @return The token contained by this variable converted to the
     *   type of this variable, or null if there is none.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    public ptolemy.data.Token getToken() throws IllegalActionException {
        ptolemy.data.Token result = super.getToken();
        if (isStringMode() && result == null) {
            result = _EMPTY_STRING_TOKEN;
        }
        return result;
    }
    
    /** Return true if this parameter is in string mode.
     *  @return True if this parameter is in string mode.
     *  @see #setStringMode(boolean)
     */
    public boolean isStringMode() {
        return _stringMode;
    }
    
    /** Remove a choice.
     *  @param choice A choice to remove from the list offered to the user.
     *  @see #removeChoice(String)
     */
    public void removeChoice(String choice) {
        if (_choices != null) {
            _choices.remove(choice);
        }
    }
    
    /** Override the base class to wrap the argument in quotation marks
     *  if this parameter is in string mode, and to escape quotation marks
     *  and backslashes so that they persist literally in the string.
     *  @param expr The expression for this parameter.
     */
    public void setExpression(String expr) {
        if (isStringMode()) {
            // NOTE: To get a backslash in a regexp, need two backslashes.
            // To get two backslashes in a string, you need four.
            // Hence the second substitution looks really weird.
            // It is not clear to me why we need eight in the replacement,
            // but it works.
            expr = "\""
                    + expr.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"")
                    + "\"";
        }
        super.setExpression(expr);
    }
    
    /** Specify whether this parameter should be in string mode.
     *  If the argument is true, then specify that the type of this
     *  parameter is string. Otherwise, specify that the type is
     *  unknown.
     *  @param stringMode True to put the parameter in string mode.
     *  @exception IllegalActionException If the current value of this
     *   parameter is incompatible with the resulting type.
     *  @see #isStringMode()
     */
    public void setStringMode(boolean stringMode)
            throws IllegalActionException {
        _stringMode = stringMode;
        if (_stringMode) {
            setTypeEquals(BaseType.STRING);
        } else {
            setTypeEquals(BaseType.UNKNOWN);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
   
    /** Override the base class so that if this parameter is in string
     *  mode, we parse the string for references to variables rather
     *  than using the expression language parser. For non-string mode
     *  parameters, refer to the superclass documentation. Here, we
     *  explain what this does for string mode only.
     *  <p>
     *  If this variable
     *  was last set directly with a token, then do nothing. In other words,
     *  the string is evaluated only if the value of the token was most
     *  recently given by an expression.  The expression is also evaluated
     *  if any of the variables it refers to have changed since the last
     *  evaluation.  If the value of this variable
     *  changes due to this evaluation, then notify all
     *  value dependents and notify the container (if there is one) by
     *  calling its attributeChanged() method. An exception is thrown
     *  if the expression is illegal, for example if a parse error occurs
     *  or if there is a dependency loop.
     *  <p>
     *  Part of this method is read-synchronized on the workspace.
     *
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if a dependency loop is found.
     */
    protected void _evaluate() throws IllegalActionException {
        if (!isStringMode()) {
            super._evaluate();
            return;
        }
        // _currentExpression is null if the parameter was set by setToken().
        // It's value has the enclosing quotation marks and escaping of
        // internal characters, unlike the value returned by getExpression().
        String expression = getExpression();
        if (_currentExpression == null
                || expression.trim().equals("")) {
            _setToken(null);
            return;
        }
        // If _dependencyLoop is true, then this call to evaluate() must
        // have been triggered by evaluating the expression of this variable,
        // which means that the expression directly or indirectly refers
        // to itself.
        if (_dependencyLoop) {
            _dependencyLoop = false;
            throw new IllegalActionException("A parameter value cannot refer to itself.");
        }
        _dependencyLoop = true;

        try {
            workspace().getReadAccess();
            if (_parserScope == null) {
                // Use this as the reference for the scope so that
                // a parameter can define its own constants.
                _parserScope = new VariableScope(this);
            }
            if (!_parseTreeValid) {
                _parseList = new ParseList(expression, _parserScope);
                _parseTreeValid = true;
            }
            ptolemy.data.Token result = new StringToken(_parseList.evaluate());
            _setTokenAndNotify(result);
        } catch (IllegalActionException ex) {
            _needsEvaluation = true;
            throw new IllegalActionException(this, ex,
                    "Error evaluating expression: "
                    + expression);
        } finally {
            _dependencyLoop = false;
            workspace().doneReading();
        }
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The List of choices.
    private List _choices;

    // Used to check for dependency loops among variables.
    private transient boolean _dependencyLoop = false;
    
    // List of components making up the string.
    private ParseList _parseList;

    // Indicator of whether this parameter is in string mode.
    private boolean _stringMode;
    
    // Empty string token.
    private static StringToken _EMPTY_STRING_TOKEN = new StringToken("");
        
    // Indicator that a parse list entry is an identifier.
    private static boolean _IDENTIFIER = false;
    
    // Indicator that a parse list entry is a literal.
    private static boolean _LITERAL = true;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // List of references to either literal strings or variables.
    private class ParseList {
        
        /** Construct a parse list for the specified expression and
         *  scope.
         *  @param expression The expression.
         *  @param scope The scope.
         *  @throws IllegalActionException If a parse error occurs.
         */
        public ParseList(String expression, ParserScope scope)
                throws IllegalActionException {
            // NOTE: This is a brute force parser... Surely there is a
            // better way to create this.      
            _parseList = new LinkedList();
            _scope = scope;   
            int dollar = expression.indexOf('$');
            if (dollar < 0) {
                // No variable references at all.
                _parseList.add(new Element(expression, _LITERAL));
                return;
            }
            int parsedTo = 0;
            while (dollar >= 0) {
                // Get the string up to the $.
                if (parsedTo < dollar) {
                    _parseList.add(new Element(
                            expression.substring(parsedTo, dollar),
                            _LITERAL));
                }
                // Error if the string ends in $.
                if (dollar >= expression.length() - 1) {
                    throw new IllegalActionException(Parameter.this,
                    "Parse error: Expecting identifier after $: " + expression);
                }
                // We are now sure dollar is not the last character.
                if (expression.charAt(dollar + 1) == '$') {
                    // Found a double $.
                    _parseList.add(new Element("$", _LITERAL));
                    // If this is the last character, stop.
                    if (expression.length() == dollar + 2) {
                        parsedTo = dollar + 2;
                        break;
                    }
                    // Look for the next dollar sign.
                    parsedTo = dollar + 2;
                    dollar = expression.indexOf("$", parsedTo);
                    continue;
                }
                // We now know the dollar is not a $$.
                // See whether the identifier is bracketed with
                // curly braces.
                if (expression.charAt(dollar + 1) == '{') {
                    int closeBrace = expression.indexOf('}', dollar + 1);
                    if (closeBrace < 0) {
                        throw new IllegalActionException(Parameter.this,
                        "Parse error: Missing closing brace: " + expression);
                    }
                    _parseList.add(new Element(
                            expression.substring(dollar + 2, closeBrace),
                            _IDENTIFIER));
                    // Continue the search after the identifier.
                    dollar = closeBrace;
                } else if (expression.charAt(dollar + 1) == '(') {
                    int closeBrace = expression.indexOf(')', dollar + 1);
                    if (closeBrace < 0) {
                        throw new IllegalActionException(Parameter.this,
                        "Parse error: Missing closing brace: " + expression);
                    }
                    _parseList.add(new Element(
                            expression.substring(dollar + 2, closeBrace),
                            _IDENTIFIER));
                    // Continue the search after the identifier.
                    dollar = closeBrace;
                } else {
                    // No braces. Close the identifier with a non-word character.
                    String[] split = expression.substring(dollar + 1).split("\\W", 2);
                    _parseList.add(new Element(split[0], _IDENTIFIER));
                    dollar += split[0].length();
                    if (dollar >= expression.length() - 1) {
                        // At the end.
                        parsedTo = dollar + 1;
                        break;
                    }
                }
                // Look for the next dollar sign.
                parsedTo = dollar + 1;
                dollar = expression.indexOf("$", parsedTo);
            }
            // Catch the trailing edge of the string.
            if (parsedTo < expression.length()) {
                // No variable references at all.
                _parseList.add(new Element(expression.substring(parsedTo), _LITERAL));
            }
        }
        
        /** Return the result of evaluating the parse list.
         *  @return A string that is a concatenation of the literals in
         *   the parse list and the values of the identifiers.
         *  @exception IllegalActionException If the string refers
         *   to an identifier and that identifier is undefined.
         */
        public String evaluate() throws IllegalActionException {
            StringBuffer result = new StringBuffer("");
            Iterator elements = _parseList.iterator();
            while (elements.hasNext()) {
                Element element = (Element)elements.next();
                if (element.literal == _LITERAL) {
                    result.append(element.string);
                } else {
                    ptolemy.data.Token value = _scope.get(element.string);
                    if (value == null) {
                        // Look up registered constants.
                        value = Constants.get(element.string);
                        if (value == null) {                        
                            throw new IllegalActionException(Parameter.this,
                            "The ID " + element.string + " is undefined.");
                        }
                    }
                    // NOTE: Treat string tokens specially to avoid
                    // getting their double quotes.
                    if (value instanceof StringToken) {
                        result.append(((StringToken)value).stringValue());
                    } else {
                        result.append(value.toString());
                    }
                }
            }
            return result.toString();
        }
        
        // A list of instances of Element.
        private List _parseList;
        
        // The parser scope.
        private ParserScope _scope;
        
        // Nested inner class to store an element that is either a literal
        // string or an identifier.
        private class Element {
            
            /** Create a list element with the specified string.
             *  If the <i>literal</i> argument is true, then the string
             *  is assumed to be a literal string.  Otherwise, it is
             *  assumed to be an identifier.
             *  @param string A literal string or an identifier.
             *  @param literal An indicator that the string is a literal.
             */
            public Element(String string, boolean literal) {
                this.string = string;
                this.literal = literal;
            }
            public String string;
            public boolean literal;
        }
    }
}
