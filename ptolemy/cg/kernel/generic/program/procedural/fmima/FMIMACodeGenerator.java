/* Generate a Functional Mockup Interface Master Algorithm (FMIMA) description of a model.

Copyright (c) 2014 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.program.procedural.fmima;

import java.io.File;

import ptolemy.actor.CompositeActor;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StreamExec;

///////////////////////////////////////////////////////////////////
////FMIMACodeGenerator

/** Generate a Functional Mockup Interface Master Algorithm (FMIMA) description of a model.
 *  <p>To generate an FMIMA version of a model, use:
 *  <pre>
$PTII/bin/ptcg -generatorPackage ptolemy.cg.kernel.generic.program.procedural.fmima \
    -generatorPackagelist generic.program.procedural.fmima \
    $PTII/ptolemy/cg/kernel/generic/program/procedural/fmima/test/auto/FMUIncScale20RC1pt.xml
 * </pre>
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (cxh)
 *  @Pt.AcceptedRating red (cxh)
 */
public class FMIMACodeGenerator extends ProceduralCodeGenerator /*GenericCodeGenerator*/{

    /** Create a new instance of the FMIMACodeGenerator.
     *  The value of the <i>generatorPackageList</i> parameter of the
     *  base class is set to <code>generic.program.procedural.fmima</code>
     *  @param container The container.
     *  @param name The name of the FMIMACodeGenerator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public FMIMACodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        //super(container, name, "c");
        super(container, name, "c", "c");
        generatorPackageList.setExpression("generic.program.procedural.fmima");
    }

    /** Return a formatted comment containing the specified string. In
     *  this base class, the comments is a C-style comment, which
     *  begins with "/ *" and ends with "* /" followed by the platform
     *  dependent end of line character(s): under Unix: backslash n, under
     *  Windows: backslash n backslash r. Subclasses may override this produce comments
     *  that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    @Override
    public String comment(String comment) {
        return "/" + "* " + comment + " *" + "/" + _eol;
    }

    /** Generate the main entry point.
     *  @return Return the definition of the main entry point for a program.
     *   In C, this would be defining main().
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateMainEntryCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("ptolemy/cg/kernel/generic/program/procedural/fmima/FMIMACodeGenerator.java"));
        code.append(comment("Probably the thing to do is to create .c files and copy them over to the cg/ directory."));
        code.append(comment("Then we can create a few functions that do the real work."));
        if (_isTopLevel()) {
            code.append(_eol + _eol + "int main(int argc, char *argv[]) {"
                    + _eol);
            code.append(((FMIMACodeGeneratorAdapter) getAdapter(toplevel()))
                    .generateFMIMA());

            code.append(_eol + "return 0;" + _eol + "}");
        }
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate FMIMA and append it to the given string buffer.
     *  Write the code to the directory specified by the <i>codeDirectory</i>
     *  parameter.  The file name is a sanitized version of the model
     *  name with a suffix that is based on last package name of the
     *  <i>generatorPackage</i> parameter.  Thus if the
     *  <i>codeDirectory</i> is <code>$HOME</code>, the name of the
     *  model is <code>Foo</code> and the <i>generatorPackage</i>
     *  is <code>ptolemy.cg.kernel.generic.program.procedural.fmima</code>, then the file that is
     *  written will be <code>$HOME/Foo.html</code>
     *  This method is the main entry point to generate fmima
     *
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    @Override
    protected int _generateCode(StringBuffer code) throws KernelException {

        // Hint:  Look at ptolemy/cg/kernel/generic/program/procedural/c/CCodeGenerator.java

        code.append(comment("Generated from ptolemy/cg/kernel/generic/program/procedural/fmima/FMIMACodeGenerator.java _generateCode"));

        // Copy the .c and .h files from $PTII/ptolemy/actor/lib/fmi/ma.

        String directory = codeDirectory.stringValue();
        if (!directory.endsWith("/")) {
            directory += "/";
        }

        String directoryFmi = directory + "fmi/";

        if (new File(directoryFmi).mkdirs()) {
            if (!_includes.contains("-I " + directoryFmi)) {
                _includes.add("-I " + directoryFmi);
            }
        }

        String directoryFmiShared = directoryFmi + "shared/";

        if (new File(directoryFmiShared).mkdirs()) {
            if (!_includes.contains("-I " + directoryFmiShared)) {
                _includes.add("-I " + directoryFmiShared);
            }
        }
        _copyCFileTosrc("ptolemy/actor/lib/fmi/ma/shared/", directoryFmiShared,
                "sim_support.c");
        _copyCFileTosrc("ptolemy/actor/lib/fmi/ma/shared/", directoryFmiShared,
                "sim_support.h");

        String directoryFmiIncludes = directoryFmi + "includes/";
        if (new File(directoryFmiIncludes).mkdirs()) {
            if (!_includes.contains("-I " + directoryFmiIncludes)) {
                _includes.add("-I " + directoryFmiIncludes);
            }
        }
        _copyCFilesTosrc("ptolemy/actor/lib/fmi/ma/includes/",
                directoryFmiIncludes, new String[] { "fmi.h",
                        "fmiFunctionTypes.h", "fmiFunctions.h",
                        "fmiTypesPlatform.h" });

        String directoryFmiParser = directoryFmi + "parser/";
        if (new File(directoryFmiParser).mkdirs()) {
            if (!_includes.contains("-I " + directoryFmiParser)) {
                _includes.add("-I " + directoryFmiParser);
            }
        }
        _copyCFilesTosrc(
                "ptolemy/actor/lib/fmi/ma/parser/",
                directoryFmiParser,
                new String[] { "XmlElement.cpp", "XmlElement.h",
                        "XmlParserCApi.cpp", "XmlParserCApi.h",
                        "XmlParser.cpp", "XmlParserException.h", "XmlParser.h" });

        String directoryFmiParserLibxml = directoryFmi + "parser/libxml/";
        if (new File(directoryFmiParserLibxml).mkdirs()) {
            if (!_includes.contains("-I " + directoryFmiParserLibxml)) {
                _includes.add("-I " + directoryFmiParserLibxml);
            }
        }
        _copyCFilesTosrc("ptolemy/actor/lib/fmi/ma/parser/libxml/",
                directoryFmiParserLibxml, new String[] { "dict.h",
                        "encoding.h", "entities.h", "globals.h", "hash.h",
                        "list.h", "parser.h", "relaxng.h", "SAX2.h", "SAX.h",
                        "threads.h", "tree.h", "valid.h", "xlink.h",
                        "xmlautomata.h", "xmlerror.h", "xmlexports.h",
                        "xmlIO.h", "xmlmemory.h", "xmlreader.h", "xmlregexp.h",
                        "xmlschemas.h", "xmlstring.h", "xmlversion.h" });
        if (_executeCommands == null) {
            _executeCommands = new StreamExec();
        }

        // Writing the Makefile
        CompositeActor container = (CompositeActor) getContainer();
        _writeMakefile(container, directory);

        // Hopefully, we can skip the XML parsing because we have
        // already parsed the modelDescription.xml file.

        return super._generateCode(code);
    }

    /** Return the filter class to find adapters. All
     *  adapters have to extend this class.
     *  @return The base class for the adapters.
     */
    @Override
    protected Class<?> _getAdapterClassFilter() {
        return FMIMACodeGeneratorAdapter.class;
    }

    /** Read in a template makefile, substitute variables and write
     *  the resulting makefile.
     *
     *  <p>See {@link  ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator#_writeMakefile(CompositeEntity, String)}
     *  for a complete list of variables that are substituted.</p>
     *  <p>In this class, the following variables are substituted
     *  <dl>
     *  <dt><code>@PTCGPPCompiler@</code>
     *  <dd>The g++ compiler</dd>
     *  <dt><code>@PTCGCompler@</code>
     *  <dd>The gcc compiler</dd>
     *  </dl>
     *  @param container The composite actor for which we generate the makefile
     *  @param currentDirectory The director in which the makefile is to be written.
     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    @Override
    protected void _writeMakefile(CompositeEntity container,
            String currentDirectory) throws IllegalActionException {
        _substituteMap.put("@PTCGPPCompiler@", "g++");
        _substituteMap.put("@PTCGCompiler@", "gcc");

        _substituteMap.put("@PTCGLibraries@", _concatenateElements(_libraries));

        super._writeMakefile(container, currentDirectory);
    }
}
