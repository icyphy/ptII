#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "fmiCSFunctions.h"
#include "fmi1_functions.h"
#include <jmi.h>
#include <jmi_block_residual.h>
#include <fmi1_me.h>
#include <fmi1_cs.h>
#include "jmi_log.h"
#include "ModelicaUtilities.h"
#include "ModelicaStandardTables.h"



#define MODEL_IDENTIFIER AMS_AMSSim
const char *C_GUID = "65b6f6bd39be2fc146c5086c655699d3";
//#define C_GUID "65b6f6bd39be2fc146c5086c655699d3"

static int model_ode_guards_init(jmi_t* jmi);
static int model_init_R0(jmi_t* jmi, jmi_real_t** res);
static int model_ode_initialize(jmi_t* jmi);

static const int N_real_ci = 3;
static const int N_real_cd = 0;
static const int N_real_pi = 283;
static const int N_real_pd = 1;

static const int N_integer_ci = 0 + 0;
static const int N_integer_cd = 0 + 0;
static const int N_integer_pi = 8 + 0;
static const int N_integer_pd = 0 + 0;

static const int N_boolean_ci = 0;
static const int N_boolean_cd = 0;
static const int N_boolean_pi = 8;
static const int N_boolean_pd = 0;

static const int N_string_ci = 0;
static const int N_string_cd = 0;
static const int N_string_pi = 0;
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

static const int N_sw = 0;
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
static const int DAE_initial_relations[1]= {-1};

static const int N_relations = 0;
static const int DAE_relations[1]= {-1};

