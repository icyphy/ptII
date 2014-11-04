# Tests for verification
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2014 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.htm for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCaptureErr] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}


# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


## 
# Run NuSMV on a model by getting the MathematicalModelConverter and calling generateFile
proc generateFile {model {expectedResults {}}} {
    set w1 [java::new ptolemy.kernel.util.Workspace w1]
    set parser1 [java::new ptolemy.moml.MoMLParser $w1]
    set e0 [$parser1 parseFile $model]
    set modelConverter [java::cast ptolemy.verification.kernel.MathematicalModelConverter [$e0 getAttribute "Formal Model Converter"]]

# BTW - here is how to set parameters
#    set modelConverter [java::new ptolemy.verification.kernel.MathematicalModelConverter [java::cast ptolemy.kernel.util.NamedObj $e0] modelConverter]

#     # getParameter is defined in $PTII/util/testsuite/models.tcl
#     set target [getParameter $modelConverter target]
#     $target setExpression stl.kripke

#     set modelType [getParameter $modelConverter modelType]
#     $modelType setExpression Kripke

#     set formulaType [getParameter $modelConverter "Formula Type"]
#     $formulaType setExpression CTL

#     set outputType [getParameter $modelConverter "Output Type"]
#     $outputType setExpression SMV

#     set formula [getParameter $modelConverter formula]
#     $formula setExpression {! EF (CarLightNormal.state = Cgrn & PedestrianLightNormal.state = Pgreen)}


    set file [[java::field $modelConverter target] asFile]

    set modelTypeChosen [[java::field $modelConverter modelType] getChosenValue]

    set inputTemporalFormula [[java::field $modelConverter formula] getExpression]

    set formulaTypeChosen [[java::field $modelConverter formulaType] getChosenValue]
    
    set span [[java::cast ptolemy.data.IntToken [[java::field $modelConverter span] getToken]] intValue]

    set outputTypeChosen [[java::field $modelConverter outputType] getChosenValue]

    set bufferSize [[java::cast ptolemy.data.IntToken [[java::field $modelConverter buffer] getToken]] intValue]

    # Invoke NuSMV!n
    set stringBuffer [$modelConverter generateFile $file $modelTypeChosen $inputTemporalFormula $formulaTypeChosen $span $outputTypeChosen $bufferSize]

    set results [$stringBuffer toString]

    if {$expectedResults == {} } {
	return $results
    }

    if { [regexp -- $expectedResults $results] != 1} {
	return $results
    } else {
	return 1
    }
}

#####
#
test verification-1.1 {SimpleTrafficLight} {
    generateFile $PTII/ptolemy/verification/demo/SimpleTrafficLight/SimpleTrafficLight.xml \
       {-- specification \!\(EF \(CarLightNormal.state = Cgrn \& PedestrianLightNormal.state = Pgreen\)\)  is true}
} {1}

# Run the convert and return the contents of the file.
# The file itself is deleted.
proc runAndReturnFile {model resultsFile} {
    file delete -force $resultsFile

    generateFile $model
    
    set fp [open $resultsFile r]
    set fileData [read $fp]
    close $fp
    file delete -force $resultsFile
    return $fileData
}

