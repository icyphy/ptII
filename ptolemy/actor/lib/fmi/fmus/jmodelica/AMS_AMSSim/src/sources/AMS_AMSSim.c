/* Run-time. */
#include "stdio.h"
#include "stdlib.h"
#include "math.h"
#include "jmi.h"
#include "jmi_block_residual.h"
#include "jmi_log.h"
#include "ModelicaUtilities.h"
#include "ModelicaStandardTables.h"

#include "fmi2_me.h"
#include "fmi2_cs.h"
#include "fmi2Functions.h"
#include "fmi2FunctionTypes.h"
#include "fmi2TypesPlatform.h"

/* Generated code. */


extern void dgemm_(char* TRANSA, char* TRANSB, int* M, int* N, int* K, double* ALPHA, double* A, int* LDA, double* B, int* LDB, double* BETA, double* C, int* LDC);

const char *C_GUID = "fc9ab008db6240a0335abf0ab37c4c35";

static int model_ode_guards_init(jmi_t* jmi);
static int model_init_R0(jmi_t* jmi, jmi_real_t** res);
static int model_ode_initialize(jmi_t* jmi);

static const int N_real_ci = 6;
static const int N_real_cd = 0;
static const int N_real_pi = 287;
static const int N_real_pi_s = 0;
static const int N_real_pi_f = 0;
static const int N_real_pi_e = 0;
static const int N_real_pd = 33;

static const int N_integer_ci = 0 + 0;
static const int N_integer_cd = 0 + 0;
static const int N_integer_pi = 7 + 0;
static const int N_integer_pi_s = 0 + 0;
static const int N_integer_pi_f = 0 + 0;
static const int N_integer_pi_e = 0 + 0;
static const int N_integer_pd = 0 + 0;

static const int N_boolean_ci = 0;
static const int N_boolean_cd = 0;
static const int N_boolean_pi = 8;
static const int N_boolean_pi_s = 0;
static const int N_boolean_pi_f = 0;
static const int N_boolean_pi_e = 0;
static const int N_boolean_pd = 0;

static const int N_string_ci = 0;
static const int N_string_cd = 0;
static const int N_string_pi = 0;
static const int N_string_pi_s = 0;
static const int N_string_pi_f = 0;
static const int N_string_pi_e = 0;
static const int N_string_pd = 0;

static const int N_real_dx = 4;
static const int N_real_x = 4;
static const int N_real_u = 0;
static const int N_real_w = 18;

static const int N_real_d = 0;

static const int N_integer_d = 0 + 0;
static const int N_integer_u = 0 + 0;

static const int N_boolean_d = 0;
static const int N_boolean_u = 0;

static const int N_string_d = 0;
static const int N_string_u = 0;

static const int N_ext_objs = 0;

static const int N_time_sw = 0;
static const int N_state_sw = 0;
static const int N_sw = 0 + 0;
static const int N_delay_sw = 0;
static const int N_eq_F = 22;
static const int N_eq_R = 0;

static const int N_dae_blocks = 2;
static const int N_dae_init_blocks = 2;
static const int N_guards = 0;

static const int N_eq_F0 = 22 + 4;
static const int N_eq_F1 = 22;
static const int N_eq_Fp = 0;
static const int N_eq_R0 = 0 + 0;
static const int N_sw_init = 0;
static const int N_guards_init = 0;

static const int N_delays = 0;
static const int N_spatialdists = 0;

static const int Scaling_method = JMI_SCALING_NONE;

#define sf(i) (jmi->variable_scaling_factors[i])

static const int N_outputs = 0;
static const int Output_vrefs[1] = {-1};

static int CAD_dae_n_nz = 1;
static const int CAD_dae_nz_rows[1] = {-1};
static const int CAD_dae_nz_cols[1] = {-1};


static const int CAD_ODE_A_n_nz = 0;
static const int CAD_ODE_B_n_nz = 0;
static const int CAD_ODE_C_n_nz = 0;
static const int CAD_ODE_D_n_nz = 0;
static const int CAD_ODE_A_nz_rows[1] = {-1};
static const int CAD_ODE_A_nz_cols[1] = {-1};
static const int CAD_ODE_B_nz_rows[1] = {-1};
static const int CAD_ODE_B_nz_cols[1] = {-1};
static const int CAD_ODE_C_nz_rows[1] = {-1};
static const int CAD_ODE_C_nz_cols[1] = {-1};
static const int CAD_ODE_D_nz_rows[1] = {-1};
static const int CAD_ODE_D_nz_cols[1] = {-1};


static const int N_initial_relations = 0;
static const int DAE_initial_relations[] = { -1 };

static const int N_relations = 0;
static const int DAE_relations[] = { -1 };

static const int N_nominals = 4;
static const jmi_real_t DAE_nominals[] = { 1.0, 1.0, 1.0, 1.0 };

