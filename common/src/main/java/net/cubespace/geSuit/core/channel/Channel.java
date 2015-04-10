package net.cubespace.geSuit.core.channel;

public interface Channel<T>
{
	/**
	 * Gets the name of the channel
	 */
	public String getName();
	
	/**
	 * Sets the codec for this channel. This is <b>required</b> if T is not byte[]
	 * @param codec The codec to use. This cannot be null
	 */
	public void setCodec(ChannelCodec<T> codec);
	
	/**
	 * Sends a data packet to all receivers excluding this server
	 * @param value The value to send, this must not be null
	 */
	public void broadcast(T value);
	
	/**
	 * Adds a receiver for this channel
	 * @param receiver The receiver to add, must not be null
	 */
	public void addReceiver(ChannelDataReceiver<T> receiver);
}
