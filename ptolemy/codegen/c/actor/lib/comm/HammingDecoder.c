/***preinitBlock***/
    int $actorSymbol(parityMatrix)[$val(uncodedRate)][$val(codedRate)-$val(uncodedRate)];
    int $actorSymbol(syndrome)[$val(codedRate)-$val(uncodedRate)];
    int $actorSymbol(order) = $val(codedRate)-$val(uncodedRate);
    int $actorSymbol(result)[$val(codedRate)];
    int $actorSymbol(flag) = 0;
    int $actorSymbol(pos) = 0;
    int $actorSymbol(eValue);
    int $actorSymbol(eIndex);
    int $actorSymbol(index)[$val(codedRate) + 1];
    int $actorSymbol(i);
    int $actorSymbol(j);
/**/

/***initBlock***/
    // Generate the parity matrix and look-up table.
    for ($actorSymbol(i) = 1; $actorSymbol(i) <= $val(codedRate); $actorSymbol(i)++) {
        if ($actorSymbol(i) == (1 << $actorSymbol(flag))) {
            $actorSymbol(index)[$actorSymbol(i)] = $val(codedRate) - 1 - $actorSymbol(flag);
            $actorSymbol(flag)++;
        } else {
            $actorSymbol(index)[$actorSymbol(i)] = $actorSymbol(pos);
            for ($actorSymbol(j) = 0; $actorSymbol(j) < $actorSymbol(order); $actorSymbol(j)++) {
                $actorSymbol(parityMatrix)[$actorSymbol(pos)][$actorSymbol(j)] = ($actorSymbol(i) >> ($actorSymbol(order) - $actorSymbol(j) - 1)) & 1;
            }
            $actorSymbol(pos)++;
        }
    }
/**/

/***readBlock(<arg>)***/
    $actorSymbol(result)[<arg>] = $ref(input, <arg>);
/**/


/***fireBlock***/
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(order); $actorSymbol(i)++) {
        $actorSymbol(syndrome)[$actorSymbol(i)] = 0;
    }

    $actorSymbol(eValue) = 0;

    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(order); $actorSymbol(i)++) {
        for ($actorSymbol(j) = 0; $actorSymbol(j) < $val(uncodedRate); $actorSymbol(j)++) {
            $actorSymbol(syndrome)[$actorSymbol(i)] = $actorSymbol(syndrome)[$actorSymbol(i)] ^ ($actorSymbol(result)[$actorSymbol(j)] & $actorSymbol(parityMatrix)[$actorSymbol(j)][$actorSymbol(i)]);
        }

        $actorSymbol(syndrome)[$actorSymbol(i)] = $actorSymbol(syndrome)[$actorSymbol(i)] ^ ($actorSymbol(result)[$actorSymbol(i) + $val(uncodedRate)]);
        $actorSymbol(eValue) = ($actorSymbol(eValue) << 1) | $actorSymbol(syndrome)[$actorSymbol(i)];
    }

    $actorSymbol(eIndex) = $actorSymbol(index)[$actorSymbol(eValue)];

    if ($actorSymbol(eIndex) < $val(uncodedRate)) {
        $actorSymbol(result)[$actorSymbol(eIndex)] = !$actorSymbol(result)[$actorSymbol(eIndex)];
    }
/**/

/***writeBlock(<arg>)***/
    $ref(output, <arg>) = $actorSymbol(result)[<arg>];
/**/
