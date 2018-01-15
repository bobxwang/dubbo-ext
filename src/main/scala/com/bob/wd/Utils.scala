package com.bob.wd

import javassist.bytecode.LocalVariableAttribute

import org.objenesis.ObjenesisHelper

/**
  * Created by wangxiang on 17/12/13.
  */
object Utils {

  import java.util.Map
  import javassist._

  import collection.JavaConverters._
  import scala.collection.mutable

  private val pool: ClassPool = ClassPool.getDefault

  /**
    * 获得方法参数名称
    *
    * @param clasz
    * @param method
    * @return
    */
  def getMethodParamNames(clasz: Class[_], method: String): List[String] = {
    val cc = pool.get(clasz.getName)
    val cm = cc.getDeclaredMethod(method)
    val mi = cm.getMethodInfo
    val ca = mi.getCodeAttribute()
    val attr = ca.getAttribute(LocalVariableAttribute.tag).asInstanceOf[LocalVariableAttribute]
    if (attr == null) {
      List()
    } else {
      val pos = if (Modifier.isStatic(cm.getModifiers)) 0 else 1
      (0 to cm.getParameterTypes.length - 1).map(x => attr.variableName(x + pos)).toList
    }
  }

  /**
    * 将Map转成Class, Map的Key即为Class的属性, Key对应的Value不可为空, 因为需要获得属性类型
    *
    * @param claszName
    * @param map
    * @param isSetValue
    * @return
    */
  def make(claszName: String, map: java.util.Map[String, Object], isSetValue: Boolean = false): Any = {

    val ms = map.entrySet().asScala

    var clasz: CtClass = pool.getOrNull(claszName)
    if (clasz == null) {
      clasz = pool.makeClass(claszName)
      generateClass(clasz, ms)
      clasz.toClass()
    } else {
      //      clasz.defrost()
      //      generateClass(clasz, ms)
    }

    val o = ObjenesisHelper.newInstance(Class.forName(claszName))
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