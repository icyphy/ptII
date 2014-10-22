/* Common interface of the matchers in model transformations.

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
package ptolemy.actor.gt;

import java.util.Set;

import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

/**
 Common interface of the matchers in model transformations.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface GTEntity extends Nameable {

    /** Return the attribute that stores all the criteria for this matcher.
     *
     *  @return The attribute that stores all the criteria.
     */
    public GTIngredientsAttribute getCriteriaAttribute();

    /** Return a string that contains the SVG icon description
     *  ("&lt;svg&gt;...&lt;/svg&gt;") for this matcher. This icon description
     *  is the default icon for the matcher, which may be changed by the
     *  criteria.
     *
     *  @return The icon description.
     */
    public String getDefaultIconDescription();

    /** Return the attribute that stores all the operations for this matcher.
     *
     *  @return The attribute that stores all the operations.
     */
    public GTIngredientsAttribute getOperationsAttribute();

    /** Return the attribute that stores the name of the corresponding entity in
     *  the pattern of the same {@link TransformationRule}, if this entity is in
     *  the replacement, or <tt>null</tt> otherwise.
     *
     *  @return The attribute that stores the name of the corresponding entity.
     *  @see #labelSet()
     */
    public PatternObjectAttribute getPatternObjectAttribute();

    /** Return the set of names of ingredients contained in this entity that can
     *  be resolved.
     *
     *  @return The set of names.
     */
    public Set<String> labelSet();

    /** Test whether this GTEntity can match the given object. The matching
     *  is shallow in the sense that objects contained by this GTEntity need not
     *  match the corresponding objects in the given object for the return
     *  result to be true.
     *
     *  @param object The NamedObj.
     *  @return Whether this GTEntity can match the given object.
     */
    public boolean match(NamedObj object);

    /** Update appearance of this entity.
     *
     *  @param attribute The attribute containing ingredients of this entity.
     */
    public void updateAppearance(GTIngredientsAttribute attribute);
}
