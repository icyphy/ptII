/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.domains.pthales.JNI;

/**
 * ABF class.
 *
 * @author rbarrere
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ABF implements ABFConstants {
    public static void AddJam(int nb_ant, int nb_rg, int nb_pul,
            float[] sig_in, int nb_ant2, int nb_rg2, int nb_pul2,
            float[] jammed, float CIR, float Power, float Freq,
            float SubArraySpacing, float lambda, float rgsize) {
        ABFJNI.AddJam(nb_ant, nb_rg, nb_pul, sig_in, nb_ant2, nb_rg2, nb_pul2,
                jammed, CIR, Power, Freq, SubArraySpacing, lambda, rgsize);
    }

    public static void AddNoise(int nx, int ny, float[] sig_in, float Sigma2,
            int nx2, int ny2, float[] noisy) {
        ABFJNI.AddNoise(nx, ny, sig_in, Sigma2, noisy);
    }

    public static void Apply_Filter(int Na, int Nx, float[] Sig, int Na2,
            float[] Fil, int Nx2, float[] Out) {
        ABFJNI.Apply_Filter(Na, Nx, Sig, Na2, Fil, Nx2, Out);
    }

    public static double C_get() {
        return ABFJNI.C_get();
    }

    public static void Calc_Chirp(int size, float[] Chirp, int Lchirp,
            float Kchirp) {
        ABFJNI.Calc_Chirp(size, Chirp, Lchirp, Kchirp);
    }

    public static void Calc_Echo(int nb_samp_chirpX4, float[] ChirpX4,
            int nb_ant, int nb_rg, int nb_pul, float[] echo_out, int rg_min,
            float rg_size, float SubArraySpacing, float lambda, float Tpulse,
            float Targ_angle, float Targ_V, float Targ_dist, float Targ_RCS) {
        ABFJNI.Calc_Echo(nb_samp_chirpX4, ChirpX4, nb_ant, nb_rg, nb_pul,
                echo_out, rg_min, rg_size, SubArraySpacing, lambda, Tpulse,
                Targ_angle, Targ_V, Targ_dist, Targ_RCS);
    }

    public static void Calc_SteerVect(int Nb_beams, int Nb_ant,
            float[] SteerOut, float Beamwidth, float SubArraySpacing,
            float lambda) {
        ABFJNI.Calc_SteerVect(Nb_beams, Nb_ant, SteerOut, Beamwidth,
                SubArraySpacing, lambda);
    }

    public static void CalcWeights(int Nb_ants, int Nb_beams, float[] Vec,
            int Nb_ants2, int Nb_ants3, float[] Mat, int Nb_ants4,
            int Nb_beams2, float[] W) {
        ABFJNI.CalcWeights(Nb_ants, Nb_beams, Vec, Nb_ants2, Nb_ants3, Mat,
                Nb_ants4, Nb_beams2, W);
    }

    public static void CovAvCov(int Nb_ant, int Nb_rg, int Nb_pul, float[] In,
            int Nb_ant2, int Nb_ant3, float[] Out) {
        ABFJNI.CovAvCov(Nb_ant, Nb_rg, Nb_pul, In, Nb_ant2, Nb_ant3, Out);
    }

    public static void DecimBy4(int Lfilt4, float[] Filx4, int Lfilt,
            float[] Fil) {
        ABFJNI.DecimBy4(Lfilt4, Filx4, Lfilt, Fil);
    }

    public static void MAT_fft_CF(int N, float[] Xin, int dummy1, int dummy2,
            int N2, float[] Xout) {
        ABFJNI.lazy_FFT(N, Xin, N2, Xout);
    }

    public static void Mat_Invert(int nb_cols, int nb_cols2, float[] Matin,
            int nb_cols3, int nb_cols4, float[] Matout) {
        ABFJNI.Mat_Invert(nb_cols, nb_cols2, Matin, nb_cols3, nb_cols4, Matout);
    }

    public static void Matmat(int Nrows1, int Ncols1, float[] Mat1, int Ncols2,
            int Ncols1_2, float[] Mat2, int Ncols2_2, int Nrows1_2,
            float[] Matprod) {
        ABFJNI.Matmat(Nrows1, Ncols1, Mat1, Ncols2, Ncols1_2, Mat2, Ncols2_2,
                Nrows1_2, Matprod);
    }

    public static void Slid_Filter(int Nrg, float[] Sig, int Lfilt,
            float[] Fil, int out, float[] Out) {
        ABFJNI.Slid_Filter(Nrg, Sig, Lfilt, Fil, out, Out);
    }
}
