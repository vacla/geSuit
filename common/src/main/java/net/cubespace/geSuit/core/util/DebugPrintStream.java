package net.cubespace.geSuit.core.util;

import java.io.PrintStream;

public class DebugPrintStream {
    private PrintStream out;
    private StringBuffer buffer;
    
    public DebugPrintStream(PrintStream out) {
        this.out = out;
        
        buffer = new StringBuffer();
    }
    
    public void println(String value) {
        print(value);
        out.println(buffer.toString());
        buffer.setLength(0);
    }
    
    public void print(String value) {
        buffer.append(value);
    }
    
    public static final DebugPrintStream systemOut = new DebugPrintStream(System.out);
    public static final DebugPrintStream systemErr = new DebugPrintStream(System.err);
}
