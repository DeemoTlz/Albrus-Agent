package com.deemo.agent;

import com.deemo.TransFormer.XxxxServiceImplTF2;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentMain2 {

    public static void agentmain(String args, Instrumentation ins) throws UnmodifiableClassException {
        System.out.println("AgentMain2.agentmain...");
        System.out.println("Args: " + args);

        Class[] classes = ins.getAllLoadedClasses();
        for (Class aClass : classes) {
            if ("com.deemo.service.XxxxServiceImpl".equals(aClass.getName())) {
            // if ("com.deemo.service.ZzzzServiceImpl".equals(aClass.getName())) {
                System.out.println("Finded..");
                ins.addTransformer(new XxxxServiceImplTF2(), true);
                ins.retransformClasses(aClass);

                break;
            }
        }
    }

}