#define _valve1_C_2 ((*(jmi->z))[jmi->offs_real_ci+0])
#define _valve2_C_9 ((*(jmi->z))[jmi->offs_real_ci+1])
#define _W4_298 ((*(jmi->z))[jmi->offs_real_ci+2])
#define _inlet_p_env_0 ((*(jmi->z))[jmi->offs_real_pi+0])
#define _inlet_t_env_1 ((*(jmi->z))[jmi->offs_real_pi+1])
#define _fork_v_3 ((*(jmi->z))[jmi->offs_real_pi+2])
#define _ambientair_p_env_10 ((*(jmi->z))[jmi->offs_real_pi+3])
#define _ambientair_t_env_11 ((*(jmi->z))[jmi->offs_real_pi+4])
#define _hx_m_12 ((*(jmi->z))[jmi->offs_real_pi+5])
#define _hx_wc_grid_1_13 ((*(jmi->z))[jmi->offs_real_pi+6])
#define _hx_wc_grid_2_14 ((*(jmi->z))[jmi->offs_real_pi+7])
#define _hx_wc_grid_3_15 ((*(jmi->z))[jmi->offs_real_pi+8])
#define _hx_wc_grid_4_16 ((*(jmi->z))[jmi->offs_real_pi+9])
#define _hx_wc_grid_5_17 ((*(jmi->z))[jmi->offs_real_pi+10])
#define _hx_wc_grid_6_18 ((*(jmi->z))[jmi->offs_real_pi+11])
#define _hx_wc_grid_7_19 ((*(jmi->z))[jmi->offs_real_pi+12])
#define _hx_wc_grid_8_20 ((*(jmi->z))[jmi->offs_real_pi+13])
#define _hx_wc_grid_9_21 ((*(jmi->z))[jmi->offs_real_pi+14])
#define _hx_wc_grid_10_22 ((*(jmi->z))[jmi->offs_real_pi+15])
#define _hx_wc_grid_11_23 ((*(jmi->z))[jmi->offs_real_pi+16])
#define _hx_wh_grid_1_24 ((*(jmi->z))[jmi->offs_real_pi+17])
#define _hx_wh_grid_2_25 ((*(jmi->z))[jmi->offs_real_pi+18])
#define _hx_wh_grid_3_26 ((*(jmi->z))[jmi->offs_real_pi+19])
#define _hx_wh_grid_4_27 ((*(jmi->z))[jmi->offs_real_pi+20])
#define _hx_wh_grid_5_28 ((*(jmi->z))[jmi->offs_real_pi+21])
#define _hx_wh_grid_6_29 ((*(jmi->z))[jmi->offs_real_pi+22])
#define _hx_wh_grid_7_30 ((*(jmi->z))[jmi->offs_real_pi+23])
#define _hx_wh_grid_8_31 ((*(jmi->z))[jmi->offs_real_pi+24])
#define _hx_wh_grid_9_32 ((*(jmi->z))[jmi->offs_real_pi+25])
#define _hx_wh_grid_10_33 ((*(jmi->z))[jmi->offs_real_pi+26])
#define _hx_wh_grid_11_34 ((*(jmi->z))[jmi->offs_real_pi+27])
#define _hx_th_grid_1_35 ((*(jmi->z))[jmi->offs_real_pi+28])
#define _hx_th_grid_2_36 ((*(jmi->z))[jmi->offs_real_pi+29])
#define _hx_th_grid_3_37 ((*(jmi->z))[jmi->offs_real_pi+30])
#define _hx_th_grid_4_38 ((*(jmi->z))[jmi->offs_real_pi+31])
#define _hx_th_grid_5_39 ((*(jmi->z))[jmi->offs_real_pi+32])
#define _hx_th_grid_6_40 ((*(jmi->z))[jmi->offs_real_pi+33])
#define _hx_th_grid_7_41 ((*(jmi->z))[jmi->offs_real_pi+34])
#define _hx_th_grid_8_42 ((*(jmi->z))[jmi->offs_real_pi+35])
#define _hx_th_grid_9_43 ((*(jmi->z))[jmi->offs_real_pi+36])
#define _hx_th_grid_10_44 ((*(jmi->z))[jmi->offs_real_pi+37])
#define _hx_tc_grid_1_45 ((*(jmi->z))[jmi->offs_real_pi+38])
#define _hx_tc_grid_2_46 ((*(jmi->z))[jmi->offs_real_pi+39])
#define _hx_tc_grid_3_47 ((*(jmi->z))[jmi->offs_real_pi+40])
#define _hx_tc_grid_4_48 ((*(jmi->z))[jmi->offs_real_pi+41])
#define _hx_tc_grid_5_49 ((*(jmi->z))[jmi->offs_real_pi+42])
#define _hx_tc_grid_6_50 ((*(jmi->z))[jmi->offs_real_pi+43])
#define _hx_tc_grid_7_51 ((*(jmi->z))[jmi->offs_real_pi+44])
#define _hx_tc_grid_8_52 ((*(jmi->z))[jmi->offs_real_pi+45])
#define _hx_tc_grid_9_53 ((*(jmi->z))[jmi->offs_real_pi+46])
#define _hx_tc_grid_10_54 ((*(jmi->z))[jmi->offs_real_pi+47])
#define _hx_hc_grid_1_1_55 ((*(jmi->z))[jmi->offs_real_pi+48])
#define _hx_hc_grid_1_2_56 ((*(jmi->z))[jmi->offs_real_pi+49])
#define _hx_hc_grid_1_3_57 ((*(jmi->z))[jmi->offs_real_pi+50])
#define _hx_hc_grid_1_4_58 ((*(jmi->z))[jmi->offs_real_pi+51])
#define _hx_hc_grid_1_5_59 ((*(jmi->z))[jmi->offs_real_pi+52])
#define _hx_hc_grid_1_6_60 ((*(jmi->z))[jmi->offs_real_pi+53])
#define _hx_hc_grid_1_7_61 ((*(jmi->z))[jmi->offs_real_pi+54])
#define _hx_hc_grid_1_8_62 ((*(jmi->z))[jmi->offs_real_pi+55])
#define _hx_hc_grid_1_9_63 ((*(jmi->z))[jmi->offs_real_pi+56])
#define _hx_hc_grid_1_10_64 ((*(jmi->z))[jmi->offs_real_pi+57])
#define _hx_hc_grid_2_1_65 ((*(jmi->z))[jmi->offs_real_pi+58])
#define _hx_hc_grid_2_2_66 ((*(jmi->z))[jmi->offs_real_pi+59])
#define _hx_hc_grid_2_3_67 ((*(jmi->z))[jmi->offs_real_pi+60])
#define _hx_hc_grid_2_4_68 ((*(jmi->z))[jmi->offs_real_pi+61])
#define _hx_hc_grid_2_5_69 ((*(jmi->z))[jmi->offs_real_pi+62])
#define _hx_hc_grid_2_6_70 ((*(jmi->z))[jmi->offs_real_pi+63])
#define _hx_hc_grid_2_7_71 ((*(jmi->z))[jmi->offs_real_pi+64])
#define _hx_hc_grid_2_8_72 ((*(jmi->z))[jmi->offs_real_pi+65])
#define _hx_hc_grid_2_9_73 ((*(jmi->z))[jmi->offs_real_pi+66])
#define _hx_hc_grid_2_10_74 ((*(jmi->z))[jmi->offs_real_pi+67])
#define _hx_hc_grid_3_1_75 ((*(jmi->z))[jmi->offs_real_pi+68])
#define _hx_hc_grid_3_2_76 ((*(jmi->z))[jmi->offs_real_pi+69])
#define _hx_hc_grid_3_3_77 ((*(jmi->z))[jmi->offs_real_pi+70])
#define _hx_hc_grid_3_4_78 ((*(jmi->z))[jmi->offs_real_pi+71])
#define _hx_hc_grid_3_5_79 ((*(jmi->z))[jmi->offs_real_pi+72])
#define _hx_hc_grid_3_6_80 ((*(jmi->z))[jmi->offs_real_pi+73])
#define _hx_hc_grid_3_7_81 ((*(jmi->z))[jmi->offs_real_pi+74])
#define _hx_hc_grid_3_8_82 ((*(jmi->z))[jmi->offs_real_pi+75])
#define _hx_hc_grid_3_9_83 ((*(jmi->z))[jmi->offs_real_pi+76])
#define _hx_hc_grid_3_10_84 ((*(jmi->z))[jmi->offs_real_pi+77])
#define _hx_hc_grid_4_1_85 ((*(jmi->z))[jmi->offs_real_pi+78])
#define _hx_hc_grid_4_2_86 ((*(jmi->z))[jmi->offs_real_pi+79])
#define _hx_hc_grid_4_3_87 ((*(jmi->z))[jmi->offs_real_pi+80])
#define _hx_hc_grid_4_4_88 ((*(jmi->z))[jmi->offs_real_pi+81])
#define _hx_hc_grid_4_5_89 ((*(jmi->z))[jmi->offs_real_pi+82])
#define _hx_hc_grid_4_6_90 ((*(jmi->z))[jmi->offs_real_pi+83])
#define _hx_hc_grid_4_7_91 ((*(jmi->z))[jmi->offs_real_pi+84])
#define _hx_hc_grid_4_8_92 ((*(jmi->z))[jmi->offs_real_pi+85])
#define _hx_hc_grid_4_9_93 ((*(jmi->z))[jmi->offs_real_pi+86])
#define _hx_hc_grid_4_10_94 ((*(jmi->z))[jmi->offs_real_pi+87])
#define _hx_hc_grid_5_1_95 ((*(jmi->z))[jmi->offs_real_pi+88])
#define _hx_hc_grid_5_2_96 ((*(jmi->z))[jmi->offs_real_pi+89])
#define _hx_hc_grid_5_3_97 ((*(jmi->z))[jmi->offs_real_pi+90])
#define _hx_hc_grid_5_4_98 ((*(jmi->z))[jmi->offs_real_pi+91])
#define _hx_hc_grid_5_5_99 ((*(jmi->z))[jmi->offs_real_pi+92])
#define _hx_hc_grid_5_6_100 ((*(jmi->z))[jmi->offs_real_pi+93])
#define _hx_hc_grid_5_7_101 ((*(jmi->z))[jmi->offs_real_pi+94])
#define _hx_hc_grid_5_8_102 ((*(jmi->z))[jmi->offs_real_pi+95])
#define _hx_hc_grid_5_9_103 ((*(jmi->z))[jmi->offs_real_pi+96])
#define _hx_hc_grid_5_10_104 ((*(jmi->z))[jmi->offs_real_pi+97])
#define _hx_hc_grid_6_1_105 ((*(jmi->z))[jmi->offs_real_pi+98])
#define _hx_hc_grid_6_2_106 ((*(jmi->z))[jmi->offs_real_pi+99])
#define _hx_hc_grid_6_3_107 ((*(jmi->z))[jmi->offs_real_pi+100])
#define _hx_hc_grid_6_4_108 ((*(jmi->z))[jmi->offs_real_pi+101])
#define _hx_hc_grid_6_5_109 ((*(jmi->z))[jmi->offs_real_pi+102])
#define _hx_hc_grid_6_6_110 ((*(jmi->z))[jmi->offs_real_pi+103])
#define _hx_hc_grid_6_7_111 ((*(jmi->z))[jmi->offs_real_pi+104])
#define _hx_hc_grid_6_8_112 ((*(jmi->z))[jmi->offs_real_pi+105])
#define _hx_hc_grid_6_9_113 ((*(jmi->z))[jmi->offs_real_pi+106])
#define _hx_hc_grid_6_10_114 ((*(jmi->z))[jmi->offs_real_pi+107])
#define _hx_hc_grid_7_1_115 ((*(jmi->z))[jmi->offs_real_pi+108])
#define _hx_hc_grid_7_2_116 ((*(jmi->z))[jmi->offs_real_pi+109])
#define _hx_hc_grid_7_3_117 ((*(jmi->z))[jmi->offs_real_pi+110])
#define _hx_hc_grid_7_4_118 ((*(jmi->z))[jmi->offs_real_pi+111])
#define _hx_hc_grid_7_5_119 ((*(jmi->z))[jmi->offs_real_pi+112])
#define _hx_hc_grid_7_6_120 ((*(jmi->z))[jmi->offs_real_pi+113])
#define _hx_hc_grid_7_7_121 ((*(jmi->z))[jmi->offs_real_pi+114])
#define _hx_hc_grid_7_8_122 ((*(jmi->z))[jmi->offs_real_pi+115])
#define _hx_hc_grid_7_9_123 ((*(jmi->z))[jmi->offs_real_pi+116])
#define _hx_hc_grid_7_10_124 ((*(jmi->z))[jmi->offs_real_pi+117])
#define _hx_hc_grid_8_1_125 ((*(jmi->z))[jmi->offs_real_pi+118])
#define _hx_hc_grid_8_2_126 ((*(jmi->z))[jmi->offs_real_pi+119])
#define _hx_hc_grid_8_3_127 ((*(jmi->z))[jmi->offs_real_pi+120])
#define _hx_hc_grid_8_4_128 ((*(jmi->z))[jmi->offs_real_pi+121])
#define _hx_hc_grid_8_5_129 ((*(jmi->z))[jmi->offs_real_pi+122])
#define _hx_hc_grid_8_6_130 ((*(jmi->z))[jmi->offs_real_pi+123])
#define _hx_hc_grid_8_7_131 ((*(jmi->z))[jmi->offs_real_pi+124])
#define _hx_hc_grid_8_8_132 ((*(jmi->z))[jmi->offs_real_pi+125])
#define _hx_hc_grid_8_9_133 ((*(jmi->z))[jmi->offs_real_pi+126])
#define _hx_hc_grid_8_10_134 ((*(jmi->z))[jmi->offs_real_pi+127])
#define _hx_hc_grid_9_1_135 ((*(jmi->z))[jmi->offs_real_pi+128])
#define _hx_hc_grid_9_2_136 ((*(jmi->z))[jmi->offs_real_pi+129])
#define _hx_hc_grid_9_3_137 ((*(jmi->z))[jmi->offs_real_pi+130])
#define _hx_hc_grid_9_4_138 ((*(jmi->z))[jmi->offs_real_pi+131])
#define _hx_hc_grid_9_5_139 ((*(jmi->z))[jmi->offs_real_pi+132])
#define _hx_hc_grid_9_6_140 ((*(jmi->z))[jmi->offs_real_pi+133])
#define _hx_hc_grid_9_7_141 ((*(jmi->z))[jmi->offs_real_pi+134])
#define _hx_hc_grid_9_8_142 ((*(jmi->z))[jmi->offs_real_pi+135])
#define _hx_hc_grid_9_9_143 ((*(jmi->z))[jmi->offs_real_pi+136])
#define _hx_hc_grid_9_10_144 ((*(jmi->z))[jmi->offs_real_pi+137])
#define _hx_hc_grid_10_1_145 ((*(jmi->z))[jmi->offs_real_pi+138])
#define _hx_hc_grid_10_2_146 ((*(jmi->z))[jmi->offs_real_pi+139])
#define _hx_hc_grid_10_3_147 ((*(jmi->z))[jmi->offs_real_pi+140])
#define _hx_hc_grid_10_4_148 ((*(jmi->z))[jmi->offs_real_pi+141])
#define _hx_hc_grid_10_5_149 ((*(jmi->z))[jmi->offs_real_pi+142])
#define _hx_hc_grid_10_6_150 ((*(jmi->z))[jmi->offs_real_pi+143])
#define _hx_hc_grid_10_7_151 ((*(jmi->z))[jmi->offs_real_pi+144])
#define _hx_hc_grid_10_8_152 ((*(jmi->z))[jmi->offs_real_pi+145])
#define _hx_hc_grid_10_9_153 ((*(jmi->z))[jmi->offs_real_pi+146])
#define _hx_hc_grid_10_10_154 ((*(jmi->z))[jmi->offs_real_pi+147])
#define _hx_hc_grid_11_1_155 ((*(jmi->z))[jmi->offs_real_pi+148])
#define _hx_hc_grid_11_2_156 ((*(jmi->z))[jmi->offs_real_pi+149])
#define _hx_hc_grid_11_3_157 ((*(jmi->z))[jmi->offs_real_pi+150])
#define _hx_hc_grid_11_4_158 ((*(jmi->z))[jmi->offs_real_pi+151])
#define _hx_hc_grid_11_5_159 ((*(jmi->z))[jmi->offs_real_pi+152])
#define _hx_hc_grid_11_6_160 ((*(jmi->z))[jmi->offs_real_pi+153])
#define _hx_hc_grid_11_7_161 ((*(jmi->z))[jmi->offs_real_pi+154])
#define _hx_hc_grid_11_8_162 ((*(jmi->z))[jmi->offs_real_pi+155])
#define _hx_hc_grid_11_9_163 ((*(jmi->z))[jmi->offs_real_pi+156])
#define _hx_hc_grid_11_10_164 ((*(jmi->z))[jmi->offs_real_pi+157])
#define _hx_hh_grid_1_1_165 ((*(jmi->z))[jmi->offs_real_pi+158])
#define _hx_hh_grid_1_2_166 ((*(jmi->z))[jmi->offs_real_pi+159])
#define _hx_hh_grid_1_3_167 ((*(jmi->z))[jmi->offs_real_pi+160])
#define _hx_hh_grid_1_4_168 ((*(jmi->z))[jmi->offs_real_pi+161])
#define _hx_hh_grid_1_5_169 ((*(jmi->z))[jmi->offs_real_pi+162])
#define _hx_hh_grid_1_6_170 ((*(jmi->z))[jmi->offs_real_pi+163])
#define _hx_hh_grid_1_7_171 ((*(jmi->z))[jmi->offs_real_pi+164])
#define _hx_hh_grid_1_8_172 ((*(jmi->z))[jmi->offs_real_pi+165])
#define _hx_hh_grid_1_9_173 ((*(jmi->z))[jmi->offs_real_pi+166])
#define _hx_hh_grid_1_10_174 ((*(jmi->z))[jmi->offs_real_pi+167])
#define _hx_hh_grid_2_1_175 ((*(jmi->z))[jmi->offs_real_pi+168])
#define _hx_hh_grid_2_2_176 ((*(jmi->z))[jmi->offs_real_pi+169])
#define _hx_hh_grid_2_3_177 ((*(jmi->z))[jmi->offs_real_pi+170])
#define _hx_hh_grid_2_4_178 ((*(jmi->z))[jmi->offs_real_pi+171])
#define _hx_hh_grid_2_5_179 ((*(jmi->z))[jmi->offs_real_pi+172])
#define _hx_hh_grid_2_6_180 ((*(jmi->z))[jmi->offs_real_pi+173])
#define _hx_hh_grid_2_7_181 ((*(jmi->z))[jmi->offs_real_pi+174])
#define _hx_hh_grid_2_8_182 ((*(jmi->z))[jmi->offs_real_pi+175])
#define _hx_hh_grid_2_9_183 ((*(jmi->z))[jmi->offs_real_pi+176])
#define _hx_hh_grid_2_10_184 ((*(jmi->z))[jmi->offs_real_pi+177])
#define _hx_hh_grid_3_1_185 ((*(jmi->z))[jmi->offs_real_pi+178])
#define _hx_hh_grid_3_2_186 ((*(jmi->z))[jmi->offs_real_pi+179])
#define _hx_hh_grid_3_3_187 ((*(jmi->z))[jmi->offs_real_pi+180])
#define _hx_hh_grid_3_4_188 ((*(jmi->z))[jmi->offs_real_pi+181])
#define _hx_hh_grid_3_5_189 ((*(jmi->z))[jmi->offs_real_pi+182])
#define _hx_hh_grid_3_6_190 ((*(jmi->z))[jmi->offs_real_pi+183])
#define _hx_hh_grid_3_7_191 ((*(jmi->z))[jmi->offs_real_pi+184])
#define _hx_hh_grid_3_8_192 ((*(jmi->z))[jmi->offs_real_pi+185])
#define _hx_hh_grid_3_9_193 ((*(jmi->z))[jmi->offs_real_pi+186])
#define _hx_hh_grid_3_10_194 ((*(jmi->z))[jmi->offs_real_pi+187])
#define _hx_hh_grid_4_1_195 ((*(jmi->z))[jmi->offs_real_pi+188])
#define _hx_hh_grid_4_2_196 ((*(jmi->z))[jmi->offs_real_pi+189])
#define _hx_hh_grid_4_3_197 ((*(jmi->z))[jmi->offs_real_pi+190])
#define _hx_hh_grid_4_4_198 ((*(jmi->z))[jmi->offs_real_pi+191])
#define _hx_hh_grid_4_5_199 ((*(jmi->z))[jmi->offs_real_pi+192])
#define _hx_hh_grid_4_6_200 ((*(jmi->z))[jmi->offs_real_pi+193])
#define _hx_hh_grid_4_7_201 ((*(jmi->z))[jmi->offs_real_pi+194])
#define _hx_hh_grid_4_8_202 ((*(jmi->z))[jmi->offs_real_pi+195])
#define _hx_hh_grid_4_9_203 ((*(jmi->z))[jmi->offs_real_pi+196])
#define _hx_hh_grid_4_10_204 ((*(jmi->z))[jmi->offs_real_pi+197])
#define _hx_hh_grid_5_1_205 ((*(jmi->z))[jmi->offs_real_pi+198])
#define _hx_hh_grid_5_2_206 ((*(jmi->z))[jmi->offs_real_pi+199])
#define _hx_hh_grid_5_3_207 ((*(jmi->z))[jmi->offs_real_pi+200])
#define _hx_hh_grid_5_4_208 ((*(jmi->z))[jmi->offs_real_pi+201])
#define _hx_hh_grid_5_5_209 ((*(jmi->z))[jmi->offs_real_pi+202])
#define _hx_hh_grid_5_6_210 ((*(jmi->z))[jmi->offs_real_pi+203])
#define _hx_hh_grid_5_7_211 ((*(jmi->z))[jmi->offs_real_pi+204])
#define _hx_hh_grid_5_8_212 ((*(jmi->z))[jmi->offs_real_pi+205])
#define _hx_hh_grid_5_9_213 ((*(jmi->z))[jmi->offs_real_pi+206])
#define _hx_hh_grid_5_10_214 ((*(jmi->z))[jmi->offs_real_pi+207])
#define _hx_hh_grid_6_1_215 ((*(jmi->z))[jmi->offs_real_pi+208])
#define _hx_hh_grid_6_2_216 ((*(jmi->z))[jmi->offs_real_pi+209])
#define _hx_hh_grid_6_3_217 ((*(jmi->z))[jmi->offs_real_pi+210])
#define _hx_hh_grid_6_4_218 ((*(jmi->z))[jmi->offs_real_pi+211])
#define _hx_hh_grid_6_5_219 ((*(jmi->z))[jmi->offs_real_pi+212])
#define _hx_hh_grid_6_6_220 ((*(jmi->z))[jmi->offs_real_pi+213])
#define _hx_hh_grid_6_7_221 ((*(jmi->z))[jmi->offs_real_pi+214])
#define _hx_hh_grid_6_8_222 ((*(jmi->z))[jmi->offs_real_pi+215])
#define _hx_hh_grid_6_9_223 ((*(jmi->z))[jmi->offs_real_pi+216])
#define _hx_hh_grid_6_10_224 ((*(jmi->z))[jmi->offs_real_pi+217])
#define _hx_hh_grid_7_1_225 ((*(jmi->z))[jmi->offs_real_pi+218])
#define _hx_hh_grid_7_2_226 ((*(jmi->z))[jmi->offs_real_pi+219])
#define _hx_hh_grid_7_3_227 ((*(jmi->z))[jmi->offs_real_pi+220])
#define _hx_hh_grid_7_4_228 ((*(jmi->z))[jmi->offs_real_pi+221])
#define _hx_hh_grid_7_5_229 ((*(jmi->z))[jmi->offs_real_pi+222])
#define _hx_hh_grid_7_6_230 ((*(jmi->z))[jmi->offs_real_pi+223])
#define _hx_hh_grid_7_7_231 ((*(jmi->z))[jmi->offs_real_pi+224])
#define _hx_hh_grid_7_8_232 ((*(jmi->z))[jmi->offs_real_pi+225])
#define _hx_hh_grid_7_9_233 ((*(jmi->z))[jmi->offs_real_pi+226])
#define _hx_hh_grid_7_10_234 ((*(jmi->z))[jmi->offs_real_pi+227])
#define _hx_hh_grid_8_1_235 ((*(jmi->z))[jmi->offs_real_pi+228])
#define _hx_hh_grid_8_2_236 ((*(jmi->z))[jmi->offs_real_pi+229])
#define _hx_hh_grid_8_3_237 ((*(jmi->z))[jmi->offs_real_pi+230])
#define _hx_hh_grid_8_4_238 ((*(jmi->z))[jmi->offs_real_pi+231])
#define _hx_hh_grid_8_5_239 ((*(jmi->z))[jmi->offs_real_pi+232])
#define _hx_hh_grid_8_6_240 ((*(jmi->z))[jmi->offs_real_pi+233])
#define _hx_hh_grid_8_7_241 ((*(jmi->z))[jmi->offs_real_pi+234])
#define _hx_hh_grid_8_8_242 ((*(jmi->z))[jmi->offs_real_pi+235])
#define _hx_hh_grid_8_9_243 ((*(jmi->z))[jmi->offs_real_pi+236])
#define _hx_hh_grid_8_10_244 ((*(jmi->z))[jmi->offs_real_pi+237])
#define _hx_hh_grid_9_1_245 ((*(jmi->z))[jmi->offs_real_pi+238])
#define _hx_hh_grid_9_2_246 ((*(jmi->z))[jmi->offs_real_pi+239])
#define _hx_hh_grid_9_3_247 ((*(jmi->z))[jmi->offs_real_pi+240])
#define _hx_hh_grid_9_4_248 ((*(jmi->z))[jmi->offs_real_pi+241])
#define _hx_hh_grid_9_5_249 ((*(jmi->z))[jmi->offs_real_pi+242])
#define _hx_hh_grid_9_6_250 ((*(jmi->z))[jmi->offs_real_pi+243])
#define _hx_hh_grid_9_7_251 ((*(jmi->z))[jmi->offs_real_pi+244])
#define _hx_hh_grid_9_8_252 ((*(jmi->z))[jmi->offs_real_pi+245])
#define _hx_hh_grid_9_9_253 ((*(jmi->z))[jmi->offs_real_pi+246])
#define _hx_hh_grid_9_10_254 ((*(jmi->z))[jmi->offs_real_pi+247])
#define _hx_hh_grid_10_1_255 ((*(jmi->z))[jmi->offs_real_pi+248])
#define _hx_hh_grid_10_2_256 ((*(jmi->z))[jmi->offs_real_pi+249])
#define _hx_hh_grid_10_3_257 ((*(jmi->z))[jmi->offs_real_pi+250])
#define _hx_hh_grid_10_4_258 ((*(jmi->z))[jmi->offs_real_pi+251])
#define _hx_hh_grid_10_5_259 ((*(jmi->z))[jmi->offs_real_pi+252])
#define _hx_hh_grid_10_6_260 ((*(jmi->z))[jmi->offs_real_pi+253])
#define _hx_hh_grid_10_7_261 ((*(jmi->z))[jmi->offs_real_pi+254])
#define _hx_hh_grid_10_8_262 ((*(jmi->z))[jmi->offs_real_pi+255])
#define _hx_hh_grid_10_9_263 ((*(jmi->z))[jmi->offs_real_pi+256])
#define _hx_hh_grid_10_10_264 ((*(jmi->z))[jmi->offs_real_pi+257])
#define _hx_hh_grid_11_1_265 ((*(jmi->z))[jmi->offs_real_pi+258])
#define _hx_hh_grid_11_2_266 ((*(jmi->z))[jmi->offs_real_pi+259])
#define _hx_hh_grid_11_3_267 ((*(jmi->z))[jmi->offs_real_pi+260])
#define _hx_hh_grid_11_4_268 ((*(jmi->z))[jmi->offs_real_pi+261])
#define _hx_hh_grid_11_5_269 ((*(jmi->z))[jmi->offs_real_pi+262])
#define _hx_hh_grid_11_6_270 ((*(jmi->z))[jmi->offs_real_pi+263])
#define _hx_hh_grid_11_7_271 ((*(jmi->z))[jmi->offs_real_pi+264])
#define _hx_hh_grid_11_8_272 ((*(jmi->z))[jmi->offs_real_pi+265])
#define _hx_hh_grid_11_9_273 ((*(jmi->z))[jmi->offs_real_pi+266])
#define _hx_hh_grid_11_10_274 ((*(jmi->z))[jmi->offs_real_pi+267])
#define _hx_Ahx_275 ((*(jmi->z))[jmi->offs_real_pi+268])
#define _hx_Cmetal_276 ((*(jmi->z))[jmi->offs_real_pi+269])
#define _cabin_v_284 ((*(jmi->z))[jmi->offs_real_pi+270])
#define _cabin_p_285 ((*(jmi->z))[jmi->offs_real_pi+271])
#define _cabin_Qpass_286 ((*(jmi->z))[jmi->offs_real_pi+272])
#define _cabin_dQ_288 ((*(jmi->z))[jmi->offs_real_pi+273])
#define _fan_wf_293 ((*(jmi->z))[jmi->offs_real_pi+274])
#define __block_jacobian_check_tol_307 ((*(jmi->z))[jmi->offs_real_pi+275])
#define __cs_rel_tol_309 ((*(jmi->z))[jmi->offs_real_pi+276])
#define __cs_step_size_311 ((*(jmi->z))[jmi->offs_real_pi+277])
#define __events_default_tol_313 ((*(jmi->z))[jmi->offs_real_pi+278])
#define __events_tol_factor_314 ((*(jmi->z))[jmi->offs_real_pi+279])
#define __nle_solver_default_tol_318 ((*(jmi->z))[jmi->offs_real_pi+280])
#define __nle_solver_min_tol_321 ((*(jmi->z))[jmi->offs_real_pi+281])
#define __nle_solver_tol_factor_322 ((*(jmi->z))[jmi->offs_real_pi+282])
#define _hx_hc_277 ((*(jmi->z))[jmi->offs_real_pd+0])
#define _cabin_passenger_287 ((*(jmi->z))[jmi->offs_integer_pi+0])
#define __block_solver_experimental_mode_308 ((*(jmi->z))[jmi->offs_integer_pi+1])
#define __cs_solver_310 ((*(jmi->z))[jmi->offs_integer_pi+2])
#define __iteration_variable_scaling_315 ((*(jmi->z))[jmi->offs_integer_pi+3])
#define __log_level_316 ((*(jmi->z))[jmi->offs_integer_pi+4])
#define __nle_solver_log_level_319 ((*(jmi->z))[jmi->offs_integer_pi+5])
#define __nle_solver_max_iter_320 ((*(jmi->z))[jmi->offs_integer_pi+6])
#define __residual_equation_scaling_325 ((*(jmi->z))[jmi->offs_integer_pi+7])
#define __block_jacobian_check_306 ((*(jmi->z))[jmi->offs_boolean_pi+0])
#define __enforce_bounds_312 ((*(jmi->z))[jmi->offs_boolean_pi+1])
#define __nle_solver_check_jac_cond_317 ((*(jmi->z))[jmi->offs_boolean_pi+2])
#define __rescale_after_singular_jac_323 ((*(jmi->z))[jmi->offs_boolean_pi+3])
#define __rescale_each_step_324 ((*(jmi->z))[jmi->offs_boolean_pi+4])
#define __runtime_log_to_file_326 ((*(jmi->z))[jmi->offs_boolean_pi+5])
#define __use_Brent_in_1d_327 ((*(jmi->z))[jmi->offs_boolean_pi+6])
#define __use_jacobian_equilibration_328 ((*(jmi->z))[jmi->offs_boolean_pi+7])
#define _der_fork_m_302 ((*(jmi->z))[jmi->offs_real_dx+0])
#define _der_fork_Q_303 ((*(jmi->z))[jmi->offs_real_dx+1])
#define _der_hx_Q_304 ((*(jmi->z))[jmi->offs_real_dx+2])
#define _der_cabin_m_305 ((*(jmi->z))[jmi->offs_real_dx+3])
#define _fork_m_7 ((*(jmi->z))[jmi->offs_real_x+0])
#define _fork_Q_8 ((*(jmi->z))[jmi->offs_real_x+1])
#define _hx_Q_280 ((*(jmi->z))[jmi->offs_real_x+2])
#define _cabin_m_290 ((*(jmi->z))[jmi->offs_real_x+3])
#define _fork_outport_w_4 ((*(jmi->z))[jmi->offs_real_w+0])
#define _fork_p_5 ((*(jmi->z))[jmi->offs_real_w+1])
#define _fork_t_6 ((*(jmi->z))[jmi->offs_real_w+2])
#define _hx_hh_278 ((*(jmi->z))[jmi->offs_real_w+3])
#define _hx_t_279 ((*(jmi->z))[jmi->offs_real_w+4])
#define _hx_rhoh_281 ((*(jmi->z))[jmi->offs_real_w+5])
#define _hx_qh_282 ((*(jmi->z))[jmi->offs_real_w+6])
#define _hx_qc_283 ((*(jmi->z))[jmi->offs_real_w+7])
#define _cabin_t_289 ((*(jmi->z))[jmi->offs_real_w+8])
#define _cabin_Q_291 ((*(jmi->z))[jmi->offs_real_w+9])
#define _cabin_wa_292 ((*(jmi->z))[jmi->offs_real_w+10])
#define _W1_294 ((*(jmi->z))[jmi->offs_real_w+11])
#define _W31_295 ((*(jmi->z))[jmi->offs_real_w+12])
#define _W32_296 ((*(jmi->z))[jmi->offs_real_w+13])
#define _W33_297 ((*(jmi->z))[jmi->offs_real_w+14])
#define _T6_299 ((*(jmi->z))[jmi->offs_real_w+15])
#define _T8_300 ((*(jmi->z))[jmi->offs_real_w+16])
#define _W8_301 ((*(jmi->z))[jmi->offs_real_w+17])
#define _time ((*(jmi->z))[jmi->offs_t])
#define pre_der_fork_m_302 ((*(jmi->z))[jmi->offs_pre_real_dx+0])
#define pre_der_fork_Q_303 ((*(jmi->z))[jmi->offs_pre_real_dx+1])
#define pre_der_hx_Q_304 ((*(jmi->z))[jmi->offs_pre_real_dx+2])
#define pre_der_cabin_m_305 ((*(jmi->z))[jmi->offs_pre_real_dx+3])
#define pre_fork_m_7 ((*(jmi->z))[jmi->offs_pre_real_x+0])
#define pre_fork_Q_8 ((*(jmi->z))[jmi->offs_pre_real_x+1])
#define pre_hx_Q_280 ((*(jmi->z))[jmi->offs_pre_real_x+2])
#define pre_cabin_m_290 ((*(jmi->z))[jmi->offs_pre_real_x+3])
#define pre_fork_outport_w_4 ((*(jmi->z))[jmi->offs_pre_real_w+0])
#define pre_fork_p_5 ((*(jmi->z))[jmi->offs_pre_real_w+1])
#define pre_fork_t_6 ((*(jmi->z))[jmi->offs_pre_real_w+2])
#define pre_hx_hh_278 ((*(jmi->z))[jmi->offs_pre_real_w+3])
#define pre_hx_t_279 ((*(jmi->z))[jmi->offs_pre_real_w+4])
#define pre_hx_rhoh_281 ((*(jmi->z))[jmi->offs_pre_real_w+5])
#define pre_hx_qh_282 ((*(jmi->z))[jmi->offs_pre_real_w+6])
#define pre_hx_qc_283 ((*(jmi->z))[jmi->offs_pre_real_w+7])
#define pre_cabin_t_289 ((*(jmi->z))[jmi->offs_pre_real_w+8])
#define pre_cabin_Q_291 ((*(jmi->z))[jmi->offs_pre_real_w+9])
#define pre_cabin_wa_292 ((*(jmi->z))[jmi->offs_pre_real_w+10])
#define pre_W1_294 ((*(jmi->z))[jmi->offs_pre_real_w+11])
#define pre_W31_295 ((*(jmi->z))[jmi->offs_pre_real_w+12])
#define pre_W32_296 ((*(jmi->z))[jmi->offs_pre_real_w+13])
#define pre_W33_297 ((*(jmi->z))[jmi->offs_pre_real_w+14])
#define pre_T6_299 ((*(jmi->z))[jmi->offs_pre_real_w+15])
#define pre_T8_300 ((*(jmi->z))[jmi->offs_pre_real_w+16])
#define pre_W8_301 ((*(jmi->z))[jmi->offs_pre_real_w+17])


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
    "_nle_solver_log_level",
    "_nle_solver_max_iter",
    "_nle_solver_min_tol",
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
    536871207, 278, 268435744, 279, 268435745, 280, 536871208, 281, 282, 268435746,
    268435747, 536871209, 283, 268435748, 268435749, 284, 285, 536871210, 536871211, 268435750,
    536871212, 536871213, 536871214, 0
};

