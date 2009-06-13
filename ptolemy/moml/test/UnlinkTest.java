package ptolemy.moml.test;


import ptolemy.actor.CompositeActor;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.moml.MoMLParser;

public class UnlinkTest {
    
    static public final String moml = "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" "+
      "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">                            "+
      "<entity name=\"const3Relations\" class=\"ptolemy.actor.TypedCompositeActor\"> "+
    "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.0.beta\"> "+
    "</property> "+ 
    "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\"> "+
        "<doc>Create a constant sequence.</doc> "+
        "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\"> "+
            "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\"> "+
            "</property> "+
            "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\"> "+
            "</property> "+
        "</property>"+
        "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[420.0, 210.0]\"> "+
        "</property> "+
    "</entity> "+
    "<relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\"> "+
        "<vertex name=\"vertex1\" value=\"{300.0, 200.0}\">"+
        "</vertex>"+
    "</relation>"+
    "<relation name=\"relation2\" class=\"ptolemy.actor.TypedIORelation\"> "+
        "<vertex name=\"vertex1\" value=\"{320.0, 220.0}\">"+
        "</vertex>"+
    "</relation>"+
    "<relation name=\"relation3\" class=\"ptolemy.actor.TypedIORelation\">"+
        "<vertex name=\"vertex1\" value=\"{340.0, 240.0}\">"+
        "</vertex>"+
    "</relation>"+
    "<link port=\"Const.trigger\" relation=\"relation\"/> "+
    "<link port=\"Const.trigger\" relation=\"relation2\"/> "+
    "<link port=\"Const.trigger\" relation=\"relation3\"/> "+
"</entity>";
    
    public static void main(String[] args) {
        MoMLParser parser = new MoMLParser();
        try {
            CompositeActor model = (CompositeActor) parser.parse(moml);
            Port multiport = model.getPort("Const.trigger");
            
            for (int ii = 0; ii < 1; ii++) {
                for (int i = 0; i < 3; i++) {
                    String relationName = "relation";
                    if(i!=0) relationName += i+1;
                    
                    Relation relation = model.getRelation(relationName);
                    System.out.println("Unlink: "+i+" Link: "+relation.getName()+" at "+i);
                    multiport.unlink(i);
                    multiport.insertLink(i,relation);
                }
            }
            
            System.out.println(model.exportMoML());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

}
