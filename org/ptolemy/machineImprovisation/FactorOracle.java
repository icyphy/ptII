/* Build a factor oracle data structure that represents a finite acyclic
automaton that contains at least all the suffixes of a given input
sequence.

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLChangeRequest;

/**
 * Build a factor oracle data structure that represents a finite acyclic
 * automaton that contains at least all the suffixes of a given input
 * sequence.
 *
 @author Ilge Akkaya
 @version  $Id$
 @since Ptolemy II 10.1
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating 
 */
public class FactorOracle extends FSMActor {
   /** Construct an actor with the given container and name.
    *  @param container The container.
    *  @param name The name of this actor
    *  @param trainingSequence The input string that the oracle is built from
    *  @exception IllegalActionException If the actor cannot be contained
    *   by the proposed container.
    *  @exception NameDuplicationException If the container already has an
    *   actor with this name.
    */
   public FactorOracle(CompositeEntity container, String name, Object[] trainingSequence, double repetitionFactor, boolean interpretAsNotes)
           throws NameDuplicationException, IllegalActionException {
       super(container, name);
       
       output = new TypedIOPort(this, "output", false, true);
       output.setTypeEquals(BaseType.STRING);
       
       if(repetitionFactor > 1.0 || repetitionFactor < 0.0){
           throw new IllegalActionException(this, "Repetition factor must be in range [0.0,1.0].");
       }
       _repetitionFactor = repetitionFactor;
       
       _adjacencyList = new HashMap<Integer,List<Integer>>();
       _adjacencyListSymbols = new HashMap<Integer,List<Integer>>();
       _longestRepeatedSuffixes = new LinkedList<String>();
       
       _suffixLinks = new HashMap();
       _alphabet = new HashSet();
       _sequenceLength = 0;
       
       _inputSequence = trainingSequence;
       _sequenceLength = _inputSequence.length;
       _interpretAsNotes = interpretAsNotes;
       
       _learnFactorOracle();
       _buildFactorOracle(); 
       
   }
   
