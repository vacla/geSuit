package net.cubespace.geSuit.core.channel;

/**
 * This interface allows you to receive messages from a {@link Channel}
 * @param <T> The type of data being received
 */
public interface ChannelDataReceiver<T> {
    /**
     * Called when data is received through the channel.
     * @param channel The channel the data was sent through
     * @param value The received value
     * @param sourceId The ID of the sender of this message
     * @param isBroadcast True if this was broadcast, false if it was sent directly to me
     */
    public void onDataReceive(Channel<T> channel, T value, int sourceId, boolean isBroadcast);
}
