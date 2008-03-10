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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class NaomiParameter extends StringParameter {

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

    public String getAttributeName() {
        return _attributeName;
    }

    public Date getModifiedDate() {
        return _modifiedDate;
    }

    public void setExpression(String expr) {
        if (expr == null || expr.equals("")) {
            expr = "no_name (" + DATE_FORMAT.format(new Date()) + ")";
        }
        super.setExpression(expr);
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
