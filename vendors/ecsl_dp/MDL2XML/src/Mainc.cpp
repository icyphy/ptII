/*
Copyright (c) Vanderbilt University, 2000-2001
ALL RIGHTS RESERVED
Vanderbilt University disclaims all warranties with regard to this
software, including all implied warranties of merchantability
and fitness.  In no event shall Vanderbilt University be liable for
any special, indirect or consequential damages or any damages
whatsoever resulting from loss of use, data or profits, whether
in an action of contract, negligence or other tortious action,
arising out of or in connection with the use or performance of
this software.
*/

#include "UdmDom.h"
#include "UmlExt.h"
#include "FileSearch.h"

#include "mdl.h"
#include "matlab.h"
#include "MDLLexer.hpp"
#include "MDLParser.hpp"

#include "MatlabIntersection2Clock.h"
#include "TransLabelStringParser.h"

#include <fstream>
#include <map>

#define Matlab matlab
// maps and dummies go here
map<matlab::Block,mdl::Block> block_map;
static UdmDom::DomDataNetwork *mdl_net_child = NULL;
// 7/26/01 kwc put these maps into a ContextContainer structure
class ContextContainer{
public:
// 7/26/01 kwc
	map<matlab::machine,mdl::machine> machine_map;
	map<matlab::chart,mdl::chart> chart_map;
	map<matlab::state,mdl::state> state_map;
	map<matlab::target,mdl::target> target_map;
	map<matlab::event,mdl::event> event_map;
	map<matlab::data,mdl::data> data_map;
	map<matlab::instance,mdl::instance> instance_map;
	map<Matlab::transition,mdl::transition> transition_map;
	map<Matlab::junction,mdl::junction> junction_map;
// 7/26/01 kwc put these maps into a ContextContainer structure
};
// 7/26/01 kwc

// 7/25/01 kwc use this to keep a reference to the simulink objects from
//			externally referenced mdl files
class stateflowContext{
public:
	mdl::Stateflow stateflow;
	ContextContainer *context;
	// 12/27/01 kwc  keep track of what the original name of the externally
	//		referenced subsystem is and its reference name, for this stateflow
	//		object
	string refsubsystemname;
	string origsubystemname;
	// 12/27/01
	
	Matlab::machine dummy_machine;
	Matlab::chart dummy_chart;
};
list<stateflowContext *> stateflowList;
stateflowContext *globalContext;
// 7/26/01 kwc

// 7/31/01 kwc
class ImportedSubSystemInfo{
public:
	mdl::Block rootSubSystem;
	string pathname;
};
list<ImportedSubSystemInfo *> importedSubSystemList;

string rootSystemName;

struct PARAM_DEFAULT {
	string name;
	string value;
};
multimap<string, PARAM_DEFAULT> block_parameter_default_map;


map<string, string> stateflow_sfunctions_map;

// function declarations go here
void SimulinkPass1(mdl::Simulink &rootSim, Matlab::Simulink &t_rootSim);
void SimulinkPass2(Matlab::Simulink &t_sim);
void ModelPass0(mdl::ModelOrLib &mod_con);
void ModelPass1(mdl::ModelOrLib &mod_con, Matlab::SimContainer &t_mod_con);
void ModelPass2(Matlab::SimContainer &t_mod_con);
void BlockPass1(string &currentPath, mdl::Block &blk, Matlab::Block &t_blk, string refsubsystemname);
void BlockPass2(const Matlab::Block &t_blk);
void SystemPass1(string &currentPath, mdl::System &sys, Matlab::System &t_sys, string refsystemname);
void SystemPass2(Matlab::System &t_sys);
void AnnotationPass1(mdl::Annotation &ann, Matlab::Annotation &t_ann);
void AnnotationPass2(const Matlab::Annotation &t_ann);
void LinePass1(mdl::Line &lin, Matlab::Line &t_lin);
void LinePass2(const Matlab::Line &t_line);
void resolveLineSrcPort(mdl::Line &lin, Matlab::Line &t_lin);
void resolveLineDstPort(mdl::Line &lin, Matlab::DirectLine &t_lin);
void BranchPass1(mdl::Branch &br, Matlab::Branch &t_br);
void BranchPass2(Matlab::Branch &t_br);
void resolveBranchDstPort(mdl::Branch &br, Matlab::DirectBranch &t_br);
void StateflowPass0(string root_name, Matlab::Simulink &t_topSim);
void StateflowPass1(mdl::Stateflow &sf, Matlab::Stateflow &t_sf);
void StateflowPass2(Matlab::Stateflow &t_sf);
void MachinePass1(ContextContainer *context, mdl::machine &mach, Matlab::machine &t_mach);
void MachinePass2(Matlab::machine &t_mach);
void ChartPass1(stateflowContext *sfc, mdl::chart &cht, Matlab::chart &t_cht);
void ChartPass2(stateflowContext *sfc, const Matlab::chart &t_cht);
void StatePass1(stateflowContext *sfc, mdl::state &st, Matlab::state &t_st);
void StatePass2(stateflowContext *sfc, const Matlab::state &t_st);
void TargetPass1(stateflowContext *sfc, mdl::target &targ, Matlab::target &t_targ);
void TargetPass2(stateflowContext *sfc, const Matlab::target &t_targ);
void InstancePass1(stateflowContext *sfc, mdl::instance &inst, Matlab::instance &t_inst);
void InstancePass2(stateflowContext *sfc, const Matlab::instance &t_inst);
void EventPass1(stateflowContext *sfc, mdl::event &ev, Matlab::event &t_ev);
void EventPass2(stateflowContext *sfc, const Matlab::event &t_ev);
void TransitionPass1(stateflowContext *sfc, mdl::transition &trans, Matlab::transition &t_trans);
void TransitionPass2(stateflowContext *sfc, const Matlab::transition &t_trans);
void JunctionPass1(stateflowContext *sfc, mdl::junction &junct, Matlab::junction &t_junct);
void JunctionPass2(stateflowContext *sfc, const Matlab::junction &t_junct);
void DataPass1(stateflowContext *sfc, mdl::data &datum, Matlab::data &t_datum);
void DataPass2(stateflowContext *sfc, const Matlab::data &t_datum);
void PropsPass1(mdl::props &prop, Matlab::props &t_prop);
void PropsPass2(Matlab::props &t_prop);
void RangePass1(mdl::range &rng, Matlab::range &t_rng);
void RangePass2(Matlab::range &t_rng);
void ArrayPass1(mdl::array &arr, Matlab::array &t_arr);
void ArrayPass2(Matlab::array &t_arr);
void ParameterPass1(mdl::parameter &par, Matlab::Text &t_par);


string FindSubsystem(string name);
string DuplicateChartName(string chartname);

void Cleanup();

// template functions go here
template<class T>
string SimParamLookup(T MDLObj, string pName) {
	string ret;
	bool found = false;

	set<mdl::parameter> params = MDLObj.simParams();
	for(set<mdl::parameter>::iterator i = params.begin(); i != params.end(); i++) {
		mdl::parameter currParam = *i;
		string currParamName = currParam.name();
		if(currParamName.compare(pName) == 0) {
			ret = currParam.value();
			found = true;
			break;
		}
	}

	// Because of BlockParameterDefaults in the .mdl file
	if (!found)	{
		string blocktype;
		for(set<mdl::parameter>::iterator i = params.begin(); i != params.end(); i++) {
			if (!((string)(*i).name()).compare("BlockType")) {
				blocktype = (*i).value();
				break;
			}
		}

		pair<multimap<string, PARAM_DEFAULT>::iterator, multimap<string, PARAM_DEFAULT>::iterator>> t = block_parameter_default_map.equal_range(blocktype);
		while(t.first != t.second) {
			if ( !((*t.first).second.name).compare(pName) ) {
				ret = (*t.first).second.value;
				break;
			}
			++t.first;
		}
	}
	// --------------------------------------------------
	
	return(ret);
}

template<class T>
string SFParamLookup(T MDLObj, string pName)
{
	string ret;

	set<mdl::parameter> params = MDLObj.sfParams();
	for(set<mdl::parameter>::iterator i = params.begin(); i != params.end(); i++)
	{
		mdl::parameter currParam = *i;
		string currParamName = currParam.name();
		if(currParamName.compare(pName) == 0)
		{
			ret = currParam.value();
			break;
		}
	}
		return(ret);
}

// generic block parameters that can be ignored
string ignorableParamsStrs[] = { "Name", "BlockType", "AttributesFormatString", 
											"Orientation", "ForegroundColor", "BackgroundColor",
											"DropShadow", "NamePlacement", "FontName", "FontSize",
											"FontWeight", "FontAngle", "Position", "ShowName",
											"ShowPortLabels", "CloseFcn", "CopyFcn", "DeleteFcn",
											"InitFcn", "LoadFcn", "ModelCloseFcn", "NameChangeFcn",
											"OpenFcn", "ParentCloseFcn", "PreSaveFcn", "PostSaveFcn",
											"StartFcn", "StopFcn", "UndoDeleteFcn", "LinkStatus", "Ports", "Port" };
string ignorableParamsStrsForReferred[] = { "SourceBlock", "SourceType", "InitialBlockCM",
											"BlockCM", "Frame", "DisplayStringWithTags", "MaskDisplayString",
											"HorizontalTextAlignment", "LeftAlignmentValue", "SourceBlockDiagram",
											"TagMaxNumber",
											"TreatAsAtomicUnit", "RTWSystemCode", "RTWFcnNameOpts",
											"RTWFileNameOpts" };
list<string> ignorableParams;
list<string> ignorableParamsForReferred;


void SimulinkPass1(mdl::Simulink &topSim, Matlab::Simulink &t_topSim)
{
	mdl::Model mod; 
	mdl::Library lib;

	string root_name;

	set<mdl::ModelOrLib> ml_set = topSim.rootSim();
	for(set<mdl::ModelOrLib>::iterator mi = ml_set.begin(); mi != ml_set.end(); mi++)
	{
		root_name = SimParamLookup<mdl::ModelOrLib>(*mi,"Name");
		try
		{
			mod = mdl::Model::Cast(*mi);
			Matlab::Model t_mod = Matlab::Model::Create(t_topSim);
			ModelPass0(mod);
			ModelPass1(mod,t_mod);
		}
		catch(udm_exception& e)
		{
			lib = mdl::Library::Cast(*mi);
			Matlab::Library t_lib = Matlab::Library::Create(t_topSim);
			ModelPass0(lib);
			ModelPass1(lib,t_lib);
		}
	}

	mdl::Stateflow rootSF;

	set<mdl::Stateflow> sf_set = topSim.rootSF();
	for(set<mdl::Stateflow>::iterator sfi = sf_set.begin(); sfi != sf_set.end(); sfi++)
	{
		rootSF = *sfi;
		Matlab::Stateflow t_rootSF = Matlab::Stateflow::Create(t_topSim);
		StateflowPass1(rootSF,t_rootSF);
	}


	// This is called only if there is no stateflow stuff in the root mdl file!
	if (sf_set.empty()) {
		StateflowPass0(root_name, t_topSim);
	}

}

void SimulinkPass2(Matlab::Simulink &t_sim)
{
	Matlab::SimContainer t_mod_con = t_sim.simContainer();
	ModelPass2(t_mod_con);

	Matlab::Stateflow t_sf = t_sim.stateflow();
	if(t_sf)
	{
		StateflowPass2(t_sf);
	}
}

void ModelPass0(mdl::ModelOrLib &mod_con) {

	block_parameter_default_map.clear();
	
	mdl::BlockParameterDefault bpd;
	set<mdl::BlockParameterDefault> bpd_set = mod_con.blockparameterDefault();
	for(set<mdl::BlockParameterDefault>::iterator i = bpd_set.begin(); i != bpd_set.end(); i++) {

		string blocktype;
		set<mdl::parameter> param_set = (*i).parameter_kind_children();
		for(set<mdl::parameter>::iterator j = param_set.begin(); j != param_set.end(); j++) {
			if (!((string)(*j).name()).compare("BlockType")) {
				blocktype = (*j).value();
				break;
			}
		}

		PARAM_DEFAULT pd;
		for(j = param_set.begin(); j != param_set.end(); j++) {
			if (((string)(*j).name()).compare("BlockType")) {
				pd.name = (*j).name();
				pd.value = (*j).value();
				block_parameter_default_map.insert(pair<const string, PARAM_DEFAULT>(blocktype, pd));
			}
		}
	}
}


void ModelPass1(mdl::ModelOrLib &mod_con, Matlab::SimContainer &t_mod_con)
{
	string currentPath;
	mdl::System rootSys = mod_con.rootSystem();
	Matlab::System t_rootSys = Matlab::System::Create(t_mod_con);
	SystemPass1(currentPath,rootSys,t_rootSys,"");

	t_mod_con.Name() = SimParamLookup<mdl::ModelOrLib>(mod_con,"Name");
}

void ModelPass2(Matlab::SimContainer &t_mod_con)
{
	Matlab::System t_sys = t_mod_con.system();
	SystemPass2(t_sys);

}


