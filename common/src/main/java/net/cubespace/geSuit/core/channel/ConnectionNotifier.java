package net.cubespace.geSuit.core.channel;

public interface ConnectionNotifier {
    public void onConnectionRestored();
    
    public void onConnectionLost(Throwable e);
}
