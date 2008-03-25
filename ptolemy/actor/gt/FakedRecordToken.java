/*

@Copyright (c) 1997-2008 The Regents of the University of California.
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

package ptolemy.actor.gt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class FakedRecordToken extends RecordToken {

    public FakedRecordToken() throws IllegalActionException {
        super(new String[0], new Token[0]);
    }

    public boolean equals(Object object) {
        return this == object;
    }

    public Token get(String label) {
        return null;
    }

    public Type getType() {
        Set<String> labelSet = labelSet();
        Iterator<String> labelIterator = labelSet.iterator();
        String[] labels = new String[labelSet.size()];
        Type[] types = new Type[labelSet.size()];

        for (int i = 0; i < labels.length; i++) {
            labels[i] = labelIterator.next();
            types[i] = BaseType.UNKNOWN;
        }

        return new FakedRecordType(labels, types);
    }

    public int hashCode() {
        return Arrays.hashCode(_thisArray);
    }

    public Set<String> labelSet() {
        return _emptySet;
    }

    public int length() {
        return 0;
    }

    public Token one() throws IllegalActionException {
        throw new IllegalActionException(
                "Multiplicative identity (one) does not exist.");
    }

    public Token zero() throws IllegalActionException {
        throw new IllegalActionException(
                "Additive identity (zero) does not exist.");
    }

    public static class FakedRecordType extends RecordType {

        public FakedRecordType(String[] labels, Type[] types) {
            super(labels, types);
        }

    }

    protected Token _add(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Add operation is not supported.");
    }

    protected Token _divide(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Divide operation is not supported.");
    }

    protected BooleanToken _isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        throw new IllegalActionException(
                "IsCloseTo operation is not supported.");
    }

    protected BooleanToken _isEqualTo(Token token)
            throws IllegalActionException {
        throw new IllegalActionException(
                "IsEqualTo operation is not supported.");
    }

    protected Token _modulo(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Modulo operation is not supported.");
    }

    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException("Multiply operation is not supported.");
    }

    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException("Subtract operation is not supported.");
    }

    private final Set<String> _emptySet = new HashSet<String>();

    private final FakedRecordToken[] _thisArray = new FakedRecordToken[] { this };

}