// this function is called for the actual block; if the block is of type
//	Reference, this means that the actual block could not be found.
// 12/28/01 kwc - added new parameter to set name of subsystem block if not empty
void BlockPass1(string &currentPath, mdl::Block &blk, Matlab::Block &t_blk, string refsubsystemname)
{
//	cout << "Name: " << SimParamLookup<mdl::Block>(blk,"Name") << endl;
	
	
	// 7/31/01 kwc
	string currName = currentPath;
	currName.append("/");
	if (refsubsystemname.empty())
		currName.append(SimParamLookup<mdl::Block>(blk,"Name"));
	else
		currName.append(refsubsystemname);
	// 7/31/01 kwc

	block_map[t_blk] = blk;

	// set common attributes
	string blocktype = SimParamLookup<mdl::Block>(blk,"BlockType");
	t_blk.BlockType() = blocktype;
	if (refsubsystemname.empty())
		t_blk.Name() = SimParamLookup<mdl::Block>(blk,"Name");
	else
		t_blk.Name() = refsubsystemname;

	t_blk.Description() = SimParamLookup<mdl::Block>(blk,"Description");
	t_blk.Priority() = SimParamLookup<mdl::Block>(blk,"Priority");
	t_blk.Tag() = SimParamLookup<mdl::Block>(blk,"Tag");
	t_blk.Position() = SimParamLookup<mdl::Block>(blk,"Position");
	
	// !!VIZA sampletime processing
	std::string st= SimParamLookup<mdl::Block>(blk,"SampleTime");
//	if ( false== st.empty())
//		std::cout << st << "->" << atof( st.c_str()) << std::endl;
	t_blk.SampleTime() = atof( st.c_str());		

	if (!blocktype.compare("S-Function")) {
		stateflow_sfunctions_map[currentPath] = "S-Function";
	}


	Matlab::Subsystem t_subsys;

	try
	{
		t_subsys = Matlab::Subsystem::Cast(t_blk);
	}
	catch(const udm_exception &) { }

	if(t_subsys)
	{
		mdl::System childSys = blk.subSystem();
		 
		if(childSys)
		{
			Matlab::System t_sys = Matlab::System::Create(t_subsys);

//			cout << currName << ":" << refsubsystemname << endl;
			
			SystemPass1(currName, childSys, t_sys, refsubsystemname);
		}
	}
}

/*
	7/24/01 Look through the block objects
	*/
mdl::Block SystemSearch(mdl::System &sys, string name);
mdl::Block BlockSearch(mdl::Block &blk, string &name){
	mdl::System childSys = blk.subSystem();

	if(childSys)
		return SystemSearch(childSys, name);
	else
		return NULL;
}

/*
	7/24/01 Look through the System objects
*/
mdl::Block ImportReference(mdl::Block &ref);
mdl::Block SystemSearch(mdl::System &sys, string name){

	// search through all the blocks in the system
	set<mdl::Block> blocks = sys.blocks();
	for(set<mdl::Block>::iterator bi = blocks.begin(); bi != blocks.end(); bi++)
	{
		mdl::Block blk = *bi;
		string type = SimParamLookup<mdl::Block>(blk,"BlockType");

//		string nm = SimParamLookup<mdl::Block>(blk,"Name");
//		cout << "Name: " + nm + "/Type: " + type + "\n";
		
		if(type.compare("SubSystem") == 0) {
			string nm = SimParamLookup<mdl::Block>(blk, "Name");
			if (nm.compare(name) == 0) {
				return blk;
			} else {
//				BUG!!!!!!!!
//				return BlockSearch(blk, name);

				mdl::Block temp_blk;
				temp_blk = BlockSearch(blk, name);
				if (temp_blk) return temp_blk;
			}
		} else if(type.compare("Reference") == 0) {
//			I think, BUG here too!!!!!!!!

//			return ImportReference(blk);
		}
	}

	return NULL;
}

/*
	7/24/01 Look down the model object
*/
mdl::Block ModelSearch(mdl::ModelOrLib &mod_con, string &name){
	mdl::System rootSys = mod_con.rootSystem();
	return SystemSearch(rootSys, name);
}

/*
	7/24/01 Start looking in the Simulink object
	*/
mdl::Block SimulinkSearch(mdl::Simulink &topSimChild, string &name){
	mdl::Model mod; 
	mdl::Library lib;
	mdl::Block blk;

	set<mdl::ModelOrLib> ml_set = topSimChild.rootSim();
	for(set<mdl::ModelOrLib>::iterator mi = ml_set.begin(); mi != ml_set.end(); mi++)
	{

		string nm = SimParamLookup<mdl::ModelOrLib>(*mi, "Name");
//		cout << "\nSystem name: " + nm + "\n";

		try
		{
			mod = mdl::Model::Cast(*mi);
			blk = ModelSearch(mod, name);
			if (blk) return blk;
		}
		catch(udm_exception& e)
		{
			lib = mdl::Library::Cast(*mi);
			blk =  ModelSearch(lib, name);

//			if (!blk) { cout << "\nNULL bug\n"; }
			if (blk) return blk;
		}
	}

	return NULL;
}


struct WORKING_MDL {
	string filename;
	string modelname;
	string blockname;
//	UdmDom::DomDataNetwork *mdl_net_child;
};
//multimap<string, WORKING_MDL> __working_mdl; // filename, struct
vector<WORKING_MDL> __working_mdl; // filename, struct

/*
	7/24/01 Import referenced mdl file into a new mdl data structure
  */
mdl::Block ImportReference(mdl::Block &ref){
	// get the string in the SourceType field to first see if
	// this is a reference to a subsystem.
	string sourcetype = SimParamLookup<mdl::Block>(ref,"SourceType");

	// if the SourceType field is empty, return false
	if (sourcetype.empty() == true)
		return NULL;
	// if this doesn't refer to a subsystem, then nevermind
/*	if (sourcetype.compare("SubSystem") != 0){
		sourcetype.erase(0, sourcetype.length());
		return NULL;
	}
*/
	// get the string in the SourceBlock field of the reference
	string sourceblock = SimParamLookup<mdl::Block>(ref,"SourceBlock");
	string blockname = SimParamLookup<mdl::Block>(ref,"Name");

	// if the source block field is empty, return false
	if (sourceblock.empty() == true){
		sourcetype.erase(0, sourcetype.length());
		return NULL;
	}

	// parse the filename from the sourceblock...assuming that
	//	the first string before '/' matches the name of the file
	//	containing the system referred to by sourceblock.
	//  {filename}/{subsystem}
	int delim = sourceblock.find("/");
	if (delim == -1)
		return NULL;
	// extract the file name from source block
	string filename = sourceblock.substr(0, delim) + ".mdl";
	// extract out the model name from source block
	// by locating the last '/' which will precede the model name
	
	delim = sourceblock.find_last_of("/");
	string modelname = sourceblock.substr(delim+1, sourceblock.length() - (delim + 1));

//	cout << "\nSourceType: " + sourcetype;
//	cout << sourceblock << " : " << blockname << endl;
//	cout << "File: " + filename + " | Model: " + modelname + " >>> " + blockname + "\n";

	// try to open the file
	char filename_fullpath[MAX_PATH];
	searchFile(".", filename.c_str(), filename_fullpath);


	// It means that the file has a self-reference, and we don't want an infinite loop ;-)
	if (!__working_mdl.empty()
		&& !__working_mdl.back().filename.compare(filename_fullpath) 
		&& !__working_mdl.back().modelname.compare(modelname) 
		&& !__working_mdl.back().blockname.compare(blockname)) {
	
		__working_mdl.pop_back();
		return NULL;
	}
	
/*	
	bool found = false;
	for(multimap<string, WORKING_MDL>::iterator i=__working_mdl.begin(); i!=__working_mdl.end(); i++) {
		if (!(*i).second.filename.compare(filename_fullpath) && !(*i).second.modelname.compare(modelname) && !(*i).second.blockname.compare(blockname))	found = true;
	}
	if (found) return NULL;
*/

//	cout << "Try to load: " << filename << " : " << filename_fullpath << endl;

	ifstream ifs(filename_fullpath, 0x20);
//	ifstream ifs(filename_fullpath, ios::nocreate);
//	ifstream ifs(filename.c_str(), ios::nocreate);

	// if failed, return false
	if (!ifs.is_open()){
		cout << "The externally referenced mdl file, " << filename.c_str() << ", was not located in the directory structure as the main mdl file.  Therefore, the subsystem block, " << modelname.c_str() << ", cannot be imported.  A Reference block will be created in its place." << endl;
		sourcetype.erase(0, sourcetype.length());
		sourceblock.erase(0, sourceblock.length());
		filename.erase(0, filename.length());
		modelname.erase(0, modelname.length());

		return NULL;
	}

	WORKING_MDL wm;
	wm.filename = filename_fullpath;
	wm.modelname = modelname;
	wm.blockname = blockname;
	__working_mdl.push_back(wm);

	mdl_net_child = new UdmDom::DomDataNetwork(mdl::diagram);

	mdl_net_child->CreateNew("_temp_file.xml", "mdl", mdl::Simulink::meta, Udm::CHANGES_LOST_DEFAULT);


/*
	UdmDom::DomDataNetwork *mdl_net_child;

	multimap<string, WORKING_MDL>::iterator __wm = __working_mdl.find(filename_fullpath);
	if (__wm == __working_mdl.end()) {

		wm.mdl_net_child = new UdmDom::DomDataNetwork(mdl::diagram);
		wm.mdl_net_child->CreateNew((filename + ".xml").c_str(),"mdl.dtd",mdl::Simulink::meta,Udm::CHANGES_LOST_DEFAULT);

		mdl_net_child =	wm.mdl_net_child;

		__working_mdl.insert(pair<string, WORKING_MDL>(filename_fullpath, wm));
//		cout << "Loaded: " << filename_fullpath << endl;

	} else {
		mdl_net_child =	(*__wm).second.mdl_net_child;

		__working_mdl.insert(pair<string, WORKING_MDL>(filename_fullpath, wm));
//		cout << "Exists: " << filename_fullpath << endl;
	}
*/

	mdl::Simulink topSimChild = mdl::Simulink::Cast(mdl_net_child->GetRootObject());
	// parse in the mdl file into a data structure in memory
	MDLLexer lexerchild(ifs);
	MDLParser parserchild(lexerchild);

	parserchild.start(topSimChild);
	ifs.close();

	// got the child mdl in a data structure in memory
	mdl::Block childBlock = SimulinkSearch(topSimChild, modelname);

	if (!__working_mdl.empty()) {
		__working_mdl.pop_back();
	}

	if (childBlock){
		mdl::Stateflow rootSF;

		set<mdl::Stateflow> sf_set = topSimChild.rootSF();
		for(set<mdl::Stateflow>::iterator sfi = sf_set.begin(); sfi != sf_set.end(); sfi++)
		{
			rootSF = *sfi;
			// only store unique Stateflow objects in the list
			bool found = false;
			// 12/27/01 kwc don't check for duplicate stateflow objects..they will
			//		have a different reference subsystem name when ImportReference() is called
			/*
			for (list<stateflowContext *>::iterator tsf = stateflowList.begin(); tsf != stateflowList.end(); tsf++){
				mdl::Stateflow tsl = (*tsf)->stateflow;
				if (tsl == rootSF){
					found = true;
					break;
				}
			}
			*/
			if (!found){
				// store it in the list
				stateflowContext *ct = new stateflowContext();
				ct->stateflow = rootSF;
				ct->context = new ContextContainer();
				// 12/27/01 kwc
				ct->origsubystemname = modelname;
				ct->refsubsystemname = SimParamLookup<mdl::Block>(ref,"Name");
				// 12/27/01
				stateflowList.push_back(ct);
			}

		}		
	}
	else{
		cout << "The externally referenced subsystem block, " <<  modelname.c_str() << ", could not be found in the externally referenced mdl file (" << filename.c_str() << ").  Therefore, it cannot be imported.  A Reference block will be created in its place." << endl;
	}

	// clean up strings!
	sourcetype.erase(0, sourcetype.length());
	sourceblock.erase(0, sourceblock.length());
	filename.erase(0, filename.length());
	modelname.erase(0, modelname.length());

	return childBlock;
}

/*
	7/24/01 Used to open up the externally referenced model file and 
	traverse it.
	Return true for successful traversal.
	Return false for failure and ask parent to treat this block as a
	reference.
  */
bool ExploreExternal(string &currentPath, mdl::Block &ref, Matlab::System &t_sys){
	// get the actual subsystem referenced by ref
	mdl::Block childBlock = ImportReference(ref);

	if (childBlock){
		// determine if masked or normal
		string masktestchild = SimParamLookup<mdl::Block>(childBlock,"MaskType");

		if(masktestchild.size() == 0)
		{
			Matlab::Normal t_normal_child = Matlab::Normal::Create(t_sys);

			set<mdl::parameter> pars = ref.simParams();
			for(set<mdl::parameter>::iterator i=pars.begin(); i != pars.end(); i++) {
				mdl::parameter currParam = mdl::parameter::Cast(*i);
				bool skip_param = false;
				for(list<string>::iterator j = ignorableParamsForReferred.begin(); j != ignorableParamsForReferred.end(); j++) {
					if ( (*j).compare(currParam.name())==0 ) {
							skip_param = true;
							break;
					}
				}
				if (!skip_param) {
					Matlab::Text t_par = Matlab::Text::Create(t_normal_child);
					ParameterPass1(currParam, t_par);
				}
			}

			BlockPass1(currentPath, childBlock, t_normal_child, SimParamLookup<mdl::Block>(ref,"Name"));
		}
		else
		{
			Matlab::Masked t_masked_child = Matlab::Masked::Create(t_sys);

			set<mdl::parameter> pars = ref.simParams();
			for(set<mdl::parameter>::iterator i=pars.begin(); i != pars.end(); i++) {
				mdl::parameter currParam = mdl::parameter::Cast(*i);
				bool skip_param = false;
				for(list<string>::iterator j = ignorableParamsForReferred.begin(); j != ignorableParamsForReferred.end(); j++) {
					if ( (*j).compare(currParam.name())==0 ) {
							skip_param = true;
							break;
					}
				}
				if (!skip_param) {
					Matlab::Text t_par = Matlab::Text::Create(t_masked_child);
					ParameterPass1(currParam, t_par);
				}
			}

			BlockPass1(currentPath, childBlock,t_masked_child, SimParamLookup<mdl::Block>(ref,"Name"));
		}
		return true;
	}
	else
		return false;
}