#define _valve1_C_4 ((*(jmi->z))[jmi->offs_real_ci+0])
#define _valve2_C_12 ((*(jmi->z))[jmi->offs_real_ci+1])
#define _W4_308 ((*(jmi->z))[jmi->offs_real_ci+2])
#define _ambientair_port_w_321 ((*(jmi->z))[jmi->offs_real_ci+3])
#define _hx_wc_322 ((*(jmi->z))[jmi->offs_real_ci+4])
#define _hx_cin_w_325 ((*(jmi->z))[jmi->offs_real_ci+5])
#define _inlet_p_env_0 ((*(jmi->z))[jmi->offs_real_pi+0])
#define _inlet_t_env_1 ((*(jmi->z))[jmi->offs_real_pi+1])
#define _fork_v_5 ((*(jmi->z))[jmi->offs_real_pi+2])
#define _ambientair_p_env_13 ((*(jmi->z))[jmi->offs_real_pi+3])
#define _ambientair_t_env_14 ((*(jmi->z))[jmi->offs_real_pi+4])
#define _hx_m_17 ((*(jmi->z))[jmi->offs_real_pi+5])
#define _hx_wc_grid_1_18 ((*(jmi->z))[jmi->offs_real_pi+6])
#define _hx_wc_grid_2_19 ((*(jmi->z))[jmi->offs_real_pi+7])
#define _hx_wc_grid_3_20 ((*(jmi->z))[jmi->offs_real_pi+8])
#define _hx_wc_grid_4_21 ((*(jmi->z))[jmi->offs_real_pi+9])
#define _hx_wc_grid_5_22 ((*(jmi->z))[jmi->offs_real_pi+10])
#define _hx_wc_grid_6_23 ((*(jmi->z))[jmi->offs_real_pi+11])
#define _hx_wc_grid_7_24 ((*(jmi->z))[jmi->offs_real_pi+12])
#define _hx_wc_grid_8_25 ((*(jmi->z))[jmi->offs_real_pi+13])
#define _hx_wc_grid_9_26 ((*(jmi->z))[jmi->offs_real_pi+14])
#define _hx_wc_grid_10_27 ((*(jmi->z))[jmi->offs_real_pi+15])
#define _hx_wc_grid_11_28 ((*(jmi->z))[jmi->offs_real_pi+16])
#define _hx_wh_grid_1_29 ((*(jmi->z))[jmi->offs_real_pi+17])
#define _hx_wh_grid_2_30 ((*(jmi->z))[jmi->offs_real_pi+18])
#define _hx_wh_grid_3_31 ((*(jmi->z))[jmi->offs_real_pi+19])
#define _hx_wh_grid_4_32 ((*(jmi->z))[jmi->offs_real_pi+20])
#define _hx_wh_grid_5_33 ((*(jmi->z))[jmi->offs_real_pi+21])
#define _hx_wh_grid_6_34 ((*(jmi->z))[jmi->offs_real_pi+22])
#define _hx_wh_grid_7_35 ((*(jmi->z))[jmi->offs_real_pi+23])
#define _hx_wh_grid_8_36 ((*(jmi->z))[jmi->offs_real_pi+24])
#define _hx_wh_grid_9_37 ((*(jmi->z))[jmi->offs_real_pi+25])
#define _hx_wh_grid_10_38 ((*(jmi->z))[jmi->offs_real_pi+26])
#define _hx_wh_grid_11_39 ((*(jmi->z))[jmi->offs_real_pi+27])
#define _hx_th_grid_1_40 ((*(jmi->z))[jmi->offs_real_pi+28])
#define _hx_th_grid_2_41 ((*(jmi->z))[jmi->offs_real_pi+29])
#define _hx_th_grid_3_42 ((*(jmi->z))[jmi->offs_real_pi+30])
#define _hx_th_grid_4_43 ((*(jmi->z))[jmi->offs_real_pi+31])
#define _hx_th_grid_5_44 ((*(jmi->z))[jmi->offs_real_pi+32])
#define _hx_th_grid_6_45 ((*(jmi->z))[jmi->offs_real_pi+33])
#define _hx_th_grid_7_46 ((*(jmi->z))[jmi->offs_real_pi+34])
#define _hx_th_grid_8_47 ((*(jmi->z))[jmi->offs_real_pi+35])
#define _hx_th_grid_9_48 ((*(jmi->z))[jmi->offs_real_pi+36])
#define _hx_th_grid_10_49 ((*(jmi->z))[jmi->offs_real_pi+37])
#define _hx_tc_grid_1_50 ((*(jmi->z))[jmi->offs_real_pi+38])
#define _hx_tc_grid_2_51 ((*(jmi->z))[jmi->offs_real_pi+39])
#define _hx_tc_grid_3_52 ((*(jmi->z))[jmi->offs_real_pi+40])
#define _hx_tc_grid_4_53 ((*(jmi->z))[jmi->offs_real_pi+41])
#define _hx_tc_grid_5_54 ((*(jmi->z))[jmi->offs_real_pi+42])
#define _hx_tc_grid_6_55 ((*(jmi->z))[jmi->offs_real_pi+43])
#define _hx_tc_grid_7_56 ((*(jmi->z))[jmi->offs_real_pi+44])
#define _hx_tc_grid_8_57 ((*(jmi->z))[jmi->offs_real_pi+45])
#define _hx_tc_grid_9_58 ((*(jmi->z))[jmi->offs_real_pi+46])
#define _hx_tc_grid_10_59 ((*(jmi->z))[jmi->offs_real_pi+47])
#define _hx_hc_grid_1_1_60 ((*(jmi->z))[jmi->offs_real_pi+48])
#define _hx_hc_grid_1_2_61 ((*(jmi->z))[jmi->offs_real_pi+49])
#define _hx_hc_grid_1_3_62 ((*(jmi->z))[jmi->offs_real_pi+50])
#define _hx_hc_grid_1_4_63 ((*(jmi->z))[jmi->offs_real_pi+51])
#define _hx_hc_grid_1_5_64 ((*(jmi->z))[jmi->offs_real_pi+52])
#define _hx_hc_grid_1_6_65 ((*(jmi->z))[jmi->offs_real_pi+53])
#define _hx_hc_grid_1_7_66 ((*(jmi->z))[jmi->offs_real_pi+54])
#define _hx_hc_grid_1_8_67 ((*(jmi->z))[jmi->offs_real_pi+55])
#define _hx_hc_grid_1_9_68 ((*(jmi->z))[jmi->offs_real_pi+56])
#define _hx_hc_grid_1_10_69 ((*(jmi->z))[jmi->offs_real_pi+57])
#define _hx_hc_grid_2_1_70 ((*(jmi->z))[jmi->offs_real_pi+58])
#define _hx_hc_grid_2_2_71 ((*(jmi->z))[jmi->offs_real_pi+59])
#define _hx_hc_grid_2_3_72 ((*(jmi->z))[jmi->offs_real_pi+60])
#define _hx_hc_grid_2_4_73 ((*(jmi->z))[jmi->offs_real_pi+61])
#define _hx_hc_grid_2_5_74 ((*(jmi->z))[jmi->offs_real_pi+62])
#define _hx_hc_grid_2_6_75 ((*(jmi->z))[jmi->offs_real_pi+63])
#define _hx_hc_grid_2_7_76 ((*(jmi->z))[jmi->offs_real_pi+64])
#define _hx_hc_grid_2_8_77 ((*(jmi->z))[jmi->offs_real_pi+65])
#define _hx_hc_grid_2_9_78 ((*(jmi->z))[jmi->offs_real_pi+66])
#define _hx_hc_grid_2_10_79 ((*(jmi->z))[jmi->offs_real_pi+67])
#define _hx_hc_grid_3_1_80 ((*(jmi->z))[jmi->offs_real_pi+68])
#define _hx_hc_grid_3_2_81 ((*(jmi->z))[jmi->offs_real_pi+69])
#define _hx_hc_grid_3_3_82 ((*(jmi->z))[jmi->offs_real_pi+70])
#define _hx_hc_grid_3_4_83 ((*(jmi->z))[jmi->offs_real_pi+71])
#define _hx_hc_grid_3_5_84 ((*(jmi->z))[jmi->offs_real_pi+72])
#define _hx_hc_grid_3_6_85 ((*(jmi->z))[jmi->offs_real_pi+73])
#define _hx_hc_grid_3_7_86 ((*(jmi->z))[jmi->offs_real_pi+74])
#define _hx_hc_grid_3_8_87 ((*(jmi->z))[jmi->offs_real_pi+75])
#define _hx_hc_grid_3_9_88 ((*(jmi->z))[jmi->offs_real_pi+76])
#define _hx_hc_grid_3_10_89 ((*(jmi->z))[jmi->offs_real_pi+77])
#define _hx_hc_grid_4_1_90 ((*(jmi->z))[jmi->offs_real_pi+78])
#define _hx_hc_grid_4_2_91 ((*(jmi->z))[jmi->offs_real_pi+79])
#define _hx_hc_grid_4_3_92 ((*(jmi->z))[jmi->offs_real_pi+80])
#define _hx_hc_grid_4_4_93 ((*(jmi->z))[jmi->offs_real_pi+81])
#define _hx_hc_grid_4_5_94 ((*(jmi->z))[jmi->offs_real_pi+82])
#define _hx_hc_grid_4_6_95 ((*(jmi->z))[jmi->offs_real_pi+83])
#define _hx_hc_grid_4_7_96 ((*(jmi->z))[jmi->offs_real_pi+84])
#define _hx_hc_grid_4_8_97 ((*(jmi->z))[jmi->offs_real_pi+85])
#define _hx_hc_grid_4_9_98 ((*(jmi->z))[jmi->offs_real_pi+86])
#define _hx_hc_grid_4_10_99 ((*(jmi->z))[jmi->offs_real_pi+87])
#define _hx_hc_grid_5_1_100 ((*(jmi->z))[jmi->offs_real_pi+88])
#define _hx_hc_grid_5_2_101 ((*(jmi->z))[jmi->offs_real_pi+89])
#define _hx_hc_grid_5_3_102 ((*(jmi->z))[jmi->offs_real_pi+90])
#define _hx_hc_grid_5_4_103 ((*(jmi->z))[jmi->offs_real_pi+91])
#define _hx_hc_grid_5_5_104 ((*(jmi->z))[jmi->offs_real_pi+92])
#define _hx_hc_grid_5_6_105 ((*(jmi->z))[jmi->offs_real_pi+93])
#define _hx_hc_grid_5_7_106 ((*(jmi->z))[jmi->offs_real_pi+94])
#define _hx_hc_grid_5_8_107 ((*(jmi->z))[jmi->offs_real_pi+95])
#define _hx_hc_grid_5_9_108 ((*(jmi->z))[jmi->offs_real_pi+96])
#define _hx_hc_grid_5_10_109 ((*(jmi->z))[jmi->offs_real_pi+97])
#define _hx_hc_grid_6_1_110 ((*(jmi->z))[jmi->offs_real_pi+98])
#define _hx_hc_grid_6_2_111 ((*(jmi->z))[jmi->offs_real_pi+99])
#define _hx_hc_grid_6_3_112 ((*(jmi->z))[jmi->offs_real_pi+100])
#define _hx_hc_grid_6_4_113 ((*(jmi->z))[jmi->offs_real_pi+101])
#define _hx_hc_grid_6_5_114 ((*(jmi->z))[jmi->offs_real_pi+102])
#define _hx_hc_grid_6_6_115 ((*(jmi->z))[jmi->offs_real_pi+103])
#define _hx_hc_grid_6_7_116 ((*(jmi->z))[jmi->offs_real_pi+104])
#define _hx_hc_grid_6_8_117 ((*(jmi->z))[jmi->offs_real_pi+105])
#define _hx_hc_grid_6_9_118 ((*(jmi->z))[jmi->offs_real_pi+106])
#define _hx_hc_grid_6_10_119 ((*(jmi->z))[jmi->offs_real_pi+107])
#define _hx_hc_grid_7_1_120 ((*(jmi->z))[jmi->offs_real_pi+108])
#define _hx_hc_grid_7_2_121 ((*(jmi->z))[jmi->offs_real_pi+109])
#define _hx_hc_grid_7_3_122 ((*(jmi->z))[jmi->offs_real_pi+110])
#define _hx_hc_grid_7_4_123 ((*(jmi->z))[jmi->offs_real_pi+111])
#define _hx_hc_grid_7_5_124 ((*(jmi->z))[jmi->offs_real_pi+112])
#define _hx_hc_grid_7_6_125 ((*(jmi->z))[jmi->offs_real_pi+113])
#define _hx_hc_grid_7_7_126 ((*(jmi->z))[jmi->offs_real_pi+114])
#define _hx_hc_grid_7_8_127 ((*(jmi->z))[jmi->offs_real_pi+115])
#define _hx_hc_grid_7_9_128 ((*(jmi->z))[jmi->offs_real_pi+116])
#define _hx_hc_grid_7_10_129 ((*(jmi->z))[jmi->offs_real_pi+117])
#define _hx_hc_grid_8_1_130 ((*(jmi->z))[jmi->offs_real_pi+118])
#define _hx_hc_grid_8_2_131 ((*(jmi->z))[jmi->offs_real_pi+119])
#define _hx_hc_grid_8_3_132 ((*(jmi->z))[jmi->offs_real_pi+120])
#define _hx_hc_grid_8_4_133 ((*(jmi->z))[jmi->offs_real_pi+121])
#define _hx_hc_grid_8_5_134 ((*(jmi->z))[jmi->offs_real_pi+122])
#define _hx_hc_grid_8_6_135 ((*(jmi->z))[jmi->offs_real_pi+123])
#define _hx_hc_grid_8_7_136 ((*(jmi->z))[jmi->offs_real_pi+124])
#define _hx_hc_grid_8_8_137 ((*(jmi->z))[jmi->offs_real_pi+125])
#define _hx_hc_grid_8_9_138 ((*(jmi->z))[jmi->offs_real_pi+126])
#define _hx_hc_grid_8_10_139 ((*(jmi->z))[jmi->offs_real_pi+127])
#define _hx_hc_grid_9_1_140 ((*(jmi->z))[jmi->offs_real_pi+128])
#define _hx_hc_grid_9_2_141 ((*(jmi->z))[jmi->offs_real_pi+129])
#define _hx_hc_grid_9_3_142 ((*(jmi->z))[jmi->offs_real_pi+130])
#define _hx_hc_grid_9_4_143 ((*(jmi->z))[jmi->offs_real_pi+131])
#define _hx_hc_grid_9_5_144 ((*(jmi->z))[jmi->offs_real_pi+132])
#define _hx_hc_grid_9_6_145 ((*(jmi->z))[jmi->offs_real_pi+133])
#define _hx_hc_grid_9_7_146 ((*(jmi->z))[jmi->offs_real_pi+134])
#define _hx_hc_grid_9_8_147 ((*(jmi->z))[jmi->offs_real_pi+135])
#define _hx_hc_grid_9_9_148 ((*(jmi->z))[jmi->offs_real_pi+136])
#define _hx_hc_grid_9_10_149 ((*(jmi->z))[jmi->offs_real_pi+137])
#define _hx_hc_grid_10_1_150 ((*(jmi->z))[jmi->offs_real_pi+138])
#define _hx_hc_grid_10_2_151 ((*(jmi->z))[jmi->offs_real_pi+139])
#define _hx_hc_grid_10_3_152 ((*(jmi->z))[jmi->offs_real_pi+140])
#define _hx_hc_grid_10_4_153 ((*(jmi->z))[jmi->offs_real_pi+141])
#define _hx_hc_grid_10_5_154 ((*(jmi->z))[jmi->offs_real_pi+142])
#define _hx_hc_grid_10_6_155 ((*(jmi->z))[jmi->offs_real_pi+143])
#define _hx_hc_grid_10_7_156 ((*(jmi->z))[jmi->offs_real_pi+144])
#define _hx_hc_grid_10_8_157 ((*(jmi->z))[jmi->offs_real_pi+145])
#define _hx_hc_grid_10_9_158 ((*(jmi->z))[jmi->offs_real_pi+146])
#define _hx_hc_grid_10_10_159 ((*(jmi->z))[jmi->offs_real_pi+147])
#define _hx_hc_grid_11_1_160 ((*(jmi->z))[jmi->offs_real_pi+148])
#define _hx_hc_grid_11_2_161 ((*(jmi->z))[jmi->offs_real_pi+149])
#define _hx_hc_grid_11_3_162 ((*(jmi->z))[jmi->offs_real_pi+150])
#define _hx_hc_grid_11_4_163 ((*(jmi->z))[jmi->offs_real_pi+151])
#define _hx_hc_grid_11_5_164 ((*(jmi->z))[jmi->offs_real_pi+152])
#define _hx_hc_grid_11_6_165 ((*(jmi->z))[jmi->offs_real_pi+153])
#define _hx_hc_grid_11_7_166 ((*(jmi->z))[jmi->offs_real_pi+154])
#define _hx_hc_grid_11_8_167 ((*(jmi->z))[jmi->offs_real_pi+155])
#define _hx_hc_grid_11_9_168 ((*(jmi->z))[jmi->offs_real_pi+156])
#define _hx_hc_grid_11_10_169 ((*(jmi->z))[jmi->offs_real_pi+157])
#define _hx_hh_grid_1_1_170 ((*(jmi->z))[jmi->offs_real_pi+158])
#define _hx_hh_grid_1_2_171 ((*(jmi->z))[jmi->offs_real_pi+159])
#define _hx_hh_grid_1_3_172 ((*(jmi->z))[jmi->offs_real_pi+160])
#define _hx_hh_grid_1_4_173 ((*(jmi->z))[jmi->offs_real_pi+161])
#define _hx_hh_grid_1_5_174 ((*(jmi->z))[jmi->offs_real_pi+162])
#define _hx_hh_grid_1_6_175 ((*(jmi->z))[jmi->offs_real_pi+163])
#define _hx_hh_grid_1_7_176 ((*(jmi->z))[jmi->offs_real_pi+164])
#define _hx_hh_grid_1_8_177 ((*(jmi->z))[jmi->offs_real_pi+165])
#define _hx_hh_grid_1_9_178 ((*(jmi->z))[jmi->offs_real_pi+166])
#define _hx_hh_grid_1_10_179 ((*(jmi->z))[jmi->offs_real_pi+167])
#define _hx_hh_grid_2_1_180 ((*(jmi->z))[jmi->offs_real_pi+168])
#define _hx_hh_grid_2_2_181 ((*(jmi->z))[jmi->offs_real_pi+169])
#define _hx_hh_grid_2_3_182 ((*(jmi->z))[jmi->offs_real_pi+170])
#define _hx_hh_grid_2_4_183 ((*(jmi->z))[jmi->offs_real_pi+171])
#define _hx_hh_grid_2_5_184 ((*(jmi->z))[jmi->offs_real_pi+172])
#define _hx_hh_grid_2_6_185 ((*(jmi->z))[jmi->offs_real_pi+173])
#define _hx_hh_grid_2_7_186 ((*(jmi->z))[jmi->offs_real_pi+174])
#define _hx_hh_grid_2_8_187 ((*(jmi->z))[jmi->offs_real_pi+175])
#define _hx_hh_grid_2_9_188 ((*(jmi->z))[jmi->offs_real_pi+176])
#define _hx_hh_grid_2_10_189 ((*(jmi->z))[jmi->offs_real_pi+177])
#define _hx_hh_grid_3_1_190 ((*(jmi->z))[jmi->offs_real_pi+178])
#define _hx_hh_grid_3_2_191 ((*(jmi->z))[jmi->offs_real_pi+179])
#define _hx_hh_grid_3_3_192 ((*(jmi->z))[jmi->offs_real_pi+180])
#define _hx_hh_grid_3_4_193 ((*(jmi->z))[jmi->offs_real_pi+181])
#define _hx_hh_grid_3_5_194 ((*(jmi->z))[jmi->offs_real_pi+182])
#define _hx_hh_grid_3_6_195 ((*(jmi->z))[jmi->offs_real_pi+183])
#define _hx_hh_grid_3_7_196 ((*(jmi->z))[jmi->offs_real_pi+184])
#define _hx_hh_grid_3_8_197 ((*(jmi->z))[jmi->offs_real_pi+185])
#define _hx_hh_grid_3_9_198 ((*(jmi->z))[jmi->offs_real_pi+186])
#define _hx_hh_grid_3_10_199 ((*(jmi->z))[jmi->offs_real_pi+187])
#define _hx_hh_grid_4_1_200 ((*(jmi->z))[jmi->offs_real_pi+188])
#define _hx_hh_grid_4_2_201 ((*(jmi->z))[jmi->offs_real_pi+189])
#define _hx_hh_grid_4_3_202 ((*(jmi->z))[jmi->offs_real_pi+190])
#define _hx_hh_grid_4_4_203 ((*(jmi->z))[jmi->offs_real_pi+191])
#define _hx_hh_grid_4_5_204 ((*(jmi->z))[jmi->offs_real_pi+192])
#define _hx_hh_grid_4_6_205 ((*(jmi->z))[jmi->offs_real_pi+193])
#define _hx_hh_grid_4_7_206 ((*(jmi->z))[jmi->offs_real_pi+194])
#define _hx_hh_grid_4_8_207 ((*(jmi->z))[jmi->offs_real_pi+195])
#define _hx_hh_grid_4_9_208 ((*(jmi->z))[jmi->offs_real_pi+196])
#define _hx_hh_grid_4_10_209 ((*(jmi->z))[jmi->offs_real_pi+197])
#define _hx_hh_grid_5_1_210 ((*(jmi->z))[jmi->offs_real_pi+198])
#define _hx_hh_grid_5_2_211 ((*(jmi->z))[jmi->offs_real_pi+199])
#define _hx_hh_grid_5_3_212 ((*(jmi->z))[jmi->offs_real_pi+200])
#define _hx_hh_grid_5_4_213 ((*(jmi->z))[jmi->offs_real_pi+201])
#define _hx_hh_grid_5_5_214 ((*(jmi->z))[jmi->offs_real_pi+202])
#define _hx_hh_grid_5_6_215 ((*(jmi->z))[jmi->offs_real_pi+203])
#define _hx_hh_grid_5_7_216 ((*(jmi->z))[jmi->offs_real_pi+204])
#define _hx_hh_grid_5_8_217 ((*(jmi->z))[jmi->offs_real_pi+205])
#define _hx_hh_grid_5_9_218 ((*(jmi->z))[jmi->offs_real_pi+206])
#define _hx_hh_grid_5_10_219 ((*(jmi->z))[jmi->offs_real_pi+207])
#define _hx_hh_grid_6_1_220 ((*(jmi->z))[jmi->offs_real_pi+208])
#define _hx_hh_grid_6_2_221 ((*(jmi->z))[jmi->offs_real_pi+209])
#define _hx_hh_grid_6_3_222 ((*(jmi->z))[jmi->offs_real_pi+210])
#define _hx_hh_grid_6_4_223 ((*(jmi->z))[jmi->offs_real_pi+211])
#define _hx_hh_grid_6_5_224 ((*(jmi->z))[jmi->offs_real_pi+212])
#define _hx_hh_grid_6_6_225 ((*(jmi->z))[jmi->offs_real_pi+213])
#define _hx_hh_grid_6_7_226 ((*(jmi->z))[jmi->offs_real_pi+214])
#define _hx_hh_grid_6_8_227 ((*(jmi->z))[jmi->offs_real_pi+215])
#define _hx_hh_grid_6_9_228 ((*(jmi->z))[jmi->offs_real_pi+216])
#define _hx_hh_grid_6_10_229 ((*(jmi->z))[jmi->offs_real_pi+217])
#define _hx_hh_grid_7_1_230 ((*(jmi->z))[jmi->offs_real_pi+218])
#define _hx_hh_grid_7_2_231 ((*(jmi->z))[jmi->offs_real_pi+219])
#define _hx_hh_grid_7_3_232 ((*(jmi->z))[jmi->offs_real_pi+220])
#define _hx_hh_grid_7_4_233 ((*(jmi->z))[jmi->offs_real_pi+221])
#define _hx_hh_grid_7_5_234 ((*(jmi->z))[jmi->offs_real_pi+222])
#define _hx_hh_grid_7_6_235 ((*(jmi->z))[jmi->offs_real_pi+223])
#define _hx_hh_grid_7_7_236 ((*(jmi->z))[jmi->offs_real_pi+224])
#define _hx_hh_grid_7_8_237 ((*(jmi->z))[jmi->offs_real_pi+225])
#define _hx_hh_grid_7_9_238 ((*(jmi->z))[jmi->offs_real_pi+226])
#define _hx_hh_grid_7_10_239 ((*(jmi->z))[jmi->offs_real_pi+227])
#define _hx_hh_grid_8_1_240 ((*(jmi->z))[jmi->offs_real_pi+228])
#define _hx_hh_grid_8_2_241 ((*(jmi->z))[jmi->offs_real_pi+229])
#define _hx_hh_grid_8_3_242 ((*(jmi->z))[jmi->offs_real_pi+230])
#define _hx_hh_grid_8_4_243 ((*(jmi->z))[jmi->offs_real_pi+231])
#define _hx_hh_grid_8_5_244 ((*(jmi->z))[jmi->offs_real_pi+232])
#define _hx_hh_grid_8_6_245 ((*(jmi->z))[jmi->offs_real_pi+233])
#define _hx_hh_grid_8_7_246 ((*(jmi->z))[jmi->offs_real_pi+234])
#define _hx_hh_grid_8_8_247 ((*(jmi->z))[jmi->offs_real_pi+235])
#define _hx_hh_grid_8_9_248 ((*(jmi->z))[jmi->offs_real_pi+236])
#define _hx_hh_grid_8_10_249 ((*(jmi->z))[jmi->offs_real_pi+237])
#define _hx_hh_grid_9_1_250 ((*(jmi->z))[jmi->offs_real_pi+238])
#define _hx_hh_grid_9_2_251 ((*(jmi->z))[jmi->offs_real_pi+239])
#define _hx_hh_grid_9_3_252 ((*(jmi->z))[jmi->offs_real_pi+240])
#define _hx_hh_grid_9_4_253 ((*(jmi->z))[jmi->offs_real_pi+241])
#define _hx_hh_grid_9_5_254 ((*(jmi->z))[jmi->offs_real_pi+242])
#define _hx_hh_grid_9_6_255 ((*(jmi->z))[jmi->offs_real_pi+243])
#define _hx_hh_grid_9_7_256 ((*(jmi->z))[jmi->offs_real_pi+244])
#define _hx_hh_grid_9_8_257 ((*(jmi->z))[jmi->offs_real_pi+245])
#define _hx_hh_grid_9_9_258 ((*(jmi->z))[jmi->offs_real_pi+246])
#define _hx_hh_grid_9_10_259 ((*(jmi->z))[jmi->offs_real_pi+247])
#define _hx_hh_grid_10_1_260 ((*(jmi->z))[jmi->offs_real_pi+248])
#define _hx_hh_grid_10_2_261 ((*(jmi->z))[jmi->offs_real_pi+249])
#define _hx_hh_grid_10_3_262 ((*(jmi->z))[jmi->offs_real_pi+250])
#define _hx_hh_grid_10_4_263 ((*(jmi->z))[jmi->offs_real_pi+251])
#define _hx_hh_grid_10_5_264 ((*(jmi->z))[jmi->offs_real_pi+252])
#define _hx_hh_grid_10_6_265 ((*(jmi->z))[jmi->offs_real_pi+253])
#define _hx_hh_grid_10_7_266 ((*(jmi->z))[jmi->offs_real_pi+254])
#define _hx_hh_grid_10_8_267 ((*(jmi->z))[jmi->offs_real_pi+255])
#define _hx_hh_grid_10_9_268 ((*(jmi->z))[jmi->offs_real_pi+256])
#define _hx_hh_grid_10_10_269 ((*(jmi->z))[jmi->offs_real_pi+257])
#define _hx_hh_grid_11_1_270 ((*(jmi->z))[jmi->offs_real_pi+258])
#define _hx_hh_grid_11_2_271 ((*(jmi->z))[jmi->offs_real_pi+259])
#define _hx_hh_grid_11_3_272 ((*(jmi->z))[jmi->offs_real_pi+260])
#define _hx_hh_grid_11_4_273 ((*(jmi->z))[jmi->offs_real_pi+261])
#define _hx_hh_grid_11_5_274 ((*(jmi->z))[jmi->offs_real_pi+262])
#define _hx_hh_grid_11_6_275 ((*(jmi->z))[jmi->offs_real_pi+263])
#define _hx_hh_grid_11_7_276 ((*(jmi->z))[jmi->offs_real_pi+264])
#define _hx_hh_grid_11_8_277 ((*(jmi->z))[jmi->offs_real_pi+265])
#define _hx_hh_grid_11_9_278 ((*(jmi->z))[jmi->offs_real_pi+266])
#define _hx_hh_grid_11_10_279 ((*(jmi->z))[jmi->offs_real_pi+267])
#define _hx_Ahx_280 ((*(jmi->z))[jmi->offs_real_pi+268])
#define _hx_Cmetal_281 ((*(jmi->z))[jmi->offs_real_pi+269])
#define _cabin_v_289 ((*(jmi->z))[jmi->offs_real_pi+270])
#define _cabin_p_290 ((*(jmi->z))[jmi->offs_real_pi+271])
#define _cabin_Qpass_291 ((*(jmi->z))[jmi->offs_real_pi+272])
#define _cabin_dQ_293 ((*(jmi->z))[jmi->offs_real_pi+273])
#define _fan_wf_300 ((*(jmi->z))[jmi->offs_real_pi+274])
#define __block_jacobian_check_tol_342 ((*(jmi->z))[jmi->offs_real_pi+275])
#define __cs_rel_tol_344 ((*(jmi->z))[jmi->offs_real_pi+276])
#define __cs_step_size_346 ((*(jmi->z))[jmi->offs_real_pi+277])
#define __events_default_tol_348 ((*(jmi->z))[jmi->offs_real_pi+278])
#define __events_tol_factor_349 ((*(jmi->z))[jmi->offs_real_pi+279])
#define __nle_solver_default_tol_353 ((*(jmi->z))[jmi->offs_real_pi+280])
#define __nle_solver_max_residual_scaling_factor_355 ((*(jmi->z))[jmi->offs_real_pi+281])
#define __nle_solver_min_residual_scaling_factor_356 ((*(jmi->z))[jmi->offs_real_pi+282])
#define __nle_solver_min_tol_357 ((*(jmi->z))[jmi->offs_real_pi+283])
#define __nle_solver_regularization_tolerance_358 ((*(jmi->z))[jmi->offs_real_pi+284])
#define __nle_solver_step_limit_factor_359 ((*(jmi->z))[jmi->offs_real_pi+285])
#define __nle_solver_tol_factor_360 ((*(jmi->z))[jmi->offs_real_pi+286])
#define _T1_2 ((*(jmi->z))[jmi->offs_real_pd+0])
#define _P1_3 ((*(jmi->z))[jmi->offs_real_pd+1])
#define _T4_11 ((*(jmi->z))[jmi->offs_real_pd+2])
#define _P5_15 ((*(jmi->z))[jmi->offs_real_pd+3])
#define _W7_16 ((*(jmi->z))[jmi->offs_real_pd+4])
#define _valve1_pi_282 ((*(jmi->z))[jmi->offs_real_pd+5])
#define _valve1_t_294 ((*(jmi->z))[jmi->offs_real_pd+6])
#define _ambientair_port_p_299 ((*(jmi->z))[jmi->offs_real_pd+7])
#define _ambientair_port_t_301 ((*(jmi->z))[jmi->offs_real_pd+8])
#define _valve2_po_302 ((*(jmi->z))[jmi->offs_real_pd+9])
#define _cabin_outport_p_307 ((*(jmi->z))[jmi->offs_real_pd+10])
#define _fan_outport_w_309 ((*(jmi->z))[jmi->offs_real_pd+11])
#define _valve1_inport_p_311 ((*(jmi->z))[jmi->offs_real_pd+12])
#define _inlet_port_p_314 ((*(jmi->z))[jmi->offs_real_pd+13])
#define _valve1_inport_t_315 ((*(jmi->z))[jmi->offs_real_pd+14])
#define _valve1_outport_t_316 ((*(jmi->z))[jmi->offs_real_pd+15])
#define _inlet_port_t_317 ((*(jmi->z))[jmi->offs_real_pd+16])
#define _fork_inport_t_318 ((*(jmi->z))[jmi->offs_real_pd+17])
#define _hx_cin_p_319 ((*(jmi->z))[jmi->offs_real_pd+18])
#define _hx_cin_t_320 ((*(jmi->z))[jmi->offs_real_pd+19])
#define _hx_hc_323 ((*(jmi->z))[jmi->offs_real_pd+20])
#define _mixer_inport_2_p_324 ((*(jmi->z))[jmi->offs_real_pd+21])
#define _mixer_inport_1_p_326 ((*(jmi->z))[jmi->offs_real_pd+22])
#define _mixer_inport_3_p_327 ((*(jmi->z))[jmi->offs_real_pd+23])
#define _mixer_outport_p_328 ((*(jmi->z))[jmi->offs_real_pd+24])
#define _hx_hout_p_329 ((*(jmi->z))[jmi->offs_real_pd+25])
#define _valve2_outport_p_330 ((*(jmi->z))[jmi->offs_real_pd+26])
#define _cabin_inport_p_331 ((*(jmi->z))[jmi->offs_real_pd+27])
#define _fan_outport_p_332 ((*(jmi->z))[jmi->offs_real_pd+28])
#define _fan_inport_p_333 ((*(jmi->z))[jmi->offs_real_pd+29])
#define _fan_inport_w_334 ((*(jmi->z))[jmi->offs_real_pd+30])
#define _mixer_inport_2_w_335 ((*(jmi->z))[jmi->offs_real_pd+31])
#define _cabin_outport_w_336 ((*(jmi->z))[jmi->offs_real_pd+32])
#define _cabin_passenger_292 ((*(jmi->z))[jmi->offs_integer_pi+0])
#define __block_solver_experimental_mode_343 ((*(jmi->z))[jmi->offs_integer_pi+1])
#define __cs_solver_345 ((*(jmi->z))[jmi->offs_integer_pi+2])
#define __iteration_variable_scaling_350 ((*(jmi->z))[jmi->offs_integer_pi+3])
#define __log_level_351 ((*(jmi->z))[jmi->offs_integer_pi+4])
#define __nle_solver_max_iter_354 ((*(jmi->z))[jmi->offs_integer_pi+5])
#define __residual_equation_scaling_363 ((*(jmi->z))[jmi->offs_integer_pi+6])
#define __block_jacobian_check_341 ((*(jmi->z))[jmi->offs_boolean_pi+0])
#define __enforce_bounds_347 ((*(jmi->z))[jmi->offs_boolean_pi+1])
#define __nle_solver_check_jac_cond_352 ((*(jmi->z))[jmi->offs_boolean_pi+2])
#define __rescale_after_singular_jac_361 ((*(jmi->z))[jmi->offs_boolean_pi+3])
#define __rescale_each_step_362 ((*(jmi->z))[jmi->offs_boolean_pi+4])
#define __runtime_log_to_file_364 ((*(jmi->z))[jmi->offs_boolean_pi+5])
#define __use_Brent_in_1d_365 ((*(jmi->z))[jmi->offs_boolean_pi+6])
#define __use_jacobian_equilibration_366 ((*(jmi->z))[jmi->offs_boolean_pi+7])
#define _der_fork_m_337 ((*(jmi->z))[jmi->offs_real_dx+0])
#define _der_fork_Q_338 ((*(jmi->z))[jmi->offs_real_dx+1])
#define _der_hx_Q_339 ((*(jmi->z))[jmi->offs_real_dx+2])
#define _der_cabin_m_340 ((*(jmi->z))[jmi->offs_real_dx+3])
#define _fork_m_9 ((*(jmi->z))[jmi->offs_real_x+0])
#define _fork_Q_10 ((*(jmi->z))[jmi->offs_real_x+1])
#define _hx_Q_285 ((*(jmi->z))[jmi->offs_real_x+2])
#define _cabin_m_296 ((*(jmi->z))[jmi->offs_real_x+3])
#define _fork_outport_w_6 ((*(jmi->z))[jmi->offs_real_w+0])
#define _fork_p_7 ((*(jmi->z))[jmi->offs_real_w+1])
#define _fork_t_8 ((*(jmi->z))[jmi->offs_real_w+2])
#define _hx_hh_283 ((*(jmi->z))[jmi->offs_real_w+3])
#define _hx_t_284 ((*(jmi->z))[jmi->offs_real_w+4])
#define _hx_rhoh_286 ((*(jmi->z))[jmi->offs_real_w+5])
#define _hx_qh_287 ((*(jmi->z))[jmi->offs_real_w+6])
#define _hx_qc_288 ((*(jmi->z))[jmi->offs_real_w+7])
#define _cabin_t_295 ((*(jmi->z))[jmi->offs_real_w+8])
#define _cabin_Q_297 ((*(jmi->z))[jmi->offs_real_w+9])
#define _cabin_wa_298 ((*(jmi->z))[jmi->offs_real_w+10])
#define _W1_303 ((*(jmi->z))[jmi->offs_real_w+11])
#define _W31_304 ((*(jmi->z))[jmi->offs_real_w+12])
#define _W32_305 ((*(jmi->z))[jmi->offs_real_w+13])
#define _W33_306 ((*(jmi->z))[jmi->offs_real_w+14])
#define _T6_310 ((*(jmi->z))[jmi->offs_real_w+15])
#define _T8_312 ((*(jmi->z))[jmi->offs_real_w+16])
#define _W8_313 ((*(jmi->z))[jmi->offs_real_w+17])
#define _time ((*(jmi->z))[jmi->offs_t])
#define pre_der_fork_m_337 ((*(jmi->z))[jmi->offs_pre_real_dx+0])
#define pre_der_fork_Q_338 ((*(jmi->z))[jmi->offs_pre_real_dx+1])
#define pre_der_hx_Q_339 ((*(jmi->z))[jmi->offs_pre_real_dx+2])
#define pre_der_cabin_m_340 ((*(jmi->z))[jmi->offs_pre_real_dx+3])
#define pre_fork_m_9 ((*(jmi->z))[jmi->offs_pre_real_x+0])
#define pre_fork_Q_10 ((*(jmi->z))[jmi->offs_pre_real_x+1])
#define pre_hx_Q_285 ((*(jmi->z))[jmi->offs_pre_real_x+2])
#define pre_cabin_m_296 ((*(jmi->z))[jmi->offs_pre_real_x+3])
#define pre_fork_outport_w_6 ((*(jmi->z))[jmi->offs_pre_real_w+0])
#define pre_fork_p_7 ((*(jmi->z))[jmi->offs_pre_real_w+1])
#define pre_fork_t_8 ((*(jmi->z))[jmi->offs_pre_real_w+2])
#define pre_hx_hh_283 ((*(jmi->z))[jmi->offs_pre_real_w+3])
#define pre_hx_t_284 ((*(jmi->z))[jmi->offs_pre_real_w+4])
#define pre_hx_rhoh_286 ((*(jmi->z))[jmi->offs_pre_real_w+5])
#define pre_hx_qh_287 ((*(jmi->z))[jmi->offs_pre_real_w+6])
#define pre_hx_qc_288 ((*(jmi->z))[jmi->offs_pre_real_w+7])
#define pre_cabin_t_295 ((*(jmi->z))[jmi->offs_pre_real_w+8])
#define pre_cabin_Q_297 ((*(jmi->z))[jmi->offs_pre_real_w+9])
#define pre_cabin_wa_298 ((*(jmi->z))[jmi->offs_pre_real_w+10])
#define pre_W1_303 ((*(jmi->z))[jmi->offs_pre_real_w+11])
#define pre_W31_304 ((*(jmi->z))[jmi->offs_pre_real_w+12])
#define pre_W32_305 ((*(jmi->z))[jmi->offs_pre_real_w+13])
#define pre_W33_306 ((*(jmi->z))[jmi->offs_pre_real_w+14])
#define pre_T6_310 ((*(jmi->z))[jmi->offs_pre_real_w+15])
#define pre_T8_312 ((*(jmi->z))[jmi->offs_pre_real_w+16])
#define pre_W8_313 ((*(jmi->z))[jmi->offs_pre_real_w+17])