#####
#
test verification-1.2 {SimpleTrafficLightDECTA} {
    # FIXME: This model returns different results each time it is run.
    runAndReturnFile \
	$PTII/ptolemy/verification/demo/SimpleTrafficLight/SimpleTrafficLightDECTA.xml \
	$PTII/ptolemy/verification/demo/SimpleTrafficLight/test2.d
} {{/*

This file represents a Communicating Timed Automata (CTA)
representation for a model described by Ptolemy II.
It is compatible with the format of the tool "Regional
Encoding Diagram" (RED 7.0) which is an integrated 
symbolic TCTL model-checker/simulator.

Process 1: CarLightNormal
Process 2: PedestrianLightNormal
Process 3: Clock
Process 4: TimedDelay
Process 5: TimedDelay2
Process 6: CarLightNormal_Port_Sec
Process 7: PedestrianLightNormal_Port_PstopDelay
Process 8: PedestrianLightNormal_Port_PgoDelay

*/

#define Clock_PERIOD 3
#define TimedDelay_DELAY 1
#define TimedDelay2_DELAY 2


process count = 8;

global discrete CarLightNormal_count:0..2; 


global clock  Clock_C1, TimedDelay_C0, TimedDelay2_C0;
local clock t;
 
global synchronizer TokenPstopDelayConsume, Pstop, PstopDelay, ND_PgoDelay, ND_Sec, Pgo, TokenSecConsume, TokenPgoDelayConsume, ND_PstopDelay, PgoDelay, Sec, N_CarLightNormal, N_PedestrianLightNormal, tick ;
 
/* Process name: CarLightNormal */
mode CarLightNormal_Port_Sec_TokenEmpty ( true ) { 
    when ?Sec !ND_Sec (true) may ; 
    when ?Sec (true) may t = 0; goto CarLightNormal_Port_Sec_TokenOccupied;
} 
mode CarLightNormal_Port_Sec_TokenOccupied ( t==0 ) { 
    when !ND_Sec (true) may ; goto CarLightNormal_Port_Sec_TokenEmpty;
    when  ?Sec (true) may ; 
/*    when  (t>=0) may  goto CarLightNormal_Port_Sec_TokenEmpty; */
    when ?TokenSecConsume (t>=0) may  goto CarLightNormal_Port_Sec_TokenEmpty; 
} 
mode CarLightNormal_State_Cyel ( true ) { 
    when  ?ND_Sec  !Pgo (t>0) may CarLightNormal_count = 0; t = 0; goto CarLightNormal_State_Cred ;
} 
mode CarLightNormal_State_Credyel ( true ) { 
    when  ?ND_Sec (t>0) may CarLightNormal_count = 0; t = 0; goto CarLightNormal_State_Cgrn ;
} 
mode CarLightNormal_State_Cgrn ( true ) { 
    when  ?ND_Sec (CarLightNormal_count == 1 && t>0) may   t = 0; goto CarLightNormal_State_Cyel ;
    when  ?ND_Sec (CarLightNormal_count < 1 && t>0 ) may CarLightNormal_count = CarLightNormal_count + 1; t = 0; goto CarLightNormal_State_Cgrn ;
} 
mode CarLightNormal_State_Cred ( true ) { 
    when  ?ND_Sec (CarLightNormal_count < 2 && t>0 ) may CarLightNormal_count = CarLightNormal_count + 1; t = 0; goto CarLightNormal_State_Cred ;
    when  ?ND_Sec  !Pstop (CarLightNormal_count == 2 && t>0 ) may CarLightNormal_count = 0; t = 0; goto CarLightNormal_State_Credyel ;
} 
mode CarLightNormal_State_Cinit_Plum ( true ) { 
    when ?Sec (t>=0) may CarLightNormal_count = 0; t=0; goto CarLightNormal_State_Cred ;
} 
mode CarLightNormal_State_Cinit ( true ) { 
    when  !TokenSecConsume (false && t>0) may   t = 0; goto CarLightNormal_State_Cinit ;
    when ?ND_Sec (t>0) may CarLightNormal_count = 0; t = 0; goto CarLightNormal_State_Cred ;
} 

/* Process name: PedestrianLightNormal */
mode PedestrianLightNormal_Port_PstopDelay_TokenEmpty ( true ) { 
    when ?PstopDelay !ND_PstopDelay (true) may ; 
    when ?PstopDelay (true) may t = 0; goto PedestrianLightNormal_Port_PstopDelay_TokenOccupied;
} 
mode PedestrianLightNormal_Port_PstopDelay_TokenOccupied ( t==0 ) { 
    when !ND_PstopDelay (true) may ; goto PedestrianLightNormal_Port_PstopDelay_TokenEmpty;
    when  ?PstopDelay (true) may ; 
/*    when  (t>=0) may  goto PedestrianLightNormal_Port_PstopDelay_TokenEmpty; */
    when ?TokenPstopDelayConsume (t>=0) may  goto PedestrianLightNormal_Port_PstopDelay_TokenEmpty; 
} 
mode PedestrianLightNormal_Port_PgoDelay_TokenEmpty ( true ) { 
    when ?PgoDelay !ND_PgoDelay (true) may ; 
    when ?PgoDelay (true) may t = 0; goto PedestrianLightNormal_Port_PgoDelay_TokenOccupied;
} 
mode PedestrianLightNormal_Port_PgoDelay_TokenOccupied ( t==0 ) { 
    when !ND_PgoDelay (true) may ; goto PedestrianLightNormal_Port_PgoDelay_TokenEmpty;
    when  ?PgoDelay (true) may ; 
/*    when  (t>=0) may  goto PedestrianLightNormal_Port_PgoDelay_TokenEmpty; */
    when ?TokenPgoDelayConsume (t>=0) may  goto PedestrianLightNormal_Port_PgoDelay_TokenEmpty; 
} 
mode PedestrianLightNormal_State_Pgreen ( true ) { 
    when  !TokenPgoDelayConsume (t>0) may   t = 0; goto PedestrianLightNormal_State_Pgreen ;
    when  ?ND_PstopDelay (t>0) may   t = 0; goto PedestrianLightNormal_State_Pred ;
} 
mode PedestrianLightNormal_State_Pred ( true ) { 
    when  !TokenPstopDelayConsume (t>0) may   t = 0; goto PedestrianLightNormal_State_Pred ;
    when  ?ND_PgoDelay (t>0) may   t = 0; goto PedestrianLightNormal_State_Pgreen ;
} 
mode PedestrianLightNormal_State_Pinit_Plum ( true ) { 
    when ?ND_PgoDelay (t>=0) may  t=0; goto PedestrianLightNormal_State_Pred ;
    when ?ND_PstopDelay (t>=0) may  t=0; goto PedestrianLightNormal_State_Pred ;
} 
mode PedestrianLightNormal_State_Pinit ( true ) { 
    when  !TokenPgoDelayConsume (false && t>0) may   t = 0; goto PedestrianLightNormal_State_Pinit ;
    when  !TokenPstopDelayConsume (false && t>0) may   t = 0; goto PedestrianLightNormal_State_Pinit ;
    when ?ND_PgoDelay (t>0) may   t = 0; goto PedestrianLightNormal_State_Pred ;
    when ?ND_PstopDelay (t>0) may   t = 0; goto PedestrianLightNormal_State_Pred ;
} 

/* Process name: Clock */
mode Clock_init (Clock_C1 == 0) { 
    when !Sec (Clock_C1 == 0) may Clock_C1 = 0 ; goto Clock_state;
}
mode Clock_state (Clock_C1 <= Clock_PERIOD) { 
    when !Sec (Clock_C1 == Clock_PERIOD) may Clock_C1 = 0 ; 
}

/* Process name: TimedDelay */
mode TimedDelay_S0 (true ) { 
    when ?Pgo (true) may TimedDelay_C0 = 0 ; goto TimedDelay_S1; 
}
mode TimedDelay_S1 ( TimedDelay_C0<= TimedDelay_DELAY  ) { 
    when !PgoDelay (TimedDelay_C0 == TimedDelay_DELAY ) may goto TimedDelay_S0; 
    when ?PgoDelay (true) may goto Buffer_Overflow; 
}

/* Process name: TimedDelay2 */
mode TimedDelay2_S0 (true ) { 
    when ?Pstop (true) may TimedDelay2_C0 = 0 ; goto TimedDelay2_S1; 
}
mode TimedDelay2_S1 ( TimedDelay2_C0<= TimedDelay2_DELAY  ) { 
    when !PstopDelay (TimedDelay2_C0 == TimedDelay2_DELAY ) may goto TimedDelay2_S0; 
    when ?PstopDelay (true) may goto Buffer_Overflow; 
}

/*State representing buffer overflow. */
mode Buffer_Overflow (true) {
}

/*Initial Condition */
initially
    CarLightNormal_State_Cinit_Plum[1] && t[1] == 0 && 
    PedestrianLightNormal_State_Pinit_Plum[2] && t[2] == 0 && 
    Clock_init[3] && t[3] == 0 && 
    TimedDelay_S0[4] && t[4] == 0 && 
    TimedDelay2_S0[5] && t[5] == 0 && 
    CarLightNormal_Port_Sec_TokenEmpty[6] && t[6] == 0 && 
    PedestrianLightNormal_Port_PstopDelay_TokenEmpty[7] && t[7] == 0 && 
    PedestrianLightNormal_Port_PgoDelay_TokenEmpty[8] && t[8] == 0 && 
    CarLightNormal_count == 0  && 
     Clock_C1 == 0 && 
     TimedDelay_C0 == 0 && 
     TimedDelay2_C0 == 0 ;
 
/*Specification */
/* In RED 7.0, specification must be placed in separated files. */
/**/
}}  {Known Failure, results differs each time}

