#ifndef MOBIES_ECSL_DP_H
#define MOBIES_ECSL_DP_H
// header file ECSL_DP.h generated from diagram ECSL_DP
// generated on Mon Aug 09 15:08:35 2004

#ifndef MOBIES_UDMBASE_H
#include "UdmBase.h"
#endif



namespace ECSL_DP {

	class  Block;
	class  Primitive;
	class  TriggerPort;
	class  Parameter;
	class  Port;
	class  Reference;
	class  Annotation;
	class  Line;
	class  InputPort;
	class  OutputPort;
	class  Dataflow;
	class  EnablePort;
	class  System;
	class  State;
	class  Transition;
	class  Event;
	class  History;
	class  TransStart;
	class  Junction;
	class  Stateflow;
	class  Data;
	class  TransConnector;
	class  ConnectorRef;
	class  RTConstraint;
	class  RTCOut;
	class  RTCIn;
	class  SystemRef;
	class  InPortMapping;
	class  ComponentShortcut;
	class  ComponentSheet;
	class  Signal;
	class  COutPort;
	class  CInPort;
	class  ComponentModels;
	class  OutPortMapping;
	class  CPort;
	class  Component;
	class  COM;
	class  FirmwareLink;
	class  FirmwareModule;
	class  OS;
	class  Wire;
	class  Bus;
	class  BusChan;
	class  CommElement;
	class  HWElement;
	class  HardwareSheet;
	class  HardwareModels;
	class  Channel;
	class  ECU;
	class  IChan;
	class  OChan;
	class  BusMessage;
	class  BusMessageRef;
	class  OutCommMapping;
	class  Task;
	class  ComponentRef;
	class  Order;
	class  CommDst;
	class  CommMapping;
	class  InCommMapping;
	class  RootFolder;
	class  MgaObject;

	  void Initialize();

	  void Initialize(const Uml::Diagram &dgr);
	extern  Udm::UdmDiagram diagram;

	class  Dataflow :  public Udm::Object {
	 public:
		static Uml::Class meta;

		Dataflow() { }
		Dataflow(Udm::ObjectImpl *impl) : Object(impl) { }
		Dataflow(const Dataflow &master) : Object(master) { }
		static Dataflow Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Dataflow Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Dataflow CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Dataflow CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Dataflow> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Dataflow>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Dataflow, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Dataflow, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Dataflow> Derived() { return Udm::DerivedAttr<ECSL_DP::Dataflow>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Dataflow, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Dataflow, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Dataflow> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Dataflow>(impl);}

		static Uml::Attribute meta_name;
		Udm::StringAttr name() const { return Udm::StringAttr(impl, meta_name); }

		static Uml::CompositionChildRole meta_System_child;
		Udm::ChildAttr<ECSL_DP::System> System_child() const { return Udm::ChildAttr<ECSL_DP::System>(impl, meta_System_child); }

		Udm::ChildrenAttr<ECSL_DP::Block> Block_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Block>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Block, Pred> Block_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Block, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::System> System_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::System>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::System, Pred> System_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::System, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_RootFolder_parent;
		Udm::ParentAttr<ECSL_DP::RootFolder> RootFolder_parent() const { return Udm::ParentAttr<ECSL_DP::RootFolder>(impl, meta_RootFolder_parent); }

		Udm::ParentAttr<ECSL_DP::RootFolder> parent() const { return Udm::ParentAttr<ECSL_DP::RootFolder>(impl, Udm::NULLPARENTROLE); }
	};

	class  Stateflow :  public Udm::Object {
	 public:
		static Uml::Class meta;

		Stateflow() { }
		Stateflow(Udm::ObjectImpl *impl) : Object(impl) { }
		Stateflow(const Stateflow &master) : Object(master) { }
		static Stateflow Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Stateflow Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Stateflow CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Stateflow CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Stateflow> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Stateflow>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Stateflow, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Stateflow, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Stateflow> Derived() { return Udm::DerivedAttr<ECSL_DP::Stateflow>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Stateflow, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Stateflow, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Stateflow> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Stateflow>(impl);}

		static Uml::Attribute meta_name;
		Udm::StringAttr name() const { return Udm::StringAttr(impl, meta_name); }

		static Uml::CompositionChildRole meta_State_children;
		Udm::ChildrenAttr<ECSL_DP::State> State_children() const { return Udm::ChildrenAttr<ECSL_DP::State>(impl, meta_State_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::State, Pred> State_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::State, Pred>(impl, meta_State_children); }

		Udm::ChildrenAttr<ECSL_DP::State> State_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::State>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::State, Pred> State_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::State, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::TransConnector> TransConnector_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::TransConnector>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::TransConnector, Pred> TransConnector_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::TransConnector, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_RootFolder_parent;
		Udm::ParentAttr<ECSL_DP::RootFolder> RootFolder_parent() const { return Udm::ParentAttr<ECSL_DP::RootFolder>(impl, meta_RootFolder_parent); }

		Udm::ParentAttr<ECSL_DP::RootFolder> parent() const { return Udm::ParentAttr<ECSL_DP::RootFolder>(impl, Udm::NULLPARENTROLE); }
	};

	class  ComponentModels :  public Udm::Object {
	 public:
		static Uml::Class meta;

		ComponentModels() { }
		ComponentModels(Udm::ObjectImpl *impl) : Object(impl) { }
		ComponentModels(const ComponentModels &master) : Object(master) { }
		static ComponentModels Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static ComponentModels Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		ComponentModels CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		ComponentModels CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<ComponentModels> Instances() { return Udm::InstantiatedAttr<ECSL_DP::ComponentModels>(impl);}
		template <class Pred> Udm::InstantiatedAttr<ComponentModels, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::ComponentModels, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::ComponentModels> Derived() { return Udm::DerivedAttr<ECSL_DP::ComponentModels>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::ComponentModels, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::ComponentModels, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::ComponentModels> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::ComponentModels>(impl);}

		static Uml::Attribute meta_name;
		Udm::StringAttr name() const { return Udm::StringAttr(impl, meta_name); }

		static Uml::CompositionChildRole meta_ComponentSheet_children;
		Udm::ChildrenAttr<ECSL_DP::ComponentSheet> ComponentSheet_children() const { return Udm::ChildrenAttr<ECSL_DP::ComponentSheet>(impl, meta_ComponentSheet_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::ComponentSheet, Pred> ComponentSheet_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ComponentSheet, Pred>(impl, meta_ComponentSheet_children); }

		Udm::ChildrenAttr<ECSL_DP::ComponentSheet> ComponentSheet_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::ComponentSheet>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::ComponentSheet, Pred> ComponentSheet_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ComponentSheet, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_RootFolder_parent;
		Udm::ParentAttr<ECSL_DP::RootFolder> RootFolder_parent() const { return Udm::ParentAttr<ECSL_DP::RootFolder>(impl, meta_RootFolder_parent); }

		Udm::ParentAttr<ECSL_DP::RootFolder> parent() const { return Udm::ParentAttr<ECSL_DP::RootFolder>(impl, Udm::NULLPARENTROLE); }
	};

	class  HardwareModels :  public Udm::Object {
	 public:
		static Uml::Class meta;

		HardwareModels() { }
		HardwareModels(Udm::ObjectImpl *impl) : Object(impl) { }
		HardwareModels(const HardwareModels &master) : Object(master) { }
		static HardwareModels Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static HardwareModels Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		HardwareModels CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		HardwareModels CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<HardwareModels> Instances() { return Udm::InstantiatedAttr<ECSL_DP::HardwareModels>(impl);}
		template <class Pred> Udm::InstantiatedAttr<HardwareModels, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::HardwareModels, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::HardwareModels> Derived() { return Udm::DerivedAttr<ECSL_DP::HardwareModels>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::HardwareModels, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::HardwareModels, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::HardwareModels> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::HardwareModels>(impl);}

		static Uml::Attribute meta_name;
		Udm::StringAttr name() const { return Udm::StringAttr(impl, meta_name); }

		static Uml::CompositionChildRole meta_HardwareSheet_children;
		Udm::ChildrenAttr<ECSL_DP::HardwareSheet> HardwareSheet_children() const { return Udm::ChildrenAttr<ECSL_DP::HardwareSheet>(impl, meta_HardwareSheet_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::HardwareSheet, Pred> HardwareSheet_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::HardwareSheet, Pred>(impl, meta_HardwareSheet_children); }

		Udm::ChildrenAttr<ECSL_DP::HardwareSheet> HardwareSheet_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::HardwareSheet>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::HardwareSheet, Pred> HardwareSheet_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::HardwareSheet, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_RootFolder_parent;
		Udm::ParentAttr<ECSL_DP::RootFolder> RootFolder_parent() const { return Udm::ParentAttr<ECSL_DP::RootFolder>(impl, meta_RootFolder_parent); }

		Udm::ParentAttr<ECSL_DP::RootFolder> parent() const { return Udm::ParentAttr<ECSL_DP::RootFolder>(impl, Udm::NULLPARENTROLE); }
	};

	class  RootFolder :  public Udm::Object {
	 public:
		static Uml::Class meta;

		RootFolder() { }
		RootFolder(Udm::ObjectImpl *impl) : Object(impl) { }
		RootFolder(const RootFolder &master) : Object(master) { }
		static RootFolder Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static RootFolder Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		RootFolder CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		RootFolder CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<RootFolder> Instances() { return Udm::InstantiatedAttr<ECSL_DP::RootFolder>(impl);}
		template <class Pred> Udm::InstantiatedAttr<RootFolder, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::RootFolder, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::RootFolder> Derived() { return Udm::DerivedAttr<ECSL_DP::RootFolder>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::RootFolder, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::RootFolder, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::RootFolder> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::RootFolder>(impl);}

		static Uml::Attribute meta_name;
		Udm::StringAttr name() const { return Udm::StringAttr(impl, meta_name); }