const char *fmi_runtime_options_map_names[] = {
    "_block_jacobian_check",
    "_block_jacobian_check_tol",
    "_block_solver_experimental_mode",
    "_cs_rel_tol",
    "_cs_solver",
    "_cs_step_size",
    "_enforce_bounds",
    "_events_default_tol",
    "_events_tol_factor",
    "_iteration_variable_scaling",
    "_log_level",
    "_nle_solver_check_jac_cond",
    "_nle_solver_default_tol",
    "_nle_solver_max_iter",
    "_nle_solver_max_residual_scaling_factor",
    "_nle_solver_min_residual_scaling_factor",
    "_nle_solver_min_tol",
    "_nle_solver_regularization_tolerance",
    "_nle_solver_step_limit_factor",
    "_nle_solver_tol_factor",
    "_rescale_after_singular_jac",
    "_rescale_each_step",
    "_residual_equation_scaling",
    "_runtime_log_to_file",
    "_use_Brent_in_1d",
    "_use_jacobian_equilibration",
    NULL
};

const int fmi_runtime_options_map_vrefs[] = {
    536871245, 281, 268435783, 282, 268435784, 283, 536871246, 284, 285, 268435785,
    268435786, 536871247, 286, 268435787, 287, 288, 289, 290, 291, 292,
    536871248, 536871249, 268435788, 536871250, 536871251, 536871252, 0
};

const int fmi_runtime_options_map_length = 26;

#define _real_ci(i) ((*(jmi->z))[jmi->offs_real_ci+i])
#define _real_cd(i) ((*(jmi->z))[jmi->offs_real_cd+i])
#define _real_pi(i) ((*(jmi->z))[jmi->offs_real_pi+i])
#define _real_pd(i) ((*(jmi->z))[jmi->offs_real_pd+i])
#define _real_dx(i) ((*(jmi->z))[jmi->offs_real_dx+i])
#define _real_x(i) ((*(jmi->z))[jmi->offs_real_x+i])
#define _real_u(i) ((*(jmi->z))[jmi->offs_real_u+i])
#define _real_w(i) ((*(jmi->z))[jmi->offs_real_w+i])
#define _t ((*(jmi->z))[jmi->offs_t])

#define _real_d(i) ((*(jmi->z))[jmi->offs_real_d+i])
#define _integer_d(i) ((*(jmi->z))[jmi->offs_integer_d+i])
#define _integer_u(i) ((*(jmi->z))[jmi->offs_integer_u+i])
#define _boolean_d(i) ((*(jmi->z))[jmi->offs_boolean_d+i])
#define _boolean_u(i) ((*(jmi->z))[jmi->offs_boolean_u+i])

#define _pre_real_dx(i) ((*(jmi->z))[jmi->offs_pre_real_dx+i])
#define _pre_real_x(i) ((*(jmi->z))[jmi->offs_pre_real_x+i])
#define _pre_real_u(i) ((*(jmi->z))[jmi->offs_pre_real_u+i])
#define _pre_real_w(i) ((*(jmi->z))[jmi->offs_pre_real_w+i])

#define _pre_real_d(i) ((*(jmi->z))[jmi->offs_pre_real_d+i])
#define _pre_integer_d(i) ((*(jmi->z))[jmi->offs_pre_integer_d+i])
#define _pre_integer_u(i) ((*(jmi->z))[jmi->offs_pre_integer_u+i])
#define _pre_boolean_d(i) ((*(jmi->z))[jmi->offs_pre_boolean_d+i])
#define _pre_boolean_u(i) ((*(jmi->z))[jmi->offs_pre_boolean_u+i])

#define _sw(i) ((*(jmi->z))[jmi->offs_sw + i])
#define _sw_init(i) ((*(jmi->z))[jmi->offs_sw_init + i])
#define _pre_sw(i) ((*(jmi->z))[jmi->offs_pre_sw + i])
#define _pre_sw_init(i) ((*(jmi->z))[jmi->offs_pre_sw_init + i])
#define _guards(i) ((*(jmi->z))[jmi->offs_guards + i])
#define _guards_init(i) ((*(jmi->z))[jmi->offs_guards_init + i])
#define _pre_guards(i) ((*(jmi->z))[jmi->offs_pre_guards + i])
#define _pre_guards_init(i) ((*(jmi->z))[jmi->offs_pre_guards_init + i])

#define _atInitial (jmi->atInitial)





void func_AMS_LinearMap_def0(jmi_array_t* w_grid_a, jmi_array_t* t_grid_a, jmi_array_t* h_grid_a, jmi_ad_var_t w_v, jmi_ad_var_t t_v, jmi_ad_var_t* h_o);
jmi_ad_var_t func_AMS_LinearMap_exp0(jmi_array_t* w_grid_a, jmi_array_t* t_grid_a, jmi_array_t* h_grid_a, jmi_ad_var_t w_v, jmi_ad_var_t t_v);




static int dae_block_0(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    /***** Block: 1 *****/
    jmi_real_t** res = &residual;
    int ef = 0;
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
        x[0] = 300;
    } else if (evaluation_mode == JMI_BLOCK_START) {
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
        x[0] = 0.0;
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 364;
    } else if (evaluation_mode == JMI_BLOCK_NON_REAL_VALUE_REFERENCE) {
    } else if (evaluation_mode == JMI_BLOCK_ACTIVE_SWITCH_INDEX) {
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _T6_310;
        init_with_lbound(x[0], 0.0, "Resetting initial value for variable T6");
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        memset(residual, 0, 1 * sizeof(jmi_real_t));
        residual[0] = 716.75 * (- _W33_306);
    } else if (evaluation_mode & JMI_BLOCK_EVALUATE || evaluation_mode & JMI_BLOCK_WRITE_BACK) {
        if ((evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) == 0) {
            check_lbound(x[0], 0.0, "Out of bounds for variable T6");
            _T6_310 = x[0];
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE) {
            (*res)[0] = 0 - (716.75 * ((- _W33_306) * _T6_310 + _W33_306 * _fork_t_8) - _hx_qh_287);
        }
    }
    return ef;
}

static int dae_block_1(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    /***** Block: 2 *****/
    jmi_real_t** res = &residual;
    int ef = 0;
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_START) {
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 359;
    } else if (evaluation_mode == JMI_BLOCK_NON_REAL_VALUE_REFERENCE) {
    } else if (evaluation_mode == JMI_BLOCK_ACTIVE_SWITCH_INDEX) {
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _cabin_wa_298;
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        memset(residual, 0, 1 * sizeof(jmi_real_t));
        residual[0] = - _cabin_t_295 * 716.75;
    } else if (evaluation_mode & JMI_BLOCK_EVALUATE || evaluation_mode & JMI_BLOCK_WRITE_BACK) {
        if ((evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) == 0) {
            _cabin_wa_298 = x[0];
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE) {
            (*res)[0] = (_W8_313 * _T8_312 + _fan_outport_w_309 * _cabin_t_295 + _cabin_wa_298 * _cabin_t_295) * 716.75 + _cabin_Qpass_291 * _cabin_passenger_292 + _cabin_dQ_293 - (0);
        }
    }
    return ef;
}



static int dae_init_block_0(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    /***** Init block: 1 *****/
    jmi_real_t** res = &residual;
    int ef = 0;
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
        x[0] = 300;
    } else if (evaluation_mode == JMI_BLOCK_START) {
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
        x[0] = 0.0;
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 364;
    } else if (evaluation_mode == JMI_BLOCK_NON_REAL_VALUE_REFERENCE) {
    } else if (evaluation_mode == JMI_BLOCK_ACTIVE_SWITCH_INDEX) {
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _T6_310;
        init_with_lbound(x[0], 0.0, "Resetting initial value for variable T6");
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        memset(residual, 0, 1 * sizeof(jmi_real_t));
        residual[0] = 716.75 * (- _W33_306);
    } else if (evaluation_mode & JMI_BLOCK_EVALUATE || evaluation_mode & JMI_BLOCK_WRITE_BACK) {
        if ((evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) == 0) {
            check_lbound(x[0], 0.0, "Out of bounds for variable T6");
            _T6_310 = x[0];
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE) {
            (*res)[0] = 0 - (716.75 * ((- _W33_306) * _T6_310 + _W33_306 * _fork_t_8) - _hx_qh_287);
        }
    }
    return ef;
}

static int dae_init_block_1(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    /***** Init block: 2 *****/
    jmi_real_t** res = &residual;
    int ef = 0;
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_START) {
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 359;
    } else if (evaluation_mode == JMI_BLOCK_NON_REAL_VALUE_REFERENCE) {
    } else if (evaluation_mode == JMI_BLOCK_ACTIVE_SWITCH_INDEX) {
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _cabin_wa_298;
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        memset(residual, 0, 1 * sizeof(jmi_real_t));
        residual[0] = - _cabin_t_295 * 716.75;
    } else if (evaluation_mode & JMI_BLOCK_EVALUATE || evaluation_mode & JMI_BLOCK_WRITE_BACK) {
        if ((evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) == 0) {
            _cabin_wa_298 = x[0];
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE) {
            (*res)[0] = (_W8_313 * _T8_312 + _fan_outport_w_309 * _cabin_t_295 + _cabin_wa_298 * _cabin_t_295) * 716.75 + _cabin_Qpass_291 * _cabin_passenger_292 + _cabin_dQ_293 - (0);
        }
    }
    return ef;
}