// 12/28/01 kwc - Added parameter refsystemname to set the name of this system if it
//		is immediated inside an externally imported subsystem block
void SystemPass1(string &currentPath, mdl::System &sys, Matlab::System &t_sys, string refsystemname)
{
	// 7/31/01 kwc
	string currName = currentPath;
	if (!currName.empty())
		currName.append("/");
	else
		// want to remember the root system name so it can be used in ChartPass1
		rootSystemName = SimParamLookup<mdl::System>(sys,"Name");

//		cout << "\nSystem translated: " + rootSystemName + "\n";
	
	// add on to the current Simulink path to this system by appending this
	//	system's name to the end of currName
	// 12/28/01 kwc - use the reference name instead if not empty
	if (refsystemname.empty())
		currName.append(SimParamLookup<mdl::System>(sys,"Name"));
	else
		currName.append(refsystemname);
	// 7/31/01 kwc

	// NOTE: Blocks must be processed before Lines for conns to be made correctly
	set<mdl::Block> blocks = sys.blocks();
	for(set<mdl::Block>::iterator bi = blocks.begin(); bi != blocks.end(); bi++)
	{
		mdl::Block blk = *bi;
		string type = SimParamLookup<mdl::Block>(blk,"BlockType");

		if(type.compare("SubSystem") == 0)
		{
			// determine if masked or normal
			string masktest = SimParamLookup<mdl::Block>(blk,"MaskType");
			if(masktest.size() == 0)
			{
				Matlab::Normal t_normal = Matlab::Normal::Create(t_sys);
				BlockPass1(currName, blk,t_normal, "");
			}
			else
			{
				Matlab::Masked t_masked = Matlab::Masked::Create(t_sys);
				BlockPass1(currName, blk,t_masked, "");
			}
		}
		else if(type.compare("Reference") == 0)
		{
//			cout << "Refname " <<  SimParamLookup<mdl::Block>(blk,"Name") << endl;

			// add kwc
			// exploreexternal updates currName to append name of blk at end.
			if (ExploreExternal(currName, blk, t_sys)){
				// create a subsystem info object and store in the list
				ImportedSubSystemInfo *subsys = new ImportedSubSystemInfo();
				subsys->rootSubSystem = blk;
				subsys->pathname.resize(currName.length());
				subsys->pathname.replace(0, subsys->pathname.size(), currName);
				importedSubSystemList.push_front(subsys);
			} else {
				// add kwc
				Matlab::Reference t_ref = Matlab::Reference::Create(t_sys);
				
				// get all parameters (except ones already done and ignorables)
				set<mdl::parameter> pars = blk.simParams();
				for(set<mdl::parameter>::iterator i = pars.begin(); i != pars.end(); i++)
				{
					mdl::parameter currParam = mdl::parameter::Cast(*i);
					bool skip_param = false;
					for(list<string>::iterator j = ignorableParams.begin(); j != ignorableParams.end(); j++)
					{
						if((*j).compare(currParam.name()) == 0)
						{
							skip_param = true;
							break;
						}
					}

					if(!skip_param)
					{
						Matlab::Text t_par = Matlab::Text::Create(t_ref);
						ParameterPass1(currParam,t_par);
					}

				}

				BlockPass1(currName, blk, t_ref, "");
			}
		} else {
			// Primitive
			Matlab::Primitive t_prim = Matlab::Primitive::Create(t_sys);
			
			// get all parameters (except ones already done and ignorables)
			set<mdl::parameter> pars = blk.simParams();
			for(set<mdl::parameter>::iterator i = pars.begin(); i != pars.end(); i++)
			{
				mdl::parameter currParam = mdl::parameter::Cast(*i);
				bool skip_param = false;
				for(list<string>::iterator j = ignorableParams.begin(); j != ignorableParams.end(); j++)
				{
					if((*j).compare(currParam.name()) == 0)
					{
						skip_param = true;
						break;
					}
				}

				if(!skip_param)
				{
					Matlab::Text t_par = Matlab::Text::Create(t_prim);
					ParameterPass1(currParam,t_par);
				}

			}

			BlockPass1(currName, blk,t_prim, "");

		}
	}

	set<mdl::Annotation> annotations = sys.annotations();
	for(set<mdl::Annotation>::iterator ai = annotations.begin(); ai != annotations.end(); ai++)
	{
		mdl::Annotation ann = *ai;
		Matlab::Annotation t_ann = Matlab::Annotation::Create(t_sys);
		AnnotationPass1(ann,t_ann);
	}

	set<mdl::Line> lines = sys.lines();
	for(set<mdl::Line>::iterator li = lines.begin(); li != lines.end(); li++)
	{
		mdl::Line lin = *li;

		set<mdl::Branch> childBranches = lin.branches();
		if(childBranches.size() == 0)
		{
			// DirectLine
			Matlab::DirectLine t_dirlin = Matlab::DirectLine::Create(t_sys);
			LinePass1(lin,t_dirlin);
		}
		else
		{
			// BranchedLine

			Matlab::BranchedLine t_bralin = Matlab::BranchedLine::Create(t_sys);
			LinePass1(lin,t_bralin);
		}
		
	}

	if (refsystemname.empty())
		t_sys.Name() = SimParamLookup<mdl::System>(sys,"Name");
	else
		t_sys.Name() = refsystemname;
}

// set the block's Inport/Outport/TriggerPort .refPort() to the created Matlab::...up in
//	the grandparent Subsystem (where the parent System contains this block).
void BlockPass2(const Matlab::Block &t_blk)
{
	mdl::Block blk;
	map<Matlab::Block,mdl::Block>::iterator mapPos = block_map.find(t_blk);
	if(mapPos != block_map.end())
	{
		blk = mapPos->second;
	}

	// determine subclass before proceeding
	Matlab::Subsystem t_subsys;
	Matlab::Reference t_ref;
	Matlab::Primitive t_prim;
	bool resolved = false;
	// 8/23/01 kwc
	bool foundport = false;
	// 8/23/01 kwc
	int attempt = 0;

	while(!resolved)
	{
		try
		{
			switch(attempt)
			{
			case 0: t_subsys = Matlab::Subsystem::Cast(t_blk);
					resolved = true;
					break;
			case 1: t_ref = Matlab::Reference::Cast(t_blk);
					resolved = true;
					break;
			case 2: t_prim = Matlab::Primitive::Cast(t_blk);
					resolved = true;
					break;
			}
		}
		catch(const udm_exception &) { attempt++; if(attempt > 2) throw; }
		
	}
	
	if(t_subsys)
	{
		// just process child system
		Matlab::System childSys = t_subsys.system();
		SystemPass2(childSys);
	}
	else if(t_ref)
	{
		t_ref.SourceBlock() = SimParamLookup<mdl::Block>(blk,"SourceBlock");
		t_ref.SourceType() = SimParamLookup<mdl::Block>(blk,"SourceType");
	}
	else
	{
		string type = t_prim.BlockType();

		if(type.compare("Inport") == 0)
		{
			// set refport to the port of the enclosing block
			string port_num = SimParamLookup<mdl::Block>(blk,"Port");
			Matlab::System parent_sys = t_prim.parent();
			Matlab::Subsystem grandparent_subsys;
			try
			{
				grandparent_subsys = parent_sys.parentSubsystem();
			}
			catch(const udm_exception &) {	}
			if(grandparent_subsys)
			{
				// 8/23/01 kwc
				foundport = false;
				// 8/23/01 kwc
				set<Matlab::InputPort> input_set = grandparent_subsys.InputPort_kind_children();
				for(set<Matlab::InputPort>::iterator ipi = input_set.begin(); ipi != input_set.end(); ipi++)
				{
					__int64 num = ipi->Number();
					if(num == atoi(port_num.c_str()))
					{
						t_prim.refPort() = *ipi;
						ipi->portBlock() = t_prim;
						// 8/23/01 kwc
						foundport = true;
						// 8/23/01 kwc
					}
				}
				// 8/23/01 kwc
				// if we couldn't find the Matlab::InputPort in the grandparent subsystem
				//	this means it wasn't created due to no line connecting into that Inport in the mdl file.
				// resolution is to create the Matlab::InputPort in the grandparent subsystem
				if (!foundport){
					Matlab::InputPort created_inport = Matlab::InputPort::Create(grandparent_subsys);
					created_inport.Number() = atoi(port_num.c_str());
					created_inport.Name() = "";
					t_prim.refPort() = created_inport;
					created_inport.portBlock() = t_prim;
				}
				// 8/23/01 kwc
			}

		}
		else if(type.compare("Outport") == 0)
		{
			// set refport to the port of the enclosing block
			string port_num = SimParamLookup<mdl::Block>(blk,"Port");
			Matlab::System parent_sys = t_prim.parent();
			Matlab::Subsystem grandparent_subsys;
			try
			{
				grandparent_subsys = parent_sys.parentSubsystem();
			}
			catch(const udm_exception &) {	}
			if(grandparent_subsys)
			{
				// 8/23/01 kwc
				foundport = false;
				// 8/23/01 kwc
				set<Matlab::OutputPort> output_set = grandparent_subsys.OutputPort_kind_children();
				for(set<Matlab::OutputPort>::iterator opi = output_set.begin(); opi != output_set.end(); opi++)
				{
					__int64 num = opi->Number();
					if(num == atoi(port_num.c_str()))
					{
						t_prim.refPort() = *opi;
						opi->portBlock() = t_prim;
						// 8/23/01 kwc
						foundport = true;
						// 8/23/01 kwc
					}
				}
				// 8/23/01 kwc
				if (!foundport){
					Matlab::OutputPort created_outport = Matlab::OutputPort::Create(grandparent_subsys);
					created_outport.Number() = atoi(port_num.c_str());
					// see if the matching port exists in the mdl;
					// will only be present if the port is named
					bool nameset = false;
					set<mdl::Port> mdl_ports = blk.ports();
					for(set<mdl::Port>::iterator pi = mdl_ports.begin(); pi != mdl_ports.end(); pi++)
					{
						mdl::Port currPort = *pi;
						string currPortNum = SimParamLookup<mdl::Port>(currPort,"PortNumber");
						if(currPortNum.compare(port_num.c_str()) == 0)
						{
							// copy the name over
							string currPortName = SimParamLookup<mdl::Port>(currPort,"Name");
							created_outport.Name() = currPortName;
							nameset = true;
							break;
						}
					}
					if(!nameset)
					{
						created_outport.Name() = "";
					}
					t_prim.refPort() = created_outport;
					created_outport.portBlock() = t_prim;
				}
				// 8/23/01 kwc
			}
			
		}
		// 8/20/01 kwc
		else if(type.compare("TriggerPort") == 0)
		{
			// set the refport to the port of the enclosing block
			Matlab::System parent_sys = t_prim.parent();
			Matlab::Subsystem grandparent_subsys;
			try
			{
				grandparent_subsys = parent_sys.parentSubsystem();
			}
			catch(const udm_exception &) {	}
			if(grandparent_subsys)
			{
				set<Matlab::TriggerPort> trig_set = grandparent_subsys.TriggerPort_kind_children();
				// there can only be one TriggerPort in a Subsystem
				if (trig_set.size() > 0){
					Matlab::TriggerPort trigport = *trig_set.begin();
					t_prim.refPort() = trigport;
					trigport.portBlock() = t_prim;
					// set the Matlab::TriggerPort's name
					trigport.Name() = t_prim.Name();
				}
				// 8/23/01 kwc
				else{
					// create new TriggerPort in the grandparent subsystem
					Matlab::TriggerPort created_trigport = Matlab::TriggerPort::Create(grandparent_subsys);
					created_trigport.TriggerType() = SimParamLookup<mdl::Block>(blk, "TriggerType");
					t_prim.refPort() = created_trigport;
					created_trigport.portBlock() = t_prim;
					created_trigport.Name() = t_prim.Name();
				}
				// 8/23/01 kwc
			}

		}
		// 8/20/01 kwc
		// 8/21/01 kwc
		else if(type.compare("EnablePort") == 0)
		{
			// set the refport to the port of the enclosing block
			Matlab::System parent_sys = t_prim.parent();
			Matlab::Subsystem grandparent_subsys;
			try
			{
				grandparent_subsys = parent_sys.parentSubsystem();
			}
			catch(const udm_exception &) {	}
			if(grandparent_subsys)
			{
				set<Matlab::EnablePort> en_set = grandparent_subsys.EnablePort_kind_children();
				// there can only be one EnablePort in a Subsystem
				if (en_set.size() > 0){
					Matlab::EnablePort enport = *en_set.begin();
					t_prim.refPort() = enport;
					enport.portBlock() = t_prim;
					// set the Matlab::EnablePort's name
					enport.Name() = t_prim.Name();
				}
				// 8/23/01 kwc
				else{
					// create new EnablePort in the grandparent subsystem
					Matlab::EnablePort created_enport = Matlab::EnablePort::Create(grandparent_subsys);
					created_enport.StatesWhenEnabling() = SimParamLookup<mdl::Block>(blk, "StatesWhenEnabling");
					t_prim.refPort() = created_enport;
					created_enport.portBlock() = t_prim;
					created_enport.Name() = t_prim.Name();
				}
				// 8/23/01 kwc
			}

		}
		// 8/21/01 kwc

	}

}

void SystemPass2(Matlab::System &t_sys)
{
	set<Matlab::Line> line_set = t_sys.lines();
	for(set<Matlab::Line>::iterator li = line_set.begin(); li != line_set.end(); li++)
	{
		LinePass2(*li);
	}

	set<Matlab::Block> block_set = t_sys.blocks();
	for(set<Matlab::Block>::iterator bi = block_set.begin(); bi != block_set.end(); bi++)
	{
		BlockPass2(*bi);
	}

	set<Matlab::Annotation> ann_set = t_sys.annotations();
	for(set<Matlab::Annotation>::iterator ai = ann_set.begin(); ai != ann_set.end(); ai++)
	{
		AnnotationPass2(*ai);
	}

}

void AnnotationPass1(mdl::Annotation &ann, Matlab::Annotation &t_ann)
{
	t_ann.Text() = SimParamLookup<mdl::Annotation>(ann,"Text");
	t_ann.Position() = SimParamLookup<mdl::Annotation>(ann,"Position");
}

void AnnotationPass2(const Matlab::Annotation &t_ann)
{
	// this space intentionally left blank
}

