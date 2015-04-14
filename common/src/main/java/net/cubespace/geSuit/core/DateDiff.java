package net.cubespace.geSuit.core;

// TODO: This is a stub class. This needs to be implemented
public class DateDiff {
    private long time;
    
    public long fromNow() {
        return System.currentTimeMillis() + time;
    }
    
    public long from(long date) {
        return date + time;
    }
    
    public static DateDiff valueOf(String text) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
}
