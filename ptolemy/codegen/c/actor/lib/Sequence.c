/****initBlock****
int currentIndex = 0;
int outputProduced = 0;
****initBlock****/



/****codeBlock1****
if (currentIndex < $size(values)) {
    $ref(output) = $ref(values, currentIndex);
    outputProduced = 1;
});
****codeBlock1****/


/****codeBlock2****
if ($ref(enable) != 0 && currentIndex < $size(values)) {
    $ref(output) = $ref(values, currentIndex);
    outputProduced = 1;
}
****codeBlock2****/



/****codeBlock3****
if (outputProduced != 0) {
    outputProduced = 0;
    currentIndex += 1;
    if (currentIndex >= $size(values)) {
        if ($val(repeat) ! = 0) {
           currentIndex = 0;
        } else {
           /*To prevent overflow...*/
           currentIndex = $size(values);
        }
    }
}
****codeBlock3****/
