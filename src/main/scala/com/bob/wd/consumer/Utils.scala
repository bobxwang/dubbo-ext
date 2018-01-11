package com.bob.wd.consumer

/**
  * Created by wangxiang on 17/12/13.
  */
object Utils {

  import javassist.{ClassPool, CtClass, CtField}

  import collection.JavaConverters._
  import javassist.CtNewMethod
  import java.util.Map
  import javassist.{Modifier, NotFoundException}
  import scala.collection.mutable

  private val pool: ClassPool = ClassPool.getDefault

  /**
    * 将Map转成Class, Map的Key即为Class的属性, Key对应的Value不可为空, 因为需要获得属性类型
    *
    * @param claszName
    * @param map
    * @return
    */
  def make(claszName: String, map: java.util.Map[String, Object]): Any = {

    val ms = map.entrySet().asScala
    var clasz: CtClass = pool.getOrNull(claszName)
    if (clasz == null) {
      clasz = pool.makeClass(claszName)
      generateClass(clasz, ms)
      clasz.toClass()
      clasz.getDeclaredFields.foreach(x => println(s"${Console.RED} ${x.getName} ${Console.RESET}"))
    } else {
      //      clasz.defrost()
      //      generateClass(clasz, ms)
      //      clasz.getDeclaredFields.foreach(x => println(s"${Console.BLUE} ${x.getName} ${Console.RESET}"))
    }

    val c = Class.forName(claszName)
    val o = c.newInstance()
    ms.foreach(x => {
      if (!x.getKey.contentEquals("class")) {
        this.setFieldValue(o, x.getKey, x.getValue)
      }
    })
    o
  }

  private def generateClass(clasz: CtClass, ms: mutable.Set[Map.Entry[String, Object]]) = {
    ms.foreach(x => {
      if (!x.getKey.contentEquals("class")) {
        val fieldName = x.getKey
        val fieldValue = x.getValue
        if (fieldValue != null) {
          try {
            clasz.getDeclaredField(fieldName)
            println(s"${fieldName} alerdy exists")
          } catch {
            case _: NotFoundException => {
              val fieldType = fieldValue.getClass.getName
              val ctField = new CtField(pool.get(fieldType), fieldName, clasz)
              ctField.setModifiers(Modifier.PRIVATE)
              clasz.addField(ctField)
              clasz.addMethod(CtNewMethod.getter(s"get${fieldName.substring(0, 1).toUpperCase}${fieldName.substring(1)}", ctField))
            }
          }
        } else {
        }
      }
    })
  }

  private def setFieldValue(o: Any, fieldName: String, value: Object): Object = {
    var result: Object = null
    try {
      if (value != null) {
        val fu = o.getClass.getDeclaredField(fieldName)
        try {
          fu.setAccessible(true)
          fu.set(o, value)
          result = fu.get(o)
        } catch {
          case e: IllegalAccessException =>
            println(s"${Console.RED} ${e.getMessage} ${Console.RESET}")
        }
      }
    } catch {
      case e: NoSuchFieldException =>
        println(s"${Console.RED} ${e.getMessage} ${Console.RESET}")
    }
    result
  }
}