/***preinitBlock***/
// True if there are more tokens on other channels.
boolean $actorSymbol(moreTokensOnOtherChannels);

// Last time this actor was fired.
Time $actorSymbol(previousModelTime);

// Mircostep of director during last firing of this actor.
int $actorSymbol(previousMicrostep);

//A flag to indicate whether the input events can be discarded.
boolean $actorSymbol(discardEvents);
/**/

/***initBlock($discardEvents)***/
$actorSymbol(moreTokensOnOtherChannels) = false;
$actorSymbol(discardEvents) = $discardEvents;
/**/


/***prefireBlock***/
struct Director* director = (*(actor->getDirector))(actor);
if($actorSymbol(moreTokensOnOtherChannels)
                && (*(director->getModelTime))(director) == $actorSymbol(previousModelTime)
                && (*(((struct DEDirector*)director)->getMicrostep))((struct DEDirector*)director) == $actorSymbol(previousMicrostep)) {
        return false;
}
return true;
/**/

/***fireBeginBlock***/
struct Director* director = (*(actor->getDirector))(actor);
$actorSymbol(previousModelTime) = (*(director->getModelTime))(director);
$actorSymbol(previousMicrostep) = (*(((struct DEDirector*)director)->getMicrostep))((struct DEDirector*)director);
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

                Token* token = malloc(sizeof(Token));
                while ($hasToken(input#$channel)) {
                        token->type = TYPE_$cgType(input);
                        token->payload.$cgType(input) = $get(input#$channel);
                        $put(output#0, token->payload.$cgType(input));
                }
                free(token);
        } else {
                if ($actorSymbol(discardEvents)) {
                        // this token is not the first available token
                        // in this firing, consume and discard all tokens
                        // from the input channel
                        while ($hasToken(input#$channel)) {
                                (void)$get(input#$channel);
                        }
                } else {
                        // Refiring the actor to handle the other tokens
                        // that are still in channels
                        $fireAt(actor, (*(director->getModelTime))(director),
                                        (*(((struct DEDirector*)director)->getMicrostep))((struct DEDirector*)director)+1);
                        $actorSymbol(moreTokensOnOtherChannels) = true;
                        return;
                }
        }
}
/**/
