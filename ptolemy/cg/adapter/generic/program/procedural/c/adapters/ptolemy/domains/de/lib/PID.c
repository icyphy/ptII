/***preinitBlock***/
struct TimedEvent {
        Time timestamp;
        Token token;
};

Token $actorSymbol(Kp);
Token $actorSymbol(Ki);
Token $actorSymbol(Kd);
struct TimedEvent * $actorSymbol(currentInput);
struct TimedEvent * $actorSymbol(lastInput);
double $actorSymbol(accumulated);

/**/

/***initBlock($Kp, $Ki, $Kd)***/
// Initialization of the parameters
$actorSymbol(Kp).type = TYPE_Double;
$actorSymbol(Kp).payload.Double = $Kp;
$actorSymbol(Ki).type = TYPE_Double;
$actorSymbol(Ki).payload.Double = $Ki;
$actorSymbol(Kd).type = TYPE_Double;
$actorSymbol(Kd).payload.Double = $Kd;
$actorSymbol(lastInput) = NULL;
$actorSymbol(currentInput) = NULL;
/**/

/***customFireBlock***/
// Consume input, generate output only if input provided.
struct Director* director = (*(actor->getDirector))(actor);
if ($hasToken(input)) {
        Time currentTime = (*(director->getModelTime))(director);
        double currentToken = $get(input);
        $actorSymbol(currentInput) = calloc(1, sizeof(struct TimedEvent));
        if ($actorSymbol(currentInput) == NULL) {
                fprintf(stderr, "Allocation Error : PID_Fire");
                exit(-1);
        }
        $actorSymbol(currentInput)->timestamp = currentTime;
        $actorSymbol(currentInput)->token.type = TYPE_Double;
        $actorSymbol(currentInput)->token.payload.Double = currentToken;

        // Add proportional component to controller output.
        double currentOutput = currentToken * $actorSymbol(Kp).payload.Double;

        // If a previous input was given, then add integral and
        // derivative components.
        if ($actorSymbol(lastInput) != NULL) {
                double lastToken = $actorSymbol(lastInput)->token.payload.Double;
                Time lastTime = $actorSymbol(lastInput)->timestamp;
                Token timeGap;
                timeGap.type = TYPE_Double;
                timeGap.payload.Double = currentTime - lastTime;

                Token zeroDoubleToken;
                zeroDoubleToken.type = TYPE_Double;
                zeroDoubleToken.payload.Double = 0.0;

                Token epsilonDoubleToken;
                epsilonDoubleToken.type = TYPE_Double;
                epsilonDoubleToken.payload.Double = 0.000000001;
                //If the timeGap is zero, then we have received a
                // simultaneous event. If the value of the input has
                // not changed, then we can ignore this input, as a
                // control signal was already generated. However if
                // the value has changed, then the signal is
                // discontinuous and we should throw an exception
                // unless derivative control is disabled (Kd=0).

                if (Double_isCloseTo(&timeGap, &zeroDoubleToken, &epsilonDoubleToken)->payload.Boolean) {
                        if (!Double_isCloseTo(&$actorSymbol(Kd), &zeroDoubleToken, &epsilonDoubleToken)->payload.Boolean
                                        && currentToken == lastToken) {
                                fprintf(stderr, "PID controller recevied discontinuous input.");
                                exit(-1);
                        }
                }
                // Otherwise, the signal is continuous and we add
                // integral and derivative components.
                else {
                        if (!(Double_isCloseTo(&$actorSymbol(Ki), &zeroDoubleToken, &epsilonDoubleToken)->payload.Boolean)) {
                                //Calculate integral component and accumulate
                                $actorSymbol(accumulated) = $actorSymbol(accumulated) + ((currentToken + lastToken) * timeGap.payload.Double * 0.5);

                                // Add integral component to controller output.
                                currentOutput = currentOutput + $actorSymbol(accumulated) * $actorSymbol(Ki).payload.Double;
                        }

                        // Add derivative component to controller output.
                        if (!(Double_isCloseTo(&$actorSymbol(Kd), &zeroDoubleToken, &epsilonDoubleToken)->payload.Boolean)) {
                                currentOutput = currentOutput + $actorSymbol(Kd).payload.Double * (currentToken - lastToken) / timeGap.payload.Double;
                        }
                }
        }

        $put(output, currentOutput);
}
/**/

/***resetConnectedBlock***/
if ($hasToken(reset#0)) {
        // Consume reset token.
        $get(reset#0);

        // Reset the current input.
        if ($actorSymbol(currentInput) != NULL)
                free($actorSymbol(currentInput));

        // Reset accumulation.
        $actorSymbol(accumulated) = 0.0;
}
/**/

/***postFireBlock***/
if ($actorSymbol(lastInput))
        free($actorSymbol(lastInput));
$actorSymbol(lastInput) = $actorSymbol(currentInput);
$actorSymbol(currentInput) = NULL;
return true;
/**/

/***wrapupBlock***/
if ($actorSymbol(currentInput) != NULL) {
        free($actorSymbol(currentInput));
        $actorSymbol(currentInput) = NULL;
}

if ($actorSymbol(lastInput) != NULL) {
        free($actorSymbol(lastInput));
        $actorSymbol(lastInput) = NULL;
}
/**/
