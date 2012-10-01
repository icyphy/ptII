/***fireBlock($delayInitValue)***/
static double delayValue = $delayInitValue;

if ($hasToken(delay)) {
        delayValue = $get(delay);
}

Timestamp addTime = delayValue;

$put(output#0, $get(input));

/**/