void func_AMS_LinearMap_def0(jmi_array_t* w_grid_a, jmi_array_t* t_grid_a, jmi_array_t* h_grid_a, jmi_ad_var_t w_v, jmi_ad_var_t t_v, jmi_ad_var_t* h_o) {
    JMI_DYNAMIC_INIT()
    jmi_ad_var_t h_v;
    jmi_ad_var_t n_v;
    jmi_ad_var_t m_v;
    jmi_ad_var_t i_0i;
    jmi_ad_var_t i_0ie;
    jmi_ad_var_t j_1i;
    jmi_ad_var_t j_1ie;
    n_v = jmi_array_size(w_grid_a, 0);
    m_v = jmi_array_size(t_grid_a, 0);
    if (COND_EXP_EQ(n_v, jmi_array_size(h_grid_a, 0), JMI_TRUE, JMI_FALSE) == JMI_FALSE) {
        jmi_assert_failed("mass flow size mismatch", JMI_ASSERT_ERROR);
    }
    if (COND_EXP_EQ(m_v, jmi_array_size(h_grid_a, 1), JMI_TRUE, JMI_FALSE) == JMI_FALSE) {
        jmi_assert_failed("temperature size mismatch", JMI_ASSERT_ERROR);
    }
    if (LOG_EXP_AND(COND_EXP_GE(w_v, jmi_array_val_1(w_grid_a, AD_WRAP_LITERAL(1)), JMI_TRUE, JMI_FALSE), COND_EXP_LE(w_v, jmi_array_val_1(w_grid_a, n_v), JMI_TRUE, JMI_FALSE)) == JMI_FALSE) {
        jmi_assert_failed("mass flow out of range", JMI_ASSERT_ERROR);
    }
    if (LOG_EXP_AND(COND_EXP_GE(t_v, jmi_array_val_1(t_grid_a, AD_WRAP_LITERAL(1)), JMI_TRUE, JMI_FALSE), COND_EXP_LE(t_v, jmi_array_val_1(t_grid_a, m_v), JMI_TRUE, JMI_FALSE)) == JMI_FALSE) {
        jmi_assert_failed("temperature out of range", JMI_ASSERT_ERROR);
    }
    i_0ie = n_v - 1 + 1 / 2.0;
    for (i_0i = 1; i_0i < i_0ie; i_0i += 1) {
        if (LOG_EXP_AND(COND_EXP_GE(w_v, jmi_array_val_1(w_grid_a, i_0i), JMI_TRUE, JMI_FALSE), COND_EXP_LE(w_v, jmi_array_val_1(w_grid_a, i_0i + 1), JMI_TRUE, JMI_FALSE))) {
            j_1ie = m_v - 1 + 1 / 2.0;
            for (j_1i = 1; j_1i < j_1ie; j_1i += 1) {
                if (LOG_EXP_AND(COND_EXP_GE(t_v, jmi_array_val_1(t_grid_a, j_1i), JMI_TRUE, JMI_FALSE), COND_EXP_LE(t_v, jmi_array_val_1(t_grid_a, j_1i + 1), JMI_TRUE, JMI_FALSE))) {
                    h_v = jmi_divide_function("AMS.LinearMap", jmi_divide_function("AMS.LinearMap", ((w_v - jmi_array_val_1(w_grid_a, i_0i)) * (t_v - jmi_array_val_1(t_grid_a, j_1i)) * jmi_array_val_2(h_grid_a, i_0i + 1, j_1i + 1) + (w_v - jmi_array_val_1(w_grid_a, i_0i)) * (jmi_array_val_1(t_grid_a, j_1i + 1) - t_v) * jmi_array_val_2(h_grid_a, i_0i + 1, j_1i) + (jmi_array_val_1(w_grid_a, i_0i + 1) - w_v) * (t_v - jmi_array_val_1(t_grid_a, j_1i)) * jmi_array_val_2(h_grid_a, i_0i, j_1i + 1) + (jmi_array_val_1(w_grid_a, i_0i + 1) - w_v) * (jmi_array_val_1(t_grid_a, j_1i + 1) - t_v) * jmi_array_val_2(h_grid_a, i_0i, j_1i)),(jmi_array_val_1(t_grid_a, j_1i + 1) - jmi_array_val_1(t_grid_a, j_1i)),"((w - w_grid[i]) * (t - t_grid[j]) * h_grid[i + 1,j + 1] + (w - w_grid[i]) * (t_grid[j + 1] - t) * h_grid[i + 1,j] + (w_grid[i + 1] - w) * (t - t_grid[j]) * h_grid[i,j + 1] + (w_grid[i + 1] - w) * (t_grid[j + 1] - t) * h_grid[i,j]) / (t_grid[j + 1] - t_grid[j])"),(jmi_array_val_1(w_grid_a, i_0i + 1) - jmi_array_val_1(w_grid_a, i_0i)),"((w - w_grid[i]) * (t - t_grid[j]) * h_grid[i + 1,j + 1] + (w - w_grid[i]) * (t_grid[j + 1] - t) * h_grid[i + 1,j] + (w_grid[i + 1] - w) * (t - t_grid[j]) * h_grid[i,j + 1] + (w_grid[i + 1] - w) * (t_grid[j + 1] - t) * h_grid[i,j]) / (t_grid[j + 1] - t_grid[j]) / (w_grid[i + 1] - w_grid[i])");
                }
            }
        }
    }
    JMI_RET(GEN, h_o, h_v)
    JMI_DYNAMIC_FREE()
    return;
}

jmi_ad_var_t func_AMS_LinearMap_exp0(jmi_array_t* w_grid_a, jmi_array_t* t_grid_a, jmi_array_t* h_grid_a, jmi_ad_var_t w_v, jmi_ad_var_t t_v) {
    jmi_ad_var_t h_v;
    func_AMS_LinearMap_def0(w_grid_a, t_grid_a, h_grid_a, w_v, t_v, &h_v);
    return h_v;
}








static int model_ode_guards(jmi_t* jmi) {

    return 0;
}

static int model_ode_next_time_event(jmi_t* jmi, jmi_time_event_t* event) {
    jmi_time_event_t nextEvent = {0};
    jmi_real_t nSamp;
    *event = nextEvent;


    return 0;
}

static int model_ode_derivatives(jmi_t* jmi) {
    int ef = 0;
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_1, 11, 1)
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_2, 10, 1)
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_3, 110, 2)
    model_ode_guards(jmi);
/************* ODE section *********/
    _fork_t_8 = jmi_divide_equation(jmi, (- _fork_Q_10),(- _fork_m_9 * 716.75),"(- fork.Q) / (- fork.m * 716.75)");
    _fork_p_7 = jmi_divide_equation(jmi, _fork_m_9 * _fork_t_8 * 8.314472,(_fork_v_5 * 0.02897),"fork.m * fork.t * 8.314472 / (fork.v * 0.02897)");
    _W1_303 = COND_EXP_EQ((COND_EXP_GT(_fork_p_7, AD_WRAP_LITERAL(0.5) * _valve1_pi_282, JMI_TRUE, JMI_FALSE)), JMI_TRUE, AD_WRAP_LITERAL(7.316E-5) * (_valve1_pi_282 + AD_WRAP_LITERAL(2) * _fork_p_7) * sqrt(jmi_divide_equation(jmi, (AD_WRAP_LITERAL(1) - jmi_divide_equation(jmi, _fork_p_7,_valve1_pi_282,"fork.p / valve1.pi")),_valve1_t_294,"(1 - fork.p / valve1.pi) / valve1.t")), AD_WRAP_LITERAL(1.0338499999999999E-4) * _valve1_pi_282 * sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(1),_valve1_t_294,"1 / valve1.t")));
    _hx_rhoh_286 = jmi_divide_equation(jmi, 0.02897 * _fork_p_7,(8.314472 * _fork_t_8),"0.02897 * fork.p / (8.314472 * fork.t)");
    _W33_306 = sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(2) * (_fork_p_7 - _valve2_po_302) * _hx_rhoh_286 * (1.0 * (_hx_Ahx_280) * (_hx_Ahx_280)),0.009895,"2 * (fork.p - valve2.po) * hx.rhoh * hx.Ahx ^ 2 / 0.009895"));
    _W32_305 = COND_EXP_EQ((COND_EXP_GT(_valve2_po_302, AD_WRAP_LITERAL(0.5) * _fork_p_7, JMI_TRUE, JMI_FALSE)), JMI_TRUE, AD_WRAP_LITERAL(4.72E-5) * (_fork_p_7 + AD_WRAP_LITERAL(2) * _valve2_po_302) * sqrt(jmi_divide_equation(jmi, (AD_WRAP_LITERAL(1) - jmi_divide_equation(jmi, _valve2_po_302,_fork_p_7,"valve2.po / fork.p")),_fork_t_8,"(1 - valve2.po / fork.p) / fork.t")), AD_WRAP_LITERAL(6.67E-5) * _fork_p_7 * sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(1),_fork_t_8,"1 / fork.t")));
    _fork_outport_w_6 = - _W33_306 + (- _W32_305);
    _der_fork_m_337 = _W1_303 + _fork_outport_w_6;
    _der_fork_Q_338 = _W1_303 * _valve1_t_294 * 716.75 + _fork_outport_w_6 * _fork_t_8 * 716.75;
    JMI_ARRAY_INIT_1(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_1, 11, 1, 11)
    jmi_array_ref_1(tmp_1, 1) = _hx_wh_grid_1_29;
    jmi_array_ref_1(tmp_1, 2) = _hx_wh_grid_2_30;
    jmi_array_ref_1(tmp_1, 3) = _hx_wh_grid_3_31;
    jmi_array_ref_1(tmp_1, 4) = _hx_wh_grid_4_32;
    jmi_array_ref_1(tmp_1, 5) = _hx_wh_grid_5_33;
    jmi_array_ref_1(tmp_1, 6) = _hx_wh_grid_6_34;
    jmi_array_ref_1(tmp_1, 7) = _hx_wh_grid_7_35;
    jmi_array_ref_1(tmp_1, 8) = _hx_wh_grid_8_36;
    jmi_array_ref_1(tmp_1, 9) = _hx_wh_grid_9_37;
    jmi_array_ref_1(tmp_1, 10) = _hx_wh_grid_10_38;
    jmi_array_ref_1(tmp_1, 11) = _hx_wh_grid_11_39;
    JMI_ARRAY_INIT_1(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_2, 10, 1, 10)
    jmi_array_ref_1(tmp_2, 1) = _hx_th_grid_1_40;
    jmi_array_ref_1(tmp_2, 2) = _hx_th_grid_2_41;
    jmi_array_ref_1(tmp_2, 3) = _hx_th_grid_3_42;
    jmi_array_ref_1(tmp_2, 4) = _hx_th_grid_4_43;
    jmi_array_ref_1(tmp_2, 5) = _hx_th_grid_5_44;
    jmi_array_ref_1(tmp_2, 6) = _hx_th_grid_6_45;
    jmi_array_ref_1(tmp_2, 7) = _hx_th_grid_7_46;
    jmi_array_ref_1(tmp_2, 8) = _hx_th_grid_8_47;
    jmi_array_ref_1(tmp_2, 9) = _hx_th_grid_9_48;
    jmi_array_ref_1(tmp_2, 10) = _hx_th_grid_10_49;
    JMI_ARRAY_INIT_2(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_3, 110, 2, 11, 10)
    jmi_array_ref_2(tmp_3, 1,1) = _hx_hh_grid_1_1_170;
    jmi_array_ref_2(tmp_3, 1,2) = _hx_hh_grid_1_2_171;
    jmi_array_ref_2(tmp_3, 1,3) = _hx_hh_grid_1_3_172;
    jmi_array_ref_2(tmp_3, 1,4) = _hx_hh_grid_1_4_173;
    jmi_array_ref_2(tmp_3, 1,5) = _hx_hh_grid_1_5_174;
    jmi_array_ref_2(tmp_3, 1,6) = _hx_hh_grid_1_6_175;
    jmi_array_ref_2(tmp_3, 1,7) = _hx_hh_grid_1_7_176;
    jmi_array_ref_2(tmp_3, 1,8) = _hx_hh_grid_1_8_177;
    jmi_array_ref_2(tmp_3, 1,9) = _hx_hh_grid_1_9_178;
    jmi_array_ref_2(tmp_3, 1,10) = _hx_hh_grid_1_10_179;
    jmi_array_ref_2(tmp_3, 2,1) = _hx_hh_grid_2_1_180;
    jmi_array_ref_2(tmp_3, 2,2) = _hx_hh_grid_2_2_181;
    jmi_array_ref_2(tmp_3, 2,3) = _hx_hh_grid_2_3_182;
    jmi_array_ref_2(tmp_3, 2,4) = _hx_hh_grid_2_4_183;
    jmi_array_ref_2(tmp_3, 2,5) = _hx_hh_grid_2_5_184;
    jmi_array_ref_2(tmp_3, 2,6) = _hx_hh_grid_2_6_185;
    jmi_array_ref_2(tmp_3, 2,7) = _hx_hh_grid_2_7_186;
    jmi_array_ref_2(tmp_3, 2,8) = _hx_hh_grid_2_8_187;
    jmi_array_ref_2(tmp_3, 2,9) = _hx_hh_grid_2_9_188;
    jmi_array_ref_2(tmp_3, 2,10) = _hx_hh_grid_2_10_189;
    jmi_array_ref_2(tmp_3, 3,1) = _hx_hh_grid_3_1_190;
    jmi_array_ref_2(tmp_3, 3,2) = _hx_hh_grid_3_2_191;
    jmi_array_ref_2(tmp_3, 3,3) = _hx_hh_grid_3_3_192;
    jmi_array_ref_2(tmp_3, 3,4) = _hx_hh_grid_3_4_193;
    jmi_array_ref_2(tmp_3, 3,5) = _hx_hh_grid_3_5_194;
    jmi_array_ref_2(tmp_3, 3,6) = _hx_hh_grid_3_6_195;
    jmi_array_ref_2(tmp_3, 3,7) = _hx_hh_grid_3_7_196;
    jmi_array_ref_2(tmp_3, 3,8) = _hx_hh_grid_3_8_197;
    jmi_array_ref_2(tmp_3, 3,9) = _hx_hh_grid_3_9_198;
    jmi_array_ref_2(tmp_3, 3,10) = _hx_hh_grid_3_10_199;
    jmi_array_ref_2(tmp_3, 4,1) = _hx_hh_grid_4_1_200;
    jmi_array_ref_2(tmp_3, 4,2) = _hx_hh_grid_4_2_201;
    jmi_array_ref_2(tmp_3, 4,3) = _hx_hh_grid_4_3_202;
    jmi_array_ref_2(tmp_3, 4,4) = _hx_hh_grid_4_4_203;
    jmi_array_ref_2(tmp_3, 4,5) = _hx_hh_grid_4_5_204;
    jmi_array_ref_2(tmp_3, 4,6) = _hx_hh_grid_4_6_205;
    jmi_array_ref_2(tmp_3, 4,7) = _hx_hh_grid_4_7_206;
    jmi_array_ref_2(tmp_3, 4,8) = _hx_hh_grid_4_8_207;
    jmi_array_ref_2(tmp_3, 4,9) = _hx_hh_grid_4_9_208;
    jmi_array_ref_2(tmp_3, 4,10) = _hx_hh_grid_4_10_209;
    jmi_array_ref_2(tmp_3, 5,1) = _hx_hh_grid_5_1_210;
    jmi_array_ref_2(tmp_3, 5,2) = _hx_hh_grid_5_2_211;
    jmi_array_ref_2(tmp_3, 5,3) = _hx_hh_grid_5_3_212;
    jmi_array_ref_2(tmp_3, 5,4) = _hx_hh_grid_5_4_213;
    jmi_array_ref_2(tmp_3, 5,5) = _hx_hh_grid_5_5_214;
    jmi_array_ref_2(tmp_3, 5,6) = _hx_hh_grid_5_6_215;
    jmi_array_ref_2(tmp_3, 5,7) = _hx_hh_grid_5_7_216;
    jmi_array_ref_2(tmp_3, 5,8) = _hx_hh_grid_5_8_217;
    jmi_array_ref_2(tmp_3, 5,9) = _hx_hh_grid_5_9_218;
    jmi_array_ref_2(tmp_3, 5,10) = _hx_hh_grid_5_10_219;
    jmi_array_ref_2(tmp_3, 6,1) = _hx_hh_grid_6_1_220;
    jmi_array_ref_2(tmp_3, 6,2) = _hx_hh_grid_6_2_221;
    jmi_array_ref_2(tmp_3, 6,3) = _hx_hh_grid_6_3_222;
    jmi_array_ref_2(tmp_3, 6,4) = _hx_hh_grid_6_4_223;
    jmi_array_ref_2(tmp_3, 6,5) = _hx_hh_grid_6_5_224;
    jmi_array_ref_2(tmp_3, 6,6) = _hx_hh_grid_6_6_225;
    jmi_array_ref_2(tmp_3, 6,7) = _hx_hh_grid_6_7_226;
    jmi_array_ref_2(tmp_3, 6,8) = _hx_hh_grid_6_8_227;
    jmi_array_ref_2(tmp_3, 6,9) = _hx_hh_grid_6_9_228;
    jmi_array_ref_2(tmp_3, 6,10) = _hx_hh_grid_6_10_229;
    jmi_array_ref_2(tmp_3, 7,1) = _hx_hh_grid_7_1_230;
    jmi_array_ref_2(tmp_3, 7,2) = _hx_hh_grid_7_2_231;
    jmi_array_ref_2(tmp_3, 7,3) = _hx_hh_grid_7_3_232;
    jmi_array_ref_2(tmp_3, 7,4) = _hx_hh_grid_7_4_233;
    jmi_array_ref_2(tmp_3, 7,5) = _hx_hh_grid_7_5_234;
    jmi_array_ref_2(tmp_3, 7,6) = _hx_hh_grid_7_6_235;
    jmi_array_ref_2(tmp_3, 7,7) = _hx_hh_grid_7_7_236;
    jmi_array_ref_2(tmp_3, 7,8) = _hx_hh_grid_7_8_237;
    jmi_array_ref_2(tmp_3, 7,9) = _hx_hh_grid_7_9_238;
    jmi_array_ref_2(tmp_3, 7,10) = _hx_hh_grid_7_10_239;
    jmi_array_ref_2(tmp_3, 8,1) = _hx_hh_grid_8_1_240;
    jmi_array_ref_2(tmp_3, 8,2) = _hx_hh_grid_8_2_241;
    jmi_array_ref_2(tmp_3, 8,3) = _hx_hh_grid_8_3_242;
    jmi_array_ref_2(tmp_3, 8,4) = _hx_hh_grid_8_4_243;
    jmi_array_ref_2(tmp_3, 8,5) = _hx_hh_grid_8_5_244;
    jmi_array_ref_2(tmp_3, 8,6) = _hx_hh_grid_8_6_245;
    jmi_array_ref_2(tmp_3, 8,7) = _hx_hh_grid_8_7_246;
    jmi_array_ref_2(tmp_3, 8,8) = _hx_hh_grid_8_8_247;
    jmi_array_ref_2(tmp_3, 8,9) = _hx_hh_grid_8_9_248;
    jmi_array_ref_2(tmp_3, 8,10) = _hx_hh_grid_8_10_249;
    jmi_array_ref_2(tmp_3, 9,1) = _hx_hh_grid_9_1_250;
    jmi_array_ref_2(tmp_3, 9,2) = _hx_hh_grid_9_2_251;
    jmi_array_ref_2(tmp_3, 9,3) = _hx_hh_grid_9_3_252;
    jmi_array_ref_2(tmp_3, 9,4) = _hx_hh_grid_9_4_253;
    jmi_array_ref_2(tmp_3, 9,5) = _hx_hh_grid_9_5_254;
    jmi_array_ref_2(tmp_3, 9,6) = _hx_hh_grid_9_6_255;
    jmi_array_ref_2(tmp_3, 9,7) = _hx_hh_grid_9_7_256;
    jmi_array_ref_2(tmp_3, 9,8) = _hx_hh_grid_9_8_257;
    jmi_array_ref_2(tmp_3, 9,9) = _hx_hh_grid_9_9_258;
    jmi_array_ref_2(tmp_3, 9,10) = _hx_hh_grid_9_10_259;
    jmi_array_ref_2(tmp_3, 10,1) = _hx_hh_grid_10_1_260;
    jmi_array_ref_2(tmp_3, 10,2) = _hx_hh_grid_10_2_261;
    jmi_array_ref_2(tmp_3, 10,3) = _hx_hh_grid_10_3_262;
    jmi_array_ref_2(tmp_3, 10,4) = _hx_hh_grid_10_4_263;
    jmi_array_ref_2(tmp_3, 10,5) = _hx_hh_grid_10_5_264;
    jmi_array_ref_2(tmp_3, 10,6) = _hx_hh_grid_10_6_265;
    jmi_array_ref_2(tmp_3, 10,7) = _hx_hh_grid_10_7_266;
    jmi_array_ref_2(tmp_3, 10,8) = _hx_hh_grid_10_8_267;
    jmi_array_ref_2(tmp_3, 10,9) = _hx_hh_grid_10_9_268;
    jmi_array_ref_2(tmp_3, 10,10) = _hx_hh_grid_10_10_269;
    jmi_array_ref_2(tmp_3, 11,1) = _hx_hh_grid_11_1_270;
    jmi_array_ref_2(tmp_3, 11,2) = _hx_hh_grid_11_2_271;
    jmi_array_ref_2(tmp_3, 11,3) = _hx_hh_grid_11_3_272;
    jmi_array_ref_2(tmp_3, 11,4) = _hx_hh_grid_11_4_273;
    jmi_array_ref_2(tmp_3, 11,5) = _hx_hh_grid_11_5_274;
    jmi_array_ref_2(tmp_3, 11,6) = _hx_hh_grid_11_6_275;
    jmi_array_ref_2(tmp_3, 11,7) = _hx_hh_grid_11_7_276;
    jmi_array_ref_2(tmp_3, 11,8) = _hx_hh_grid_11_8_277;
    jmi_array_ref_2(tmp_3, 11,9) = _hx_hh_grid_11_9_278;
    jmi_array_ref_2(tmp_3, 11,10) = _hx_hh_grid_11_10_279;
    _hx_hh_283 = func_AMS_LinearMap_exp0(tmp_1, tmp_2, tmp_3, _W33_306, _fork_t_8);
    _hx_t_284 = jmi_divide_equation(jmi, (- _hx_Q_285),(- _hx_m_17 * _hx_Cmetal_281),"(- hx.Q) / (- hx.m * hx.Cmetal)");
    _hx_qh_287 = _hx_hh_283 * (_fork_t_8 - _hx_t_284);
    _hx_qc_288 = _hx_hc_323 * (_ambientair_port_t_301 - _hx_t_284);
    _der_hx_Q_339 = _hx_qh_287 + _hx_qc_288;
    _W8_313 = - (- _W33_306 + _fan_outport_w_309 + (- _W32_305));
    ef |= jmi_solve_block_residual(jmi->dae_block_residuals[0]);
    _cabin_t_295 = jmi_divide_equation(jmi, (- _cabin_p_290),(- _cabin_m_296 * 8.314472 * jmi_divide_equation(jmi, 1.0,_cabin_v_289,"(1.0 / cabin.v)") * jmi_divide_equation(jmi, 1.0,0.02897,"(1.0 / 0.02897)")),"(- cabin.p) / (- cabin.m * 8.314472 * (1.0 / cabin.v) * (1.0 / 0.02897))");
    _T8_312 = jmi_divide_equation(jmi, (- _W33_306 * _T6_310 + (- (- _fan_outport_w_309) * _cabin_t_295) + (- _W32_305 * _fork_t_8)),(-1.0 * _W8_313),"(- W33 * T6 + (- (- fan.outport.w) * cabin.t) + (- W32 * fork.t)) / (-1.0 * W8)");
    ef |= jmi_solve_block_residual(jmi->dae_block_residuals[1]);
    _der_cabin_m_340 = _W8_313 + _fan_outport_w_309 + _cabin_wa_298;
