

PetriNetDirector

// we will have three kinds of fire, fire one transition,
// fire one round transitions, and fire till no more
// transitions can fire or go infinite. The director should have all the
// three parameters to choose. The implementation details
// are minor differences. We have implemented two methods
// the fire one transition, and fire one round transition.
// we have not implemented the fire all transitions till
// no more transitions to fire yet.

//  
// second problem is to choose from all the ready transition which one
// to fire, and when it fires, it changes the state, and
// we again to choose from many of the ready states
// until no more transitions can fire.
// the current method just fire the transition sequentially.



 public void fire() throws IllegalActionException {


/* A Petri net transition.

// fire has two parts: to modify the marking in the output places and
// to modify the marking at the input places. The method also checks
// whether there is a weight in the middle of the place-transition
// connection. The weight is defined as single-input single-output
// transformer to hold the weight of the arcs. 


    public void fire() throws IllegalActionException {


// prefire is similar with fire, it checks all the input places
// to see whether the marking in that place is bigger than
// the weight on the arc or not.


    public boolean prefire() throws IllegalActionException {


/* A Petri net place.

    public Parameter initialMarking;

    public int getMarking() { 
    public void increaseMarking(int i) {
    public void decreaseMarking(int i) {
    public void printMarking() { 
    public void initialize() throws IllegalActionException {
  
/* A Petri net Weight.
 
    public Parameter initialWeight;
    input.setMultiport(false);
    output.setMultiport(false);
   
    public void initialize() throws IllegalActionException {
    public int getWeight() {  
    public void printWeight() { 
    private int _currentWeight = 1;