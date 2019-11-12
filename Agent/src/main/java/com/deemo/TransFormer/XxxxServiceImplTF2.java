package com.deemo.TransFormer;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class XxxxServiceImplTF2 implements ClassFileTransformer {
    private static final String START_TIME = "\nlong startTime = System.currentTimeMillis();\n";
    private static final String END_TIME = "\nlong endTime = System.currentTimeMillis();\n";
    private static final String METHOD_RUTURN_VALUE_VAR = "__time_monitor_result";
    private static final String EMPTY = "";

    /**
     * 为每一个拦截到的方法 执行一个方法的耗时操作
     *
     * @param ctMethod
     * @param ctClass
     * @throws Exception
     */
    private void transformMethod(CtMethod ctMethod, CtClass ctClass) throws Exception {
        //抽象的方法是不能修改的 或者方法前面加了final关键字
        if ((ctMethod.getModifiers() & Modifier.ABSTRACT) > 0) {
            return;
        }
        //获取原始方法名称
        String methodName = ctMethod.getName();
        String monitorStr = "\nSystem.out.println(\"method " + ctMethod.getLongName() + " cost:\" +(endTime - startTime) +\"ms.\");";
        //实例化新的方法名称
        String newMethodName = methodName + "$impl";
        //设置新的方法名称
        ctMethod.setName(newMethodName);
        //创建新的方法，复制原来的方法 ，名字为原来的名字
        CtMethod newMethod = CtNewMethod.copy(ctMethod, methodName, ctClass, null);

        StringBuilder bodyStr = new StringBuilder();
        //拼接新的方法内容
        bodyStr.append("{");

        //返回类型
        CtClass returnType = ctMethod.getReturnType();

        //是否需要返回
        boolean hasReturnValue = (CtClass.voidType != returnType);

        if (hasReturnValue) {
            String returnClass = returnType.getName();
            bodyStr.append("\n").append(returnClass + " " + METHOD_RUTURN_VALUE_VAR + ";");
        }


        bodyStr.append(START_TIME);

        if (hasReturnValue) {
            bodyStr.append("\n").append(METHOD_RUTURN_VALUE_VAR + " = ($r)" + newMethodName + "($$);");
        } else {
            bodyStr.append("\n").append(newMethodName + "($$);");
        }

        bodyStr.append(END_TIME);
        bodyStr.append(monitorStr);

        if (hasReturnValue) {
            bodyStr.append("\n").append("return " + METHOD_RUTURN_VALUE_VAR + " ;");
        }

        bodyStr.append("}");

        System.out.println(bodyStr.toString());

        //替换新方法
        newMethod.setBody(bodyStr.toString());
        //增加新方法
        ctClass.addMethod(newMethod);
    }

    private byte[] modifyMethodAndReturnBytes() {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get("com.deemo.service.XxxxServiceImpl");

            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                System.out.println(method.getName());

                String methodStr = "{" +
                        "System.out.println(\"长老：“我选大D，他为社团坐过牢！”\");" +
                        "System.out.println(\"长老：“我选阿乐，他是真心为社团做事！”\");" +
                        "System.out.println(\"邓伯：“我选阿乐！”\");" +
                        "}";
                method.insertBefore(methodStr);

                method.insertAfter("System.out.println(\"乐少：“如果输了，棍子不交，帐薄不交，妈的，什么都不交！”\");");
                method.insertAfter("System.out.println();");
                method.insertAfter("System.out.println(\"乱入：“末将于禁,愿为曹家世代赴汤蹈火!”\");");
                method.insertAfter("System.out.println();");
            }

            return ctClass.toBytecode();
        } catch (Exception e) {
            System.out.println("Javassist error." + e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!className.equals("com/deemo/service/XxxxServiceImpl")) {
            return null;
        }
        System.out.println(className);

        return modifyMethodAndReturnBytes();
    }
}
