/** A Factor Oracle (FO) builder from an input sequence.

Copyright (c) 2013 The Regents of the University of California.
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
package org.ptolemy.machineImprovisation;

 
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLChangeRequest;



/** A Factor Oracle (FO) builder from an input sequence. Given an input
 * of type String, recognizes a set of alphabet by recognizing the
 * distinct characters in the symbol and builds a factor oracle data
 * structure.
 * 
 * <p>In the future, this actor will be replaced by an on-line algorithm
 * which adds to the data structure as more symbols are received at
 * the input and the string requirement will be replaced by a music
 * sequence specification.</p>
 * 
 * <p>This actor builds a factor oracle data structure that represents
 * a finite acyclic automaton that contains at least all the suffixes of
 * a given input sequence.</p>

 @author Ilge Akkaya
 @version  $Id$
 @since Ptolemy II 10.1
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating 
 */
public class FactorOracleGenerator extends TypedAtomicActor {
   /** Construct an actor with the given container and name.
    *  @param container The container.
    *  @param name The name of this actor
    *  @exception IllegalActionException If the actor cannot be contained
    *   by the proposed container.
    *  @exception NameDuplicationException If the container already has an
    *   actor with this name.
    */
   public FactorOracleGenerator(CompositeEntity container, String name)
           throws NameDuplicationException, IllegalActionException {
       super(container, name);
       
       pitchSequence = new TypedIOPort(this, "input", true, false);
       pitchSequence.setTypeEquals(BaseType.INT);
       
       durationSequence = new TypedIOPort(this, "durations", true, false);
       durationSequence.setTypeEquals(BaseType.INT);
       
       repetitionProbability = new Parameter(this, "repetitionFactor");
       repetitionProbability.setTypeEquals(BaseType.DOUBLE);
       repetitionProbability.setExpression("0.9");
       
       isPitchOracle = new Parameter(this, "isPitchOracle");
       isPitchOracle.setTypeEquals(BaseType.BOOLEAN);
       isPitchOracle.setExpression("TRUE");
       
       _adjacencyList = new HashMap<Integer,List<Integer>>();
       _adjacencyListSymbols = new HashMap<Integer,List<Character>>(); 
       
       _FOindex=0;
       _pitchSequence = new LinkedList();
       _durationSequence = new LinkedList();
      
   }
   
   public void attributeChanged(Attribute attribute)
           throws IllegalActionException {
       if(attribute == repetitionProbability){
           double token = ((DoubleToken)repetitionProbability.getToken()).doubleValue();
           if( token > 1.0 || token < 0.0){
               throw new IllegalActionException(this, "Repetition factor must be in range [0.0,1.0].");
           }
           _repetitionFactor = token;
           
       }
           super.attributeChanged(attribute);
   }
   

   ///////////////////////////////////////////////////////////////////
   ////                         public variables                  ////

   public TypedIOPort pitchSequence;
   
   public TypedIOPort durationSequence;

   public Parameter repetitionProbability;
   
   public Parameter isPitchOracle;

   

   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////

   // When fired, 
   
