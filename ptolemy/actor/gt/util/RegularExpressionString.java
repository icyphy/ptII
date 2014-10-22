/* A wrapper for a string containing a regular expression.

@Copyright (c) 2007-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor.gt.util;

import java.util.regex.Pattern;

//////////////////////////////////////////////////////////////////////////
//// RegularExpressionString

/**
 A wrapper for a string containing a regular expression.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RegularExpressionString {

    /** Construct a Ptolemy expression string.
     */
    public RegularExpressionString() {
        this("");
    }

    /** Construct a Ptolemy expression string with the given value as its
     *  initial value.
     *
     *  @param value The initial value.
     */
    public RegularExpressionString(String value) {
        set(value);
    }

    /** Get the current value.
     *
     *  @return The value.
     *  @see #set(String)
     */
    public String get() {
        return _value;
    }

    /** Get the pattern for the regular expression.
     *
     *  @return The pattern.
     */
    public Pattern getPattern() {
        if (_needReparse) {
            _pattern = Pattern.compile(_value);
            _needReparse = false;
        }
        return _pattern;
    }

    /** Set the value.
     *
     *  @param value The value.
     *  @see #get()
     */
    public void set(String value) {
        _value = value;
        _needReparse = true;
    }

    /** Return the regular expression in a string.
     *
     *  @return The regular expression.
     */
    @Override
    public String toString() {
        return get();
    }

    /** Whether the regular expression needs to be reparsed.
     */
    private boolean _needReparse;

    /** The pattern for the regular expression.
     */
    private Pattern _pattern;

    /** The regular expression.
     */
    private String _value;
}