const int fmi_runtime_options_map_length = 23;

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





void func_AMS_LinearMap_def(jmi_array_t* w_grid_a, jmi_array_t* t_grid_a, jmi_array_t* h_grid_a, jmi_ad_var_t w_v, jmi_ad_var_t t_v, jmi_ad_var_t* h_o);
jmi_ad_var_t func_AMS_LinearMap_exp(jmi_array_t* w_grid_a, jmi_array_t* t_grid_a, jmi_array_t* h_grid_a, jmi_ad_var_t w_v, jmi_ad_var_t t_v);




static int dae_block_0(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    jmi_real_t** res = &residual;
    int ef = 0;
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
        x[0] = 300;
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
        x[0] = 0.0;
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 326;
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
        (*res)[0] = 1;
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _T6_299;
        init_with_lbound(x[0], 0.0, "Resetting initial value for variable T6");
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        memset(residual, 0, 1 * sizeof(jmi_real_t));
        residual[0] = 716.75 * (- _W33_297);
    } else if (evaluation_mode == JMI_BLOCK_EVALUATE) {
        check_lbound(x[0], 0.0, "Out of bounds for variable T6");
        _T6_299 = x[0];
        (*res)[0] = 0 - (716.75 * ((- _W33_297) * _T6_299 + _W33_297 * _fork_t_6) - _hx_qh_282);
    } else if (evaluation_mode == JMI_BLOCK_EVALUATE_NON_REALS) {
    } else if (evaluation_mode == JMI_BLOCK_WRITE_BACK) {
        _T6_299 = x[0];
    }
    return ef;
}

static int dae_block_1(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    jmi_real_t** res = &residual;
    int ef = 0;
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 321;
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
        (*res)[0] = 1;
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _cabin_wa_292;
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        memset(residual, 0, 1 * sizeof(jmi_real_t));
        residual[0] = - _cabin_t_289 * 716.75;
    } else if (evaluation_mode == JMI_BLOCK_EVALUATE) {
        _cabin_wa_292 = x[0];
        (*res)[0] = (_W8_301 * _T8_300 + (- _fan_wf_293) * _cabin_t_289 + _cabin_wa_292 * _cabin_t_289) * 716.75 + _cabin_Qpass_286 * _cabin_passenger_287 + _cabin_dQ_288 - (0);
    } else if (evaluation_mode == JMI_BLOCK_EVALUATE_NON_REALS) {
    } else if (evaluation_mode == JMI_BLOCK_WRITE_BACK) {
        _cabin_wa_292 = x[0];
    }
    return ef;
}



static int dae_init_block_0(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    jmi_real_t** res = &residual;
    int ef = 0;
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
        x[0] = 300;
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
        x[0] = 0.0;
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 326;
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
        (*res)[0] = 1;
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _T6_299;
        init_with_lbound(x[0], 0.0, "Resetting initial value for variable T6");
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        memset(residual, 0, 1 * sizeof(jmi_real_t));
        residual[0] = 716.75 * (- _W33_297);
    } else if (evaluation_mode == JMI_BLOCK_EVALUATE) {
        check_lbound(x[0], 0.0, "Out of bounds for variable T6");
        _T6_299 = x[0];
        (*res)[0] = 0 - (716.75 * ((- _W33_297) * _T6_299 + _W33_297 * _fork_t_6) - _hx_qh_282);
    } else if (evaluation_mode == JMI_BLOCK_EVALUATE_NON_REALS) {
    } else if (evaluation_mode == JMI_BLOCK_WRITE_BACK) {
        _T6_299 = x[0];
    }
    return ef;
}

static int dae_init_block_1(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    jmi_real_t** res = &residual;
    int ef = 0;
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 321;
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
        (*res)[0] = 1;
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _cabin_wa_292;
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        memset(residual, 0, 1 * sizeof(jmi_real_t));
        residual[0] = - _cabin_t_289 * 716.75;
    } else if (evaluation_mode == JMI_BLOCK_EVALUATE) {
        _cabin_wa_292 = x[0];
        (*res)[0] = (_W8_301 * _T8_300 + (- _fan_wf_293) * _cabin_t_289 + _cabin_wa_292 * _cabin_t_289) * 716.75 + _cabin_Qpass_286 * _cabin_passenger_287 + _cabin_dQ_288 - (0);
    } else if (evaluation_mode == JMI_BLOCK_EVALUATE_NON_REALS) {
    } else if (evaluation_mode == JMI_BLOCK_WRITE_BACK) {
        _cabin_wa_292 = x[0];
    }
    return ef;
}







void func_AMS_LinearMap_def(jmi_array_t* w_grid_a, jmi_array_t* t_grid_a, jmi_array_t* h_grid_a, jmi_ad_var_t w_v, jmi_ad_var_t t_v, jmi_ad_var_t* h_o) {
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
    if (h_o != NULL) *h_o = h_v;
    JMI_DYNAMIC_FREE()
    return;
}

jmi_ad_var_t func_AMS_LinearMap_exp(jmi_array_t* w_grid_a, jmi_array_t* t_grid_a, jmi_array_t* h_grid_a, jmi_ad_var_t w_v, jmi_ad_var_t t_v) {
    jmi_ad_var_t h_v;
    func_AMS_LinearMap_def(w_grid_a, t_grid_a, h_grid_a, w_v, t_v, &h_v);
    return h_v;
}








static int model_ode_guards(jmi_t* jmi) {
  
  return 0;
}

static int model_ode_next_time_event(jmi_t* jmi, jmi_real_t* nextTime) {
  jmi_real_t nextTimeEvent;
  jmi_real_t nextTimeEventTmp;
  jmi_real_t nSamp;
  nextTimeEvent = JMI_INF;
  *nextTime = nextTimeEvent;

  return 0;
}

