/* A set of keys as preference indices.

 Copyright (c) 2005-2013 The Regents of the University of California.
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
package ptolemy.backtrack.eclipse.plugin.preferences;

///////////////////////////////////////////////////////////////////
//// PreferenceConstants
/**
 A set of keys as preference indices.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PreferenceConstants {

    ///////////////////////////////////////////////////////////////////
    ////                        backtracking                       ////

    /** Configuration preference.
     */
    public static final String BACKTRACK_CONFIGURATION = "ptolemy.backtrackConfiguration";

    /** Extra classpath.
     */
    public static final String BACKTRACK_EXTRA_CLASSPATHS = "ptolemy.backtrackExtraClassPaths";

    /** Whether to generate configuration.
     */
    public static final String BACKTRACK_GENERATE_CONFIGURATION = "ptolemy.backtrackGenerateConfiguration";

    /** Whether to overwrite exiting files.
     */
    public static final String BACKTRACK_OVERWRITE = "ptolemy.backtrackOverwrite";

    /** Package prefix.
     */
    public static final String BACKTRACK_PREFIX = "ptolemy.backtrackPrefix";

    /** Root path.
     */
    public static final String BACKTRACK_ROOT = "ptolemy.backtrackingRoot";

    /** Backtracking source files.
     */
    public static final String BACKTRACK_SOURCES = "ptolemy.backtrackSources";

    /** Backtracking source list.
     */
    public static final String BACKTRACK_SOURCE_LIST = "ptolemy.backtrackSourceList";

    ///////////////////////////////////////////////////////////////////
    ////                           editor                          ////

    /** Whether special methods should have bold face.
     */
    public static final String EDITOR_ACTOR_METHOD_BOLD = "ptolemy.editorActorMethodBold";

    /** Color for special methods.
     */
    public static final String EDITOR_ACTOR_METHOD_COLOR = "ptolemy.editorActorMethodColor";

    /** Whether special methods should be italic.
     */
    public static final String EDITOR_ACTOR_METHOD_ITALIC = "ptolemy.editorActorMethodItalic";

    /** Whether semantic highlighting is enabled.
     */
    public static final String EDITOR_HIGHLIGHTING_ENABLED = "ptolemy.editorHighlightingEnabled";

    /** Whether state variables should have bold face.
     */
    public static final String EDITOR_STATE_BOLD = "ptolemy.editorStateBold";

    /** Color for state variables.
     */
    public static final String EDITOR_STATE_COLOR = "ptolemy.editorStateColor";

    /** Whether state variables should be italic.
     */
    public static final String EDITOR_STATE_ITALIC = "ptolemy.editorStateItalic";

    ///////////////////////////////////////////////////////////////////
    ////                            PTII                           ////

    /** PTII path.
     */
    public static final String PTII = "ptolemy.PTII";

    /** ID of the backtracking preferences.
     */
    public static final String PTII_PREFERENCE_ID = "ptolemy.backtrack.preferences.Ptolemy";
}
