/* Interface of the class declaration handlers called by TypeAnalyzer.

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
package ptolemy.backtrack.ast.transform;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ptolemy.backtrack.ast.TypeAnalyzerState;

//////////////////////////////////////////////////////////////////////////
//// ClassHandler

/**
 Interface of the class declaration handlers called by {@link TypeAnalyzer}.
 Users may register class declaration handlers (and other kinds of supported
 handlers) to the {@link TypeAnalyzer} used to analyze Java source code.
 When the analyzer detects a class declaration, it calls back those
 handlers after the classes are completely traversed.
 <p>
 Class declaration handlers are allowed to modify the classes.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface ClassHandler {
    /** Enter an anonymous class declaration.
     *
     *  @param node The anonymous class declaration to be handled.
     *  @param state The current state of the analyzer.
     */
    public void enter(AnonymousClassDeclaration node, TypeAnalyzerState state);

    /** Enter a class declaration.
     *
     *  @param node The class declaration to be handled.
     *  @param state The current state of the analyzer.
     */
    public void enter(TypeDeclaration node, TypeAnalyzerState state);

    /** Exit an anonymous class declaration.
     *
     *  @param node The anonymous class declaration to be handled.
     *  @param state The current state of the analyzer.
     */
    public void exit(AnonymousClassDeclaration node, TypeAnalyzerState state);

    /** Exit a class declaration.
     *
     *  @param node The class declaration to be handled.
     *  @param state The current state of the analyzer.
     */
    public void exit(TypeDeclaration node, TypeAnalyzerState state);
}
