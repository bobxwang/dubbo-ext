package com.bob.wd.consumer

object Objectex {

  import java.lang.reflect.Method
  import java.util
  import javassist.bytecode.LocalVariableAttribute
  import javassist.{ClassPool, CtClass, Modifier}

  import com.alibaba.dubbo.common.compiler.support.ClassUtils
  import com.alibaba.dubbo.common.utils.ReflectUtils
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.alibaba.dubbo.common.utils.CompatibleTypeUtils

  import collection.JavaConverters._

  implicit def funString(s: String) = new RichString(s)

  implicit def funMethod(s: Method) = new RichMethod(s)

  implicit def funUniqueServiceDef(u: UniqueServiceDef) = new RichUniqueServiceDef(u)

  def convertJson2Map(uniqueServiceDef: UniqueServiceDef, jsonBody: String): util.Map[String, AnyRef] = {

    import java.lang.reflect.Array

    val mapper = new ObjectMapper
    val m = mapper.readValue(jsonBody, classOf[util.Map[String, AnyRef]])

    val map: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]

    if (jsonBody.isNullOrEmpty) return map

    if (!uniqueServiceDef.getInputDto.isNullOrEmpty) {
      // DTO类型
      val ps = uniqueServiceDef.getDtoProperty.split(",")
      val ts = uniqueServiceDef.getParamType.split(",")
      val list = ps zip ts
      list.foreach(x => map.put(x._1, m.get(x._1)))

    } else if (!uniqueServiceDef.getInputEnum.isNullOrEmpty) {
      // 多参数简单类型
      val ps = uniqueServiceDef.getInputEnum.split(",")
      val ts = uniqueServiceDef.getParamType.split(",")
      val list = ps.zip(ts)
      list.foreach(l => {
        val obj = m.get(l._1)
        if (classOf[util.List[_]].isAssignableFrom(obj.getClass)) {

          val leftpos = l._2.indexOf("[")
          val rightpos = l._2.indexOf("]")

          var cc: Class[_] = null
          // 如果是泛型类则是记录具体的泛型类型,也就是列表中的每个具体值
          var isGenericClass = false
          if (rightpos != leftpos + 1) { // 说明是泛型类列表
            val fc = l._2.substring(leftpos + 1, rightpos)
            cc = ClassUtils.forName(fc)
            isGenericClass = true
          }
          else cc = ClassUtils.forName(l._2.replace("[]", ""))

          val jsonArray = obj.asInstanceOf[util.List[_]].asScala
          val v = Array.newInstance(cc, jsonArray.size)
          var i = 0
          jsonArray.foreach(o => {
            Array.set(v, i, CompatibleTypeUtils.compatibleTypeConvert(o, cc))
            i = i + 1
          })

          if (!isGenericClass) {
            map.put(l._1, v)
          } else {
            val contentType = l._2.substring(0, leftpos)
            if (contentType.contentEquals("java.util.Set")) {
              val hs = new util.HashSet[AnyRef]
              i = 0
              jsonArray.foreach(_ => {
                hs.add(Array.get(v, i))
                i = i + 1
              })
              map.put(l._1, hs)
            } else if (contentType.contentEquals("java.util.List")) {
              map.put(l._1, util.Arrays.asList(v))
            }
          }
        } else map.put(l._1, obj)
      })
    } else map.putAll(m)