   public FactorOracle(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException{
       super(container,name);
   }
   
   ///////////////////////////////////////////////////////////////////
   ////                         public variables                  ////

   public TypedIOPort inputSequence;
   
   public TypedIOPort output;

   /* The repetition probability P(moving along the original sequence rather than taking a jump along 
    * a suffix link)*/
   public Parameter repetitionFactor;

   

   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////

   private void _buildFactorOracle() throws NameDuplicationException, IllegalActionException{
       
       // create factor oracle transitions including the suffix links. 
       //TODO: attach symbols to the transitions that produce 
       //symbols. Mark the others as nondeterministic for now, until a probability value is associated with these
       // which should be asap.
       
       // probability of adding a variation to the output by taking a forward jump in the oracle
       
       String outputExpression = "A";
       String exitAngle = "0.0";
       
       HashMap _stateList = new HashMap();
       for(int i=0; i<=_adjacencyList.size();i++){
           
           State s = new State(this,"S"+i);
           Double horizontal = i*150.0;
           Double vertical = i*0.0; //alternate above&below: (1-(i%2))*50.0;
           // change default state location
           String changeExpression = "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{" 
                   + horizontal.toString() + "," + vertical.toString() + "}\"></property>";
           MoMLChangeRequest change = new MoMLChangeRequest(this, s, changeExpression);
           requestChange(change);
           _stateList.put(i, s);
           
       }
       
       // set initial and final states
       ((State)_stateList.get(0)).isInitialState.setToken("true");
       ((State)_stateList.get(0)).isInitialState.setPersistent(true);
       ((State)_stateList.get(_sequenceLength)).isFinalState.setToken("true");
       ((State)_stateList.get(_sequenceLength)).isFinalState.setPersistent(true);
       
       for(int i=0; i<_adjacencyList.size();i++){
           
           List destinations = (List)_adjacencyList.get(i);
           int _nTrans = destinations.size();
           // divide probability amongst all transitions from this state
           // if there is a suffix from this state to another, the destination will be >=0 ( -1 is reserved
           // for bottom)
           int hasSuffix = (Integer)_suffixLinks.get(i);
           int suffixCount = hasSuffix >= 0 ? 1 : 0;
           
           for( int k = 0 ; k < _nTrans; k++){ 
               // the destination node for this transition
               int j = (Integer)destinations.get(k);
              
               DoubleToken _probability;
               if(i == j-1){
                   // for the original string, probability will be repetitionfactor - enabledVariations*_improvisationFactor
                   
                   _probability = new DoubleToken(_repetitionFactor);
                   exitAngle = "0.0";
                   
               }else{
                   // divide the improvisation probability amongst the other transitions
                   int numberOfBranches = _nTrans - (1-suffixCount);
                   _probability = new DoubleToken((1-_repetitionFactor)/numberOfBranches*1.0);
                   // testing sth
                   //_probability = new DoubleToken();
                   exitAngle = "-0.7";
                   
               }
               
               String transitionProbabilityExpression = "probability(" + _probability + ")";
               
               String relationName = "relation_" + i + j ; //this will be unique. i:source state, j:destination state
               // label the original string transitions with the repetition factor
               
               String outputChar = " ";
            // get the symbol to be produced, when this transition is taken
               if( _interpretAsNotes == true){
                    outputChar = _translateKeyToLetterNote((Integer)((List)(_adjacencyListSymbols.get(i))).get(k));
               } else{
                   outputChar = ((List)(_adjacencyListSymbols.get(i))).get(k).toString();
               }
                   // set the output expression for this transition
               if(outputChar != null){
                   outputExpression = "output = \"" + outputChar.toString() +"\"";
               }else{
                   outputExpression = "";
               }
               
               
               Transition t = new Transition(this, relationName);
               (t.exitAngle).setExpression(exitAngle);
               (t.outputActions).setExpression(outputExpression);
               (t.guardExpression).setExpression(transitionProbabilityExpression);
               ((State)_stateList.get(i)).outgoingPort.link(t);
               ((State)_stateList.get(j)).incomingPort.link(t);
           } 
       }
       exitAngle = "-0.6";
       for(int i=0; i<_suffixLinks.size();i++){
            int destination = (Integer)_suffixLinks.get(i);
            String relationName = "relation"+i+destination;
            if(destination >= 0){    
                 
                Transition t = new Transition(this, relationName);
                (t.exitAngle).setExpression(exitAngle);
                (t.defaultTransition).setExpression("true");
                ((State)_stateList.get(i)).outgoingPort.link(t);
                ((State)_stateList.get(destination)).incomingPort.link(t);
            }
        }
   }
   
   /*
    * The function that builds the factor oracle data structure
    */
private String _translateKeyToLetterNote(int keyIndex){
    if( keyIndex < 21|| keyIndex > 108){
        return null;
    }
    else{
        int note = keyIndex % 12;
        String noteName = "";
        switch(note){
            case 0 : noteName = "C";  break;
            case 1 : noteName = "C#"; break;
            case 2 : noteName = "D";  break;
            case 3 : noteName = "D#"; break;
            case 4 : noteName = "E";  break;
            case 5 : noteName = "F";  break;
            case 6 : noteName = "F#"; break;
            case 7 : noteName = "G";  break;
            case 8 : noteName = "G#"; break;
            case 9 : noteName = "A";  break;
            case 10: noteName = "A#"; break;
            case 11: noteName = "B";  break;
            default: break;
        }
        // 0 : C
        // 1 : C#
        // 2 : D
        // 3 : D#
        // 4 : E
        // 5 : F
        // 6 : F#
        // 7 : G
        // 8 : G#
        // 9 : A
        // 10: A#
        // 11: B
        int octave = (keyIndex - (keyIndex % 12))/12 - 1;
        noteName = noteName.concat(octave+"");
        return noteName;
    }
}
private void _learnFactorOracle(){
       
       for( int i = 0; i< _sequenceLength; i++){
           Object p = _inputSequence[i];
           _alphabet.add(p);
           
           // add original transitions to graph
           List initialEdge = new LinkedList<Integer>();
           initialEdge.add(i+1);
           _adjacencyList.put(i, initialEdge);
           
           List initialSymbol = new LinkedList<Character>();
           initialSymbol.add(_inputSequence[i]);
           _adjacencyListSymbols.put(i, initialSymbol);
       }
       
       // by definition, the suffix link from state zero is the bottom element (represented as -1)
       _suffixLinks.put(0, -1);
       
       for( int i = 1 ; i <= _sequenceLength ; i++){
           
           // already created the original links
           int l = (Integer)_suffixLinks.get(i-1);
           // while previous node DOES exist and there is no w[i]-son of state l...
           Object wiSon = _inputSequence[i-1];
           while( l!=-1 && ((List)_adjacencyListSymbols.get(l)).contains(wiSon) == false){ 
               List prevList = (List<Integer>)_getTransitionsFrom(l);
               prevList.add(i);
               List prevSymbols = (List<Character>)_adjacencyListSymbols.get(l);
               prevSymbols.add(wiSon);
               // update adjacency list
               _adjacencyList.put(l, prevList);
               _adjacencyListSymbols.put(l, prevSymbols);
               l = (Integer)_suffixLinks.get(l);
           }
           if(l == -1){
               _suffixLinks.put(i, 0);
           }else{
               Integer wiSonIndex =((List)_adjacencyListSymbols.get(l)).indexOf(wiSon);
               Integer wiSonValue = _getTransitionsFrom(l).get(wiSonIndex);
               _suffixLinks.put(i, wiSonValue );
           }
       }
   } 
   public boolean postfire() throws IllegalActionException{
       
       _longestRepeatedSuffixes.clear();
       _suffixLinks.clear();
       _adjacencyList.clear();
       _adjacencyListSymbols.clear();
       
       return super.postfire();
   }
   protected List<Integer> _getTransitionsFrom( Integer node){
       List<Integer> _transitions = (List<Integer>)_adjacencyList.get(node);
       return _transitions;
   }
   /* Find the (unique) path that produces a given prefix and return the node index which terminates the 
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
   
   private Object[] _inputSequence;
   /* The adjacency list given on the Factor Oracle graph structure */
   private HashMap _adjacencyList;
   
   /* The symbol map given on the Factor Oracle graph structure */
   private HashMap _adjacencyListSymbols;
   
   private List _longestRepeatedSuffixes;
   
   private HashMap _suffixLinks;
   
   private Set _alphabet;
   
   private boolean _interpretAsNotes;
   
   private int _sequenceLength;
   
   private double _repetitionFactor;
}
