package com.e6yun.demo;

import javax.print.attribute.standard.PrinterResolution;
import java.util.concurrent.TimeUnit;

public class Hello{
    public static void main(String[] agrs) throws Exception{
        System.out.println("hello");
        while (true) {
            TimeUnit.SECONDS.sleep(100);
            System.out.println("app sleeping...");
        }
    }
}