#################################################################
#####
#


    set results1_6 {
MODULE CarLightNormal( Sec_isPresent )
	VAR 
		state : {C XXXXXX};
		count : { ls,0,1,2,gt };
	ASSIGN 
		init(state) := Cinit;
		next(state) :=
			case
				state=Cinit & count=ls :{ Cred };
				state=Cinit & count=0 :{ Cred };
				state=Cinit & count=1 :{ Cred };
				state=Cinit & count=2 :{ Cred };
				state=Cinit & count=gt :{ Cred };
				Sec_isPresent & state=Cred & count=ls :{ Cred };
				Sec_isPresent & state=Cred & count=0 :{ Cred };
				Sec_isPresent & state=Cred & count=1 :{ Cred };
				Sec_isPresent & state=Cred & count=2 :{ Credyel };
				Sec_isPresent & state=Credyel & count=ls :{ Cgrn };
				Sec_isPresent & state=Credyel & count=0 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=1 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=2 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=gt :{ Cgrn };
				Sec_isPresent & state=Cgrn & count=1 :{ Cyel };
				Sec_isPresent & state=Cgrn & count=ls :{ Cgrn };
				Sec_isPresent & state=Cgrn & count=0 :{ Cgrn };
				Sec_isPresent & state=Cyel & count=ls :{ Cred };
				Sec_isPresent & state=Cyel & count=0 :{ Cred };
				Sec_isPresent & state=Cyel & count=1 :{ Cred };
				Sec_isPresent & state=Cyel & count=2 :{ Cred };
				Sec_isPresent & state=Cyel & count=gt :{ Cred };
				TRUE             : state;
			esac;

		init(count) := 0;
		next(count) :=
			case
				state=Cinit & count=ls :{ 0 };
				state=Cinit & count=0 :{ 0 };
				state=Cinit & count=1 :{ 0 };
				state=Cinit & count=2 :{ 0 };
				state=Cinit & count=gt :{ 0 };
				Sec_isPresent & state=Cred & count=ls :{ ls };
				Sec_isPresent & state=Cred & count=ls :{ 0 };
				Sec_isPresent & state=Cred & count=0 :{ 1 };
				Sec_isPresent & state=Cred & count=1 :{ 2 };
				Sec_isPresent & state=Cred & count=2 :{ 0 };
				Sec_isPresent & state=Credyel & count=ls :{ 0 };
				Sec_isPresent & state=Credyel & count=0 :{ 0 };
				Sec_isPresent & state=Credyel & count=1 :{ 0 };
				Sec_isPresent & state=Credyel & count=2 :{ 0 };
				Sec_isPresent & state=Credyel & count=gt :{ 0 };
				Sec_isPresent & state=Cgrn & count=ls :{ ls };
				Sec_isPresent & state=Cgrn & count=ls :{ 0 };
				Sec_isPresent & state=Cgrn & count=0 :{ 1 };
				Sec_isPresent & state=Cyel & count=ls :{ 0 };
				Sec_isPresent & state=Cyel & count=0 :{ 0 };
				Sec_isPresent & state=Cyel & count=1 :{ 0 };
				Sec_isPresent & state=Cyel & count=2 :{ 0 };
				Sec_isPresent & state=Cyel & count=gt :{ 0 };
				TRUE             : count;
			esac;


	DEFINE
		Pstop_isPresent :=  ( Sec_isPresent & state=Cred & count=2 ) ;

  		Cyel_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Cred & count=2 )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cgrn & count=1 )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cgrn_value :=  (  !(state=Cinit & count=ls & Cgrn_isPresent )  )   & (  !(state=Cinit & count=0 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=1 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=2 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=gt & Cgrn_isPresent )  )   | ( Sec_isPresent & state=Credyel & count=ls & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=0 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=1 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=2 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=gt & Cgrn_isPresent  )   & (  !(Sec_isPresent & state=Cgrn & count=1 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=ls & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=0 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=1 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=2 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=gt & Cgrn_isPresent )  ) ;

 		Cred_value :=  ( state=Cinit & count=ls & Cred_isPresent  )   | ( state=Cinit & count=0 & Cred_isPresent  )   | ( state=Cinit & count=1 & Cred_isPresent  )   | ( state=Cinit & count=2 & Cred_isPresent  )   | ( state=Cinit & count=gt & Cred_isPresent  )   & (  !(Sec_isPresent & state=Credyel & count=ls & Cred_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=0 & Cred_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=1 & Cred_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=2 & Cred_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=gt & Cred_isPresent )  )   | ( Sec_isPresent & state=Cyel & count=ls & Cred_isPresent  )   | ( Sec_isPresent & state=Cyel & count=0 & Cred_isPresent  )   | ( Sec_isPresent & state=Cyel & count=1 & Cred_isPresent  )   | ( Sec_isPresent & state=Cyel & count=2 & Cred_isPresent  )   | ( Sec_isPresent & state=Cyel & count=gt & Cred_isPresent  ) ;

 		Pgo_isPresent :=  ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cgrn_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cgrn & count=1 )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cred_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Pgo_value :=  (  !(Sec_isPresent & state=Cyel & count=ls & Pgo_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=0 & Pgo_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=1 & Pgo_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=2 & Pgo_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=gt & Pgo_isPresent )  ) ;

 		Cyel_value :=  (  !(state=Cinit & count=ls & Cyel_isPresent )  )   & (  !(state=Cinit & count=0 & Cyel_isPresent )  )   & (  !(state=Cinit & count=1 & Cyel_isPresent )  )   & (  !(state=Cinit & count=2 & Cyel_isPresent )  )   & (  !(state=Cinit & count=gt & Cyel_isPresent )  )   | ( Sec_isPresent & state=Cred & count=2 & Cyel_isPresent  )   & (  !(Sec_isPresent & state=Credyel & count=ls & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=0 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=1 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=2 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=gt & Cyel_isPresent )  )   | ( Sec_isPresent & state=Cgrn & count=1 & Cyel_isPresent  )   & (  !(Sec_isPresent & state=Cyel & count=ls & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=0 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=1 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=2 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=gt & Cyel_isPresent )  ) ;

 		Pstop_value :=  ( Sec_isPresent & state=Cred & count=2 & Pstop_isPresent  ) ;

  
