/* An attribute that compiles a model.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.domains.pn.kernel;

// Ptolemy imports.
import java.awt.Frame;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.graph.DirectedGraph;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ModelCompiler
/**
   This attribute is a visible attribute that when configured (by double
   clicking on it or by invoking Configure in the context menu) it generates
   Giotto code and displays it a text editor.  It is up to the user to save
   the Giotto code in an appropriate file, if necessary.

   @author Haiyang Zheng, Rachel Zhou
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (johnr)
*/

public class ModelCompiler extends Attribute {

    /** Construct a factory with the default workspace and "" as name.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ModelCompiler()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ModelCompiler(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate Giotto code for the given model.
     *  @return The Giotto code.
     */
    public void compileTypedCompositeActor(
        TypedCompositeActor compositeActor) throws IllegalActionException {
        try {
            ///////////////////////////////////////////////////////////
            // The recursion part.
            
            // Find each opaque composite actor and decompose
            // it into several composite actors if it is not sequential. 
            List opaqueCompositeActors 
                = _containedOpaqueCompositeActors(compositeActor);
            
            // Find all opaque composite actor, who do not have an attribute
            // saying "Atomic".
            Iterator opaqueCompositeActorsIterator  
                = opaqueCompositeActors.iterator();
            
            // Iterate each opaque composite actor to do depth-first-search
            while (opaqueCompositeActorsIterator.hasNext()) {
                TypedCompositeActor containedCompositeActor 
                = (TypedCompositeActor)opaqueCompositeActorsIterator.next();
                // recursively call compileTyedCompositeActor on the
                // cloned composite actor.
                compileTypedCompositeActor(containedCompositeActor);
            }
            
            ///////////////////////////////////////////////////////////
            // The condition the recursion ends and generates results.
            
            // Get the container of this composite actor.
            // NOTE: The container may be null if this composite actor
            // is at the top level.
            CompositeEntity container 
                = (CompositeEntity)compositeActor.getContainer();
            
            // Get the function dependency of the composite actor
            FunctionDependencyOfCompositeActor functionDependency
                = ((FunctionDependencyOfCompositeActor)
                    compositeActor.getFunctionDependency());
    
            // Get the detailed dependency graph of the composite actor
            DirectedGraph detailedDependencyGraph 
                = functionDependency.getDetailedDependencyGraph();
            
            // Get a list of subgraphs based on the dependency graph 
            List listOfSubgraphs = detailedDependencyGraph.subgraphs();
            
            // If the number of subgraphs is 1, there is nothing to do.
            if (listOfSubgraphs.size() == 1) {
                return;
            }
            
            // FIXME: if the composite actor is at top level, group actors 
            // by introducing a new layer of hierarchy.
            if (container == null) {
                //wrap up and return;
                return;
            }
            
            ///////////////////////////////////////////////////////////
            // Decompose this composite actor with its dependency graph. 
            
            // Create a sub-composite actor for each subgraph. 
            for (int i=0; i < listOfSubgraphs.size(); i++) {
                
                TypedCompositeActor clone = 
                    (TypedCompositeActor)compositeActor.clone();
                // NOTE: make the name of the composite actor unique.
                clone.setName("compiled" 
                    + compositeActor.getName() + "_" + i);

                // The following code is unnecessary if the above FIXME
                // is fixed.
//                // Associate the cloned composite actor with the 
//                // upper level composite actor if it is not null.
//                // Otherwise, associate the cloned composite actor with 
//                // this composite actor.
//                // NOTE: If the container is null, a new layer in hierarchy
//                // is introduced. Otherwise, the layers of hierarchy are
//                // kept the same.
//                if (container == null) {
//                    clone.setContainer(compositeActor);
//                } else {
//                    clone.setContainer(container);
//                }
                
                clone.setContainer(container);
                DirectedGraph subgraph = (DirectedGraph)listOfSubgraphs.get(i);
                
                // remove all the entities whose ports are not included
                // as nodes in the subgraph
                List entitiesOfClone = clone.deepEntityList();
                List entitiesOfOrigin 
                    = compositeActor.deepEntityList();
                // NOTE: we have to play the ports backwards. Otherwise,
                // a change to the port lists will affect the order of the
                // ports that have not been processed. This invalids the 
                // following algorithm. 
                // DATE: Less then 23 hours from the project deadline 
                // on 11/18/2004. 
                for (int j=entitiesOfClone.size()-1; j>=0; j--) {
                    ComponentEntity cloneEntity 
                        = (ComponentEntity)entitiesOfClone.get(j);
                    ComponentEntity originalEntity 
                        = (ComponentEntity)entitiesOfOrigin.get(j);
                    Iterator portsOfOriginalEntity 
                        = originalEntity.portList().listIterator();
                    boolean originalEntityInSubgraph = false;
                    while (portsOfOriginalEntity.hasNext() 
                        && !originalEntityInSubgraph) {
                        IOPort portOfOriginalEntity 
                            = (IOPort)portsOfOriginalEntity.next();
                        if (subgraph.containsNodeWeight(
                                portOfOriginalEntity)) {
                            originalEntityInSubgraph = true;
                        }
                    }
                    if (!originalEntityInSubgraph) {
                        // remove all the relations of this entity 
                        // from the cloned composite actor.
                        Iterator relations = 
                            cloneEntity.linkedRelationList().listIterator();
                        while (relations.hasNext()) {
                            IORelation relation 
                                = (IORelation)relations.next();
                            relation.setContainer(null);
                        }
                        // remove the entity from the cloned composite actor.
                        cloneEntity.setContainer(null);
                    }
                }
                
                // NOTE: we assume that the number of ports is the same 
                // for both the clone and the original actor. 
                List portsOfClone = clone.portList();
                List portsOfOrigin = compositeActor.portList();

                // remove all the ports that are not 
                // included in the subgraph and connect the ports in 
                // the subgraph to outside
                // NOTE: we have to play the ports backwards. Otherwise,
                // a change to the port lists will affect the order of the
                // ports that have not been processed. This invalids the 
                // following algorithm. 
                // NOTE: Less then 23 hours from the project deadline 
                // on 11/18/2004. 
                for (int j=portsOfClone.size()-1; j>=0; j--) {
                    IOPort portOfClone = (IOPort)portsOfClone.get(j);
                    IOPort portOfOrigin = (IOPort)portsOfOrigin.get(j);
                    if (!subgraph.containsNodeWeight(portOfOrigin)) {
                        portOfClone.setContainer(null);
                    } else {
                        Iterator relations = 
                            portOfOrigin.linkedRelationList().iterator();
                        while (relations.hasNext()) {
                            IORelation relation 
                                = (IORelation)relations.next();
                            portOfClone.link(relation);
                        }
                    }
                }
           }
            //remove this composite actor 
            compositeActor.setContainer(null);
            // Construct function dependency graph for the new model.
            functionDependency = (FunctionDependencyOfCompositeActor)
                ((CompositeActor)container).getFunctionDependency();
            functionDependency.getDetailedDependencyGraph();
            //System.out.println(container.exportMoML());
        } catch (KernelException ex) {
            System.out.println(ex.getMessage());
            throw new IllegalActionException(ex.getMessage());
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    /** Generate Giotto code for the given model.
     *  @return The Giotto code.
     */
    public TypedCompositeActor compileModel(TypedCompositeActor oldModel) 
        throws IllegalActionException {
        try {
            // Use a new name for the compiled model.
            String containerName = "compiled" + oldModel.getName();
            
            // Construct a new workspace.
            Workspace workspace = new Workspace("newWorkspace");
            
            // Clone the old model into the above workspace
            TypedCompositeActor modelClone 
                = (TypedCompositeActor)oldModel.clone(workspace);
            
            // Compile the cloned model.
            compileTypedCompositeActor(modelClone);
            
            // return the compiled model.
            return modelClone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } catch (KernelException ex) {
            System.out.println(ex.getMessage());
            throw new IllegalActionException(ex.getMessage());
        }
        return null; 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Method to instantiate the Editor Factory class called from the
     *  constructor. The reason for having this is that it can be
     *  overridden by subclasses
     */
    protected void _instantiateEditorFactoryClass()
        throws IllegalActionException, NameDuplicationException {
        new ModelCompilerEditorFactory(this, "_editorFactory");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    private void _init()
        throws IllegalActionException, NameDuplicationException {
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>"
                + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ncompile model.</text></svg>");

        _instantiateEditorFactoryClass();

        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);
    }

    // Return a list of opaque composite actors contained by the model.
    private List _containedOpaqueCompositeActors(TypedCompositeActor model) {
        LinkedList opaqueCompositeActors = new LinkedList();
        Iterator actors = model.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (actor instanceof CompositeActor) {
                // FIXME: This does not handle class definition.
                // && !((InstantiableNamedObj) actor).isClassDefinition()) {
                Attribute attribute = ((NamedObj) actor).getAttribute("Atomic");
                if (attribute == null) {
                    opaqueCompositeActors.add(actor);
                }
            }
        }
        return opaqueCompositeActors;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    protected class ModelCompilerEditorFactory extends EditorFactory {

        public ModelCompilerEditorFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create an editor for configuring the specified object with the
         *  specified parent window.
         *  @param object The object to configure.
         *  @param parent The parent window, or null if there is none.
         */
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Configuration configuration
                    = ((TableauFrame)parent).getConfiguration();

                TypedCompositeActor model = (TypedCompositeActor)
                    ModelCompiler.this.getContainer();

                // Preinitialize and resolve types.
                CompositeActor toplevel = (CompositeActor)model.toplevel();
                Manager manager = toplevel.getManager();
                if (manager == null) {
                    manager = new Manager(
                            toplevel.workspace(), "manager");
                    toplevel.setManager(manager);
                }

                manager.preinitializeAndResolveTypes();

                TextEffigy codeEffigy = TextEffigy.newTextEffigy(
                        configuration.getDirectory(),
                        compileModel(model).exportMoML());
                codeEffigy.setModified(true);
                configuration.createPrimaryTableau(codeEffigy);
                
                // end the model execution.
                manager.stop();
                manager.wrapup();
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                        "Cannot generate code. Perhaps outside Vergil?");
            } finally {
            }
            
        }
    }
}
