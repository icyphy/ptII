/* Transform Actors using Soot

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/


package ptolemy.copernicus.jhdl;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.java.*;
import ptolemy.copernicus.kernel.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
//import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.*;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.dava.*;
import soot.util.*;

//////////////////////////////////////////////////////////////////////////
//// Main
/**

@author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class Main extends KernelMain {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read in a MoML model.
     *  @param args The first element of the array is the MoML class
     *  name or file name, subsequent optional arguments are Soot
     *  command line options, see the superclass documentation for details.
     *  @exception IllegalActionException If the model cannot be parsed.
     */
    public Main(String [] args) throws IllegalActionException {
        // args[0] contains the MoML class name.
        super(args[0]);
    }

    /** Add transforms to the Scene.
     */
    public void addTransforms() {
        //super.addTransforms();

        Pack pack = PackManager.v().getPack("wjtp");

        // Set up a watch dog timer to exit after a certain amount of time.
        // For example, to time out after 5 minutes, or 300000 ms:
        // -p wjtp.watchDog time:30000
        addTransform(pack, "wjtp.watchDog", WatchDogTimer.v(),
                "time:" + _watchDogTimer);

        // Create a class for the composite actor of the model, and for
        // all actors referenced by the model.
        addTransform(pack, "wjtp.mt", ModelTransformer.v(_toplevel),
                     "targetPackage:" + _targetPackage);
        

        // Inline the director into the composite actor.
        addTransform(pack, "wjtp.idt",InlineDirectorTransformer.v(_toplevel),
               "targetPackage:" + _targetPackage + 
               " outDir:" + _outputDirectory);


        // Add a command line interface (i.e. Main)
       addTransform(pack, "wjtp.clt",
                    CommandLineTransformer.v(_toplevel),
                    "targetPackage:" + _targetPackage);
       
       addTransform(pack, "wjtp.ta1",
               new TransformerAdapter(TypeAssigner.v()));
       addStandardOptimizations(pack,1);

       if(_snapshots) {
           addTransform(pack,"wjtp.snapshot1",
                   JimpleWriter.v(), "outDir:" + _outputDirectory +
                   "/jimple1");
       }

       addTransform(pack, "wjtp.ib1", InvocationBinder.v());
       
       addTransform(pack, "wjtp.ls7",
                        new TransformerAdapter(LocalSplitter.v()));
       addTransform(pack, "wjtp.ffet",
                        FieldsForEntitiesTransformer.v(_toplevel),
               "targetPackage:" + _targetPackage);

        // Infer the types of locals again, since replacing attributes
        // depends on the types of fields
       addTransform(pack, "wjtp.ta2",
                    new TransformerAdapter(TypeAssigner.v()));
       addTransform(pack, "wjtp.cie1",
               new TransformerAdapter(
                       CastAndInstanceofEliminator.v()));
       addStandardOptimizations(pack, 2);

        // In each actor and composite actor, ensure that there
        // is a field for every attribute, and replace calls
        // to getAttribute with references to those fields.
       addTransform(pack, "wjtp.ffat1",
               FieldsForAttributesTransformer.v(_toplevel),
               "targetPackage:" + _targetPackage);
        // In each actor and composite actor, ensure that there
        // is a field for every port, and replace calls
        // to getPort with references to those fields.
       addTransform(pack, "wjtp.ffpt",
                    FieldsForPortsTransformer.v(_toplevel),
                    "targetPackage:" + _targetPackage);

       addTransform(pack, "wjtp.ls2",
               new TransformerAdapter(LocalSplitter.v()));
       addTransform(pack, "wjtp.lns",
               new TransformerAdapter(LocalNameStandardizer.v()));
       addStandardOptimizations(pack, 3);
       
       addTransform(pack, "wjtp.ls3",
               new TransformerAdapter(LocalSplitter.v()));
       addTransform(pack, "wjtp.ta3",
               new TransformerAdapter(TypeAssigner.v()));
       addTransform(pack, "wjtp.ib2",
                    InvocationBinder.v());

        // Set about removing reference to attributes and parameters.
        // Anywhere where a method is called on an attribute or
        // parameter, replace the method call with the return value
        // of the method.  This is possible, since after
        // initialization attribute values are assumed not to
        // change.  (Note: There are certain cases where this is
        // not true, i.e. the expression actor.  Those will be
        // specially handled before this point, or we should detect
        // assignments to attributes and handle them differently.)
       addTransform(pack, "wjtp.iat",
               InlineParameterTransformer.v(_toplevel),
               "targetPackage:" + _targetPackage);

        // Anywhere we have a method call on a token that can be
        // statically evaluated (usually, these will have been
        // created by inlining parameters), inline those calls.
        // We do this before port transformation, since it
        // often allows us to statically determine the channels
        // of port reads and writes.
       addTransform(pack, "wjtp.itt1",
                    InlineTokenTransformer.v(_toplevel),
                    "targetPackage:" + _targetPackage);
       addTransform(pack, "wjtp.ls4",
               new TransformerAdapter(LocalSplitter.v()));
       
        // While we still have references to ports, use the
        // resolved types of the ports and run a typing
        // algorithm to specialize the types of domain
        // polymorphic actors.  After this step, no
        // uninstantiable types should remain.
       addTransform(pack, "wjtp.tie",
                    new TransformerAdapter(TokenInstanceofEliminator.v()));
       addTransform(pack, "wjtp.ta4",
               new TransformerAdapter(TypeAssigner.v()));
       addTransform(pack, "wjtp.cp1",
               new TransformerAdapter(CopyPropagator.v()));

       
       if(_snapshots) {
           addTransform(pack, "wjtp.snapshot2", ClassWriter.v(),
                   "outDir:" + _outputDirectory + "/jimple2");
           addTransform(pack, "wjtp.snapshot2jimple", JimpleWriter.v(),
                   "outDir:" + _outputDirectory + "/jimple2");
       }

       addTransform(pack, "wjtp.ta5",
               new TransformerAdapter(TypeAssigner.v()));
       addTransform(pack, "wjtp.nee",
               NamedObjEqualityEliminator.v(_toplevel),
               "targetPackage:" + _targetPackage);

        // Remove casts and instanceof Checks.
       addTransform(pack, "wjtp.cie2",
               new TransformerAdapter(
                                      CastAndInstanceofEliminator.v()));
       addStandardOptimizations(pack, 4);

        // Anywhere we have a method call on a token that can be
        // statically evaluated (usually, these will have been
        // created by inlining parameters), inline those calls.
        // We do this before port transformation, since it
        // often allows us to statically determine the channels
        // of port reads and writes.
       addTransform(pack, "wjtp.itt2",
               InlineTokenTransformer.v(_toplevel),
               "targetPackage:" + _targetPackage);
       addTransform(pack, "wjtp.ipt",
               InlinePortTransformer.v(_toplevel),
               "targetPackage:" + _targetPackage);


       addStandardOptimizations(pack, 5);

        // Unroll loops with constant loop bounds.
//          Scene.v().getPack("jtp").add(
//                  new Transform("jtp.clu",
//                          ConstantLoopUnroller.v()));
       addTransform(pack, "wjtp.finalSnapshotJimple",
                    JimpleWriter.v(),
                    "outDir:" + _outputDirectory);
       
       addTransform(pack, "wjtp.circuit",
                    ptolemy.copernicus.jhdl.CircuitTransformer.v(_toplevel),
                    "JHDL");
       

    }

    /** Read in a MoML model, generate .class files for use with JHDL.
     *  @exception IllegalActionException If the model cannot be parsed.
     *  @exception NameDuplicationException If the name of the
     *  model cannot be changed to a Java identifier String.
     */
    public static void main(String[] args)
            throws IllegalActionException, NameDuplicationException {
        Main main = new Main(args);

        // Parse the model.
        CompositeActor toplevel = main.readInModel(args[0]);

        // Create instance classes for the actors.
        main.initialize(toplevel);

        // Parse any copernicus args.
        String[] sootArgs = _parseArgs(args);
 
        // Add Transforms to the Scene.
        main.addTransforms();

        // Generate Code
        main.generateCode(sootArgs);
    }

    /** Parse any Copernicus arguments.
     */ 
    protected static String[] _parseArgs(String args[]) {
        // Ignore the first argument.
        for(int i = 1; i < args.length; i++) {
            if(args[i].equals("-snapshots")) {
                _snapshots = true;
            } if(args[i].equals("-targetPackage")) {
                i++;
                if(i < args.length) {
                    _targetPackage = args[i];
                } else {
                    throw new RuntimeException(
                            "Expected argument to -targetPackage");
                }
//             } else if(args[i].equals("-templateDirectory")) {
//                 i++;
//                 if(i < args.length) {
//                     _templateDirectory = args[i];
//                 } else {
//                     throw new RuntimeException(
//                             "Expected argument to -templateDirectory");
//                 }
            } else if(args[i].equals("-watchDogTimer")) {
                i++;
                if(i < args.length) {
                    _watchDogTimer = args[i];
                } else {
                    throw new RuntimeException(
                            "Expected argument to -watchDogTimer");
                }
            } else if(args[i].equals("-outputDirectory")) {
                i++;
                if(i < args.length) {
                    _outputDirectory = args[i];
                } else {
                    throw new RuntimeException(
                            "Expected argument to -outputDirectory");
                }
        //     } else if(args[i].equals("-generatorAttributeFileName")) {
//                 i++;
//                 if(i < args.length) {
//                     _generatorAttributeFileName = args[i];
//                 } else {
//                     throw new RuntimeException(
//                            "Expected argument to -generatorAttributeFileName");
//                 }
            } else if(args[i].equals("-sootArgs")) {
                String[] sootArgs = new String[args.length - i];
                i++;
                sootArgs[0] = args[0];
                for(int j = 1; j < sootArgs.length; j++) {
                    sootArgs[j] = args[i++];
                }
                return sootArgs;
            }
        }   
        
        // Default args to soot, if no -sootArgs is found.
        String[] sootArgs = new String[1];
        sootArgs[0] = args[0];
        return sootArgs;
    }

    private static boolean _snapshots = false;
    private static String _watchDogTimer = "unsetParameter";
    private static String _targetPackage = "unsetParameter";
    private static String _outputDirectory = "unsetParameter";
    
}
