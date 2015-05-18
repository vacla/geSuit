package net.cubespace.geSuit.core.channel;

import java.util.Collection;

public interface ChannelManager {
    /**
     * The id for the proxy server. Always 0
     */
    public static final int PROXY = 0;
    /**
     * The broadcast id. Used to send to all servers
     */
    public static final int BROADCAST = 0xFFFFFFFF;
    
	/**
	 * Creates a channel 
	 * @param name The name of the channel, case insensitive. This needs to be unique
	 * @return The channel object
	 * @throws IllegalArgumentException Thrown if the channel name already exists
	 */
	public Channel<byte[]> createChannel(String name) throws IllegalArgumentException;
	
	/**
	 * Creates a channel 
	 * @param name The name of the channel, case insensitive. This needs to be unique
	 * @param clazz The class handled in the channel
	 * @return The channel object
	 * @throws IllegalArgumentException Thrown if the channel name already exists
	 */
	public <T> Channel<T> createChannel(String name, Class<T> clazz) throws IllegalArgumentException;
	
	/**
	 * Gets a channel
	 * @param name The name of the channel, case insensitive
	 * @return The channel object or null
	 */
	public Channel<?> getChannel(String name);
	
	/**
	 * Gets all the channels
	 * @return A collection of channels, or an empty collection
	 */
	public Collection<Channel<?>> getChannels();
}