static int model_ode_derivatives(jmi_t* jmi) {
  int ef = 0;
      JMI_ARRAY_STATIC(tmp_1, 11, 1)
    JMI_ARRAY_STATIC(tmp_2, 10, 1)
    JMI_ARRAY_STATIC(tmp_3, 110, 2)
    model_ode_guards(jmi);
/************* ODE section *********/
    _fork_t_6 = jmi_divide_equation(jmi, (- _fork_Q_8),(- _fork_m_7 * 716.75),"(- fork.Q) / (- fork.m * 716.75)");
    _fork_p_5 = jmi_divide_equation(jmi, _fork_m_7 * _fork_t_6 * 8.314472,(_fork_v_3 * 0.02897),"fork.m * fork.t * 8.314472 / (fork.v * 0.02897)");
    _W1_294 = COND_EXP_EQ((COND_EXP_GT(_fork_p_5, AD_WRAP_LITERAL(0.5) * _inlet_p_env_0, JMI_TRUE, JMI_FALSE)), JMI_TRUE, AD_WRAP_LITERAL(7.316E-5) * (_inlet_p_env_0 + AD_WRAP_LITERAL(2) * _fork_p_5) * sqrt(jmi_divide_equation(jmi, (AD_WRAP_LITERAL(1) - jmi_divide_equation(jmi, _fork_p_5,_inlet_p_env_0,"fork.p / inlet.p_env")),_inlet_t_env_1,"(1 - fork.p / inlet.p_env) / inlet.t_env")), AD_WRAP_LITERAL(1.0338499999999999E-4) * _inlet_p_env_0 * sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(1),_inlet_t_env_1,"1 / inlet.t_env")));
    _hx_rhoh_281 = jmi_divide_equation(jmi, 0.02897 * _fork_p_5,(8.314472 * _fork_t_6),"0.02897 * fork.p / (8.314472 * fork.t)");
    _W33_297 = sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(2) * (_fork_p_5 - _cabin_p_285) * _hx_rhoh_281 * (1.0 * (_hx_Ahx_275) * (_hx_Ahx_275)),0.009895,"2 * (fork.p - cabin.p) * hx.rhoh * hx.Ahx ^ 2 / 0.009895"));
    _W32_296 = COND_EXP_EQ((COND_EXP_GT(_cabin_p_285, AD_WRAP_LITERAL(0.5) * _fork_p_5, JMI_TRUE, JMI_FALSE)), JMI_TRUE, AD_WRAP_LITERAL(4.72E-5) * (_fork_p_5 + AD_WRAP_LITERAL(2) * _cabin_p_285) * sqrt(jmi_divide_equation(jmi, (AD_WRAP_LITERAL(1) - jmi_divide_equation(jmi, _cabin_p_285,_fork_p_5,"cabin.p / fork.p")),_fork_t_6,"(1 - cabin.p / fork.p) / fork.t")), AD_WRAP_LITERAL(6.67E-5) * _fork_p_5 * sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(1),_fork_t_6,"1 / fork.t")));
    _fork_outport_w_4 = - _W33_297 + (- _W32_296) + 0;
    _der_fork_m_302 = _W1_294 + _fork_outport_w_4;
    _der_fork_Q_303 = _W1_294 * _inlet_t_env_1 * 716.75 + _fork_outport_w_4 * _fork_t_6 * 716.75;
    JMI_ARRAY_STATIC_INIT_1(tmp_1, 11)
    jmi_array_ref_1(tmp_1, 1) = _hx_wh_grid_1_24;
    jmi_array_ref_1(tmp_1, 2) = _hx_wh_grid_2_25;
    jmi_array_ref_1(tmp_1, 3) = _hx_wh_grid_3_26;
    jmi_array_ref_1(tmp_1, 4) = _hx_wh_grid_4_27;
    jmi_array_ref_1(tmp_1, 5) = _hx_wh_grid_5_28;
    jmi_array_ref_1(tmp_1, 6) = _hx_wh_grid_6_29;
    jmi_array_ref_1(tmp_1, 7) = _hx_wh_grid_7_30;
    jmi_array_ref_1(tmp_1, 8) = _hx_wh_grid_8_31;
    jmi_array_ref_1(tmp_1, 9) = _hx_wh_grid_9_32;
    jmi_array_ref_1(tmp_1, 10) = _hx_wh_grid_10_33;
    jmi_array_ref_1(tmp_1, 11) = _hx_wh_grid_11_34;
    JMI_ARRAY_STATIC_INIT_1(tmp_2, 10)
    jmi_array_ref_1(tmp_2, 1) = _hx_th_grid_1_35;
    jmi_array_ref_1(tmp_2, 2) = _hx_th_grid_2_36;
    jmi_array_ref_1(tmp_2, 3) = _hx_th_grid_3_37;
    jmi_array_ref_1(tmp_2, 4) = _hx_th_grid_4_38;
    jmi_array_ref_1(tmp_2, 5) = _hx_th_grid_5_39;
    jmi_array_ref_1(tmp_2, 6) = _hx_th_grid_6_40;
    jmi_array_ref_1(tmp_2, 7) = _hx_th_grid_7_41;
    jmi_array_ref_1(tmp_2, 8) = _hx_th_grid_8_42;
    jmi_array_ref_1(tmp_2, 9) = _hx_th_grid_9_43;
    jmi_array_ref_1(tmp_2, 10) = _hx_th_grid_10_44;
    JMI_ARRAY_STATIC_INIT_2(tmp_3, 11, 10)
    jmi_array_ref_2(tmp_3, 1,1) = _hx_hh_grid_1_1_165;
    jmi_array_ref_2(tmp_3, 1,2) = _hx_hh_grid_1_2_166;
    jmi_array_ref_2(tmp_3, 1,3) = _hx_hh_grid_1_3_167;
    jmi_array_ref_2(tmp_3, 1,4) = _hx_hh_grid_1_4_168;
    jmi_array_ref_2(tmp_3, 1,5) = _hx_hh_grid_1_5_169;
    jmi_array_ref_2(tmp_3, 1,6) = _hx_hh_grid_1_6_170;
    jmi_array_ref_2(tmp_3, 1,7) = _hx_hh_grid_1_7_171;
    jmi_array_ref_2(tmp_3, 1,8) = _hx_hh_grid_1_8_172;
    jmi_array_ref_2(tmp_3, 1,9) = _hx_hh_grid_1_9_173;
    jmi_array_ref_2(tmp_3, 1,10) = _hx_hh_grid_1_10_174;
    jmi_array_ref_2(tmp_3, 2,1) = _hx_hh_grid_2_1_175;
    jmi_array_ref_2(tmp_3, 2,2) = _hx_hh_grid_2_2_176;
    jmi_array_ref_2(tmp_3, 2,3) = _hx_hh_grid_2_3_177;
    jmi_array_ref_2(tmp_3, 2,4) = _hx_hh_grid_2_4_178;
    jmi_array_ref_2(tmp_3, 2,5) = _hx_hh_grid_2_5_179;
    jmi_array_ref_2(tmp_3, 2,6) = _hx_hh_grid_2_6_180;
    jmi_array_ref_2(tmp_3, 2,7) = _hx_hh_grid_2_7_181;
    jmi_array_ref_2(tmp_3, 2,8) = _hx_hh_grid_2_8_182;
    jmi_array_ref_2(tmp_3, 2,9) = _hx_hh_grid_2_9_183;
    jmi_array_ref_2(tmp_3, 2,10) = _hx_hh_grid_2_10_184;
    jmi_array_ref_2(tmp_3, 3,1) = _hx_hh_grid_3_1_185;
    jmi_array_ref_2(tmp_3, 3,2) = _hx_hh_grid_3_2_186;
    jmi_array_ref_2(tmp_3, 3,3) = _hx_hh_grid_3_3_187;
    jmi_array_ref_2(tmp_3, 3,4) = _hx_hh_grid_3_4_188;
    jmi_array_ref_2(tmp_3, 3,5) = _hx_hh_grid_3_5_189;
    jmi_array_ref_2(tmp_3, 3,6) = _hx_hh_grid_3_6_190;
    jmi_array_ref_2(tmp_3, 3,7) = _hx_hh_grid_3_7_191;
    jmi_array_ref_2(tmp_3, 3,8) = _hx_hh_grid_3_8_192;
    jmi_array_ref_2(tmp_3, 3,9) = _hx_hh_grid_3_9_193;
    jmi_array_ref_2(tmp_3, 3,10) = _hx_hh_grid_3_10_194;
    jmi_array_ref_2(tmp_3, 4,1) = _hx_hh_grid_4_1_195;
    jmi_array_ref_2(tmp_3, 4,2) = _hx_hh_grid_4_2_196;
    jmi_array_ref_2(tmp_3, 4,3) = _hx_hh_grid_4_3_197;
    jmi_array_ref_2(tmp_3, 4,4) = _hx_hh_grid_4_4_198;
    jmi_array_ref_2(tmp_3, 4,5) = _hx_hh_grid_4_5_199;
    jmi_array_ref_2(tmp_3, 4,6) = _hx_hh_grid_4_6_200;
    jmi_array_ref_2(tmp_3, 4,7) = _hx_hh_grid_4_7_201;
    jmi_array_ref_2(tmp_3, 4,8) = _hx_hh_grid_4_8_202;
    jmi_array_ref_2(tmp_3, 4,9) = _hx_hh_grid_4_9_203;
    jmi_array_ref_2(tmp_3, 4,10) = _hx_hh_grid_4_10_204;
    jmi_array_ref_2(tmp_3, 5,1) = _hx_hh_grid_5_1_205;
    jmi_array_ref_2(tmp_3, 5,2) = _hx_hh_grid_5_2_206;
    jmi_array_ref_2(tmp_3, 5,3) = _hx_hh_grid_5_3_207;
    jmi_array_ref_2(tmp_3, 5,4) = _hx_hh_grid_5_4_208;
    jmi_array_ref_2(tmp_3, 5,5) = _hx_hh_grid_5_5_209;
    jmi_array_ref_2(tmp_3, 5,6) = _hx_hh_grid_5_6_210;
    jmi_array_ref_2(tmp_3, 5,7) = _hx_hh_grid_5_7_211;
    jmi_array_ref_2(tmp_3, 5,8) = _hx_hh_grid_5_8_212;
    jmi_array_ref_2(tmp_3, 5,9) = _hx_hh_grid_5_9_213;
    jmi_array_ref_2(tmp_3, 5,10) = _hx_hh_grid_5_10_214;
    jmi_array_ref_2(tmp_3, 6,1) = _hx_hh_grid_6_1_215;
    jmi_array_ref_2(tmp_3, 6,2) = _hx_hh_grid_6_2_216;
    jmi_array_ref_2(tmp_3, 6,3) = _hx_hh_grid_6_3_217;
    jmi_array_ref_2(tmp_3, 6,4) = _hx_hh_grid_6_4_218;
    jmi_array_ref_2(tmp_3, 6,5) = _hx_hh_grid_6_5_219;
    jmi_array_ref_2(tmp_3, 6,6) = _hx_hh_grid_6_6_220;
    jmi_array_ref_2(tmp_3, 6,7) = _hx_hh_grid_6_7_221;
    jmi_array_ref_2(tmp_3, 6,8) = _hx_hh_grid_6_8_222;
    jmi_array_ref_2(tmp_3, 6,9) = _hx_hh_grid_6_9_223;
    jmi_array_ref_2(tmp_3, 6,10) = _hx_hh_grid_6_10_224;
    jmi_array_ref_2(tmp_3, 7,1) = _hx_hh_grid_7_1_225;
    jmi_array_ref_2(tmp_3, 7,2) = _hx_hh_grid_7_2_226;
    jmi_array_ref_2(tmp_3, 7,3) = _hx_hh_grid_7_3_227;
    jmi_array_ref_2(tmp_3, 7,4) = _hx_hh_grid_7_4_228;
    jmi_array_ref_2(tmp_3, 7,5) = _hx_hh_grid_7_5_229;
    jmi_array_ref_2(tmp_3, 7,6) = _hx_hh_grid_7_6_230;
    jmi_array_ref_2(tmp_3, 7,7) = _hx_hh_grid_7_7_231;
    jmi_array_ref_2(tmp_3, 7,8) = _hx_hh_grid_7_8_232;
    jmi_array_ref_2(tmp_3, 7,9) = _hx_hh_grid_7_9_233;
    jmi_array_ref_2(tmp_3, 7,10) = _hx_hh_grid_7_10_234;
    jmi_array_ref_2(tmp_3, 8,1) = _hx_hh_grid_8_1_235;
    jmi_array_ref_2(tmp_3, 8,2) = _hx_hh_grid_8_2_236;
    jmi_array_ref_2(tmp_3, 8,3) = _hx_hh_grid_8_3_237;
    jmi_array_ref_2(tmp_3, 8,4) = _hx_hh_grid_8_4_238;
    jmi_array_ref_2(tmp_3, 8,5) = _hx_hh_grid_8_5_239;
    jmi_array_ref_2(tmp_3, 8,6) = _hx_hh_grid_8_6_240;
    jmi_array_ref_2(tmp_3, 8,7) = _hx_hh_grid_8_7_241;
    jmi_array_ref_2(tmp_3, 8,8) = _hx_hh_grid_8_8_242;
    jmi_array_ref_2(tmp_3, 8,9) = _hx_hh_grid_8_9_243;
    jmi_array_ref_2(tmp_3, 8,10) = _hx_hh_grid_8_10_244;
    jmi_array_ref_2(tmp_3, 9,1) = _hx_hh_grid_9_1_245;
    jmi_array_ref_2(tmp_3, 9,2) = _hx_hh_grid_9_2_246;
    jmi_array_ref_2(tmp_3, 9,3) = _hx_hh_grid_9_3_247;
    jmi_array_ref_2(tmp_3, 9,4) = _hx_hh_grid_9_4_248;
    jmi_array_ref_2(tmp_3, 9,5) = _hx_hh_grid_9_5_249;
    jmi_array_ref_2(tmp_3, 9,6) = _hx_hh_grid_9_6_250;
    jmi_array_ref_2(tmp_3, 9,7) = _hx_hh_grid_9_7_251;
    jmi_array_ref_2(tmp_3, 9,8) = _hx_hh_grid_9_8_252;
    jmi_array_ref_2(tmp_3, 9,9) = _hx_hh_grid_9_9_253;
    jmi_array_ref_2(tmp_3, 9,10) = _hx_hh_grid_9_10_254;
    jmi_array_ref_2(tmp_3, 10,1) = _hx_hh_grid_10_1_255;
    jmi_array_ref_2(tmp_3, 10,2) = _hx_hh_grid_10_2_256;
    jmi_array_ref_2(tmp_3, 10,3) = _hx_hh_grid_10_3_257;
    jmi_array_ref_2(tmp_3, 10,4) = _hx_hh_grid_10_4_258;
    jmi_array_ref_2(tmp_3, 10,5) = _hx_hh_grid_10_5_259;
    jmi_array_ref_2(tmp_3, 10,6) = _hx_hh_grid_10_6_260;
    jmi_array_ref_2(tmp_3, 10,7) = _hx_hh_grid_10_7_261;
    jmi_array_ref_2(tmp_3, 10,8) = _hx_hh_grid_10_8_262;
    jmi_array_ref_2(tmp_3, 10,9) = _hx_hh_grid_10_9_263;
    jmi_array_ref_2(tmp_3, 10,10) = _hx_hh_grid_10_10_264;
    jmi_array_ref_2(tmp_3, 11,1) = _hx_hh_grid_11_1_265;
    jmi_array_ref_2(tmp_3, 11,2) = _hx_hh_grid_11_2_266;
    jmi_array_ref_2(tmp_3, 11,3) = _hx_hh_grid_11_3_267;
    jmi_array_ref_2(tmp_3, 11,4) = _hx_hh_grid_11_4_268;
    jmi_array_ref_2(tmp_3, 11,5) = _hx_hh_grid_11_5_269;
    jmi_array_ref_2(tmp_3, 11,6) = _hx_hh_grid_11_6_270;
    jmi_array_ref_2(tmp_3, 11,7) = _hx_hh_grid_11_7_271;
    jmi_array_ref_2(tmp_3, 11,8) = _hx_hh_grid_11_8_272;
    jmi_array_ref_2(tmp_3, 11,9) = _hx_hh_grid_11_9_273;
    jmi_array_ref_2(tmp_3, 11,10) = _hx_hh_grid_11_10_274;
    _hx_hh_278 = func_AMS_LinearMap_exp(tmp_1, tmp_2, tmp_3, _W33_297, _fork_t_6);
    _hx_t_279 = jmi_divide_equation(jmi, (- _hx_Q_280),(- _hx_m_12 * _hx_Cmetal_276),"(- hx.Q) / (- hx.m * hx.Cmetal)");
    _hx_qh_282 = _hx_hh_278 * (_fork_t_6 - _hx_t_279);
    _hx_qc_283 = _hx_hc_277 * (_ambientair_t_env_11 - _hx_t_279);
    _der_hx_Q_304 = _hx_qh_282 + _hx_qc_283;
    _W8_301 = jmi_divide_equation(jmi, (- _W33_297 + (- _fan_wf_293) + (- _W32_296)),(- 1.0),"(- W33 + (- fan.wf) + (- W32)) / (- 1.0)");
    ef |= jmi_solve_block_residual(jmi->dae_block_residuals[0]);
    _cabin_t_289 = jmi_divide_equation(jmi, (- _cabin_p_285),(- _cabin_m_290 * 8.314472 * jmi_divide_equation(jmi, 1.0,_cabin_v_284,"(1.0 / cabin.v)") * jmi_divide_equation(jmi, 1.0,0.02897,"(1.0 / 0.02897)")),"(- cabin.p) / (- cabin.m * 8.314472 * (1.0 / cabin.v) * (1.0 / 0.02897))");
    _T8_300 = jmi_divide_equation(jmi, (- _W33_297 * _T6_299 + (- _fan_wf_293 * _cabin_t_289) + (- _W32_296 * _fork_t_6)),((- 1.0) * _W8_301),"(- W33 * T6 + (- fan.wf * cabin.t) + (- W32 * fork.t)) / ((- 1.0) * W8)");
    ef |= jmi_solve_block_residual(jmi->dae_block_residuals[1]);
    _der_cabin_m_305 = _W8_301 + (- _fan_wf_293) + _cabin_wa_292;
