/*** sharedBlock ***/
        $super();
        int $actorClass(i);
        int $actorClass(index);
        double $actorClass(factor);
/**/

/*** sharedBlock2 ***/
        $targetType(input) $actorClass(datum_$targetType(input));
/**/

/*** initBlock ***/
        $actorSymbol(_taps) = $typeFunc(TYPE_Array::clone($ref(taps)));
        $super();
/**/

/*** fireBlock ***/
        // First update the taps.
        $actorClass(index) = ($val(errorDelay) * $val(decimation)) + $val(decimationPhase);

        $actorClass(factor) = $ref(error) * $val(stepSize);

        for ($actorClass(i) = 0; $actorClass(i) < $size(taps); $actorClass(i)++) {
            // The data item to use here should be "index" in the past,
            // where an index of zero would be the current input.
            $actorClass(datum_$targetType(input)) = Array_get($actorSymbol(_data), (($actorSymbol(_mostRecent) + $actorClass(index) - 1) % $actorClass(length))).payload.$cgType(input);
            $actorSymbol(_taps).payload.Array->elements[$actorClass(i)].payload.$cgType(input) += ($actorClass(factor) * $actorClass(datum_$targetType(input)));
            $actorClass(index)++;
        }

        // Update the tapValues output.
        // NOTE: This may be a relatively costly operation to be doing here.
        $ref(tapValues) = $actorSymbol(_taps);

        // Then run FIR filter
        $super.fireBlock0();
        $super();
/**/

/*** wrapupBlock ***/
        Array_delete($actorSymbol(_taps));
        $super();
/**/