/************ Real outputs *********/
/****Integer and boolean outputs ***/
/**** Other variables ***/
    _cabin_Q_297 = _cabin_m_296 * _cabin_t_295 * 716.75;
    _W31_304 = _W32_305 + _W33_306;
/********* Write back reinits *******/

    return ef;
}

static int model_ode_derivatives_dir_der(jmi_t* jmi) {
    int ef = 0;

    return ef;
}

static int model_ode_outputs(jmi_t* jmi) {
    int ef = 0;

    return ef;
}

static int model_ode_guards_init(jmi_t* jmi) {

    return 0;
}

static int model_ode_initialize(jmi_t* jmi) {
    int ef = 0;
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_1, 11, 1)
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_2, 10, 1)
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_3, 110, 2)
    model_ode_guards(jmi);
    _fork_p_7 = 80000;
    _fork_t_8 = 350;
    _W32_305 = COND_EXP_EQ((COND_EXP_GT(_valve2_po_302, AD_WRAP_LITERAL(0.5) * _fork_p_7, JMI_TRUE, JMI_FALSE)), JMI_TRUE, AD_WRAP_LITERAL(4.72E-5) * (_fork_p_7 + AD_WRAP_LITERAL(2) * _valve2_po_302) * sqrt(jmi_divide_equation(jmi, (AD_WRAP_LITERAL(1) - jmi_divide_equation(jmi, _valve2_po_302,_fork_p_7,"valve2.po / fork.p")),_fork_t_8,"(1 - valve2.po / fork.p) / fork.t")), AD_WRAP_LITERAL(6.67E-5) * _fork_p_7 * sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(1),_fork_t_8,"1 / fork.t")));
    _hx_rhoh_286 = jmi_divide_equation(jmi, 0.02897 * _fork_p_7,(8.314472 * _fork_t_8),"0.02897 * fork.p / (8.314472 * fork.t)");
    _W33_306 = sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(2) * (_fork_p_7 - _valve2_po_302) * _hx_rhoh_286 * (1.0 * (_hx_Ahx_280) * (_hx_Ahx_280)),0.009895,"2 * (fork.p - valve2.po) * hx.rhoh * hx.Ahx ^ 2 / 0.009895"));
    _W31_304 = _W32_305 + _W33_306;
    _W1_303 = COND_EXP_EQ((COND_EXP_GT(_fork_p_7, AD_WRAP_LITERAL(0.5) * _valve1_pi_282, JMI_TRUE, JMI_FALSE)), JMI_TRUE, AD_WRAP_LITERAL(7.316E-5) * (_valve1_pi_282 + AD_WRAP_LITERAL(2) * _fork_p_7) * sqrt(jmi_divide_equation(jmi, (AD_WRAP_LITERAL(1) - jmi_divide_equation(jmi, _fork_p_7,_valve1_pi_282,"fork.p / valve1.pi")),_valve1_t_294,"(1 - fork.p / valve1.pi) / valve1.t")), AD_WRAP_LITERAL(1.0338499999999999E-4) * _valve1_pi_282 * sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(1),_valve1_t_294,"1 / valve1.t")));
    _fork_m_9 = jmi_divide_equation(jmi, (- _fork_p_7),(- _fork_t_8 * 8.314472 * jmi_divide_equation(jmi, 1.0,_fork_v_5,"(1.0 / fork.v)") * jmi_divide_equation(jmi, 1.0,0.02897,"(1.0 / 0.02897)")),"(- fork.p) / (- fork.t * 8.314472 * (1.0 / fork.v) * (1.0 / 0.02897))");
    _fork_Q_10 = _fork_m_9 * _fork_t_8 * 716.75;
    _fork_outport_w_6 = - _W33_306 + (- _W32_305);
    _der_fork_m_337 = _W1_303 + _fork_outport_w_6;
    _der_fork_Q_338 = _W1_303 * _valve1_t_294 * 716.75 + _fork_outport_w_6 * _fork_t_8 * 716.75;
    JMI_ARRAY_INIT_1(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_1, 11, 1, 11)
    jmi_array_ref_1(tmp_1, 1) = _hx_wh_grid_1_29;
    jmi_array_ref_1(tmp_1, 2) = _hx_wh_grid_2_30;
    jmi_array_ref_1(tmp_1, 3) = _hx_wh_grid_3_31;
    jmi_array_ref_1(tmp_1, 4) = _hx_wh_grid_4_32;
    jmi_array_ref_1(tmp_1, 5) = _hx_wh_grid_5_33;
    jmi_array_ref_1(tmp_1, 6) = _hx_wh_grid_6_34;
    jmi_array_ref_1(tmp_1, 7) = _hx_wh_grid_7_35;
    jmi_array_ref_1(tmp_1, 8) = _hx_wh_grid_8_36;
    jmi_array_ref_1(tmp_1, 9) = _hx_wh_grid_9_37;
    jmi_array_ref_1(tmp_1, 10) = _hx_wh_grid_10_38;
    jmi_array_ref_1(tmp_1, 11) = _hx_wh_grid_11_39;
    JMI_ARRAY_INIT_1(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_2, 10, 1, 10)
    jmi_array_ref_1(tmp_2, 1) = _hx_th_grid_1_40;
    jmi_array_ref_1(tmp_2, 2) = _hx_th_grid_2_41;
    jmi_array_ref_1(tmp_2, 3) = _hx_th_grid_3_42;
    jmi_array_ref_1(tmp_2, 4) = _hx_th_grid_4_43;
    jmi_array_ref_1(tmp_2, 5) = _hx_th_grid_5_44;
    jmi_array_ref_1(tmp_2, 6) = _hx_th_grid_6_45;
    jmi_array_ref_1(tmp_2, 7) = _hx_th_grid_7_46;
    jmi_array_ref_1(tmp_2, 8) = _hx_th_grid_8_47;
    jmi_array_ref_1(tmp_2, 9) = _hx_th_grid_9_48;
    jmi_array_ref_1(tmp_2, 10) = _hx_th_grid_10_49;
    JMI_ARRAY_INIT_2(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_3, 110, 2, 11, 10)
    jmi_array_ref_2(tmp_3, 1,1) = _hx_hh_grid_1_1_170;
    jmi_array_ref_2(tmp_3, 1,2) = _hx_hh_grid_1_2_171;
    jmi_array_ref_2(tmp_3, 1,3) = _hx_hh_grid_1_3_172;
    jmi_array_ref_2(tmp_3, 1,4) = _hx_hh_grid_1_4_173;
    jmi_array_ref_2(tmp_3, 1,5) = _hx_hh_grid_1_5_174;
    jmi_array_ref_2(tmp_3, 1,6) = _hx_hh_grid_1_6_175;
    jmi_array_ref_2(tmp_3, 1,7) = _hx_hh_grid_1_7_176;
    jmi_array_ref_2(tmp_3, 1,8) = _hx_hh_grid_1_8_177;
    jmi_array_ref_2(tmp_3, 1,9) = _hx_hh_grid_1_9_178;
    jmi_array_ref_2(tmp_3, 1,10) = _hx_hh_grid_1_10_179;
    jmi_array_ref_2(tmp_3, 2,1) = _hx_hh_grid_2_1_180;
    jmi_array_ref_2(tmp_3, 2,2) = _hx_hh_grid_2_2_181;
    jmi_array_ref_2(tmp_3, 2,3) = _hx_hh_grid_2_3_182;
    jmi_array_ref_2(tmp_3, 2,4) = _hx_hh_grid_2_4_183;
    jmi_array_ref_2(tmp_3, 2,5) = _hx_hh_grid_2_5_184;
    jmi_array_ref_2(tmp_3, 2,6) = _hx_hh_grid_2_6_185;
    jmi_array_ref_2(tmp_3, 2,7) = _hx_hh_grid_2_7_186;
    jmi_array_ref_2(tmp_3, 2,8) = _hx_hh_grid_2_8_187;
    jmi_array_ref_2(tmp_3, 2,9) = _hx_hh_grid_2_9_188;
    jmi_array_ref_2(tmp_3, 2,10) = _hx_hh_grid_2_10_189;
    jmi_array_ref_2(tmp_3, 3,1) = _hx_hh_grid_3_1_190;
    jmi_array_ref_2(tmp_3, 3,2) = _hx_hh_grid_3_2_191;
    jmi_array_ref_2(tmp_3, 3,3) = _hx_hh_grid_3_3_192;
    jmi_array_ref_2(tmp_3, 3,4) = _hx_hh_grid_3_4_193;
    jmi_array_ref_2(tmp_3, 3,5) = _hx_hh_grid_3_5_194;
    jmi_array_ref_2(tmp_3, 3,6) = _hx_hh_grid_3_6_195;
    jmi_array_ref_2(tmp_3, 3,7) = _hx_hh_grid_3_7_196;
    jmi_array_ref_2(tmp_3, 3,8) = _hx_hh_grid_3_8_197;
    jmi_array_ref_2(tmp_3, 3,9) = _hx_hh_grid_3_9_198;
    jmi_array_ref_2(tmp_3, 3,10) = _hx_hh_grid_3_10_199;
    jmi_array_ref_2(tmp_3, 4,1) = _hx_hh_grid_4_1_200;
    jmi_array_ref_2(tmp_3, 4,2) = _hx_hh_grid_4_2_201;
    jmi_array_ref_2(tmp_3, 4,3) = _hx_hh_grid_4_3_202;
    jmi_array_ref_2(tmp_3, 4,4) = _hx_hh_grid_4_4_203;
    jmi_array_ref_2(tmp_3, 4,5) = _hx_hh_grid_4_5_204;
    jmi_array_ref_2(tmp_3, 4,6) = _hx_hh_grid_4_6_205;
    jmi_array_ref_2(tmp_3, 4,7) = _hx_hh_grid_4_7_206;
    jmi_array_ref_2(tmp_3, 4,8) = _hx_hh_grid_4_8_207;
    jmi_array_ref_2(tmp_3, 4,9) = _hx_hh_grid_4_9_208;
    jmi_array_ref_2(tmp_3, 4,10) = _hx_hh_grid_4_10_209;
    jmi_array_ref_2(tmp_3, 5,1) = _hx_hh_grid_5_1_210;
    jmi_array_ref_2(tmp_3, 5,2) = _hx_hh_grid_5_2_211;
    jmi_array_ref_2(tmp_3, 5,3) = _hx_hh_grid_5_3_212;
    jmi_array_ref_2(tmp_3, 5,4) = _hx_hh_grid_5_4_213;
    jmi_array_ref_2(tmp_3, 5,5) = _hx_hh_grid_5_5_214;
    jmi_array_ref_2(tmp_3, 5,6) = _hx_hh_grid_5_6_215;
    jmi_array_ref_2(tmp_3, 5,7) = _hx_hh_grid_5_7_216;
    jmi_array_ref_2(tmp_3, 5,8) = _hx_hh_grid_5_8_217;
    jmi_array_ref_2(tmp_3, 5,9) = _hx_hh_grid_5_9_218;
    jmi_array_ref_2(tmp_3, 5,10) = _hx_hh_grid_5_10_219;
    jmi_array_ref_2(tmp_3, 6,1) = _hx_hh_grid_6_1_220;
    jmi_array_ref_2(tmp_3, 6,2) = _hx_hh_grid_6_2_221;
    jmi_array_ref_2(tmp_3, 6,3) = _hx_hh_grid_6_3_222;
    jmi_array_ref_2(tmp_3, 6,4) = _hx_hh_grid_6_4_223;
    jmi_array_ref_2(tmp_3, 6,5) = _hx_hh_grid_6_5_224;
    jmi_array_ref_2(tmp_3, 6,6) = _hx_hh_grid_6_6_225;
    jmi_array_ref_2(tmp_3, 6,7) = _hx_hh_grid_6_7_226;
    jmi_array_ref_2(tmp_3, 6,8) = _hx_hh_grid_6_8_227;
    jmi_array_ref_2(tmp_3, 6,9) = _hx_hh_grid_6_9_228;
    jmi_array_ref_2(tmp_3, 6,10) = _hx_hh_grid_6_10_229;
    jmi_array_ref_2(tmp_3, 7,1) = _hx_hh_grid_7_1_230;
    jmi_array_ref_2(tmp_3, 7,2) = _hx_hh_grid_7_2_231;
    jmi_array_ref_2(tmp_3, 7,3) = _hx_hh_grid_7_3_232;
    jmi_array_ref_2(tmp_3, 7,4) = _hx_hh_grid_7_4_233;
    jmi_array_ref_2(tmp_3, 7,5) = _hx_hh_grid_7_5_234;
    jmi_array_ref_2(tmp_3, 7,6) = _hx_hh_grid_7_6_235;
    jmi_array_ref_2(tmp_3, 7,7) = _hx_hh_grid_7_7_236;
    jmi_array_ref_2(tmp_3, 7,8) = _hx_hh_grid_7_8_237;
    jmi_array_ref_2(tmp_3, 7,9) = _hx_hh_grid_7_9_238;
    jmi_array_ref_2(tmp_3, 7,10) = _hx_hh_grid_7_10_239;
    jmi_array_ref_2(tmp_3, 8,1) = _hx_hh_grid_8_1_240;
    jmi_array_ref_2(tmp_3, 8,2) = _hx_hh_grid_8_2_241;
    jmi_array_ref_2(tmp_3, 8,3) = _hx_hh_grid_8_3_242;
    jmi_array_ref_2(tmp_3, 8,4) = _hx_hh_grid_8_4_243;
    jmi_array_ref_2(tmp_3, 8,5) = _hx_hh_grid_8_5_244;
    jmi_array_ref_2(tmp_3, 8,6) = _hx_hh_grid_8_6_245;
    jmi_array_ref_2(tmp_3, 8,7) = _hx_hh_grid_8_7_246;
    jmi_array_ref_2(tmp_3, 8,8) = _hx_hh_grid_8_8_247;
    jmi_array_ref_2(tmp_3, 8,9) = _hx_hh_grid_8_9_248;
    jmi_array_ref_2(tmp_3, 8,10) = _hx_hh_grid_8_10_249;
    jmi_array_ref_2(tmp_3, 9,1) = _hx_hh_grid_9_1_250;
    jmi_array_ref_2(tmp_3, 9,2) = _hx_hh_grid_9_2_251;
    jmi_array_ref_2(tmp_3, 9,3) = _hx_hh_grid_9_3_252;
    jmi_array_ref_2(tmp_3, 9,4) = _hx_hh_grid_9_4_253;
    jmi_array_ref_2(tmp_3, 9,5) = _hx_hh_grid_9_5_254;
    jmi_array_ref_2(tmp_3, 9,6) = _hx_hh_grid_9_6_255;
    jmi_array_ref_2(tmp_3, 9,7) = _hx_hh_grid_9_7_256;
    jmi_array_ref_2(tmp_3, 9,8) = _hx_hh_grid_9_8_257;
    jmi_array_ref_2(tmp_3, 9,9) = _hx_hh_grid_9_9_258;
    jmi_array_ref_2(tmp_3, 9,10) = _hx_hh_grid_9_10_259;
    jmi_array_ref_2(tmp_3, 10,1) = _hx_hh_grid_10_1_260;
    jmi_array_ref_2(tmp_3, 10,2) = _hx_hh_grid_10_2_261;
    jmi_array_ref_2(tmp_3, 10,3) = _hx_hh_grid_10_3_262;
    jmi_array_ref_2(tmp_3, 10,4) = _hx_hh_grid_10_4_263;
    jmi_array_ref_2(tmp_3, 10,5) = _hx_hh_grid_10_5_264;
    jmi_array_ref_2(tmp_3, 10,6) = _hx_hh_grid_10_6_265;
    jmi_array_ref_2(tmp_3, 10,7) = _hx_hh_grid_10_7_266;
    jmi_array_ref_2(tmp_3, 10,8) = _hx_hh_grid_10_8_267;
    jmi_array_ref_2(tmp_3, 10,9) = _hx_hh_grid_10_9_268;
    jmi_array_ref_2(tmp_3, 10,10) = _hx_hh_grid_10_10_269;
    jmi_array_ref_2(tmp_3, 11,1) = _hx_hh_grid_11_1_270;
    jmi_array_ref_2(tmp_3, 11,2) = _hx_hh_grid_11_2_271;
    jmi_array_ref_2(tmp_3, 11,3) = _hx_hh_grid_11_3_272;
    jmi_array_ref_2(tmp_3, 11,4) = _hx_hh_grid_11_4_273;
    jmi_array_ref_2(tmp_3, 11,5) = _hx_hh_grid_11_5_274;
    jmi_array_ref_2(tmp_3, 11,6) = _hx_hh_grid_11_6_275;
    jmi_array_ref_2(tmp_3, 11,7) = _hx_hh_grid_11_7_276;
    jmi_array_ref_2(tmp_3, 11,8) = _hx_hh_grid_11_8_277;
    jmi_array_ref_2(tmp_3, 11,9) = _hx_hh_grid_11_9_278;
    jmi_array_ref_2(tmp_3, 11,10) = _hx_hh_grid_11_10_279;
    _hx_hh_283 = func_AMS_LinearMap_exp0(tmp_1, tmp_2, tmp_3, _W33_306, _fork_t_8);
    _hx_t_284 = 250;
    _hx_qh_287 = _hx_hh_283 * (_fork_t_8 - _hx_t_284);
    ef |= jmi_solve_block_residual(jmi->dae_init_block_residuals[0]);
    _hx_Q_285 = _hx_m_17 * _hx_t_284 * _hx_Cmetal_281;
    _hx_qc_288 = _hx_hc_323 * (_ambientair_port_t_301 - _hx_t_284);
    _der_hx_Q_339 = _hx_qh_287 + _hx_qc_288;
    _W8_313 = - (- _W33_306 + _fan_outport_w_309 + (- _W32_305));
    _cabin_t_295 = 297.2;
    _T8_312 = jmi_divide_equation(jmi, (- _W33_306 * _T6_310 + (- (- _fan_outport_w_309) * _cabin_t_295) + (- _W32_305 * _fork_t_8)),(-1.0 * _W8_313),"(- W33 * T6 + (- (- fan.outport.w) * cabin.t) + (- W32 * fork.t)) / (-1.0 * W8)");
    _cabin_m_296 = jmi_divide_equation(jmi, (- _cabin_p_290),(- _cabin_t_295 * 8.314472 * jmi_divide_equation(jmi, 1.0,_cabin_v_289,"(1.0 / cabin.v)") * jmi_divide_equation(jmi, 1.0,0.02897,"(1.0 / 0.02897)")),"(- cabin.p) / (- cabin.t * 8.314472 * (1.0 / cabin.v) * (1.0 / 0.02897))");
    _cabin_Q_297 = _cabin_m_296 * _cabin_t_295 * 716.75;
    ef |= jmi_solve_block_residual(jmi->dae_init_block_residuals[1]);
    _der_cabin_m_340 = _W8_313 + _fan_outport_w_309 + _cabin_wa_298;

    return ef;
}