/************ Real outputs *********/
/****Integer and boolean outputs ***/
/**** Other variables ***/
    _cabin_Q_291 = _cabin_m_290 * _cabin_t_289 * 716.75;
    _W31_295 = _W32_296 + _W33_297;

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
      JMI_ARRAY_STATIC(tmp_1, 11, 1)
    JMI_ARRAY_STATIC(tmp_2, 10, 1)
    JMI_ARRAY_STATIC(tmp_3, 110, 2)
    model_ode_guards(jmi);
    _fork_p_5 = 80000;
    _fork_t_6 = 350;
    _W32_296 = COND_EXP_EQ((COND_EXP_GT(_cabin_p_285, AD_WRAP_LITERAL(0.5) * _fork_p_5, JMI_TRUE, JMI_FALSE)), JMI_TRUE, AD_WRAP_LITERAL(4.72E-5) * (_fork_p_5 + AD_WRAP_LITERAL(2) * _cabin_p_285) * sqrt(jmi_divide_equation(jmi, (AD_WRAP_LITERAL(1) - jmi_divide_equation(jmi, _cabin_p_285,_fork_p_5,"cabin.p / fork.p")),_fork_t_6,"(1 - cabin.p / fork.p) / fork.t")), AD_WRAP_LITERAL(6.67E-5) * _fork_p_5 * sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(1),_fork_t_6,"1 / fork.t")));
    _hx_rhoh_281 = jmi_divide_equation(jmi, 0.02897 * _fork_p_5,(8.314472 * _fork_t_6),"0.02897 * fork.p / (8.314472 * fork.t)");
    _W33_297 = sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(2) * (_fork_p_5 - _cabin_p_285) * _hx_rhoh_281 * (1.0 * (_hx_Ahx_275) * (_hx_Ahx_275)),0.009895,"2 * (fork.p - cabin.p) * hx.rhoh * hx.Ahx ^ 2 / 0.009895"));
    _W31_295 = _W32_296 + _W33_297;
    _W1_294 = COND_EXP_EQ((COND_EXP_GT(_fork_p_5, AD_WRAP_LITERAL(0.5) * _inlet_p_env_0, JMI_TRUE, JMI_FALSE)), JMI_TRUE, AD_WRAP_LITERAL(7.316E-5) * (_inlet_p_env_0 + AD_WRAP_LITERAL(2) * _fork_p_5) * sqrt(jmi_divide_equation(jmi, (AD_WRAP_LITERAL(1) - jmi_divide_equation(jmi, _fork_p_5,_inlet_p_env_0,"fork.p / inlet.p_env")),_inlet_t_env_1,"(1 - fork.p / inlet.p_env) / inlet.t_env")), AD_WRAP_LITERAL(1.0338499999999999E-4) * _inlet_p_env_0 * sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(1),_inlet_t_env_1,"1 / inlet.t_env")));
    _fork_m_7 = jmi_divide_equation(jmi, (- _fork_p_5),(- _fork_t_6 * 8.314472 * jmi_divide_equation(jmi, 1.0,_fork_v_3,"(1.0 / fork.v)") * jmi_divide_equation(jmi, 1.0,0.02897,"(1.0 / 0.02897)")),"(- fork.p) / (- fork.t * 8.314472 * (1.0 / fork.v) * (1.0 / 0.02897))");
    _fork_Q_8 = _fork_m_7 * _fork_t_6 * 716.75;
    _fork_outport_w_4 = - _W33_297 + (- _W32_296) + 0;
    _der_fork_m_302 = _W1_294 + _fork_outport_w_4;
    _der_fork_Q_303 = _W1_294 * _inlet_t_env_1 * 716.75 + _fork_outport_w_4 * _fork_t_6 * 716.75;
    JMI_ARRAY_STATIC_INIT_1(tmp_1, 11)
    jmi_array_ref_1(tmp_1, 1) = _hx_wh_grid_1_24;
    jmi_array_ref_1(tmp_1, 2) = _hx_wh_grid_2_25;
    jmi_array_ref_1(tmp_1, 3) = _hx_wh_grid_3_26;
    jmi_array_ref_1(tmp_1, 4) = _hx_wh_grid_4_27;
    jmi_array_ref_1(tmp_1, 5) = _hx_wh_grid_5_28;
    jmi_array_ref_1(tmp_1, 6) = _hx_wh_grid_6_29;
    jmi_array_ref_1(tmp_1, 7) = _hx_wh_grid_7_30;
    jmi_array_ref_1(tmp_1, 8) = _hx_wh_grid_8_31;
    jmi_array_ref_1(tmp_1, 9) = _hx_wh_grid_9_32;
    jmi_array_ref_1(tmp_1, 10) = _hx_wh_grid_10_33;
    jmi_array_ref_1(tmp_1, 11) = _hx_wh_grid_11_34;
    JMI_ARRAY_STATIC_INIT_1(tmp_2, 10)
    jmi_array_ref_1(tmp_2, 1) = _hx_th_grid_1_35;
    jmi_array_ref_1(tmp_2, 2) = _hx_th_grid_2_36;
    jmi_array_ref_1(tmp_2, 3) = _hx_th_grid_3_37;
    jmi_array_ref_1(tmp_2, 4) = _hx_th_grid_4_38;
    jmi_array_ref_1(tmp_2, 5) = _hx_th_grid_5_39;
    jmi_array_ref_1(tmp_2, 6) = _hx_th_grid_6_40;
    jmi_array_ref_1(tmp_2, 7) = _hx_th_grid_7_41;
    jmi_array_ref_1(tmp_2, 8) = _hx_th_grid_8_42;
    jmi_array_ref_1(tmp_2, 9) = _hx_th_grid_9_43;
    jmi_array_ref_1(tmp_2, 10) = _hx_th_grid_10_44;
    JMI_ARRAY_STATIC_INIT_2(tmp_3, 11, 10)
    jmi_array_ref_2(tmp_3, 1,1) = _hx_hh_grid_1_1_165;
    jmi_array_ref_2(tmp_3, 1,2) = _hx_hh_grid_1_2_166;
    jmi_array_ref_2(tmp_3, 1,3) = _hx_hh_grid_1_3_167;
    jmi_array_ref_2(tmp_3, 1,4) = _hx_hh_grid_1_4_168;
    jmi_array_ref_2(tmp_3, 1,5) = _hx_hh_grid_1_5_169;
    jmi_array_ref_2(tmp_3, 1,6) = _hx_hh_grid_1_6_170;
    jmi_array_ref_2(tmp_3, 1,7) = _hx_hh_grid_1_7_171;
    jmi_array_ref_2(tmp_3, 1,8) = _hx_hh_grid_1_8_172;
    jmi_array_ref_2(tmp_3, 1,9) = _hx_hh_grid_1_9_173;
    jmi_array_ref_2(tmp_3, 1,10) = _hx_hh_grid_1_10_174;
    jmi_array_ref_2(tmp_3, 2,1) = _hx_hh_grid_2_1_175;
    jmi_array_ref_2(tmp_3, 2,2) = _hx_hh_grid_2_2_176;
    jmi_array_ref_2(tmp_3, 2,3) = _hx_hh_grid_2_3_177;
    jmi_array_ref_2(tmp_3, 2,4) = _hx_hh_grid_2_4_178;
    jmi_array_ref_2(tmp_3, 2,5) = _hx_hh_grid_2_5_179;
    jmi_array_ref_2(tmp_3, 2,6) = _hx_hh_grid_2_6_180;
    jmi_array_ref_2(tmp_3, 2,7) = _hx_hh_grid_2_7_181;
    jmi_array_ref_2(tmp_3, 2,8) = _hx_hh_grid_2_8_182;
    jmi_array_ref_2(tmp_3, 2,9) = _hx_hh_grid_2_9_183;
    jmi_array_ref_2(tmp_3, 2,10) = _hx_hh_grid_2_10_184;
    jmi_array_ref_2(tmp_3, 3,1) = _hx_hh_grid_3_1_185;
    jmi_array_ref_2(tmp_3, 3,2) = _hx_hh_grid_3_2_186;
    jmi_array_ref_2(tmp_3, 3,3) = _hx_hh_grid_3_3_187;
    jmi_array_ref_2(tmp_3, 3,4) = _hx_hh_grid_3_4_188;
    jmi_array_ref_2(tmp_3, 3,5) = _hx_hh_grid_3_5_189;
    jmi_array_ref_2(tmp_3, 3,6) = _hx_hh_grid_3_6_190;
    jmi_array_ref_2(tmp_3, 3,7) = _hx_hh_grid_3_7_191;
    jmi_array_ref_2(tmp_3, 3,8) = _hx_hh_grid_3_8_192;
    jmi_array_ref_2(tmp_3, 3,9) = _hx_hh_grid_3_9_193;
    jmi_array_ref_2(tmp_3, 3,10) = _hx_hh_grid_3_10_194;
    jmi_array_ref_2(tmp_3, 4,1) = _hx_hh_grid_4_1_195;
    jmi_array_ref_2(tmp_3, 4,2) = _hx_hh_grid_4_2_196;
    jmi_array_ref_2(tmp_3, 4,3) = _hx_hh_grid_4_3_197;
    jmi_array_ref_2(tmp_3, 4,4) = _hx_hh_grid_4_4_198;
    jmi_array_ref_2(tmp_3, 4,5) = _hx_hh_grid_4_5_199;
    jmi_array_ref_2(tmp_3, 4,6) = _hx_hh_grid_4_6_200;
    jmi_array_ref_2(tmp_3, 4,7) = _hx_hh_grid_4_7_201;
    jmi_array_ref_2(tmp_3, 4,8) = _hx_hh_grid_4_8_202;
    jmi_array_ref_2(tmp_3, 4,9) = _hx_hh_grid_4_9_203;
    jmi_array_ref_2(tmp_3, 4,10) = _hx_hh_grid_4_10_204;
    jmi_array_ref_2(tmp_3, 5,1) = _hx_hh_grid_5_1_205;
    jmi_array_ref_2(tmp_3, 5,2) = _hx_hh_grid_5_2_206;
    jmi_array_ref_2(tmp_3, 5,3) = _hx_hh_grid_5_3_207;
    jmi_array_ref_2(tmp_3, 5,4) = _hx_hh_grid_5_4_208;
    jmi_array_ref_2(tmp_3, 5,5) = _hx_hh_grid_5_5_209;
    jmi_array_ref_2(tmp_3, 5,6) = _hx_hh_grid_5_6_210;
    jmi_array_ref_2(tmp_3, 5,7) = _hx_hh_grid_5_7_211;
    jmi_array_ref_2(tmp_3, 5,8) = _hx_hh_grid_5_8_212;
    jmi_array_ref_2(tmp_3, 5,9) = _hx_hh_grid_5_9_213;
    jmi_array_ref_2(tmp_3, 5,10) = _hx_hh_grid_5_10_214;
    jmi_array_ref_2(tmp_3, 6,1) = _hx_hh_grid_6_1_215;
    jmi_array_ref_2(tmp_3, 6,2) = _hx_hh_grid_6_2_216;
    jmi_array_ref_2(tmp_3, 6,3) = _hx_hh_grid_6_3_217;
    jmi_array_ref_2(tmp_3, 6,4) = _hx_hh_grid_6_4_218;
    jmi_array_ref_2(tmp_3, 6,5) = _hx_hh_grid_6_5_219;
    jmi_array_ref_2(tmp_3, 6,6) = _hx_hh_grid_6_6_220;
    jmi_array_ref_2(tmp_3, 6,7) = _hx_hh_grid_6_7_221;
    jmi_array_ref_2(tmp_3, 6,8) = _hx_hh_grid_6_8_222;
    jmi_array_ref_2(tmp_3, 6,9) = _hx_hh_grid_6_9_223;
    jmi_array_ref_2(tmp_3, 6,10) = _hx_hh_grid_6_10_224;
    jmi_array_ref_2(tmp_3, 7,1) = _hx_hh_grid_7_1_225;
    jmi_array_ref_2(tmp_3, 7,2) = _hx_hh_grid_7_2_226;
    jmi_array_ref_2(tmp_3, 7,3) = _hx_hh_grid_7_3_227;
    jmi_array_ref_2(tmp_3, 7,4) = _hx_hh_grid_7_4_228;
    jmi_array_ref_2(tmp_3, 7,5) = _hx_hh_grid_7_5_229;
    jmi_array_ref_2(tmp_3, 7,6) = _hx_hh_grid_7_6_230;
    jmi_array_ref_2(tmp_3, 7,7) = _hx_hh_grid_7_7_231;
    jmi_array_ref_2(tmp_3, 7,8) = _hx_hh_grid_7_8_232;
    jmi_array_ref_2(tmp_3, 7,9) = _hx_hh_grid_7_9_233;
    jmi_array_ref_2(tmp_3, 7,10) = _hx_hh_grid_7_10_234;
    jmi_array_ref_2(tmp_3, 8,1) = _hx_hh_grid_8_1_235;
    jmi_array_ref_2(tmp_3, 8,2) = _hx_hh_grid_8_2_236;
    jmi_array_ref_2(tmp_3, 8,3) = _hx_hh_grid_8_3_237;
    jmi_array_ref_2(tmp_3, 8,4) = _hx_hh_grid_8_4_238;
    jmi_array_ref_2(tmp_3, 8,5) = _hx_hh_grid_8_5_239;
    jmi_array_ref_2(tmp_3, 8,6) = _hx_hh_grid_8_6_240;
    jmi_array_ref_2(tmp_3, 8,7) = _hx_hh_grid_8_7_241;
    jmi_array_ref_2(tmp_3, 8,8) = _hx_hh_grid_8_8_242;
    jmi_array_ref_2(tmp_3, 8,9) = _hx_hh_grid_8_9_243;
    jmi_array_ref_2(tmp_3, 8,10) = _hx_hh_grid_8_10_244;
    jmi_array_ref_2(tmp_3, 9,1) = _hx_hh_grid_9_1_245;
    jmi_array_ref_2(tmp_3, 9,2) = _hx_hh_grid_9_2_246;
    jmi_array_ref_2(tmp_3, 9,3) = _hx_hh_grid_9_3_247;
    jmi_array_ref_2(tmp_3, 9,4) = _hx_hh_grid_9_4_248;
    jmi_array_ref_2(tmp_3, 9,5) = _hx_hh_grid_9_5_249;
    jmi_array_ref_2(tmp_3, 9,6) = _hx_hh_grid_9_6_250;
    jmi_array_ref_2(tmp_3, 9,7) = _hx_hh_grid_9_7_251;
    jmi_array_ref_2(tmp_3, 9,8) = _hx_hh_grid_9_8_252;
    jmi_array_ref_2(tmp_3, 9,9) = _hx_hh_grid_9_9_253;
    jmi_array_ref_2(tmp_3, 9,10) = _hx_hh_grid_9_10_254;
    jmi_array_ref_2(tmp_3, 10,1) = _hx_hh_grid_10_1_255;
    jmi_array_ref_2(tmp_3, 10,2) = _hx_hh_grid_10_2_256;
    jmi_array_ref_2(tmp_3, 10,3) = _hx_hh_grid_10_3_257;
    jmi_array_ref_2(tmp_3, 10,4) = _hx_hh_grid_10_4_258;
    jmi_array_ref_2(tmp_3, 10,5) = _hx_hh_grid_10_5_259;
    jmi_array_ref_2(tmp_3, 10,6) = _hx_hh_grid_10_6_260;
    jmi_array_ref_2(tmp_3, 10,7) = _hx_hh_grid_10_7_261;
    jmi_array_ref_2(tmp_3, 10,8) = _hx_hh_grid_10_8_262;
    jmi_array_ref_2(tmp_3, 10,9) = _hx_hh_grid_10_9_263;
    jmi_array_ref_2(tmp_3, 10,10) = _hx_hh_grid_10_10_264;
    jmi_array_ref_2(tmp_3, 11,1) = _hx_hh_grid_11_1_265;
    jmi_array_ref_2(tmp_3, 11,2) = _hx_hh_grid_11_2_266;
    jmi_array_ref_2(tmp_3, 11,3) = _hx_hh_grid_11_3_267;
    jmi_array_ref_2(tmp_3, 11,4) = _hx_hh_grid_11_4_268;
    jmi_array_ref_2(tmp_3, 11,5) = _hx_hh_grid_11_5_269;
    jmi_array_ref_2(tmp_3, 11,6) = _hx_hh_grid_11_6_270;
    jmi_array_ref_2(tmp_3, 11,7) = _hx_hh_grid_11_7_271;
    jmi_array_ref_2(tmp_3, 11,8) = _hx_hh_grid_11_8_272;
    jmi_array_ref_2(tmp_3, 11,9) = _hx_hh_grid_11_9_273;
    jmi_array_ref_2(tmp_3, 11,10) = _hx_hh_grid_11_10_274;
    _hx_hh_278 = func_AMS_LinearMap_exp(tmp_1, tmp_2, tmp_3, _W33_297, _fork_t_6);
    _hx_t_279 = 250;
    _hx_qh_282 = _hx_hh_278 * (_fork_t_6 - _hx_t_279);
    ef |= jmi_solve_block_residual(jmi->dae_init_block_residuals[0]);
    _hx_Q_280 = _hx_m_12 * _hx_t_279 * _hx_Cmetal_276;
    _hx_qc_283 = _hx_hc_277 * (_ambientair_t_env_11 - _hx_t_279);
    _der_hx_Q_304 = _hx_qh_282 + _hx_qc_283;
    _W8_301 = jmi_divide_equation(jmi, (- _W33_297 + (- _fan_wf_293) + (- _W32_296)),(- 1.0),"(- W33 + (- fan.wf) + (- W32)) / (- 1.0)");
    _cabin_t_289 = 297.2;
    _T8_300 = jmi_divide_equation(jmi, (- _W33_297 * _T6_299 + (- _fan_wf_293 * _cabin_t_289) + (- _W32_296 * _fork_t_6)),((- 1.0) * _W8_301),"(- W33 * T6 + (- fan.wf * cabin.t) + (- W32 * fork.t)) / ((- 1.0) * W8)");
    _cabin_m_290 = jmi_divide_equation(jmi, (- _cabin_p_285),(- _cabin_t_289 * 8.314472 * jmi_divide_equation(jmi, 1.0,_cabin_v_284,"(1.0 / cabin.v)") * jmi_divide_equation(jmi, 1.0,0.02897,"(1.0 / 0.02897)")),"(- cabin.p) / (- cabin.t * 8.314472 * (1.0 / cabin.v) * (1.0 / 0.02897))");
    _cabin_Q_291 = _cabin_m_290 * _cabin_t_289 * 716.75;
    ef |= jmi_solve_block_residual(jmi->dae_init_block_residuals[1]);
    _der_cabin_m_305 = _W8_301 + (- _fan_wf_293) + _cabin_wa_292;

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
    (*res)[0] = 0.0 - _fork_outport_w_4;
    (*res)[1] = 0.0 - _fork_m_7;
    (*res)[2] = 0.0 - _fork_Q_8;
    (*res)[3] = 0.0 - _hx_hh_278;
    (*res)[4] = 0.0 - _hx_Q_280;
    (*res)[5] = 0.0 - _hx_rhoh_281;
    (*res)[6] = 0.0 - _hx_qh_282;
    (*res)[7] = 0.0 - _hx_qc_283;
    (*res)[8] = 0.0 - _cabin_m_290;
    (*res)[9] = 0.0 - _cabin_Q_291;
    (*res)[10] = 0.0 - _cabin_wa_292;
    (*res)[11] = 0.0 - _W1_294;
    (*res)[12] = 0.0 - _W31_295;
    (*res)[13] = 0.0 - _W32_296;
    (*res)[14] = 0.0 - _W33_297;
    (*res)[15] = 288.15 - _T6_299;
    (*res)[16] = 288.15 - _T8_300;
    (*res)[17] = 0.0 - _W8_301;

	return 0;
}

