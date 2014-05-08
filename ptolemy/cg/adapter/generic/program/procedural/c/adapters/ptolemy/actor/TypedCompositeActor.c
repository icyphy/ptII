/***variableDeclareBlock***/
$include("$ModelName()_types.h")
$include("$ModelName()__Receiver.h")
$include("$ModelName()__Queue.h")
$include("$ModelName()__Actor.h")
$include("$ModelName()__Director.h")
/**/

/***containerSetBlock($containerName, $DirectorName, $nbAtomicActors, $nbCompositeActors)***/

// Set of the container
CompositeActorSet(&$containerName, &$DirectorName, $nbAtomicActors, $nbCompositeActors,
                $containerName_constructorActors, $containerName_constructorPorts, $containerName_constructorReceivers);
/**/

/***actorSet($actorName, $containerName, $nbPorts, $depth, $priority)***/

// Set of the actor $actorName
ActorSet(&($actorName), $containerName, $nbPorts, $depth, $priority,
                $actorName_preinitialize, $actorName_initialize,
                $actorName_prefire, $actorName_fire,
                $actorName_postfire, $actorName_wrapup);
/**/

/***compositeActorSet($actorName, $containerName, $nbPorts, $depth, $priority)***/

// Set of the composite actor $actorName
ActorSet(&($actorName.actor), $containerName, $nbPorts, $depth, $priority,
                $actorName_preinitialize, $actorName_initialize,
                $actorName_prefire, $actorName_fire,
                $actorName_postfire, $actorName_wrapup);
/**/

/***atomicActorsSet($containerName, $i, $actorName)***/

// Set of the atomic actor $i
$containerName.containedAtomicActors[$i] = &$actorName;
/**/

/***IOPortSet($actorName, $j, $portName, $portType, $isInput, $isOutput, $isMultiPort, $widthInside, $widthOutside)***/

IOPortSet($actorName.ports + $j, &$actorName,
                $portName, $portType, $isInput, $isOutput, $isMultiPort, $widthInside, $widthOutside);
/**/

/***ReceiverSetBlock($actorName, $i, $channel)***/
ReceiverSetReceiver($actorName.ports[$i].receivers + $channel,
                        $actorName.ports + $i);
/**/

/***FarReceiverSetBlock($actorName, $i, $channel, $farActorName, $FarActorNameForArgs, $farActorPortName, $farChannel)***/
ReceiverSetRemoteFarReceiver($actorName.ports[$i].farReceivers + $channel,
                $FarActorNameForArgs.ports[enum_$farActorName_$farActorPortName].receivers + $farChannel);
/**/

/***preinitializeBlock($director)***/
(*($director->preinitializeFunction))();
/**/


/***initializeBlock($director, $currentActor)***/
// Note that this is assured of firing the local director,
// not the executive director, because this is opaque.
// The initialize() method of the local director must be called
// after the ports are cleared, because the FixedPointDirector
// relies on this to reset the status of its receivers.
//(*($director->initializeFunction))();
(*($director->initialize))($director);
//$currentActor_TransferOutputs();
// $director->transferOutputs($director);
/**/

/***prefireBlock($director)***/
boolean result = (*($director->prefireFunction))();
return result;
/**/


/***fireBlock($director, $currentActor)***/
$currentActor_TransferInputs();

(*($director->fireFunction))();

// Use the local director to transfer outputs.
$currentActor_TransferOutputs();
/**/

/***postFireBlock($director)***/
boolean result = true;
// Note that this is assured of firing the local director,
// not the executive director, because this is opaque.
result = result && (*($director->postfireFunction))();
return result;
/**/

/***WrapupBlock($director)***/
(*($director->wrapupFunction))();
/**/
