# JustTryHard-Agent
Just learn java agent…φ(๑˃∀˂๑)♪ master提交测试

# Java Agent

## 引言

以前做开发，修改某处代码后，必须重启整个项目才能生效。在实际工作中，若开发某功能在调试过程中发现一个问题，修改并重启，发现还有另一个问题，修改再重启后甚至还有BUG，就会频繁的重复如下四个操作：运行、调试、修改、重启。后来使用Spring提供的一款热部署插件，它只是部分重启，相当于重新加载了我们自己写的代码，效率提高很多。后来遇到了[Jrebel](https://zeroturnaround.com/software/jrebel/)，它只重新加载我们修改的那个类，比Springboot热部署插件重启速度更快，连改mybatis的xml文件都能热部署，太方便了有不有！（顺便安利一下同一家公司的另一个软件[XRebel](https://zeroturnaround.com/software/xrebel/download/)，实时监控服务请求）后来又接触了[BTrace](https://github.com/btraceio/btrace)，它可以线上调试代码而不需要重启项目，也是很吊的一个东西。通过了解，上面所说的几个东西都是通过Java Agent来实现的，那么Java Agent到底是啥，为啥这么吊？

## 简介

先说一下它的用途，在JDK1.5以后，我们可以使用agent技术构建一个独立于应用程序的代理程序（即为Agent），用来协助监测、运行甚至替换其他JVM上的程序。使用它可以实现虚拟机级别的AOP功能。

## Agent实例

Agent分为两种，一种是在主程序之前运行的Agent，一种是在主程序之后运行的Agent（前者的升级版，1.6以后提供）。

### 1. 在主程序运行之前的代理程序

> Agent类

```java
/*
    代码很简单，只有一个premain方法，顾名思义它代表着他将在主程序的main方法之前运行。
        args: 传递过来的参数
        inst: agent技术主要使用的API
*/
public class PreAgent {
    public static void premain(String args, Instrumentation inst) {
        System.out.println("PreAgent.premain...");
        System.out.println("Args: " + args);
    }
}
```

> 编写 MANIFEST.MF

```
Manifest-Version: 1.0
Premain-Class: com.deemo.agent.PreAgent
```

> 命令行运行

```
cmd: java -javaagent:xxxPath\Agent.jar=args -jar Main.jar
```

### 2. 在主程序运行之后的代理程序

 *JDK1.6以后提供了在程序运行之后改变程序的能力* 

> Agent类

```java
public class AgentMain {
    /*
        public static void agentmain(String agentArgs, Instrumentation inst); [1] 
        public static void agentmain(String agentArgs); [2]
        [1]的优先级比[2]高，将会被优先执行。
    */
    public static void agentmain(String args, Instrumentation ins) throws UnmodifiableClassException {
        System.out.println("AgentMain.agentmain...");
        System.out.println("Args: " + args);

        Class[] classes = ins.getAllLoadedClasses();
        for (Class aClass : classes) {
            if ("com.deemo.tlz.XxxxServiceImpl".equals(aClass.getName())) {
                System.out.println(aClass.getName());
                ins.addTransformer(new XxxxServiceImplTF(), true);
                ins.retransformClasses(aClass);

                break;
            }
        }
    }
}
```

> 编写 MANIFEST.MF

```
Manifest-Version: 1.0
Agent-Class: com.deemo.agent.AgentMain
Can-Redefine-Classes: true
Can-Retransform-Classes: true
```

> 监听

```java
/*
	因 agentmain 是在程序运行之后再挂在到目标进程的JVM中，故无法像 premain 一样在命令行中指定。
	需借用一个挂载API：
	VirtualMachine: Jar包：${env.JAVA_HOME}/lib/tools.jar
*/
public class AttachThread extends Thread {

    private final String jarPath;
    private final List<VirtualMachineDescriptor> beforeVM;

    public AttachThread(String jarPath, List<VirtualMachineDescriptor> beforeVM) {
        this.jarPath = jarPath;
        this.beforeVM = beforeVM;
    }

    @Override
    public void run() {
        int i = 0;

        do {
            try {
                VirtualMachine vm = null;
                List<VirtualMachineDescriptor> listAfter = VirtualMachine.list();
                for (VirtualMachineDescriptor vmd : listAfter) {
                    if (!beforeVM.contains(vmd)) {
                        // 如果 VM 有增加，我们就认为是被监控的 VM 启动了
                        // 这时，我们开始监控这个 VM
                        vm = VirtualMachine.attach(vmd);
                        break;
                    }
                }

                if (null != vm) {
                    vm.loadAgent(jarPath);
                    vm.detach();
                    break;
                }

                Thread.sleep(1000);
                System.out.println("-------");
            } catch (Exception e) {
                System.out.println("Attach error." + e);
                break;
            }
        } while (++i < 20);
    }

    public static void main(String[] args) {
        AttachThread attachThread = new AttachThread("...\\path\\Agent.jar", VirtualMachine.list());
        attachThread.start();
    }

}
```

## Maven项目打包

```xml
<!-- 在 pom.xml 文件中，添加如下片段，会将自定义属性添加到 MANIFEST.MF 中 -->
<build>
    <plugins>
      <plugin>
        <artifactId>maven-jar-plugin</artifactId>
        <configuration>
          <archive>
            <manifest>
              <addClasspath>true</addClasspath>
            </manifest>
            <manifestEntries>
              <!--<Premain-Class>
                com.deemo.agent.PreAgent
              </Premain-Class>-->
              <Agent-Class>
                com.deemo.agent.AgentMain2
              </Agent-Class>
              <Can-Redefine-Classes>
                true
              </Can-Redefine-Classes>
              <Can-Retransform-Classes>
                true
              </Can-Retransform-Classes>
            </manifestEntries>
          </archive>
        </configuration>
      </plugin>
    </plugins>
</build>
```

## 几项配置

### 1. META-INF/MAINIFEST.MF

**以下是agent jar文件的Manifest Attributes清单：**

```properties
Premain-Class: #如果 JVM 启动时指定了代理，那么此属性指定代理类，即包含 premain 方法的类。如果 JVM 启动时指定了代理，那么此属性是必需的。如果该属性不存在，那么 JVM 将中止。注：此属性是类名，不是文件名或路径。

Agent-Class: #如果实现支持 VM 启动之后某一时刻启动代理的机制，那么此属性指定代理类，即包含 agentmain 方法的类。 此属性是必需的，如果不存在，代理将无法启动。注：这是类名，而不是文件名或路径。

Boot-Class-Path: #设置引导类加载器搜索的路径列表。路径表示目录或库（在许多平台上通常作为 JAR 或 zip 库被引用）。**查找类的特定于平台的机制失败后，引导类加载器会搜索这些路径**。按列出的顺序搜索路径。列表中的路径由一个或多个空格分开。路径使用分层 URI 的路径组件语法。如果该路径以斜杠字符（“/”）开头，则为绝对路径，否则为相对路径。相对路径根据代理 JAR 文件的绝对路径解析。忽略格式不正确的路径和不存在的路径。如果代理是在 VM 启动之后某一时刻启动的，则忽略不表示 JAR 文件的路径。此属性是可选的。

Can-Redefine-Classes: 布尔值（true 或 false，与大小写无关）。**是否能重定义此代理所需的类**。true 以外的值均被视为 false。此属性是可选的，默认值为 false。

Can-Retransform-Classes: 布尔值（true 或 false，与大小写无关）。**是否能重转换此代理所需的类**。true 以外的值均被视为 false。此属性是可选的，默认值为 false。

Can-Set-Native-Method-Prefix: 布尔值（true 或 false，与大小写无关）。**是否能设置此代理所需的本机方法前缀**。true 以外的值均被视为 false。此属性是可选的，默认值为 false。
```

### 2. Instrument两个核心API

```properties
ClassFileTransformer: **定义了类加载前的预处理类**，可以在这个类中对要加载的类的字节码做一些处理，譬如进行字节码增强。

Instrumentation: 增强器，由JVM在入口参数中传递给我们，提供了如下的功能： 
    addTransformer/removeTransformer**: 注册/删除ClassFileTransformer；

    retransformClasses**: **对于已经加载的类重新进行转换处理，即会触发重新加载类定义**，需要注意的是，新加载的类不能修改旧有的类声明，譬如不能增加属性、不能修改方法声明；

    redefineClasses**: 与如上类似，**但不是重新进行转换处理**，而是直接把处理结果(bytecode)直接给JVM；

    getAllLoadedClasses**: **获得当前已经加载的Class**，可配合retransformClasses使用；

    getInitiatedClasses**: **获得由某个特定的ClassLoader加载的类定义**；

    getObjectSize**: **获得一个对象占用的空间**，包括其引用的对象；

    appendToBootstrapClassLoaderSearch/appendToSystemClassLoaderSearch**: 增加BootstrapClassLoader/SystemClassLoader的搜索路径；

    isNativeMethodPrefixSupported/setNativeMethodPrefix**: **判断JVM是否支持拦截Native Method**；
```

# Javassist

遇到的两个坑：

1. 如果动态植入需要通过javassist去修改某些类，那么需要在MANIFEST.MF中设置Can-Retransform-Classes和Can-Redefine-Classes为true；

   ```properties
   Can-Retransform-Classes: true
   Can-Redefine-Classes: true
   ```

   

2. 要动态修改的类Test不要跟测试入口类SampleApp放在一个类文件中，否则代理启动动态修改Class时会报如下异常：

   ```java
   java.lang.reflect.InvocationTargetException
   at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
   at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
   at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
   at java.lang.reflect.Method.invoke(Unknown Source)
   at sun.instrument.InstrumentationImpl.loadClassAndStartAgent(Unknown Source)
   at sun.instrument.InstrumentationImpl.loadClassAndCallAgentmain(Unknown Source)
   Caused by: java.lang.UnsupportedOperationException: class redefinition failed: attempted to add a method
   at sun.instrument.InstrumentationImpl.retransformClasses0(Native Method)
   at sun.instrument.InstrumentationImpl.retransformClasses(Unknown Source)
   at io.fengfu.learning.instrument.DynamicAgent.agentmain(DynamicAgent.java:14)
   ... 6 more
   ```
   
   分析原因，是因为SampleApp正在运行中，而JVM是不允许reload一个正在运行时的类的。一旦classloader加载了一个class，在运行时就不能重新加载这个class的另一个版本。

