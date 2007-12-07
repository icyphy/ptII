// This is an example of an exported function.
double fnTest(long* in, long* inout) {
  if (in[0] == 1) {
    inout[0] = 6;
  } else {
    inout[0] = -6;
  }
  if (in[1] == 1) {
    inout[1] = -8;
  } else {
    inout[1] = 8;
  }
  return (double)1.0;
}

