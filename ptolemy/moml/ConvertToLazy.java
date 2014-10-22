/* An application that executes non-graphical
 models specified on the command line.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.moml;

import java.io.File;
import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.moml.filter.BackwardCompatibility;

///////////////////////////////////////////////////////////////////
//// ConvertToLazy

/** Read a specified MoML file and convert all instances of
 *  TypedCompositeActor that contain more than a specified
 *  number of entities to LazyTypedCompositeActor. The
 *  converted model's MoML is produced on standard out.
 *  To use this on the command line, invoke as follows:
 *  <pre>
 *     $PTII/bin/convertToLazy inputMoML.xml <i>numberOfEntities</i> &gt; outputMoML.xml
 *  </pre>
 *  or
 *  <pre>
 *     java -classpath $PTII ptolemy.moml.ConvertToLazy inputMoML.xml <i>numberOfEntities</i> &gt; outputMoML.xml
 *  </pre>
 *  If the <i>numberOfEntities</i> argument is not supplied, then it
 *  defaults to 100.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (eal)
 */
public class ConvertToLazy implements ChangeListener {

    /** Parse the xml file and convert it.
     *  @param xmlFileName A string that refers to an MoML file that
     *  contains a Ptolemy II model.  The string should be
     *  a relative pathname.
     *  @param threshold The number of contained entities that a composite
     *   should have to be converted to a lazy composite.
     *  @exception Throwable If there was a problem parsing
     *  or running the model.
     */
    public ConvertToLazy(String xmlFileName, int threshold) throws Throwable {
        MoMLParser parser = new MoMLParser();

        // Save the current MoMLFilters before conversion so that if
        // we call this class from within a larger application, we don't
        // change the filters.
        List oldFilters = MoMLParser.getMoMLFilters();
        try {
            // The test suite calls UseLazyCompositeApplication multiple times,
            // and the list of filters is static, so we reset it each time
            // so as to avoid adding filters every time we run an auto test.
            // We set the list of MoMLFilters to handle Backward Compatibility.
            MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

            // If there is a MoML error, then throw the exception as opposed
            // to skipping the error.  If we call StreamErrorHandler instead,
            // then the nightly build may fail to report MoML parse errors
            // as failed tests
            //parser.setErrorHandler(new StreamErrorHandler());
            // We use parse(URL, URL) here instead of parseFile(String)
            // because parseFile() works best on relative pathnames and
            // has problems finding resources like files specified in
            // parameters if the xml file was specified as an absolute path.
            TypedCompositeActor toplevel = (TypedCompositeActor) parser.parse(
                    null, new File(xmlFileName).toURI().toURL());
            convert(toplevel, threshold);

            // We export and then reparse and then export again so
            // that the resulting MoML has the <configure>
            // ...</configure> blocks.  If just exportMoML, then the moml will
            // not have the <configure> ...</configure> blocks because the
            // LazyTypedCompositeActor._exportMoMLContents() method is not
            // called.  See ConvertToLazy-1.1 in test/ConvertToLazy.tcl
            // where we check that the moml has "configure" in it.
            String moml = toplevel.exportMoML();
            parser.resetAll();
            toplevel = (TypedCompositeActor) parser.parse(moml);
            System.out.println(toplevel.exportMoML());

        } finally {
            MoMLParser.setMoMLFilters(oldFilters);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Public methods                    ////

    /** React to a change request has been successfully executed by
     *  doing nothing. This method is called after a change request
     *  has been executed successfully.  In this class, we
     *  do nothing.
     *  @param change The change that has been executed, or null if
     *   the change was not done via a ChangeRequest.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
    }

    /** React to a change request that has resulted in an exception.
     *  This method is called after a change request was executed,
     *  but during the execution in an exception was thrown.
     *  This method throws a runtime exception with a description
     *  of the original exception.
     *  @param change The change that was attempted or null if
     *   the change was not done via a ChangeRequest.
     *  @param exception The exception that resulted.
     */
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        // If we do not implement ChangeListener, then ChangeRequest
        // will print any errors to stdout and continue.
        // This causes no end of trouble with the test suite
        // We can't throw an Exception here because this method in
        // the base class does not throw Exception.
        String description = "";

        if (change != null) {
            description = change.getDescription();
        }

        System.err.println("UseLazyCompositeApplication.changeFailed(): "
                + description + " failed:\n" + exception);
    }

    /** Convert the model.
     *  @param actor The model to convert.
     *  @param threshold The threshold to use.
     */
    public void convert(TypedCompositeActor actor, int threshold) {
        List<ComponentEntity> entities = actor.entityList();
        for (ComponentEntity entity : entities) {
            if (entity instanceof TypedCompositeActor) {
                // Do the conversion depth-first.
                convert((TypedCompositeActor) entity, threshold);
                if (entity.getClassName().equals(
                        "ptolemy.actor.TypedCompositeActor")
                        && count((TypedCompositeActor) entity) >= threshold) {
                    entity.setClassName("ptolemy.actor.LazyTypedCompositeActor");
                }
            }
        }
        //         List<ComponentEntity> classDefinitions = actor.classDefinitionList();
        //         for (ComponentEntity classDefinition : classDefinitions) {
        //             if (classDefinition instanceof TypedCompositeActor) {
        //                 // Do the conversion depth-first.
        //                 convert((TypedCompositeActor) classDefinition, threshold);
        //                 if (classDefinition.getClassName().equals(
        //                         "ptolemy.actor.TypedCompositeActor")
        //                         && count((TypedCompositeActor) classDefinition) >= threshold) {
        //                     classDefinition
        //                             .setClassName("ptolemy.actor.LazyTypedCompositeActor");
        //                 }
        //             }
        //         }

    }

    /** Count the number of contained entities that have not already been made
     *  lazy.
     *  @param actor The actor to count.
     *  @return The number of contained entities (deeply) that are not already
     *   lazy.
     */
    public int count(TypedCompositeActor actor) {
        int result = 0;
        List<ComponentEntity> entities = actor.entityList();
        for (ComponentEntity entity : entities) {
            result++;
            if (entity instanceof TypedCompositeActor
                    && !entity.getClassName().equals(
                            "ptolemy.actor.lib.LazyTypedCompositeActor")) {
                result += count((TypedCompositeActor) entity);
            }
        }
        return result;
    }

    /** Create an instance of a model and convert it.
     *  @param args The command-line arguments providing the number
     *   of entities threshold and naming the .xml file to convert.
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Usage: FIXME  MoMLFile <numberOfEnties>\n");
                return;
            }
            if (args.length == 1) {
                new ConvertToLazy(args[0], 10);
                return;
            }
            int threshold = Integer.parseInt(args[1]);
            new ConvertToLazy(args[0], threshold);
        } catch (Throwable ex) {
            System.err.println("Command failed: " + ex);
            ex.printStackTrace();
        }
    }
}
