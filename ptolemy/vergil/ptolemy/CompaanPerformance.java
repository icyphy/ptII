/* A performance object for Ptolemy models in vergil.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.vergil.ptolemy;

import ptolemy.vergil.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;

import ptolemy.vergil.graph.*;
import ptolemy.vergil.toolbox.*;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;

/** This class obtains particular performance values from a model in
 *  vergil. In a model, actors and relations can have variables that
 *  contain important performance metrics. These performance metrics
 *  can influence the way a model needs to be visualized in
 *  vergil. <p>
 *
 *  Performance metrics are typically the number of times an actor is
 *  fired, or the number of tokens being transported over a particular
 *  relation. This object collects there metrics from the individual
 *  actors and relations and derives from these values global
 *  performance values. <p>
 *
 *  These global performance values are used by CompaanNotation to
 *  color the relations and actors. This class provides a particular
 *  color schema, in which a high value gets a red color (from hot)
 *  and a low value gets a blue color (from begin cold). <p>
 *
 * @author Bart KIenhuis
 * @version $Id$
 */

public class CompaanPerformance {

    /** */
    public CompaanPerformance() {
        _haveGlobalInfo = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Get the color for an actor based on the variable value. The
        actual value of the variable is compared to the global
        performance metrics. Depending on its relative location to the
        minimum and maximum of a performance metrics, determine its
        color. 
        @param variable variable from a
        @return a color.
    */
    public Color getActorColor( Variable variable ) {
        if ( _haveGlobalInfo ) {
            double variableValue = _getVariableValue( variable );
            if ( variableValue > 0 ) {
                // Determine the color index in the color schema.
                int colorIndex = 
                    (int) Math.floor( 
                            (double)(variableValue - _min)/
                            (double)_bin);
                return (Color)colorList.get(new Integer(colorIndex));
            }
        }
        // The global performance metrics haven't been
        // determine yet, return the yellow color.
        return Color.yellow;
    }

    /** */
    public Color getRelationColor( Variable variable ) {
        if ( _haveGlobalInfo ) {
            double variableValue = _getVariableValue( variable );
            if ( variableValue > 0 ) {
                // Determine the color index in the color schema.
                int colorIndex = 
                    (int) Math.floor( 
                            (double)(variableValue - _min_comm)/
                            (double)_bin_comm);
                return (Color)colorList.get(new Integer(colorIndex));
            } 
        }
        // The global performance metrics haven't been
        // determine yet, return the yellow color.
        return Color.blue;                     
    }

    /** Extract global Performance information from a model given by
        its manager. This is done by traversing all the actors in the
        model in a recursive way. At each actor, a number of Variable
        values is check (e.g. fire). On the basis of these variable
        values, a global value like max and min are set. This is done
        before the model is rerendered by Vergil.  
        @param manager The manager of the model.
    */
    public void determineGlobalPerformanceMetrics(Manager manager) {

        if ( manager != null ) {
            // Get the list of actors from the manager for this
            // model
            List actorList = 
                ((CompositeActor)manager.getContainer()).deepEntityList();
            _getActorPerformanceMetrics( actorList );

            // Get the list of relations from the manager for this
            // model
            List relationList = 
                ((CompositeActor)manager.getContainer()).relationList();
            _getRelationPerformanceMetrics( relationList );
            
            // Give feedback on the found results
            System.out.println("\n Global Perfromance Info:");
            System.out.println(" max firings: " + _max );
            System.out.println(" min firings: " + _min );        
            System.out.println(" bin size: " + _bin);
            System.out.println("");
            System.out.println(" max communication: " + _max_comm );
            System.out.println(" min communication: " + _min_comm );        
            System.out.println(" bin size: " + _bin_comm);
                                    
            // flag that we have all info....
            _haveGlobalInfo = true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private double _getVariableValue(Variable variable) {
        if ( variable != null ) {
            try {
                Token token = variable.getToken();
                double tokenValue = 0;
                if ( token instanceof IntToken ) {
                    tokenValue = ((IntToken)token).intValue();
                }
                if ( token instanceof DoubleToken ) {
                    tokenValue = ((DoubleToken)token).doubleValue();
                }                     
                return tokenValue;
            } catch (IllegalActionException e) { 
                // if token cannot be processed...
                return 0;
            }    
        }      
        // Return 0, if variable is not defined yet.
        return 0;
    }

    /** Get the performance matrics from an actor */
    private void _getActorPerformanceMetrics(List actorList) {
        
        // Get the iterator over the linked list containing all the
        // actors
        Iterator entities = actorList.iterator();
            
        // Initialize the value
        _max = 0;
        _min = Integer.MAX_VALUE;
            
        // Walk through the model
        while(entities.hasNext()) {
            ComponentEntity a = (ComponentEntity)entities.next();
            if(a instanceof Actor) {
                Variable f = (Variable)a.getAttribute("ehrhart");
                double variableValue = _getVariableValue( f );
                if ( variableValue > _max ) {
                    _max = (int)variableValue;
                        }
                if ( variableValue < _min ) {
                    _min = (int)variableValue;
                }
            }
        }
        
         // determine the bin size...
        _bin = (_max - _min)/(colorList.size()-1);
    }

    /** Get performance metrics from the list of relation. Iterator
        through the list of relations, and check whether certain
        variables are present in the actors. If such variable is
        present, obtain its value and update the global performance
        metric.  
        @param relationList list containing the relations.
    */
    private void _getRelationPerformanceMetrics(List relationList) {

        // Initialize the value
        _max_comm = 0;
        _min_comm = Integer.MAX_VALUE;

        // Get the iterator over the linked list containing all the
        // relations
        Iterator j = relationList.iterator();        
        while( j.hasNext() ) {
            ComponentRelation cr = (ComponentRelation)j.next();
            //System.out.println("ComponentRelation: " + cr.toString());
            if(cr instanceof Relation) {
                Variable variable = (Variable)cr.getAttribute("communication");
                double variableValue = _getVariableValue( variable );
                if ( variableValue > _max_comm ) {
                    _max_comm = (int)variableValue;
                }
                if ( variableValue < _min_comm ) {
                    _min_comm = (int)variableValue;
                }
            }
        }

         // determine the bin size...
        _bin_comm = (_max_comm - _min_comm)/(colorList.size()-1);

    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** */
    private static Map colorList = new HashMap();

    /** */
    private int _max = 0;

    /** */
    private int _min = Integer.MAX_VALUE;

    /** */
    private int _bin = 1;

    /** */
    private int _max_comm = 0;

    /** */
    private int _min_comm = Integer.MAX_VALUE;

    /** */
    private int _bin_comm = 1;

    /** The global flag to determine if the global performance metrics
        have been collected.
    */
    private boolean _haveGlobalInfo = false;
    
    ///////////////////////////////////////////////////////////////////
    ////                         static functions                  ////

    static {
        colorList.put(new Integer(0), Color.blue.brighter() );
        colorList.put(new Integer(1), Color.blue );
        colorList.put(new Integer(2), Color.blue.darker() );
        colorList.put(new Integer(3), Color.cyan.brighter() );
        colorList.put(new Integer(4), Color.cyan );
        colorList.put(new Integer(5), Color.cyan.darker() );
        colorList.put(new Integer(6), Color.green.brighter() );
        colorList.put(new Integer(7), Color.green );
        colorList.put(new Integer(8), Color.green.darker() );
        colorList.put(new Integer(9), Color.yellow.brighter() );
        colorList.put(new Integer(10), Color.yellow );
        colorList.put(new Integer(11), Color.yellow.darker() );
        colorList.put(new Integer(12), Color.pink.brighter() );
        colorList.put(new Integer(13), Color.pink );
        colorList.put(new Integer(14), Color.pink.darker() );
        colorList.put(new Integer(15), Color.orange.brighter() );
        colorList.put(new Integer(16), Color.orange );
        colorList.put(new Integer(17), Color.orange.darker() );
        colorList.put(new Integer(18), Color.red.brighter() );
        colorList.put(new Integer(19), Color.red );        
        colorList.put(new Integer(20), Color.red.darker() );        
    }
}
