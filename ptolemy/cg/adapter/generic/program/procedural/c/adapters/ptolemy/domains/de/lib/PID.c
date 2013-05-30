/***preinitBlock***/
/** Proportional gain of the controller. Default value is 1.0.
**/
Token $actorSymbol(Kp);

/** Integral gain of the controller. Default value is 0.0,
*  which disables integral control.
**/
Token $actorSymbol(Ki);

/** Derivative gain of the controller. Default value is 0.0, which disables
*  derivative control. If Kd=0.0, this actor can receive discontinuous
*  signals as input; otherwise, if Kd is nonzero and a discontinuous signal
*  is received, an exception will be thrown.
*/
Token $actorSymbol(Kd);
$include("$ModelName()_DEEvent.h")
static DEEvent * $actorSymbol(currentInput);

static DEEvent * $actorSymbol(lastInput);

static double $actorSymbol(accumulated);

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
if ($hasToken(input)) {
	Time currentTime = $DirectorName()->currentModelTime;
	double currentToken = $get(input);
	$actorSymbol(currentInput) = newDEEventWithParam(NULL, NULL, 0, 0, 0, currentTime);
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

		if (Double_isCloseTo(timeGap, zeroDoubleToken, epsilonDoubleToken).payload.Boolean) {
			if (!Double_isCloseTo($actorSymbol(Kd), zeroDoubleToken, epsilonDoubleToken).payload.Boolean
					&& currentToken == lastToken) {
				perror("PID controller recevied discontinuous input.");
				exit(1);
			}
		}
		// Otherwise, the signal is continuous and we add
		// integral and derivative components.
		else {
			if (!(Double_isCloseTo($actorSymbol(Ki), zeroDoubleToken, epsilonDoubleToken).payload.Boolean)) {
				//Calculate integral component and accumulate
				$actorSymbol(accumulated) = $actorSymbol(accumulated) + ((currentToken + lastToken) * timeGap.payload.Double * 0.5);

				// Add integral component to controller output.
				currentOutput = currentOutput + $actorSymbol(accumulated) * $actorSymbol(Ki).payload.Double;
			}

			// Add derivative component to controller output.
			if (!(Double_isCloseTo($actorSymbol(Kd), zeroDoubleToken, epsilonDoubleToken).payload.Boolean)) {
				currentOutput = currentOutput + $actorSymbol(Kd).payload.Double * (currentToken - lastToken) / timeGap.payload.Double;
			}
		}
	}

	$put(output, currentOutput);
}
/**/

/***postFireBlock($resetConnected)***/
//If reset port is connected and has a token, reset state.
if ($resetConnected) {
	if ($hasToken(reset#0)) {
		// Consume reset token.
		$get(reset#0);

		// Reset the current input.
		if ($actorSymbol(currentInput) != NULL)
			free($actorSymbol(currentInput));

		// Reset accumulation.
		$actorSymbol(accumulated) = 0.0;
	}
}
$actorSymbol(lastInput) = $actorSymbol(currentInput);
$actorSymbol(currentInput) = NULL;
return;
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
