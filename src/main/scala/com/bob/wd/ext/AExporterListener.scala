package com.bob.wd.ext

import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

import com.alibaba.dubbo.common.extension.Activate
import com.alibaba.dubbo.common.json.{GenericJSONConverter, JSON}
import com.alibaba.dubbo.rpc.Exporter
import com.alibaba.dubbo.rpc.listener.ExporterListenerAdapter
import org.objenesis.ObjenesisHelper
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by wangxiang on 18/1/9.
  */
@Activate
class AExporterListener extends ExporterListenerAdapter {

  private val logger: Logger = LoggerFactory.getLogger(classOf[AExporterListener])

  private val map: ConcurrentHashMap[String, Exporter[_]] = new ConcurrentHashMap

  private val gjc: GenericJSONConverter = new GenericJSONConverter

  override def exported(exporter: Exporter[_]): Unit = {

    val invoke = exporter.getInvoker
    val clasz = invoke.getInterface
    if (!map.containsKey(clasz.toString)) {
      map.put(clasz.toString, exporter)
      logger.info(s"${Console.RED} ${clasz.toString} ${Console.RESET}")
      clasz.getDeclaredMethods.foreach(m => {
        if (Modifier.isPublic(m.getModifiers)) {
          val rc = m.getReturnType
          logger.info(s"method: ${m.getName} will return type: ${rc}")
          val sb = new StringBuilder
          if (!rc.getName.contains("java.lang")) {
            sb.append("the return type has below properties: \n")
            rc.getDeclaredFields.foreach(f => sb.append(s"${f.getClass} -- ${f.getName}\n"))
            logger.info(s"${Console.RED} ${sb.substring(0, sb.length - 1)} ${Console.RESET}")
            try {
              val l = JSON.json(ObjenesisHelper.newInstance(rc))
              logger.info(s"the return type example json is: ${l}")
            } catch {
              case e: Throwable => logger.error(s"add example has error ${e.getMessage}", e)
            }
          }

          sb.setLength(0)
          if (m.getParameterTypes.size > 0) {
            sb.append(s"method: ${m.getName} need below arguments: \n")
            m.getParameterTypes.foreach(x => sb.append(s"${x}"))
            logger.info(sb.toString())
          } else {
            logger.info(s"method: ${m.getName} is no arguments")
          }
        }
      })
    }
  }
}