		static Uml::CompositionChildRole meta_Stateflow_children;
		Udm::ChildrenAttr<ECSL_DP::Stateflow> Stateflow_children() const { return Udm::ChildrenAttr<ECSL_DP::Stateflow>(impl, meta_Stateflow_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Stateflow, Pred> Stateflow_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Stateflow, Pred>(impl, meta_Stateflow_children); }

		static Uml::CompositionChildRole meta_Dataflow_children;
		Udm::ChildrenAttr<ECSL_DP::Dataflow> Dataflow_children() const { return Udm::ChildrenAttr<ECSL_DP::Dataflow>(impl, meta_Dataflow_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Dataflow, Pred> Dataflow_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Dataflow, Pred>(impl, meta_Dataflow_children); }

		static Uml::CompositionChildRole meta_ComponentModels_children;
		Udm::ChildrenAttr<ECSL_DP::ComponentModels> ComponentModels_children() const { return Udm::ChildrenAttr<ECSL_DP::ComponentModels>(impl, meta_ComponentModels_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::ComponentModels, Pred> ComponentModels_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ComponentModels, Pred>(impl, meta_ComponentModels_children); }

		static Uml::CompositionChildRole meta_HardwareModels_children;
		Udm::ChildrenAttr<ECSL_DP::HardwareModels> HardwareModels_children() const { return Udm::ChildrenAttr<ECSL_DP::HardwareModels>(impl, meta_HardwareModels_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::HardwareModels, Pred> HardwareModels_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::HardwareModels, Pred>(impl, meta_HardwareModels_children); }

		static Uml::CompositionChildRole meta_RootFolder_children;
		Udm::ChildrenAttr<ECSL_DP::RootFolder> RootFolder_children() const { return Udm::ChildrenAttr<ECSL_DP::RootFolder>(impl, meta_RootFolder_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::RootFolder, Pred> RootFolder_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::RootFolder, Pred>(impl, meta_RootFolder_children); }

		Udm::ChildrenAttr<ECSL_DP::Dataflow> Dataflow_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Dataflow>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Dataflow, Pred> Dataflow_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Dataflow, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Stateflow> Stateflow_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Stateflow>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Stateflow, Pred> Stateflow_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Stateflow, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::ComponentModels> ComponentModels_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::ComponentModels>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::ComponentModels, Pred> ComponentModels_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ComponentModels, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::HardwareModels> HardwareModels_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::HardwareModels>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::HardwareModels, Pred> HardwareModels_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::HardwareModels, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::RootFolder> RootFolder_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::RootFolder>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::RootFolder, Pred> RootFolder_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::RootFolder, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_RootFolder_parent;
		Udm::ParentAttr<ECSL_DP::RootFolder> RootFolder_parent() const { return Udm::ParentAttr<ECSL_DP::RootFolder>(impl, meta_RootFolder_parent); }

		Udm::ParentAttr<ECSL_DP::RootFolder> parent() const { return Udm::ParentAttr<ECSL_DP::RootFolder>(impl, Udm::NULLPARENTROLE); }
	};

	class  MgaObject :  public Udm::Object {
	 public:
		static Uml::Class meta;

		MgaObject() { }
		MgaObject(Udm::ObjectImpl *impl) : Object(impl) { }
		MgaObject(const MgaObject &master) : Object(master) { }
		static MgaObject Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static MgaObject Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		MgaObject CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		MgaObject CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<MgaObject> Instances() { return Udm::InstantiatedAttr<ECSL_DP::MgaObject>(impl);}
		template <class Pred> Udm::InstantiatedAttr<MgaObject, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::MgaObject, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::MgaObject> Derived() { return Udm::DerivedAttr<ECSL_DP::MgaObject>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::MgaObject, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::MgaObject, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::MgaObject> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::MgaObject>(impl);}

		static Uml::Attribute meta_position;
		Udm::StringAttr position() const { return Udm::StringAttr(impl, meta_position); }

		static Uml::Attribute meta_name;
		Udm::StringAttr name() const { return Udm::StringAttr(impl, meta_name); }

		Udm::ParentAttr<Udm::Object> parent() const { return Udm::ParentAttr<Udm::Object>(impl, Udm::NULLPARENTROLE); }
	};

	class  Block :  public MgaObject {
	 public:
		static Uml::Class meta;

		Block() { }
		Block(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Block(const Block &master) : MgaObject(master) { }
		static Block Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Block Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Block CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Block CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Block> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Block>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Block, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Block, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Block> Derived() { return Udm::DerivedAttr<ECSL_DP::Block>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Block, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Block, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Block> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Block>(impl);}

		static Uml::Attribute meta_Tag;
		Udm::StringAttr Tag() const { return Udm::StringAttr(impl, meta_Tag); }

		static Uml::Attribute meta_BlockType;
		Udm::StringAttr BlockType() const { return Udm::StringAttr(impl, meta_BlockType); }

		static Uml::Attribute meta_Name;
		Udm::StringAttr Name() const { return Udm::StringAttr(impl, meta_Name); }

		static Uml::Attribute meta_Description;
		Udm::StringAttr Description() const { return Udm::StringAttr(impl, meta_Description); }

		static Uml::Attribute meta_Priority;
		Udm::IntegerAttr Priority() const { return Udm::IntegerAttr(impl, meta_Priority); }

		static Uml::Attribute meta_SampleTime;
		Udm::RealAttr SampleTime() const { return Udm::RealAttr(impl, meta_SampleTime); }

		static Uml::CompositionChildRole meta_Annotation_children;
		Udm::ChildrenAttr<ECSL_DP::Annotation> Annotation_children() const { return Udm::ChildrenAttr<ECSL_DP::Annotation>(impl, meta_Annotation_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Annotation, Pred> Annotation_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Annotation, Pred>(impl, meta_Annotation_children); }

		static Uml::CompositionChildRole meta_ConnectorRef_children;
		Udm::ChildrenAttr<ECSL_DP::ConnectorRef> ConnectorRef_children() const { return Udm::ChildrenAttr<ECSL_DP::ConnectorRef>(impl, meta_ConnectorRef_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::ConnectorRef, Pred> ConnectorRef_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ConnectorRef, Pred>(impl, meta_ConnectorRef_children); }

		static Uml::CompositionChildRole meta_Line;
		Udm::ChildrenAttr<ECSL_DP::Line> Line() const { return Udm::ChildrenAttr<ECSL_DP::Line>(impl, meta_Line); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Line, Pred> Line_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Line, Pred>(impl, meta_Line); }

		static Uml::CompositionChildRole meta_Port_children;
		Udm::ChildrenAttr<ECSL_DP::Port> Port_children() const { return Udm::ChildrenAttr<ECSL_DP::Port>(impl, meta_Port_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Port, Pred> Port_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Port, Pred>(impl, meta_Port_children); }

		static Uml::CompositionChildRole meta_Parameter;
		Udm::ChildrenAttr<ECSL_DP::Parameter> Parameter() const { return Udm::ChildrenAttr<ECSL_DP::Parameter>(impl, meta_Parameter); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Parameter, Pred> Parameter_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Parameter, Pred>(impl, meta_Parameter); }

		Udm::ChildrenAttr<ECSL_DP::TriggerPort> TriggerPort_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::TriggerPort>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::TriggerPort, Pred> TriggerPort_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::TriggerPort, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Parameter> Parameter_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Parameter>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Parameter, Pred> Parameter_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Parameter, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Port> Port_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Port>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Port, Pred> Port_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Port, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Annotation> Annotation_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Annotation>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Annotation, Pred> Annotation_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Annotation, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Line> Line_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Line>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Line, Pred> Line_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Line, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::InputPort> InputPort_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::InputPort>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::InputPort, Pred> InputPort_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::InputPort, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::OutputPort> OutputPort_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::OutputPort>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::OutputPort, Pred> OutputPort_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::OutputPort, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::EnablePort> EnablePort_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::EnablePort>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::EnablePort, Pred> EnablePort_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::EnablePort, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::TransConnector> TransConnector_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::TransConnector>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::TransConnector, Pred> TransConnector_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::TransConnector, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::ConnectorRef> ConnectorRef_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::ConnectorRef>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::ConnectorRef, Pred> ConnectorRef_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ConnectorRef, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_System_parent;
		Udm::ParentAttr<ECSL_DP::System> System_parent() const { return Udm::ParentAttr<ECSL_DP::System>(impl, meta_System_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Primitive :  public Block {
	 public:
		static Uml::Class meta;

		Primitive() { }
		Primitive(Udm::ObjectImpl *impl) : Block(impl) { }
		Primitive(const Primitive &master) : Block(master) { }
		static Primitive Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Primitive Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Primitive CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Primitive CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Primitive> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Primitive>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Primitive, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Primitive, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Primitive> Derived() { return Udm::DerivedAttr<ECSL_DP::Primitive>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Primitive, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Primitive, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Primitive> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Primitive>(impl);}

		static Uml::Attribute meta_Deadline;
		Udm::IntegerAttr Deadline() const { return Udm::IntegerAttr(impl, meta_Deadline); }

		static Uml::Attribute meta_ExecutionTime;
		Udm::IntegerAttr ExecutionTime() const { return Udm::IntegerAttr(impl, meta_ExecutionTime); }

		static Uml::Attribute meta_Period;
		Udm::IntegerAttr Period() const { return Udm::IntegerAttr(impl, meta_Period); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Parameter :  public MgaObject {
	 public:
		static Uml::Class meta;

		Parameter() { }
		Parameter(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Parameter(const Parameter &master) : MgaObject(master) { }
		static Parameter Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Parameter Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Parameter CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Parameter CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Parameter> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Parameter>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Parameter, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Parameter, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Parameter> Derived() { return Udm::DerivedAttr<ECSL_DP::Parameter>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Parameter, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Parameter, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Parameter> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Parameter>(impl);}

		static Uml::Attribute meta_Value;
		Udm::StringAttr Value() const { return Udm::StringAttr(impl, meta_Value); }

		static Uml::CompositionParentRole meta_Parameter_Block_parent;
		Udm::ParentAttr<ECSL_DP::Block> Parameter_Block_parent() const { return Udm::ParentAttr<ECSL_DP::Block>(impl, meta_Parameter_Block_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Port :  public MgaObject {
	 public:
		static Uml::Class meta;

		Port() { }
		Port(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Port(const Port &master) : MgaObject(master) { }
		static Port Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Port Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Port CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Port CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Port> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Port>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Port, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Port, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Port> Derived() { return Udm::DerivedAttr<ECSL_DP::Port>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Port, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Port, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Port> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Port>(impl);}

		static Uml::AssociationRole meta_srcLine, meta_srcLine_rev;
		Udm::AClassAssocAttr<ECSL_DP::Line, ECSL_DP::Port> srcLine() const { return Udm::AClassAssocAttr<ECSL_DP::Line, ECSL_DP::Port>(impl, meta_srcLine, meta_srcLine_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::Line, ECSL_DP::Port, Pred> srcLine_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::Line, ECSL_DP::Port, Pred>(impl, meta_srcLine, meta_srcLine_rev); }

		static Uml::AssociationRole meta_dstLine, meta_dstLine_rev;
		Udm::AClassAssocAttr<ECSL_DP::Line, ECSL_DP::Port> dstLine() const { return Udm::AClassAssocAttr<ECSL_DP::Line, ECSL_DP::Port>(impl, meta_dstLine, meta_dstLine_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::Line, ECSL_DP::Port, Pred> dstLine_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::Line, ECSL_DP::Port, Pred>(impl, meta_dstLine, meta_dstLine_rev); }

		static Uml::CompositionParentRole meta_Block_parent;
		Udm::ParentAttr<ECSL_DP::Block> Block_parent() const { return Udm::ParentAttr<ECSL_DP::Block>(impl, meta_Block_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  TriggerPort :  public Port {
	 public:
		static Uml::Class meta;

		TriggerPort() { }
		TriggerPort(Udm::ObjectImpl *impl) : Port(impl) { }
		TriggerPort(const TriggerPort &master) : Port(master) { }
		static TriggerPort Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static TriggerPort Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		TriggerPort CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		TriggerPort CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<TriggerPort> Instances() { return Udm::InstantiatedAttr<ECSL_DP::TriggerPort>(impl);}
		template <class Pred> Udm::InstantiatedAttr<TriggerPort, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::TriggerPort, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::TriggerPort> Derived() { return Udm::DerivedAttr<ECSL_DP::TriggerPort>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::TriggerPort, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::TriggerPort, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::TriggerPort> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::TriggerPort>(impl);}

		static Uml::Attribute meta_TriggerType;
		Udm::StringAttr TriggerType() const { return Udm::StringAttr(impl, meta_TriggerType); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Reference :  public Block {
	 public:
		static Uml::Class meta;

		Reference() { }
		Reference(Udm::ObjectImpl *impl) : Block(impl) { }
		Reference(const Reference &master) : Block(master) { }
		static Reference Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Reference Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Reference CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Reference CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Reference> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Reference>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Reference, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Reference, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Reference> Derived() { return Udm::DerivedAttr<ECSL_DP::Reference>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Reference, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Reference, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Reference> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Reference>(impl);}

		static Uml::Attribute meta_SourceBlock;
		Udm::StringAttr SourceBlock() const { return Udm::StringAttr(impl, meta_SourceBlock); }

		static Uml::Attribute meta_SourceType;
		Udm::StringAttr SourceType() const { return Udm::StringAttr(impl, meta_SourceType); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Annotation :  public MgaObject {
	 public:
		static Uml::Class meta;

		Annotation() { }
		Annotation(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Annotation(const Annotation &master) : MgaObject(master) { }
		static Annotation Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Annotation Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Annotation CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Annotation CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Annotation> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Annotation>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Annotation, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Annotation, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Annotation> Derived() { return Udm::DerivedAttr<ECSL_DP::Annotation>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Annotation, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Annotation, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Annotation> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Annotation>(impl);}

		static Uml::Attribute meta_Text;
		Udm::StringAttr Text() const { return Udm::StringAttr(impl, meta_Text); }

		static Uml::CompositionParentRole meta_Block_parent;
		Udm::ParentAttr<ECSL_DP::Block> Block_parent() const { return Udm::ParentAttr<ECSL_DP::Block>(impl, meta_Block_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Line :  public MgaObject {
	 public:
		static Uml::Class meta;

		Line() { }
		Line(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Line(const Line &master) : MgaObject(master) { }
		static Line Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Line Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Line CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Line CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Line> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Line>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Line, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Line, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Line> Derived() { return Udm::DerivedAttr<ECSL_DP::Line>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Line, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Line, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Line> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Line>(impl);}

		static Uml::Attribute meta_Name;
		Udm::StringAttr Name() const { return Udm::StringAttr(impl, meta_Name); }

		static Uml::CompositionParentRole meta_Line_Block_parent;
		Udm::ParentAttr<ECSL_DP::Block> Line_Block_parent() const { return Udm::ParentAttr<ECSL_DP::Block>(impl, meta_Line_Block_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_dstLine_end_;
		Udm::AssocEndAttr<ECSL_DP::Port> dstLine_end() const { return Udm::AssocEndAttr<ECSL_DP::Port>(impl, meta_dstLine_end_); }

		static Uml::AssociationRole meta_srcLine_end_;
		Udm::AssocEndAttr<ECSL_DP::Port> srcLine_end() const { return Udm::AssocEndAttr<ECSL_DP::Port>(impl, meta_srcLine_end_); }

	};

	class  InputPort :  public Port {
	 public:
		static Uml::Class meta;

		InputPort() { }
		InputPort(Udm::ObjectImpl *impl) : Port(impl) { }
		InputPort(const InputPort &master) : Port(master) { }
		static InputPort Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static InputPort Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		InputPort CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		InputPort CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<InputPort> Instances() { return Udm::InstantiatedAttr<ECSL_DP::InputPort>(impl);}
		template <class Pred> Udm::InstantiatedAttr<InputPort, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::InputPort, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::InputPort> Derived() { return Udm::DerivedAttr<ECSL_DP::InputPort>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::InputPort, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::InputPort, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::InputPort> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::InputPort>(impl);}

		static Uml::Attribute meta_Number;
		Udm::IntegerAttr Number() const { return Udm::IntegerAttr(impl, meta_Number); }

		static Uml::AssociationRole meta_srcInPortMapping, meta_srcInPortMapping_rev;
		Udm::AClassAssocAttr<ECSL_DP::InPortMapping, ECSL_DP::CInPort> srcInPortMapping() const { return Udm::AClassAssocAttr<ECSL_DP::InPortMapping, ECSL_DP::CInPort>(impl, meta_srcInPortMapping, meta_srcInPortMapping_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::InPortMapping, ECSL_DP::CInPort, Pred> srcInPortMapping_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::InPortMapping, ECSL_DP::CInPort, Pred>(impl, meta_srcInPortMapping, meta_srcInPortMapping_rev); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  OutputPort :  public Port {
	 public:
		static Uml::Class meta;

		OutputPort() { }
		OutputPort(Udm::ObjectImpl *impl) : Port(impl) { }
		OutputPort(const OutputPort &master) : Port(master) { }
		static OutputPort Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static OutputPort Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		OutputPort CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		OutputPort CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<OutputPort> Instances() { return Udm::InstantiatedAttr<ECSL_DP::OutputPort>(impl);}
		template <class Pred> Udm::InstantiatedAttr<OutputPort, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::OutputPort, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::OutputPort> Derived() { return Udm::DerivedAttr<ECSL_DP::OutputPort>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::OutputPort, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::OutputPort, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::OutputPort> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::OutputPort>(impl);}

		static Uml::Attribute meta_Number;
		Udm::IntegerAttr Number() const { return Udm::IntegerAttr(impl, meta_Number); }

		static Uml::AssociationRole meta_dstOutPortMapping, meta_dstOutPortMapping_rev;
		Udm::AClassAssocAttr<ECSL_DP::OutPortMapping, ECSL_DP::COutPort> dstOutPortMapping() const { return Udm::AClassAssocAttr<ECSL_DP::OutPortMapping, ECSL_DP::COutPort>(impl, meta_dstOutPortMapping, meta_dstOutPortMapping_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::OutPortMapping, ECSL_DP::COutPort, Pred> dstOutPortMapping_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::OutPortMapping, ECSL_DP::COutPort, Pred>(impl, meta_dstOutPortMapping, meta_dstOutPortMapping_rev); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  EnablePort :  public Port {
	 public:
		static Uml::Class meta;

		EnablePort() { }
		EnablePort(Udm::ObjectImpl *impl) : Port(impl) { }
		EnablePort(const EnablePort &master) : Port(master) { }
		static EnablePort Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static EnablePort Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		EnablePort CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		EnablePort CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<EnablePort> Instances() { return Udm::InstantiatedAttr<ECSL_DP::EnablePort>(impl);}
		template <class Pred> Udm::InstantiatedAttr<EnablePort, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::EnablePort, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::EnablePort> Derived() { return Udm::DerivedAttr<ECSL_DP::EnablePort>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::EnablePort, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::EnablePort, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::EnablePort> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::EnablePort>(impl);}

		static Uml::Attribute meta_StatesWhenEnabling;
		Udm::StringAttr StatesWhenEnabling() const { return Udm::StringAttr(impl, meta_StatesWhenEnabling); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  System :  public Block {
	 public:
		static Uml::Class meta;

		System() { }
		System(Udm::ObjectImpl *impl) : Block(impl) { }
		System(const System &master) : Block(master) { }
		static System Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static System Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		System CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		System CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<System> Instances() { return Udm::InstantiatedAttr<ECSL_DP::System>(impl);}
		template <class Pred> Udm::InstantiatedAttr<System, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::System, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::System> Derived() { return Udm::DerivedAttr<ECSL_DP::System>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::System, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::System, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::System> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::System>(impl);}

		static Uml::AssociationRole meta_referedbySystemRef;
		Udm::AssocAttr<ECSL_DP::SystemRef> referedbySystemRef() const { return Udm::AssocAttr<ECSL_DP::SystemRef>(impl, meta_referedbySystemRef); }
		template <class Pred> Udm::AssocAttr<ECSL_DP::SystemRef, Pred > referedbySystemRef_sorted(const Pred &) const { return Udm::AssocAttr<ECSL_DP::SystemRef, Pred>(impl, meta_referedbySystemRef); }

		static Uml::CompositionChildRole meta_Block_children;
		Udm::ChildrenAttr<ECSL_DP::Block> Block_children() const { return Udm::ChildrenAttr<ECSL_DP::Block>(impl, meta_Block_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Block, Pred> Block_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Block, Pred>(impl, meta_Block_children); }

		Udm::ChildrenAttr<ECSL_DP::Block> Block_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Block>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Block, Pred> Block_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Block, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Primitive> Primitive_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Primitive>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Primitive, Pred> Primitive_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Primitive, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Reference> Reference_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Reference>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Reference, Pred> Reference_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Reference, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::System> System_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::System>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::System, Pred> System_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::System, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_Dataflow_parent;
		Udm::ParentAttr<ECSL_DP::Dataflow> Dataflow_parent() const { return Udm::ParentAttr<ECSL_DP::Dataflow>(impl, meta_Dataflow_parent); }

		Udm::ParentAttr<Udm::Object> parent() const { return Udm::ParentAttr<Udm::Object>(impl, Udm::NULLPARENTROLE); }
	};

	class  Transition :  public MgaObject {
	 public:
		static Uml::Class meta;

		Transition() { }
		Transition(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Transition(const Transition &master) : MgaObject(master) { }
		static Transition Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Transition Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Transition CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Transition CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Transition> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Transition>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Transition, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Transition, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Transition> Derived() { return Udm::DerivedAttr<ECSL_DP::Transition>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Transition, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Transition, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Transition> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Transition>(impl);}

		static Uml::Attribute meta_Guard;
		Udm::StringAttr Guard() const { return Udm::StringAttr(impl, meta_Guard); }

		static Uml::Attribute meta_Trigger;
		Udm::StringAttr Trigger() const { return Udm::StringAttr(impl, meta_Trigger); }

		static Uml::Attribute meta_Action;
		Udm::StringAttr Action() const { return Udm::StringAttr(impl, meta_Action); }

		static Uml::Attribute meta_ConditionAction;
		Udm::StringAttr ConditionAction() const { return Udm::StringAttr(impl, meta_ConditionAction); }

		static Uml::Attribute meta_Order;
		Udm::StringAttr Order() const { return Udm::StringAttr(impl, meta_Order); }

		static Uml::CompositionParentRole meta_Transition_State_parent;
		Udm::ParentAttr<ECSL_DP::State> Transition_State_parent() const { return Udm::ParentAttr<ECSL_DP::State>(impl, meta_Transition_State_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_dstTransition_end_;
		Udm::AssocEndAttr<ECSL_DP::TransConnector> dstTransition_end() const { return Udm::AssocEndAttr<ECSL_DP::TransConnector>(impl, meta_dstTransition_end_); }

		static Uml::AssociationRole meta_srcTransition_end_;
		Udm::AssocEndAttr<ECSL_DP::TransConnector> srcTransition_end() const { return Udm::AssocEndAttr<ECSL_DP::TransConnector>(impl, meta_srcTransition_end_); }

	};

	class  Event :  public MgaObject {
	 public:
		static Uml::Class meta;

		Event() { }
		Event(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Event(const Event &master) : MgaObject(master) { }
		static Event Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Event Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Event CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Event CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Event> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Event>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Event, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Event, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Event> Derived() { return Udm::DerivedAttr<ECSL_DP::Event>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Event, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Event, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Event> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Event>(impl);}

		static Uml::Attribute meta_Name;
		Udm::StringAttr Name() const { return Udm::StringAttr(impl, meta_Name); }

		static Uml::Attribute meta_Trigger;
		Udm::StringAttr Trigger() const { return Udm::StringAttr(impl, meta_Trigger); }

		static Uml::Attribute meta_Scope;
		Udm::StringAttr Scope() const { return Udm::StringAttr(impl, meta_Scope); }

		static Uml::Attribute meta_Description;
		Udm::StringAttr Description() const { return Udm::StringAttr(impl, meta_Description); }

		static Uml::CompositionParentRole meta_State_parent;
		Udm::ParentAttr<ECSL_DP::State> State_parent() const { return Udm::ParentAttr<ECSL_DP::State>(impl, meta_State_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Data :  public MgaObject {
	 public:
		static Uml::Class meta;

		Data() { }
		Data(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Data(const Data &master) : MgaObject(master) { }
		static Data Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Data Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Data CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Data CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Data> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Data>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Data, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Data, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Data> Derived() { return Udm::DerivedAttr<ECSL_DP::Data>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Data, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Data, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Data> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Data>(impl);}

		static Uml::Attribute meta_ArrayFirstIndex;
		Udm::IntegerAttr ArrayFirstIndex() const { return Udm::IntegerAttr(impl, meta_ArrayFirstIndex); }

		static Uml::Attribute meta_ArraySize;
		Udm::IntegerAttr ArraySize() const { return Udm::IntegerAttr(impl, meta_ArraySize); }

		static Uml::Attribute meta_InitialValue;
		Udm::StringAttr InitialValue() const { return Udm::StringAttr(impl, meta_InitialValue); }

		static Uml::Attribute meta_Min;
		Udm::StringAttr Min() const { return Udm::StringAttr(impl, meta_Min); }

		static Uml::Attribute meta_Max;
		Udm::StringAttr Max() const { return Udm::StringAttr(impl, meta_Max); }

		static Uml::Attribute meta_Units;
		Udm::StringAttr Units() const { return Udm::StringAttr(impl, meta_Units); }

		static Uml::Attribute meta_DataType;
		Udm::StringAttr DataType() const { return Udm::StringAttr(impl, meta_DataType); }

		static Uml::Attribute meta_Name;
		Udm::StringAttr Name() const { return Udm::StringAttr(impl, meta_Name); }

		static Uml::Attribute meta_Scope;
		Udm::StringAttr Scope() const { return Udm::StringAttr(impl, meta_Scope); }

		static Uml::Attribute meta_Description;
		Udm::StringAttr Description() const { return Udm::StringAttr(impl, meta_Description); }

		static Uml::CompositionParentRole meta_State_parent;
		Udm::ParentAttr<ECSL_DP::State> State_parent() const { return Udm::ParentAttr<ECSL_DP::State>(impl, meta_State_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  TransConnector :  public MgaObject {
	 public:
		static Uml::Class meta;

		TransConnector() { }
		TransConnector(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		TransConnector(const TransConnector &master) : MgaObject(master) { }
		static TransConnector Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static TransConnector Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		TransConnector CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		TransConnector CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<TransConnector> Instances() { return Udm::InstantiatedAttr<ECSL_DP::TransConnector>(impl);}
		template <class Pred> Udm::InstantiatedAttr<TransConnector, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::TransConnector, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::TransConnector> Derived() { return Udm::DerivedAttr<ECSL_DP::TransConnector>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::TransConnector, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::TransConnector, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::TransConnector> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::TransConnector>(impl);}

		static Uml::AssociationRole meta_srcTransition, meta_srcTransition_rev;
		Udm::AClassAssocAttr<ECSL_DP::Transition, ECSL_DP::TransConnector> srcTransition() const { return Udm::AClassAssocAttr<ECSL_DP::Transition, ECSL_DP::TransConnector>(impl, meta_srcTransition, meta_srcTransition_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::Transition, ECSL_DP::TransConnector, Pred> srcTransition_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::Transition, ECSL_DP::TransConnector, Pred>(impl, meta_srcTransition, meta_srcTransition_rev); }

		static Uml::AssociationRole meta_dstTransition, meta_dstTransition_rev;
		Udm::AClassAssocAttr<ECSL_DP::Transition, ECSL_DP::TransConnector> dstTransition() const { return Udm::AClassAssocAttr<ECSL_DP::Transition, ECSL_DP::TransConnector>(impl, meta_dstTransition, meta_dstTransition_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::Transition, ECSL_DP::TransConnector, Pred> dstTransition_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::Transition, ECSL_DP::TransConnector, Pred>(impl, meta_dstTransition, meta_dstTransition_rev); }

		static Uml::AssociationRole meta_referedbyConnectorRef;
		Udm::AssocAttr<ECSL_DP::ConnectorRef> referedbyConnectorRef() const { return Udm::AssocAttr<ECSL_DP::ConnectorRef>(impl, meta_referedbyConnectorRef); }
		template <class Pred> Udm::AssocAttr<ECSL_DP::ConnectorRef, Pred > referedbyConnectorRef_sorted(const Pred &) const { return Udm::AssocAttr<ECSL_DP::ConnectorRef, Pred>(impl, meta_referedbyConnectorRef); }

		static Uml::CompositionParentRole meta_State_parent;
		Udm::ParentAttr<ECSL_DP::State> State_parent() const { return Udm::ParentAttr<ECSL_DP::State>(impl, meta_State_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  State :  public TransConnector {
	 public:
		static Uml::Class meta;

		State() { }
		State(Udm::ObjectImpl *impl) : TransConnector(impl) { }
		State(const State &master) : TransConnector(master) { }
		static State Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static State Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		State CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		State CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<State> Instances() { return Udm::InstantiatedAttr<ECSL_DP::State>(impl);}
		template <class Pred> Udm::InstantiatedAttr<State, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::State, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::State> Derived() { return Udm::DerivedAttr<ECSL_DP::State>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::State, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::State, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::State> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::State>(impl);}

		static Uml::Attribute meta_Name;
		Udm::StringAttr Name() const { return Udm::StringAttr(impl, meta_Name); }

		static Uml::Attribute meta_Decomposition;
		Udm::StringAttr Decomposition() const { return Udm::StringAttr(impl, meta_Decomposition); }

		static Uml::Attribute meta_EnterAction;
		Udm::StringAttr EnterAction() const { return Udm::StringAttr(impl, meta_EnterAction); }

		static Uml::Attribute meta_DuringAction;
		Udm::StringAttr DuringAction() const { return Udm::StringAttr(impl, meta_DuringAction); }

		static Uml::Attribute meta_ExitAction;
		Udm::StringAttr ExitAction() const { return Udm::StringAttr(impl, meta_ExitAction); }

		static Uml::Attribute meta_Order;
		Udm::StringAttr Order() const { return Udm::StringAttr(impl, meta_Order); }

		static Uml::CompositionChildRole meta_Event_children;
		Udm::ChildrenAttr<ECSL_DP::Event> Event_children() const { return Udm::ChildrenAttr<ECSL_DP::Event>(impl, meta_Event_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Event, Pred> Event_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Event, Pred>(impl, meta_Event_children); }

		static Uml::CompositionChildRole meta_Transition;
		Udm::ChildrenAttr<ECSL_DP::Transition> Transition() const { return Udm::ChildrenAttr<ECSL_DP::Transition>(impl, meta_Transition); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Transition, Pred> Transition_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Transition, Pred>(impl, meta_Transition); }

		static Uml::CompositionChildRole meta_Data_children;
		Udm::ChildrenAttr<ECSL_DP::Data> Data_children() const { return Udm::ChildrenAttr<ECSL_DP::Data>(impl, meta_Data_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Data, Pred> Data_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Data, Pred>(impl, meta_Data_children); }

		static Uml::CompositionChildRole meta_TransConnector_children;
		Udm::ChildrenAttr<ECSL_DP::TransConnector> TransConnector_children() const { return Udm::ChildrenAttr<ECSL_DP::TransConnector>(impl, meta_TransConnector_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::TransConnector, Pred> TransConnector_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::TransConnector, Pred>(impl, meta_TransConnector_children); }

		Udm::ChildrenAttr<ECSL_DP::State> State_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::State>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::State, Pred> State_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::State, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Transition> Transition_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Transition>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Transition, Pred> Transition_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Transition, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Event> Event_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Event>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Event, Pred> Event_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Event, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::History> History_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::History>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::History, Pred> History_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::History, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::TransStart> TransStart_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::TransStart>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::TransStart, Pred> TransStart_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::TransStart, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Junction> Junction_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Junction>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Junction, Pred> Junction_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Junction, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Data> Data_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Data>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Data, Pred> Data_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Data, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::TransConnector> TransConnector_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::TransConnector>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::TransConnector, Pred> TransConnector_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::TransConnector, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::ConnectorRef> ConnectorRef_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::ConnectorRef>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::ConnectorRef, Pred> ConnectorRef_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ConnectorRef, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_Stateflow_parent;
		Udm::ParentAttr<ECSL_DP::Stateflow> Stateflow_parent() const { return Udm::ParentAttr<ECSL_DP::Stateflow>(impl, meta_Stateflow_parent); }

		Udm::ParentAttr<Udm::Object> parent() const { return Udm::ParentAttr<Udm::Object>(impl, Udm::NULLPARENTROLE); }
	};

	class  History :  public TransConnector {
	 public:
		static Uml::Class meta;

		History() { }
		History(Udm::ObjectImpl *impl) : TransConnector(impl) { }
		History(const History &master) : TransConnector(master) { }
		static History Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static History Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		History CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		History CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<History> Instances() { return Udm::InstantiatedAttr<ECSL_DP::History>(impl);}
		template <class Pred> Udm::InstantiatedAttr<History, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::History, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::History> Derived() { return Udm::DerivedAttr<ECSL_DP::History>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::History, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::History, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::History> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::History>(impl);}

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  TransStart :  public TransConnector {
	 public:
		static Uml::Class meta;

		TransStart() { }
		TransStart(Udm::ObjectImpl *impl) : TransConnector(impl) { }
		TransStart(const TransStart &master) : TransConnector(master) { }
		static TransStart Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static TransStart Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		TransStart CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		TransStart CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<TransStart> Instances() { return Udm::InstantiatedAttr<ECSL_DP::TransStart>(impl);}
		template <class Pred> Udm::InstantiatedAttr<TransStart, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::TransStart, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::TransStart> Derived() { return Udm::DerivedAttr<ECSL_DP::TransStart>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::TransStart, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::TransStart, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::TransStart> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::TransStart>(impl);}

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Junction :  public TransConnector {
	 public:
		static Uml::Class meta;

		Junction() { }
		Junction(Udm::ObjectImpl *impl) : TransConnector(impl) { }
		Junction(const Junction &master) : TransConnector(master) { }
		static Junction Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Junction Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Junction CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Junction CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Junction> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Junction>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Junction, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Junction, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Junction> Derived() { return Udm::DerivedAttr<ECSL_DP::Junction>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Junction, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Junction, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Junction> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Junction>(impl);}

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  ConnectorRef :  public TransConnector {
	 public:
		static Uml::Class meta;

		ConnectorRef() { }
		ConnectorRef(Udm::ObjectImpl *impl) : TransConnector(impl) { }
		ConnectorRef(const ConnectorRef &master) : TransConnector(master) { }
		static ConnectorRef Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static ConnectorRef Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		ConnectorRef CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		ConnectorRef CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<ConnectorRef> Instances() { return Udm::InstantiatedAttr<ECSL_DP::ConnectorRef>(impl);}
		template <class Pred> Udm::InstantiatedAttr<ConnectorRef, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::ConnectorRef, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::ConnectorRef> Derived() { return Udm::DerivedAttr<ECSL_DP::ConnectorRef>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::ConnectorRef, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::ConnectorRef, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::ConnectorRef> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::ConnectorRef>(impl);}

		static Uml::AssociationRole meta_ref;
		Udm::PointerAttr<ECSL_DP::TransConnector> ref() const { return Udm::PointerAttr<ECSL_DP::TransConnector>(impl, meta_ref); }

		static Uml::CompositionParentRole meta_Block_parent;
		Udm::ParentAttr<ECSL_DP::Block> Block_parent() const { return Udm::ParentAttr<ECSL_DP::Block>(impl, meta_Block_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  RTConstraint :  public MgaObject {
	 public:
		static Uml::Class meta;

		RTConstraint() { }
		RTConstraint(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		RTConstraint(const RTConstraint &master) : MgaObject(master) { }
		static RTConstraint Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static RTConstraint Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		RTConstraint CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		RTConstraint CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<RTConstraint> Instances() { return Udm::InstantiatedAttr<ECSL_DP::RTConstraint>(impl);}
		template <class Pred> Udm::InstantiatedAttr<RTConstraint, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::RTConstraint, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::RTConstraint> Derived() { return Udm::DerivedAttr<ECSL_DP::RTConstraint>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::RTConstraint, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::RTConstraint, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::RTConstraint> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::RTConstraint>(impl);}

		static Uml::Attribute meta_Latency;
		Udm::IntegerAttr Latency() const { return Udm::IntegerAttr(impl, meta_Latency); }

		static Uml::AssociationRole meta_srcRTCOut, meta_srcRTCOut_rev;
		Udm::AClassAssocAttr<ECSL_DP::RTCOut, ECSL_DP::CInPort> srcRTCOut() const { return Udm::AClassAssocAttr<ECSL_DP::RTCOut, ECSL_DP::CInPort>(impl, meta_srcRTCOut, meta_srcRTCOut_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::RTCOut, ECSL_DP::CInPort, Pred> srcRTCOut_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::RTCOut, ECSL_DP::CInPort, Pred>(impl, meta_srcRTCOut, meta_srcRTCOut_rev); }

		static Uml::AssociationRole meta_dstRTCIn, meta_dstRTCIn_rev;
		Udm::AClassAssocAttr<ECSL_DP::RTCIn, ECSL_DP::COutPort> dstRTCIn() const { return Udm::AClassAssocAttr<ECSL_DP::RTCIn, ECSL_DP::COutPort>(impl, meta_dstRTCIn, meta_dstRTCIn_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::RTCIn, ECSL_DP::COutPort, Pred> dstRTCIn_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::RTCIn, ECSL_DP::COutPort, Pred>(impl, meta_dstRTCIn, meta_dstRTCIn_rev); }

		static Uml::CompositionParentRole meta_Component_parent;
		Udm::ParentAttr<ECSL_DP::Component> Component_parent() const { return Udm::ParentAttr<ECSL_DP::Component>(impl, meta_Component_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  RTCOut :  public MgaObject {
	 public:
		static Uml::Class meta;

		RTCOut() { }
		RTCOut(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		RTCOut(const RTCOut &master) : MgaObject(master) { }
		static RTCOut Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static RTCOut Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		RTCOut CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		RTCOut CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<RTCOut> Instances() { return Udm::InstantiatedAttr<ECSL_DP::RTCOut>(impl);}
		template <class Pred> Udm::InstantiatedAttr<RTCOut, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::RTCOut, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::RTCOut> Derived() { return Udm::DerivedAttr<ECSL_DP::RTCOut>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::RTCOut, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::RTCOut, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::RTCOut> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::RTCOut>(impl);}

		static Uml::CompositionParentRole meta_Component_parent;
		Udm::ParentAttr<ECSL_DP::Component> Component_parent() const { return Udm::ParentAttr<ECSL_DP::Component>(impl, meta_Component_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_dstRTCOut_end_;
		Udm::AssocEndAttr<ECSL_DP::RTConstraint> dstRTCOut_end() const { return Udm::AssocEndAttr<ECSL_DP::RTConstraint>(impl, meta_dstRTCOut_end_); }

		static Uml::AssociationRole meta_srcRTCOut_end_;
		Udm::AssocEndAttr<ECSL_DP::CInPort> srcRTCOut_end() const { return Udm::AssocEndAttr<ECSL_DP::CInPort>(impl, meta_srcRTCOut_end_); }

	};

	class  RTCIn :  public MgaObject {
	 public:
		static Uml::Class meta;

		RTCIn() { }
		RTCIn(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		RTCIn(const RTCIn &master) : MgaObject(master) { }
		static RTCIn Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static RTCIn Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		RTCIn CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		RTCIn CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<RTCIn> Instances() { return Udm::InstantiatedAttr<ECSL_DP::RTCIn>(impl);}
		template <class Pred> Udm::InstantiatedAttr<RTCIn, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::RTCIn, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::RTCIn> Derived() { return Udm::DerivedAttr<ECSL_DP::RTCIn>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::RTCIn, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::RTCIn, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::RTCIn> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::RTCIn>(impl);}

		static Uml::CompositionParentRole meta_Component_parent;
		Udm::ParentAttr<ECSL_DP::Component> Component_parent() const { return Udm::ParentAttr<ECSL_DP::Component>(impl, meta_Component_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_srcRTCIn_end_;
		Udm::AssocEndAttr<ECSL_DP::RTConstraint> srcRTCIn_end() const { return Udm::AssocEndAttr<ECSL_DP::RTConstraint>(impl, meta_srcRTCIn_end_); }

		static Uml::AssociationRole meta_dstRTCIn_end_;
		Udm::AssocEndAttr<ECSL_DP::COutPort> dstRTCIn_end() const { return Udm::AssocEndAttr<ECSL_DP::COutPort>(impl, meta_dstRTCIn_end_); }

	};

	class  SystemRef :  public MgaObject {
	 public:
		static Uml::Class meta;

		SystemRef() { }
		SystemRef(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		SystemRef(const SystemRef &master) : MgaObject(master) { }
		static SystemRef Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static SystemRef Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		SystemRef CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		SystemRef CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<SystemRef> Instances() { return Udm::InstantiatedAttr<ECSL_DP::SystemRef>(impl);}
		template <class Pred> Udm::InstantiatedAttr<SystemRef, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::SystemRef, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::SystemRef> Derived() { return Udm::DerivedAttr<ECSL_DP::SystemRef>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::SystemRef, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::SystemRef, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::SystemRef> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::SystemRef>(impl);}

		static Uml::AssociationRole meta_ref;
		Udm::PointerAttr<ECSL_DP::System> ref() const { return Udm::PointerAttr<ECSL_DP::System>(impl, meta_ref); }

		static Uml::CompositionParentRole meta_Component_parent;
		Udm::ParentAttr<ECSL_DP::Component> Component_parent() const { return Udm::ParentAttr<ECSL_DP::Component>(impl, meta_Component_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  InPortMapping :  public MgaObject {
	 public:
		static Uml::Class meta;

		InPortMapping() { }
		InPortMapping(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		InPortMapping(const InPortMapping &master) : MgaObject(master) { }
		static InPortMapping Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static InPortMapping Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		InPortMapping CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		InPortMapping CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<InPortMapping> Instances() { return Udm::InstantiatedAttr<ECSL_DP::InPortMapping>(impl);}
		template <class Pred> Udm::InstantiatedAttr<InPortMapping, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::InPortMapping, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::InPortMapping> Derived() { return Udm::DerivedAttr<ECSL_DP::InPortMapping>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::InPortMapping, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::InPortMapping, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::InPortMapping> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::InPortMapping>(impl);}

		static Uml::CompositionParentRole meta_Component_parent;
		Udm::ParentAttr<ECSL_DP::Component> Component_parent() const { return Udm::ParentAttr<ECSL_DP::Component>(impl, meta_Component_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_dstInPortMapping_end_;
		Udm::AssocEndAttr<ECSL_DP::InputPort> dstInPortMapping_end() const { return Udm::AssocEndAttr<ECSL_DP::InputPort>(impl, meta_dstInPortMapping_end_); }

		static Uml::AssociationRole meta_srcInPortMapping_end_;
		Udm::AssocEndAttr<ECSL_DP::CInPort> srcInPortMapping_end() const { return Udm::AssocEndAttr<ECSL_DP::CInPort>(impl, meta_srcInPortMapping_end_); }

	};

	class  ComponentShortcut :  public MgaObject {
	 public:
		static Uml::Class meta;

		ComponentShortcut() { }
		ComponentShortcut(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		ComponentShortcut(const ComponentShortcut &master) : MgaObject(master) { }
		static ComponentShortcut Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static ComponentShortcut Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		ComponentShortcut CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		ComponentShortcut CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<ComponentShortcut> Instances() { return Udm::InstantiatedAttr<ECSL_DP::ComponentShortcut>(impl);}
		template <class Pred> Udm::InstantiatedAttr<ComponentShortcut, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::ComponentShortcut, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::ComponentShortcut> Derived() { return Udm::DerivedAttr<ECSL_DP::ComponentShortcut>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::ComponentShortcut, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::ComponentShortcut, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::ComponentShortcut> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::ComponentShortcut>(impl);}

		static Uml::AssociationRole meta_ref;
		Udm::PointerAttr<ECSL_DP::Component> ref() const { return Udm::PointerAttr<ECSL_DP::Component>(impl, meta_ref); }

		static Uml::CompositionParentRole meta_ComponentSheet_parent;
		Udm::ParentAttr<ECSL_DP::ComponentSheet> ComponentSheet_parent() const { return Udm::ParentAttr<ECSL_DP::ComponentSheet>(impl, meta_ComponentSheet_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  ComponentSheet :  public MgaObject {
	 public:
		static Uml::Class meta;

		ComponentSheet() { }
		ComponentSheet(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		ComponentSheet(const ComponentSheet &master) : MgaObject(master) { }
		static ComponentSheet Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static ComponentSheet Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		ComponentSheet CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		ComponentSheet CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<ComponentSheet> Instances() { return Udm::InstantiatedAttr<ECSL_DP::ComponentSheet>(impl);}
		template <class Pred> Udm::InstantiatedAttr<ComponentSheet, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::ComponentSheet, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::ComponentSheet> Derived() { return Udm::DerivedAttr<ECSL_DP::ComponentSheet>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::ComponentSheet, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::ComponentSheet, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::ComponentSheet> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::ComponentSheet>(impl);}

		static Uml::CompositionChildRole meta_Component_children;
		Udm::ChildrenAttr<ECSL_DP::Component> Component_children() const { return Udm::ChildrenAttr<ECSL_DP::Component>(impl, meta_Component_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Component, Pred> Component_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Component, Pred>(impl, meta_Component_children); }

		static Uml::CompositionChildRole meta_ComponentShortcut_children;
		Udm::ChildrenAttr<ECSL_DP::ComponentShortcut> ComponentShortcut_children() const { return Udm::ChildrenAttr<ECSL_DP::ComponentShortcut>(impl, meta_ComponentShortcut_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::ComponentShortcut, Pred> ComponentShortcut_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ComponentShortcut, Pred>(impl, meta_ComponentShortcut_children); }

		static Uml::CompositionChildRole meta_Signal_children;
		Udm::ChildrenAttr<ECSL_DP::Signal> Signal_children() const { return Udm::ChildrenAttr<ECSL_DP::Signal>(impl, meta_Signal_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Signal, Pred> Signal_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Signal, Pred>(impl, meta_Signal_children); }

		Udm::ChildrenAttr<ECSL_DP::ComponentShortcut> ComponentShortcut_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::ComponentShortcut>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::ComponentShortcut, Pred> ComponentShortcut_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ComponentShortcut, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Signal> Signal_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Signal>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Signal, Pred> Signal_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Signal, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Component> Component_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Component>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Component, Pred> Component_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Component, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_ComponentModels_parent;
		Udm::ParentAttr<ECSL_DP::ComponentModels> ComponentModels_parent() const { return Udm::ParentAttr<ECSL_DP::ComponentModels>(impl, meta_ComponentModels_parent); }

		Udm::ParentAttr<ECSL_DP::ComponentModels> parent() const { return Udm::ParentAttr<ECSL_DP::ComponentModels>(impl, Udm::NULLPARENTROLE); }
	};

	class  Signal :  public MgaObject {
	 public:
		static Uml::Class meta;

		Signal() { }
		Signal(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Signal(const Signal &master) : MgaObject(master) { }
		static Signal Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Signal Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Signal CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Signal CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Signal> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Signal>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Signal, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Signal, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Signal> Derived() { return Udm::DerivedAttr<ECSL_DP::Signal>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Signal, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Signal, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Signal> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Signal>(impl);}

		static Uml::CompositionParentRole meta_ComponentSheet_parent;
		Udm::ParentAttr<ECSL_DP::ComponentSheet> ComponentSheet_parent() const { return Udm::ParentAttr<ECSL_DP::ComponentSheet>(impl, meta_ComponentSheet_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_srcSignal_end_;
		Udm::AssocEndAttr<ECSL_DP::COutPort> srcSignal_end() const { return Udm::AssocEndAttr<ECSL_DP::COutPort>(impl, meta_srcSignal_end_); }

		static Uml::AssociationRole meta_dstSignal_end_;
		Udm::AssocEndAttr<ECSL_DP::CInPort> dstSignal_end() const { return Udm::AssocEndAttr<ECSL_DP::CInPort>(impl, meta_dstSignal_end_); }

	};

	class  OutPortMapping :  public MgaObject {
	 public:
		static Uml::Class meta;

		OutPortMapping() { }
		OutPortMapping(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		OutPortMapping(const OutPortMapping &master) : MgaObject(master) { }
		static OutPortMapping Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static OutPortMapping Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		OutPortMapping CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		OutPortMapping CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<OutPortMapping> Instances() { return Udm::InstantiatedAttr<ECSL_DP::OutPortMapping>(impl);}
		template <class Pred> Udm::InstantiatedAttr<OutPortMapping, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::OutPortMapping, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::OutPortMapping> Derived() { return Udm::DerivedAttr<ECSL_DP::OutPortMapping>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::OutPortMapping, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::OutPortMapping, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::OutPortMapping> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::OutPortMapping>(impl);}

		static Uml::CompositionParentRole meta_Component_parent;
		Udm::ParentAttr<ECSL_DP::Component> Component_parent() const { return Udm::ParentAttr<ECSL_DP::Component>(impl, meta_Component_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_dstOutPortMapping_end_;
		Udm::AssocEndAttr<ECSL_DP::COutPort> dstOutPortMapping_end() const { return Udm::AssocEndAttr<ECSL_DP::COutPort>(impl, meta_dstOutPortMapping_end_); }

		static Uml::AssociationRole meta_srcOutPortMapping_end_;
		Udm::AssocEndAttr<ECSL_DP::OutputPort> srcOutPortMapping_end() const { return Udm::AssocEndAttr<ECSL_DP::OutputPort>(impl, meta_srcOutPortMapping_end_); }

	};

	class  CPort :  public MgaObject {
	 public:
		static Uml::Class meta;

		CPort() { }
		CPort(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		CPort(const CPort &master) : MgaObject(master) { }
		static CPort Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static CPort Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		CPort CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		CPort CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<CPort> Instances() { return Udm::InstantiatedAttr<ECSL_DP::CPort>(impl);}
		template <class Pred> Udm::InstantiatedAttr<CPort, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::CPort, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::CPort> Derived() { return Udm::DerivedAttr<ECSL_DP::CPort>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::CPort, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::CPort, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::CPort> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::CPort>(impl);}

		static Uml::Attribute meta_Min;
		Udm::IntegerAttr Min() const { return Udm::IntegerAttr(impl, meta_Min); }

		static Uml::Attribute meta_Max;
		Udm::IntegerAttr Max() const { return Udm::IntegerAttr(impl, meta_Max); }

		static Uml::Attribute meta_CName;
		Udm::StringAttr CName() const { return Udm::StringAttr(impl, meta_CName); }

		static Uml::Attribute meta_DataOffset;
		Udm::RealAttr DataOffset() const { return Udm::RealAttr(impl, meta_DataOffset); }

		static Uml::Attribute meta_DataScale;
		Udm::RealAttr DataScale() const { return Udm::RealAttr(impl, meta_DataScale); }

		static Uml::Attribute meta_DataInit;
		Udm::StringAttr DataInit() const { return Udm::StringAttr(impl, meta_DataInit); }

		static Uml::Attribute meta_DataSign;
		Udm::BooleanAttr DataSign() const { return Udm::BooleanAttr(impl, meta_DataSign); }

		static Uml::Attribute meta_DataSize;
		Udm::StringAttr DataSize() const { return Udm::StringAttr(impl, meta_DataSize); }

		static Uml::Attribute meta_DataType;
		Udm::StringAttr DataType() const { return Udm::StringAttr(impl, meta_DataType); }

		static Uml::AssociationRole meta_srcInCommMapping, meta_srcInCommMapping_rev;
		Udm::AClassAssocAttr<ECSL_DP::InCommMapping, ECSL_DP::CommDst> srcInCommMapping() const { return Udm::AClassAssocAttr<ECSL_DP::InCommMapping, ECSL_DP::CommDst>(impl, meta_srcInCommMapping, meta_srcInCommMapping_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::InCommMapping, ECSL_DP::CommDst, Pred> srcInCommMapping_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::InCommMapping, ECSL_DP::CommDst, Pred>(impl, meta_srcInCommMapping, meta_srcInCommMapping_rev); }

		static Uml::AssociationRole meta_dstOutCommMapping, meta_dstOutCommMapping_rev;
		Udm::AClassAssocAttr<ECSL_DP::OutCommMapping, ECSL_DP::CommDst> dstOutCommMapping() const { return Udm::AClassAssocAttr<ECSL_DP::OutCommMapping, ECSL_DP::CommDst>(impl, meta_dstOutCommMapping, meta_dstOutCommMapping_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::OutCommMapping, ECSL_DP::CommDst, Pred> dstOutCommMapping_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::OutCommMapping, ECSL_DP::CommDst, Pred>(impl, meta_dstOutCommMapping, meta_dstOutCommMapping_rev); }

		static Uml::CompositionParentRole meta_Component_parent;
		Udm::ParentAttr<ECSL_DP::Component> Component_parent() const { return Udm::ParentAttr<ECSL_DP::Component>(impl, meta_Component_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  COutPort :  public CPort {
	 public:
		static Uml::Class meta;

		COutPort() { }
		COutPort(Udm::ObjectImpl *impl) : CPort(impl) { }
		COutPort(const COutPort &master) : CPort(master) { }
		static COutPort Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static COutPort Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		COutPort CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		COutPort CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<COutPort> Instances() { return Udm::InstantiatedAttr<ECSL_DP::COutPort>(impl);}
		template <class Pred> Udm::InstantiatedAttr<COutPort, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::COutPort, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::COutPort> Derived() { return Udm::DerivedAttr<ECSL_DP::COutPort>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::COutPort, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::COutPort, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::COutPort> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::COutPort>(impl);}

		static Uml::AssociationRole meta_srcOutPortMapping, meta_srcOutPortMapping_rev;
		Udm::AClassAssocAttr<ECSL_DP::OutPortMapping, ECSL_DP::OutputPort> srcOutPortMapping() const { return Udm::AClassAssocAttr<ECSL_DP::OutPortMapping, ECSL_DP::OutputPort>(impl, meta_srcOutPortMapping, meta_srcOutPortMapping_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::OutPortMapping, ECSL_DP::OutputPort, Pred> srcOutPortMapping_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::OutPortMapping, ECSL_DP::OutputPort, Pred>(impl, meta_srcOutPortMapping, meta_srcOutPortMapping_rev); }

		static Uml::AssociationRole meta_srcRTCIn, meta_srcRTCIn_rev;
		Udm::AClassAssocAttr<ECSL_DP::RTCIn, ECSL_DP::RTConstraint> srcRTCIn() const { return Udm::AClassAssocAttr<ECSL_DP::RTCIn, ECSL_DP::RTConstraint>(impl, meta_srcRTCIn, meta_srcRTCIn_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::RTCIn, ECSL_DP::RTConstraint, Pred> srcRTCIn_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::RTCIn, ECSL_DP::RTConstraint, Pred>(impl, meta_srcRTCIn, meta_srcRTCIn_rev); }

		static Uml::AssociationRole meta_dstSignal, meta_dstSignal_rev;
		Udm::AClassAssocAttr<ECSL_DP::Signal, ECSL_DP::CInPort> dstSignal() const { return Udm::AClassAssocAttr<ECSL_DP::Signal, ECSL_DP::CInPort>(impl, meta_dstSignal, meta_dstSignal_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::Signal, ECSL_DP::CInPort, Pred> dstSignal_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::Signal, ECSL_DP::CInPort, Pred>(impl, meta_dstSignal, meta_dstSignal_rev); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  CInPort :  public CPort {
	 public:
		static Uml::Class meta;

		CInPort() { }
		CInPort(Udm::ObjectImpl *impl) : CPort(impl) { }
		CInPort(const CInPort &master) : CPort(master) { }
		static CInPort Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static CInPort Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		CInPort CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		CInPort CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<CInPort> Instances() { return Udm::InstantiatedAttr<ECSL_DP::CInPort>(impl);}
		template <class Pred> Udm::InstantiatedAttr<CInPort, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::CInPort, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::CInPort> Derived() { return Udm::DerivedAttr<ECSL_DP::CInPort>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::CInPort, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::CInPort, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::CInPort> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::CInPort>(impl);}

		static Uml::AssociationRole meta_dstInPortMapping, meta_dstInPortMapping_rev;
		Udm::AClassAssocAttr<ECSL_DP::InPortMapping, ECSL_DP::InputPort> dstInPortMapping() const { return Udm::AClassAssocAttr<ECSL_DP::InPortMapping, ECSL_DP::InputPort>(impl, meta_dstInPortMapping, meta_dstInPortMapping_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::InPortMapping, ECSL_DP::InputPort, Pred> dstInPortMapping_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::InPortMapping, ECSL_DP::InputPort, Pred>(impl, meta_dstInPortMapping, meta_dstInPortMapping_rev); }

		static Uml::AssociationRole meta_dstRTCOut, meta_dstRTCOut_rev;
		Udm::AClassAssocAttr<ECSL_DP::RTCOut, ECSL_DP::RTConstraint> dstRTCOut() const { return Udm::AClassAssocAttr<ECSL_DP::RTCOut, ECSL_DP::RTConstraint>(impl, meta_dstRTCOut, meta_dstRTCOut_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::RTCOut, ECSL_DP::RTConstraint, Pred> dstRTCOut_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::RTCOut, ECSL_DP::RTConstraint, Pred>(impl, meta_dstRTCOut, meta_dstRTCOut_rev); }

		static Uml::AssociationRole meta_srcSignal, meta_srcSignal_rev;
		Udm::AClassAssocAttr<ECSL_DP::Signal, ECSL_DP::COutPort> srcSignal() const { return Udm::AClassAssocAttr<ECSL_DP::Signal, ECSL_DP::COutPort>(impl, meta_srcSignal, meta_srcSignal_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::Signal, ECSL_DP::COutPort, Pred> srcSignal_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::Signal, ECSL_DP::COutPort, Pred>(impl, meta_srcSignal, meta_srcSignal_rev); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Component :  public MgaObject {
	 public:
		static Uml::Class meta;

		Component() { }
		Component(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Component(const Component &master) : MgaObject(master) { }
		static Component Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Component Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Component CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Component CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Component> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Component>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Component, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Component, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Component> Derived() { return Udm::DerivedAttr<ECSL_DP::Component>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Component, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Component, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Component> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Component>(impl);}

		static Uml::Attribute meta_CName;
		Udm::StringAttr CName() const { return Udm::StringAttr(impl, meta_CName); }

		static Uml::AssociationRole meta_referedbyComponentShortcut;
		Udm::AssocAttr<ECSL_DP::ComponentShortcut> referedbyComponentShortcut() const { return Udm::AssocAttr<ECSL_DP::ComponentShortcut>(impl, meta_referedbyComponentShortcut); }
		template <class Pred> Udm::AssocAttr<ECSL_DP::ComponentShortcut, Pred > referedbyComponentShortcut_sorted(const Pred &) const { return Udm::AssocAttr<ECSL_DP::ComponentShortcut, Pred>(impl, meta_referedbyComponentShortcut); }

		static Uml::AssociationRole meta_referedbyComponentRef;
		Udm::AssocAttr<ECSL_DP::ComponentRef> referedbyComponentRef() const { return Udm::AssocAttr<ECSL_DP::ComponentRef>(impl, meta_referedbyComponentRef); }
		template <class Pred> Udm::AssocAttr<ECSL_DP::ComponentRef, Pred > referedbyComponentRef_sorted(const Pred &) const { return Udm::AssocAttr<ECSL_DP::ComponentRef, Pred>(impl, meta_referedbyComponentRef); }

		static Uml::CompositionChildRole meta_CPort_children;
		Udm::ChildrenAttr<ECSL_DP::CPort> CPort_children() const { return Udm::ChildrenAttr<ECSL_DP::CPort>(impl, meta_CPort_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::CPort, Pred> CPort_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::CPort, Pred>(impl, meta_CPort_children); }

		static Uml::CompositionChildRole meta_SystemRef_child;
		Udm::ChildAttr<ECSL_DP::SystemRef> SystemRef_child() const { return Udm::ChildAttr<ECSL_DP::SystemRef>(impl, meta_SystemRef_child); }

		static Uml::CompositionChildRole meta_RTConstraint_children;
		Udm::ChildrenAttr<ECSL_DP::RTConstraint> RTConstraint_children() const { return Udm::ChildrenAttr<ECSL_DP::RTConstraint>(impl, meta_RTConstraint_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::RTConstraint, Pred> RTConstraint_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::RTConstraint, Pred>(impl, meta_RTConstraint_children); }

		static Uml::CompositionChildRole meta_RTCIn_children;
		Udm::ChildrenAttr<ECSL_DP::RTCIn> RTCIn_children() const { return Udm::ChildrenAttr<ECSL_DP::RTCIn>(impl, meta_RTCIn_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::RTCIn, Pred> RTCIn_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::RTCIn, Pred>(impl, meta_RTCIn_children); }

		static Uml::CompositionChildRole meta_RTCOut_children;
		Udm::ChildrenAttr<ECSL_DP::RTCOut> RTCOut_children() const { return Udm::ChildrenAttr<ECSL_DP::RTCOut>(impl, meta_RTCOut_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::RTCOut, Pred> RTCOut_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::RTCOut, Pred>(impl, meta_RTCOut_children); }

		static Uml::CompositionChildRole meta_OutPortMapping_children;
		Udm::ChildrenAttr<ECSL_DP::OutPortMapping> OutPortMapping_children() const { return Udm::ChildrenAttr<ECSL_DP::OutPortMapping>(impl, meta_OutPortMapping_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::OutPortMapping, Pred> OutPortMapping_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::OutPortMapping, Pred>(impl, meta_OutPortMapping_children); }

		static Uml::CompositionChildRole meta_InPortMapping_children;
		Udm::ChildrenAttr<ECSL_DP::InPortMapping> InPortMapping_children() const { return Udm::ChildrenAttr<ECSL_DP::InPortMapping>(impl, meta_InPortMapping_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::InPortMapping, Pred> InPortMapping_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::InPortMapping, Pred>(impl, meta_InPortMapping_children); }

		Udm::ChildrenAttr<ECSL_DP::RTConstraint> RTConstraint_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::RTConstraint>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::RTConstraint, Pred> RTConstraint_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::RTConstraint, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::RTCOut> RTCOut_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::RTCOut>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::RTCOut, Pred> RTCOut_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::RTCOut, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::RTCIn> RTCIn_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::RTCIn>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::RTCIn, Pred> RTCIn_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::RTCIn, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::SystemRef> SystemRef_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::SystemRef>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::SystemRef, Pred> SystemRef_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::SystemRef, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::InPortMapping> InPortMapping_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::InPortMapping>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::InPortMapping, Pred> InPortMapping_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::InPortMapping, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::COutPort> COutPort_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::COutPort>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::COutPort, Pred> COutPort_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::COutPort, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::CInPort> CInPort_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::CInPort>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::CInPort, Pred> CInPort_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::CInPort, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::OutPortMapping> OutPortMapping_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::OutPortMapping>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::OutPortMapping, Pred> OutPortMapping_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::OutPortMapping, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::CPort> CPort_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::CPort>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::CPort, Pred> CPort_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::CPort, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_ComponentSheet_parent;
		Udm::ParentAttr<ECSL_DP::ComponentSheet> ComponentSheet_parent() const { return Udm::ParentAttr<ECSL_DP::ComponentSheet>(impl, meta_ComponentSheet_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  COM :  public MgaObject {
	 public:
		static Uml::Class meta;

		COM() { }
		COM(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		COM(const COM &master) : MgaObject(master) { }
		static COM Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static COM Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		COM CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		COM CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<COM> Instances() { return Udm::InstantiatedAttr<ECSL_DP::COM>(impl);}
		template <class Pred> Udm::InstantiatedAttr<COM, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::COM, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::COM> Derived() { return Udm::DerivedAttr<ECSL_DP::COM>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::COM, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::COM, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::COM> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::COM>(impl);}

		static Uml::Attribute meta_HandleRX;
		Udm::BooleanAttr HandleRX() const { return Udm::BooleanAttr(impl, meta_HandleRX); }

		static Uml::Attribute meta_HandleTX;
		Udm::BooleanAttr HandleTX() const { return Udm::BooleanAttr(impl, meta_HandleTX); }

		static Uml::Attribute meta_HandleCCL;
		Udm::BooleanAttr HandleCCL() const { return Udm::BooleanAttr(impl, meta_HandleCCL); }

		static Uml::Attribute meta_HandleNM;
		Udm::BooleanAttr HandleNM() const { return Udm::BooleanAttr(impl, meta_HandleNM); }

		static Uml::Attribute meta_CycleTime;
		Udm::StringAttr CycleTime() const { return Udm::StringAttr(impl, meta_CycleTime); }

		static Uml::Attribute meta_GenerateTask;
		Udm::BooleanAttr GenerateTask() const { return Udm::BooleanAttr(impl, meta_GenerateTask); }

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  FirmwareLink :  public MgaObject {
	 public:
		static Uml::Class meta;

		FirmwareLink() { }
		FirmwareLink(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		FirmwareLink(const FirmwareLink &master) : MgaObject(master) { }
		static FirmwareLink Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static FirmwareLink Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		FirmwareLink CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		FirmwareLink CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<FirmwareLink> Instances() { return Udm::InstantiatedAttr<ECSL_DP::FirmwareLink>(impl);}
		template <class Pred> Udm::InstantiatedAttr<FirmwareLink, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::FirmwareLink, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::FirmwareLink> Derived() { return Udm::DerivedAttr<ECSL_DP::FirmwareLink>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::FirmwareLink, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::FirmwareLink, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::FirmwareLink> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::FirmwareLink>(impl);}

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_dstFirmwareLink_end_;
		Udm::AssocEndAttr<ECSL_DP::Channel> dstFirmwareLink_end() const { return Udm::AssocEndAttr<ECSL_DP::Channel>(impl, meta_dstFirmwareLink_end_); }

		static Uml::AssociationRole meta_srcFirmwareLink_end_;
		Udm::AssocEndAttr<ECSL_DP::FirmwareModule> srcFirmwareLink_end() const { return Udm::AssocEndAttr<ECSL_DP::FirmwareModule>(impl, meta_srcFirmwareLink_end_); }

	};

	class  FirmwareModule :  public MgaObject {
	 public:
		static Uml::Class meta;

		FirmwareModule() { }
		FirmwareModule(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		FirmwareModule(const FirmwareModule &master) : MgaObject(master) { }
		static FirmwareModule Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static FirmwareModule Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		FirmwareModule CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		FirmwareModule CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<FirmwareModule> Instances() { return Udm::InstantiatedAttr<ECSL_DP::FirmwareModule>(impl);}
		template <class Pred> Udm::InstantiatedAttr<FirmwareModule, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::FirmwareModule, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::FirmwareModule> Derived() { return Udm::DerivedAttr<ECSL_DP::FirmwareModule>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::FirmwareModule, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::FirmwareModule, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::FirmwareModule> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::FirmwareModule>(impl);}

		static Uml::Attribute meta_EventPublished;
		Udm::StringAttr EventPublished() const { return Udm::StringAttr(impl, meta_EventPublished); }

		static Uml::Attribute meta_WriteAccessor;
		Udm::StringAttr WriteAccessor() const { return Udm::StringAttr(impl, meta_WriteAccessor); }

		static Uml::Attribute meta_ReadAccessor;
		Udm::StringAttr ReadAccessor() const { return Udm::StringAttr(impl, meta_ReadAccessor); }

		static Uml::Attribute meta_SourceFile;
		Udm::StringAttr SourceFile() const { return Udm::StringAttr(impl, meta_SourceFile); }

		static Uml::Attribute meta_ISR;
		Udm::StringAttr ISR() const { return Udm::StringAttr(impl, meta_ISR); }

		static Uml::Attribute meta_LibraryFile;
		Udm::StringAttr LibraryFile() const { return Udm::StringAttr(impl, meta_LibraryFile); }

		static Uml::Attribute meta_OSBinding;
		Udm::StringAttr OSBinding() const { return Udm::StringAttr(impl, meta_OSBinding); }

		static Uml::AssociationRole meta_dstFirmwareLink, meta_dstFirmwareLink_rev;
		Udm::AClassAssocAttr<ECSL_DP::FirmwareLink, ECSL_DP::Channel> dstFirmwareLink() const { return Udm::AClassAssocAttr<ECSL_DP::FirmwareLink, ECSL_DP::Channel>(impl, meta_dstFirmwareLink, meta_dstFirmwareLink_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::FirmwareLink, ECSL_DP::Channel, Pred> dstFirmwareLink_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::FirmwareLink, ECSL_DP::Channel, Pred>(impl, meta_dstFirmwareLink, meta_dstFirmwareLink_rev); }

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  OS :  public MgaObject {
	 public:
		static Uml::Class meta;

		OS() { }
		OS(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		OS(const OS &master) : MgaObject(master) { }
		static OS Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static OS Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		OS CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		OS CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<OS> Instances() { return Udm::InstantiatedAttr<ECSL_DP::OS>(impl);}
		template <class Pred> Udm::InstantiatedAttr<OS, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::OS, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::OS> Derived() { return Udm::DerivedAttr<ECSL_DP::OS>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::OS, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::OS, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::OS> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::OS>(impl);}

		static Uml::Attribute meta_Compiler;
		Udm::StringAttr Compiler() const { return Udm::StringAttr(impl, meta_Compiler); }

		static Uml::Attribute meta_Conformance;
		Udm::StringAttr Conformance() const { return Udm::StringAttr(impl, meta_Conformance); }

		static Uml::Attribute meta_Schedule;
		Udm::StringAttr Schedule() const { return Udm::StringAttr(impl, meta_Schedule); }

		static Uml::Attribute meta_TickTime;
		Udm::IntegerAttr TickTime() const { return Udm::IntegerAttr(impl, meta_TickTime); }

		static Uml::Attribute meta_Status;
		Udm::StringAttr Status() const { return Udm::StringAttr(impl, meta_Status); }

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  CommElement :  virtual public MgaObject {
	 public:
		static Uml::Class meta;

		CommElement() { }
		CommElement(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		CommElement(const CommElement &master) : MgaObject(master) { }
		static CommElement Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static CommElement Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		CommElement CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		CommElement CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<CommElement> Instances() { return Udm::InstantiatedAttr<ECSL_DP::CommElement>(impl);}
		template <class Pred> Udm::InstantiatedAttr<CommElement, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::CommElement, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::CommElement> Derived() { return Udm::DerivedAttr<ECSL_DP::CommElement>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::CommElement, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::CommElement, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::CommElement> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::CommElement>(impl);}

		static Uml::AssociationRole meta_srcWire, meta_srcWire_rev;
		Udm::AClassAssocAttr<ECSL_DP::Wire, ECSL_DP::CommElement> srcWire() const { return Udm::AClassAssocAttr<ECSL_DP::Wire, ECSL_DP::CommElement>(impl, meta_srcWire, meta_srcWire_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::Wire, ECSL_DP::CommElement, Pred> srcWire_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::Wire, ECSL_DP::CommElement, Pred>(impl, meta_srcWire, meta_srcWire_rev); }

		static Uml::AssociationRole meta_dstWire, meta_dstWire_rev;
		Udm::AClassAssocAttr<ECSL_DP::Wire, ECSL_DP::CommElement> dstWire() const { return Udm::AClassAssocAttr<ECSL_DP::Wire, ECSL_DP::CommElement>(impl, meta_dstWire, meta_dstWire_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::Wire, ECSL_DP::CommElement, Pred> dstWire_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::Wire, ECSL_DP::CommElement, Pred>(impl, meta_dstWire, meta_dstWire_rev); }

		Udm::ParentAttr<Udm::Object> parent() const { return Udm::ParentAttr<Udm::Object>(impl, Udm::NULLPARENTROLE); }
	};

	class  HWElement :  virtual public MgaObject {
	 public:
		static Uml::Class meta;

		HWElement() { }
		HWElement(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		HWElement(const HWElement &master) : MgaObject(master) { }
		static HWElement Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static HWElement Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		HWElement CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		HWElement CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<HWElement> Instances() { return Udm::InstantiatedAttr<ECSL_DP::HWElement>(impl);}
		template <class Pred> Udm::InstantiatedAttr<HWElement, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::HWElement, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::HWElement> Derived() { return Udm::DerivedAttr<ECSL_DP::HWElement>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::HWElement, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::HWElement, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::HWElement> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::HWElement>(impl);}

		static Uml::CompositionParentRole meta_HardwareSheet_parent;
		Udm::ParentAttr<ECSL_DP::HardwareSheet> HardwareSheet_parent() const { return Udm::ParentAttr<ECSL_DP::HardwareSheet>(impl, meta_HardwareSheet_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Wire :  public HWElement {
	 public:
		static Uml::Class meta;

		Wire() { }
		Wire(Udm::ObjectImpl *impl) : HWElement(impl), MgaObject(impl) { }
		Wire(const Wire &master) : HWElement(master), MgaObject(master) { }
		static Wire Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Wire Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Wire CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Wire CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Wire> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Wire>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Wire, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Wire, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Wire> Derived() { return Udm::DerivedAttr<ECSL_DP::Wire>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Wire, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Wire, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Wire> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Wire>(impl);}

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_dstWire_end_;
		Udm::AssocEndAttr<ECSL_DP::CommElement> dstWire_end() const { return Udm::AssocEndAttr<ECSL_DP::CommElement>(impl, meta_dstWire_end_); }

		static Uml::AssociationRole meta_srcWire_end_;
		Udm::AssocEndAttr<ECSL_DP::CommElement> srcWire_end() const { return Udm::AssocEndAttr<ECSL_DP::CommElement>(impl, meta_srcWire_end_); }

	};

	class  Bus :  public CommElement, public HWElement {
	 public:
		static Uml::Class meta;

		Bus() { }
		Bus(Udm::ObjectImpl *impl) : CommElement(impl),HWElement(impl), MgaObject(impl) { }
		Bus(const Bus &master) : CommElement(master),HWElement(master), MgaObject(master) { }
		static Bus Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Bus Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Bus CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Bus CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Bus> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Bus>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Bus, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Bus, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Bus> Derived() { return Udm::DerivedAttr<ECSL_DP::Bus>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Bus, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Bus, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Bus> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Bus>(impl);}

		static Uml::Attribute meta_NM;
		Udm::BooleanAttr NM() const { return Udm::BooleanAttr(impl, meta_NM); }

		static Uml::Attribute meta_CName;
		Udm::StringAttr CName() const { return Udm::StringAttr(impl, meta_CName); }

		static Uml::Attribute meta_Medium;
		Udm::StringAttr Medium() const { return Udm::StringAttr(impl, meta_Medium); }

		static Uml::Attribute meta_FrameSize;
		Udm::IntegerAttr FrameSize() const { return Udm::IntegerAttr(impl, meta_FrameSize); }

		static Uml::Attribute meta_BitRate;
		Udm::IntegerAttr BitRate() const { return Udm::IntegerAttr(impl, meta_BitRate); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  HardwareSheet :  public MgaObject {
	 public:
		static Uml::Class meta;

		HardwareSheet() { }
		HardwareSheet(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		HardwareSheet(const HardwareSheet &master) : MgaObject(master) { }
		static HardwareSheet Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static HardwareSheet Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		HardwareSheet CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		HardwareSheet CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<HardwareSheet> Instances() { return Udm::InstantiatedAttr<ECSL_DP::HardwareSheet>(impl);}
		template <class Pred> Udm::InstantiatedAttr<HardwareSheet, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::HardwareSheet, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::HardwareSheet> Derived() { return Udm::DerivedAttr<ECSL_DP::HardwareSheet>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::HardwareSheet, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::HardwareSheet, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::HardwareSheet> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::HardwareSheet>(impl);}

		static Uml::CompositionChildRole meta_HWElement_children;
		Udm::ChildrenAttr<ECSL_DP::HWElement> HWElement_children() const { return Udm::ChildrenAttr<ECSL_DP::HWElement>(impl, meta_HWElement_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::HWElement, Pred> HWElement_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::HWElement, Pred>(impl, meta_HWElement_children); }

		Udm::ChildrenAttr<ECSL_DP::Wire> Wire_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Wire>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Wire, Pred> Wire_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Wire, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Bus> Bus_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Bus>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Bus, Pred> Bus_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Bus, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::HWElement> HWElement_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::HWElement>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::HWElement, Pred> HWElement_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::HWElement, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::ECU> ECU_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::ECU>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::ECU, Pred> ECU_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ECU, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		static Uml::CompositionParentRole meta_HardwareModels_parent;
		Udm::ParentAttr<ECSL_DP::HardwareModels> HardwareModels_parent() const { return Udm::ParentAttr<ECSL_DP::HardwareModels>(impl, meta_HardwareModels_parent); }

		Udm::ParentAttr<ECSL_DP::HardwareModels> parent() const { return Udm::ParentAttr<ECSL_DP::HardwareModels>(impl, Udm::NULLPARENTROLE); }
	};

	class  Channel :  virtual public MgaObject {
	 public:
		static Uml::Class meta;

		Channel() { }
		Channel(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Channel(const Channel &master) : MgaObject(master) { }
		static Channel Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Channel Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Channel CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Channel CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Channel> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Channel>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Channel, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Channel, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Channel> Derived() { return Udm::DerivedAttr<ECSL_DP::Channel>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Channel, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Channel, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Channel> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Channel>(impl);}

		static Uml::Attribute meta_InterruptNum;
		Udm::StringAttr InterruptNum() const { return Udm::StringAttr(impl, meta_InterruptNum); }

		static Uml::Attribute meta_CName;
		Udm::StringAttr CName() const { return Udm::StringAttr(impl, meta_CName); }

		static Uml::AssociationRole meta_srcFirmwareLink, meta_srcFirmwareLink_rev;
		Udm::AClassAssocAttr<ECSL_DP::FirmwareLink, ECSL_DP::FirmwareModule> srcFirmwareLink() const { return Udm::AClassAssocAttr<ECSL_DP::FirmwareLink, ECSL_DP::FirmwareModule>(impl, meta_srcFirmwareLink, meta_srcFirmwareLink_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::FirmwareLink, ECSL_DP::FirmwareModule, Pred> srcFirmwareLink_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::FirmwareLink, ECSL_DP::FirmwareModule, Pred>(impl, meta_srcFirmwareLink, meta_srcFirmwareLink_rev); }

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  BusChan :  public CommElement, public Channel {
	 public:
		static Uml::Class meta;

		BusChan() { }
		BusChan(Udm::ObjectImpl *impl) : CommElement(impl),Channel(impl), MgaObject(impl) { }
		BusChan(const BusChan &master) : CommElement(master),Channel(master), MgaObject(master) { }
		static BusChan Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static BusChan Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		BusChan CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		BusChan CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<BusChan> Instances() { return Udm::InstantiatedAttr<ECSL_DP::BusChan>(impl);}
		template <class Pred> Udm::InstantiatedAttr<BusChan, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::BusChan, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::BusChan> Derived() { return Udm::DerivedAttr<ECSL_DP::BusChan>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::BusChan, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::BusChan, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::BusChan> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::BusChan>(impl);}

		static Uml::AssociationRole meta_members;
		Udm::AssocAttr<ECSL_DP::BusMessage> members() const { return Udm::AssocAttr<ECSL_DP::BusMessage>(impl, meta_members); }
		template <class Pred> Udm::AssocAttr<ECSL_DP::BusMessage, Pred > members_sorted(const Pred &) const { return Udm::AssocAttr<ECSL_DP::BusMessage, Pred>(impl, meta_members); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  ECU :  public HWElement {
	 public:
		static Uml::Class meta;

		ECU() { }
		ECU(Udm::ObjectImpl *impl) : HWElement(impl), MgaObject(impl) { }
		ECU(const ECU &master) : HWElement(master), MgaObject(master) { }
		static ECU Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static ECU Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		ECU CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		ECU CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<ECU> Instances() { return Udm::InstantiatedAttr<ECSL_DP::ECU>(impl);}
		template <class Pred> Udm::InstantiatedAttr<ECU, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::ECU, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::ECU> Derived() { return Udm::DerivedAttr<ECSL_DP::ECU>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::ECU, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::ECU, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::ECU> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::ECU>(impl);}

		static Uml::Attribute meta_Simulator;
		Udm::StringAttr Simulator() const { return Udm::StringAttr(impl, meta_Simulator); }

		static Uml::Attribute meta_CName;
		Udm::StringAttr CName() const { return Udm::StringAttr(impl, meta_CName); }

		static Uml::Attribute meta_ROM;
		Udm::IntegerAttr ROM() const { return Udm::IntegerAttr(impl, meta_ROM); }

		static Uml::Attribute meta_RAM;
		Udm::IntegerAttr RAM() const { return Udm::IntegerAttr(impl, meta_RAM); }

		static Uml::Attribute meta_Speed;
		Udm::IntegerAttr Speed() const { return Udm::IntegerAttr(impl, meta_Speed); }

		static Uml::Attribute meta_CPU;
		Udm::StringAttr CPU() const { return Udm::StringAttr(impl, meta_CPU); }

		static Uml::CompositionChildRole meta_OS_child;
		Udm::ChildAttr<ECSL_DP::OS> OS_child() const { return Udm::ChildAttr<ECSL_DP::OS>(impl, meta_OS_child); }

		static Uml::CompositionChildRole meta_COM_children;
		Udm::ChildrenAttr<ECSL_DP::COM> COM_children() const { return Udm::ChildrenAttr<ECSL_DP::COM>(impl, meta_COM_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::COM, Pred> COM_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::COM, Pred>(impl, meta_COM_children); }

		static Uml::CompositionChildRole meta_Channel_children;
		Udm::ChildrenAttr<ECSL_DP::Channel> Channel_children() const { return Udm::ChildrenAttr<ECSL_DP::Channel>(impl, meta_Channel_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Channel, Pred> Channel_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Channel, Pred>(impl, meta_Channel_children); }

		static Uml::CompositionChildRole meta_FirmwareModule_children;
		Udm::ChildrenAttr<ECSL_DP::FirmwareModule> FirmwareModule_children() const { return Udm::ChildrenAttr<ECSL_DP::FirmwareModule>(impl, meta_FirmwareModule_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::FirmwareModule, Pred> FirmwareModule_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::FirmwareModule, Pred>(impl, meta_FirmwareModule_children); }

		static Uml::CompositionChildRole meta_ComponentRef_children;
		Udm::ChildrenAttr<ECSL_DP::ComponentRef> ComponentRef_children() const { return Udm::ChildrenAttr<ECSL_DP::ComponentRef>(impl, meta_ComponentRef_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::ComponentRef, Pred> ComponentRef_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ComponentRef, Pred>(impl, meta_ComponentRef_children); }

		static Uml::CompositionChildRole meta_Task_children;
		Udm::ChildrenAttr<ECSL_DP::Task> Task_children() const { return Udm::ChildrenAttr<ECSL_DP::Task>(impl, meta_Task_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Task, Pred> Task_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Task, Pred>(impl, meta_Task_children); }

		static Uml::CompositionChildRole meta_CommMapping_children;
		Udm::ChildrenAttr<ECSL_DP::CommMapping> CommMapping_children() const { return Udm::ChildrenAttr<ECSL_DP::CommMapping>(impl, meta_CommMapping_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::CommMapping, Pred> CommMapping_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::CommMapping, Pred>(impl, meta_CommMapping_children); }

		static Uml::CompositionChildRole meta_Order_children;
		Udm::ChildrenAttr<ECSL_DP::Order> Order_children() const { return Udm::ChildrenAttr<ECSL_DP::Order>(impl, meta_Order_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::Order, Pred> Order_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Order, Pred>(impl, meta_Order_children); }

		static Uml::CompositionChildRole meta_FirmwareLink_children;
		Udm::ChildrenAttr<ECSL_DP::FirmwareLink> FirmwareLink_children() const { return Udm::ChildrenAttr<ECSL_DP::FirmwareLink>(impl, meta_FirmwareLink_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::FirmwareLink, Pred> FirmwareLink_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::FirmwareLink, Pred>(impl, meta_FirmwareLink_children); }

		static Uml::CompositionChildRole meta_BusMessage_children;
		Udm::ChildrenAttr<ECSL_DP::BusMessage> BusMessage_children() const { return Udm::ChildrenAttr<ECSL_DP::BusMessage>(impl, meta_BusMessage_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::BusMessage, Pred> BusMessage_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::BusMessage, Pred>(impl, meta_BusMessage_children); }

		static Uml::CompositionChildRole meta_BusMessageRef_children;
		Udm::ChildrenAttr<ECSL_DP::BusMessageRef> BusMessageRef_children() const { return Udm::ChildrenAttr<ECSL_DP::BusMessageRef>(impl, meta_BusMessageRef_children); }
		 template <class Pred> Udm::ChildrenAttr<ECSL_DP::BusMessageRef, Pred> BusMessageRef_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::BusMessageRef, Pred>(impl, meta_BusMessageRef_children); }

		Udm::ChildrenAttr<ECSL_DP::COM> COM_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::COM>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::COM, Pred> COM_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::COM, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::FirmwareLink> FirmwareLink_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::FirmwareLink>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::FirmwareLink, Pred> FirmwareLink_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::FirmwareLink, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::FirmwareModule> FirmwareModule_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::FirmwareModule>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::FirmwareModule, Pred> FirmwareModule_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::FirmwareModule, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::OS> OS_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::OS>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::OS, Pred> OS_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::OS, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::BusChan> BusChan_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::BusChan>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::BusChan, Pred> BusChan_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::BusChan, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Channel> Channel_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Channel>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Channel, Pred> Channel_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Channel, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::IChan> IChan_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::IChan>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::IChan, Pred> IChan_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::IChan, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::OChan> OChan_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::OChan>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::OChan, Pred> OChan_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::OChan, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::BusMessage> BusMessage_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::BusMessage>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::BusMessage, Pred> BusMessage_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::BusMessage, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::BusMessageRef> BusMessageRef_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::BusMessageRef>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::BusMessageRef, Pred> BusMessageRef_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::BusMessageRef, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::OutCommMapping> OutCommMapping_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::OutCommMapping>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::OutCommMapping, Pred> OutCommMapping_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::OutCommMapping, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Task> Task_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Task>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Task, Pred> Task_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Task, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::ComponentRef> ComponentRef_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::ComponentRef>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::ComponentRef, Pred> ComponentRef_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::ComponentRef, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::Order> Order_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::Order>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::Order, Pred> Order_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::Order, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::CommDst> CommDst_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::CommDst>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::CommDst, Pred> CommDst_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::CommDst, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::CommMapping> CommMapping_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::CommMapping>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::CommMapping, Pred> CommMapping_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::CommMapping, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::InCommMapping> InCommMapping_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::InCommMapping>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::InCommMapping, Pred> InCommMapping_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::InCommMapping, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ChildrenAttr<ECSL_DP::MgaObject> MgaObject_kind_children() const { return Udm::ChildrenAttr<ECSL_DP::MgaObject>(impl, Udm::NULLCHILDROLE); }
		template<class Pred> Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred> MgaObject_kind_children_sorted(const Pred &) const { return Udm::ChildrenAttr<ECSL_DP::MgaObject, Pred>(impl, Udm::NULLCHILDROLE); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Task :  public MgaObject {
	 public:
		static Uml::Class meta;

		Task() { }
		Task(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Task(const Task &master) : MgaObject(master) { }
		static Task Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Task Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Task CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Task CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Task> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Task>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Task, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Task, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Task> Derived() { return Udm::DerivedAttr<ECSL_DP::Task>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Task, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Task, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Task> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Task>(impl);}

		static Uml::Attribute meta_CycleTime;
		Udm::IntegerAttr CycleTime() const { return Udm::IntegerAttr(impl, meta_CycleTime); }

		static Uml::Attribute meta_Activation;
		Udm::IntegerAttr Activation() const { return Udm::IntegerAttr(impl, meta_Activation); }

		static Uml::Attribute meta_Cyclic;
		Udm::BooleanAttr Cyclic() const { return Udm::BooleanAttr(impl, meta_Cyclic); }

		static Uml::Attribute meta_Type;
		Udm::StringAttr Type() const { return Udm::StringAttr(impl, meta_Type); }

		static Uml::Attribute meta_Preemption;
		Udm::StringAttr Preemption() const { return Udm::StringAttr(impl, meta_Preemption); }

		static Uml::Attribute meta_Comment;
		Udm::StringAttr Comment() const { return Udm::StringAttr(impl, meta_Comment); }

		static Uml::Attribute meta_AutoStart;
		Udm::BooleanAttr AutoStart() const { return Udm::BooleanAttr(impl, meta_AutoStart); }

		static Uml::Attribute meta_Priority;
		Udm::IntegerAttr Priority() const { return Udm::IntegerAttr(impl, meta_Priority); }

		static Uml::AssociationRole meta_members;
		Udm::AssocAttr<ECSL_DP::ComponentRef> members() const { return Udm::AssocAttr<ECSL_DP::ComponentRef>(impl, meta_members); }
		template <class Pred> Udm::AssocAttr<ECSL_DP::ComponentRef, Pred > members_sorted(const Pred &) const { return Udm::AssocAttr<ECSL_DP::ComponentRef, Pred>(impl, meta_members); }

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  ComponentRef :  public MgaObject {
	 public:
		static Uml::Class meta;

		ComponentRef() { }
		ComponentRef(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		ComponentRef(const ComponentRef &master) : MgaObject(master) { }
		static ComponentRef Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static ComponentRef Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		ComponentRef CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		ComponentRef CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<ComponentRef> Instances() { return Udm::InstantiatedAttr<ECSL_DP::ComponentRef>(impl);}
		template <class Pred> Udm::InstantiatedAttr<ComponentRef, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::ComponentRef, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::ComponentRef> Derived() { return Udm::DerivedAttr<ECSL_DP::ComponentRef>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::ComponentRef, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::ComponentRef, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::ComponentRef> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::ComponentRef>(impl);}

		static Uml::AssociationRole meta_ref;
		Udm::PointerAttr<ECSL_DP::Component> ref() const { return Udm::PointerAttr<ECSL_DP::Component>(impl, meta_ref); }

		static Uml::AssociationRole meta_Task;
		// Access method for non-navigable association Task omitted 

		static Uml::AssociationRole meta_dstOrder, meta_dstOrder_rev;
		Udm::AClassAssocAttr<ECSL_DP::Order, ECSL_DP::ComponentRef> dstOrder() const { return Udm::AClassAssocAttr<ECSL_DP::Order, ECSL_DP::ComponentRef>(impl, meta_dstOrder, meta_dstOrder_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::Order, ECSL_DP::ComponentRef, Pred> dstOrder_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::Order, ECSL_DP::ComponentRef, Pred>(impl, meta_dstOrder, meta_dstOrder_rev); }

		static Uml::AssociationRole meta_srcOrder, meta_srcOrder_rev;
		Udm::AClassAssocAttr<ECSL_DP::Order, ECSL_DP::ComponentRef> srcOrder() const { return Udm::AClassAssocAttr<ECSL_DP::Order, ECSL_DP::ComponentRef>(impl, meta_srcOrder, meta_srcOrder_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::Order, ECSL_DP::ComponentRef, Pred> srcOrder_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::Order, ECSL_DP::ComponentRef, Pred>(impl, meta_srcOrder, meta_srcOrder_rev); }

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  Order :  public MgaObject {
	 public:
		static Uml::Class meta;

		Order() { }
		Order(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		Order(const Order &master) : MgaObject(master) { }
		static Order Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static Order Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		Order CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		Order CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<Order> Instances() { return Udm::InstantiatedAttr<ECSL_DP::Order>(impl);}
		template <class Pred> Udm::InstantiatedAttr<Order, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::Order, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::Order> Derived() { return Udm::DerivedAttr<ECSL_DP::Order>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::Order, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::Order, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::Order> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::Order>(impl);}

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_srcOrder_end_;
		Udm::AssocEndAttr<ECSL_DP::ComponentRef> srcOrder_end() const { return Udm::AssocEndAttr<ECSL_DP::ComponentRef>(impl, meta_srcOrder_end_); }

		static Uml::AssociationRole meta_dstOrder_end_;
		Udm::AssocEndAttr<ECSL_DP::ComponentRef> dstOrder_end() const { return Udm::AssocEndAttr<ECSL_DP::ComponentRef>(impl, meta_dstOrder_end_); }

	};

	class  CommDst :  virtual public MgaObject {
	 public:
		static Uml::Class meta;

		CommDst() { }
		CommDst(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		CommDst(const CommDst &master) : MgaObject(master) { }
		static CommDst Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static CommDst Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		CommDst CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		CommDst CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<CommDst> Instances() { return Udm::InstantiatedAttr<ECSL_DP::CommDst>(impl);}
		template <class Pred> Udm::InstantiatedAttr<CommDst, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::CommDst, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::CommDst> Derived() { return Udm::DerivedAttr<ECSL_DP::CommDst>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::CommDst, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::CommDst, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::CommDst> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::CommDst>(impl);}

		static Uml::AssociationRole meta_dstInCommMapping, meta_dstInCommMapping_rev;
		Udm::AClassAssocAttr<ECSL_DP::InCommMapping, ECSL_DP::CPort> dstInCommMapping() const { return Udm::AClassAssocAttr<ECSL_DP::InCommMapping, ECSL_DP::CPort>(impl, meta_dstInCommMapping, meta_dstInCommMapping_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::InCommMapping, ECSL_DP::CPort, Pred> dstInCommMapping_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::InCommMapping, ECSL_DP::CPort, Pred>(impl, meta_dstInCommMapping, meta_dstInCommMapping_rev); }

		static Uml::AssociationRole meta_srcOutCommMapping, meta_srcOutCommMapping_rev;
		Udm::AClassAssocAttr<ECSL_DP::OutCommMapping, ECSL_DP::CPort> srcOutCommMapping() const { return Udm::AClassAssocAttr<ECSL_DP::OutCommMapping, ECSL_DP::CPort>(impl, meta_srcOutCommMapping, meta_srcOutCommMapping_rev); }
		template<class Pred> Udm::AClassAssocAttr<ECSL_DP::OutCommMapping, ECSL_DP::CPort, Pred> srcOutCommMapping_sorted(const Pred &) const { return Udm::AClassAssocAttr<ECSL_DP::OutCommMapping, ECSL_DP::CPort, Pred>(impl, meta_srcOutCommMapping, meta_srcOutCommMapping_rev); }

		Udm::ParentAttr<Udm::Object> parent() const { return Udm::ParentAttr<Udm::Object>(impl, Udm::NULLPARENTROLE); }
	};

	class  IChan :  public Channel, public CommDst {
	 public:
		static Uml::Class meta;

		IChan() { }
		IChan(Udm::ObjectImpl *impl) : Channel(impl),CommDst(impl), MgaObject(impl) { }
		IChan(const IChan &master) : Channel(master),CommDst(master), MgaObject(master) { }
		static IChan Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static IChan Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		IChan CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		IChan CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<IChan> Instances() { return Udm::InstantiatedAttr<ECSL_DP::IChan>(impl);}
		template <class Pred> Udm::InstantiatedAttr<IChan, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::IChan, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::IChan> Derived() { return Udm::DerivedAttr<ECSL_DP::IChan>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::IChan, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::IChan, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::IChan> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::IChan>(impl);}

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  OChan :  public Channel, public CommDst {
	 public:
		static Uml::Class meta;

		OChan() { }
		OChan(Udm::ObjectImpl *impl) : Channel(impl),CommDst(impl), MgaObject(impl) { }
		OChan(const OChan &master) : Channel(master),CommDst(master), MgaObject(master) { }
		static OChan Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static OChan Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		OChan CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		OChan CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<OChan> Instances() { return Udm::InstantiatedAttr<ECSL_DP::OChan>(impl);}
		template <class Pred> Udm::InstantiatedAttr<OChan, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::OChan, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::OChan> Derived() { return Udm::DerivedAttr<ECSL_DP::OChan>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::OChan, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::OChan, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::OChan> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::OChan>(impl);}

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  BusMessage :  public CommDst {
	 public:
		static Uml::Class meta;

		BusMessage() { }
		BusMessage(Udm::ObjectImpl *impl) : CommDst(impl), MgaObject(impl) { }
		BusMessage(const BusMessage &master) : CommDst(master), MgaObject(master) { }
		static BusMessage Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static BusMessage Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		BusMessage CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		BusMessage CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<BusMessage> Instances() { return Udm::InstantiatedAttr<ECSL_DP::BusMessage>(impl);}
		template <class Pred> Udm::InstantiatedAttr<BusMessage, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::BusMessage, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::BusMessage> Derived() { return Udm::DerivedAttr<ECSL_DP::BusMessage>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::BusMessage, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::BusMessage, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::BusMessage> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::BusMessage>(impl);}

		static Uml::Attribute meta_CycleTime;
		Udm::IntegerAttr CycleTime() const { return Udm::IntegerAttr(impl, meta_CycleTime); }

		static Uml::Attribute meta_CName;
		Udm::StringAttr CName() const { return Udm::StringAttr(impl, meta_CName); }

		static Uml::Attribute meta_SendType;
		Udm::StringAttr SendType() const { return Udm::StringAttr(impl, meta_SendType); }

		static Uml::Attribute meta_Size;
		Udm::StringAttr Size() const { return Udm::StringAttr(impl, meta_Size); }

		static Uml::Attribute meta_ID;
		Udm::StringAttr ID() const { return Udm::StringAttr(impl, meta_ID); }

		static Uml::AssociationRole meta_BusChan;
		// Access method for non-navigable association BusChan omitted 

		static Uml::AssociationRole meta_referedbyBusMessageRef;
		Udm::AssocAttr<ECSL_DP::BusMessageRef> referedbyBusMessageRef() const { return Udm::AssocAttr<ECSL_DP::BusMessageRef>(impl, meta_referedbyBusMessageRef); }
		template <class Pred> Udm::AssocAttr<ECSL_DP::BusMessageRef, Pred > referedbyBusMessageRef_sorted(const Pred &) const { return Udm::AssocAttr<ECSL_DP::BusMessageRef, Pred>(impl, meta_referedbyBusMessageRef); }

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  BusMessageRef :  public CommDst {
	 public:
		static Uml::Class meta;

		BusMessageRef() { }
		BusMessageRef(Udm::ObjectImpl *impl) : CommDst(impl), MgaObject(impl) { }
		BusMessageRef(const BusMessageRef &master) : CommDst(master), MgaObject(master) { }
		static BusMessageRef Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static BusMessageRef Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		BusMessageRef CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		BusMessageRef CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<BusMessageRef> Instances() { return Udm::InstantiatedAttr<ECSL_DP::BusMessageRef>(impl);}
		template <class Pred> Udm::InstantiatedAttr<BusMessageRef, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::BusMessageRef, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::BusMessageRef> Derived() { return Udm::DerivedAttr<ECSL_DP::BusMessageRef>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::BusMessageRef, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::BusMessageRef, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::BusMessageRef> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::BusMessageRef>(impl);}

		static Uml::AssociationRole meta_ref;
		Udm::PointerAttr<ECSL_DP::BusMessage> ref() const { return Udm::PointerAttr<ECSL_DP::BusMessage>(impl, meta_ref); }

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  CommMapping :  public MgaObject {
	 public:
		static Uml::Class meta;

		CommMapping() { }
		CommMapping(Udm::ObjectImpl *impl) : MgaObject(impl) { }
		CommMapping(const CommMapping &master) : MgaObject(master) { }
		static CommMapping Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static CommMapping Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		CommMapping CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		CommMapping CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<CommMapping> Instances() { return Udm::InstantiatedAttr<ECSL_DP::CommMapping>(impl);}
		template <class Pred> Udm::InstantiatedAttr<CommMapping, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::CommMapping, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::CommMapping> Derived() { return Udm::DerivedAttr<ECSL_DP::CommMapping>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::CommMapping, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::CommMapping, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::CommMapping> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::CommMapping>(impl);}

		static Uml::Attribute meta_StartBit;
		Udm::StringAttr StartBit() const { return Udm::StringAttr(impl, meta_StartBit); }

		static Uml::Attribute meta_NumBits;
		Udm::StringAttr NumBits() const { return Udm::StringAttr(impl, meta_NumBits); }

		static Uml::CompositionParentRole meta_ECU_parent;
		Udm::ParentAttr<ECSL_DP::ECU> ECU_parent() const { return Udm::ParentAttr<ECSL_DP::ECU>(impl, meta_ECU_parent); }

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
	};

	class  OutCommMapping :  public CommMapping {
	 public:
		static Uml::Class meta;

		OutCommMapping() { }
		OutCommMapping(Udm::ObjectImpl *impl) : CommMapping(impl) { }
		OutCommMapping(const OutCommMapping &master) : CommMapping(master) { }
		static OutCommMapping Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static OutCommMapping Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		OutCommMapping CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		OutCommMapping CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<OutCommMapping> Instances() { return Udm::InstantiatedAttr<ECSL_DP::OutCommMapping>(impl);}
		template <class Pred> Udm::InstantiatedAttr<OutCommMapping, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::OutCommMapping, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::OutCommMapping> Derived() { return Udm::DerivedAttr<ECSL_DP::OutCommMapping>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::OutCommMapping, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::OutCommMapping, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::OutCommMapping> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::OutCommMapping>(impl);}

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_srcOutCommMapping_end_;
		Udm::AssocEndAttr<ECSL_DP::CPort> srcOutCommMapping_end() const { return Udm::AssocEndAttr<ECSL_DP::CPort>(impl, meta_srcOutCommMapping_end_); }

		static Uml::AssociationRole meta_dstOutCommMapping_end_;
		Udm::AssocEndAttr<ECSL_DP::CommDst> dstOutCommMapping_end() const { return Udm::AssocEndAttr<ECSL_DP::CommDst>(impl, meta_dstOutCommMapping_end_); }

	};

	class  InCommMapping :  public CommMapping {
	 public:
		static Uml::Class meta;

		InCommMapping() { }
		InCommMapping(Udm::ObjectImpl *impl) : CommMapping(impl) { }
		InCommMapping(const InCommMapping &master) : CommMapping(master) { }
		static InCommMapping Cast(const Udm::Object &a) { return __Cast(a, meta); }

		static InCommMapping Create(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role); }

		InCommMapping CreateInstance(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl); }

		InCommMapping CreateDerived(const Udm::Object &parent, const Uml::CompositionChildRole &role = Udm::NULLCHILDROLE) { return __Create(meta, parent, role, impl, true); }

		Udm::InstantiatedAttr<InCommMapping> Instances() { return Udm::InstantiatedAttr<ECSL_DP::InCommMapping>(impl);}
		template <class Pred> Udm::InstantiatedAttr<InCommMapping, Pred> Instances_sorted(const Pred &) { return Udm::InstantiatedAttr<ECSL_DP::InCommMapping, Pred>(impl);}

		Udm::DerivedAttr<ECSL_DP::InCommMapping> Derived() { return Udm::DerivedAttr<ECSL_DP::InCommMapping>(impl);}
		template <class Pred> Udm::DerivedAttr<ECSL_DP::InCommMapping, Pred> Derived_sorted(const Pred &) { return Udm::DerivedAttr<ECSL_DP::InCommMapping, Pred>(impl);}

		Udm::ArchetypeAttr<ECSL_DP::InCommMapping> Archetype() { return Udm::ArchetypeAttr<ECSL_DP::InCommMapping>(impl);}

		Udm::ParentAttr<ECSL_DP::MgaObject> parent() const { return Udm::ParentAttr<ECSL_DP::MgaObject>(impl, Udm::NULLPARENTROLE); }
		static Uml::AssociationRole meta_dstInCommMapping_end_;
		Udm::AssocEndAttr<ECSL_DP::CPort> dstInCommMapping_end() const { return Udm::AssocEndAttr<ECSL_DP::CPort>(impl, meta_dstInCommMapping_end_); }

		static Uml::AssociationRole meta_srcInCommMapping_end_;
		Udm::AssocEndAttr<ECSL_DP::CommDst> srcInCommMapping_end() const { return Udm::AssocEndAttr<ECSL_DP::CommDst>(impl, meta_srcInCommMapping_end_); }

	};

}

#endif //MOBIES_ECSL_DP_H
