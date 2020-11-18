package com.supanadit.restsuite.util;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
public class Analyses {
    public static volatile Analyses instance = null;

    private Analyses() {
    }

    public static final Analyses getInstance() {
        if (Analyses.instance == null) {
            synchronized(Analyses.class) {
                if (Analyses.instance == null) {
                    Analyses.instance = new Analyses();
                }
            }
        }
        return Analyses.instance;
    }

    public static void logAppel(String method1, String method2) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("log.dot", true));
            writer.write(((method1 + "->") + method2) + ";\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}