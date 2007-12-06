/* Abstract superclass of Ptolemy semantic highlighting.

 Copyright (c) 2005-2007 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.plugin.editor;

//////////////////////////////////////////////////////////////////////////
//// SemanticHighlighting

/**
 Abstract superclass of Ptolemy semantic highlighting.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 @see SemanticHighlightings
 */
public abstract class SemanticHighlighting {

    /** Test whether a semantic token can be consumed.
     *
     *  @param token The token to be tested.
     *  @return true if the token can be consumed.
     */
    public abstract boolean consumes(SemanticToken token);

    /** Get the key of the bold face preference.
     *
     *  @return The key for the bold face preference.
     */
    public abstract String getBoldPreferenceKey();

    /** Get the key of the color preference.
     *
     *  @return The key of the color preference.
     */
    public abstract String getColorPreferenceKey();

    /** Get the key of the enabled preference.
     *
     *  @return The key of the enabled preference.
     */
    public abstract String getEnabledPreferenceKey();

    /** Get the key of the italic font preference.
     *
     *  @return The key of the italic font preference.
     */
    public abstract String getItalicPreferenceKey();
}
