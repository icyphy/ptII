/*** preinitBlock() ***/
int $actorSymbol(i);
Complex $actorSymbol(_inComplexArray)[];
Complex $actorSymbol(_outComplexArray)[];
Token $actorSymbol(_outTokenArray);
int $actorSymbol(_transformSize);
/**/

/*** initBlock() ***/
$actorSymbol(_inComplexArray) = new Complex[$actorSymbol(_transformSize)];
int $actorSymbol(_transformSize) = (int) Math.pow(2, $val(order));
$actorSymbol(_outTokenArray) = $new(Array($actorSymbol(_transformSize), 0));
/**/

/*** fireBlock() ***/
for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_transformSize); $actorSymbol(i)++) {
    Token token = $get(input, $actorSymbol(_transformSize));
    $actorSymbol(_inComplexArray)[$actorSymbol(i)] = new Complex(((ComplexCG)token.payload).real, ((ComplexCG)token.payload).imag);
}

{
    Complex $actorSymbol(outComplexArray)[] = ptolemy.math.SignalProcessing.FFTComplexOut($actorSymbol(_inComplexArray), $val(order));

    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_transformSize); $actorSymbol(i)++) {
        Complex complex = $actorSymbol(outComplexArray)[$actorSymbol(i)];
        ((Array)$actorSymbol(_outTokenArray).payload).elements[$actorSymbol(i)] = $Complex_new(complex.real, complex.imag);
    }
}

$put(output, $actorSymbol(_outTokenArray));
/**/

