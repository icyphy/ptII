/*** preinitBlock($zero) ***/
    int $actorSymbol(startCopy);
    int $actorSymbol(length);
    int $actorSymbol(destination);
    int $actorSymbol(pastBufferIndex);
    int $actorSymbol(i);

    int $actorSymbol(_highLimit);
    int $actorSymbol(_lowLimit);
    int $actorSymbol(_inputIndex);
    $targetType(input) $actorSymbol(_pastBuffer)[$val(offset) > 0 ? $val(offset) : 0];
    $targetType(input) $actorSymbol(zero) = $zero;
/**/

/*** sharedBlock ***/
    $super.arraycopyBlock($targetType(input))
/**/

/*** initBlock ***/
        if ($val(usePastInputs) && $val(offset) > 0) {
            // Fill past buffer with zeros.
            for ($actorSymbol(i) = 0; $actorSymbol(i) < $val(offset); $actorSymbol(i)++) {
                $actorSymbol(_pastBuffer)[$actorSymbol(i)] = $actorSymbol(zero);
            }
        }

        if ($val(offset) >= 0) {
            $actorSymbol(_lowLimit) = $val(offset);
            $actorSymbol(_inputIndex) = 0;
        } else {
            $actorSymbol(_lowLimit) = 0;
            $actorSymbol(_inputIndex) = -($val(offset));
        }

        $actorSymbol(_highLimit) = ($val(offset) + $val(numberToRead)) - 1;

        if ($actorSymbol(_highLimit) >= $val(numberToWrite)) {
            $actorSymbol(_highLimit) = $val(numberToWrite) - 1;
        }

/**/


/*** fireBlock ***/
        int $actorSymbol(inputIndex) = $actorSymbol(_inputIndex);
        $actorSymbol(pastBufferIndex) = 0;
        //Token[] inBuffer = input.get(0, $val(numberToRead));

        for ($actorSymbol(i) = 0; $actorSymbol(i) < $val(numberToWrite); $actorSymbol(i)++) {
            if ($actorSymbol(i) > $actorSymbol(_highLimit)) {
                    $ref(output, $actorSymbol(i)) = $actorSymbol(zero);
                //_buffer[i] = zero;

            } else if ($actorSymbol(i) < $actorSymbol(_lowLimit)) {

                if ($val(usePastInputs)) {
                            $ref(output, $actorSymbol(i)) = $actorSymbol(_pastBuffer)[$actorSymbol(pastBufferIndex)++];
                    //_buffer[i] = _pastBuffer[pastBufferIndex++];

                } else {
                            $ref(output, $actorSymbol(i)) = $actorSymbol(zero);
                    //_buffer[i] = zero;
                }
            } else {
                // FIXME: This will access past samples...
                    $ref(output, $actorSymbol(i)) = $ref(input, $actorSymbol(inputIndex));
                //_buffer[i] = inBuffer[inputIndex];

                $actorSymbol(inputIndex)++;
            }
        }

        if ($val(usePastInputs) && ($val(offset) > 0)) {
            // Copy input buffer into past buffer.  Have to be careful
            // here because the buffer might be longer than the
            // input window.

            $actorSymbol(startCopy) = $val(numberToRead) - $val(offset);
            $actorSymbol(length) = $val(offset);
            $actorSymbol(destination) = 0;

            if ($actorSymbol(startCopy) < 0) {
                // Shift older data.
                $actorSymbol(destination) = $val(offset) - $val(numberToRead);

                                $targetType(input)_arraycopy($actorSymbol(_pastBuffer), $val(numberToRead), $actorSymbol(_pastBuffer), 0, $actorSymbol(destination));
                //System.arraycopy(_pastBuffer, _numberToRead, _pastBuffer, 0, destination);

                $actorSymbol(startCopy) = 0;
                $actorSymbol(length) = $val(numberToRead);
            }

                        $targetType(input)_arraycopy(&$ref(input), $actorSymbol(startCopy), $actorSymbol(_pastBuffer), $actorSymbol(destination), $actorSymbol(length));
            //System.arraycopy(inBuffer, startCopy, _pastBuffer, destination, length);
        }
/**/
