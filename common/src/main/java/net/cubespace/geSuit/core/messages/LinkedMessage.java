package net.cubespace.geSuit.core.messages;

import java.util.concurrent.ExecutionException;

public interface LinkedMessage<T>
{
	public boolean isReply();
	
	public boolean isSource(LinkedMessage<?> message);
	
	public T getReply() throws ExecutionException;
}