   public void fire() throws IllegalActionException {
       
       super.fire();
       
       if( durationSequence.hasToken(0)){
            int incomingDuration = ((IntToken)durationSequence.get(0)).intValue();
            _durationSequence.add(incomingDuration);
       }
       
       // pitch sequence triggers the generation, if any
       if ( pitchSequence.hasToken(0)){
       
           int incomingNote =((IntToken)pitchSequence.get(0)).intValue();
           
           //if(_pitchSequence != null && _pitchSequence.size() > 0){
           int m = _pitchSequence.size();
           int n = _durationSequence.size();
           boolean isPitch = ((BooleanToken)(isPitchOracle.getToken())).booleanValue();
           
           if(incomingNote <= 36 && m >0 && n > 0){
               try{
                       // pitch oracle
                       String appendPitch = "P" ;
                       String fullName = "FO_" + appendPitch +_FOindex;
                       _constructNewFactorOracle(fullName, true);
                       
                       // duration oracle
                       appendPitch = "D" ;
                       fullName = "FO_" + appendPitch +_FOindex;
                       _constructNewFactorOracle(fullName, false);
                   
               }catch(NameDuplicationException e){
                   System.err.println("NameDuplicationException at FactorOracleGenerator");
               }
           } else if(incomingNote >= 5000){
               // long notes and a duration of 5s trigger FO generation
               try{
                   if( m > 1){
                       
                       String appendPitch = "D" ;
                       String fullName = "FO_" + appendPitch +_FOindex;
                       _constructNewFactorOracle(fullName, false);
                   }      
               }catch(NameDuplicationException e){
                   System.err.println("NameDuplicationException at FactorOracleGenerator");
               }
               
           }else{
               
                   _pitchSequence.add(incomingNote); //.toString();
                
               }
               //}
           
       }
 }
       
       
   private void _constructNewFactorOracle(String fullName, boolean isPitch) throws NameDuplicationException, IllegalActionException{
       // TODO: need a naming convention
       FactorOracle fo;
       Double horizontal;
       if(isPitch){
            fo =new FactorOracle((CompositeEntity)this.getContainer(), 
               fullName, _pitchSequence.toArray(), _repetitionFactor, isPitch);
            horizontal = 200.0;
       }else{
            fo =new FactorOracle((CompositeEntity)this.getContainer(), 
                   fullName, _durationSequence.toArray(), _repetitionFactor, isPitch);
            horizontal = 250.0;
       }
        
       Double vertical = _FOindex*100.0+300.0;
       String changeExpression = "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{" 
               + horizontal.toString() + "," + vertical.toString() + "}\"></property>";
       MoMLChangeRequest change = new MoMLChangeRequest(this, fo, changeExpression);
       requestChange(change);
       
       //Display d = new Display((CompositeEntity)this.getContainer(),"Display_"+_FOindex);
       //horizontal += 100.0;
       //changeExpression = "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{" 
       //        + (horizontal).toString() + "," + vertical.toString() + "}\"></property>";
       //change = new MoMLChangeRequest(this, d, changeExpression);
       //requestChange(change);
       //TypedIORelation r = new TypedIORelation(this._workspace);
       //r.setName("Relation_"+_inputSequence);
       //r.setContainer((CompositeEntity)this.getContainer());
       
       //fo.getPort("output").link(r);
       //d.getPort("input").link(r);
       
       // empty the factor oracle
       if(isPitch){
           _pitchSequence.clear();
       }else{
           _durationSequence.clear();
           _FOindex ++;
       }
    
}

protected List<Integer> _getTransitionsFrom( Integer node){
       List<Integer> _transitions = (List<Integer>)_adjacencyList.get(node);
       return _transitions;
   }
   /* Find the (unique) path that produces a given prefix and ret durn the node index which terminates the 
    * desired string sequence. Return null if no such path is found. Implements depth-first search on the
    * adjacency list
    * 
    * */
   
   
   /* Method that computes the shortest path ( minimum number of hops) between two nodes in the factor oracle
    * graph (Djkstra).
    * */
   private String minimalLengthString(int start, int end){
       
       // function implementation based on ptII/doc/tutorial/graph/ShortestPathFinder.java
       boolean [] visited = new boolean[_adjacencyList.size()+1];
       // initial distances
       int[] distance = new int[_adjacencyList.size()+1];
       
       for(int i = 0; i < _adjacencyList.size(); i++){
           distance[i] = Integer.MAX_VALUE;
           visited[i] = false;
       }
       
       distance[start] = 0;
       visited[start] = true;
       
       Queue<Integer> q = new LinkedList<Integer>();
       HashMap<Integer,Integer> prevHop = new HashMap<Integer,Integer>();
       
       String word ="";
       int current = start;
       q.add(start);
       
       while(!q.isEmpty()){
           current = q.remove();
           if(current == end){
               break;
           }
           else{
               List neighbors = (List<Integer>) _getTransitionsFrom(current);
               if ( neighbors != null){
                   Iterator j = neighbors.iterator();
                   while(j.hasNext()){
                       Integer thisNeighbor = (Integer)j.next();
                       if(visited[thisNeighbor] == false){
                           q.add(thisNeighbor);
                           visited[thisNeighbor] = true;
                           prevHop.put(thisNeighbor, current);
                       }
                   }
               }
           }
       }
       if(current != end){
           return "";
       }
       
       Integer j = end;
       Integer i = prevHop.get(j);
       
       while ( i != null){
           int index = ((List)_getTransitionsFrom(i)).indexOf(j);
           // get the symbol produced by the transition i -> j
           Character produced = (Character)((List)_adjacencyListSymbols.get(i)).get(index);
           
           word = produced.toString().concat(word);
           j = i;
           i = prevHop.get(i);    
       }     
       return word;
   }
   
   private List _pitchSequence;
   /* The adjacency list given on the Factor Oracle graph structure */
   private HashMap _adjacencyList;
   
   /* The symbol map given on the Factor Oracle graph structure */
   private HashMap _adjacencyListSymbols; 
   
   /* The repetition factor that determines the probability of moving along the original sequence in the FO */
   private double _repetitionFactor;
   
   private int _FOindex = 0;
   
   private List _durationSequence;
   
}
