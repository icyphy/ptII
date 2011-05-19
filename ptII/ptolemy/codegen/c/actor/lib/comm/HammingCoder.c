/***preinitBlock***/
int $actorSymbol(parityMatrix)[$val(uncodedRate)][$val(codedRate)-$val(uncodedRate)];
int $actorSymbol(parity)[$val(codedRate)-$val(uncodedRate)];
int $actorSymbol(order) = $val(codedRate)-$val(uncodedRate);
int $actorSymbol(result)[$val(codedRate)];
int $actorSymbol(flag);
int $actorSymbol(index);
int $actorSymbol(i);
int $actorSymbol(j);
/**/

/***initBlock***/
$actorSymbol(flag) = 0;
$actorSymbol(index) = 0;

for ($actorSymbol(i) = 1; $actorSymbol(i) <= $val(codedRate); $actorSymbol(i)++) {
    if ($actorSymbol(i) == (1 << $actorSymbol(flag))) {
        $actorSymbol(flag)++;
    } else {
        for ($actorSymbol(j) = 0; $actorSymbol(j) < $actorSymbol(order); $actorSymbol(j)++) {
            $actorSymbol(parityMatrix)[$actorSymbol(index)][$actorSymbol(j)] = ($actorSymbol(i) >> ($actorSymbol(order) - $actorSymbol(j) - 1)) & 1;
        }
        $actorSymbol(index)++;
    }
}
/**/

/***fireBlock***/
//read
for ($actorSymbol(i) = 0; $actorSymbol(i) < $val(uncodedRate); $actorSymbol(i)++) {
    $actorSymbol(result)[$actorSymbol(i)] = $ref(input, $actorSymbol(i));
}

for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(order); $actorSymbol(i)++) {
    $actorSymbol(parity)[$actorSymbol(i)] = 0;
}

for ($actorSymbol(i) = 0; $actorSymbol(i) < $val(uncodedRate); $actorSymbol(i)++) {
    for ($actorSymbol(j) = 0; $actorSymbol(j) < $actorSymbol(order); $actorSymbol(j)++) {
        $actorSymbol(parity)[$actorSymbol(j)] = $actorSymbol(parity)[$actorSymbol(j)] ^ ($actorSymbol(result)[$actorSymbol(i)] & $actorSymbol(parityMatrix)[$actorSymbol(i)][$actorSymbol(j)]);
    }
}

for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(order); $actorSymbol(i)++) {
    $actorSymbol(result)[$actorSymbol(i) + $val(uncodedRate)] = ($actorSymbol(parity)[$actorSymbol(i)] == 1) ? 1 : 0;
}

//write
for ($actorSymbol(i) = 0; $actorSymbol(i) < $val(codedRate); $actorSymbol(i)++) {
        $ref(output, $actorSymbol(i)) = $actorSymbol(result)[$actorSymbol(i)];
}
/**/
