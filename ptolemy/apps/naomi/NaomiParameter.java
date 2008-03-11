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

import java.text.DateFormat;
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

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class NaomiParameter extends StringParameter implements ChangeListener {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public NaomiParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public void changeExecuted(ChangeRequest change) {
        if (change instanceof MoMLChangeRequest) {
            NamedObj container = getContainer();
            if (container != null && container.getContainer() ==
                    ((MoMLChangeRequest) change).getContext()) {
                String moml = "<property name=\"" + getName() + "\" value=\"" +
                getExpression(_attributeName, new Date()) + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        container, moml);
                request.setUndoable(true);
                request.setMergeWithPreviousUndo(true);
                container.requestChange(request);
            }
        }
    }

    public void changeFailed(ChangeRequest change, Exception exception) {
    }

    public String getAttributeName() {
        return _attributeName;
    }

    public static String getExpression(String attributeName,
            Date modifiedDate) {
        return attributeName + " (" + DATE_FORMAT.format(modifiedDate) + ")";
    }

    public Date getModifiedDate() {
        return _modifiedDate;
    }

    public void setAttributeName(String name) {
        setExpression(getExpression(name, _modifiedDate));
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

    public void setExpression(String expr) {
        if (expr == null || expr.equals("")) {
            expr = "no_name (" + DATE_FORMAT.format(new Date()) + ")";
        }
        super.setExpression(expr);
    }

    public void setModifiedDate(Date date) {
        setExpression(getExpression(_attributeName, date));
    }

    public Collection<?> validate() throws IllegalActionException {
        String expression = getExpression();
        Matcher matcher = _PATTERN.matcher(expression);
        if (!matcher.matches()) {
            throw new IllegalActionException(this,
                    "Fail to parse: " + expression);
        }
        try {
            _modifiedDate = DATE_FORMAT.parse(matcher.group(2));
        } catch (ParseException e) {
            throw new IllegalActionException(this, e,
                    "Fail to parse: " + expression);
        }
        _attributeName = matcher.group(1);
        return super.validate();
    }

    public static final DateFormat DATE_FORMAT =
        new SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss.SSS Z");

    private static final Pattern _PATTERN = Pattern.compile("\\s*" +
            "([a-zA-Z\\$_][a-zA-Z\\$_0-9]*(?:\\.[a-zA-Z\\$_][a-zA-Z\\$_0-9]*)*)"
            + "\\s+\\((.*)\\)\\s*");

    private String _attributeName;

    private Date _modifiedDate;
}
