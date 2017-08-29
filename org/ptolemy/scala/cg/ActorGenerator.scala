/**
 *
 * Copyright (c) 2013-2017 The Regents of the University of California.
 * All rights reserved.
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2
 * COPYRIGHTENDKEY
 *
 */

package org.ptolemy.scala.cg

import java.io.File
import java.io.FileWriter
import org.ptolemy.scala.actor.gui.CompositeActorApplication

/**
 * @author Moez Ben Hajhmida
 *
 */
object ActorGenerator extends App{
  
  def createFieldDeclaration(field: java.lang.reflect.Field, objectName: String, writer: FileWriter): Unit = {
    if (field.getModifiers == 1) //public field
      writer.write("  var " + field.getName + " = " + objectName + "." + field.getName + "\n")
  }

  def codeGenerate(className: String, path: String): Unit = {

val copyright:String = "/**\nCopyright (c) 2013-2017 The Regents of the University of California.\nAll rights reserved.\nPermission is hereby granted, without written agreement and without\nlicense or royalty fees, to use, copy, modify, and distribute this\nsoftware and its documentation for any purpose, provided that the above\ncopyright notice and the following two paragraphs appear in all copies\nof this software.\n\nIN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY\nFOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES\nARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF\nTHE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF\nSUCH DAMAGE.\n\nTHE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,\nINCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF\nMERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE\nPROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF\nCALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,\nENHANCEMENTS, OR MODIFICATIONS.\n\nPT_COPYRIGHT_VERSION_2\nCOPYRIGHTENDKEY\n*/"

    try {
      // load the class named className
      val cls = Class.forName(className)
      // Name of the instance 
      var clsName = className.split('.').last
      var firstChar = clsName.charAt(0)
      val objectName = clsName.replace(firstChar, firstChar.toLower)
      // get the defined fields
      val fields = cls.getFields
      // get the package name
      val pack = cls.getPackage.getName;

      val file = new File(path + className.replaceFirst("ptolemy", "org.ptolemy.scala").replaceAll("[.]", "/") + ".scala")
      file.getParentFile().mkdirs()
      val writer = new FileWriter(file)

      //copyright comment				
      writer.write(copyright + "\n")

      //package name
      writer.write("package " + pack.replaceFirst("ptolemy", "org.ptolemy.scala") + "\n")
      //imports
      writer.write("import ptolemy.kernel.CompositeEntity \n")
      writer.write("import org.ptolemy.scala.implicits._\n")
      writer.write("import org.ptolemy.scala.actor.TypedAtomicActor\n")
      writer.write("import org.ptolemy.scala.actor.ApplicationActor\n")
      writer.write("import org.ptolemy.scala.actor.ComponentEntity \n \n")
      //class comment
      val classComment = "/** Construct an actor in the given container with the given name.\n*  The container argument is an implicit parameter.\n*  This class is a wrapper of the " + className + "\n*  \n*  @param container ( implicit) Container of the director.\n*  @param name Name of this director. \n*/"
      writer.write(classComment + "\n")

      // class declaration
      writer.write("case class " + clsName + "(name: String)(implicit container: CompositeEntity) extends TypedAtomicActor{\n")

      //reference to the Java actor comment
      val fieldComment = "/**\n*  This field is a reference to the Java actor.\n*   It makes possible to access all attributes and methods \n*   provided by PtolemyII in Java language.\n*/"
      writer.write(fieldComment + "\n")
      writer.write("  var " + objectName + " = new " + className + " (container, name) \n")
      // fields comment
      val fieldsComment = "/**\n*  These fields are references to the public fields of the\n	*    " + className + ". \n	*/"
      writer.write(fieldsComment + "\n")
      // create a field for each public field of the class
      fields.foreach(f => createFieldDeclaration(f, objectName, writer))

      // getActor() comment
      val getActorComment = "/** Returns a reference to the wrapped actor.\n	* This method returns the Ptolemy actor (java) wrapped  by the scala actor.\n	* It's useful when the developer wants to access the java methods.\n	*	\n	*  @return Reference to the wrapped actor.	\n	*/  "
      writer.write(getActorComment + "\n")

      // getActor() method
      writer.write("\n \n  def getActor():" + className + " = " + objectName + "\n\n")

      // set() comment
      val setComment = "/** Invokes the method 'setExpression(String expression)' of the Parameter named 'parameterName'\n* The field named 'parameterName' is of type 'ptolemy.data.expr.Parameter', and is a field of the object 'obj'.'\n* This function performs a java reflection to execute the java code:\n* objectName.parameterName.setExpression(expression) .\n*\n*  @param parameterName The name of the field to be set.\n*  @param expression The expression to be set to the field\n*  @return Reference to the current object.\n*/ "
      writer.write(setComment + "\n")

      // set() method
      writer.write("  def set(parameterName: String, expressionString: String):" + clsName + " = {   \n")
      writer.write("    setExpression (" + objectName + ", parameterName, expressionString)\n")
      writer.write("    this \n  }\n")

      // overloaded constructor1 comment
      val constructorComment1 = "/**\n*  Overloading constructor \n*  Permits call a Class constructor like in PtolemyII Java classes\n*  val actor  = new Actor(container , \"name\")\n* @param container The container.\n*  @param name The name of this actor\n*/  "
      writer.write(constructorComment1 + "\n")

      // overloaded constructor2
      writer.write("  def this (container: ComponentEntity, name: String) {  this (name)(container.getActor().asInstanceOf[CompositeEntity])} \n ")

      // overloaded constructor1 comment
      val constructorComment2 = "/**\n*  Overloading constructor \n*  Permits call a Class constructor like in PtolemyII Java classes\n*  val actor  = new Actor(container , \"name\")\n* @param container The container. Here the container is of type  ApplicationActor,\n which is used to write an application to run.\n*  @param name The name of this actor\n*/  "
      writer.write(constructorComment2 + "\n")

      // overloaded constructor2
      writer.write("   def this (container : ApplicationActor, name: String) {  this (name)(container.getActor().asInstanceOf[CompositeEntity])}  \n ")

      //end of class
      writer.write("}\n")
      writer.close()
      println("success: file " + path + className.replaceFirst("ptolemy", "org/ptolemy/scala").replace('.', '/') + ".scala successfuly created")
    } catch { // how should we manage? DSL language checker?
      case noMethod: NoSuchMethodException           => println(noMethod.getMessage.toString())
      case nullpointer: NullPointerException         => println(nullpointer.getMessage.toString())
      case noClass: java.lang.ClassNotFoundException => println("error: file " +path+className.replace('.', '/') + ".java not found")
    }

  }
  
  override def  main(args: Array[String]): Unit = {
    try {
    val ptolemyHome: String =  sys.env("PTII")    
    args.foreach(cls => codeGenerate(cls, ptolemyHome+"/"))
    }catch {
      case noElement: java.util.NoSuchElementException => println("error: You must set the PTII environment variable before generating code"); println(noElement.getMessage.toString())
    }
    
        

  }

}