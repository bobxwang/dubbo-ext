package com.bob.wd.ext

import java.lang.reflect.Modifier

import com.alibaba.dubbo.common.extension.Activate
import com.alibaba.dubbo.common.json.JSON
import com.alibaba.dubbo.rpc.Exporter
import com.bob.wd.Utils
import org.objenesis.ObjenesisHelper

/**
  * Created by wangxiang on 18/1/15.
  */
@Activate
class APrintExporterListener extends AExporterListener {

  import collection.JavaConverters._

  override def exported(exporter: Exporter[_], clasz: Option[Class[_]]): Unit = {

    clasz.foreach(c => {

      logger.info(s"${Console.RED} ${c.getName} ${Console.RESET}")

      c.getDeclaredMethods.foreach(m => {

        if (Modifier.isPublic(m.getModifiers)) {

          val rc = m.getReturnType
          logger.info(s"method: ${m.getName} will return type: ${rc}")
          val sb = new StringBuilder
          if (!rc.getName.contains("java.lang")) {
            try {
              val l = JSON.json(ObjenesisHelper.newInstance(rc))
              logger.info(s"the return type example json is: ${l}")
            } catch {
              case e: Throwable => logger.error(s"add example has error ${e.getMessage}", e)
            }
          }

          sb.setLength(0)
          if (m.getParameterTypes.size > 0) {
            val mpmap: scala.collection.mutable.Map[String, Object] = scala.collection.mutable.Map()
            val mpt = m.getParameterTypes
            val mpn = Utils.getMethodParamNames(c, m.getName)
            (0 to mpn.length - 1).foreach(i => {
              mpmap.put(mpn(i), ObjenesisHelper.newInstance(mpt(i)).asInstanceOf[Object])
            })
            val mpmapj = mpmap.asJava
            val oc = Utils.make(s"${c.getPackage.getName}.${m.getName}InputParam", mpmapj)

            try {
              val l = JSON.json(mpmapj)
              logger.info(s"the method param example json is: ${l}")
            } catch {
              case e: Throwable => logger.error(s"add example has error ${e.getMessage}", e)
            }
          } else {
            logger.info(s"method: ${m.getName} is no arguments")
          }
        }
      })
    })

  }
}