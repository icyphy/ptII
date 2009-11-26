package ptolemy.domains.pthales.JNI;

public class ABF implements ABFConstants {
  public static void Calc_Chirp(int Lchirp, float[] Chirp, float Kchirp) {
    ABFJNI.Calc_Chirp(Lchirp,Chirp, Kchirp);
  }
/*
  public static void Calc_Echo(int nb_samp_chirpX4, float[] ChirpX4, int nb_ant, int nb_rg, int nb_pul, SWIGTYPE_p_a_nb_rg__a_nb_ant__float[] echo_out, int rg_min, float rg_size, float SubArraySpacing, float lambda, float Tpulse, float Targ_angle, float Targ_V, float Targ_dist, float Targ_RCS) {
    ABFJNI.Calc_Echo(nb_samp_chirpX4, float[] ChirpX4, ChirpX4, nb_ant, nb_rg, nb_pul, SWIGTYPE_p_a_nb_rg__a_nb_ant__float[].getCPtr(echo_out), rg_min, rg_size, SubArraySpacing, lambda, Tpulse, Targ_angle, Targ_V, Targ_dist, Targ_RCS);
  }

  public static void Calc_SteerVect(int Nb_beams, int Nb_ant, SWIGTYPE_p_a_Nb_ant__float[] SteerOut, float Beamwidth, float SubArraySpacing, float lambda) {
    ABFJNI.Calc_SteerVect(Nb_beams, Nb_ant, SWIGTYPE_p_a_Nb_ant__float[].getCPtr(SteerOut), Beamwidth, SubArraySpacing, lambda);
  }

  public static void DecimBy4(int Lfilt, float[] Filx4, float[] Fil) {
    ABFJNI.DecimBy4(Lfilt, float[].getCPtr(Filx4), Filx4, float[].getCPtr(Fil), Fil);
  }

  public static void AddNoise(int nx, int ny, SWIGTYPE_p_a_nx__float[] sig_in, SWIGTYPE_p_a_nx__float[] noisy, float Sigma2) {
    ABFJNI.AddNoise(nx, ny, SWIGTYPE_p_a_nx__float[].getCPtr(sig_in), SWIGTYPE_p_a_nx__float[].getCPtr(noisy), Sigma2);
  }

  public static void AddJam(int nb_pul, int nb_rg, int nb_ant, SWIGTYPE_p_a_nb_rg__a_nb_ant__float[] sig_in, SWIGTYPE_p_a_nb_rg__a_nb_ant__float[] jammed, float CIR, float Power, float Freq, float SubArraySpacing, float lambda, float rgsize) {
    ABFJNI.AddJam(nb_pul, nb_rg, nb_ant, SWIGTYPE_p_a_nb_rg__a_nb_ant__float[].getCPtr(sig_in), SWIGTYPE_p_a_nb_rg__a_nb_ant__float[].getCPtr(jammed), CIR, Power, Freq, SubArraySpacing, lambda, rgsize);
  }

  public static void turn_1(int X, int Y, SWIGTYPE_p_a_X__float[] IN, SWIGTYPE_p_a_Y__float[] OUT) {
    ABFJNI.turn_1(X, Y, SWIGTYPE_p_a_X__float[].getCPtr(IN), SWIGTYPE_p_a_Y__float[].getCPtr(OUT));
  }

  public static void Slid_Filter(int Nrg, int Lfilt, float[] Sig, float[] Fil, float[] Out) {
    ABFJNI.Slid_Filter(Nrg, Lfilt, float[].getCPtr(Sig), Sig, float[].getCPtr(Fil), Fil, float[].getCPtr(Out), Out);
  }

  public static void turn_2(int Xmax, int Ymax, SWIGTYPE_p_a_Xmax__float[] IN, int Xxfer, int Yxfer, SWIGTYPE_p_a_Yxfer__float[] OUT, int Xoffest, int Yoffset) {
    ABFJNI.turn_2(Xmax, Ymax, SWIGTYPE_p_a_Xmax__float[].getCPtr(IN), Xxfer, Yxfer, SWIGTYPE_p_a_Yxfer__float[].getCPtr(OUT), Xoffest, Yoffset);
  }

  public static void CovAvCov(int Nb_ant, int Nb_rg, int Nb_pul, SWIGTYPE_p_a_nb_rg__a_nb_ant__float[] In, SWIGTYPE_p_a_Nb_ant__float[] Out) {
    ABFJNI.CovAvCov(Nb_ant, Nb_rg, Nb_pul, SWIGTYPE_p_a_nb_rg__a_nb_ant__float[].getCPtr(In), SWIGTYPE_p_a_Nb_ant__float[].getCPtr(Out));
  }

  public static void Mat_Invert(int nb_cols, SWIGTYPE_p_a_nb_cols__float[] Matin, SWIGTYPE_p_a_nb_cols__float[] Matout) {
    ABFJNI.Mat_Invert(nb_cols, SWIGTYPE_p_a_nb_cols__float[].getCPtr(Matin), SWIGTYPE_p_a_nb_cols__float[].getCPtr(Matout));
  }

  public static void Matmat(int Nrows1, int Ncols1, int Ncols2, SWIGTYPE_p_a_Ncols1__float[] Mat1, SWIGTYPE_p_a_Ncols2__float[] Mat2, SWIGTYPE_p_a_Ncols2__float[] Matprod) {
    ABFJNI.Matmat(Nrows1, Ncols1, Ncols2, SWIGTYPE_p_a_Ncols1__float[].getCPtr(Mat1), SWIGTYPE_p_a_Ncols2__float[].getCPtr(Mat2), SWIGTYPE_p_a_Ncols2__float[].getCPtr(Matprod));
  }

  public static void CalcWeights(int Nb_ants, int Nb_beams, SWIGTYPE_p_a_Nb_ants__float[] Vec, SWIGTYPE_p_a_Nb_ants__float[] Mat, SWIGTYPE_p_a_Nb_ants__float[] W) {
    ABFJNI.CalcWeights(Nb_ants, Nb_beams, SWIGTYPE_p_a_Nb_ants__float[].getCPtr(Vec), SWIGTYPE_p_a_Nb_ants__float[].getCPtr(Mat), SWIGTYPE_p_a_Nb_ants__float[].getCPtr(W));
  }

  public static void turn_3(int X, int Y, int Z, SWIGTYPE_p_a_Y__a_X__float[] IN, SWIGTYPE_p_a_Z__a_Y__float[] OUT) {
    ABFJNI.turn_3(X, Y, Z, SWIGTYPE_p_a_Y__a_X__float[].getCPtr(IN), SWIGTYPE_p_a_Z__a_Y__float[].getCPtr(OUT));
  }

  public static void Apply_Filter(int Na, int Nx, SWIGTYPE_p_a_Na__float[] Sig, float[] Fil, float[] Out) {
    ABFJNI.Apply_Filter(Na, Nx, SWIGTYPE_p_a_Na__float[].getCPtr(Sig), float[].getCPtr(Fil), Fil, float[].getCPtr(Out), Out);
  }

  public static void lazy_FFT(int N, float[] Xin, float[] Xout) {
    ABFJNI.lazy_FFT(N, float[].getCPtr(Xin), Xin, float[].getCPtr(Xout), Xout);
  }
  */
}
