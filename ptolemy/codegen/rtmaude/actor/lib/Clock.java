/* RTMaude Code generator helper class for the Clock class.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.actor.lib;

import java.util.Map;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Clock

/**
 * Generate RTMaude code for a Clock actor in DE domain.
 *
 * @see ptolemy.actor.lib.Clock
 * @author Kyungmin Bae
 * @version $Id$
 * @Pt.ProposedRating Red (kquine)
 *
 */
public class Clock extends Entity {

    /**
     * Constructor method for the Clock adapter.
     * @param actor the associated actor
     */
    public Clock(ptolemy.actor.lib.Clock component) {
        super(component);
    }
    
    @Override
    public String generateFireCode() throws IllegalActionException {
        ptolemy.actor.lib.Clock component = (ptolemy.actor.lib.Clock) getComponent();
        String evt = _generateBlockCode("eventBlock",
                component.getName(),
                component.output.getName(),
                _generateBlockCode("firstValueBlock"),
                _generateBlockCode("firstOffsetBlock")
                );
        return evt + super.generateFireCode();
    }

    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        atts.put("period", "'period");
        atts.put("offsets", "'offsets");
        atts.put("values", "'values");
        atts.put("index", "0");
        return atts;
    }
}