MODULE PedestrianLightNormal( Pstop_isPresent,Pgo_isPresent,Pgo_value,Pstop_value )
	VAR 
		state : {P XXXXXX};
	ASSIGN 
		init(state) := Pinit;
		next(state) :=
			case
				state=Pinit :{ Pred };
				state=Pred & Pgo_value=0 & Pgo_isPresent  :{ Pgreen };
				state=Pgreen & Pstop_value=1 & Pstop_isPresent  :{ Pred };
				TRUE             : state;
			esac;


	DEFINE
		Pgrn_isPresent :=  ( state=Pinit )   | ( state=Pred & Pgo_value=0 & Pgo_isPresent  )   | ( state=Pgreen & Pstop_value=1 & Pstop_isPresent  ) ;

 		Pred_value :=  ( state=Pinit & Pred_isPresent  )   & (  !(state=Pred & Pred_isPresent  & Pgo_value=0 & Pgo_isPresent )  )   | ( state=Pgreen & Pred_isPresent  & Pstop_value=1 & Pstop_isPresent  ) ;

 		Pgrn_value :=  (  !(state=Pinit & Pgrn_isPresent )  )   | ( state=Pred & Pgo_value=0 & Pgo_isPresent  & Pgrn_isPresent  )   & (  !(state=Pgreen & Pgrn_isPresent  & Pstop_value=1 & Pstop_isPresent )  ) ;

 		Pred_isPresent :=  ( state=Pinit )   | ( state=Pred & Pgo_value=0 & Pgo_isPresent  )   | ( state=Pgreen & Pstop_value=1 & Pstop_isPresent  ) ;

 

MODULE main 
	VAR 
		CarLightNormal: CarLightNormal( TRUE);
		PedestrianLightNormal: PedestrianLightNormal(CarLightNormal.Pstop_isPresent, CarLightNormal.Pgo_isPresent, CarLightNormal.Pgo_value, CarLightNormal.Pstop_value );

	SPEC 
		! EF (CarLightNormal.state = Cgrn & PedestrianLightNormal.state = Pgreen)
}



    set results1_8 {
MODULE CarLightNormal( Sec_isPresent )
	VAR 
		state : {C XXXXXX};
		count : { ls,0,1,2,gt };
	ASSIGN 
		init(state) := Cinit;
		next(state) :=
			case
				state=Cinit & count=ls :{ Cred };
				state=Cinit & count=0 :{ Cred };
				state=Cinit & count=1 :{ Cred };
				state=Cinit & count=2 :{ Cred };
				state=Cinit & count=gt :{ Cred };
				Sec_isPresent & state=Cred & count=ls :{ Cred };
				Sec_isPresent & state=Cred & count=0 :{ Cred };
				Sec_isPresent & state=Cred & count=1 :{ Cred };
				Sec_isPresent & state=Cred & count=2 :{ Credyel };
				Sec_isPresent & state=Credyel & count=ls :{ Cgrn };
				Sec_isPresent & state=Credyel & count=0 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=1 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=2 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=gt :{ Cgrn };
				Sec_isPresent & state=Cgrn & count=1 :{ Cyel };
				Sec_isPresent & state=Cgrn & count=ls :{ Cgrn };
				Sec_isPresent & state=Cgrn & count=0 :{ Cgrn };
				Sec_isPresent & state=Cyel & count=ls :{ Cred };
				Sec_isPresent & state=Cyel & count=0 :{ Cred };
				Sec_isPresent & state=Cyel & count=1 :{ Cred };
				Sec_isPresent & state=Cyel & count=2 :{ Cred };
				Sec_isPresent & state=Cyel & count=gt :{ Cred };
				TRUE             : state;
			esac;

		init(count) := 0;
		next(count) :=
			case
				state=Cinit & count=ls :{ 0 };
				state=Cinit & count=0 :{ 0 };
				state=Cinit & count=1 :{ 0 };
				state=Cinit & count=2 :{ 0 };
				state=Cinit & count=gt :{ 0 };
				Sec_isPresent & state=Cred & count=ls :{ ls };
				Sec_isPresent & state=Cred & count=ls :{ 0 };
				Sec_isPresent & state=Cred & count=0 :{ 1 };
				Sec_isPresent & state=Cred & count=1 :{ 2 };
				Sec_isPresent & state=Cred & count=2 :{ 0 };
				Sec_isPresent & state=Credyel & count=ls :{ 0 };
				Sec_isPresent & state=Credyel & count=0 :{ 0 };
				Sec_isPresent & state=Credyel & count=1 :{ 0 };
				Sec_isPresent & state=Credyel & count=2 :{ 0 };
				Sec_isPresent & state=Credyel & count=gt :{ 0 };
				Sec_isPresent & state=Cgrn & count=ls :{ ls };
				Sec_isPresent & state=Cgrn & count=ls :{ 0 };
				Sec_isPresent & state=Cgrn & count=0 :{ 1 };
				Sec_isPresent & state=Cyel & count=ls :{ 0 };
				Sec_isPresent & state=Cyel & count=0 :{ 0 };
				Sec_isPresent & state=Cyel & count=1 :{ 0 };
				Sec_isPresent & state=Cyel & count=2 :{ 0 };
				Sec_isPresent & state=Cyel & count=gt :{ 0 };
				TRUE             : count;
			esac;


	DEFINE
		Cred_value :=  ( state=Cinit & Cred_isPresent  & count=ls )   | ( state=Cinit & Cred_isPresent  & count=0 )   | ( state=Cinit & Cred_isPresent  & count=1 )   | ( state=Cinit & Cred_isPresent  & count=2 )   | ( state=Cinit & Cred_isPresent  & count=gt )   & (  !(Sec_isPresent & state=Credyel & Cred_isPresent  & count=ls)  )   & (  !(Sec_isPresent & state=Credyel & Cred_isPresent  & count=0)  )   & (  !(Sec_isPresent & state=Credyel & Cred_isPresent  & count=1)  )   & (  !(Sec_isPresent & state=Credyel & Cred_isPresent  & count=2)  )   & (  !(Sec_isPresent & state=Credyel & Cred_isPresent  & count=gt)  )   | ( Sec_isPresent & state=Cyel & Cred_isPresent  & count=ls )   | ( Sec_isPresent & state=Cyel & Cred_isPresent  & count=0 )   | ( Sec_isPresent & state=Cyel & Cred_isPresent  & count=1 )   | ( Sec_isPresent & state=Cyel & Cred_isPresent  & count=2 )   | ( Sec_isPresent & state=Cyel & Cred_isPresent  & count=gt ) ;

 		Pgo_value :=  (  !(Sec_isPresent & state=Cyel & Pgo_isPresent  & count=ls)  )   & (  !(Sec_isPresent & state=Cyel & Pgo_isPresent  & count=0)  )   & (  !(Sec_isPresent & state=Cyel & Pgo_isPresent  & count=1)  )   & (  !(Sec_isPresent & state=Cyel & Pgo_isPresent  & count=2)  )   & (  !(Sec_isPresent & state=Cyel & Pgo_isPresent  & count=gt)  ) ;

 		Pstop_isPresent :=  ( Sec_isPresent & state=Cred & count=2 ) ;

  		Pgo_isPresent :=  ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Pstop_value :=  ( Sec_isPresent & state=Cred & count=2 & Pstop_isPresent  ) ;

  		Cred_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cyel_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Cred & count=2 )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cgrn & count=1 )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cyel_value :=  (  !(state=Cinit & count=ls & Cyel_isPresent )  )   & (  !(state=Cinit & count=0 & Cyel_isPresent )  )   & (  !(state=Cinit & count=1 & Cyel_isPresent )  )   & (  !(state=Cinit & count=2 & Cyel_isPresent )  )   & (  !(state=Cinit & count=gt & Cyel_isPresent )  )   | ( Sec_isPresent & state=Cred & count=2 & Cyel_isPresent  )   & (  !(Sec_isPresent & state=Credyel & count=ls & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=0 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=1 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=2 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=gt & Cyel_isPresent )  )   | ( Sec_isPresent & state=Cgrn & count=1 & Cyel_isPresent  )   & (  !(Sec_isPresent & state=Cyel & count=ls & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=0 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=1 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=2 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=gt & Cyel_isPresent )  ) ;

 		Cgrn_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cgrn & count=1 )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cgrn_value :=  (  !(state=Cinit & count=ls & Cgrn_isPresent )  )   & (  !(state=Cinit & count=0 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=1 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=2 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=gt & Cgrn_isPresent )  )   | ( Sec_isPresent & state=Credyel & count=ls & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=0 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=1 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=2 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=gt & Cgrn_isPresent  )   & (  !(Sec_isPresent & state=Cgrn & count=1 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=ls & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=0 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=1 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=2 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=gt & Cgrn_isPresent )  ) ;

 
