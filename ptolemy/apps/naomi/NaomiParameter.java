/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

package ptolemy.apps.naomi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class NaomiParameter extends StringParameter implements ChangeListener {

    public NaomiParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setVisibility(EXPERT);
    }

    public void changeExecuted(ChangeRequest change) {
        if (change instanceof MoMLChangeRequest
                && !(change instanceof PrivateMoMLChangeRequest)) {
            NamedObj container = getContainer();
            NamedObj context = ((MoMLChangeRequest) change).getContext();
            if (container != null
                    && (container == context || container.getContainer() == context)) {
                String expression = StringUtilities
                        .unescapeForXML(formatExpression(_method,
                                _attributeName, new Date(), _unit,
                                _documentation));
                String moml = "<property name=\"" + getName() + "\" value=\""
                        + expression + "\"/>";
                PrivateMoMLChangeRequest request = new PrivateMoMLChangeRequest(
                        this, container, moml);
                request.setUndoable(true);
                request.setMergeWithPreviousUndo(true);
                container.requestChange(request);
            }
        }
    }

    private static class PrivateMoMLChangeRequest extends MoMLChangeRequest {

        public PrivateMoMLChangeRequest(Object originator, NamedObj context,
                String request) {
            super(originator, context, request);
        }
    }

    public void changeFailed(ChangeRequest change, Exception exception) {
    }

    public String getAttributeName() {
        return _attributeName;
    }

    public String getDocumentation() {
        return _documentation;
    }

    public static String formatExpression(Method method, String attributeName,
            Date modifiedDate, String unit, String documentation) {
        if (method == null) {
            return "";
        } else {
            return method + ":" + attributeName + " ("
                    + new SimpleDateFormat(DATE_FORMAT).format(modifiedDate)
                    + ") (" + unit + ") (" + documentation + ")";
        }
    }

    public Method getMethod() {
        return _method;
    }

    public Date getModifiedDate() {
        return _modifiedDate;
    }

    public String getUnit() {
        return _unit;
    }

    public void setAttributeName(String name) {
        setExpression(formatExpression(_method, name, _modifiedDate, _unit,
                _documentation));
    }

    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        NamedObj oldContainer = getContainer();
        if (container != oldContainer) {
            super.setContainer(container);
            if (oldContainer != null) {
                oldContainer.removeChangeListener(this);
            }
            if (container != null) {
                container.addChangeListener(this);
            }
        }
    }

    public void setDocumentation(String documentation) {
        setExpression(formatExpression(_method, _attributeName, _modifiedDate,
                _unit, documentation));
    }

    public void setExpression(String expr) {
        if (expr == null) {
            expr = "";
        }
        super.setExpression(expr);
    }

    public void setMethod(Method method) {
        setExpression(formatExpression(method, _attributeName, _modifiedDate,
                _unit, _documentation));
    }

    public void setModifiedDate(Date date) {
        setExpression(formatExpression(_method, _attributeName, date, _unit,
                _documentation));
    }

    public void setUnit(String unit) {
        setExpression(formatExpression(_method, _attributeName, _modifiedDate,
                unit, _documentation));
    }

    public Collection<?> validate() throws IllegalActionException {
        String expression = getExpression();
        if (expression == null || expression.equals("")) {
            _method = null;
            _attributeName = null;
            _modifiedDate = null;
            _unit = null;
            _documentation = null;
        } else {
            Matcher matcher = _PATTERN.matcher(expression);
            if (!matcher.matches()) {
                throw new IllegalActionException(this, "Fail to parse: "
                        + expression);
            }
            String method = matcher.group(1);
            if (method.equals("get")) {
                _method = Method.GET;
            } else if (method.equals("put")) {
                _method = Method.PUT;
            } else if (method.equals("sync")) {
                _method = Method.SYNC;
            } else {
                throw new IllegalActionException(this, "Unknown method: "
                        + method);
            }
            _attributeName = matcher.group(2);
            try {
                _modifiedDate = new SimpleDateFormat(DATE_FORMAT).parse(matcher
                        .group(3));
            } catch (ParseException e) {
                throw new IllegalActionException(this, e, "Fail to parse: "
                        + expression);
            }
            _unit = matcher.group(4);
            _documentation = matcher.group(5);
        }
        return super.validate();
    }

    public static final String DATE_FORMAT = "EEE, MMM dd yyyy HH:mm:ss.SSS Z";

    public enum Method {
        GET("get"), PUT("put"), SYNC("sync");

        public String toString() {
            return _name;
        }

        Method(String name) {
            _name = name;
        }

        private String _name;
    }

    private static final Pattern _PATTERN = Pattern
            .compile("\\s*"
                    + "((?:get)|(?:put)|(?:sync)):"
                    + "([a-zA-Z\\$_][a-zA-Z\\$_0-9]*(?:\\.[a-zA-Z\\$_][a-zA-Z\\$_0-9]*)*)"
                    + "\\s+\\((.*)\\)\\s+\\((.*)\\)\\s+\\((.*)\\)\\s*");

    private String _attributeName;

    private String _documentation;

    private Method _method;

    private Date _modifiedDate;

    private String _unit;
}
