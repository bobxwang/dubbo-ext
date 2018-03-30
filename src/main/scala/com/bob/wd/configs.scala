package com.bob.wd

/**
  *
  * @param appName  应用名称
  * @param protocol 协议
  * @param address  地址
  * @param timeout  超时时间,单位秒
  */
case class DubboConsumerConfig(appName: String, protocol: String, address: String, timeout: Int)

case class DubboProviderConfig(dcc: DubboConsumerConfig, threads: Int, name: String, port:Int)

class ConfigException(message: String) extends RuntimeException(message)

class InvokeException(message: String, e: Throwable) extends RuntimeException(message, e)