MODULE PedestrianLightNormal( Pgo_value,Pgo_isPresent,Pstop_isPresent,Pstop_value )
	VAR 
		state : {P XXXXXX};
	ASSIGN 
		init(state) := Pinit;
		next(state) :=
			case
				state=Pinit :{ Pred };
				state=Pred & Pgo_value=0 & Pgo_isPresent  :{ Pgreen };
				state=Pgreen & Pstop_value=1 & Pstop_isPresent  :{ Pred };
				TRUE             : state;
			esac;


	DEFINE
		Pgrn_isPresent :=  ( state=Pinit )   | ( state=Pred & Pgo_value=0 & Pgo_isPresent  )   | ( state=Pgreen & Pstop_value=1 & Pstop_isPresent  ) ;

 		Pred_value :=  ( state=Pinit & Pred_isPresent  )   & (  !(state=Pred & Pgo_value=0 & Pgo_isPresent  & Pred_isPresent )  )   | ( state=Pgreen & Pred_isPresent  & Pstop_value=1 & Pstop_isPresent  ) ;

 		Pgrn_value :=  (  !(state=Pinit & Pgrn_isPresent )  )   | ( state=Pred & Pgo_value=0 & Pgo_isPresent  & Pgrn_isPresent  )   & (  !(state=Pgreen & Pstop_value=1 & Pstop_isPresent  & Pgrn_isPresent )  ) ;

 		Pred_isPresent :=  ( state=Pinit )   | ( state=Pred & Pgo_value=0 & Pgo_isPresent  )   | ( state=Pgreen & Pstop_value=1 & Pstop_isPresent  ) ;

 

MODULE main 
	VAR 
		CarLightNormal: CarLightNormal( TRUE);
		PedestrianLightNormal: PedestrianLightNormal(CarLightNormal.Pgo_value, CarLightNormal.Pgo_isPresent, CarLightNormal.Pstop_isPresent, CarLightNormal.Pstop_value );

	SPEC 
		! EF (CarLightNormal.state = Cgrn & PedestrianLightNormal.state = Pgreen)
}

test verification-1.3 {SimpleTrafficLightBooleanToken} {
    set results [runAndReturnFile $PTII/ptolemy/verification/demo/SimpleTrafficLight/SimpleTrafficLightBooleanToken.xml  $PTII/ptolemy/verification/demo/SimpleTrafficLight/stlbt.kripke]

    # The model does not produce the same results each time, so we blank out the differences.

    regsub {state : \{C[^\}]*\}} $results {state : \{C XXXXXX\}} results2
    regsub {state : \{P[^\}]*\}} $results2 {state : \{P XXXXXX\}} results3

if { $results3 != $results1_6 } {
    if { $results3 != $results1_8 } {
	puts "Results was: $results3\n which is not the same as the Java 1.6 results:\n $results1_6\nDiffs:\n[diffText $results1_6 $results3]\n or 1.8 results:\n $results1_8\nDiffs:\n[diffText $results1_8 $results3]"
	error 
    }
 }
} {}
 

