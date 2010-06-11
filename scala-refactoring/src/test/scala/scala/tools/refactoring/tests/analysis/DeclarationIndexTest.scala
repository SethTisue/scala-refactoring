/*
 * Copyright 2005-2010 LAMP/EPFL
 */
// $Id$

package scala.tools.refactoring.tests.analysis

import scala.tools.refactoring.tests.util.TestHelper
import org.junit.{Test, Before}
import junit.framework.TestCase
import org.junit.Assert._
import scala.tools.refactoring.common.Selections
import scala.tools.refactoring.util.CompilerProvider
import scala.tools.refactoring.analysis._
import scala.tools.nsc.ast.Trees
import scala.tools.nsc.util.{SourceFile, BatchSourceFile, RangePosition}

@Test
class DeclarationIndexTest extends TestHelper with IndexImplementations with TreeAnalysis {

  import global._
  
  var index: IndexLookup = null
  
  def withIndex(src: String)(body: (IndexLookup, Tree) => Unit ) {
    val tree = treeFrom(src)
    index = GlobalIndex(List(CompilationUnitIndex(tree)))
    body(index, tree)
  }
  
  def assertDeclarationOfSelection(expected: String, src: String) = withIndex(src) { (index, tree) =>
  
    val declarations = findMarkedNodes(src, tree).get.selectedTopLevelTrees.head match {
      case t: RefTree => 
        index.declaration(t.symbol).head
      case t => throw new Exception("found: "+ t)
    }
    assertEquals(expected, declarations.toString)
  }  
  
  def assertReferencesOfSelection(expected: String, src: String) = withIndex(src) { (index, tree) =>
  
    val references = findMarkedNodes(src, tree).get.selectedTopLevelTrees.head match {
      case t: DefTree => 
        index.references(t.symbol).toList filter (_.pos.isRange) map ( ref => ref.toString +" ("+ ref.pos.start +", "+ ref.pos.end +")" )
      case t => throw new Exception("found: "+ t)
    }
    assertEquals(expected, references mkString ", ")
  }
  
  @Test
  def findValReference() = {
    assertDeclarationOfSelection("private[this] val x: Int = 1", """
      object A {
        private[this] val x = 1
        val y = /*(*/  x  /*)*/
      }
      """)
  }  
  
  @Test
  def findValReferenceFromMethod() = {
    assertDeclarationOfSelection("private[this] val x: Int = 1", """
      object A {
        private[this] val x = 1
        def go {
          val y = /*(*/  x  /*)*/
        }
      }
      """)
  }  
  
  @Test
  def findShadowed() = {
    assertDeclarationOfSelection("""val x: java.lang.String = "a"""", """
      object A {
        private[this] val x = 1
        def go  = {
          val x = "a"
          val y = /*(*/  x  /*)*/
          y
        }
      }
      """)
  }

  @Test
  def findShadowedWithThis() = {
    assertDeclarationOfSelection("""private[this] val x: Int = 1""", """
      object A {
        private[this] val x = 1
        def go = {
          val x = "a"
         /*(*/  this.x  /*)*/
        }
      }
      """)
  }  
    
  @Test
  def findMethod() = {
    assertDeclarationOfSelection("""def x(): Int = 5""", """
      object A {
        def x() = 5
        def go  = {
          val y = /*(*/  x  /*)*/ ()
          y
        }
      }
      """)
  }
  
  @Test
  def findMethodFromOtherClass() = {
    assertDeclarationOfSelection("""<stable> <accessor> def x: Int = N.this.x""", """
      class N {
        val x = 5
      }
      object M {
        def go  = {
          val a = new N
          val y = /*(*/  a.x  /*)*/
          y
        }
      }
      """)
  }  
  
  @Test
  def findReferencesToLocal() = {
    assertReferencesOfSelection("a (86, 87), a (98, 99)", """
      class H {
        def go  = {
 /*(*/    val a = 5      /*)*/
          val y = a
          a
        }
      }
      """)
  }
  
  @Test
  def findReferencesToMethod() = {
    assertReferencesOfSelection("""G.this.go (96, 98)""", """
      class G {
 /*(*/       
        def go() = {
          5
        } /*)*/
        val g = go()
      } 

      """)
  }  
  
  @Test
  def findReferencesToClass() = {
    assertReferencesOfSelection("""Z (71, 72), Z (91, 92), Z (115, 116), Z (119, 120), Z (127, 128)""", """
      package xyz
    
 /*(*/  class Z   /*)*/

      class B extends Z

      class C(a: Z) {
        def get(a: Z): Z = new Z
      }
      """)
  }
}
