package net.cubespace.geSuit.core.channel;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Channel codecs allow you to push and receive POJOs with channels
 * @param <T> The type of the object being encoded or decoded
 */
public interface ChannelCodec<T>
{
	/**
	 * Encodes the object into a stream of bytes
	 * @param value The value to be encoded
	 * @param out The buffer to write to
	 */
	public void encode(T value, DataOutput out) throws IOException;
	
	/**
	 * Decodes the bytes into an object. This method may throw an exception if the value cannot be decoded
	 * @param in The bytes to load
	 * @return The decoded value
	 */
	public T decode(DataInput in) throws IOException;
}