#################################################################
#####
#
set results1_4jdk1_6 {
MODULE CarLightNormal( Sec_isPresent )
	VAR 
		state : {C XXXXXX};
		count : { ls,0,1,2,gt };
	ASSIGN 
		init(state) := Cinit;
		next(state) :=
			case
				state=Cinit & count=ls :{ Cred };
				state=Cinit & count=0 :{ Cred };
				state=Cinit & count=1 :{ Cred };
				state=Cinit & count=2 :{ Cred };
				state=Cinit & count=gt :{ Cred };
				Sec_isPresent & state=Cred & count=ls :{ Cred };
				Sec_isPresent & state=Cred & count=0 :{ Cred };
				Sec_isPresent & state=Cred & count=1 :{ Cred };
				Sec_isPresent & state=Cred & count=2 :{ Credyel };
				Sec_isPresent & state=Credyel & count=ls :{ Cgrn };
				Sec_isPresent & state=Credyel & count=0 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=1 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=2 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=gt :{ Cgrn };
				Sec_isPresent & state=Cgrn & count=1 :{ Cyel };
				Sec_isPresent & state=Cgrn & count=ls :{ Cgrn };
				Sec_isPresent & state=Cgrn & count=0 :{ Cgrn };
				Sec_isPresent & state=Cyel & count=ls :{ Cred };
				Sec_isPresent & state=Cyel & count=0 :{ Cred };
				Sec_isPresent & state=Cyel & count=1 :{ Cred };
				Sec_isPresent & state=Cyel & count=2 :{ Cred };
				Sec_isPresent & state=Cyel & count=gt :{ Cred };
				TRUE             : state;
			esac;

		init(count) := 0;
		next(count) :=
			case
				state=Cinit & count=ls :{ 0 };
				state=Cinit & count=0 :{ 0 };
				state=Cinit & count=1 :{ 0 };
				state=Cinit & count=2 :{ 0 };
				state=Cinit & count=gt :{ 0 };
				Sec_isPresent & state=Cred & count=ls :{ ls };
				Sec_isPresent & state=Cred & count=ls :{ 0 };
				Sec_isPresent & state=Cred & count=0 :{ 1 };
				Sec_isPresent & state=Cred & count=1 :{ 2 };
				Sec_isPresent & state=Cred & count=2 :{ 0 };
				Sec_isPresent & state=Credyel & count=ls :{ 0 };
				Sec_isPresent & state=Credyel & count=0 :{ 0 };
				Sec_isPresent & state=Credyel & count=1 :{ 0 };
				Sec_isPresent & state=Credyel & count=2 :{ 0 };
				Sec_isPresent & state=Credyel & count=gt :{ 0 };
				Sec_isPresent & state=Cgrn & count=ls :{ ls };
				Sec_isPresent & state=Cgrn & count=ls :{ 0 };
				Sec_isPresent & state=Cgrn & count=0 :{ 1 };
				Sec_isPresent & state=Cyel & count=ls :{ 0 };
				Sec_isPresent & state=Cyel & count=0 :{ 0 };
				Sec_isPresent & state=Cyel & count=1 :{ 0 };
				Sec_isPresent & state=Cyel & count=2 :{ 0 };
				Sec_isPresent & state=Cyel & count=gt :{ 0 };
				TRUE             : count;
			esac;


	DEFINE
		Pstop_isPresent :=  ( Sec_isPresent & state=Cred & count=2 ) ;

  		Cyel_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Cred & count=2 )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cgrn & count=1 )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cgrn_value :=  (  !(state=Cinit & count=ls & Cgrn_isPresent )  )   & (  !(state=Cinit & count=0 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=1 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=2 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=gt & Cgrn_isPresent )  )   | ( Sec_isPresent & state=Credyel & count=ls & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=0 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=1 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=2 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=gt & Cgrn_isPresent  )   & (  !(Sec_isPresent & state=Cgrn & count=1 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=ls & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=0 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=1 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=2 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=gt & Cgrn_isPresent )  ) ;

 		Cred_value :=  ( state=Cinit & count=ls & Cred_isPresent  )   | ( state=Cinit & count=0 & Cred_isPresent  )   | ( state=Cinit & count=1 & Cred_isPresent  )   | ( state=Cinit & count=2 & Cred_isPresent  )   | ( state=Cinit & count=gt & Cred_isPresent  )   & (  !(Sec_isPresent & state=Credyel & count=ls & Cred_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=0 & Cred_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=1 & Cred_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=2 & Cred_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=gt & Cred_isPresent )  )   | ( Sec_isPresent & state=Cyel & count=ls & Cred_isPresent  )   | ( Sec_isPresent & state=Cyel & count=0 & Cred_isPresent  )   | ( Sec_isPresent & state=Cyel & count=1 & Cred_isPresent  )   | ( Sec_isPresent & state=Cyel & count=2 & Cred_isPresent  )   | ( Sec_isPresent & state=Cyel & count=gt & Cred_isPresent  ) ;

 		Pgo_isPresent :=  ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cgrn_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cgrn & count=1 )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cred_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Pgo_value :=  ( Sec_isPresent & state=Cyel & count=ls & Pgo_isPresent  )   | ( Sec_isPresent & state=Cyel & count=0 & Pgo_isPresent  )   | ( Sec_isPresent & state=Cyel & count=1 & Pgo_isPresent  )   | ( Sec_isPresent & state=Cyel & count=2 & Pgo_isPresent  )   | ( Sec_isPresent & state=Cyel & count=gt & Pgo_isPresent  ) ;

 		Cyel_value :=  (  !(state=Cinit & count=ls & Cyel_isPresent )  )   & (  !(state=Cinit & count=0 & Cyel_isPresent )  )   & (  !(state=Cinit & count=1 & Cyel_isPresent )  )   & (  !(state=Cinit & count=2 & Cyel_isPresent )  )   & (  !(state=Cinit & count=gt & Cyel_isPresent )  )   | ( Sec_isPresent & state=Cred & count=2 & Cyel_isPresent  )   & (  !(Sec_isPresent & state=Credyel & count=ls & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=0 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=1 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=2 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=gt & Cyel_isPresent )  )   | ( Sec_isPresent & state=Cgrn & count=1 & Cyel_isPresent  )   & (  !(Sec_isPresent & state=Cyel & count=ls & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=0 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=1 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=2 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=gt & Cyel_isPresent )  ) ;

 		Pstop_value :=  ( Sec_isPresent & state=Cred & count=2 & Pstop_isPresent  ) ;

  
