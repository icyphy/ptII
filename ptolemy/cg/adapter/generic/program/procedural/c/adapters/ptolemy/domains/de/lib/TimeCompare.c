/***preinitBlock***/
boolean $actorSymbol(nonnegative);
PblList* $actorSymbol(input1TimeStamps);
PblList* $actorSymbol(input2TimeStamps);
/**/

/***initBlock***/
$actorSymbol(nonnegative) = $val(nonnegative);
$actorSymbol(input1TimeStamps) = pblListNewLinkedList();
$actorSymbol(input2TimeStamps) = pblListNewLinkedList();
/**/

/*** prefireBlock ***/
return ($hasToken(input1) || $hasToken(input2));
/**/

/***fireBlock***/
struct Director* director = (*(actor->getDirector))(actor);
while ($hasToken(input1)) {
        $get(input1);
        Time* dynCurrentTime = calloc(1, sizeof(Time));
        if (!dynCurrentTime) {
                fprintf(stderr, "Allocation Error : TimeCompare_fire");
                exit(-1);
        }
        *dynCurrentTime = (*(director->getModelTime))(director);
        pblListAdd($actorSymbol(input1TimeStamps), dynCurrentTime);
}

while ($hasToken(input2)) {
        $get(input2);
        Time* dynCurrentTime = calloc(1, sizeof(Time));
        if (!dynCurrentTime) {
                fprintf(stderr, "Allocation Error : TimeCompare_fire");
                exit(-1);
        }
        *dynCurrentTime = (*(director->getModelTime))(director);
        pblListAdd($actorSymbol(input2TimeStamps), dynCurrentTime);
}

while (!pblListIsEmpty($actorSymbol(input1TimeStamps)) && !pblListIsEmpty($actorSymbol(input2TimeStamps))) {
        double* dynInput1 = pblListPeek($actorSymbol(input1TimeStamps));
        double* dynInput2 = pblListPoll($actorSymbol(input2TimeStamps));
        double input1 = *dynInput1;
        double input2 = *dynInput2;
        free(dynInput2);

        double difference = input2 - input1;

        if ($actorSymbol(nonnegative)) {
                while (difference < 0.0 && !pblListIsEmpty($actorSymbol(input2TimeStamps))) {
                        dynInput2 = pblListPoll($actorSymbol(input2TimeStamps));
                        double input2 = *dynInput2;
                        free(dynInput2);
                        difference = input2 - input1;
                }
                if (difference >= 0.0) {
                        pblListPoll($actorSymbol(input1TimeStamps));
                        free(dynInput1);
                        $put(output, difference);
                }
        } else {
                pblListPoll($actorSymbol(input1TimeStamps));
                free(dynInput1);
                $put(output, difference);
        }
}
/**/

/*** wrapupBlock ***/
pblListFree($actorSymbol(input1TimeStamps));
pblListFree($actorSymbol(input2TimeStamps));
/**/
