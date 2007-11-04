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

import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.ingredients.operations.Operation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeRequest;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GraphTransformer {

    public boolean transform(TransformationRule transformer,
            MatchResult matchResult, CompositeEntity hostGraph) {
        boolean success = true;
        
        success = success && _performOperations(matchResult,
                transformer.getPattern(), transformer.getReplacement());
        
        return success;
    }

    private boolean _performOperations(MatchResult matchResult, Pattern pattern,
            CompositeEntity replacementContainer) {
        for (Object replacementObject : replacementContainer.entityList()) {
            if (replacementObject instanceof CompositeEntity) {
                _performOperations(matchResult, pattern,
                        (CompositeEntity) replacementObject);
            }

            if (!(replacementObject instanceof GTEntity)) {
                continue;
            }

            GTEntity replacementEntity = (GTEntity) replacementObject;
            PatternEntityAttribute attribute =
                replacementEntity.getPatternEntityAttribute();
            if (attribute == null) {
                continue;
            }

            String patternEntityName = attribute.getExpression();
            GTEntity patternEntity =
                (GTEntity) pattern.getEntity(patternEntityName);
            if (patternEntity == null) {
                return false;
            }

            try {
                ComponentEntity match =
                    (ComponentEntity) matchResult.get(patternEntity);
                GTIngredientList ingredientList = replacementEntity
                        .getOperationsAttribute().getIngredientList();
                for (GTIngredient ingredient : ingredientList) {
                    ChangeRequest request =
                        ((Operation) ingredient).getChangeRequest(
                                patternEntity, replacementEntity, match);
                    match.requestChange(request);
                }
            } catch (MalformedStringException e) {
                return false;
            }
        }
        return true;
    }
}