static int model_init_Fp(jmi_t* jmi, jmi_real_t** res) {
  /* C_DAE_initial_dependent_parameter_residuals */
	return -1;
}

static int model_init_eval_parameters(jmi_t* jmi) {
    JMI_ARRAY_STATIC(tmp_4, 11, 1)
    JMI_ARRAY_STATIC(tmp_5, 10, 1)
    JMI_ARRAY_STATIC(tmp_6, 110, 2)
    JMI_ARRAY_STATIC_INIT_1(tmp_4, 11)
    jmi_array_ref_1(tmp_4, 1) = _hx_wc_grid_1_13;
    jmi_array_ref_1(tmp_4, 2) = _hx_wc_grid_2_14;
    jmi_array_ref_1(tmp_4, 3) = _hx_wc_grid_3_15;
    jmi_array_ref_1(tmp_4, 4) = _hx_wc_grid_4_16;
    jmi_array_ref_1(tmp_4, 5) = _hx_wc_grid_5_17;
    jmi_array_ref_1(tmp_4, 6) = _hx_wc_grid_6_18;
    jmi_array_ref_1(tmp_4, 7) = _hx_wc_grid_7_19;
    jmi_array_ref_1(tmp_4, 8) = _hx_wc_grid_8_20;
    jmi_array_ref_1(tmp_4, 9) = _hx_wc_grid_9_21;
    jmi_array_ref_1(tmp_4, 10) = _hx_wc_grid_10_22;
    jmi_array_ref_1(tmp_4, 11) = _hx_wc_grid_11_23;
    JMI_ARRAY_STATIC_INIT_1(tmp_5, 10)
    jmi_array_ref_1(tmp_5, 1) = _hx_tc_grid_1_45;
    jmi_array_ref_1(tmp_5, 2) = _hx_tc_grid_2_46;
    jmi_array_ref_1(tmp_5, 3) = _hx_tc_grid_3_47;
    jmi_array_ref_1(tmp_5, 4) = _hx_tc_grid_4_48;
    jmi_array_ref_1(tmp_5, 5) = _hx_tc_grid_5_49;
    jmi_array_ref_1(tmp_5, 6) = _hx_tc_grid_6_50;
    jmi_array_ref_1(tmp_5, 7) = _hx_tc_grid_7_51;
    jmi_array_ref_1(tmp_5, 8) = _hx_tc_grid_8_52;
    jmi_array_ref_1(tmp_5, 9) = _hx_tc_grid_9_53;
    jmi_array_ref_1(tmp_5, 10) = _hx_tc_grid_10_54;
    JMI_ARRAY_STATIC_INIT_2(tmp_6, 11, 10)
    jmi_array_ref_2(tmp_6, 1,1) = _hx_hc_grid_1_1_55;
    jmi_array_ref_2(tmp_6, 1,2) = _hx_hc_grid_1_2_56;
    jmi_array_ref_2(tmp_6, 1,3) = _hx_hc_grid_1_3_57;
    jmi_array_ref_2(tmp_6, 1,4) = _hx_hc_grid_1_4_58;
    jmi_array_ref_2(tmp_6, 1,5) = _hx_hc_grid_1_5_59;
    jmi_array_ref_2(tmp_6, 1,6) = _hx_hc_grid_1_6_60;
    jmi_array_ref_2(tmp_6, 1,7) = _hx_hc_grid_1_7_61;
    jmi_array_ref_2(tmp_6, 1,8) = _hx_hc_grid_1_8_62;
    jmi_array_ref_2(tmp_6, 1,9) = _hx_hc_grid_1_9_63;
    jmi_array_ref_2(tmp_6, 1,10) = _hx_hc_grid_1_10_64;
    jmi_array_ref_2(tmp_6, 2,1) = _hx_hc_grid_2_1_65;
    jmi_array_ref_2(tmp_6, 2,2) = _hx_hc_grid_2_2_66;
    jmi_array_ref_2(tmp_6, 2,3) = _hx_hc_grid_2_3_67;
    jmi_array_ref_2(tmp_6, 2,4) = _hx_hc_grid_2_4_68;
    jmi_array_ref_2(tmp_6, 2,5) = _hx_hc_grid_2_5_69;
    jmi_array_ref_2(tmp_6, 2,6) = _hx_hc_grid_2_6_70;
    jmi_array_ref_2(tmp_6, 2,7) = _hx_hc_grid_2_7_71;
    jmi_array_ref_2(tmp_6, 2,8) = _hx_hc_grid_2_8_72;
    jmi_array_ref_2(tmp_6, 2,9) = _hx_hc_grid_2_9_73;
    jmi_array_ref_2(tmp_6, 2,10) = _hx_hc_grid_2_10_74;
    jmi_array_ref_2(tmp_6, 3,1) = _hx_hc_grid_3_1_75;
    jmi_array_ref_2(tmp_6, 3,2) = _hx_hc_grid_3_2_76;
    jmi_array_ref_2(tmp_6, 3,3) = _hx_hc_grid_3_3_77;
    jmi_array_ref_2(tmp_6, 3,4) = _hx_hc_grid_3_4_78;
    jmi_array_ref_2(tmp_6, 3,5) = _hx_hc_grid_3_5_79;
    jmi_array_ref_2(tmp_6, 3,6) = _hx_hc_grid_3_6_80;
    jmi_array_ref_2(tmp_6, 3,7) = _hx_hc_grid_3_7_81;
    jmi_array_ref_2(tmp_6, 3,8) = _hx_hc_grid_3_8_82;
    jmi_array_ref_2(tmp_6, 3,9) = _hx_hc_grid_3_9_83;
    jmi_array_ref_2(tmp_6, 3,10) = _hx_hc_grid_3_10_84;
    jmi_array_ref_2(tmp_6, 4,1) = _hx_hc_grid_4_1_85;
    jmi_array_ref_2(tmp_6, 4,2) = _hx_hc_grid_4_2_86;
    jmi_array_ref_2(tmp_6, 4,3) = _hx_hc_grid_4_3_87;
    jmi_array_ref_2(tmp_6, 4,4) = _hx_hc_grid_4_4_88;
    jmi_array_ref_2(tmp_6, 4,5) = _hx_hc_grid_4_5_89;
    jmi_array_ref_2(tmp_6, 4,6) = _hx_hc_grid_4_6_90;
    jmi_array_ref_2(tmp_6, 4,7) = _hx_hc_grid_4_7_91;
    jmi_array_ref_2(tmp_6, 4,8) = _hx_hc_grid_4_8_92;
    jmi_array_ref_2(tmp_6, 4,9) = _hx_hc_grid_4_9_93;
    jmi_array_ref_2(tmp_6, 4,10) = _hx_hc_grid_4_10_94;
    jmi_array_ref_2(tmp_6, 5,1) = _hx_hc_grid_5_1_95;
    jmi_array_ref_2(tmp_6, 5,2) = _hx_hc_grid_5_2_96;
    jmi_array_ref_2(tmp_6, 5,3) = _hx_hc_grid_5_3_97;
    jmi_array_ref_2(tmp_6, 5,4) = _hx_hc_grid_5_4_98;
    jmi_array_ref_2(tmp_6, 5,5) = _hx_hc_grid_5_5_99;
    jmi_array_ref_2(tmp_6, 5,6) = _hx_hc_grid_5_6_100;
    jmi_array_ref_2(tmp_6, 5,7) = _hx_hc_grid_5_7_101;
    jmi_array_ref_2(tmp_6, 5,8) = _hx_hc_grid_5_8_102;
    jmi_array_ref_2(tmp_6, 5,9) = _hx_hc_grid_5_9_103;
    jmi_array_ref_2(tmp_6, 5,10) = _hx_hc_grid_5_10_104;
    jmi_array_ref_2(tmp_6, 6,1) = _hx_hc_grid_6_1_105;
    jmi_array_ref_2(tmp_6, 6,2) = _hx_hc_grid_6_2_106;
    jmi_array_ref_2(tmp_6, 6,3) = _hx_hc_grid_6_3_107;
    jmi_array_ref_2(tmp_6, 6,4) = _hx_hc_grid_6_4_108;
    jmi_array_ref_2(tmp_6, 6,5) = _hx_hc_grid_6_5_109;
    jmi_array_ref_2(tmp_6, 6,6) = _hx_hc_grid_6_6_110;
    jmi_array_ref_2(tmp_6, 6,7) = _hx_hc_grid_6_7_111;
    jmi_array_ref_2(tmp_6, 6,8) = _hx_hc_grid_6_8_112;
    jmi_array_ref_2(tmp_6, 6,9) = _hx_hc_grid_6_9_113;
    jmi_array_ref_2(tmp_6, 6,10) = _hx_hc_grid_6_10_114;
    jmi_array_ref_2(tmp_6, 7,1) = _hx_hc_grid_7_1_115;
    jmi_array_ref_2(tmp_6, 7,2) = _hx_hc_grid_7_2_116;
    jmi_array_ref_2(tmp_6, 7,3) = _hx_hc_grid_7_3_117;
    jmi_array_ref_2(tmp_6, 7,4) = _hx_hc_grid_7_4_118;
    jmi_array_ref_2(tmp_6, 7,5) = _hx_hc_grid_7_5_119;
    jmi_array_ref_2(tmp_6, 7,6) = _hx_hc_grid_7_6_120;
    jmi_array_ref_2(tmp_6, 7,7) = _hx_hc_grid_7_7_121;
    jmi_array_ref_2(tmp_6, 7,8) = _hx_hc_grid_7_8_122;
    jmi_array_ref_2(tmp_6, 7,9) = _hx_hc_grid_7_9_123;
    jmi_array_ref_2(tmp_6, 7,10) = _hx_hc_grid_7_10_124;
    jmi_array_ref_2(tmp_6, 8,1) = _hx_hc_grid_8_1_125;
    jmi_array_ref_2(tmp_6, 8,2) = _hx_hc_grid_8_2_126;
    jmi_array_ref_2(tmp_6, 8,3) = _hx_hc_grid_8_3_127;
    jmi_array_ref_2(tmp_6, 8,4) = _hx_hc_grid_8_4_128;
    jmi_array_ref_2(tmp_6, 8,5) = _hx_hc_grid_8_5_129;
    jmi_array_ref_2(tmp_6, 8,6) = _hx_hc_grid_8_6_130;
    jmi_array_ref_2(tmp_6, 8,7) = _hx_hc_grid_8_7_131;
    jmi_array_ref_2(tmp_6, 8,8) = _hx_hc_grid_8_8_132;
    jmi_array_ref_2(tmp_6, 8,9) = _hx_hc_grid_8_9_133;
    jmi_array_ref_2(tmp_6, 8,10) = _hx_hc_grid_8_10_134;
    jmi_array_ref_2(tmp_6, 9,1) = _hx_hc_grid_9_1_135;
    jmi_array_ref_2(tmp_6, 9,2) = _hx_hc_grid_9_2_136;
    jmi_array_ref_2(tmp_6, 9,3) = _hx_hc_grid_9_3_137;
    jmi_array_ref_2(tmp_6, 9,4) = _hx_hc_grid_9_4_138;
    jmi_array_ref_2(tmp_6, 9,5) = _hx_hc_grid_9_5_139;
    jmi_array_ref_2(tmp_6, 9,6) = _hx_hc_grid_9_6_140;
    jmi_array_ref_2(tmp_6, 9,7) = _hx_hc_grid_9_7_141;
    jmi_array_ref_2(tmp_6, 9,8) = _hx_hc_grid_9_8_142;
    jmi_array_ref_2(tmp_6, 9,9) = _hx_hc_grid_9_9_143;
    jmi_array_ref_2(tmp_6, 9,10) = _hx_hc_grid_9_10_144;
    jmi_array_ref_2(tmp_6, 10,1) = _hx_hc_grid_10_1_145;
    jmi_array_ref_2(tmp_6, 10,2) = _hx_hc_grid_10_2_146;
    jmi_array_ref_2(tmp_6, 10,3) = _hx_hc_grid_10_3_147;
    jmi_array_ref_2(tmp_6, 10,4) = _hx_hc_grid_10_4_148;
    jmi_array_ref_2(tmp_6, 10,5) = _hx_hc_grid_10_5_149;
    jmi_array_ref_2(tmp_6, 10,6) = _hx_hc_grid_10_6_150;
    jmi_array_ref_2(tmp_6, 10,7) = _hx_hc_grid_10_7_151;
    jmi_array_ref_2(tmp_6, 10,8) = _hx_hc_grid_10_8_152;
    jmi_array_ref_2(tmp_6, 10,9) = _hx_hc_grid_10_9_153;
    jmi_array_ref_2(tmp_6, 10,10) = _hx_hc_grid_10_10_154;
    jmi_array_ref_2(tmp_6, 11,1) = _hx_hc_grid_11_1_155;
    jmi_array_ref_2(tmp_6, 11,2) = _hx_hc_grid_11_2_156;
    jmi_array_ref_2(tmp_6, 11,3) = _hx_hc_grid_11_3_157;
    jmi_array_ref_2(tmp_6, 11,4) = _hx_hc_grid_11_4_158;
    jmi_array_ref_2(tmp_6, 11,5) = _hx_hc_grid_11_5_159;
    jmi_array_ref_2(tmp_6, 11,6) = _hx_hc_grid_11_6_160;
    jmi_array_ref_2(tmp_6, 11,7) = _hx_hc_grid_11_7_161;
    jmi_array_ref_2(tmp_6, 11,8) = _hx_hc_grid_11_8_162;
    jmi_array_ref_2(tmp_6, 11,9) = _hx_hc_grid_11_9_163;
    jmi_array_ref_2(tmp_6, 11,10) = _hx_hc_grid_11_10_164;
    _hx_hc_277 = (func_AMS_LinearMap_exp(tmp_4, tmp_5, tmp_6, 4.0, _ambientair_t_env_11));

        return 0;
}