void LinePass1(mdl::Line &lin, Matlab::Line &t_lin)
{
	// get common attributes: position 
	t_lin.Points() = SimParamLookup<mdl::Line>(lin,"Points");

	set<mdl::Branch> childBranches = lin.branches();

	for(set<mdl::Branch>::iterator i = childBranches.begin(); i != childBranches.end(); i++)
	{
		mdl::Branch brch = *i;
		// add child branches and process them
		// need to determine if nested or directbranch before creating
		set<mdl::Branch> nestedBranches = brch.subBranches();

		if(nestedBranches.size() == 0)
		{
			// DirectBranch
			Matlab::DirectBranch dbrch = Matlab::DirectBranch::Create(t_lin);
			BranchPass1(brch, dbrch);
		}
		else
		{
			// NestedBranch
			Matlab::NestedBranch nbrch = Matlab::NestedBranch::Create(t_lin);
			BranchPass1(brch, nbrch);
		}
	}

	Matlab::DirectLine d_lin;
	Matlab::BranchedLine b_lin;

	try
	{
		d_lin = Matlab::DirectLine::Cast(t_lin);
	}
	catch(const udm_exception &)
	{
		b_lin = Matlab::BranchedLine::Cast(t_lin);
	}

	if(d_lin)
	{
		resolveLineSrcPort(lin, d_lin);
		resolveLineDstPort(lin, d_lin);
	}
	else
	{
		resolveLineSrcPort(lin, b_lin);
	}
}

void LinePass2(const Matlab::Line &t_line)
{
	// nothing else to do
}

void resolveLineSrcPort(mdl::Line &lin, Matlab::Line &t_lin)
{
	bool srcfound = false;

	Matlab::System lineParent = t_lin.parent();
	Matlab::Block theBlock;

	string srcblock = SimParamLookup<mdl::Line>(lin, "SrcBlock");
	string srcport = SimParamLookup<mdl::Line>(lin,"SrcPort");

	set<Matlab::Block> local_blocks = lineParent.blocks();
	for(set<Matlab::Block>::iterator bi = local_blocks.begin(); bi != local_blocks.end(); bi++)
	{
		theBlock = *bi;

		mdl::Block blk;
		map<Matlab::Block,mdl::Block>::iterator mapPos = block_map.find(theBlock);
		if(mapPos != block_map.end())
		{
			blk = mapPos->second;
		}

		string blockName = theBlock.Name();
		if( ( blockName.compare(srcblock) == 0 ) )
		{
			srcfound = true;
			// see if this block has an outport # to match, if not, need to create new outport
			bool portfound = false;
			set<Matlab::OutputPort> outport_set = theBlock.OutputPort_kind_children();
			for(set<Matlab::OutputPort>::iterator opi = outport_set.begin(); opi != outport_set.end(); opi++)
			{
				Matlab::OutputPort outport = *opi;
				// see if the port number matches
				__int64 outnum = outport.Number();
				if(outnum == atoi(srcport.c_str()))
				{
					t_lin.srcPort() = outport;
					portfound = true;
					break;
				}
			
			}
			// at this point, need to see if the outport already existed, if not, need to make new one
			if(!portfound)
			{
				// create new outport in the block with srcport as its number

				Matlab::OutputPort created_outport = Matlab::OutputPort::Create(theBlock);
				created_outport.Number() = atoi(srcport.c_str());
				created_outport.owningLine() = t_lin;
				t_lin.srcPort() = created_outport;		

				// see if the matching port exists in the mdl;
				// will only be present if the port is named
				bool nameset = false;
				set<mdl::Port> mdl_ports = blk.ports();
				for(set<mdl::Port>::iterator pi = mdl_ports.begin(); pi != mdl_ports.end(); pi++)
				{
					mdl::Port currPort = *pi;
					string currPortNum = SimParamLookup<mdl::Port>(currPort,"PortNumber");
					if(currPortNum.compare(srcport.c_str()) == 0)
					{
						// copy the name over
						string currPortName = SimParamLookup<mdl::Port>(currPort,"Name");
						created_outport.Name() = currPortName;
						nameset = true;
						break;
					}
				}
				if(!nameset)
				{
					created_outport.Name() = "";
				}
			}

			break;
		}
	}

}

// 8/21/01 kwc
// helper function to go into the System in the passed in block and search for the
//	specified block type to retrieve the value in the specified parm
string getNestedBlockInfo(Matlab::Block theBlock, string blocktype, string parm){
	set<mdl::System> sys_set;
	set<mdl::Block> blk_set;
	string blktype;
	mdl::Block	blk, searchblk;
	mdl::System sys;

	blktype = theBlock.BlockType();
	if (blktype.compare("SubSystem") != 0)
		return "";

	blktype.erase(0, blktype.length());
	map<Matlab::Block,mdl::Block>::iterator mapPos = block_map.find(theBlock);
	if (mapPos != block_map.end()){
		blk = mapPos->second;
		// since the destination is a trigger, the DstBlock specifies a subsystem
		sys_set = blk.System_kind_children();
		if (sys_set.size() > 0){
			// there is only one System inside a Subsystem
			sys = *sys_set.begin();
			blk_set = sys.Block_kind_children();
			for (set<mdl::Block>::iterator bi = blk_set.begin(); bi != blk_set.end(); bi++){
				searchblk = *bi;
				blktype = SimParamLookup<mdl::Block>(searchblk, "BlockType");
				// find the triggerport blocktype and get the TriggerType parameter.
				if (blktype.compare(blocktype) == 0){
					return SimParamLookup<mdl::Block>(searchblk, parm);
				}
			}
		}
	}

	return "";
}
// 8/21/01 kwc

// see description in resolveBranchDstPort
void resolveLineDstPort(mdl::Line &lin, Matlab::DirectLine &t_lin)
{
	bool dstfound = false;

	Matlab::System lineParent = t_lin.parent();
	Matlab::Block theBlock;

	string dstblock = SimParamLookup<mdl::Line>(lin, "DstBlock");
	string dstport = SimParamLookup<mdl::Line>(lin,"DstPort");

	set<Matlab::Block> local_blocks = lineParent.blocks();
	for(set<Matlab::Block>::iterator bi = local_blocks.begin(); bi != local_blocks.end(); bi++)
	{
		theBlock = *bi;

		string blockName = theBlock.Name();
		if( ( blockName.compare(dstblock) == 0 ) )
		{
			// found the destination block in this System (lineParent)
			dstfound = true;
			// if dstport = "trigger", then look for a TriggerPort, else 
			// see if this block has an inport # to match, if not, need to create new inputport or triggerport
			bool portfound = false;
			// 8/20/01 kwc
			if (dstport.compare("trigger") == 0){
				set<Matlab::TriggerPort> trigport_set = theBlock.TriggerPort_kind_children();
				// there can only be one TriggerPort in a System
				if (trigport_set.size() > 0){
					portfound = true;
					set<Matlab::TriggerPort>::iterator tpi = trigport_set.begin();
					Matlab::TriggerPort trigport = *trigport_set.begin();
					// set the branch's dstPort to this TriggerPort
					t_lin.dstPort() = trigport;
					break;
				}
			}
			// 8/21/01 kwc
			else if (dstport.compare("enable") == 0){
				set<Matlab::EnablePort> enport_set = theBlock.EnablePort_kind_children();
				// there can only be one EnablePort in a System
				if (enport_set.size() > 0){
					portfound = true;
					set<Matlab::EnablePort>::iterator tpi = enport_set.begin();
					Matlab::EnablePort enport = *enport_set.begin();
					// set the branch's dstPort to this EnablePort
					t_lin.dstPort() = enport;
					break;
				}
			}
			// 8/21/01 kwc
			else{
			// 8/20/01 kwc
				// it's a number so look for a InputPort by number.
				set<Matlab::InputPort> inport_set = theBlock.InputPort_kind_children();
				for(set<Matlab::InputPort>::iterator ipi = inport_set.begin(); ipi != inport_set.end(); ipi++)
				{
					Matlab::InputPort inport = *ipi;
					// see if the port number matches
					__int64 innum = inport.Number();
					if(innum == atoi(dstport.c_str()))
					{
						t_lin.dstPort() = inport;
						portfound = true;
						break;
					}
				
				}
			// 8/20/01 kwc
			}
			// 8/20/01 kwc
			// at this point, need to see if the inport already existed, if not, need to make new one
			if(!portfound)
			{
				// 8/20/01 kwc
				if (dstport.compare("trigger") == 0){
					// create new TriggerPort in the block
					Matlab::TriggerPort created_trigport = Matlab::TriggerPort::Create(theBlock);
					// 8/21/01 kwc
					//need to dive into the System on the matlab side...assume the objects inside
					//	theBlock are already created....
					created_trigport.TriggerType() = getNestedBlockInfo(theBlock, "TriggerPort", "TriggerType");
					// 8/21/01 kwc
					created_trigport.owningDirectLine() = t_lin;
					t_lin.dstPort() = created_trigport;
					created_trigport.Name() = "";
				}
				// 8/21/01 kwc
				else if (dstport.compare("enable") == 0){
					// create new EnablePort in the block
					Matlab::EnablePort created_enport = Matlab::EnablePort::Create(theBlock);
					created_enport.StatesWhenEnabling() = getNestedBlockInfo(theBlock, "EnablePort", "StatesWhenEnabling");
					created_enport.owningDirectLine() = t_lin;
					t_lin.dstPort() = created_enport;
					created_enport.Name() = "";
				}
				// 8/21/01 kwc
				else{
				// 8/20/01 kwc
					// create new inport in the block with dstport as its number

					Matlab::InputPort created_inport = Matlab::InputPort::Create(theBlock);
					created_inport.Number() = atoi(dstport.c_str());
					created_inport.owningDirectLine() = t_lin;
					t_lin.dstPort() = created_inport;

					// InputPorts cannot be named
					created_inport.Name() = "";
				// 8/20/01 kwc
				}
				// 8/20/01 kwc
			}

			break;
		}
	}

}

void BranchPass1(mdl::Branch &br, Matlab::Branch &t_br)
{
	// get position
	t_br.Points() = SimParamLookup<mdl::Branch>(br,"Points");

	Matlab::DirectBranch d_br;
	Matlab::NestedBranch n_br;

	try
	{
		d_br = Matlab::DirectBranch::Cast(t_br);
	}
	catch(const udm_exception &)
	{
		n_br = Matlab::NestedBranch::Cast(t_br);
	}

	if(d_br)
	{
		// DirectBranch
		resolveBranchDstPort(br, d_br);
	}
	else
	{
		// NestedBranch
		set<mdl::Branch> childBranches = br.subBranches();

		for(set<mdl::Branch>::iterator i = childBranches.begin(); i != childBranches.end(); i++)
		{
			mdl::Branch brch = *i;
			// add child branches and process them
			// need to determine if nested or directbranch before creating
			set<mdl::Branch> nestedBranches = brch.subBranches();

			if(nestedBranches.size() == 0)
			{
				// DirectBranch
				Matlab::DirectBranch dbrch = Matlab::DirectBranch::Create(n_br);
				BranchPass1(brch, dbrch);
			}
			else
			{
				// NestedBranch
				Matlab::NestedBranch nbrch = Matlab::NestedBranch::Create(n_br);
				BranchPass1(brch, nbrch);
			}
		}
	}

}

void BranchPass2(Matlab::Branch &t_br)
{
	// nothing needed
}


// Locate the destination block that this DirectBranch connects to. The branch's
//	parent is the System.
// see if the port number in the Branch object matches any port numbers of Matlab::InputPort objects
//	in the Block located within the System (by name).  If match,
//	then set the branch's dstPort() to the found Matlab::InputPort (in the found block),
//	otherwise, create an Matlab::InputPort in the found block and then
//	set the branch's dstPort() to the created Matlab::InputPort

