#Ptolemy VHDL code generation core library compile script
#To be used with modelsim
#Author: Vinayak Nagpal

set CORE_LIB_PATH /users/vnagpal/core_lib
set CORE_IFACE_XML_PATH $CORE_LIB_PATH/xml

vlib work
vlib ieee_proposed

#IEEE_proposed

vcom -2002 -work ieee_proposed $CORE_LIB_PATH/vhdl/pkg/ieee_proposed/standard_additions_c.vhdl
vcom -2002 -work ieee_proposed $CORE_LIB_PATH/vhdl/pkg/ieee_proposed/standard_textio_additions_c.vhdl
vcom -2002 -work ieee_proposed $CORE_LIB_PATH/vhdl/pkg/ieee_proposed/std_logic_1164_additions.vhdl
vcom -2002 -work ieee_proposed $CORE_LIB_PATH/vhdl/pkg/ieee_proposed/numeric_std_additions.vhdl
vcom -2002 -work ieee_proposed $CORE_LIB_PATH/vhdl/pkg/ieee_proposed/env_c.vhdl
vcom -2002 -work ieee_proposed $CORE_LIB_PATH/vhdl/pkg/ieee_proposed/math_utility_pkg.vhdl
vcom -2002 -work ieee_proposed $CORE_LIB_PATH/vhdl/pkg/ieee_proposed/fixed_pkg_c.vhdl
vcom -2002 -work ieee_proposed $CORE_LIB_PATH/vhdl/pkg/ieee_proposed/float_pkg_c.vhdl
vcom -2002 -work ieee_proposed $CORE_LIB_PATH/vhdl/pkg/ieee_proposed/numeric_std_unsigned_c.vhdl

#Core Library packages
vcom -2002 -work work $CORE_LIB_PATH/vhdl/pkg/pt_utility/pt_utility.vhd

#Core Library Entities

#ptregister
set ENTCLASS ptregister
set ENTNAME ptregister
set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$ENTCLASS/$ENTNAME
append VHD_PATH ".vhd"
set XML_PATH $CORE_IFACE_XML_PATH/$ENTCLASS/$ENTNAME
append XML_PATH ".xml"
vcom -2002 -work work $VHD_PATH
vcom -2002 -gen_xml $ENTNAME $XML_PATH $VHD_PATH


#ptregister_async_reset
set ENTCLASS ptregister
set ENTNAME ptregister_async_reset
set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$ENTCLASS/$ENTNAME
append VHD_PATH ".vhd"
set XML_PATH $CORE_IFACE_XML_PATH/$ENTCLASS/$ENTNAME
append XML_PATH ".xml"
vcom -2002 -work work $VHD_PATH
vcom -2002 -gen_xml $ENTNAME $XML_PATH $VHD_PATH

#ptdelay
set ENTCLASS ptdelay
set ENTNAME ptdelay
set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$ENTCLASS/$ENTNAME
append VHD_PATH ".vhd"
set XML_PATH $CORE_IFACE_XML_PATH/$ENTCLASS/$ENTNAME
append XML_PATH ".xml"
vcom -2002 -work work $VHD_PATH
vcom -2002 -gen_xml $ENTNAME $XML_PATH $VHD_PATH


#pt_sfixed_add2
set ENTCLASS pt_sfixed_add2
set ENTNAME pt_sfixed_add2
set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$ENTCLASS/$ENTNAME
append VHD_PATH ".vhd"
set XML_PATH $CORE_IFACE_XML_PATH/$ENTCLASS/$ENTNAME
append XML_PATH ".xml"
vcom -2002 -work work $VHD_PATH
vcom -2002 -gen_xml $ENTNAME $XML_PATH $VHD_PATH

#pt_sfixed_add2_lat0
set ENTCLASS pt_sfixed_add2
set ENTNAME pt_sfixed_add2_lat0
set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$ENTCLASS/$ENTNAME
append VHD_PATH ".vhd"
set XML_PATH $CORE_IFACE_XML_PATH/$ENTCLASS/$ENTNAME
append XML_PATH ".xml"
vcom -2002 -work work $VHD_PATH
vcom -2002 -gen_xml $ENTNAME $XML_PATH $VHD_PATH


#pt_sfixed_sub2
set ENTCLASS pt_sfixed_sub2
set ENTNAME pt_sfixed_sub2
set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$ENTCLASS/$ENTNAME
append VHD_PATH ".vhd"
set XML_PATH $CORE_IFACE_XML_PATH/$ENTCLASS/$ENTNAME
append XML_PATH ".xml"
vcom -2002 -work work $VHD_PATH
vcom -2002 -gen_xml $ENTNAME $XML_PATH $VHD_PATH


#pt_sfixed_sub2_lat0
set ENTCLASS pt_sfixed_sub2
set ENTNAME pt_sfixed_sub2_lat0
set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$ENTCLASS/$ENTNAME
append VHD_PATH ".vhd"
set XML_PATH $CORE_IFACE_XML_PATH/$ENTCLASS/$ENTNAME
append XML_PATH ".xml"
vcom -2002 -work work $VHD_PATH
vcom -2002 -gen_xml $ENTNAME $XML_PATH $VHD_PATH


#pt_ufixed_add2
set ENTCLASS pt_ufixed_add2
set ENTNAME pt_ufixed_add2
set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$ENTCLASS/$ENTNAME
append VHD_PATH ".vhd"
set XML_PATH $CORE_IFACE_XML_PATH/$ENTCLASS/$ENTNAME
append XML_PATH ".xml"
vcom -2002 -work work $VHD_PATH
vcom -2002 -gen_xml $ENTNAME $XML_PATH $VHD_PATH


#pt_ufixed_add2_lat0
set ENTCLASS pt_ufixed_add2
set ENTNAME pt_ufixed_add2_lat0
set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$ENTCLASS/$ENTNAME
append VHD_PATH ".vhd"
set XML_PATH $CORE_IFACE_XML_PATH/$ENTCLASS/$ENTNAME
append XML_PATH ".xml"
vcom -2002 -work work $VHD_PATH
vcom -2002 -gen_xml $ENTNAME $XML_PATH $VHD_PATH


#pt_ufixed_sub2
set ENTCLASS pt_ufixed_sub2
set ENTNAME pt_ufixed_sub2
set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$ENTCLASS/$ENTNAME
append VHD_PATH ".vhd"
set XML_PATH $CORE_IFACE_XML_PATH/$ENTCLASS/$ENTNAME
append XML_PATH ".xml"
vcom -2002 -work work $VHD_PATH
vcom -2002 -gen_xml $ENTNAME $XML_PATH $VHD_PATH