static int model_init_R0(jmi_t* jmi, jmi_real_t** res) {

	return 0;
}

int jmi_new(jmi_t** jmi, jmi_callbacks_t* jmi_callbacks) {

  jmi_init(jmi, N_real_ci, N_real_cd, N_real_pi, N_real_pd,
	   N_integer_ci, N_integer_cd, N_integer_pi, N_integer_pd,
	   N_boolean_ci, N_boolean_cd, N_boolean_pi, N_boolean_pd,
	   N_string_ci, N_string_cd, N_string_pi, N_string_pd,
	   N_real_dx,N_real_x, N_real_u, N_real_w,
	   N_real_d,N_integer_d,N_integer_u,N_boolean_d,N_boolean_u,
	   N_string_d,N_string_u, N_outputs,(int (*))Output_vrefs,
           N_sw,N_sw_init,N_guards,N_guards_init,
	   N_dae_blocks,N_dae_init_blocks,
	   N_initial_relations, (int (*))DAE_initial_relations,
	   N_relations, (int (*))DAE_relations,
	   Scaling_method, N_ext_objs, jmi_callbacks);

      jmi_dae_add_equation_block(*jmi, dae_block_0, NULL, 1, 0, JMI_CONTINUOUS_VARIABILITY, JMI_LINEAR_SOLVER, 0);
    jmi_dae_add_equation_block(*jmi, dae_block_1, NULL, 1, 0, JMI_CONTINUOUS_VARIABILITY, JMI_LINEAR_SOLVER, 1);


      jmi_dae_init_add_equation_block(*jmi, dae_init_block_0, NULL, 1, 0, JMI_CONTINUOUS_VARIABILITY, JMI_LINEAR_SOLVER, 0);
    jmi_dae_init_add_equation_block(*jmi, dae_init_block_1, NULL, 1, 0, JMI_CONTINUOUS_VARIABILITY, JMI_LINEAR_SOLVER, 1);


  

  

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

	return 0;
}

int jmi_terminate(jmi_t* jmi) {

	return 0;
}

