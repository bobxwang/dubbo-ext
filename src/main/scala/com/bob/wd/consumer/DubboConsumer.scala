package com.bob.wd.consumer

import com.bob.wd.{ConfigException, DubboConsumerConfig, InvokeException}
import org.slf4j.{Logger, LoggerFactory}

/**
  * 适合没有jar包情况下的dubbo调用, 即没有元数据的情况下需要自己来组装元素据
  *
  * @param dubboConfig
  */
class DubboConsumer(dubboConfig: DubboConsumerConfig) {

  import java.util

  import Objectex.funString
  import com.alibaba.dubbo.config.utils.ReferenceConfigCache
  import com.alibaba.dubbo.config.{ApplicationConfig, ReferenceConfig, RegistryConfig}
  import com.alibaba.dubbo.rpc.service.{GenericException, GenericService}

  private val applicationConfig: ApplicationConfig = new ApplicationConfig
  applicationConfig.setName(dubboConfig.appName)

  private val registryConfig: RegistryConfig = new RegistryConfig
  registryConfig.setAddress(dubboConfig.address)
  registryConfig.setProtocol(dubboConfig.protocol)
  registryConfig.setTimeout(dubboConfig.timeout)

  private val logger: Logger = LoggerFactory.getLogger(classOf[DubboConsumer])

  def invoke(uniqueServiceDef: UniqueServiceDef, params: util.Map[String, Object]): Any = {

    val g = proxy(uniqueServiceDef)

    val paramTypeList = new util.ArrayList[String]
    val paramValueList = new util.ArrayList[AnyRef]

    if (!uniqueServiceDef.getInputDto.isNullOrEmpty) {
      // 参数是单个并且是个DTO
      paramTypeList.add(uniqueServiceDef.getInputDto)
      params.put("class", uniqueServiceDef.getInputDto)
      paramValueList.add(params)
    } else if (!uniqueServiceDef.getInputEnum.isNullOrEmpty) {
      // 参数是多个的
      if (uniqueServiceDef.getParamType.isNullOrEmpty)
        throw new ConfigException("serviceEnumInput不为空但methodParamType为空,配置出错")

      val paramName = uniqueServiceDef.getInputEnum.split(",")
      val paramType = uniqueServiceDef.getParamType.split(",")
      if (paramName.length != paramType.length) throw new ConfigException("参数名称跟参数类型长度不一致")
      val pTypeWithName = paramType.zip(paramName)
      pTypeWithName.foreach(x => {
        val ct = x._1.invokeOfClassTyee
        paramTypeList.add(ct)
        paramValueList.add(params.get(x._2))
      })
    } else {
      if (params == null || params.size() == 0) {
        // 无参调用
      } else {
        // 当作Map处理
        paramTypeList.add("java.util.Map")
        paramValueList.add(params)
      }
    }

    val targetParamType = paramTypeList.toArray(new Array[java.lang.String](paramTypeList.size))
    val targetParamValue = paramValueList.toArray(new Array[AnyRef](paramValueList.size))

    try {
      g.$invoke(uniqueServiceDef.getMethod, targetParamType, targetParamValue)
    } catch {
      case e: GenericException => throw new InvokeException(e.getMessage, e)
      case x: Throwable => throw new InvokeException(x.getMessage, x)
    }
  }

  private def proxy(uniqueServiceDef: UniqueServiceDef): GenericService = {
    val reference = new ReferenceConfig[GenericService]
    reference.setRegistry(registryConfig)
    reference.setApplication(applicationConfig)
    reference.setInterface(uniqueServiceDef.getInterfaceName)
    reference.setValidation("validation")
    if (!uniqueServiceDef.getGroup.isNullOrEmpty) reference.setGroup(uniqueServiceDef.getGroup.trim)
    reference.setGeneric(true)
    reference.setCheck(false)
    reference.setRetries(0)
    if (!uniqueServiceDef.getVersion.isNullOrEmpty) reference.setVersion(uniqueServiceDef.getVersion.trim)
    ReferenceConfigCache.getCache.get(reference)
  }

  override def finalize(): Unit = {
    try {
      ReferenceConfigCache.getCache.destroyAll()
    } catch {
      case e: Throwable =>
        logger.error(e.getMessage, e)
    }
    super.finalize()
  }
}