MODULE PedestrianLightSMV (Pgo_isPresent, Pgo_value ,Pstop_isPresent, Pstop_value ,Sec_isPresent, Sec_value )

/* The file contains contents in formats acceptable by SMV.
 * Currently there is no content checking functionality.
 * It is the designer's responsibility to keep it correct.
 */

	VAR 
		state : {Pgreen,Pinit,Pred};
	ASSIGN 
		init(state) := Pinit;
		next(state) :=
			case
				state=Pinit :{ Pred };
				Pgo_isPresent & state=Pred :{ Pgreen };
				Pstop_isPresent & state=Pgreen :{ Pred };
				1             : state;
			esac;

	DEFINE
		Pred_isPresent :=  ( state=Pinit ) |  ( Pgo_isPresent & state=Pred ) |  ( Pstop_isPresent & state=Pgreen ) ;

 		Pgrn_isPresent :=  ( state=Pinit ) |  ( Pgo_isPresent & state=Pred ) |  ( Pstop_isPresent & state=Pgreen ) ;

MODULE main 
	VAR 
		CarLightNormal: CarLightNormal( TRUE);
		PedestrianLightSMV: PedestrianLightSMV(CarLightNormal.Pgo_isPresent, CarLightNormal.Pgo_value, CarLightNormal.Pstop_isPresent, CarLightNormal.Pstop_value,  1, 1);

	SPEC 
		! EF (CarLightNormal.state = Cgrn & PedestrianLightSMV.state = Pgreen)
}


set results1_4jdk1_8 {
MODULE CarLightNormal( Sec_isPresent )
	VAR 
		state : {C XXXXXX};
		count : { ls,0,1,2,gt };
	ASSIGN 
		init(state) := Cinit;
		next(state) :=
			case
				state=Cinit & count=ls :{ Cred };
				state=Cinit & count=0 :{ Cred };
				state=Cinit & count=1 :{ Cred };
				state=Cinit & count=2 :{ Cred };
				state=Cinit & count=gt :{ Cred };
				Sec_isPresent & state=Cred & count=ls :{ Cred };
				Sec_isPresent & state=Cred & count=0 :{ Cred };
				Sec_isPresent & state=Cred & count=1 :{ Cred };
				Sec_isPresent & state=Cred & count=2 :{ Credyel };
				Sec_isPresent & state=Credyel & count=ls :{ Cgrn };
				Sec_isPresent & state=Credyel & count=0 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=1 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=2 :{ Cgrn };
				Sec_isPresent & state=Credyel & count=gt :{ Cgrn };
				Sec_isPresent & state=Cgrn & count=1 :{ Cyel };
				Sec_isPresent & state=Cgrn & count=ls :{ Cgrn };
				Sec_isPresent & state=Cgrn & count=0 :{ Cgrn };
				Sec_isPresent & state=Cyel & count=ls :{ Cred };
				Sec_isPresent & state=Cyel & count=0 :{ Cred };
				Sec_isPresent & state=Cyel & count=1 :{ Cred };
				Sec_isPresent & state=Cyel & count=2 :{ Cred };
				Sec_isPresent & state=Cyel & count=gt :{ Cred };
				TRUE             : state;
			esac;

		init(count) := 0;
		next(count) :=
			case
				state=Cinit & count=ls :{ 0 };
				state=Cinit & count=0 :{ 0 };
				state=Cinit & count=1 :{ 0 };
				state=Cinit & count=2 :{ 0 };
				state=Cinit & count=gt :{ 0 };
				Sec_isPresent & state=Cred & count=ls :{ ls };
				Sec_isPresent & state=Cred & count=ls :{ 0 };
				Sec_isPresent & state=Cred & count=0 :{ 1 };
				Sec_isPresent & state=Cred & count=1 :{ 2 };
				Sec_isPresent & state=Cred & count=2 :{ 0 };
				Sec_isPresent & state=Credyel & count=ls :{ 0 };
				Sec_isPresent & state=Credyel & count=0 :{ 0 };
				Sec_isPresent & state=Credyel & count=1 :{ 0 };
				Sec_isPresent & state=Credyel & count=2 :{ 0 };
				Sec_isPresent & state=Credyel & count=gt :{ 0 };
				Sec_isPresent & state=Cgrn & count=ls :{ ls };
				Sec_isPresent & state=Cgrn & count=ls :{ 0 };
				Sec_isPresent & state=Cgrn & count=0 :{ 1 };
				Sec_isPresent & state=Cyel & count=ls :{ 0 };
				Sec_isPresent & state=Cyel & count=0 :{ 0 };
				Sec_isPresent & state=Cyel & count=1 :{ 0 };
				Sec_isPresent & state=Cyel & count=2 :{ 0 };
				Sec_isPresent & state=Cyel & count=gt :{ 0 };
				TRUE             : count;
			esac;


	DEFINE
		Cred_value :=  ( state=Cinit & Cred_isPresent  & count=ls )   | ( state=Cinit & Cred_isPresent  & count=0 )   | ( state=Cinit & Cred_isPresent  & count=1 )   | ( state=Cinit & Cred_isPresent  & count=2 )   | ( state=Cinit & Cred_isPresent  & count=gt )   & (  !(Sec_isPresent & state=Credyel & Cred_isPresent  & count=ls)  )   & (  !(Sec_isPresent & state=Credyel & Cred_isPresent  & count=0)  )   & (  !(Sec_isPresent & state=Credyel & Cred_isPresent  & count=1)  )   & (  !(Sec_isPresent & state=Credyel & Cred_isPresent  & count=2)  )   & (  !(Sec_isPresent & state=Credyel & Cred_isPresent  & count=gt)  )   | ( Sec_isPresent & state=Cyel & Cred_isPresent  & count=ls )   | ( Sec_isPresent & state=Cyel & Cred_isPresent  & count=0 )   | ( Sec_isPresent & state=Cyel & Cred_isPresent  & count=1 )   | ( Sec_isPresent & state=Cyel & Cred_isPresent  & count=2 )   | ( Sec_isPresent & state=Cyel & Cred_isPresent  & count=gt ) ;

 		Pgo_value :=  ( Sec_isPresent & state=Cyel & Pgo_isPresent  & count=ls )   | ( Sec_isPresent & state=Cyel & Pgo_isPresent  & count=0 )   | ( Sec_isPresent & state=Cyel & Pgo_isPresent  & count=1 )   | ( Sec_isPresent & state=Cyel & Pgo_isPresent  & count=2 )   | ( Sec_isPresent & state=Cyel & Pgo_isPresent  & count=gt ) ;

 		Pstop_isPresent :=  ( Sec_isPresent & state=Cred & count=2 ) ;

  		Pgo_isPresent :=  ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Pstop_value :=  ( Sec_isPresent & state=Cred & count=2 & Pstop_isPresent  ) ;

  		Cred_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cyel_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Cred & count=2 )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cgrn & count=1 )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cyel_value :=  (  !(state=Cinit & count=ls & Cyel_isPresent )  )   & (  !(state=Cinit & count=0 & Cyel_isPresent )  )   & (  !(state=Cinit & count=1 & Cyel_isPresent )  )   & (  !(state=Cinit & count=2 & Cyel_isPresent )  )   & (  !(state=Cinit & count=gt & Cyel_isPresent )  )   | ( Sec_isPresent & state=Cred & count=2 & Cyel_isPresent  )   & (  !(Sec_isPresent & state=Credyel & count=ls & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=0 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=1 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=2 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Credyel & count=gt & Cyel_isPresent )  )   | ( Sec_isPresent & state=Cgrn & count=1 & Cyel_isPresent  )   & (  !(Sec_isPresent & state=Cyel & count=ls & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=0 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=1 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=2 & Cyel_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=gt & Cyel_isPresent )  ) ;

 		Cgrn_isPresent :=  ( state=Cinit & count=ls )   | ( state=Cinit & count=0 )   | ( state=Cinit & count=1 )   | ( state=Cinit & count=2 )   | ( state=Cinit & count=gt )   | ( Sec_isPresent & state=Credyel & count=ls )   | ( Sec_isPresent & state=Credyel & count=0 )   | ( Sec_isPresent & state=Credyel & count=1 )   | ( Sec_isPresent & state=Credyel & count=2 )   | ( Sec_isPresent & state=Credyel & count=gt )   | ( Sec_isPresent & state=Cgrn & count=1 )   | ( Sec_isPresent & state=Cyel & count=ls )   | ( Sec_isPresent & state=Cyel & count=0 )   | ( Sec_isPresent & state=Cyel & count=1 )   | ( Sec_isPresent & state=Cyel & count=2 )   | ( Sec_isPresent & state=Cyel & count=gt ) ;

 		Cgrn_value :=  (  !(state=Cinit & count=ls & Cgrn_isPresent )  )   & (  !(state=Cinit & count=0 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=1 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=2 & Cgrn_isPresent )  )   & (  !(state=Cinit & count=gt & Cgrn_isPresent )  )   | ( Sec_isPresent & state=Credyel & count=ls & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=0 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=1 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=2 & Cgrn_isPresent  )   | ( Sec_isPresent & state=Credyel & count=gt & Cgrn_isPresent  )   & (  !(Sec_isPresent & state=Cgrn & count=1 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=ls & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=0 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=1 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=2 & Cgrn_isPresent )  )   & (  !(Sec_isPresent & state=Cyel & count=gt & Cgrn_isPresent )  ) ;

 
