/****initBlock****
int sum = 0;
int count = 0; 
****initBlock****/

/****codeBlock1****
if ($ref(reset)) {
    sum = 0;
    count = 0;
} else {
    sum += $ref(input);
    count++;
    $ref(output) = sum / count;
}
****codeBlock1****/