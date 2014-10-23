/* A transformer that writes an Interpreted version of the model.

 Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.copernicus.interpreted;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.util.StringUtilities;
import soot.HasPhaseOptions;
import soot.PhaseOptions;
import soot.SceneTransformer;

/**
 A transformer that writes an interpreted version of a model.
 This transformer is very similar to 'Save As'.  This transformer is
 used as a control case to compare the effects of other code
 generators against a purely interpreted (non-code generation) run.
 This transformer is the smallest standalone Ptolemy II transformer.

 <p>For a model called Foo, we generate Foo.xml,
 in the directory named by the outputDirectory parameter.


 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class InterpretedWriter extends SceneTransformer implements
HasPhaseOptions {
    /** Construct a new transformer
     */
    private InterpretedWriter(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     * the given model. The model is assumed to already have been
     * properly initialized so that resolved types and other static
     * properties of the model can be inspected.
     * @param model The model that this class will operate on.
     * @return An instance of the AppletWriter transformer.
     */
    public static InterpretedWriter v(CompositeActor model) {
        return new InterpretedWriter(model);
    }

    @Override
    public String getDefaultOptions() {
        return "";
    }

    @Override
    public String getDeclaredOptions() {
        return "targetPackage outputDirectory";
    }

    @Override
    public String getPhaseName() {
        return "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Save the model as a .xml file
     *  <p>For example, if the model is called MyModel, and
     *  this phase is called with:
     *  <pre>
     *        -p wjtp.interpretedWriter targetPackage:foo.bar
     *  </pre>
     *  Then we will create the directory $PTII/foo/bar/MyModel and
     *  place MyModel.xml in that directory.
     *
     *  @param phaseName The name of the phase, for example
     *  <code>wjtp.interpretedWriter</code>.
     *  @param options The options Map.  This method uses the
     *  <code>targetPackage</code> option to specify package
     *  to generate code in.
     */
    @Override
    protected void internalTransform(String phaseName, Map options) {
        // FIXME: Perhaps AppletWriter should call this transformer?
        System.out.println("InterpretedWriter.internalTransform(" + phaseName
                + ", " + options + ")");

        _outputDirectory = PhaseOptions.getString(options, "outputDirectory");

        // If the targetPackage is foo.bar, and the model is Bif,
        // the we will do mkdir $PTII/foo/bar/Bif/
        //_targetPackage = PhaseOptions.getString(options, "targetPackage");

        _sanitizedModelName = StringUtilities.sanitizeName(_model.getName());

        // Create the directory where we will create the files.
        File outputDirectoryFile = new File(_outputDirectory);

        if (outputDirectoryFile.isDirectory()) {
            System.out.println(" Warning: '" + outputDirectoryFile
                    + "' already exists.");
        }

        if (!outputDirectoryFile.isDirectory()) {
            if (!outputDirectoryFile.mkdirs()) {
                throw new InternalErrorException(
                        "Failed to create directory \"" + outputDirectoryFile
                        + "\"");
            }
        }

        // Generate the .xml file.
        String modelFileName = _outputDirectory + "/" + _sanitizedModelName
                + ".xml";
        System.out.println("InterpretedWriter: about to write '"
                + modelFileName + "'");

        Writer modelFileWriter = null;
        try {
            modelFileWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(modelFileName)));
            _model.exportMoML(modelFileWriter);
        } catch (IOException ex) {
            throw new InternalErrorException("Problem writing '"
                    + modelFileName + "': " + ex);
        } finally {
            if (modelFileWriter != null) {
                try {
                    modelFileWriter.close();
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to close "
                            + modelFileName, ex);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The model we are generating code for.
    private CompositeActor _model;

    // The full path to the directory where we are creating the model
    private String _outputDirectory;

    // The sanitized modelName
    private String _sanitizedModelName;

    // The parent package relative to $PTII to generate the code in
    // The code itself is generated in a child package of the parent package
    // with the same name as the model.  So if the _targetPackage
    // is foo.bar, and the model is MyModel, we will create the code
    // in foo.bar.MyModel.
    //private String _targetPackage;
}
