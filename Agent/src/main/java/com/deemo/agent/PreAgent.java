package com.deemo.agent;

import com.deemo.TransFormer.XxxxServiceImplTF;

import java.lang.instrument.Instrumentation;

public class PreAgent {

    public static void premain(String args, Instrumentation ins) {
        System.out.println("PreAgent.premain...");
        System.out.println("Args: " + args);

        ins.addTransformer(new XxxxServiceImplTF());
    }
}
