
This directory contains 6 java files:
PetriNetActor.java     PetriNetReceiver.java  Transition.java
PetriNetDirector.java  Place.java             Weight.java

However, 3 of them are basically empty: 
PetriNetReceiver.java  Transition.java Weight.java

It is a good thing to have the Transition.java empty since
it can be easily replaced by other actors. 

********************************
A Petri net Actor  

THis is the basic unit of the hierarchical PetriNet component. It contains 
ports, places, transitions, and hierarchical petrinet components. 
The current version restricts the ports to be uniformly connected to
Places or transitions in one direction. It is not allowed to have 
ports to connect to places and transitions in the same input or output
direction.

It is also assumed that a transition is connected to places and a place
is connected to transitions eventually in the hierarchy. 

However, the system does not check for such restrictions.

The flat Peti Net model is defined as follows.
            place ----> transition ----> place
A hierarchical Petri Net model is defined as follows:
            place ----> transition ----> place
            place ----> (ports) ----> transition ---> (ports) ----> place
where (ports) means it could be 0 or any finite number of different directional
ports, ---> means one or more marked or unmarked arcs.


*********************************************
PetriNetDirector
 
 *   Petri net director. Basic Petri net model consists of places and
 *   transitions. A transition is enabled if places connected to the
 *   input of the transition all have more tokens than the corresponding 
 *   edge weights. An enabled transition can fire. WHen a transition fires,
 *   it reduces the tokens in the places connected to the input of the 
 *   transition, and increase the tokens in places connected to the output
 *   of the transition. 
 *
 *
 *   The key methods are the testing whether a transition
 *   is ready or not _testReadyTransition, and fire an enabled transition 
 *   _fireTransition. The sequence of firing is determinted by the method
 *   fireHierarchicalPetriNet.

