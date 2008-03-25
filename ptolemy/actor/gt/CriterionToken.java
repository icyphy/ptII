/*

@Copyright (c) 2007-2008 The Regents of the University of California.
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

import java.util.Set;

import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class CriterionToken extends FakedRecordToken {

    public CriterionToken(Criterion criterion) throws IllegalActionException {
        _criterion = criterion;
    }

    public boolean equals(Object object) {
        return this == object
                || (object instanceof CriterionToken && ((CriterionToken) object)._criterion
                        .equals(_criterion));
    }

    public Token get(String label) {
        // TODO
        return null;
    }

    public Criterion getCriterion() {
        return _criterion;
    }

    public int hashCode() {
        return _criterion.hashCode();
    }

    public Set<String> labelSet() {
        // TODO
        return null;
    }

    public int length() {
        // TODO
        return 0;
    }

    private Criterion _criterion;

}