MODULE PedestrianLightSMV (Pgo_isPresent, Pgo_value ,Pstop_isPresent, Pstop_value ,Sec_isPresent, Sec_value )

/* The file contains contents in formats acceptable by SMV.
 * Currently there is no content checking functionality.
 * It is the designer's responsibility to keep it correct.
 */

	VAR 
		state : {Pgreen,Pinit,Pred};
	ASSIGN 
		init(state) := Pinit;
		next(state) :=
			case
				state=Pinit :{ Pred };
				Pgo_isPresent & state=Pred :{ Pgreen };
				Pstop_isPresent & state=Pgreen :{ Pred };
				1             : state;
			esac;

	DEFINE
		Pred_isPresent :=  ( state=Pinit ) |  ( Pgo_isPresent & state=Pred ) |  ( Pstop_isPresent & state=Pgreen ) ;

 		Pgrn_isPresent :=  ( state=Pinit ) |  ( Pgo_isPresent & state=Pred ) |  ( Pstop_isPresent & state=Pgreen ) ;

MODULE main 
	VAR 
		CarLightNormal: CarLightNormal( TRUE);
		PedestrianLightSMV: PedestrianLightSMV(CarLightNormal.Pgo_isPresent, CarLightNormal.Pgo_value, CarLightNormal.Pstop_isPresent, CarLightNormal.Pstop_value,  1, 1);

	SPEC 
		! EF (CarLightNormal.state = Cgrn & PedestrianLightSMV.state = Pgreen)
}


test verification-1.4 {SimpleTrafficLightSMVModule} {
    set results [runAndReturnFile $PTII/ptolemy/verification/demo/SimpleTrafficLight/SimpleTrafficLightSMVModule.xml  $PTII/ptolemy/verification/demo/SimpleTrafficLight/test.kripke]
    regsub {state : \{C[^\}]*\}} $results {state : \{C XXXXXX\}} results2
    if { $results2 != $results1_4jdk1_6 } {
	if { $results2 != $results1_4jdk1_8 } {
	    puts "Results was: $results2\n which is not the same as the Java 1.6 results:\n $results1_4jdk1_6\nDiffs:\n[diffText $results1_4jdk1_6 $results2]\n or 1.8 results:\n $results1_4jdk1_8\nDiffs:\n[diffText $results1_4jdk1_8 $results2]"
	    error 
	}
    }
} {}
