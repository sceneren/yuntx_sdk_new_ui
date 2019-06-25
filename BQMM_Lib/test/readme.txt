1.config.properties
  修改线程数：
  thread.number.dispatcher=32
  thread.number.pushmessage=32

2.applicationContext-mcm-service.xml
  去掉：<bean id="sevenMoorController">节点

3.PushService.java PushServiceImpl.java添加线程数

4.ModuleConsumer.java 修改线程数

5.com.yuntongxun.mcm.sevenmoor 包测试类移至压测工具

6.82环境 class_new是最新的包，class是单压开始咨询临时调整的包
