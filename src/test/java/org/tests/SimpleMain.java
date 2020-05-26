package org.tests;

import java.util.List;
import java.util.ArrayList;

public class SimpleMain {

    public static void main(String... args){
       // System.out.println(Runtime.getRuntime().availableProcessors());
        List<String> lst = new ArrayList<>();
        for(int i=0;i<10;i++) {
            lst.add(i+" ");
        }

        lst.stream().parallel().forEach(System.out::print);
        System.out.println("\n");
        lst.stream().forEach(System.out::print);

    }
}