static int model_ode_initialize_dir_der(jmi_t* jmi) {
    int ef = 0;
    /* This function is not needed - no derivatives of the initialization system is exposed.*/
    return ef;
}

static int model_dae_F(jmi_t* jmi, jmi_real_t** res) {

    return 0;
}

static int model_dae_dir_dF(jmi_t* jmi, jmi_real_t** res, jmi_real_t** dF, jmi_real_t** dz) {

    return 0;
}

static int model_dae_R(jmi_t* jmi, jmi_real_t** res) {

    return 0;
}

static int model_init_F0(jmi_t* jmi, jmi_real_t** res) {

    return 0;
}

static int model_init_F1(jmi_t* jmi, jmi_real_t** res) {
    (*res)[0] = 0.0 - _fork_outport_w_6;
    (*res)[1] = 0.0 - _fork_m_9;
    (*res)[2] = 0.0 - _fork_Q_10;
    (*res)[3] = 0.0 - _hx_hh_283;
    (*res)[4] = 0.0 - _hx_Q_285;
    (*res)[5] = 0.0 - _hx_rhoh_286;
    (*res)[6] = 0.0 - _hx_qh_287;
    (*res)[7] = 0.0 - _hx_qc_288;
    (*res)[8] = 0.0 - _cabin_m_296;
    (*res)[9] = 0.0 - _cabin_Q_297;
    (*res)[10] = 0.0 - _cabin_wa_298;
    (*res)[11] = 0.0 - _W1_303;
    (*res)[12] = 0.0 - _W31_304;
    (*res)[13] = 0.0 - _W32_305;
    (*res)[14] = 0.0 - _W33_306;
    (*res)[15] = 288.15 - _T6_310;
    (*res)[16] = 288.15 - _T8_312;
    (*res)[17] = 0.0 - _W8_313;

    return 0;
}

static int model_init_Fp(jmi_t* jmi, jmi_real_t** res) {
    /* C_DAE_initial_dependent_parameter_residuals */
    return -1;
}

static int model_init_eval_parameters(jmi_t* jmi) {
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_4, 11, 1)
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_5, 10, 1)
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_6, 110, 2)
    _T1_2 = (_inlet_t_env_1);
    _P1_3 = (_inlet_p_env_0);
    _T4_11 = (_ambientair_t_env_14);
    _P5_15 = (_cabin_p_290);
    _W7_16 = (_fan_wf_300);
    _valve1_pi_282 = (_inlet_p_env_0);
    _valve1_t_294 = (_inlet_t_env_1);
    _ambientair_port_p_299 = (_ambientair_p_env_13);
    _ambientair_port_t_301 = (_ambientair_t_env_14);
    _valve2_po_302 = (_cabin_p_290);
    _cabin_outport_p_307 = (_cabin_p_290);
    _fan_outport_w_309 = (- _fan_wf_300);
    _valve1_inport_p_311 = (_valve1_pi_282);
    _inlet_port_p_314 = (_valve1_pi_282);
    _valve1_inport_t_315 = (_valve1_t_294);
    _valve1_outport_t_316 = (_valve1_t_294);
    _inlet_port_t_317 = (_valve1_t_294);
    _fork_inport_t_318 = (_valve1_t_294);
    _hx_cin_p_319 = (_ambientair_port_p_299);
    _hx_cin_t_320 = (_ambientair_port_t_301);
    JMI_ARRAY_INIT_1(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_4, 11, 1, 11)
    jmi_array_ref_1(tmp_4, 1) = _hx_wc_grid_1_18;
    jmi_array_ref_1(tmp_4, 2) = _hx_wc_grid_2_19;
    jmi_array_ref_1(tmp_4, 3) = _hx_wc_grid_3_20;
    jmi_array_ref_1(tmp_4, 4) = _hx_wc_grid_4_21;
    jmi_array_ref_1(tmp_4, 5) = _hx_wc_grid_5_22;
    jmi_array_ref_1(tmp_4, 6) = _hx_wc_grid_6_23;
    jmi_array_ref_1(tmp_4, 7) = _hx_wc_grid_7_24;
    jmi_array_ref_1(tmp_4, 8) = _hx_wc_grid_8_25;
    jmi_array_ref_1(tmp_4, 9) = _hx_wc_grid_9_26;
    jmi_array_ref_1(tmp_4, 10) = _hx_wc_grid_10_27;
    jmi_array_ref_1(tmp_4, 11) = _hx_wc_grid_11_28;
    JMI_ARRAY_INIT_1(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_5, 10, 1, 10)
    jmi_array_ref_1(tmp_5, 1) = _hx_tc_grid_1_50;
    jmi_array_ref_1(tmp_5, 2) = _hx_tc_grid_2_51;
    jmi_array_ref_1(tmp_5, 3) = _hx_tc_grid_3_52;
    jmi_array_ref_1(tmp_5, 4) = _hx_tc_grid_4_53;
    jmi_array_ref_1(tmp_5, 5) = _hx_tc_grid_5_54;
    jmi_array_ref_1(tmp_5, 6) = _hx_tc_grid_6_55;
    jmi_array_ref_1(tmp_5, 7) = _hx_tc_grid_7_56;
    jmi_array_ref_1(tmp_5, 8) = _hx_tc_grid_8_57;
    jmi_array_ref_1(tmp_5, 9) = _hx_tc_grid_9_58;
    jmi_array_ref_1(tmp_5, 10) = _hx_tc_grid_10_59;
    JMI_ARRAY_INIT_2(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_6, 110, 2, 11, 10)
    jmi_array_ref_2(tmp_6, 1,1) = _hx_hc_grid_1_1_60;
    jmi_array_ref_2(tmp_6, 1,2) = _hx_hc_grid_1_2_61;
    jmi_array_ref_2(tmp_6, 1,3) = _hx_hc_grid_1_3_62;
    jmi_array_ref_2(tmp_6, 1,4) = _hx_hc_grid_1_4_63;
    jmi_array_ref_2(tmp_6, 1,5) = _hx_hc_grid_1_5_64;
    jmi_array_ref_2(tmp_6, 1,6) = _hx_hc_grid_1_6_65;
    jmi_array_ref_2(tmp_6, 1,7) = _hx_hc_grid_1_7_66;
    jmi_array_ref_2(tmp_6, 1,8) = _hx_hc_grid_1_8_67;
    jmi_array_ref_2(tmp_6, 1,9) = _hx_hc_grid_1_9_68;
    jmi_array_ref_2(tmp_6, 1,10) = _hx_hc_grid_1_10_69;
    jmi_array_ref_2(tmp_6, 2,1) = _hx_hc_grid_2_1_70;
    jmi_array_ref_2(tmp_6, 2,2) = _hx_hc_grid_2_2_71;
    jmi_array_ref_2(tmp_6, 2,3) = _hx_hc_grid_2_3_72;
    jmi_array_ref_2(tmp_6, 2,4) = _hx_hc_grid_2_4_73;
    jmi_array_ref_2(tmp_6, 2,5) = _hx_hc_grid_2_5_74;
    jmi_array_ref_2(tmp_6, 2,6) = _hx_hc_grid_2_6_75;
    jmi_array_ref_2(tmp_6, 2,7) = _hx_hc_grid_2_7_76;
    jmi_array_ref_2(tmp_6, 2,8) = _hx_hc_grid_2_8_77;
    jmi_array_ref_2(tmp_6, 2,9) = _hx_hc_grid_2_9_78;
    jmi_array_ref_2(tmp_6, 2,10) = _hx_hc_grid_2_10_79;
    jmi_array_ref_2(tmp_6, 3,1) = _hx_hc_grid_3_1_80;
    jmi_array_ref_2(tmp_6, 3,2) = _hx_hc_grid_3_2_81;
    jmi_array_ref_2(tmp_6, 3,3) = _hx_hc_grid_3_3_82;
    jmi_array_ref_2(tmp_6, 3,4) = _hx_hc_grid_3_4_83;
    jmi_array_ref_2(tmp_6, 3,5) = _hx_hc_grid_3_5_84;
    jmi_array_ref_2(tmp_6, 3,6) = _hx_hc_grid_3_6_85;
    jmi_array_ref_2(tmp_6, 3,7) = _hx_hc_grid_3_7_86;
    jmi_array_ref_2(tmp_6, 3,8) = _hx_hc_grid_3_8_87;
    jmi_array_ref_2(tmp_6, 3,9) = _hx_hc_grid_3_9_88;
    jmi_array_ref_2(tmp_6, 3,10) = _hx_hc_grid_3_10_89;
    jmi_array_ref_2(tmp_6, 4,1) = _hx_hc_grid_4_1_90;
    jmi_array_ref_2(tmp_6, 4,2) = _hx_hc_grid_4_2_91;
    jmi_array_ref_2(tmp_6, 4,3) = _hx_hc_grid_4_3_92;
    jmi_array_ref_2(tmp_6, 4,4) = _hx_hc_grid_4_4_93;
    jmi_array_ref_2(tmp_6, 4,5) = _hx_hc_grid_4_5_94;
    jmi_array_ref_2(tmp_6, 4,6) = _hx_hc_grid_4_6_95;
    jmi_array_ref_2(tmp_6, 4,7) = _hx_hc_grid_4_7_96;
    jmi_array_ref_2(tmp_6, 4,8) = _hx_hc_grid_4_8_97;
    jmi_array_ref_2(tmp_6, 4,9) = _hx_hc_grid_4_9_98;
    jmi_array_ref_2(tmp_6, 4,10) = _hx_hc_grid_4_10_99;
    jmi_array_ref_2(tmp_6, 5,1) = _hx_hc_grid_5_1_100;
    jmi_array_ref_2(tmp_6, 5,2) = _hx_hc_grid_5_2_101;
    jmi_array_ref_2(tmp_6, 5,3) = _hx_hc_grid_5_3_102;
    jmi_array_ref_2(tmp_6, 5,4) = _hx_hc_grid_5_4_103;
    jmi_array_ref_2(tmp_6, 5,5) = _hx_hc_grid_5_5_104;
    jmi_array_ref_2(tmp_6, 5,6) = _hx_hc_grid_5_6_105;
    jmi_array_ref_2(tmp_6, 5,7) = _hx_hc_grid_5_7_106;
    jmi_array_ref_2(tmp_6, 5,8) = _hx_hc_grid_5_8_107;
    jmi_array_ref_2(tmp_6, 5,9) = _hx_hc_grid_5_9_108;
    jmi_array_ref_2(tmp_6, 5,10) = _hx_hc_grid_5_10_109;
    jmi_array_ref_2(tmp_6, 6,1) = _hx_hc_grid_6_1_110;
    jmi_array_ref_2(tmp_6, 6,2) = _hx_hc_grid_6_2_111;
    jmi_array_ref_2(tmp_6, 6,3) = _hx_hc_grid_6_3_112;
    jmi_array_ref_2(tmp_6, 6,4) = _hx_hc_grid_6_4_113;
    jmi_array_ref_2(tmp_6, 6,5) = _hx_hc_grid_6_5_114;
    jmi_array_ref_2(tmp_6, 6,6) = _hx_hc_grid_6_6_115;
    jmi_array_ref_2(tmp_6, 6,7) = _hx_hc_grid_6_7_116;
    jmi_array_ref_2(tmp_6, 6,8) = _hx_hc_grid_6_8_117;
    jmi_array_ref_2(tmp_6, 6,9) = _hx_hc_grid_6_9_118;
    jmi_array_ref_2(tmp_6, 6,10) = _hx_hc_grid_6_10_119;
    jmi_array_ref_2(tmp_6, 7,1) = _hx_hc_grid_7_1_120;
    jmi_array_ref_2(tmp_6, 7,2) = _hx_hc_grid_7_2_121;
    jmi_array_ref_2(tmp_6, 7,3) = _hx_hc_grid_7_3_122;
    jmi_array_ref_2(tmp_6, 7,4) = _hx_hc_grid_7_4_123;
    jmi_array_ref_2(tmp_6, 7,5) = _hx_hc_grid_7_5_124;
    jmi_array_ref_2(tmp_6, 7,6) = _hx_hc_grid_7_6_125;
    jmi_array_ref_2(tmp_6, 7,7) = _hx_hc_grid_7_7_126;
    jmi_array_ref_2(tmp_6, 7,8) = _hx_hc_grid_7_8_127;
    jmi_array_ref_2(tmp_6, 7,9) = _hx_hc_grid_7_9_128;
    jmi_array_ref_2(tmp_6, 7,10) = _hx_hc_grid_7_10_129;
    jmi_array_ref_2(tmp_6, 8,1) = _hx_hc_grid_8_1_130;
    jmi_array_ref_2(tmp_6, 8,2) = _hx_hc_grid_8_2_131;
    jmi_array_ref_2(tmp_6, 8,3) = _hx_hc_grid_8_3_132;
    jmi_array_ref_2(tmp_6, 8,4) = _hx_hc_grid_8_4_133;
    jmi_array_ref_2(tmp_6, 8,5) = _hx_hc_grid_8_5_134;
    jmi_array_ref_2(tmp_6, 8,6) = _hx_hc_grid_8_6_135;
    jmi_array_ref_2(tmp_6, 8,7) = _hx_hc_grid_8_7_136;
    jmi_array_ref_2(tmp_6, 8,8) = _hx_hc_grid_8_8_137;
    jmi_array_ref_2(tmp_6, 8,9) = _hx_hc_grid_8_9_138;
    jmi_array_ref_2(tmp_6, 8,10) = _hx_hc_grid_8_10_139;
    jmi_array_ref_2(tmp_6, 9,1) = _hx_hc_grid_9_1_140;
    jmi_array_ref_2(tmp_6, 9,2) = _hx_hc_grid_9_2_141;
    jmi_array_ref_2(tmp_6, 9,3) = _hx_hc_grid_9_3_142;
    jmi_array_ref_2(tmp_6, 9,4) = _hx_hc_grid_9_4_143;
    jmi_array_ref_2(tmp_6, 9,5) = _hx_hc_grid_9_5_144;
    jmi_array_ref_2(tmp_6, 9,6) = _hx_hc_grid_9_6_145;
    jmi_array_ref_2(tmp_6, 9,7) = _hx_hc_grid_9_7_146;
    jmi_array_ref_2(tmp_6, 9,8) = _hx_hc_grid_9_8_147;
    jmi_array_ref_2(tmp_6, 9,9) = _hx_hc_grid_9_9_148;
    jmi_array_ref_2(tmp_6, 9,10) = _hx_hc_grid_9_10_149;
    jmi_array_ref_2(tmp_6, 10,1) = _hx_hc_grid_10_1_150;
    jmi_array_ref_2(tmp_6, 10,2) = _hx_hc_grid_10_2_151;
    jmi_array_ref_2(tmp_6, 10,3) = _hx_hc_grid_10_3_152;
    jmi_array_ref_2(tmp_6, 10,4) = _hx_hc_grid_10_4_153;
    jmi_array_ref_2(tmp_6, 10,5) = _hx_hc_grid_10_5_154;
    jmi_array_ref_2(tmp_6, 10,6) = _hx_hc_grid_10_6_155;
    jmi_array_ref_2(tmp_6, 10,7) = _hx_hc_grid_10_7_156;
    jmi_array_ref_2(tmp_6, 10,8) = _hx_hc_grid_10_8_157;
    jmi_array_ref_2(tmp_6, 10,9) = _hx_hc_grid_10_9_158;
    jmi_array_ref_2(tmp_6, 10,10) = _hx_hc_grid_10_10_159;
    jmi_array_ref_2(tmp_6, 11,1) = _hx_hc_grid_11_1_160;
    jmi_array_ref_2(tmp_6, 11,2) = _hx_hc_grid_11_2_161;
    jmi_array_ref_2(tmp_6, 11,3) = _hx_hc_grid_11_3_162;
    jmi_array_ref_2(tmp_6, 11,4) = _hx_hc_grid_11_4_163;
    jmi_array_ref_2(tmp_6, 11,5) = _hx_hc_grid_11_5_164;
    jmi_array_ref_2(tmp_6, 11,6) = _hx_hc_grid_11_6_165;
    jmi_array_ref_2(tmp_6, 11,7) = _hx_hc_grid_11_7_166;
    jmi_array_ref_2(tmp_6, 11,8) = _hx_hc_grid_11_8_167;
    jmi_array_ref_2(tmp_6, 11,9) = _hx_hc_grid_11_9_168;
    jmi_array_ref_2(tmp_6, 11,10) = _hx_hc_grid_11_10_169;
    _hx_hc_323 = (func_AMS_LinearMap_exp0(tmp_4, tmp_5, tmp_6, 4.0, _ambientair_port_t_301));
    _mixer_inport_2_p_324 = (_valve2_po_302);
    _mixer_inport_1_p_326 = (_valve2_po_302);
    _mixer_inport_3_p_327 = (_valve2_po_302);
    _mixer_outport_p_328 = (_valve2_po_302);
    _hx_hout_p_329 = (_valve2_po_302);
    _valve2_outport_p_330 = (_valve2_po_302);
    _cabin_inport_p_331 = (_valve2_po_302);
    _fan_outport_p_332 = (_valve2_po_302);
    _fan_inport_p_333 = (_cabin_outport_p_307);
    _fan_inport_w_334 = (- _fan_outport_w_309);
    _mixer_inport_2_w_335 = (- _fan_outport_w_309);
    _cabin_outport_w_336 = (_fan_outport_w_309);

    return 0;
}