// Do similar for where DirectBranch has DstPort = trigger...locate the
//	Subsystem (Block) in the System (parent of the Line) and see if that
//	Subsystem has a TriggerPort (can only be zero or one of them).  If found
//		set DirectBranch's dstPort() to that Matlab::TriggerPort...otherwise
//		create the Matlab::TriggerPort in the found Subsystem and then
//		connect the DirectBranch's dstPort() to that Matlab::TriggerPort
void resolveBranchDstPort(mdl::Branch &br, Matlab::DirectBranch &t_br)
{
	bool dstfound = false;

	Matlab::Line branchParent;
	try
	{
		branchParent = t_br.parentBranchedLine();
	}
	catch(const udm_exception &) { branchParent = NULL; }
	Matlab::Branch enclosingBranch;
	Matlab::Branch compareBranch;
	compareBranch = t_br;
	while(!branchParent)
	{
		enclosingBranch = compareBranch.parentNestedBranch();
		try
		{
			branchParent = enclosingBranch.parentBranchedLine();
		}
		catch(const udm_exception &) { branchParent = NULL; }
		compareBranch = enclosingBranch;
	}

	Matlab::System lineParent = branchParent.parent();
	Matlab::Block theBlock;

	string dstblock = SimParamLookup<mdl::Branch>(br, "DstBlock");
	string dstport = SimParamLookup<mdl::Branch>(br,"DstPort");

	set<Matlab::Block> local_blocks = lineParent.blocks();
	for(set<Matlab::Block>::iterator bi = local_blocks.begin(); bi != local_blocks.end(); bi++)
	{
		theBlock = *bi;

		string blockName = theBlock.Name();
		if( ( blockName.compare(dstblock) == 0 ) )
		{
			// found the destination block in this System (lineParent)
			dstfound = true;
			// if dstport = "trigger", then look for a TriggerPort, else 
			// see if this block has an inport # to match, if not, need to create new inputport or triggerport
			bool portfound = false;
			// 8/20/01 kwc
			if (dstport.compare("trigger") == 0){
				set<Matlab::TriggerPort> trigport_set = theBlock.TriggerPort_kind_children();
				// there can only be one TriggerPort in a System
				if (trigport_set.size() > 0){
					portfound = true;
					Matlab::TriggerPort trigport = *trigport_set.begin();
					// set the branch's dstPort to this TriggerPort
					t_br.dstPort() = trigport;
					break;
				}
			}
			// 8/21/01 kwc
			// see if this is to an EnablePort
			else if (dstport.compare("enable") == 0){
				set<Matlab::EnablePort> enport_set = theBlock.EnablePort_kind_children();
				// there can only be one EnablePort in a System
				if (enport_set.size() > 0){
					portfound = true;
					Matlab::EnablePort enport = *enport_set.begin();
					// set the branch's dstPort to this EnablePort
					t_br.dstPort() = enport;
					break;
				}
			}
			// 8/21/01 kwc
			else{
			// 8/20/01 kwc
				// it's a number so look for a InputPort by number.
				set<Matlab::InputPort> inport_set = theBlock.InputPort_kind_children();
				for(set<Matlab::InputPort>::iterator ipi = inport_set.begin(); ipi != inport_set.end(); ipi++)
				{
					Matlab::InputPort inport = *ipi;
					// see if the port number matches
					__int64 innum = inport.Number();
					if(innum == atoi(dstport.c_str()))
					{
						t_br.dstPort() = inport;
						portfound = true;
						break;
					}
				
				}
			// 8/20/01 kwc
			}
			// 8/20/01 kwc
			// at this point, need to see if the inport already existed, if not, need to make new one
			if(!portfound)
			{
				// 8/20/01 kwc
				if (dstport.compare("trigger") == 0){
					// create new TriggerPort in the block
					Matlab::TriggerPort created_trigport = Matlab::TriggerPort::Create(theBlock);
					created_trigport.TriggerType() = getNestedBlockInfo(theBlock, "TriggerPort", "TriggerType");
					created_trigport.owningDirectBranch() = t_br;
					t_br.dstPort() = created_trigport;
					created_trigport.Name() = "";
				}
				// 8/21/01 kwc
				else if (dstport.compare("enable") == 0){
					// create new EnablePort in the block
					Matlab::EnablePort created_enport = Matlab::EnablePort::Create(theBlock);
					created_enport.StatesWhenEnabling() = getNestedBlockInfo(theBlock, "EnablePort", "StatesWhenEnabling");
					created_enport.owningDirectBranch() = t_br;
					t_br.dstPort() = created_enport;
					created_enport.Name() = "";
				}
				// 8/21/01 kwc
				else{
				// 8/20/01 kwc
					// create new InputPort in the block with dstport as its number

					Matlab::InputPort created_inport = Matlab::InputPort::Create(theBlock);
					created_inport.Number() = atoi(dstport.c_str());
					created_inport.owningDirectBranch() = t_br;
					t_br.dstPort() = created_inport;				

					// InputPorts cannot be named
					created_inport.Name() = "";
				// 8/20/01 kwc
				}
				// 8/20/01 kwc
			}

			break;
		}
	}

}

/*
	7/25/01 encapsulate the create chart code into a function
  */
void GetCharts(stateflowContext *sfc, Matlab::machine dummy_machine){
	set<mdl::chart> chart_set = sfc->stateflow.charts();
	for(set<mdl::chart>::iterator ci = chart_set.begin(); ci != chart_set.end(); ci++)
	{
		mdl::chart cht = *ci;

		Matlab::chart t_cht = Matlab::chart::Create(dummy_machine);
		ChartPass1(sfc,cht,t_cht);
	}
}

/*
	7/25/01 encapsulate the create state code into a function
  */
void GetStates(stateflowContext *sfc, Matlab::chart dummy_chart){
	set<mdl::state> state_set = sfc->stateflow.states();
	for(set<mdl::state>::iterator si = state_set.begin(); si != state_set.end(); si++)
	{
		mdl::state st = *si;

		Matlab::state t_st = Matlab::state::Create(dummy_chart);
		StatePass1(sfc,st,t_st);
	}
}

/*
	7/25/01 encapsulate the create target code into a function
  */
void GetTargets(stateflowContext *sfc, Matlab::machine dummy_machine){
	set<mdl::target> target_set = sfc->stateflow.targets();
	for(set<mdl::target>::iterator ti = target_set.begin(); ti != target_set.end(); ti++)
	{
		mdl::target targ = *ti;

		Matlab::target t_targ = Matlab::target::Create(dummy_machine);
		TargetPass1(sfc,targ,t_targ);
	}
}

/*
	7/25/01 encapsulate the create instance code into a function
  */
void GetInstances(stateflowContext *sfc, Matlab::machine dummy_machine){
	set<mdl::instance> inst_set = sfc->stateflow.instances();
	for(set<mdl::instance>::iterator ii = inst_set.begin(); ii != inst_set.end(); ii++)
	{
		mdl::instance inst = *ii;

		Matlab::instance t_inst = Matlab::instance::Create(dummy_machine);
		InstancePass1(sfc,inst,t_inst);
	}
}

/*
	7/25/01 encapsulate the create event code into a function
  */
void GetEvents(stateflowContext *sfc, Matlab::chart dummy_chart){
	set<mdl::event> ev_set = sfc->stateflow.events();
	for(set<mdl::event>::iterator ei = ev_set.begin(); ei != ev_set.end(); ei++)
	{
		mdl::event ev = *ei;

		Matlab::event t_ev = Matlab::event::Create(dummy_chart);
		EventPass1(sfc,ev,t_ev);
	}
}

/*
	7/25/01 encapsulate the create transition code into a function
  */
void GetTransitions(stateflowContext *sfc, Matlab::chart dummy_chart){
	set<mdl::transition> trans_set = sfc->stateflow.transitions();
	for(set<mdl::transition>::iterator tri = trans_set.begin(); tri != trans_set.end(); tri++)
	{
		mdl::transition trans = *tri;

		Matlab::transition t_trans = Matlab::transition::Create(dummy_chart);
		TransitionPass1(sfc,trans,t_trans);
	}
}

/*
	7/25/01 encapsulate the create junction code into a function
  */
void GetJunctions(stateflowContext *sfc, Matlab::chart dummy_chart){
	set<mdl::junction> junct_set = sfc->stateflow.junctions();
	for(set<mdl::junction>::iterator ji = junct_set.begin(); ji != junct_set.end(); ji++)
	{
		mdl::junction junct = *ji;

		Matlab::junction t_junct = Matlab::junction::Create(dummy_chart);
		JunctionPass1(sfc,junct,t_junct);
	}
}

/*
	7/25/01 encapsulate the create data code into a function
  */
void GetData(stateflowContext *sfc, Matlab::chart dummy_chart){
	set<mdl::data> data_set = sfc->stateflow.datas();
	for(set<mdl::data>::iterator di = data_set.begin(); di != data_set.end(); di++)
	{
		mdl::data datum = *di;

		Matlab::data t_datum = Matlab::data::Create(dummy_chart);
		DataPass1(sfc,datum,t_datum);
	}
}

void StateflowPass0(string root_name, Matlab::Simulink &t_topSim)
{
	if (stateflowList.empty()) return;

	Matlab::Stateflow t_sf = Matlab::Stateflow::Create(t_topSim);

	globalContext->dummy_machine = Matlab::machine::Create(t_sf);
	globalContext->dummy_chart = Matlab::chart::Create(globalContext->dummy_machine);

	mdl::machine mach = (*stateflowList.begin())->stateflow.machine();
	Matlab::machine t_mach = Matlab::machine::Create(t_sf);
	globalContext->stateflow = (*stateflowList.begin())->stateflow;
	
	globalContext->context->machine_map[t_mach] = mach;
	t_mach.name() = root_name;
	t_mach.id() = 1;
	
	stateflowContext *currsf;
	list<stateflowContext *>::iterator sfi;

	// check that all the charts are referenced!
	for (list<stateflowContext *>::reverse_iterator rsfi = stateflowList.rbegin(); rsfi != stateflowList.rend(); rsfi++){
		currsf = *rsfi;

		set<mdl::chart> chart_set = currsf->stateflow.charts();
		for(set<mdl::chart>::iterator ci = chart_set.begin(); ci != chart_set.end(); ci++) {
			mdl::chart cht = *ci;

			string pathname, chartname; pathname;
	
			chartname = SFParamLookup<mdl::chart>(cht,"name");
			// 12/27/01 kwc - Replace the string in chartname denoted as sfc->origsubsystemname
			//			with the string in sfc->refsubsystemname
			int pos = 0;
			
			if ((!currsf->origsubystemname.empty()) && (currsf->origsubystemname.compare(currsf->refsubsystemname) != 0)){
				pos = chartname.find(currsf->origsubystemname);
				// if found the original subsystem name, then replace it with the reference name
				while (pos != -1){
					chartname.erase(pos, currsf->origsubystemname.length());
					chartname.insert(pos, currsf->refsubsystemname);
					pos = chartname.find(currsf->origsubystemname, pos + 1);
				}
			}
			// 12/27/01
			chartname = DuplicateChartName(chartname);
			// locate the subsystem listed first in chartname
			pathname = FindSubsystem(chartname);
			// build the full path to this chart in chartname by prepending
			//	the path to the subsystem listed first in chartname
			if (!pathname.empty()){
				pathname.append(string("/"));
				chartname.insert(0, pathname);
			}
			else{
				// if the subsystem listed first in chartname is not found in
				//		ImportedSubSystemInfo, then
				// insert name of root system in front of the chart name
				chartname.insert(0, "/");
				chartname.insert(0, rootSystemName);
			}

			// We remove a stateflowlist member if there is no reference to that member among the blocktypes!
			if (stateflow_sfunctions_map.find(chartname) == stateflow_sfunctions_map.end()) stateflowList.remove(currsf);
		}
	}

	// Create the stateflow
	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetCharts(currsf, globalContext->dummy_machine);
	}

	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetStates(currsf, globalContext->dummy_chart);
	}

	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetTargets(currsf, globalContext->dummy_machine);
	}

	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetInstances(currsf, globalContext->dummy_machine);
	}

	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetEvents(currsf, globalContext->dummy_chart);
	}

	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetTransitions(currsf, globalContext->dummy_chart);
	}

	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetJunctions(currsf, globalContext->dummy_chart);
	}

	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetData(currsf, globalContext->dummy_chart);
	}
}

void StateflowPass1(mdl::Stateflow &sf, Matlab::Stateflow &t_sf)
{

	globalContext->dummy_machine = Matlab::machine::Create(t_sf);
	globalContext->dummy_chart = Matlab::chart::Create(globalContext->dummy_machine);

	mdl::machine mach = sf.machine();
	Matlab::machine t_mach = Matlab::machine::Create(t_sf);
	// 7/26/01 kwc
	globalContext->stateflow = sf;

	// 7/26/01 kwc
	MachinePass1(globalContext->context, mach,t_mach);

	stateflowContext *currsf;
	list<stateflowContext *>::iterator sfi;

	GetCharts(globalContext, globalContext->dummy_machine);
	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetCharts(currsf, globalContext->dummy_machine);
	}

	GetStates(globalContext, globalContext->dummy_chart);
	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetStates(currsf, globalContext->dummy_chart);
	}

	GetTargets(globalContext, globalContext->dummy_machine);

	GetInstances(globalContext, globalContext->dummy_machine);
	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetInstances(currsf, globalContext->dummy_machine);
	}

	GetEvents(globalContext, globalContext->dummy_chart);
	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetEvents(currsf, globalContext->dummy_chart);
	}

	GetTransitions(globalContext, globalContext->dummy_chart);
	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetTransitions(currsf, globalContext->dummy_chart);
	}

	GetJunctions(globalContext, globalContext->dummy_chart);
	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetJunctions(currsf, globalContext->dummy_chart);
	}

	GetData(globalContext, globalContext->dummy_chart);
	for (sfi = stateflowList.begin(); sfi != stateflowList.end(); sfi++){
		currsf = *sfi;
		GetData(currsf, globalContext->dummy_chart);
	}
}

void StateflowPass2(Matlab::Stateflow &t_sf)
{
	// fill in additional info on the chart objects, including hierarchy
	for(map<Matlab::chart, mdl::chart>::iterator cmi = globalContext->context->chart_map.begin(); cmi != globalContext->context->chart_map.end(); cmi++)
	{
		ChartPass2(globalContext, cmi->first);
	}

	for(map<Matlab::state, mdl::state>::iterator smi = globalContext->context->state_map.begin(); smi != globalContext->context->state_map.end(); smi++)
	{
		StatePass2(globalContext, smi->first);
	}

	for(map<Matlab::target, mdl::target>::iterator tmi = globalContext->context->target_map.begin(); tmi != globalContext->context->target_map.end(); tmi++)
	{
		TargetPass2(globalContext, tmi->first);
	}

	for(map<Matlab::event, mdl::event>::iterator emi = globalContext->context->event_map.begin(); emi != globalContext->context->event_map.end(); emi++)
	{
		EventPass2(globalContext, emi->first);
	}

	for(map<Matlab::data, mdl::data>::iterator dmi = globalContext->context->data_map.begin(); dmi != globalContext->context->data_map.end(); dmi++)
	{
		DataPass2(globalContext, dmi->first);
	}

	for(map<Matlab::instance, mdl::instance>::iterator imi = globalContext->context->instance_map.begin(); imi != globalContext->context->instance_map.end(); imi++)
	{
		InstancePass2(globalContext, imi->first);
	}

	for(map<Matlab::transition, mdl::transition>::iterator trmi = globalContext->context->transition_map.begin(); trmi != globalContext->context->transition_map.end(); trmi++)
	{
		TransitionPass2(globalContext, trmi->first);
	}

	for(map<Matlab::junction, mdl::junction>::iterator jmi = globalContext->context->junction_map.begin(); jmi != globalContext->context->junction_map.end(); jmi++)
	{
		JunctionPass2(globalContext, jmi->first);
	}

	// now do the same for the other contexts
	// go through the chart objects for the other contexts
	for(list<stateflowContext *>::iterator sfci = stateflowList.begin(); sfci != stateflowList.end(); sfci++){
		stateflowContext *sfc = *sfci;
		for(map<Matlab::chart, mdl::chart>::iterator cmi = sfc->context->chart_map.begin(); cmi != sfc->context->chart_map.end(); cmi++)
		{
			ChartPass2(sfc, cmi->first);
		}

		for(map<Matlab::state, mdl::state>::iterator smi = sfc->context->state_map.begin(); smi != sfc->context->state_map.end(); smi++)
		{
			StatePass2(sfc, smi->first);
		}

		for(map<Matlab::target, mdl::target>::iterator tmi = sfc->context->target_map.begin(); tmi != sfc->context->target_map.end(); tmi++)
		{
			TargetPass2(sfc, tmi->first);
		}

		for(map<Matlab::event, mdl::event>::iterator emi = sfc->context->event_map.begin(); emi != sfc->context->event_map.end(); emi++)
		{
			EventPass2(sfc, emi->first);
		}

		for(map<Matlab::data, mdl::data>::iterator dmi = sfc->context->data_map.begin(); dmi != sfc->context->data_map.end(); dmi++)
		{
			DataPass2(sfc, dmi->first);
		}

		for(map<Matlab::instance, mdl::instance>::iterator imi = sfc->context->instance_map.begin(); imi != sfc->context->instance_map.end(); imi++)
		{
			InstancePass2(sfc, imi->first);
		}

		for(map<Matlab::transition, mdl::transition>::iterator trmi = sfc->context->transition_map.begin(); trmi != sfc->context->transition_map.end(); trmi++)
		{
			TransitionPass2(sfc, trmi->first);
		}

		for(map<Matlab::junction, mdl::junction>::iterator jmi = sfc->context->junction_map.begin(); jmi != sfc->context->junction_map.end(); jmi++)
		{
			JunctionPass2(sfc, jmi->first);
		}
	}

	globalContext->dummy_chart.machine() = &Udm::_null;
	globalContext->dummy_machine.parent() = &Udm::_null;
}

