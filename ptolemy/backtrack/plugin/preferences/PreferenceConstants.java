/* 

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.plugin.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

    public static final String PTII_PREFERENCE_ID =
        "ptolemy.backtrack.plugin.preferences.PtolemyPreferencePage";
    
	public static final String PTII = "ptolemy.PTII";

    ///////////////////////////////////////////////////////////////////
    ////                        backtracking                       ////

	public static final String BACKTRACK_SOURCE_LIST =
        "ptolemy.backtrackSourceList";

	public static final String BACKTRACK_SOURCES = "ptolemy.backtrackSources";

	public static final String BACKTRACK_EXTRA_CLASSPATHS =
        "ptolemy.backtrackExtraClassPaths";
    
    public static final String BACKTRACK_ROOT = "ptolemy.backtrackingRoot";
    
    public static final String BACKTRACK_PREFIX = "ptolemy.backtrackPrefix";
    
    public static final String BACKTRACK_OVERWRITE =
        "ptolemy.backtrackOverwrite";
	
    public static final String BACKTRACK_GENERATE_CONFIGURATION =
        "ptolemy.backtrackGenerateConfiguration";
    
    public static final String BACKTRACK_CONFIGURATION =
        "ptolemy.backtrackConfiguration";

    ///////////////////////////////////////////////////////////////////
    ////                           editor                          ////

    public static final String EDITOR_HIGHLIGHTING_ENABLED =
        "ptolemy.editorHighlightingEnabled";
    
    public static final String EDITOR_STATE_BOLD = "ptolemy.editorStateBold";
    
    public static final String EDITOR_STATE_COLOR = "ptolemy.editorStateColor";
    
    public static final String EDITOR_STATE_ITALIC =
        "ptolemy.editorStateItalic";
    
    public static final String EDITOR_ACTOR_METHOD_BOLD =
        "ptolemy.editorActorMethodBold";
    
    public static final String EDITOR_ACTOR_METHOD_COLOR =
        "ptolemy.editorActorMethodColor";
    
    public static final String EDITOR_ACTOR_METHOD_ITALIC =
        "ptolemy.editorActorMethodItalic";
}
