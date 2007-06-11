/*

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.actor.gt;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// RuleList

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class RuleList extends LinkedList<Rule> {

    public RuleList() {
    }

    public RuleList(RuleList initRules) {
        super(initRules);
    }

    public static RuleList parse(String expression)
    throws MalformedStringException {
        RuleList list = new RuleList();
        int startPos = 0;
        while (startPos < expression.length()) {
            int endPos = Rule.findMatchingParen(expression, startPos);
            if (endPos < 0) {
                throw new MalformedStringException(expression);
            }
            String ruleString = expression.substring(startPos + 1, endPos);
            Rule rule = _parseRule(ruleString);
            list.add(rule);
            startPos = endPos + 1;
        }
        return list;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Rule rule : this) {
            buffer.append('(');
            buffer.append(rule.getClass().getName());
            buffer.append(Rule.FIELD_SEPARATOR);
            buffer.append(rule.getValues());
            buffer.append(')');
        }
        return buffer.toString();
    }

    public void validate() throws RuleValidationException {
        int i = 0;
        for (Rule rule : this) {
            i++;
            try {
                rule.validate();
            } catch (RuleValidationException e) {
                throw new RuleValidationException("Rule " + i + ": "
                        + e.getMessage());
            }
        }
    }

    private static Rule _parseRule(String ruleString) {
        int separator = ruleString.indexOf(Rule.FIELD_SEPARATOR);
        if (separator < 0) {
            return null;
        } else {
            String ruleClassName = ruleString.substring(0, separator);
            try {
                Class<?> namedClass = Class.forName(ruleClassName);
                Class<? extends Rule> ruleClass =
                    namedClass.asSubclass(Rule.class);
                String values = ruleString.substring(separator + 1);
                return (Rule)
                    ruleClass.getConstructor(String.class).newInstance(values);
            } catch (ClassNotFoundException e) {
            } catch (IllegalAccessException e) {
            } catch (InstantiationException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
            return null;
        }
    }

    private static final long serialVersionUID = -5344621368362493745L;
}
