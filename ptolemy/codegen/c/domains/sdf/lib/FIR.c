/*** preinitBlock ***/
Token $actorSymbol(_data);
Token $actorSymbol(_zero);
int $actorSymbol(_mostRecent);
int $actorSymbol(_phaseLength);
Token $actorSymbol(_outToken);
Token $actorSymbol(_tapItem);
Token $actorSymbol(_dataItem);
int $actorSymbol(inC);
int $actorSymbol(phase);
int $actorSymbol(length);
int $actorSymbol(i);
int $actorSymbol(tapsIndex);
int $actorSymbol(dataIndex);
int $actorSymbol(bufferIndex);		// for keeping track of the output offset in a single firing.
/**/



/*** initBlock ***/
$actorSymbol(_zero) = $tokenFunc(Array_get($ref(taps), 0)::zero());

$actorSymbol(_phaseLength) = $ref(taps).payload.Array->size / $val(interpolation);

if (($ref(taps).payload.Array->size % $val(interpolation)) != 0) {
    $actorSymbol(_phaseLength)++;
}

// Create new data array and initialize index into it.
// Avoid losing the data if possible.
// NOTE: If the filter length increases, then it is impossible
// to correctly initialize the delay line to contain previously
// seen data, because that data has not been saved.
$actorSymbol(length) = $actorSymbol(_phaseLength) + $val(decimation);

$actorSymbol(_data) = $new(Array($actorSymbol(length), 0));

for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(length); $actorSymbol(i)++) {
    Array_set($actorSymbol(_data), $actorSymbol(i), $actorSymbol(_zero));
}
$actorSymbol(_mostRecent) = $actorSymbol(_phaseLength);
/**/



/*** fireBlock ***/
$actorSymbol(bufferIndex) = 0;

// Phase keeps track of which phase of the filter coefficients
// are used. Starting phase depends on the $val(decimationPhase) value.
$actorSymbol(phase) = $val(decimation) - $val(decimationPhase) - 1;

// Transfer decimation inputs to _data[]
for ($actorSymbol(inC) = 1; $actorSymbol(inC) <= $val(decimation); $actorSymbol(inC)++) {
    if (--$actorSymbol(_mostRecent) < 0) {
        $actorSymbol(_mostRecent) = $actorSymbol(_data).payload.Array->size - 1;
    }

    // Note explicit type conversion, which is required to generate
    // code.
    Array_set($actorSymbol(_data), $actorSymbol(_mostRecent), $ref((Token) input));
}

// Interpolate once for each input consumed
for ($actorSymbol(inC) = 1; $actorSymbol(inC) <= $val(decimation); $actorSymbol(inC)++) {
    // Produce however many outputs are required
    // for each input consumed
    while ($actorSymbol(phase) < $val(interpolation)) {
        $actorSymbol(_outToken) = $actorSymbol(_zero);

        // Compute the inner product.
        for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_phaseLength); $actorSymbol(i)++) {
            $actorSymbol(tapsIndex) = ($actorSymbol(i) * $val(interpolation)) + $actorSymbol(phase);

            $actorSymbol(dataIndex) = (($actorSymbol(_mostRecent) + $val(decimation)) - $actorSymbol(inC) + $actorSymbol(i)) % ($actorSymbol(_data).payload.Array->size);

            if ($actorSymbol(tapsIndex) < $ref(taps).payload.Array->size) {
                $actorSymbol(_tapItem) = Array_get($ref(taps), $actorSymbol(tapsIndex));
                $actorSymbol(_dataItem) = Array_get($actorSymbol(_data), $actorSymbol(dataIndex));
                $actorSymbol(_dataItem) = $tokenFunc($actorSymbol(_tapItem)::multiply($actorSymbol(_dataItem)));
                $actorSymbol(_outToken) = $tokenFunc($actorSymbol(_outToken)::add($actorSymbol(_dataItem)));
            }

            // else assume tap is zero, so do nothing.
        }

        $ref(output, ($actorSymbol(bufferIndex)++)) = $actorSymbol(_outToken).payload.$cgType(output);
        $actorSymbol(phase) += $val(decimation);
    }

    $actorSymbol(phase) -= $val(interpolation);
}
/**/

/*** wrapupBlock ***/
Array_delete($actorSymbol(_data));
/**/
