/* User:      cxh
   Date:      Wed Jun 29 15:58:09 2005
   Target:    default-CGC
   Universe:  alive */

/* Define macro for prototyping functions on ANSI & non-ANSI compilers */
#ifndef ARGS
#if defined(__STDC__) || defined(__cplusplus)
#define ARGS(args) args
#else
#define ARGS(args) ()
#endif
#endif

#include <math.h>
#include <audio.h>

/* Define constants TRUE and FALSE for portability */
#ifndef TRUE
#define TRUE 1
#endif
#ifndef FALSE
#define FALSE 0
#endif

/* Define a complex data type if one has not been defined */
#if !defined(COMPLEX_DATA)
#define COMPLEX_DATA 1
typedef struct complex_data { double real; double imag; } complex;
#endif

#define CGC_MOD(a,b) ((a)>=(b) ? (a-b) : (a))

extern main ARGS((int argc, char *argv[]));

	ALport port_40;
	ALconfig config_41;
	ALport port_42;
	ALconfig config_43;
/* main function */
int main(int argc, char *argv[]) {
	/* static so that buffer will be initialized to zero */
	double buffer_35[1];
	int index_36;
	    double t_37;
	    double t_38;
	    double t_39;
double output_0;
double output_1;
double out_2;
double output_3;
double output_4;
double output_5;
double state_55;
double output_6;
int output_7;
double output_8;
double state_49;
double output_9;
double output_10;
int output_11;
double output_12;
double state_48;
double output_13;
double output_14;
int output_15;
double output_16;
double output_17;
double output_18;
double output_19;
double output_24;
double output_25;
double output_26;
double f_44[3];
double k_45[3];
double b_46[3];
double e_47[3];
double residual_27;
double k_50[9];
double B_51[9];
double f_52[9];
double b_53[9];
double e_54[9];
double residual_28;
double synthOut_29;
double left_30;
double right_31;
double out_32;
{int i; for(i=0;i<9;i++) k_50[i]=0.0;}
{int i; for(i=0;i<9;i++) B_51[i]=0.0;}
{int i; for(i=0;i<9;i++) f_52[i]=0.0;}
{int i; for(i=0;i<9;i++) b_53[i]=0.0;}
{int i; for(i=0;i<9;i++) e_54[i]=0.0;}
f_44[0]=0.0;
f_44[1]=0.0;
f_44[2]=0.0;
k_45[0]=0.0;
k_45[1]=0.0;
k_45[2]=0.0;
b_46[0]=0.0;
b_46[1]=0.0;
b_46[2]=0.0;
e_47[0]=0.0;
e_47[1]=0.0;
e_47[2]=0.0;
state_48=0.0;
state_49=0.0;
state_55=0.0;
output_0 = 0.0;
	index_36 = 0;
    {
	int i;
	for (i = 0 ; i < 1 ; i++)
	    buffer_35[i] = 0;
    }
output_1 = 0.0;
out_2 = 0.0;
output_3 = 0.0;
output_4 = 0.0;
output_5 = 0.0;
output_6 = 0.0;
output_7 = 0;
output_8 = 0.0;
output_9 = 0.0;
output_10 = 0.0;
output_11 = 0;
output_12 = 0.0;
output_13 = 0.0;
output_14 = 0.0;
output_15 = 0;
output_16 = 0.0;
output_17 = 0.0;
output_18 = 0.0;
output_19 = 0.0;
output_24 = 0.0;
output_25 = 0.0;
output_26 = 0.0;
residual_27 = 0.0;
residual_28 = 0.0;
synthOut_29 = 0.0;
left_30 = 0.0;
right_31 = 0.0;
	config_41 = ALnewconfig();
	ALsetwidth(config_41, AL_SAMPLE_16);
	ALsetchannels(config_41, AL_STEREO);
	ALsetqueuesize(config_41, 0x1000);
	port_40 = ALopenport("port_40", "r", config_41);
out_32 = 0.0;
	config_43 = ALnewconfig();
	ALsetwidth(config_43, AL_SAMPLE_16);
	ALsetchannels(config_43, AL_STEREO);
	ALsetqueuesize(config_43, 0x1000);
	port_42 = ALopenport("port_42", "w", config_43);
while(1) {
	{  /* star alive.rms1.Const1 (class CGCConst) */
	output_5 = 0.0;
	}
	{  /* star alive.rms1.splice_CGCFloatToInt_0 (class CGCFloatToInt) */
	int i = 0;
	for (; i < 1; i++) {
		output_7 = (int) floor(output_5 + 0.5);
	}
	}
	{  /* star alive.fm1.oscillator1.ConstInt1 (class CGCConstInt) */
	output_11 = 0.0;
	}
	{  /* star alive.fm1.oscillator2.ConstInt1 (class CGCConstInt) */
	output_15 = 0.0;
	}
	{  /* star alive.Const1 (class CGCConst) */
	output_24 = 2.0;
	}
	{  /* star alive.Const2 (class CGCConst) */
	output_25 = 2.0;
	}
	{  /* star alive.SGImonoIn1.SGIAudioIn1 (class CGCSGIAudioIn) */
	{
	    short buffer[2];

	    ALreadsamps(port_40, buffer, 2);
	    left_30 = buffer[0] / 32768.0;
	    right_31 = buffer[1] / 32768.0;
	}
	}
	{  /* star alive.SGImonoIn1.Expr.in=21 (class CGCExpr) */
out_32 = (left_30 + right_31)/2.0;
	}
	{  /* star alive.Delay1 (class CGCDelay) */
	output_0 = buffer_35[index_36];
	buffer_35[index_36] = out_32;
	if ( ++index_36 >= 1 )
	    index_36 -= 1;
	}
	{  /* star alive.GAL1 (class CGCGAL) */
	{
	    int m;

	    /* Update forward errors. */
	    f_44[0] = output_0;
	    for(m = 1; m <= 2; m++)
	    {
	       f_44[m] = f_44[m-1] - k_45[m] * b_46[m-1];
	    }

	    /* Update backward errors, reflection coefficients. */
	    for(m = 2; m > 0; m--)
	    {
		b_46[m] = b_46[m-1] - k_45[m]*f_44[m-1];
		e_47[m] *= 1.0 - 0.00124921923797627;
		e_47[m] += 0.00124921923797627 * (f_44[m-1]*f_44[m-1] + b_46[m-1]*b_46[m-1]);
		if (e_47[m] != 0.0)
		{
		    k_45[m] += 0.00124921923797627 * (f_44[m]*b_46[m-1] + b_46[m]*f_44[m-1]) / e_47[m];
		    if (k_45[m] > 1.0) k_45[m] = 1.0;
		    if (k_45[m] < -1.0) k_45[m] = -1.0;
		}
	    }

	    b_46[0] = output_0;
	    residual_27 =  f_44[2];
	}
	}
	{  /* star alive.Const3 (class CGCConst) */
	output_26 = 0.172787595947438;
	}
	{  /* star alive.fm1.Fork.output=21 (class CGCFork) */
	}
	{  /* star alive.fm1.Gain2 (class CGCGain) */
output_19 = 1.0 * output_26;
	}
	{  /* star alive.fm1.Gain1 (class CGCGain) */
output_18 = 2.0 * output_26;
	}
	{  /* star alive.fm1.auto-fork-node1 (class CGCFork) */
	}
	{  /* star alive.fm1.oscillator2.Integrator1 (class CGCIntegrator) */
	    if (output_15 != 0) {
		t_39 = output_18;
	    } else {
		t_39 = output_18 +
			1.0 * state_48;
	    }
	    /* Limiting is in effect */
	    /* Take care of the top */
	    if (t_39 > 3.14159265358979)
		do t_39 -= (3.14159265358979 - -3.14159265358979);
		while (t_39 > 3.14159265358979);
	    /* Take care of the bottom */
	    if (t_39 < -3.14159265358979)
		do t_39 += (3.14159265358979 - -3.14159265358979);
		while (t_39 < -3.14159265358979);
	    output_13 = t_39;
	    state_48 = t_39;
	}
	{  /* star alive.fm1.oscillator2.Cos1 (class CGCCos) */
	output_14 = cos(output_13);
	}
	{  /* star alive.fm1.Mpy.input=21 (class CGCMpy) */
	output_17 = output_24 * output_18;
	}
	{  /* star alive.fm1.oscillator2.Mpy.input=21 (class CGCMpy) */
	output_12 = output_14 * output_17;
	}
	{  /* star alive.fm1.Add.input=21 (class CGCAdd) */
	output_16 = output_12 + output_19;
	}
	{  /* star alive.fm1.oscillator1.Integrator1 (class CGCIntegrator) */
	    if (output_11 != 0) {
		t_38 = output_16;
	    } else {
		t_38 = output_16 +
			1.0 * state_49;
	    }
	    /* Limiting is in effect */
	    /* Take care of the top */
	    if (t_38 > 3.14159265358979)
		do t_38 -= (3.14159265358979 - -3.14159265358979);
		while (t_38 > 3.14159265358979);
	    /* Take care of the bottom */
	    if (t_38 < -3.14159265358979)
		do t_38 += (3.14159265358979 - -3.14159265358979);
		while (t_38 < -3.14159265358979);
	    output_9 = t_38;
	    state_49 = t_38;
	}
	{  /* star alive.fm1.oscillator1.Cos1 (class CGCCos) */
	output_10 = cos(output_9);
	}
	{  /* star alive.fm1.oscillator1.Mpy.input=21 (class CGCMpy) */
	output_8 = output_10 * output_25;
	}
	{  /* star alive.Mpy.input=21 (class CGCMpy) */
	output_1 = output_8 * output_4;
	}
	{  /* star alive.GGAL1 (class CGCGGAL) */
	{
	    double F;
	    int m;

	    F = output_1;
	    for(m = 8-1; m >= 0; m--)
	    {
		F += k_50[m+1] * B_51[m];
		B_51[m+1] = B_51[m] - k_50[m+1] * F;
	    }
	    B_51[0] = F;
	    synthOut_29 = F;
	}
	{
	    int m;

	    /* Update forward errors. */
	    f_52[0] = residual_27;
	    for(m = 1; m <= 8; m++)
	    {
	       f_52[m] = f_52[m-1] - k_50[m] * b_53[m-1];
	    }

	    /* Update backward errors, reflection coefficients. */
	    for(m = 8; m > 0; m--)
	    {
		b_53[m] = b_53[m-1] - k_50[m]*f_52[m-1];
		e_54[m] *= 1.0 - 0.0124223602484472;
		e_54[m] += 0.0124223602484472 * (f_52[m-1]*f_52[m-1] + b_53[m-1]*b_53[m-1]);
		if (e_54[m] != 0.0)
		{
		    k_50[m] += 0.0124223602484472 * (f_52[m]*b_53[m-1] + b_53[m]*f_52[m-1]) / e_54[m];
		    if (k_50[m] > 1.0) k_50[m] = 1.0;
		    if (k_50[m] < -1.0) k_50[m] = -1.0;
		}
	    }

	    b_53[0] = residual_27;
	    residual_28 =  f_52[8];
	}
	}
	{  /* star alive.rms1.square1.Expr.in=11 (class CGCExpr) */
out_2 = residual_28 * residual_28;
	}
	{  /* star alive.rms1.Gain1 (class CGCGain) */
output_3 = 0.0124223602484472 * out_2;
	}
	{  /* star alive.rms1.Integrator1 (class CGCIntegrator) */
	    if (output_7 != 0) {
		t_37 = output_3;
	    } else {
		t_37 = output_3 +
			0.987577639751553 * state_55;
	    }
	    output_6 = t_37;
	    state_55 = t_37;
	}
	{  /* star alive.SGImonoOut1.Fork.output=21 (class CGCFork) */
	}
	{  /* star alive.SGImonoOut1.SGIAudioOut1 (class CGCSGIAudioOut) */
	{
	    short buffer[2];

	    buffer[0] = (short)(synthOut_29 * 32768.0);
	    buffer[1] = (short)(synthOut_29* 32768.0);
	    ALwritesamps(port_42, buffer, 2);
	}
	}
	{  /* star alive.rms1.Sqrt1 (class CGCSqrt) */
	output_4 = sqrt(output_6);
	}
} /* end while, depth 0*/
	ALcloseport(port_40);
	ALfreeconfig(config_41);
	ALcloseport(port_42);
	ALfreeconfig(config_43);

return 1;
}