void MachinePass1(ContextContainer *context, mdl::machine &mach, Matlab::machine &t_mach)
{
	context->machine_map[t_mach] = mach;

	t_mach.name() = SFParamLookup<mdl::machine>(mach,"name");
	t_mach.id() = atoi(SFParamLookup<mdl::machine>(mach,"id").c_str());
}

void MachinePass2(Matlab::machine &t_mach)
{
	// not needed
}

// 8/1/01 kwc
/*
	Located the subsystem referred to in the first part of name in
	importedSubSystemList and return the respective path.
  */
string FindSubsystem(string name){
	string subname, searchstr = "/", sysname;
	int pos = name.find_first_of(searchstr);

	searchstr.empty();
	subname.empty();
	if (pos == -1)
		return subname;

	subname = name.substr(0, pos);
	string type, refname;
	int namepos;

	for (list<ImportedSubSystemInfo *>::iterator issli = importedSubSystemList.begin(); issli != importedSubSystemList.end(); issli++){
		ImportedSubSystemInfo *issi = *issli;
/*		// 12/26/01 
		// first see if this block is a Reference
		type = SimParamLookup<mdl::Block>(issi->rootSubSystem,"BlockType");
		if (type.compare("Reference") == 0){
			// get the path to the referenced block
			refname = SimParamLookup<mdl::Block>(issi->rootSubSystem,"SourceBlock");
			sysname = issi->pathname;
			// locate the last '/'
			namepos = refname.find_last_of(searchstr);
			if (namepos == -1)
				sysname = refname;
			else{
				// extract the name of the referenced block that follows the last '/'
				sysname = refname.substr(namepos + 1, refname.length() - (namepos + 1));
			}
		}
		else
		// 12/26/01 kwc
*/			sysname = SimParamLookup<mdl::Block>(issi->rootSubSystem, "Name");

		refname.erase();
		type.erase();

		if (subname.compare(sysname) == 0){
			subname.erase();
			return issi->pathname;
		}
	}

	return subname.erase();
}
// 7/31/01 kwc
string DuplicateChartName(string chartname){
	string name, buildname;
	int pos=0, nextpos=0;

	nextpos = chartname.find_first_of("/");
	while (nextpos != -1){
		name.erase();
		name = chartname.substr(pos, nextpos - pos);
		buildname.append(name);
		buildname.append("/");
		buildname.append(name);
		buildname.append("/");
		pos = nextpos + 1;
		nextpos = chartname.find("/", pos);
	}
	name.erase();
	// handle last name after last '/'
	buildname.append(chartname.substr(pos, chartname.length() - pos));
	buildname.append("/");
	buildname.append(chartname.substr(pos, chartname.length() - pos));

	return buildname;
}

void ChartPass1(stateflowContext *sfc, mdl::chart &cht, Matlab::chart &t_cht)
{
	string pathname, chartname; pathname;
	sfc->context->chart_map[t_cht] = cht;
	
	chartname = SFParamLookup<mdl::chart>(cht,"name");
	// 12/27/01 kwc - Replace the string in chartname denoted as sfc->origsubsystemname
	//			with the string in sfc->refsubsystemname
	int pos = 0;
	
	if ((!sfc->origsubystemname.empty()) && (sfc->origsubystemname.compare(sfc->refsubsystemname) != 0)){
		pos = chartname.find(sfc->origsubystemname);
		// if found the original subsystem name, then replace it with the reference name
		while (pos != -1){
			chartname.erase(pos, sfc->origsubystemname.length());
			chartname.insert(pos, sfc->refsubsystemname);
			pos = chartname.find(sfc->origsubystemname, pos + 1);
		}
	}
	// 12/27/01
	chartname = DuplicateChartName(chartname);
	// locate the subsystem listed first in chartname
	pathname = FindSubsystem(chartname);
	// build the full path to this chart in chartname by prepending
	//	the path to the subsystem listed first in chartname
	if (!pathname.empty()){
		pathname.append(string("/"));
		chartname.insert(0, pathname);
	}
	else{
		// if the subsystem listed first in chartname is not found in
		//		ImportedSubSystemInfo, then
		// insert name of root system in front of the chart name
		chartname.insert(0, "/");
		chartname.insert(0, rootSystemName);
	}

	pathname.erase();
	t_cht.name() = chartname;
	t_cht.id() = atoi(SFParamLookup<mdl::chart>(cht,"id").c_str());
	t_cht.decomposition() = SFParamLookup<mdl::chart>(cht,"decomposition");
	t_cht.updateMethod() = SFParamLookup<mdl::chart>(cht,"updateMethod");

	string sampleTime_str = SFParamLookup<mdl::chart>(cht,"sampleTime");
	t_cht.sampleTime() = atof(sampleTime_str.c_str());

}

void ChartPass2(stateflowContext *sfc, const Matlab::chart &t_cht)
{
	mdl::chart cht;
	map<Matlab::chart,mdl::chart>::iterator mapPos = sfc->context->chart_map.find(t_cht);
	if(mapPos != sfc->context->chart_map.end())
	{
		cht = mapPos->second;
	}

	string machine_id = SFParamLookup<mdl::chart>(cht,"machine");
	string this_id = SFParamLookup<mdl::chart>(cht,"id");

	bool parent_found = false;
	// 7/26/01 look up the machine from the global machine map
	for(map<Matlab::machine, mdl::machine>::iterator i = globalContext->context->machine_map.begin(); i != globalContext->context->machine_map.end(); i++)
	{
		mdl::machine currMach = i->second;
		string currMach_id = SFParamLookup<mdl::machine>(currMach,"id");
		if(machine_id.compare(currMach_id) == 0)
		{
			// set this chart as a child of the machine
			t_cht.machine() = i->first; 
			parent_found = true;
			break;
		}
	}

	for(map<Matlab::instance, mdl::instance>::iterator j = sfc->context->instance_map.begin(); j != sfc->context->instance_map.end(); j++)
	{
		mdl::instance currInst = j->second;
		string currInst_chart_id = SFParamLookup<mdl::instance>(currInst,"chart");
		if(this_id.compare(currInst_chart_id) == 0)
		{
			t_cht.instance() = j->first;
		}
	}

}

void StatePass1(stateflowContext *sfc, mdl::state &st, Matlab::state &t_st)
{
	sfc->context->state_map[t_st] = st;

	t_st.type() = SFParamLookup<mdl::state>(st,"type");
	t_st.decomposition() = SFParamLookup<mdl::state>(st,"decomposition");
	t_st.position() = SFParamLookup<mdl::state>(st,"position");
	t_st.id() = atoi(SFParamLookup<mdl::state>(st,"id").c_str());
}

void StatePass2(stateflowContext *sfc, const Matlab::state &t_st)
{
	mdl::state st;
	map<Matlab::state,mdl::state>::iterator mapPos = sfc->context->state_map.find(t_st);
	if(mapPos != sfc->context->state_map.end())
	{
		st = mapPos->second;
	}

	string labelString = SFParamLookup<mdl::state>(st,"labelString");
	string name, entryAction, exitAction, duringAction, eventActions;
	string type = t_st.type();

	const npos = -1;
	if(!labelString.empty() && (type == "AND_STATE" || type == "OR_STATE" || type == "FUNC_STATE")) {

		int p;
		// strip out line continuation '...' followed by a line feed
		while((p =labelString.find("...\\n")) != npos) { labelString.erase(p,5); }
		// strip out line continuation '...' without line feeds
		while((p =labelString.find("...")) != npos) { labelString.erase(p,3); }
		while((p =labelString.find("\\n")) != npos) { labelString.replace(p,2,";"); }

		p = labelString.find_first_of("/;\n");
		if(p == npos) p = labelString.length();

		name = labelString.substr(0,p);

		string str = labelString.substr(p + (labelString[p] == '\0' ? 0 : 1));
		string *target = &entryAction;
		while(str.length()) {
			int j;
			p = str.find_first_of(";\n");
			if(p == npos) p = str.length();

			for(j = 0; str[j] == ' '; j++);		// skip leading spaces
			string act = str.substr(j,p-j);
			str = str.substr(p + (str[p] == '\0' ? 0 : 1));   // do not forget to increment by 2 if '\\n'
			if(p == j) continue; 							  // empty or all space?
			
			int l = act.find_first_of(":([");					// ':' is a label only if before ( or [
			if(l != npos && act[l] == ':') {
				std::string keyword= act.substr( 0, l);
				trim( keyword);
				if (( "entry" == keyword) || ( "en" == keyword)) target = &entryAction;
				//if(act.compare(0, 6, "entry:") == 0 || act.compare(0, 3, "en:") == 0) target = &entryAction;
				else if ( ( "during" == keyword) || ( "dur" == keyword) || ( "du" == keyword)) target= &duringAction;
				//else if(act.compare(0, 7, "during:") == 0 || act.compare(0, 4, "dur:") == 0 || act.compare(0, 3, "du:") == 0)
				//	target = &duringAction;
				else if ( ( "exit" == keyword) || ( "ex" == keyword)) target= &exitAction;
				//else if(act.compare(0, 5, "exit:") == 0 || act.compare(0, 3, "ex:") == 0) target = &exitAction;
				else if(act.compare(0, 2, "on") == 0)  {
														target = &eventActions;
														target->append( act.substr(0,l+1));
				}	
				else {
					throw udm_exception("State text: Unknown label: " + act+ " (" + keyword + ")"); 
				}
				while(act[++l] == ' ');		// skip leading spaces
				act = act.substr(l);
				if(act.length() == 0) continue;
			}

			if(!target) throw udm_exception("State text: Instruction before label: " + labelString); 
			target->append(act);
			target->append(";");
			// 8/30/01 kwc
			//	The new line character is appended so that the ecsl2rtk parser
			//	understands that this is a command that ends at the new line.
			target->append("\\n");
			// 8/30/01 kwc
		}
		// do some post processing on the strings
		// 8/30/01 kwc
		unsigned int l;
		// strip out the trailing newline char. cause it will cause problems with parser
		//	in escl to rtk translator
		l = name.rfind("\\n");
		if (l == (name.length() - 2))
			name.replace(l,2,"");

		l = entryAction.rfind("\\n");
		if (l == (entryAction.length() - 2))
			entryAction.replace(l,2,"");

		l = exitAction.rfind("\\n");
		if (l == (exitAction.length() - 2))
			exitAction.replace(l,2,"");

		l = duringAction.rfind("\\n");
		if (l == (duringAction.length() - 2))
			duringAction.replace(l,2,"");

		l = eventActions.rfind("\\n");
		if (l == (eventActions.length() - 2))
			eventActions.replace(l,2,"");
		// 8/30/01 kwc


		labelString = "";    // make sure not executed twice
		t_st.name() = name;
		t_st.entryAction() = entryAction;
		t_st.exitAction() = exitAction;
		t_st.duringAction() = duringAction;
		t_st.eventActions() = eventActions;

	}

	// state's parent may be chart (if a root state) or another state

	string treeNode = SFParamLookup<mdl::state>(st,"treeNode");

	int start_num = 0;
	int end_num = 0;
	int counter = 0;

	bool found_start = false;
	string parent_id;

	for(string::iterator j = treeNode.begin(); j != treeNode.end(); j++) 
	{
		if(!found_start)
		{
			if(*j >= '0' && *j <= '9')
			{
				found_start = true;
				start_num = counter;
			}
		}
		else
		{
			if(!(*j >= '0' && *j <= '9'))
			{
				end_num = counter;
				break;
			}
		}

		counter++;
	}

	parent_id = treeNode.substr(start_num, end_num - start_num);

	// search the charts first, then the states
	bool parent_found = false;
	for(map<Matlab::chart,mdl::chart>::iterator ci = sfc->context->chart_map.begin(); ci != sfc->context->chart_map.end(); ci++)
	{
		mdl::chart currChart = ci->second;
		string currChart_id = SFParamLookup<mdl::chart>(currChart,"id");
		if(parent_id.compare(currChart_id) == 0)
		{
			// set this state as a child of the chart
			t_st.parentChart() = ci->first;
			parent_found = true;
			break;

		}
	}

	if(!parent_found)
	{
		for(map<Matlab::state,mdl::state>::iterator si = sfc->context->state_map.begin(); si != sfc->context->state_map.end(); si++)
		{
			mdl::state currState = si->second;
			string currState_id = SFParamLookup<mdl::state>(currState,"id");
			if(parent_id.compare(currState_id) == 0)
			{
				// set this state as a child of the located state
				t_st.parentState() = si->first;
				parent_found = true;
				break;

			}
		}

	}

}

