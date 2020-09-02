package com.deemo.agent;

import com.deemo.TransFormer.XxxxServiceImplTF;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentMain {

    public static void agentmain(String args, Instrumentation ins) throws UnmodifiableClassException {
        System.out.println("AgentMain.agentmain...");
        System.out.println("Args: " + args);

        Class<?>[] classes = ins.getAllLoadedClasses();
        for (Class<?> aClass : classes) {
            if ("com.deemo.service.XxxxServiceImpl".equals(aClass.getName())) {
                System.out.println(aClass.getName());
                ins.addTransformer(new XxxxServiceImplTF(), true);
                ins.retransformClasses(aClass);

                break;
            }
        }
    }
}
