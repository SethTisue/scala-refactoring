package scala.tools.refactor.tests

import utils._

import scala.tools.refactor.printer._

import junit.framework._

class PartitionerTest extends TestCase with TestHelper {
    
  def testSingleObject = "object A" partitionsInto "object |A"
  
  def testSingleClass = "class A" partitionsInto "class |A"
  
  def testSingleAbstractClass = "abstract class A" partitionsInto "abstract| class |A"
  
  def testSingleTrait = "trait C" partitionsInto "trait| |C"
  
  def testClassWithManyModifiers = "final /*comment*/ class X" partitionsInto "final| /*comment*/ class |X"
  
  def testSingleTraitWithComment = "trait /*comment*/ C" partitionsInto "trait| /*comment*/ |C"

  def testObjectWithWS =
    """
        // here comes an object:
        private object A
    """	partitionsInto
    """
        // here comes an object:
        |private| object |A|
    """

  def testPackage = "package x\n\n\n" partitionsInto "package |x|\n\n\n"

  def testPrettyPackages = "/**/ package /**/ x/**/./**/y/**/./**/z/**/" partitionsInto "/**/ |package /**/ |x|/**/./**/|y|/**/./**/|z|/**/"
  
  def testPackageAndClass = 
    """
        package x
        
        final class A
    """ partitionsInto
    """
        |package |x|
        
        |final| class |A|
    """

  def testClassExtends = "class X extends AnyRef" partitionsInto "class |X| |extends |AnyRef"
  
  def testClassExtendsWithTrait =
  """
    trait A; trait B
    class X extends AnyRef with A with B
  """ partitionsInto
  """
    |trait| |A|; |trait| |B|
    |class |X| |extends |AnyRef| with |A| with |B|
  """

  def testClassWithBody = 
  """
    class X extends AnyRef {
      
    }
  """ partitionsInto
  """
    |class |X| |extends |AnyRef| {
      
    }|
  """

  def testCaseClass = "case class X(i: Int, s: String) extends AnyRef" partitionsInto "case| class |X|(|i|: |Int|, |s|: |String|) extends |AnyRef" 
  
  def testClassParamsWithBody =
  """
    class Xyz(private val abc: String, var int: Int) {
    }
  """ partitionsInto
  """
    |class |Xyz|(|private| val |abc|: |String|, |var |int|: |Int|) {
    }|
  """

  def testTraitBody = "trait A; class Xyz extends A { object C }/*done*/" partitionsInto "trait| |A|; |class |Xyz| |extends |A| { |object |C| }|/*done*/" 
    
  def testClassParams = "class Xyz(private val abc: String, var int: Int/**/)/*done!*/" partitionsInto "class |Xyz|(|private| val |abc|: |String|, |var |int|: |Int|/**/)|/*done!*/"
  
  def testNestedPackages = "package x.y.z" partitionsInto "package |x|.|y|.|z"

  // not yet: def testEarlyDef = "trait A; class Xyz extends { type T } with A {  }/*done*/" partitionsInto "trait| |A|; |class |Xyz| |extends { |type |T| } with |A| {  }|/*done*/"

  //  
//  def testEarlyDefFromSpec5_1_8 = assert print
//  """
//    trait Greeting {
//      val name: String
//      val msg = "How are you, " +name
//    }
//    class C extends {
//      val name = "Bob"
//    } with Greeting {
//      println(msg)
//    }
//  """
}