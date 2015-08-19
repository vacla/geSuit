package net.cubespace.geSuit.core.messages;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;

import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Action;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Item;

import org.junit.Test;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class TestPlayerUpdateMessage {
    @Test
    public void testItemIO() throws IOException {
        Item item = new Item(UUID.randomUUID(), "name", "test");
        
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        item.write(Action.Add, out);
        item.write(Action.Invalidate, out);
        item.write(Action.Name, out);
        item.write(Action.Remove, out);
        item.write(Action.Reset, out);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        Item actual = new Item();
        actual.read(Action.Add, in);
        
        assertEquals(item.id, actual.id);
        assertEquals(item.username, actual.username);
        assertEquals(item.nickname, actual.nickname);
        
        actual = new Item();
        actual.read(Action.Invalidate, in);
        
        assertEquals(item.id, actual.id);
        
        actual = new Item();
        actual.read(Action.Name, in);
        
        assertEquals(item.id, actual.id);
        assertEquals(item.nickname, actual.nickname);
        
        actual = new Item();
        actual.read(Action.Remove, in);
        
        assertEquals(item.id, actual.id);
        
        actual = new Item();
        actual.read(Action.Reset, in);
        
        assertEquals(item.id, actual.id);
        assertEquals(item.username, actual.username);
        assertEquals(item.nickname, actual.nickname);
    }
    
    @Test
    public void testItemIONullNickname() throws IOException {
        Item item = new Item(UUID.randomUUID(), "name", null);
        
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        item.write(Action.Add, out);
        item.write(Action.Reset, out);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        Item actual = new Item();
        actual.read(Action.Add, in);
        
        assertNull(actual.nickname);
        
        actual = new Item();
        actual.read(Action.Reset, in);
        
        assertNull(actual.nickname);
    }

    @Test
    public void testIO() throws IOException {
        Item item1 = new Item(UUID.randomUUID(), "first", "nickname");
        Item item2 = new Item(UUID.randomUUID(), "second", null);
        
        PlayerUpdateMessage expected = new PlayerUpdateMessage(Action.Reset, item1, item2);
        
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        expected.write(out);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        PlayerUpdateMessage actual = new PlayerUpdateMessage();
        actual.read(in);
        
        assertEquals(expected.action, actual.action);
        assertEquals(2, actual.items.length);
        assertEquals(item1.id, actual.items[0].id);
        assertEquals(item2.id, actual.items[1].id);
    }
}
