package com.deemo.TransFormer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class XxxxServiceImplTF implements ClassFileTransformer {

    private final String XxxxServiceImpl = "/classes/XxxxServiceImpl.class";

    private byte[] getBytesFromClassFile() {
        InputStream is = this.getClass().getResourceAsStream(XxxxServiceImpl);
        BufferedInputStream bis = new BufferedInputStream(is);

        int length = 0;
        byte[] bytes = new byte[1024];
        try {
            length += bis.read(bytes, 0, 1024);
        } catch (Exception e) {
            System.out.println("Get bytes error.");
            return null;
        }

        return Arrays.copyOfRange(bytes, 0, length);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (!className.equals("com/deemo/tlz/XxxxServiceImpl")) {
            return null;
        }

        return getBytesFromClassFile();
    }
}
