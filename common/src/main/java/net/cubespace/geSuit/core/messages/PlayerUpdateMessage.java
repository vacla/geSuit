package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import net.cubespace.geSuit.core.util.NetworkUtils;

public class PlayerUpdateMessage extends BaseMessage
{
	public Action action;
	public Item[] items;
	
	public PlayerUpdateMessage() {}
	public PlayerUpdateMessage(Action action, Item... items)
	{
		this.action = action;
		this.items = items;
	}
	
	@Override
	public void write( DataOutput out ) throws IOException
	{
		out.writeByte(action.ordinal());
		out.writeShort(items.length);
		for (Item item : items)
			item.write(action, out);
	}

	@Override
	public void read( DataInput in ) throws IOException
	{
		action = NetworkUtils.readEnum(in, Action.class);
		if (action == null)
			throw new IOException("Unknown action");
		
		int count = in.readUnsignedShort();
		
		items = new Item[count];
		
		for(int i = 0; i < count; ++i)
		{
			items[i] = new Item();
			items[i].read(action, in);
		}
	}

	public enum Action
	{
		Add,
		Remove,
		Name,
		Invalidate,
		Reset
	}
	
	public static class Item
	{
		public UUID id;
		public String username;
		public String nickname;
		
		public Item() {}
		public Item(UUID id, String username, String nickname)
		{
			this.id = id;
			this.username = username;
			this.nickname = nickname;
		}
		
		public void write( Action action, DataOutput out ) throws IOException
		{
		    NetworkUtils.writeUUID(out, id);
			
			switch (action)
			{
			case Add:
			case Reset:
				out.writeUTF(username);
				if (nickname != null) {
				    out.writeBoolean(true);
				    out.writeUTF(nickname);
				} else {
				    out.writeBoolean(false);
				}
				
				break;
			case Name:
			    if (nickname != null) {
			        out.writeBoolean(true);
			        out.writeUTF(nickname);
			    } else {
			        out.writeBoolean(false);
			    }
				break;
			default:
				break;
			}
		}

		public void read( Action action, DataInput in ) throws IOException
		{
			id = NetworkUtils.readUUID(in);
			
			switch(action)
			{
			case Add:
			case Reset:
				username = in.readUTF();
				if (in.readBoolean()) {
				    nickname = in.readUTF();
				}
				break;
			case Name:
			    if (in.readBoolean()) {
			        nickname = in.readUTF();
			    }
				break;
			default:
				break;
			}
		}
	}
}