static int model_init_R0(jmi_t* jmi, jmi_real_t** res) {

    return 0;
}

static int model_init_delay(jmi_t* jmi) {

    return 0;
}

static int model_sample_delay(jmi_t* jmi) {

    return 0;
}

int jmi_new(jmi_t** jmi, jmi_callbacks_t* jmi_callbacks) {

    jmi_init(jmi, N_real_ci, N_real_cd,  N_real_pi,    N_real_pi_s,    N_real_pi_f,    N_real_pi_e,    N_real_pd,
             N_integer_ci, N_integer_cd, N_integer_pi, N_integer_pi_s, N_integer_pi_f, N_integer_pi_e, N_integer_pd,
             N_boolean_ci, N_boolean_cd, N_boolean_pi, N_boolean_pi_s, N_boolean_pi_f, N_boolean_pi_e, N_boolean_pd,
             N_string_ci,  N_string_cd,  N_string_pi,  N_string_pi_s,  N_string_pi_f,  N_string_pi_e,  N_string_pd,
             N_real_dx, N_real_x, N_real_u, N_real_w,
             N_real_d, N_integer_d, N_integer_u, N_boolean_d, N_boolean_u,
             N_string_d, N_string_u, N_outputs, (int (*))Output_vrefs,
             N_sw, N_sw_init, N_time_sw,N_state_sw, N_guards, N_guards_init,
             N_dae_blocks, N_dae_init_blocks,
             N_initial_relations, (int (*))DAE_initial_relations,
             N_relations, (int (*))DAE_relations,
             (jmi_real_t *) DAE_nominals,
             Scaling_method, N_ext_objs, jmi_callbacks);

    jmi_dae_add_equation_block(*jmi, dae_block_0, NULL, 1, 0, 0, JMI_CONTINUOUS_VARIABILITY, JMI_CONSTANT_VARIABILITY, JMI_LINEAR_SOLVER, 0, "1", -1);
    jmi_dae_add_equation_block(*jmi, dae_block_1, NULL, 1, 0, 0, JMI_CONTINUOUS_VARIABILITY, JMI_CONSTANT_VARIABILITY, JMI_LINEAR_SOLVER, 1, "2", -1);


    jmi_dae_init_add_equation_block(*jmi, dae_init_block_0, NULL, 1, 0, 0, JMI_CONTINUOUS_VARIABILITY, JMI_CONSTANT_VARIABILITY, JMI_LINEAR_SOLVER, 0, "1", -1);
    jmi_dae_init_add_equation_block(*jmi, dae_init_block_1, NULL, 1, 0, 0, JMI_CONTINUOUS_VARIABILITY, JMI_CONSTANT_VARIABILITY, JMI_LINEAR_SOLVER, 1, "2", -1);






    /* Initialize the DAE interface */
    jmi_dae_init(*jmi, *model_dae_F, N_eq_F, NULL, 0, NULL, NULL,
                 *model_dae_dir_dF,
                 CAD_dae_n_nz,(int (*))CAD_dae_nz_rows,(int (*))CAD_dae_nz_cols,
                 CAD_ODE_A_n_nz, (int (*))CAD_ODE_A_nz_rows, (int(*))CAD_ODE_A_nz_cols,
                 CAD_ODE_B_n_nz, (int (*))CAD_ODE_B_nz_rows, (int(*))CAD_ODE_B_nz_cols,
                 CAD_ODE_C_n_nz, (int (*))CAD_ODE_C_nz_rows, (int(*))CAD_ODE_C_nz_cols,
                 CAD_ODE_D_n_nz, (int (*))CAD_ODE_D_nz_rows, (int(*))CAD_ODE_D_nz_cols,
                 *model_dae_R, N_eq_R, NULL, 0, NULL, NULL,*model_ode_derivatives,
                 *model_ode_derivatives_dir_der,
                 *model_ode_outputs,*model_ode_initialize,*model_ode_guards,
                 *model_ode_guards_init,*model_ode_next_time_event);

    /* Initialize the Init interface */
    jmi_init_init(*jmi, *model_init_F0, N_eq_F0, NULL,
                  0, NULL, NULL,
                  *model_init_F1, N_eq_F1, NULL,
                  0, NULL, NULL,
                  *model_init_Fp, N_eq_Fp, NULL,
                  0, NULL, NULL,
                  *model_init_eval_parameters,
                  *model_init_R0, N_eq_R0, NULL,
                  0, NULL, NULL);
    
    /* Initialize the delay interface */
    jmi_init_delay_if(*jmi, N_delays, N_spatialdists, *model_init_delay, *model_sample_delay, N_delay_sw);

    return 0;
}

int jmi_destruct_external_objs(jmi_t* jmi) {

    return 0;
}

int jmi_set_start_values(jmi_t* jmi) {
    _valve1_C_4 = (0.155);
    _valve2_C_12 = (0.1);
    _W4_308 = (4);
    _ambientair_port_w_321 = (-4);
    _hx_wc_322 = (4);
    _hx_cin_w_325 = (4);
    _inlet_p_env_0 = (259932);
    _inlet_t_env_1 = (350);
    _fork_v_5 = (0.004916);
    _ambientair_p_env_13 = (50000);
    _ambientair_t_env_14 = (220);
    _hx_m_17 = (13.61);
    _hx_wc_grid_1_18 = (0.76);
    _hx_wc_grid_2_19 = (1.51);
    _hx_wc_grid_3_20 = (2.27);
    _hx_wc_grid_4_21 = (3.02);
    _hx_wc_grid_5_22 = (3.78);
    _hx_wc_grid_6_23 = (4.54);
    _hx_wc_grid_7_24 = (5.29);
    _hx_wc_grid_8_25 = (6.05);
    _hx_wc_grid_9_26 = (6.8);
    _hx_wc_grid_10_27 = (7.56);
    _hx_wc_grid_11_28 = (8.32);
    _hx_wh_grid_1_29 = (0.19);
    _hx_wh_grid_2_30 = (0.38);
    _hx_wh_grid_3_31 = (0.57);
    _hx_wh_grid_4_32 = (0.76);
    _hx_wh_grid_5_33 = (0.95);
    _hx_wh_grid_6_34 = (1.13);
    _hx_wh_grid_7_35 = (1.32);
    _hx_wh_grid_8_36 = (1.51);
    _hx_wh_grid_9_37 = (1.7);
    _hx_wh_grid_10_38 = (1.89);
    _hx_wh_grid_11_39 = (2.08);
    _hx_th_grid_1_40 = (200.0);
    _hx_th_grid_2_41 = (216.67);
    _hx_th_grid_3_42 = (233.33);
    _hx_th_grid_4_43 = (250.0);
    _hx_th_grid_5_44 = (266.67);
    _hx_th_grid_6_45 = (283.33);
    _hx_th_grid_7_46 = (300.0);
    _hx_th_grid_8_47 = (316.67);
    _hx_th_grid_9_48 = (333.33);
    _hx_th_grid_10_49 = (366.67);
    _hx_tc_grid_1_50 = (200.0);
    _hx_tc_grid_2_51 = (216.67);
    _hx_tc_grid_3_52 = (233.33);
    _hx_tc_grid_4_53 = (250.0);
    _hx_tc_grid_5_54 = (266.67);
    _hx_tc_grid_6_55 = (283.33);
    _hx_tc_grid_7_56 = (300.0);
    _hx_tc_grid_8_57 = (316.67);
    _hx_tc_grid_9_58 = (333.33);
    _hx_tc_grid_10_59 = (366.67);
    _hx_hc_grid_1_1_60 = (5.08);
    _hx_hc_grid_1_2_61 = (5.12);
    _hx_hc_grid_1_3_62 = (5.2);
    _hx_hc_grid_1_4_63 = (5.28);
    _hx_hc_grid_1_5_64 = (5.37);
    _hx_hc_grid_1_6_65 = (5.45);
    _hx_hc_grid_1_7_66 = (5.53);
    _hx_hc_grid_1_8_67 = (5.61);
    _hx_hc_grid_1_9_68 = (5.65);
    _hx_hc_grid_1_10_69 = (5.81);
    _hx_hc_grid_2_1_70 = (8.17);
    _hx_hc_grid_2_2_71 = (8.25);
    _hx_hc_grid_2_3_72 = (8.37);
    _hx_hc_grid_2_4_73 = (8.54);
    _hx_hc_grid_2_5_74 = (8.66);
    _hx_hc_grid_2_6_75 = (8.82);
    _hx_hc_grid_2_7_76 = (8.94);
    _hx_hc_grid_2_8_77 = (9.06);
    _hx_hc_grid_2_9_78 = (9.15);
    _hx_hc_grid_2_10_79 = (9.35);
    _hx_hc_grid_3_1_80 = (10.61);
    _hx_hc_grid_3_2_81 = (10.69);
    _hx_hc_grid_3_3_82 = (10.85);
    _hx_hc_grid_3_4_83 = (11.06);
    _hx_hc_grid_3_5_84 = (11.22);
    _hx_hc_grid_3_6_85 = (11.38);
    _hx_hc_grid_3_7_86 = (11.59);
    _hx_hc_grid_3_8_87 = (11.75);
    _hx_hc_grid_3_9_88 = (11.91);
    _hx_hc_grid_3_10_89 = (12.15);
    _hx_hc_grid_4_1_90 = (12.52);
    _hx_hc_grid_4_2_91 = (12.68);
    _hx_hc_grid_4_3_92 = (12.89);
    _hx_hc_grid_4_4_93 = (13.13);
    _hx_hc_grid_4_5_94 = (13.37);
    _hx_hc_grid_4_6_95 = (13.62);
    _hx_hc_grid_4_7_96 = (13.82);
    _hx_hc_grid_4_8_97 = (14.02);
    _hx_hc_grid_4_9_98 = (14.19);
    _hx_hc_grid_4_10_99 = (14.51);
    _hx_hc_grid_5_1_100 = (14.15);
    _hx_hc_grid_5_2_101 = (14.35);
    _hx_hc_grid_5_3_102 = (14.59);
    _hx_hc_grid_5_4_103 = (14.88);
    _hx_hc_grid_5_5_104 = (15.12);
    _hx_hc_grid_5_6_105 = (15.41);
    _hx_hc_grid_5_7_106 = (15.65);
    _hx_hc_grid_5_8_107 = (15.89);
    _hx_hc_grid_5_9_108 = (16.14);
    _hx_hc_grid_5_10_109 = (16.54);
    _hx_hc_grid_6_1_110 = (15.65);
    _hx_hc_grid_6_2_111 = (15.85);
    _hx_hc_grid_6_3_112 = (16.1);
    _hx_hc_grid_6_4_113 = (16.42);
    _hx_hc_grid_6_5_114 = (16.71);
    _hx_hc_grid_6_6_115 = (16.99);
    _hx_hc_grid_6_7_116 = (17.28);
    _hx_hc_grid_6_8_117 = (17.52);
    _hx_hc_grid_6_9_118 = (17.8);
    _hx_hc_grid_6_10_119 = (18.25);
    _hx_hc_grid_7_1_120 = (16.95);
    _hx_hc_grid_7_2_121 = (17.19);
    _hx_hc_grid_7_3_122 = (17.48);
    _hx_hc_grid_7_4_123 = (17.8);
    _hx_hc_grid_7_5_124 = (18.13);
    _hx_hc_grid_7_6_125 = (18.41);
    _hx_hc_grid_7_7_126 = (18.74);
    _hx_hc_grid_7_8_127 = (19.02);
    _hx_hc_grid_7_9_128 = (19.27);
    _hx_hc_grid_7_10_129 = (19.76);
    _hx_hc_grid_8_1_130 = (18.13);
    _hx_hc_grid_8_2_131 = (18.37);
    _hx_hc_grid_8_3_132 = (18.7);
    _hx_hc_grid_8_4_133 = (19.06);
    _hx_hc_grid_8_5_134 = (19.43);
    _hx_hc_grid_8_6_135 = (19.76);
    _hx_hc_grid_8_7_136 = (20.08);
    _hx_hc_grid_8_8_137 = (20.37);
    _hx_hc_grid_8_9_138 = (20.65);
    _hx_hc_grid_8_10_139 = (21.18);
    _hx_hc_grid_9_1_140 = (19.23);
    _hx_hc_grid_9_2_141 = (19.47);
    _hx_hc_grid_9_3_142 = (19.84);
    _hx_hc_grid_9_4_143 = (20.2);
    _hx_hc_grid_9_5_144 = (20.57);
    _hx_hc_grid_9_6_145 = (20.93);
    _hx_hc_grid_9_7_146 = (21.3);
    _hx_hc_grid_9_8_147 = (21.63);
    _hx_hc_grid_9_9_148 = (21.95);
    _hx_hc_grid_9_10_149 = (22.48);
    _hx_hc_grid_10_1_150 = (20.28);
    _hx_hc_grid_10_2_151 = (20.53);
    _hx_hc_grid_10_3_152 = (20.89);
    _hx_hc_grid_10_4_153 = (21.3);
    _hx_hc_grid_10_5_154 = (21.67);
    _hx_hc_grid_10_6_155 = (22.03);
    _hx_hc_grid_10_7_156 = (22.4);
    _hx_hc_grid_10_8_157 = (22.76);
    _hx_hc_grid_10_9_158 = (23.09);
    _hx_hc_grid_10_10_159 = (23.7);
    _hx_hc_grid_11_1_160 = (20.28);
    _hx_hc_grid_11_2_161 = (20.53);
    _hx_hc_grid_11_3_162 = (20.89);
    _hx_hc_grid_11_4_163 = (21.3);
    _hx_hc_grid_11_5_164 = (21.67);
    _hx_hc_grid_11_6_165 = (22.03);
    _hx_hc_grid_11_7_166 = (22.4);
    _hx_hc_grid_11_8_167 = (22.76);
    _hx_hc_grid_11_9_168 = (23.09);
    _hx_hc_grid_11_10_169 = (23.7);
    _hx_hh_grid_1_1_170 = (3.25);
    _hx_hh_grid_1_2_171 = (3.66);
    _hx_hh_grid_1_3_172 = (3.66);
    _hx_hh_grid_1_4_173 = (3.66);
    _hx_hh_grid_1_5_174 = (3.66);
    _hx_hh_grid_1_6_175 = (3.66);
    _hx_hh_grid_1_7_176 = (3.66);
    _hx_hh_grid_1_8_177 = (3.66);
    _hx_hh_grid_1_9_178 = (3.66);
    _hx_hh_grid_1_10_179 = (4.07);
    _hx_hh_grid_2_1_180 = (5.69);
    _hx_hh_grid_2_2_181 = (5.69);
    _hx_hh_grid_2_3_182 = (5.69);
    _hx_hh_grid_2_4_183 = (6.1);
    _hx_hh_grid_2_5_184 = (6.1);
    _hx_hh_grid_2_6_185 = (6.1);
    _hx_hh_grid_2_7_186 = (6.1);
    _hx_hh_grid_2_8_187 = (6.5);
    _hx_hh_grid_2_9_188 = (6.5);
    _hx_hh_grid_2_10_189 = (6.5);
    _hx_hh_grid_3_1_190 = (7.72);
    _hx_hh_grid_3_2_191 = (7.72);
    _hx_hh_grid_3_3_192 = (7.72);
    _hx_hh_grid_3_4_193 = (7.72);
    _hx_hh_grid_3_5_194 = (8.13);
    _hx_hh_grid_3_6_195 = (8.13);
    _hx_hh_grid_3_7_196 = (8.13);
    _hx_hh_grid_3_8_197 = (8.54);
    _hx_hh_grid_3_9_198 = (8.54);
    _hx_hh_grid_3_10_199 = (8.94);
    _hx_hh_grid_4_1_200 = (8.94);
    _hx_hh_grid_4_2_201 = (9.35);
    _hx_hh_grid_4_3_202 = (9.35);
    _hx_hh_grid_4_4_203 = (9.76);
    _hx_hh_grid_4_5_204 = (9.76);
    _hx_hh_grid_4_6_205 = (9.76);
    _hx_hh_grid_4_7_206 = (10.16);
    _hx_hh_grid_4_8_207 = (10.16);
    _hx_hh_grid_4_9_208 = (10.57);
    _hx_hh_grid_4_10_209 = (10.57);
    _hx_hh_grid_5_1_210 = (10.57);
    _hx_hh_grid_5_2_211 = (10.57);
    _hx_hh_grid_5_3_212 = (10.57);
    _hx_hh_grid_5_4_213 = (10.98);
    _hx_hh_grid_5_5_214 = (11.38);
    _hx_hh_grid_5_6_215 = (11.38);
    _hx_hh_grid_5_7_216 = (11.79);
    _hx_hh_grid_5_8_217 = (11.79);
    _hx_hh_grid_5_9_218 = (12.2);
    _hx_hh_grid_5_10_219 = (12.2);
    _hx_hh_grid_6_1_220 = (11.79);
    _hx_hh_grid_6_2_221 = (11.79);
    _hx_hh_grid_6_3_222 = (12.2);
    _hx_hh_grid_6_4_223 = (12.2);
    _hx_hh_grid_6_5_224 = (12.6);
    _hx_hh_grid_6_6_225 = (12.6);
    _hx_hh_grid_6_7_226 = (13.01);
    _hx_hh_grid_6_8_227 = (13.41);
    _hx_hh_grid_6_9_228 = (13.41);
    _hx_hh_grid_6_10_229 = (13.82);
    _hx_hh_grid_7_1_230 = (12.6);
    _hx_hh_grid_7_2_231 = (13.01);
    _hx_hh_grid_7_3_232 = (13.01);
    _hx_hh_grid_7_4_233 = (13.41);
    _hx_hh_grid_7_5_234 = (13.82);
    _hx_hh_grid_7_6_235 = (13.82);
    _hx_hh_grid_7_7_236 = (14.23);
    _hx_hh_grid_7_8_237 = (14.63);
    _hx_hh_grid_7_9_238 = (14.63);
    _hx_hh_grid_7_10_239 = (15.04);
    _hx_hh_grid_8_1_240 = (13.82);
    _hx_hh_grid_8_2_241 = (13.82);
    _hx_hh_grid_8_3_242 = (14.23);
    _hx_hh_grid_8_4_243 = (14.63);
    _hx_hh_grid_8_5_244 = (15.04);
    _hx_hh_grid_8_6_245 = (15.04);
    _hx_hh_grid_8_7_246 = (15.45);
    _hx_hh_grid_8_8_247 = (15.85);
    _hx_hh_grid_8_9_248 = (15.85);
    _hx_hh_grid_8_10_249 = (16.26);
    _hx_hh_grid_9_1_250 = (14.63);
    _hx_hh_grid_9_2_251 = (15.04);
    _hx_hh_grid_9_3_252 = (15.04);
    _hx_hh_grid_9_4_253 = (15.45);
    _hx_hh_grid_9_5_254 = (15.85);
    _hx_hh_grid_9_6_255 = (16.26);
    _hx_hh_grid_9_7_256 = (16.67);
    _hx_hh_grid_9_8_257 = (16.67);
    _hx_hh_grid_9_9_258 = (17.07);
    _hx_hh_grid_9_10_259 = (17.48);
    _hx_hh_grid_10_1_260 = (15.45);
    _hx_hh_grid_10_2_261 = (15.85);
    _hx_hh_grid_10_3_262 = (16.26);
    _hx_hh_grid_10_4_263 = (16.67);
    _hx_hh_grid_10_5_264 = (16.67);
    _hx_hh_grid_10_6_265 = (17.07);
    _hx_hh_grid_10_7_266 = (17.48);
    _hx_hh_grid_10_8_267 = (17.89);
    _hx_hh_grid_10_9_268 = (18.29);
    _hx_hh_grid_10_10_269 = (18.7);
    _hx_hh_grid_11_1_270 = (15.45);
    _hx_hh_grid_11_2_271 = (15.85);
    _hx_hh_grid_11_3_272 = (16.26);
    _hx_hh_grid_11_4_273 = (16.67);
    _hx_hh_grid_11_5_274 = (16.67);
    _hx_hh_grid_11_6_275 = (17.07);
    _hx_hh_grid_11_7_276 = (17.48);
    _hx_hh_grid_11_8_277 = (17.89);
    _hx_hh_grid_11_9_278 = (18.29);
    _hx_hh_grid_11_10_279 = (18.7);
    _hx_Ahx_280 = (0.00161);
    _hx_Cmetal_281 = (837.3);
    _cabin_v_289 = (141.58);
    _cabin_p_290 = (75152);
    _cabin_Qpass_291 = (90);
    _cabin_passenger_292 = (200);
    _cabin_dQ_293 = (-8792);
    _fan_wf_300 = (0.3024);
    __block_jacobian_check_341 = (JMI_FALSE);
    __block_jacobian_check_tol_342 = (1.0E-6);
    __block_solver_experimental_mode_343 = (0);
    __cs_rel_tol_344 = (1.0E-6);
    __cs_solver_345 = (0);
    __cs_step_size_346 = (0.001);
    __enforce_bounds_347 = (JMI_TRUE);
    __events_default_tol_348 = (1.0E-10);
    __events_tol_factor_349 = (1.0E-4);
    __iteration_variable_scaling_350 = (1);
    __log_level_351 = (3);
    __nle_solver_check_jac_cond_352 = (JMI_FALSE);
    __nle_solver_default_tol_353 = (1.0E-10);
    __nle_solver_max_iter_354 = (100);
    __nle_solver_max_residual_scaling_factor_355 = (1.0E10);
    __nle_solver_min_residual_scaling_factor_356 = (1.0E-10);
    __nle_solver_min_tol_357 = (1.0E-12);
    __nle_solver_regularization_tolerance_358 = (-1.0);
    __nle_solver_step_limit_factor_359 = (10.0);
    __nle_solver_tol_factor_360 = (1.0E-4);
    __rescale_after_singular_jac_361 = (JMI_TRUE);
    __rescale_each_step_362 = (JMI_FALSE);
    __residual_equation_scaling_363 = (1);
    __runtime_log_to_file_364 = (JMI_FALSE);
    __use_Brent_in_1d_365 = (JMI_TRUE);
    __use_jacobian_equilibration_366 = (JMI_FALSE);
    model_init_eval_parameters(jmi);
    _fork_outport_w_6 = (0.0);
    _fork_p_7 = (80000);
    _fork_t_8 = (350);
    _fork_m_9 = (0.0);
    _fork_Q_10 = (0.0);
    _hx_hh_283 = (0.0);
    _hx_t_284 = (250);
    _hx_Q_285 = (0.0);
    _hx_rhoh_286 = (0.0);
    _hx_qh_287 = (0.0);
    _hx_qc_288 = (0.0);
    _cabin_t_295 = (297.2);
    _cabin_m_296 = (0.0);
    _cabin_Q_297 = (0.0);
    _cabin_wa_298 = (0.0);
    _W1_303 = (0.0);
    _W31_304 = (0.0);
    _W32_305 = (0.0);
    _W33_306 = (0.0);
    _T6_310 = (288.15);
    _T8_312 = (288.15);
    _W8_313 = (0.0);
    _der_fork_m_337 = (0.0);
    _der_fork_Q_338 = (0.0);
    _der_hx_Q_339 = (0.0);
    _der_cabin_m_340 = (0.0);

    return 0;
}

