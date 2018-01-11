package com.bob.wd.provider

import java.util

import com.alibaba.dubbo.common.utils.ConcurrentHashSet
import com.bob.wd.DubboProviderConfig
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by wangxiang on 17/12/13.
  */
class DubboProvider(dubboProviderConfig: DubboProviderConfig) {

  import com.alibaba.dubbo.config.{ApplicationConfig, ProtocolConfig, RegistryConfig, ServiceConfig}

  private val applicationConfig: ApplicationConfig = new ApplicationConfig
  applicationConfig.setName(dubboProviderConfig.dc.appName)

  private val registryConfig: RegistryConfig = new RegistryConfig
  registryConfig.setAddress(dubboProviderConfig.dc.address)
  registryConfig.setProtocol(dubboProviderConfig.dc.protocol)
  registryConfig.setTimeout(dubboProviderConfig.dc.timeout)

  private val protocolConfig = new ProtocolConfig
  protocolConfig.setName(dubboProviderConfig.name)
  protocolConfig.setPort(dubboProviderConfig.port)
  protocolConfig.setThreads(dubboProviderConfig.threads)

  private val logger: Logger = LoggerFactory.getLogger(classOf[DubboProvider])

  private val serviceConfigs: util.Set[ServiceConfig[_]] = new ConcurrentHashSet[ServiceConfig[_]]

  /**
    * 注册一个dubbo服务
    *
    * @param o 某个接口的实现类
    */
  def register(o: Object, isValidation: Boolean): Unit = {
    val serviceConfig = new ServiceConfig[Any]()
    if (o.getClass.getInterfaces.length > 0) {
      val l: Class[_] = (o.getClass.getInterfaces).toList(0)
      serviceConfig.setInterface(l)
    } else throw new IllegalStateException("Failed to export remote service class " + o.getClass.getName + ", cause: The @Service undefined interfaceClass or interfaceName, and the service class unimplemented any interfaces.")

    serviceConfig.setApplication(applicationConfig)
    serviceConfig.setRegistry(registryConfig)
    serviceConfig.setProtocol(protocolConfig)
    serviceConfig.setRef(o)
    if (isValidation) {
      serviceConfig.setValidation("validation")
    }
    serviceConfig.export()
    serviceConfigs.add(serviceConfig)
    logger.info(serviceConfig.toString)
  }

  /**
    * 因为java没有默认方法,而scala有,但因为要暴露给java使用,所以这里给个重载
    * @param o
    */
  def register(o: Object): Unit = {
    register(o, false)
  }

  override def finalize(): Unit = {
    import scala.collection.JavaConversions._
    for (serviceConfig <- serviceConfigs) {
      try
        serviceConfig.unexport()
      catch {
        case e: Throwable =>
          logger.error(e.getMessage, e)
      }
    }
    super.finalize()
  }
}