package com.bob.wd.consumer

import com.bob.wd.DubboConsumerConfig
import org.slf4j.{Logger, LoggerFactory}

/**
  * 适合有jar包情况下的dubbo调用,即我们有服务方提供出来的jar包
  *
  * Created by wangxiang on 17/11/30.
  */
class DubboProxy(dubboConfig: DubboConsumerConfig) {

  import com.alibaba.dubbo.config.{ApplicationConfig, ReferenceConfig, RegistryConfig}
  import java.util.concurrent.ConcurrentHashMap

  import Objectex.funString

  private val hmReferenceConfig = new ConcurrentHashMap[String, ReferenceConfig[_]]
  private val hmService = new ConcurrentHashMap[String, Any]
  private[this] val lock = new Object()

  private val applicationConfig: ApplicationConfig = new ApplicationConfig
  applicationConfig.setName(dubboConfig.appName)

  private val registryConfig: RegistryConfig = new RegistryConfig
  registryConfig.setAddress(dubboConfig.address)
  registryConfig.setProtocol(dubboConfig.protocol)
  registryConfig.setTimeout(dubboConfig.timeout)

  private val logger: Logger = LoggerFactory.getLogger(classOf[DubboProxy])

  private def init(name: String, clazz: Class[_], version: String, group: String) = {
    val reference = new ReferenceConfig
    reference.setApplication(applicationConfig)
    reference.setRegistry(registryConfig)
    if (!version.isNullOrEmpty) {
      reference.setVersion(version)
    }
    if (!group.isNullOrEmpty) {
      reference.setGroup(group)
    }
    reference.setInterface(clazz)
    reference.setRetries(0)
    reference.setValidation("validation")
    reference.setId(name)
    lock.synchronized(hmReferenceConfig.putIfAbsent(name, reference))
  }

  @throws(classOf[Exception])
  def getProxy[T](clasz: Class[T], version: String = "1.0", group: String = ""): T = {
    val sb = new StringBuilder(clasz.getName)
    if (!group.isNullOrEmpty) {
      sb.append(s" -${group}")
    }
    if (!version.isNullOrEmpty) {
      sb.append(s" -${version}")
    }
    val name = sb.toString()
    try {
      var service = hmService.get(name).asInstanceOf[T]
      if (service != null) return service
      var reference = hmReferenceConfig.get(name)
      if (reference == null) {
        init(name, clasz, version, group)
        reference = hmReferenceConfig.get(name)
      }
      service = reference.get.asInstanceOf[T]
      lock.synchronized {
        hmService.putIfAbsent(name, service)
      }
      service
    } catch {
      case e: Exception =>
        throw e
    }
  }

  override def finalize(): Unit = {

    import collection.JavaConverters._
    hmReferenceConfig.asScala.foreach(x => {
      try {
        x._2.destroy()
      } catch {
        case e: Throwable =>
          logger.error(e.getMessage, e)
      }
    })

    super.finalize()
  }
}