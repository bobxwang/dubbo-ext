package com.bob.wd.consumer

import java.util.Collections

/**
  * Created by wangxiang on 17/11/30.
  */
object RuleHolder {

  import Objectex.funString
  import Objectex.funUniqueServiceDef
  import java.util.concurrent.ConcurrentHashMap
  import com.fasterxml.jackson.databind.ObjectMapper

  private val map: java.util.Map[String, UniqueServiceDef] = new ConcurrentHashMap[String, UniqueServiceDef]()
  private val in = this.getClass.getClassLoader.getResourceAsStream("localjarrule.json")
  private val ud = new ObjectMapper().readValue(in, classOf[Array[UniqueServiceDef]])
  ud.foreach(x => add(x))

  def getMap(): java.util.Map[String, UniqueServiceDef] = Collections.unmodifiableMap(map)

  def add(uniqueServiceDef: UniqueServiceDef) = {
    val m = uniqueServiceDef.check()
    if (m.containsKey("error")) throw new IllegalArgumentException(m.get("error").toString)

    map.put(generateKey(uniqueServiceDef), uniqueServiceDef)
  }

  def generateKey(uniqueServiceDef: UniqueServiceDef) = {

    if (uniqueServiceDef.getInterfaceName.isNullOrEmpty) throw new IllegalArgumentException("interfacename is not allowd empty or null")
    if (uniqueServiceDef.getMethod.isNullOrEmpty) throw new IllegalArgumentException("methodname is not allowd empty or null")

    val v = if (uniqueServiceDef.getVersion.isNullOrEmpty) "v" else uniqueServiceDef.getVersion
    val g = if (uniqueServiceDef.getGroup.isNullOrEmpty) "g" else uniqueServiceDef.getGroup
    s"${uniqueServiceDef.getInterfaceName}-${uniqueServiceDef.getMethod}-${v}-${g}"
  }

  def geterateKey(interfaceName: String, methodName: String, group: String = "", version: String = "") = {
    val u = new UniqueServiceDef
    u.setInterfaceName(interfaceName)
    u.setMethod(methodName)
    u.setVersion(version)
    u.setGroup(group)
    generateKey(u)
  }

  def find(key: String): UniqueServiceDef = map.get(key)
}