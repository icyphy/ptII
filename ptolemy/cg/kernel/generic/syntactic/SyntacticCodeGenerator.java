/* Code generator for syntactic representations.

Copyright (c) 2009-2014 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.syntactic;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Tableau;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.lib.syntactic.SyntacticGraph;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////SyntacticCodeGenerator

/** Generate a syntactic representation of a ptolemy model.
 *  <p>To generate a syntactic representation, use:
 *  <pre>
     java -classpath $PTII ptolemy.cg.kernel.generic.syntactic.SyntacticCodeGenerator
 *  </pre>
 *  @author Chris Shaver
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (shaver)
 *  @Pt.AcceptedRating red
 */
public class SyntacticCodeGenerator extends GenericCodeGenerator {

    /** Create a new instance of the SyntacticCodeGenerator.
     *  The value of the <i>generatorPackageList</i> parameter of the
     *  base class is set to <code>generic.syntactic</code>
     *
     *  @param container The container.
     *  @param name The name of the SyntacticCodeGenerator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public SyntacticCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        generatorPackageList.setExpression("");

        _graphCount = 0;
        _syntaxGraph = null;//new SyntacticGraph();
        //_syntaxGraph.setName("syntaxGraph");
    }

    /** Format a string as a code comment.
     *  This format is specific to the grammar of the generated
     *  syntax.
     *
     *  Inheriting classes should override this function for the
     *  appropriate conventions of the syntax.
     *
     *  @param comment The string to format as a comment.
     *  @return A formatted comment.
     */
    @Override
    public String comment(String comment) {
        return "{--- " + _eol + comment + _eol + " ---}" + _eol;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate a syntactic representation and append it to the given string buffer.
     *  Write the code to the directory specified by the <i>codeDirectory</i>
     *  parameter.  The file name is a sanitized version of the model
     *  name with a suffix that is based on last package name of the
     *  <i>generatorPackage</i> parameter.  Thus if the
     *  <i>codeDirectory</i> is <code>$HOME</code>, the name of the
     *  model is <code>Foo</code> and the <i>generatorPackage</i>
     *  is <code>ptolemy.cg.kernel.generic.syntactic</code>, then the file that is
     *  written will be <code>$HOME/Foo.html</code>
     *  This method is the main entry point to generate code.
     *
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    @Override
    protected int _generateCode(StringBuffer code) throws KernelException {
        code.append("{--- Syntactic Representation of graph ---}" + _eol);

        _syntaxGraph = new SyntacticGraph();
        _syntaxGraph.setName("syntaxGraph" + ++_graphCount);

        // Get top level actor
        CompositeEntity container = (CompositeEntity) getContainer();
        _syntaxGraph.build(container);
        _showGraph();

        code.append(_syntaxGraph.generateCode());

        // Generate file in generic super
        return super._generateCode(code);
    }

    /** Return the filter class to find adapters. All
     *  adapters have to extend this class.
     *  @return The base class for the adapters.
     */
    @Override
    protected Class<?> _getAdapterClassFilter() {
        return SyntacticCodeGeneratorAdapter.class;
    }

    /** Show the SyntacticGraph in a Tableau.
     *  @exception IllegalActionException Thrown if there is a problem
     *  getting the configuration, opening the instance of the
     *  syntactic graph or showing the tableau.
     */
    protected void _showGraph() throws IllegalActionException {
        try {
            Configuration config = (Configuration) Configuration
                    .configurations().get(0);
            _syntaxTableau = config.openInstance(_syntaxGraph);
            _syntaxTableau.show();
        } catch (Exception ex) {
            throw new IllegalActionException(getComponent(), ex,
                    "Failed to show " + "the SyntacticGraph in a Tableau");

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private SyntacticGraph _syntaxGraph;
    private Tableau _syntaxTableau;
    private int _graphCount;

}