void TargetPass1(stateflowContext *sfc, mdl::target &targ, Matlab::target &t_targ)
{
	sfc->context->target_map[t_targ] = targ;

	t_targ.name() = SFParamLookup<mdl::target>(targ,"name");
	t_targ.id() = atoi(SFParamLookup<mdl::target>(targ,"id").c_str());
	t_targ.description() = SFParamLookup<mdl::target>(targ,"description");
	t_targ.codeCommand() = SFParamLookup<mdl::target>(targ,"codeCommand");
	t_targ.makeCommand() = SFParamLookup<mdl::target>(targ,"makeCommand");
	t_targ.codeFlags() = SFParamLookup<mdl::target>(targ,"codeFlags");
	t_targ.checksumOld() = SFParamLookup<mdl::target>(targ,"checksumOld");

}

void TargetPass2(stateflowContext *sfc, const Matlab::target &t_targ)
{
	mdl::target targ;
	map<Matlab::target,mdl::target>::iterator mapPos = sfc->context->target_map.find(t_targ);
	if(mapPos != sfc->context->target_map.end())
	{
		targ = mapPos->second;
	}

	string machine_id = SFParamLookup<mdl::target>(targ,"machine");
	bool parent_found = false;
	for(map<Matlab::machine,mdl::machine>::iterator mi = globalContext->context->machine_map.begin(); mi != globalContext->context->machine_map.end(); mi++)
	{
		mdl::machine currMach = mi->second;
		string currMach_id = SFParamLookup<mdl::machine>(currMach,"id");
		
		if(machine_id.compare(currMach_id) == 0)
		{
			// set this target as a child of the machine
			t_targ.parent() = mi->first;
			parent_found = true;
			break;
		}
	}
}

// 8/1/01 kwc
string FindChartName(stateflowContext *sfc, string chartname){
	string disName;

	for(map<Matlab::chart,mdl::chart>::iterator cm = sfc->context->chart_map.begin(); cm != sfc->context->chart_map.end(); cm++){
		disName = SFParamLookup<mdl::chart>(cm->second, "name");
		if(chartname.compare(disName) == 0){
			disName.erase();
			return cm->first.name();
		}
	}

	disName.erase();
	return chartname;
}
// 8/1/01 kwc

void InstancePass1(stateflowContext *sfc, mdl::instance &inst, Matlab::instance &t_inst)
{
	sfc->context->instance_map[t_inst] = inst;
	
	t_inst.name() = FindChartName(sfc, SFParamLookup<mdl::instance>(inst,"name"));
	t_inst.id() = atoi(SFParamLookup<mdl::instance>(inst,"id").c_str());
}

void InstancePass2(stateflowContext *sfc, const Matlab::instance &t_inst)
{
	mdl::instance inst;
	map<Matlab::instance,mdl::instance>::iterator mapPos = sfc->context->instance_map.find(t_inst);
	if(mapPos != sfc->context->instance_map.end())
	{
		inst = mapPos->second;
	}

	string machine_id = SFParamLookup<mdl::instance>(inst,"machine");
	bool parent_found = false;
	for(map<Matlab::machine,mdl::machine>::iterator mi = globalContext->context->machine_map.begin(); mi != globalContext->context->machine_map.end(); mi++)
	{
		mdl::machine currMach = mi->second;
		string currMach_id = SFParamLookup<mdl::machine>(currMach,"id");
		
		if(machine_id.compare(currMach_id) == 0)
		{
			// set this target as a child of the machine
			t_inst.parent() = mi->first;
			parent_found = true;
			break;
		}
	}

	bool chart_found = false;
	string chart_id = SFParamLookup<mdl::instance>(inst,"chart");
	for(map<Matlab::chart,mdl::chart>::iterator ci = sfc->context->chart_map.begin(); ci != sfc->context->chart_map.end(); ci++)
	{
		mdl::chart currChart = ci->second;
		string currChart_id = SFParamLookup<mdl::chart>(currChart,"id");

		if(chart_id.compare(currChart_id) == 0)
		{
			t_inst.owner() = ci->first;
			chart_found = true;
			break;
		}
	}

}

void EventPass1(stateflowContext *sfc, mdl::event &ev, Matlab::event &t_ev)
{
	sfc->context->event_map[t_ev] = ev;

	t_ev.name() = SFParamLookup<mdl::event>(ev,"name");
	t_ev.id() = atoi(SFParamLookup<mdl::event>(ev,"id").c_str());
	t_ev.scope() = SFParamLookup<mdl::event>(ev,"scope");
	t_ev.trigger() = SFParamLookup<mdl::event>(ev,"trigger");
	t_ev.description() = SFParamLookup<mdl::event>(ev,"description");

}

void EventPass2(stateflowContext *sfc, const Matlab::event &t_ev)
{
	mdl::event ev;
	map<Matlab::event,mdl::event>::iterator mapPos = sfc->context->event_map.find(t_ev);
	if(mapPos != sfc->context->event_map.end())
	{
		ev = mapPos->second;
	}

	string linkNode = SFParamLookup<mdl::event>(ev,"linkNode");

	int start_num = 0;
	int end_num = 0;
	int counter = 0;

	bool found_start = false;
	string parent_id;

	for(string::iterator j = linkNode.begin(); j != linkNode.end(); j++) 
	{
		if(!found_start)
		{
			if(*j >= '0' && *j <= '9')
			{
				found_start = true;
				start_num = counter;
			}
		}
		else
		{
			if(!(*j >= '0' && *j <= '9'))
			{
				end_num = counter;
				break;
			}
		}

		counter++;
	}

	parent_id = linkNode.substr(start_num, end_num - start_num);

	// search the machine, charts, and states
	bool parent_found = false;
	for(map<Matlab::machine,mdl::machine>::iterator mi = globalContext->context->machine_map.begin(); mi != globalContext->context->machine_map.end(); mi++)
	{
		mdl::machine currMach = mi->second;
		string currMach_id = SFParamLookup<mdl::machine>(currMach,"id");
		
		if(parent_id.compare(currMach_id) == 0)
		{
			// set this event as a child of the machine
			t_ev.parentMachine() = mi->first;
			parent_found = true;
			break;
		}
	}

	if(!parent_found)
	{
		for(map<Matlab::chart,mdl::chart>::iterator ci = sfc->context->chart_map.begin(); ci != sfc->context->chart_map.end(); ci++)
		{
			mdl::chart currChart = ci->second;
			string currChart_id = SFParamLookup<mdl::chart>(currChart,"id");

			if(parent_id.compare(currChart_id) == 0)
			{
				// set this event as a child of the chart
				t_ev.parentChart() = ci->first;
				parent_found = true;
				break;
			}
		}
	}

	if(!parent_found)
	{
		for(map<Matlab::state,mdl::state>::iterator si = sfc->context->state_map.begin(); si != sfc->context->state_map.end(); si++)
		{
			mdl::state currState = si->second;
			string currState_id = SFParamLookup<mdl::state>(currState,"id");

			if(parent_id.compare(currState_id) == 0)
			{
				// set this event as a child of the state
				t_ev.parentState() = si->first;
				parent_found = true;
				break;
			}
		}

	}

}

void TransitionPass1(stateflowContext *sfc, mdl::transition &trans, Matlab::transition &t_trans)
{
	sfc->context->transition_map[t_trans] = trans;

	t_trans.dataLimits() = SFParamLookup<mdl::transition>(trans,"dataLimits");
	t_trans.id() = atoi(SFParamLookup<mdl::transition>(trans,"id").c_str());
}

void TransitionPass2(stateflowContext *sfc, const Matlab::transition &t_trans)
{
	mdl::transition trans;
	map<Matlab::transition,mdl::transition>::iterator mapPos = sfc->context->transition_map.find(t_trans);
	if(mapPos != sfc->context->transition_map.end())
	{
		trans = mapPos->second;
	}

	string labelString = SFParamLookup<mdl::transition>(trans,"labelString");
	string condition;
	string trigger;
	string conditionAction;
	string action;
	string id = SFParamLookup<mdl::transition>(trans,"id");

	const npos = -1;
	
	// get rid of all newlines
	size_t ip;
	// 8/30/01 kwc
	// strip out '...\n'  This is line continuation in Matlab
	while ((ip = labelString.find("...\\n")) != string::npos) { labelString.erase(ip,5); }
	// strip out '...'  This is line continuation in Matlab with a new line suffix
	while ((ip = labelString.find("...")) != string::npos) { labelString.erase(ip,3); }
	// 8/30/01 kwc

	// 8/30/01 kwc  comment out stripping new line character cause Sandeep's
	//			ecsl parser uses new line character to indicate multiple commands
/*	ip = labelString.find("\\n");
	while(ip != string::npos)
	{
		labelString.replace(ip,2,"");
		ip = labelString.find("\\n",++ip);
	}
*/
	bool success= parseTransitionLabel( labelString, trigger, condition, conditionAction, action);
	if ( !success) {
		throw udm_exception("Invalid transition label at id: " + id);
	}
	trim(condition);
	trim(conditionAction);
	trim(action);
/* VIZA commented out on 09/09/04

	int p = labelString.find_first_of("[{");
	if(p == npos) p = labelString.length();
	trigger = labelString.substr(0,p);

	int i = labelString.find_first_of("[");
	if(i != npos) {
		int i2 = labelString.find("]", i);
		if(i2 == npos) throw udm_exception("Invalid transition label at id: " + id);
		condition = labelString.substr(i+1,i2-(i+1));
		trim(condition);
	}

	i = labelString.find_first_of("{");
	if(i != npos) {
		int i2 = labelString.find("}", i);
		if(i2 == npos) throw udm_exception("Invalid transition label at id: " + id);
		conditionAction = labelString.substr(i+1,i2-(i+1));
		trim(conditionAction);
	}
	
	i = labelString.find_first_of("[{");
	if(i != npos) {

		int i2 = labelString.find("]", i);
		if(i2 == npos) i2 = labelString.find("}", i);

		int i3 = labelString.find("/", i2);
		if(i3 != npos) {
			action = labelString.substr(i3+1);
			trim(action);
		}
	}

//	cout << trigger << ":" << condition << ":" << conditionAction << ":" << action << endl;
	
End VIZA comment*/

/*
	if(labelString[p] == '[') {
		int p2 = labelString.find("]", p);
		if(p2 == npos) throw udm_exception("Invalid transition label at id: " + id);
		condition = labelString.substr(p+1,p2-(p+1));
		trim(condition);
		p = p2+1;
		while(isspace(labelString[p])) p++;
	}
	if(labelString[p] == '{') {
		int p2 = labelString.find("}", p);
		if(p2 == npos) throw udm_exception("Invalid transition label at id: " + id);
		conditionAction = labelString.substr(p+1,p2-(p+1));
		trim(conditionAction);
		p = p2+1;
		while(isspace(labelString[p])) p++;
	}
	if(labelString[p] == '/') {
		action = labelString.substr(p+1);
		trim(action);
	}
	else if(labelString[p] != '\0') {
		cout << "Unidentified segment at position " << p << " in transition label: " << labelString << endl; 
	}
*/	

	
	t_trans.trigger() = trigger;
	t_trans.condition() = condition;
	t_trans.conditionAction() = conditionAction;
	t_trans.action() = action;
	
	string linkNode = SFParamLookup<mdl::transition>(trans,"linkNode");

	int start_num = 0;
	int end_num = 0;
	int counter = 0;

	bool found_start = false;
	string parent_id;

	for(string::iterator j = linkNode.begin(); j != linkNode.end(); j++) 
	{
		if(!found_start)
		{
			if(*j >= '0' && *j <= '9')
			{
				found_start = true;
				start_num = counter;
			}
		}
		else
		{
			if(!(*j >= '0' && *j <= '9'))
			{
				end_num = counter;
				break;
			}
		}

		counter++;
	}

	parent_id = linkNode.substr(start_num, end_num - start_num);

	// search the charts, then the states
	bool parent_found = false;
	for(map<Matlab::chart,mdl::chart>::iterator ci = sfc->context->chart_map.begin(); ci != sfc->context->chart_map.end(); ci++)
	{
		mdl::chart currChart = ci->second;
		string currChart_id = SFParamLookup<mdl::chart>(currChart,"id");

		if(parent_id.compare(currChart_id) == 0)
		{
			// set this transition as a child of the chart
			t_trans.parentChart() = ci->first;
			parent_found = true;
			break;
		}
	}
	

	if(!parent_found)
	{
		for(map<Matlab::state,mdl::state>::iterator si = sfc->context->state_map.begin(); si != sfc->context->state_map.end(); si++)
		{
			mdl::state currState = si->second;
			string currState_id = SFParamLookup<mdl::state>(currState,"id");

			if(parent_id.compare(currState_id) == 0)
			{
				// set this transition as a child of the state
				t_trans.parentState() = si->first;
				parent_found = true;
				break;
			}
		}

	}

	// resolve src 

	string src_id;
	string src_intersection; // !!VIZA sourceoclock processing
	mdl::src t_src = trans.src();

	if(t_src)
	{
		src_id = SFParamLookup< mdl::src>(t_src,"id");
		src_intersection= SFParamLookup< mdl::src>( t_src, "intersection"); // !!VIZA sourceoclock processing
	}
	// search the junctions, then the states
	bool src_is_junction = false;

	for(map<Matlab::junction,mdl::junction>::iterator sji = sfc->context->junction_map.begin(); sji != sfc->context->junction_map.end(); sji++)
	{
		mdl::junction currJunct = sji->second;
		string currJunct_id = SFParamLookup<mdl::junction>(currJunct,"id");
		if(src_id.compare(currJunct_id) == 0)
		{
			t_trans.src() = sji->first;
			src_is_junction = true;
			break;
		}
	}
	bool src_is_state = false;
	if(!src_is_junction)
	{
		for(map<Matlab::state,mdl::state>::iterator ssti = sfc->context->state_map.begin(); ssti != sfc->context->state_map.end(); ssti++)
		{
			mdl::state currState = ssti->second;
			string currState_id = SFParamLookup<mdl::state>(currState,"id");
			if(src_id.compare(currState_id) == 0)
			{
				t_trans.src() = ssti->first;
				src_is_state= true;
				break;
			}
		}

	}

	// !!VIZA sourceoclock processing
	if ( false == src_intersection.empty())
	{
		double sourceoclock= 0.0;
		if ( src_is_junction) {
			sourceoclock= processJunctionIntersection( src_intersection);
		}
		if ( src_is_state) {
			sourceoclock= processStateIntersection( src_intersection);
		}
		t_trans.sourceOClock()= sourceoclock;
	}

	
	
	// resolve dst

	string dst_id;

	mdl::dst t_dst = trans.dst();

	if(t_dst)
	{
		dst_id = SFParamLookup<mdl::dst>(t_dst,"id");
	}

	// search the junctions, then the states
	bool dst_id_found = false;
	for(map<Matlab::junction,mdl::junction>::iterator dji = sfc->context->junction_map.begin(); dji != sfc->context->junction_map.end(); dji++)
	{
		mdl::junction currJunct = dji->second;
		string currJunct_id = SFParamLookup<mdl::junction>(currJunct,"id");
		if(dst_id.compare(currJunct_id) == 0)
		{
			t_trans.dst() = dji->first;
			dst_id_found = true;
			break;
		}
	}

	if(!dst_id_found)
	{
		for(map<Matlab::state,mdl::state>::iterator dsti = sfc->context->state_map.begin(); dsti != sfc->context->state_map.end(); dsti++)
		{
			mdl::state currState = dsti->second;
			string currState_id = SFParamLookup<mdl::state>(currState,"id");
			if(dst_id.compare(currState_id) == 0)
			{
				t_trans.dst() = dsti->first;
				dst_id_found = true;
				break;
			}
		}

	}

}

