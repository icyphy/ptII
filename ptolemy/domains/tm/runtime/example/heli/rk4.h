void rk4(float y[], float dydx[], int n, float x, float h, float yout[],
         void (*derivs)(float, float [], float []));
