/*** preinitPIBlock($director, $name) ***/
// This is the platform independent code
/**/

/*** initPIBlock ***/
initializeEvents();
initPIBlock();
/**/

/*
processEvents() {
    while (1) {
        if (safeToProcess(event)) {
            // do some analysis, and set these values...
            // access to these function ptrs some where?
            event->actor.input1 = true;
            event->actor.input2 = false;

            fire(event);
        }
    }
}

fire(event) {
    if (event->actor == A)
        A_fire();
}

A_fire() {
    //generated
    if (input1 == true) {
        // sourceOutputPort and input are hard coded.
        transferDataFunctionZout1_to_Ain1();
    //    $ref(input) = $ref(sourceOutputPort);
    }
    //FireBlock();
}
        
transferDataFunctionZout1_to_Ain1(){
    Ain1 = Zout1;
}
    

// So during codegen, Zout1_to_Ain1 would be generated using:
// $ref(input), $ref(sourceOutputPort)
*/
