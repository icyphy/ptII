// Simple test program for gmp, used by mescal.
#include <gmpxx.h>
//#include <iostream.h>
int main (void) {
    mpz_class a, b, c;

    a = 1234;
    b = "-5678";
    c = a+b;
    // cout << "sum is " << c << "\n";
    // cout << "absolute value is " << abs(c) << "\n";

    return 0;
}

