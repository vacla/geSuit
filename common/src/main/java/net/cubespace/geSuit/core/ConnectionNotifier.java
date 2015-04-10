package net.cubespace.geSuit.core;

public interface ConnectionNotifier
{
	public void onConnectionRestored();

    public void onConnectionLost(Throwable e);
}
