package com.deemo.tlz;

import com.deemo.service.ZzzzServiceImpl;
import com.deemo.service.XxxxServiceImpl;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Main.main...");

        int count = 0;
        do {
            XxxxServiceImpl.print();
            YyyyServiceImpl.print();
            ZzzzServiceImpl.print();

            Thread.sleep(500);
        } while (++count < 10);
    }
}