int jmi_set_start_values(jmi_t* jmi) {
    _valve1_C_2 = (0.155);
    _valve2_C_9 = (0.1);
    _W4_298 = (4);
    _inlet_p_env_0 = (259932);
    _inlet_t_env_1 = (350);
    _fork_v_3 = (0.004916);
    _ambientair_p_env_10 = (50000);
    _ambientair_t_env_11 = (220);
    _hx_m_12 = (13.61);
    _hx_wc_grid_1_13 = (0.76);
    _hx_wc_grid_2_14 = (1.51);
    _hx_wc_grid_3_15 = (2.27);
    _hx_wc_grid_4_16 = (3.02);
    _hx_wc_grid_5_17 = (3.78);
    _hx_wc_grid_6_18 = (4.54);
    _hx_wc_grid_7_19 = (5.29);
    _hx_wc_grid_8_20 = (6.05);
    _hx_wc_grid_9_21 = (6.8);
    _hx_wc_grid_10_22 = (7.56);
    _hx_wc_grid_11_23 = (8.32);
    _hx_wh_grid_1_24 = (0.19);
    _hx_wh_grid_2_25 = (0.38);
    _hx_wh_grid_3_26 = (0.57);
    _hx_wh_grid_4_27 = (0.76);
    _hx_wh_grid_5_28 = (0.95);
    _hx_wh_grid_6_29 = (1.13);
    _hx_wh_grid_7_30 = (1.32);
    _hx_wh_grid_8_31 = (1.51);
    _hx_wh_grid_9_32 = (1.7);
    _hx_wh_grid_10_33 = (1.89);
    _hx_wh_grid_11_34 = (2.08);
    _hx_th_grid_1_35 = (200.0);
    _hx_th_grid_2_36 = (216.67);
    _hx_th_grid_3_37 = (233.33);
    _hx_th_grid_4_38 = (250.0);
    _hx_th_grid_5_39 = (266.67);
    _hx_th_grid_6_40 = (283.33);
    _hx_th_grid_7_41 = (300.0);
    _hx_th_grid_8_42 = (316.67);
    _hx_th_grid_9_43 = (333.33);
    _hx_th_grid_10_44 = (366.67);
    _hx_tc_grid_1_45 = (200.0);
    _hx_tc_grid_2_46 = (216.67);
    _hx_tc_grid_3_47 = (233.33);
    _hx_tc_grid_4_48 = (250.0);
    _hx_tc_grid_5_49 = (266.67);
    _hx_tc_grid_6_50 = (283.33);
    _hx_tc_grid_7_51 = (300.0);
    _hx_tc_grid_8_52 = (316.67);
    _hx_tc_grid_9_53 = (333.33);
    _hx_tc_grid_10_54 = (366.67);
    _hx_hc_grid_1_1_55 = (5.08);
    _hx_hc_grid_1_2_56 = (5.12);
    _hx_hc_grid_1_3_57 = (5.2);
    _hx_hc_grid_1_4_58 = (5.28);
    _hx_hc_grid_1_5_59 = (5.37);
    _hx_hc_grid_1_6_60 = (5.45);
    _hx_hc_grid_1_7_61 = (5.53);
    _hx_hc_grid_1_8_62 = (5.61);
    _hx_hc_grid_1_9_63 = (5.65);
    _hx_hc_grid_1_10_64 = (5.81);
    _hx_hc_grid_2_1_65 = (8.17);
    _hx_hc_grid_2_2_66 = (8.25);
    _hx_hc_grid_2_3_67 = (8.37);
    _hx_hc_grid_2_4_68 = (8.54);
    _hx_hc_grid_2_5_69 = (8.66);
    _hx_hc_grid_2_6_70 = (8.82);
    _hx_hc_grid_2_7_71 = (8.94);
    _hx_hc_grid_2_8_72 = (9.06);
    _hx_hc_grid_2_9_73 = (9.15);
    _hx_hc_grid_2_10_74 = (9.35);
    _hx_hc_grid_3_1_75 = (10.61);
    _hx_hc_grid_3_2_76 = (10.69);
    _hx_hc_grid_3_3_77 = (10.85);
    _hx_hc_grid_3_4_78 = (11.06);
    _hx_hc_grid_3_5_79 = (11.22);
    _hx_hc_grid_3_6_80 = (11.38);
    _hx_hc_grid_3_7_81 = (11.59);
    _hx_hc_grid_3_8_82 = (11.75);
    _hx_hc_grid_3_9_83 = (11.91);
    _hx_hc_grid_3_10_84 = (12.15);
    _hx_hc_grid_4_1_85 = (12.52);
    _hx_hc_grid_4_2_86 = (12.68);
    _hx_hc_grid_4_3_87 = (12.89);
    _hx_hc_grid_4_4_88 = (13.13);
    _hx_hc_grid_4_5_89 = (13.37);
    _hx_hc_grid_4_6_90 = (13.62);
    _hx_hc_grid_4_7_91 = (13.82);
    _hx_hc_grid_4_8_92 = (14.02);
    _hx_hc_grid_4_9_93 = (14.19);
    _hx_hc_grid_4_10_94 = (14.51);
    _hx_hc_grid_5_1_95 = (14.15);
    _hx_hc_grid_5_2_96 = (14.35);
    _hx_hc_grid_5_3_97 = (14.59);
    _hx_hc_grid_5_4_98 = (14.88);
    _hx_hc_grid_5_5_99 = (15.12);
    _hx_hc_grid_5_6_100 = (15.41);
    _hx_hc_grid_5_7_101 = (15.65);
    _hx_hc_grid_5_8_102 = (15.89);
    _hx_hc_grid_5_9_103 = (16.14);
    _hx_hc_grid_5_10_104 = (16.54);
    _hx_hc_grid_6_1_105 = (15.65);
    _hx_hc_grid_6_2_106 = (15.85);
    _hx_hc_grid_6_3_107 = (16.1);
    _hx_hc_grid_6_4_108 = (16.42);
    _hx_hc_grid_6_5_109 = (16.71);
    _hx_hc_grid_6_6_110 = (16.99);
    _hx_hc_grid_6_7_111 = (17.28);
    _hx_hc_grid_6_8_112 = (17.52);
    _hx_hc_grid_6_9_113 = (17.8);
    _hx_hc_grid_6_10_114 = (18.25);
    _hx_hc_grid_7_1_115 = (16.95);
    _hx_hc_grid_7_2_116 = (17.19);
    _hx_hc_grid_7_3_117 = (17.48);
    _hx_hc_grid_7_4_118 = (17.8);
    _hx_hc_grid_7_5_119 = (18.13);
    _hx_hc_grid_7_6_120 = (18.41);
    _hx_hc_grid_7_7_121 = (18.74);
    _hx_hc_grid_7_8_122 = (19.02);
    _hx_hc_grid_7_9_123 = (19.27);
    _hx_hc_grid_7_10_124 = (19.76);
    _hx_hc_grid_8_1_125 = (18.13);
    _hx_hc_grid_8_2_126 = (18.37);
    _hx_hc_grid_8_3_127 = (18.7);
    _hx_hc_grid_8_4_128 = (19.06);
    _hx_hc_grid_8_5_129 = (19.43);
    _hx_hc_grid_8_6_130 = (19.76);
    _hx_hc_grid_8_7_131 = (20.08);
    _hx_hc_grid_8_8_132 = (20.37);
    _hx_hc_grid_8_9_133 = (20.65);
    _hx_hc_grid_8_10_134 = (21.18);
    _hx_hc_grid_9_1_135 = (19.23);
    _hx_hc_grid_9_2_136 = (19.47);
    _hx_hc_grid_9_3_137 = (19.84);
    _hx_hc_grid_9_4_138 = (20.2);
    _hx_hc_grid_9_5_139 = (20.57);
    _hx_hc_grid_9_6_140 = (20.93);
    _hx_hc_grid_9_7_141 = (21.3);
    _hx_hc_grid_9_8_142 = (21.63);
    _hx_hc_grid_9_9_143 = (21.95);
    _hx_hc_grid_9_10_144 = (22.48);
    _hx_hc_grid_10_1_145 = (20.28);
    _hx_hc_grid_10_2_146 = (20.53);
    _hx_hc_grid_10_3_147 = (20.89);
    _hx_hc_grid_10_4_148 = (21.3);
    _hx_hc_grid_10_5_149 = (21.67);
    _hx_hc_grid_10_6_150 = (22.03);
    _hx_hc_grid_10_7_151 = (22.4);
    _hx_hc_grid_10_8_152 = (22.76);
    _hx_hc_grid_10_9_153 = (23.09);
    _hx_hc_grid_10_10_154 = (23.7);
    _hx_hc_grid_11_1_155 = (20.28);
    _hx_hc_grid_11_2_156 = (20.53);
    _hx_hc_grid_11_3_157 = (20.89);
    _hx_hc_grid_11_4_158 = (21.3);
    _hx_hc_grid_11_5_159 = (21.67);
    _hx_hc_grid_11_6_160 = (22.03);
    _hx_hc_grid_11_7_161 = (22.4);
    _hx_hc_grid_11_8_162 = (22.76);
    _hx_hc_grid_11_9_163 = (23.09);
    _hx_hc_grid_11_10_164 = (23.7);
    _hx_hh_grid_1_1_165 = (3.25);
    _hx_hh_grid_1_2_166 = (3.66);
    _hx_hh_grid_1_3_167 = (3.66);
    _hx_hh_grid_1_4_168 = (3.66);
    _hx_hh_grid_1_5_169 = (3.66);
    _hx_hh_grid_1_6_170 = (3.66);
    _hx_hh_grid_1_7_171 = (3.66);
    _hx_hh_grid_1_8_172 = (3.66);
    _hx_hh_grid_1_9_173 = (3.66);
    _hx_hh_grid_1_10_174 = (4.07);
    _hx_hh_grid_2_1_175 = (5.69);
    _hx_hh_grid_2_2_176 = (5.69);
    _hx_hh_grid_2_3_177 = (5.69);
    _hx_hh_grid_2_4_178 = (6.1);
    _hx_hh_grid_2_5_179 = (6.1);
    _hx_hh_grid_2_6_180 = (6.1);
    _hx_hh_grid_2_7_181 = (6.1);
    _hx_hh_grid_2_8_182 = (6.5);
    _hx_hh_grid_2_9_183 = (6.5);
    _hx_hh_grid_2_10_184 = (6.5);
    _hx_hh_grid_3_1_185 = (7.72);
    _hx_hh_grid_3_2_186 = (7.72);
    _hx_hh_grid_3_3_187 = (7.72);
    _hx_hh_grid_3_4_188 = (7.72);
    _hx_hh_grid_3_5_189 = (8.13);
    _hx_hh_grid_3_6_190 = (8.13);
    _hx_hh_grid_3_7_191 = (8.13);
    _hx_hh_grid_3_8_192 = (8.54);
    _hx_hh_grid_3_9_193 = (8.54);
    _hx_hh_grid_3_10_194 = (8.94);
    _hx_hh_grid_4_1_195 = (8.94);
    _hx_hh_grid_4_2_196 = (9.35);
    _hx_hh_grid_4_3_197 = (9.35);
    _hx_hh_grid_4_4_198 = (9.76);
    _hx_hh_grid_4_5_199 = (9.76);
    _hx_hh_grid_4_6_200 = (9.76);
    _hx_hh_grid_4_7_201 = (10.16);
    _hx_hh_grid_4_8_202 = (10.16);
    _hx_hh_grid_4_9_203 = (10.57);
    _hx_hh_grid_4_10_204 = (10.57);
    _hx_hh_grid_5_1_205 = (10.57);
    _hx_hh_grid_5_2_206 = (10.57);
    _hx_hh_grid_5_3_207 = (10.57);
    _hx_hh_grid_5_4_208 = (10.98);
    _hx_hh_grid_5_5_209 = (11.38);
    _hx_hh_grid_5_6_210 = (11.38);
    _hx_hh_grid_5_7_211 = (11.79);
    _hx_hh_grid_5_8_212 = (11.79);
    _hx_hh_grid_5_9_213 = (12.2);
    _hx_hh_grid_5_10_214 = (12.2);
    _hx_hh_grid_6_1_215 = (11.79);
    _hx_hh_grid_6_2_216 = (11.79);
    _hx_hh_grid_6_3_217 = (12.2);
    _hx_hh_grid_6_4_218 = (12.2);
    _hx_hh_grid_6_5_219 = (12.6);
    _hx_hh_grid_6_6_220 = (12.6);
    _hx_hh_grid_6_7_221 = (13.01);
    _hx_hh_grid_6_8_222 = (13.41);
    _hx_hh_grid_6_9_223 = (13.41);
    _hx_hh_grid_6_10_224 = (13.82);
    _hx_hh_grid_7_1_225 = (12.6);
    _hx_hh_grid_7_2_226 = (13.01);
    _hx_hh_grid_7_3_227 = (13.01);
    _hx_hh_grid_7_4_228 = (13.41);
    _hx_hh_grid_7_5_229 = (13.82);
    _hx_hh_grid_7_6_230 = (13.82);
    _hx_hh_grid_7_7_231 = (14.23);
    _hx_hh_grid_7_8_232 = (14.63);
    _hx_hh_grid_7_9_233 = (14.63);
    _hx_hh_grid_7_10_234 = (15.04);
    _hx_hh_grid_8_1_235 = (13.82);
    _hx_hh_grid_8_2_236 = (13.82);
    _hx_hh_grid_8_3_237 = (14.23);
    _hx_hh_grid_8_4_238 = (14.63);
    _hx_hh_grid_8_5_239 = (15.04);
    _hx_hh_grid_8_6_240 = (15.04);
    _hx_hh_grid_8_7_241 = (15.45);
    _hx_hh_grid_8_8_242 = (15.85);
    _hx_hh_grid_8_9_243 = (15.85);
    _hx_hh_grid_8_10_244 = (16.26);
    _hx_hh_grid_9_1_245 = (14.63);
    _hx_hh_grid_9_2_246 = (15.04);
    _hx_hh_grid_9_3_247 = (15.04);
    _hx_hh_grid_9_4_248 = (15.45);
    _hx_hh_grid_9_5_249 = (15.85);
    _hx_hh_grid_9_6_250 = (16.26);
    _hx_hh_grid_9_7_251 = (16.67);
    _hx_hh_grid_9_8_252 = (16.67);
    _hx_hh_grid_9_9_253 = (17.07);
    _hx_hh_grid_9_10_254 = (17.48);
    _hx_hh_grid_10_1_255 = (15.45);
    _hx_hh_grid_10_2_256 = (15.85);
    _hx_hh_grid_10_3_257 = (16.26);
    _hx_hh_grid_10_4_258 = (16.67);
    _hx_hh_grid_10_5_259 = (16.67);
    _hx_hh_grid_10_6_260 = (17.07);
    _hx_hh_grid_10_7_261 = (17.48);
    _hx_hh_grid_10_8_262 = (17.89);
    _hx_hh_grid_10_9_263 = (18.29);
    _hx_hh_grid_10_10_264 = (18.7);
    _hx_hh_grid_11_1_265 = (15.45);
    _hx_hh_grid_11_2_266 = (15.85);
    _hx_hh_grid_11_3_267 = (16.26);
    _hx_hh_grid_11_4_268 = (16.67);
    _hx_hh_grid_11_5_269 = (16.67);
    _hx_hh_grid_11_6_270 = (17.07);
    _hx_hh_grid_11_7_271 = (17.48);
    _hx_hh_grid_11_8_272 = (17.89);
    _hx_hh_grid_11_9_273 = (18.29);
    _hx_hh_grid_11_10_274 = (18.7);
    _hx_Ahx_275 = (0.00161);
    _hx_Cmetal_276 = (837.3);
    _cabin_v_284 = (141.58);
    _cabin_p_285 = (75152);
    _cabin_Qpass_286 = (90);
    _cabin_passenger_287 = (200);
    _cabin_dQ_288 = (-8792);
    _fan_wf_293 = (0.3024);
    __block_jacobian_check_306 = (JMI_FALSE);
    __block_jacobian_check_tol_307 = (1.0E-6);
    __block_solver_experimental_mode_308 = (0);
    __cs_rel_tol_309 = (1.0E-6);
    __cs_solver_310 = (0);
    __cs_step_size_311 = (0.001);
    __enforce_bounds_312 = (JMI_FALSE);
    __events_default_tol_313 = (1.0E-10);
    __events_tol_factor_314 = (1.0E-4);
    __iteration_variable_scaling_315 = (1);
    __log_level_316 = (3);
    __nle_solver_check_jac_cond_317 = (JMI_FALSE);
    __nle_solver_default_tol_318 = (1.0E-10);
    __nle_solver_log_level_319 = (0);
    __nle_solver_max_iter_320 = (100);
    __nle_solver_min_tol_321 = (1.0E-12);
    __nle_solver_tol_factor_322 = (1.0E-4);
    __rescale_after_singular_jac_323 = (JMI_TRUE);
    __rescale_each_step_324 = (JMI_FALSE);
    __residual_equation_scaling_325 = (1);
    __runtime_log_to_file_326 = (JMI_FALSE);
    __use_Brent_in_1d_327 = (JMI_FALSE);
    __use_jacobian_equilibration_328 = (JMI_FALSE);
    model_init_eval_parameters(jmi);
    _fork_outport_w_4 = (0.0);
    _fork_p_5 = (80000);
    _fork_t_6 = (350);
    _fork_m_7 = (0.0);
    _fork_Q_8 = (0.0);
    _hx_hh_278 = (0.0);
    _hx_t_279 = (250);
    _hx_Q_280 = (0.0);
    _hx_rhoh_281 = (0.0);
    _hx_qh_282 = (0.0);
    _hx_qc_283 = (0.0);
    _cabin_t_289 = (297.2);
    _cabin_m_290 = (0.0);
    _cabin_Q_291 = (0.0);
    _cabin_wa_292 = (0.0);
    _W1_294 = (0.0);
    _W31_295 = (0.0);
    _W32_296 = (0.0);
    _W33_297 = (0.0);
    _T6_299 = (288.15);
    _T8_300 = (288.15);
    _W8_301 = (0.0);
    _der_fork_m_302 = (0.0);
    _der_fork_Q_303 = (0.0);
    _der_hx_Q_304 = (0.0);
    _der_cabin_m_305 = (0.0);

    return 0;
}

const char *jmi_get_model_identifier() { return "AMS_AMSSim"; }


void _emit(log_t *log, char* message) { fmi1_me_emit(log, message); }
void create_log_file_if_needed(log_t *log) { fmi1_me_create_log_file_if_needed(log); }
BOOL emitted_category(log_t *log, category_t category) { fmi1_me_emitted_category(log, category); }

/* FMI for co-simulation Functions*/

/* Inquire version numbers of header files */
DllExport const char* fmiGetTypesPlatform() {
    return fmi1_cs_get_types_platform();
}
DllExport const char* fmiGetVersion() {
    return fmi1_cs_get_version();
}

DllExport void fmiFreeSlaveInstance(fmiComponent c) {
    fmi1_cs_free_slave_instance(c);
}

/* Creation and destruction of model instances and setting debug status */
DllExport fmiComponent fmiInstantiateSlave(fmiString instanceName, fmiString GUID, fmiString fmuLocation, fmiString mimeType, 
                                   fmiReal timeout, fmiBoolean visible, fmiBoolean interactive, fmiCallbackFunctions functions, 
                                   fmiBoolean loggingOn) {
    return fmi1_cs_instantiate_slave(instanceName, GUID, fmuLocation, mimeType, timeout, visible, interactive, functions, loggingOn);
}


DllExport fmiStatus fmiTerminateSlave(fmiComponent c) {
    return fmi1_cs_terminate_slave(c);
}

DllExport fmiStatus fmiInitializeSlave(fmiComponent c, fmiReal tStart,
                                    fmiBoolean StopTimeDefined, fmiReal tStop){
    return fmi1_cs_initialize_slave(c,tStart,StopTimeDefined,tStop);
}

DllExport fmiStatus fmiSetDebugLogging(fmiComponent c, fmiBoolean loggingOn) {
    return fmi1_cs_set_debug_logging(c, loggingOn);
}

DllExport fmiStatus fmiDoStep(fmiComponent c,
			      fmiReal      currentCommunicationPoint,
			      fmiReal      communicationStepSize,
			      fmiBoolean   newStep) {
  return fmi1_cs_do_step(c, currentCommunicationPoint, communicationStepSize, newStep);
}

DllExport fmiStatus fmiCancelStep(fmiComponent c){
    return fmi1_cs_cancel_step(c);
}

DllExport fmiStatus fmiResetSlave(fmiComponent c) {
    return fmi1_cs_reset_slave(c);
}

DllExport fmiStatus fmiGetRealOutputDerivatives(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiInteger order[], fmiReal value[]){
    return fmi1_cs_get_real_output_derivatives(c, vr, nvr, order, value);
}

DllExport fmiStatus fmiSetRealInputDerivatives(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiInteger order[], const fmiReal value[]){
    return fmi1_cs_set_real_input_derivatives(c,vr,nvr,order,value);
}

DllExport fmiStatus fmiSetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]) {
    return fmi1_cs_set_real(c, vr, nvr, value);
}

DllExport fmiStatus fmiSetInteger(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiInteger value[]) {
    return fmi1_cs_set_integer(c, vr, nvr, value);
}

DllExport fmiStatus fmiSetBoolean(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiBoolean value[]) {
    return fmi1_cs_set_boolean(c, vr, nvr, value);
}

DllExport fmiStatus fmiSetString(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiString  value[]) {
    return fmi1_cs_set_string(c, vr, nvr, value);
}

DllExport fmiStatus fmiGetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[]) {
    return fmi1_cs_get_real(c, vr, nvr, value);
}

DllExport fmiStatus fmiGetInteger(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiInteger value[]) {
    return fmi1_cs_get_integer(c, vr, nvr, value);
}

DllExport fmiStatus fmiGetBoolean(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiBoolean value[]) {
    return fmi1_cs_get_boolean(c, vr, nvr, value);
}

DllExport fmiStatus fmiGetString(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiString  value[]) {
    return fmi1_cs_get_string(c, vr, nvr, value);
}

DllExport fmiStatus fmiGetStatus(fmiComponent c, const fmiStatusKind s, fmiStatus* value){
    return fmi1_cs_get_status(c,s,value);
}

DllExport fmiStatus fmiGetRealStatus(fmiComponent c, const fmiStatusKind s, fmiReal* value){
    return fmi1_cs_get_real_status(c, s, value);
}

DllExport fmiStatus fmiGetIntegerStatus(fmiComponent c, const fmiStatusKind s, fmiInteger* value){
    return fmi1_cs_get_integer_status(c, s, value);
}

DllExport fmiStatus fmiGetBooleanStatus(fmiComponent c, const fmiStatusKind s, fmiBoolean* value){
    return fmi1_cs_get_boolean_status(c, s, value);
}

DllExport fmiStatus fmiGetStringStatus(fmiComponent c, const fmiStatusKind s, fmiString* value){
    return fmi1_cs_get_string_status(c,s,value);
}

/* NOTE IN THE FILE FMICSFUNCTIONS.H WHY? */
/*
DLLExport fmiStatus fmiSaveState(fmiComponent c, size_t index){
    return fmi_save_state(c,index);
}

DLLExport fmiStatus fmiRestoreState(fmiComponent c, size_t index){
    return fmi_restore_state(c,index);
}
*/
