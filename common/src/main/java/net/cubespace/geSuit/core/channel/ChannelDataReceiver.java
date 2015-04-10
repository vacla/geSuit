package net.cubespace.geSuit.core.channel;

public interface ChannelDataReceiver<T>
{
	/**
	 * Called when data is received through the channel.
	 * @param channel The channel the data was sent through
	 * @param value The received value
	 */
	public void onDataReceive(Channel<T> channel, T value);
}
