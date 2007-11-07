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

package ptolemy.actor.gt;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTTools {

    public static CompositeActorMatcher getContainingPatternOrReplacement(
            NamedObj entity) {
        Nameable parent = entity.getContainer();
        while (parent != null && !(parent instanceof Pattern)
                && !(parent instanceof Replacement)) {
            parent = parent.getContainer();
        }
        return (CompositeActorMatcher) parent;
    }
    
    public static PatternObjectAttribute getPatternObjectAttribute(
    		NamedObj object, boolean createNew) {
        Attribute attribute = object.getAttribute("patternObject");
        if (attribute == null) {
        	if (createNew) {
	        	try {
					return new PatternObjectAttribute(object, "patternObject");
				} catch (IllegalActionException e) {
					return null;
				} catch (NameDuplicationException e) {
					return null;
				}
        	} else {
        		return null;
        	}
        }
        if (attribute instanceof PatternObjectAttribute) {
            return (PatternObjectAttribute) attribute;
        } else {
            return null;
        }
    }

    public static NamedObj getCorrespondingPatternObject(
    		NamedObj replacementEntity) {
        CompositeActorMatcher container =
            getContainingPatternOrReplacement(replacementEntity);
        if (container == null) {
            return null;
        }

        PatternObjectAttribute attribute =
        	getPatternObjectAttribute(replacementEntity, false);
        if (attribute == null) {
            return null;
        }

        String patternObjectName = attribute.getExpression();
        if (patternObjectName.equals("")) {
            return null;
        }

        TransformationRule transformer =
            (TransformationRule) container.getContainer();
        Pattern pattern = transformer.getPattern();
        return pattern.getEntity(patternObjectName);
    }

    public static boolean isInPattern(NamedObj entity) {
        CompositeActorMatcher container =
            getContainingPatternOrReplacement(entity);
        return container != null && container instanceof Pattern;
    }

    public static boolean isInReplacement(NamedObj entity) {
        CompositeActorMatcher container =
            getContainingPatternOrReplacement(entity);
        return container != null && container instanceof Replacement;
    }
}
