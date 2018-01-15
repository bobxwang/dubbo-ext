package com.bob.wd.ext

import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

import com.alibaba.dubbo.rpc.Exporter
import com.alibaba.dubbo.rpc.listener.ExporterListenerAdapter
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by wangxiang on 18/1/9.
  */
trait AExporterListener extends ExporterListenerAdapter {

  import scala.util.control._

  private val map: ConcurrentHashMap[String, Exporter[_]] = new ConcurrentHashMap

  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  override def exported(exporter: Exporter[_]): Unit = {

    val invoke = exporter.getInvoker
    val clasz = invoke.getInterface
    if (!map.containsKey(clasz.getName)) {
      map.put(clasz.getName, exporter)
      exported(exporter, getImp(clasz))
    }
  }

  protected def getImp(clasz: Class[_]): Option[Class[_]] = {

    val classLoader = Thread.currentThread().getContextClassLoader
    var classOfClassLoader: Class[_] = classLoader.getClass
    while ( {
      classOfClassLoader ne classOf[ClassLoader]
    }) classOfClassLoader = classOfClassLoader.getSuperclass

    var field: Field = null
    try
      field = classOfClassLoader.getDeclaredField("classes")
    catch {
      case e: NoSuchFieldException =>
        throw new RuntimeException("无法获取到当前线程的类加载器的classes域!", e)
    }
    field.setAccessible(true)

    val loop = new Breaks
    val ll = field.get(classLoader).asInstanceOf[java.util.Vector[Class[_]]]
    val lll = new Array[AnyRef](ll.size())
    ll.copyInto(lll)

    var rs: Option[Class[_]] = None
    loop.breakable {
      lll.foreach(x => {
        val cx = x.asInstanceOf[Class[_]]
        if (!cx.isInterface) {
          if (clasz.isAssignableFrom(cx)) {
            rs = Some(cx)
            loop.break()
          }
        }
      })
    }

    rs
  }

  def exported(exporter: Exporter[_], clasz: Option[Class[_]]): Unit
}