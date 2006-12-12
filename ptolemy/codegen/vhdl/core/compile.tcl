#Ptolemy VHDL code generation core library compile script
#To be used with modelsim
#Author: Vinayak Nagpal

set CORE_LIB_PATH $PTII/ptolemy/codegen/vhdl/core/
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

array set entity_class {
	ptregister ptregister
 	ptregister_async_reset ptregister
 	ptregister_sync_reset ptregister
	pt_sfixed_add2 pt_sfixed_add2
	pt_sfixed_add2_lat0 pt_sfixed_add2
	pt_sfixed_sub2 pt_sfixed_sub2
	pt_sfixed_sub2_lat0 pt_sfixed_sub2
	pt_ufixed_add2 pt_ufixed_add2
	pt_ufixed_add2_lat0 pt_ufixed_add2
	pt_ufixed_sub2_lat0 pt_ufixed_sub2
	pt_ufixed_sub2 pt_ufixed_sub2
	pt_sfixed_const pt_sfixed_const
	pt_ufixed_const pt_ufixed_const
	pt_sfixed_compare pt_sfixed_compare
	pt_sfixed_compare_lat0 pt_sfixed_compare
	pt_ufixed_compare pt_ufixed_compare
	pt_ufixed_compare_lat0 pt_ufixed_compare
	ptmux2 ptmux2
	ptmux2_lat0 ptmux2
	ptcounter ptcounter
	ptlogic ptlogic
	ptlogic_lat0 ptlogic
}

array set entity1_class {
	ptdelay ptdelay
}

array set entity_tb_class {
	pttest	pttest
	ptdisplay ptdisplay
}

foreach {ent class} [array get entity_class] {
	set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$class/$ent
	append VHD_PATH ".vhd"
	set XML_PATH $CORE_IFACE_XML_PATH/rtl/$class/$ent
	append XML_PATH ".xml"
	vcom -2002 -work work $VHD_PATH
	vcom -2002 -gen_xml $ent $XML_PATH $VHD_PATH 
}
foreach {ent class} [array get entity1_class] {
	set VHD_PATH $CORE_LIB_PATH/vhdl/rtl/$class/$ent
	append VHD_PATH ".vhd"
	set XML_PATH $CORE_IFACE_XML_PATH/rtl/$class/$ent
	append XML_PATH ".xml"
	vcom -2002 -work work $VHD_PATH
	vcom -2002 -gen_xml $ent $XML_PATH $VHD_PATH
}

foreach {ent class} [array get entity_tb_class] {
	set VHD_PATH $CORE_LIB_PATH/vhdl/tb/$class/$ent
	append VHD_PATH ".vhd"
	set XML_PATH $CORE_IFACE_XML_PATH/tb/$class/$ent
	append XML_PATH ".xml"
	vcom -2002 -work work $VHD_PATH
	vcom -2002 -gen_xml $ent $XML_PATH $VHD_PATH
}