void JunctionPass1(stateflowContext *sfc, mdl::junction &junct, Matlab::junction &t_junct)
{
	sfc->context->junction_map[t_junct] = junct;

	t_junct.type() = SFParamLookup<mdl::junction>(junct,"type");
	t_junct.position() = SFParamLookup<mdl::junction>(junct,"position");
	t_junct.id() = atoi(SFParamLookup<mdl::junction>(junct,"id").c_str());
}

void JunctionPass2(stateflowContext *sfc, const Matlab::junction &t_junct)
{
	mdl::junction junct;
	map<Matlab::junction,mdl::junction>::iterator mapPos = sfc->context->junction_map.find(t_junct);
	if(mapPos != sfc->context->junction_map.end())
	{
		junct = mapPos->second;
	}

	string linkNode = SFParamLookup<mdl::junction>(junct,"linkNode");

	int start_num = 0;
	int end_num = 0;
	int counter = 0;

	bool found_start = false;
	string parent_id;

	for(string::iterator j = linkNode.begin(); j != linkNode.end(); j++) 
	{
		if(!found_start)
		{
			if(*j >= '0' && *j <= '9')
			{
				found_start = true;
				start_num = counter;
			}
		}
		else
		{
			if(!(*j >= '0' && *j <= '9'))
			{
				end_num = counter;
				break;
			}
		}

		counter++;
	}

	parent_id = linkNode.substr(start_num, end_num - start_num);

	// search the charts, then the states
	bool parent_found = false;

	for(map<Matlab::chart,mdl::chart>::iterator ci = sfc->context->chart_map.begin(); ci != sfc->context->chart_map.end(); ci++)
	{
		mdl::chart currChart = ci->second;
		string currChart_id = SFParamLookup<mdl::chart>(currChart,"id");

		if(parent_id.compare(currChart_id) == 0)
		{
			// set this junction as a child of the chart
			t_junct.parentChart() = ci->first;
			parent_found = true;
			break;
		}
	}
	

	if(!parent_found)
	{
		for(map<Matlab::state,mdl::state>::iterator si = sfc->context->state_map.begin(); si != sfc->context->state_map.end(); si++)
		{
			mdl::state currState = si->second;
			string currState_id = SFParamLookup<mdl::state>(currState,"id");

			if(parent_id.compare(currState_id) == 0)
			{
				// set this junction as a child of the state
				t_junct.parentState() = si->first;
				parent_found = true;
				break;
			}
		}

	}

}

void DataPass1(stateflowContext *sfc, mdl::data &datum, Matlab::data &t_datum)
{
	sfc->context->data_map[t_datum] = datum;

	t_datum.name() = SFParamLookup<mdl::data>(datum,"name");
	t_datum.id() = atoi(SFParamLookup<mdl::data>(datum,"id").c_str());
	t_datum.scope() = SFParamLookup<mdl::data>(datum,"scope");
	t_datum.dataType() = SFParamLookup<mdl::data>(datum,"dataType");
	t_datum.description() = SFParamLookup<mdl::data>(datum,"description");
	t_datum.units() = SFParamLookup<mdl::data>(datum,"units");

	mdl::props prop = datum.props();
	if(prop)
	{
		Matlab::props t_prop = Matlab::props::Create(t_datum);
		PropsPass1(prop,t_prop);
	}
}

void DataPass2(stateflowContext *sfc, const Matlab::data &t_datum)
{
	mdl::data datum;
	map<Matlab::data,mdl::data>::iterator mapPos = sfc->context->data_map.find(t_datum);
	if(mapPos != sfc->context->data_map.end())
	{
		datum = mapPos->second;
	}

	string linkNode = SFParamLookup<mdl::data>(datum,"linkNode");

	int start_num = 0;
	int end_num = 0;
	int counter = 0;

	bool found_start = false;
	string parent_id;

	for(string::iterator j = linkNode.begin(); j != linkNode.end(); j++) 
	{
		if(!found_start)
		{
			if(*j >= '0' && *j <= '9')
			{
				found_start = true;
				start_num = counter;
			}
		}
		else
		{
			if(!(*j >= '0' && *j <= '9'))
			{
				end_num = counter;
				break;
			}
		}

		counter++;
	}

	parent_id = linkNode.substr(start_num, end_num - start_num);

	// search the machine, charts, and states
	bool parent_found = false;
	for(map<Matlab::machine,mdl::machine>::iterator mi = globalContext->context->machine_map.begin(); mi != globalContext->context->machine_map.end(); mi++)
	{
		mdl::machine currMach = mi->second;
		string currMach_id = SFParamLookup<mdl::machine>(currMach,"id");
		
		if(parent_id.compare(currMach_id) == 0)
		{
			// set this data as a child of the machine
			t_datum.parentMachine() = mi->first;
			parent_found = true;
			break;
		}
	}

	if(!parent_found)
	{
		for(map<Matlab::chart,mdl::chart>::iterator ci = sfc->context->chart_map.begin(); ci != sfc->context->chart_map.end(); ci++)
		{
			mdl::chart currChart = ci->second;
			string currChart_id = SFParamLookup<mdl::chart>(currChart,"id");

			if(parent_id.compare(currChart_id) == 0)
			{
				// set this data as a child of the chart
				t_datum.parentChart() = ci->first;
				parent_found = true;
				break;
			}
		}
	}

	if(!parent_found)
	{
		for(map<Matlab::state,mdl::state>::iterator si = sfc->context->state_map.begin(); si != sfc->context->state_map.end(); si++)
		{
			mdl::state currState = si->second;
			string currState_id = SFParamLookup<mdl::state>(currState,"id");

			if(parent_id.compare(currState_id) == 0)
			{
				// set this data as a child of the state
				t_datum.parentState() = si->first;
				parent_found = true;
				break;
			}
		}

	}

}

void PropsPass1(mdl::props &prop, Matlab::props &t_prop)
{
	t_prop.initialValue() = SFParamLookup<mdl::props>(prop,"initialValue");

	mdl::range rng = prop.range();
	if(rng)
	{
		Matlab::range t_rng = Matlab::range::Create(t_prop);
		RangePass1(rng,t_rng);
	}

	set<mdl::array> arr_set = prop.arrays();
	for(set<mdl::array>::iterator ai = arr_set.begin(); ai != arr_set.end(); ai++)
	{
		mdl::array arr = *ai;
		Matlab::array t_arr = Matlab::array::Create(t_prop);
		ArrayPass1(arr,t_arr);
	}
	
}

void PropsPass2(Matlab::props &t_prop)
{
	// nothing to do here
}

void RangePass1(mdl::range &rng, Matlab::range &t_rng)
{
	t_rng.minimum() = SFParamLookup<mdl::range>(rng,"minimum");
	t_rng.maximum() = SFParamLookup<mdl::range>(rng,"maximum");
}

void RangePass2(Matlab::range &t_rng)
{
	// nothing to do here
}

void ArrayPass1(mdl::array &arr, Matlab::array &t_arr)
{
	string size_str = SFParamLookup<mdl::array>(arr,"size");
	t_arr.size() = atoi(size_str.c_str());
	string fi_str = SFParamLookup<mdl::array>(arr,"firstIndex");
	t_arr.firstIndex() = atoi(fi_str.c_str());
}

void ArrayPass2(Matlab::array &t_arr)
{
	// nothing to do here
}

void ParameterPass1(mdl::parameter &par, Matlab::Text &t_par)
{
	t_par.Name() = par.name();
	t_par.Value() = par.value();
}

void Cleanup()
{
	rootSystemName.erase();
	for (list<ImportedSubSystemInfo *>::iterator issi = importedSubSystemList.begin(); issi != importedSubSystemList.end(); issi++){
		ImportedSubSystemInfo *subsys = *issi;
		subsys->pathname.erase();
		delete subsys;
	}
	
	importedSubSystemList.clear();

	for(list<stateflowContext *>::iterator sfci = stateflowList.begin(); sfci != stateflowList.end(); sfci++){
		stateflowContext *sfc = *sfci;
		sfc->context->chart_map.clear();
		sfc->context->data_map.clear();
		sfc->context->event_map.clear();
		sfc->context->instance_map.clear();
		sfc->context->junction_map.clear();
		sfc->context->machine_map.clear();
		sfc->context->state_map.clear();
		sfc->context->target_map.clear();
		sfc->context->transition_map.clear();
		delete sfc->context;
		delete sfc;
	}

	stateflowList.clear();
	block_map.clear();
	block_parameter_default_map.clear();

	globalContext->context->machine_map.clear();
	globalContext->context->chart_map.clear();
	globalContext->context->state_map.clear();
	globalContext->context->target_map.clear();
	globalContext->context->event_map.clear();
	globalContext->context->data_map.clear();
	globalContext->context->instance_map.clear();
	globalContext->context->transition_map.clear();
	globalContext->context->junction_map.clear();
	delete globalContext->context;
	delete globalContext;

	stateflow_sfunctions_map.clear();
}

//extern void testParser();

int main(int argc, char **argv)
{
	if(argc < 2) {
		cerr << "Usage: mdl2xml <mdlfilename>\n";
		return 1;
	}

//testParser();
//return 0;

	// set up list of ignorable block parameters
	const int N = sizeof(ignorableParamsStrs)/sizeof(ignorableParamsStrs[0]);
	for ( int i = 0; i < N; ++i) {
		ignorableParams.push_back( ignorableParamsStrs[i] );
		ignorableParamsForReferred.push_back( ignorableParamsStrs[i] );
	}
	const int M = sizeof(ignorableParamsStrsForReferred)/sizeof(ignorableParamsStrsForReferred[0]);
	for ( i = 0; i < M; ++i) ignorableParamsForReferred.push_back( ignorableParamsStrsForReferred[i] );

	try {
		string outfilename;

		UdmDom::DomDataNetwork mdl_net(mdl::diagram);
		mdl_net.CreateNew("_temp.xml", "mdl.xsd", mdl::Simulink::meta, Udm::CHANGES_LOST_DEFAULT);
		mdl::Simulink topSim = mdl::Simulink::Cast(mdl_net.GetRootObject());

		outfilename = argv[1];
		size_t extPos = outfilename.find(".mdl");
		if(extPos == string::npos) {
			extPos = outfilename.find(".MDL");
			if(extPos == string::npos) {
				cout << "Input file must have an extension of .mdl. Aborting." << endl;
				return(1);
			}
		}
		outfilename.replace(extPos,4,".xml");

		cerr << "Converting " << argv[1] << " to " << outfilename << "... \n";

		{
			ifstream ifs(argv[1]);
			if(!ifs) {
				cout << "Error opening input file " << argv[1] << ". Aborting." << endl;
				return(1);
			}
			MDLLexer lexer(ifs);
			MDLParser parser(lexer);

			parser.start(topSim);
			ifs.close();
		}


		UdmDom::DomDataNetwork matlab_net(Matlab::diagram);
		matlab_net.CreateNew(outfilename, "Matlab.xsd", Matlab::Simulink::meta, Udm::CHANGES_PERSIST_ALWAYS);
		Matlab::Simulink t_topSim = Matlab::Simulink::Cast(matlab_net.GetRootObject());

		globalContext = new stateflowContext();
		globalContext->context = new ContextContainer();
		{		
			SimulinkPass1(topSim,t_topSim);
			SimulinkPass2(t_topSim);
			Cleanup();
		}

		mdl_net.CloseWithUpdate();
	} catch(const udm_exception &e) {
		cout << "Exception: " << e.what() << endl;
		return 1;
		throw;
	}
	cerr << "\nDone." << endl;
	return 0;
}