    map
  }

  private def jmethod: Unit = {
    val s =
      """private Map<String, Object> convertJson2Map(UniqueServiceDef uniqueServiceDef, JsonObject jsonObject) {
        |
        |        Map<String, Object> map = new HashMap<>();
        |
        |        if (!Strings.isNullOrEmpty(uniqueServiceDef.getInputDto())) {
        |            // DTO类型
        |            String[] ps = uniqueServiceDef.getDtoProperty().split(",");
        |            String[] ts = uniqueServiceDef.getParamType().split(",");
        |            List<Tuple2<String, String>> list = Objectex.s2jConvenient(ps, ts);
        |            for (Tuple2<String, String> l : list) {
        |                map.put(l._1, jsonObject.getValue(l._1));
        |            }
        |        } else if (!Strings.isNullOrEmpty(uniqueServiceDef.getInputEnum())) {
        |            // 多参数简单类型
        |            String[] ps = uniqueServiceDef.getInputEnum().split(",");
        |            String[] ts = uniqueServiceDef.getParamType().split(",");
        |            List<Tuple2<String, String>> list = Objectex.s2jConvenient(ps, ts);
        |            for (Tuple2<String, String> l : list) {
        |                Object obj = jsonObject.getValue(l._1);
        |                if (obj instanceof JsonArray) {
        |                    JsonArray jsonArray = (JsonArray) obj;
        |                    Iterator<Object> iterator = jsonArray.iterator();
        |
        |                    int leftpos = l._2.indexOf("[");
        |                    int rightpos = l._2.indexOf("]");
        |
        |                    Class<?> cc; // 如果是泛型类则是记录具体的泛型类型,也就是列表中的每个具体值
        |                    Boolean isGenericClass = false;
        |                    if (rightpos != leftpos + 1) {
        |                        // 说明是泛型类列表
        |                        String fc = l._2.substring(leftpos + 1, rightpos);
        |                        cc = ClassUtils.forName(fc);
        |                        isGenericClass = true;
        |                    } else {
        |                        cc = ClassUtils.forName(l._2.replace("[]", ""));
        |                    }
        |
        |                    Object v = Array.newInstance(cc, jsonArray.size());
        |                    int i = 0;
        |                    while (iterator.hasNext()) {
        |                        Object o = iterator.next();
        |                        Array.set(v, i, CompatibleTypeUtils.compatibleTypeConvert(o, cc));
        |                        i++;
        |                    }
        |
        |                    if (!isGenericClass) {
        |                        map.put(l._1, v);
        |                    } else {
        |                        String contentType = l._2.substring(0, leftpos);
        |                        if (contentType.contentEquals("java.util.Set")) {
        |                            Set hs = new HashSet<>();
        |                            for (int j = 0; j < jsonArray.size(); j++) {
        |                                hs.add(Array.get(v, j));
        |                            }
        |
        |                            Set shs = new HashSet<>();
        |                            for (int j = 0; j < jsonArray.size(); j++) {
        |                                shs.add(jsonArray.getValue(j));
        |                            }
        |
        |                            map.put(l._1, shs);
        |                        } else if (contentType.contentEquals("java.util.List")) {
        |
        |                            map.put(l._1, Arrays.asList(v));
        |                        }
        |                    }
        |                } else {
        |                    map.put(l._1, obj);
        |                }
        |            }
        |        } else {
        |            map = jsonObject.getMap();
        |        }
        |
        |        return map;
        |    }""".stripMargin
    println(s)
  }

  class RichString(val s: String) {

    def isNullOrEmpty = s == null || s.trim.length == 0

    /**
      * 在调用dubbo时入参类型
      *
      * @return
      */
    def invokeOfClassTyee: String = {
      val lp = s.indexOf("[")
      if (lp == -1) {
        val c = s.ofClass
        val name = c.getName
        val packageName = if (name.lastIndexOf(".") == -1) name else name.substring(0, name.lastIndexOf("."))
        if (c.isArray) s"${packageName.replace("[L", "")}.${c.getSimpleName}" else name
      } else {
        // 找到了
        val rp = s.indexOf("[]")
        if (rp == -1) {
          // 说明列表中有泛型类存在
          val rpp = s.indexOf("]")
          val fname = s.substring(lp + 1, rpp)
          val c = s.substring(0, lp).ofClass
          val name = c.getName
          val packageName = if (name.lastIndexOf(".") == -1) name else name.substring(0, name.lastIndexOf("."))
          if (c.isArray) s"${packageName.replace("[L", "")}.${c.getSimpleName}" else s"${name}"
        } else {
          // 说明是数组
          val c = s.ofClass
          val name = c.getName
          val packageName = if (name.lastIndexOf(".") == -1) name else name.substring(0, name.lastIndexOf("."))
          if (c.isArray) s"${packageName.replace("[L", "")}.${c.getSimpleName}" else name
        }
      }
    }

    /**
      * 在toexample中使用
      *
      * @return
      */
    def ofClassTyee: String = {
      val lp = s.indexOf("[")
      if (lp == -1) {
        val c = s.ofClass
        val name = c.getName
        val packageName = if (name.lastIndexOf(".") == -1) name else name.substring(0, name.lastIndexOf("."))
        if (c.isArray) s"${packageName.replace("[L", "")}.${c.getSimpleName}" else name
      } else {
        // 找到了
        val rp = s.indexOf("[]")
        if (rp == -1) {
          // 说明列表中有泛型类存在
          val rpp = s.indexOf("]")
          val fname = s.substring(lp + 1, rpp)
          val c = s.substring(0, lp).ofClass
          val name = c.getName
          val packageName = if (name.lastIndexOf(".") == -1) name else name.substring(0, name.lastIndexOf("."))
          if (c.isArray) s"${packageName.replace("[L", "")}.${c.getSimpleName}[${fname}]" else s"${name}[${fname}]"
        } else {
          // 说明是数组
          val c = s.ofClass
          val name = c.getName
          val packageName = if (name.lastIndexOf(".") == -1) name else name.substring(0, name.lastIndexOf("."))
          if (c.isArray) s"${packageName.replace("[L", "")}.${c.getSimpleName}" else name
        }
      }
    }

    def ofClass: Class[_] = try {
      ReflectUtils.forName(s)
    } catch {
      case _: IllegalStateException => {
        try {
          ClassUtils.forName(s)
        } catch {
          case e: IllegalStateException => throw e
        }
      }
    }
  }

  class RichMethod(method: Method) {

    private val pool = ClassPool.getDefault

    def getAllParamaterName(): Array[String] = {
      val clazz = method.getDeclaringClass
      val clz = pool.get(clazz.getName)
      val params = new Array[CtClass](method.getParameterTypes.length)
      (1 to method.getParameterTypes.length).foreach(x => {
        params(x) = pool.getCtClass(method.getParameterTypes()(x).getName)
      })
      val cm = clz.getDeclaredMethod(method.getName, params)
      val methodInfo = cm.getMethodInfo
      val codeAttribute = methodInfo.getCodeAttribute
      if (codeAttribute == null) return null
      else {
        val attr = codeAttribute.getAttribute(LocalVariableAttribute.tag).asInstanceOf[LocalVariableAttribute]
        val pos = if (Modifier.isStatic(cm.getModifiers)) 0 else 1
        val paramNames = new Array[String](cm.getParameterTypes.length)
        (1 to paramNames.length).foreach(x => {
          paramNames(x) = attr.variableName(x + pos)
        })

        return paramNames
      }
    }
  }

  class RichUniqueServiceDef(u: UniqueServiceDef) {

    def check(): util.HashMap[String, AnyRef] = {
      val map = new util.HashMap[String, AnyRef]
      val pcheck = this.pcheck
      if (!pcheck._1) {
        map.put("error", pcheck._2)
        return map
      }

      if (!u.getInputDto.isNullOrEmpty) { // 入参是一个DTO
        if (u.getDtoProperty.isNullOrEmpty || u.getParamType.isNullOrEmpty) {
          map.put("error", "inputDto非空但是dtoProperty/paramType中有个为空")
          return map
        }
        val properties = u.getDtoProperty.split(",")
        val paramtypes = u.getParamType.split(",")
        if (paramtypes.length != paramtypes.length) {
          map.put("error", "参数名称跟参数类型长度不一致")
          return map
        }
        val pp = properties.zip(paramtypes)
        pp.foreach(x => {
          map.put(x._1, x._2.ofClassTyee)
        })
      } else if (!u.getInputEnum.isNullOrEmpty) { // 多个入参
        if (u.getParamType.isNullOrEmpty) {
          map.put("error", "inputEnum非空但是paramType为空")
          return map
        }

        val paramName = u.getInputEnum.split(",")
        val paramType = u.getParamType.split(",")
        if (paramName.length != paramType.length) {
          map.put("error", "参数名称跟参数类型长度不一致")
          return map
        }
        val pTypeWithName = paramName.zip(paramType)
        pTypeWithName.foreach(x => {
          map.put(x._1, x._2.ofClassTyee)
        })
      } else {
        // 可以理解成无参或者参数就是一个map
      }

      map.put("interfaceName", u.getInterfaceName)
      map.put("method", u.getMethod)
      if (!u.getGroup.isNullOrEmpty) map.put("group", u.getGroup)
      if (!u.getVersion.isNullOrEmpty) map.put("version", u.getVersion)

      map
    }

    private def pcheck: (Boolean, String) = {
      val sb = new StringBuilder
      if (u.getInterfaceName.isNullOrEmpty) sb.append(" interfaceName is not allowd null or empty\n")
      if (u.getMethod.isNullOrEmpty) sb.append(" method is not allowd null or empty\n")
      if (sb.length > 0) (false, sb.toString()) else (true, "")
    }
  }

}