const char *jmi_get_model_identifier() {
    return "AMS_AMSSim";
}


/* FMI Funcitons. */
/* FMI 2.0 functions common for both ME and CS.*/

FMI2_Export const char* fmi2GetTypesPlatform() {
    return fmi2_get_types_platform();
}

FMI2_Export const char* fmi2GetVersion() {
    return fmi2_get_version();
}

FMI2_Export fmi2Status fmi2SetDebugLogging(fmi2Component    c,
                                           fmi2Boolean      loggingOn, 
                                           size_t           nCategories, 
                                           const fmi2String categories[]) {
    return fmi2_set_debug_logging(c, loggingOn, nCategories, categories);
}

FMI2_Export fmi2Component fmi2Instantiate(fmi2String instanceName,
                                          fmi2Type   fmuType,
                                          fmi2String fmuGUID,
                                          fmi2String fmuResourceLocation,
                                          const fmi2CallbackFunctions* functions,
                                          fmi2Boolean                 visible,
                                          fmi2Boolean                 loggingOn) {
    if (!can_instantiate(fmuType, instanceName, functions))
        return NULL;

    return fmi2_instantiate(instanceName, fmuType, fmuGUID, fmuResourceLocation,
                            functions, visible, loggingOn);
}

FMI2_Export void fmi2FreeInstance(fmi2Component c) {
    fmi2_free_instance(c);
}

FMI2_Export fmi2Status fmi2SetupExperiment(fmi2Component c, 
                                           fmi2Boolean   toleranceDefined, 
                                           fmi2Real      tolerance, 
                                           fmi2Real      startTime, 
                                           fmi2Boolean   stopTimeDefined, 
                                           fmi2Real      stopTime) {
    return fmi2_setup_experiment(c, toleranceDefined, tolerance, startTime,
                                 stopTimeDefined, stopTime);
}

FMI2_Export fmi2Status fmi2EnterInitializationMode(fmi2Component c) {
    return fmi2_enter_initialization_mode(c);
}

FMI2_Export fmi2Status fmi2ExitInitializationMode(fmi2Component c) {
    return fmi2_exit_initialization_mode(c);
}

FMI2_Export fmi2Status fmi2Terminate(fmi2Component c) {
    return fmi2_terminate(c);
}

FMI2_Export fmi2Status fmi2Reset(fmi2Component c) {
    return fmi2_reset(c);
}

FMI2_Export fmi2Status fmi2GetReal(fmi2Component c, const fmi2ValueReference vr[],
                                   size_t nvr, fmi2Real value[]) {
    return fmi2_get_real(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2GetInteger(fmi2Component c, const fmi2ValueReference vr[],
                                      size_t nvr, fmi2Integer value[]) {
    return fmi2_get_integer(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2GetBoolean(fmi2Component c, const fmi2ValueReference vr[],
                                      size_t nvr, fmi2Boolean value[]) {
    return fmi2_get_boolean(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2GetString(fmi2Component c, const fmi2ValueReference vr[],
                                     size_t nvr, fmi2String value[]) {
    return fmi2_get_string(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2SetReal(fmi2Component c, const fmi2ValueReference vr[],
                                   size_t nvr, const fmi2Real value[]) {
    return fmi2_set_real(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2SetInteger(fmi2Component c, const fmi2ValueReference vr[],
                                      size_t nvr, const fmi2Integer value[]) {
    return fmi2_set_integer(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2SetBoolean(fmi2Component c, const fmi2ValueReference vr[],
                                      size_t nvr, const fmi2Boolean value[]) {
    return fmi2_set_boolean(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2SetString(fmi2Component c, const fmi2ValueReference vr[],
                                     size_t nvr, const fmi2String value[]) {
    return fmi2_set_string(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2GetFMUstate(fmi2Component c, fmi2FMUstate* FMUstate) {
    return fmi2_get_fmu_state(c, FMUstate);
}

FMI2_Export fmi2Status fmi2SetFMUstate(fmi2Component c, fmi2FMUstate FMUstate) {
    return fmi2_set_fmu_state(c, FMUstate);
}

FMI2_Export fmi2Status fmi2FreeFMUstate(fmi2Component c, fmi2FMUstate* FMUstate) {
    return fmi2_free_fmu_state(c, FMUstate);
}

FMI2_Export fmi2Status fmi2SerializedFMUstateSize(fmi2Component c, fmi2FMUstate FMUstate,
                                                  size_t* size) {
    return fmi2_serialized_fmu_state_size(c, FMUstate, size);
}

FMI2_Export fmi2Status fmi2SerializedFMUstate(fmi2Component c, fmi2FMUstate FMUstate,
                                  fmi2Byte serializedState[], size_t size) {
    return fmi2_serialized_fmu_state(c, FMUstate, serializedState, size);
}

FMI2_Export fmi2Status fmi2DeSerializedFMUstate(fmi2Component c,
                                  const fmi2Byte serializedState[],
                                  size_t size, fmi2FMUstate* FMUstate) {
    return fmi2_de_serialized_fmu_state(c, serializedState, size, FMUstate);
}

FMI2_Export fmi2Status fmi2GetDirectionalDerivative(fmi2Component c,
                 const fmi2ValueReference vUnknown_ref[], size_t nUnknown,
                 const fmi2ValueReference vKnown_ref[],   size_t nKnown,
                 const fmi2Real dvKnown[], fmi2Real dvUnknown[]) {
	return fmi2_get_directional_derivative(c, vUnknown_ref, nUnknown,
                                           vKnown_ref, nKnown, dvKnown, dvUnknown);
}

#ifdef FMUME20
/* FMI 2.0 functions specific for ME.*/

FMI2_Export fmi2Status fmi2EnterEventMode(fmi2Component c) {
	return fmi2_enter_event_mode(c);
}

FMI2_Export fmi2Status fmi2NewDiscreteStates(fmi2Component  c,
                                            fmi2EventInfo* fmiEventInfo) {
	return fmi2_new_discrete_state(c, fmiEventInfo);
}

FMI2_Export fmi2Status fmi2EnterContinuousTimeMode(fmi2Component c) {
	return fmi2_enter_continuous_time_mode(c);
}

FMI2_Export fmi2Status fmi2CompletedIntegratorStep(fmi2Component c,
                                                   fmi2Boolean   noSetFMUStatePriorToCurrentPoint, 
                                                   fmi2Boolean*  enterEventMode, 
                                                   fmi2Boolean*   terminateSimulation) {
	return fmi2_completed_integrator_step(c, noSetFMUStatePriorToCurrentPoint,
                                          enterEventMode, terminateSimulation);
}

FMI2_Export fmi2Status fmi2SetTime(fmi2Component c, fmi2Real time) {
	return fmi2_set_time(c, time);
}

FMI2_Export fmi2Status fmi2SetContinuousStates(fmi2Component c, const fmi2Real x[],
                                               size_t nx) {
	return fmi2_set_continuous_states(c, x, nx);
}

FMI2_Export fmi2Status fmi2GetDerivatives(fmi2Component c, fmi2Real derivatives[],
                                          size_t nx) {
	return fmi2_get_derivatives(c, derivatives, nx);
}

FMI2_Export fmi2Status fmi2GetEventIndicators(fmi2Component c, 
                                              fmi2Real eventIndicators[], size_t ni) {
	return fmi2_get_event_indicators(c, eventIndicators, ni);
}

FMI2_Export fmi2Status fmi2GetContinuousStates(fmi2Component c, fmi2Real x[],
                                               size_t nx) {
	return fmi2_get_continuous_states(c, x, nx);
}

FMI2_Export fmi2Status fmi2GetNominalsOfContinuousStates(fmi2Component c, 
                                                         fmi2Real x_nominal[], 
                                                         size_t nx) {
	return fmi2_get_nominals_of_continuous_states(c, x_nominal, nx);
}

#endif
#ifdef FMUCS20
/* FMI 2.0 functions specific for CS.*/

FMI2_Export fmi2Status fmi2SetRealInputDerivatives(fmi2Component c, 
                                                   const fmi2ValueReference vr[],
                                                   size_t nvr, const fmi2Integer order[],
                                                   const fmi2Real value[]) {
	return fmi2_set_real_input_derivatives(c, vr, nvr, order, value);
}

FMI2_Export fmi2Status fmi2GetRealOutputDerivatives(fmi2Component c,
                                                    const fmi2ValueReference vr[],
                                                    size_t nvr, const fmi2Integer order[],
                                                    fmi2Real value[]) {
	return fmi2_get_real_output_derivatives(c, vr, nvr, order, value);
}

FMI2_Export fmi2Status fmi2DoStep(fmi2Component c, fmi2Real currentCommunicationPoint,
                                  fmi2Real    communicationStepSize,
                                  fmi2Boolean noSetFMUStatePriorToCurrentPoint) {
	return fmi2_do_step(c, currentCommunicationPoint, communicationStepSize,
                        noSetFMUStatePriorToCurrentPoint);
}

FMI2_Export fmi2Status fmi2CancelStep(fmi2Component c) {
	return fmi2_cancel_step(c);
}

FMI2_Export fmi2Status fmi2GetStatus(fmi2Component c, const fmi2StatusKind s,
                                     fmi2Status* value) {
	return fmi2_get_status(c, s, value);
}

FMI2_Export fmi2Status fmi2GetRealStatus(fmi2Component c, const fmi2StatusKind s,
                                         fmi2Real* value) {
	return fmi2_get_real_status(c, s, value);
}

FMI2_Export fmi2Status fmi2GetIntegerStatus(fmi2Component c, const fmi2StatusKind s,
                                            fmi2Integer* values) {
	return fmi2_get_integer_status(c, s, values);
}

FMI2_Export fmi2Status fmi2GetBooleanStatus(fmi2Component c, const fmi2StatusKind s,
                                            fmi2Boolean* value) {
	return fmi2_get_boolean_status(c, s, value);
}

FMI2_Export fmi2Status fmi2GetStringStatus(fmi2Component c, const fmi2StatusKind s,
                                           fmi2String* value) {
	return fmi2_get_string_status(c, s, value);
	
}

#endif

/* Helper function for instantiating the FMU. */
int can_instantiate(fmi2Type fmuType, fmi2String instanceName,
                    const fmi2CallbackFunctions* functions) {
    if (fmuType == fmi2CoSimulation) {
#ifndef FMUCS20
        functions->logger(0, instanceName, fmi2Error, "ERROR", "The model is not compiled as a Co-Simulation FMU.");
        return 0;
#endif
    } else if (fmuType == fmi2ModelExchange) {
#ifndef FMUME20
        functions->logger(0, instanceName, fmi2Error, "ERROR", "The model is not compiled as a Model Exchange FMU.");
        return 0;
#endif
    }
    return 1;
}
