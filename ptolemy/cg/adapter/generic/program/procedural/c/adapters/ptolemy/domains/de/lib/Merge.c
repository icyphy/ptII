/***preinitBlock***/
// True if there are more tokens on other channels.
static boolean $actorSymbol(moreTokensOnOtherChannels);

// Last time this actor was fired.
static Time $actorSymbol(previousModelTime);

// Mircostep of director during last firing of this actor.
static int $actorSymbol(previousMicrostep);

//A flag to indicate whether the input events can be discarded.
boolean $actorSymbol(discardEvents);
/**/

/***initBlock($discardEvents)***/
$actorSymbol(moreTokensOnOtherChannels) = false;
$actorSymbol(discardEvents) = $discardEvents;
/**/


/***prefireBlock***/
// Return false if there was a firing at the current time and
// microstep that produced an output token and there are more
// tokens on other channels that are waiting to be produced at
// the same time but future microsteps.
if($actorSymbol(moreTokensOnOtherChannels)
		&& director.currentModelTime == $actorSymbol(previousModelTime)
		&& director.currentMicrostep == $actorSymbol(previousMicrostep)) {
	return false;
}
return true;
/**/

/***fireBeginBlock***/
$actorSymbol(previousModelTime) = director.currentModelTime;
$actorSymbol(previousMicrostep) = director.currentMicrostep;
$actorSymbol(moreTokensOnOtherChannels) = false;

Token firstAvailableToken;
firstAvailableToken.type = -2;

// If tokens can be discarded, this actor sends
// out the first available tokens only. It discards all
// remaining tokens from other input channels.
// Otherwise, this actor handles one channel at each firing
// and requests refiring at the current time to handle the
// the remaining channels that have tokens.
/**/

/***fireLoopBlock($channel)***/
if ($hasToken(input#$channel)) {
	if (firstAvailableToken.type == -2) {
		// we see the first available tokens
		firstAvailableToken.type = TYPE_$cgType(input);
		firstAvailableToken.payload.$cgType(input) = $get(input#$channel);
		$put(output#0, firstAvailableToken.payload.$cgType(input));

		while ($hasToken(input#$channel)) {
			Token token;
			token.type = TYPE_$cgType(input);
			token.payload.$cgType(input) = $get(input#$channel);
			$put(output#0, token.payload.$cgType(input));
		}
	} else {
		if ($actorSymbol(discardEvents)) {
			// this token is not the first available token
			// in this firing, consume and discard all tokens
			// from the input channel
			while ($hasToken(input#$channel)) {
				$get(input#$channel);
			}
		} else {
			// Refiring the actor to handle the other tokens
			// that are still in channels
			$fireAt(&director, $actorName(), director.currentModelTime, director.currentMicrostep+1);
			$actorSymbol(moreTokensOnOtherChannels) = true;
			return;
		}
	}
}
/**/
