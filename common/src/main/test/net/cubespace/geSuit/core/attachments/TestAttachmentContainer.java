package net.cubespace.geSuit.core.attachments;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.Platform;
import net.cubespace.geSuit.core.attachments.Attachment;
import net.cubespace.geSuit.core.attachments.AttachmentContainer;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.player.GlobalPlayerAttachmentUpdateEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.SyncAttachmentMessage;
import net.cubespace.geSuit.core.storage.StorageInterface;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TestAttachmentContainer {
    private GlobalPlayer player;
    private Platform platform;
    private StorageInterface storage;
    private Channel<BaseMessage> channel;
    
    private AttachmentContainer container;
    
    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        channel = mock(Channel.class);
        storage = mock(StorageInterface.class);
        platform = mock(Platform.class);
        
        UUID testUUID = UUID.randomUUID();
        player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(testUUID);
        
        container = new AttachmentContainer(player, channel, storage, platform);
    }
    
    @Test
    public void testAddAttachment() {
        TestLocalAttachment testAttachment = new TestLocalAttachment();
        
        // Try adding it
        Attachment response = container.addAttachment(testAttachment);
        assertSame(testAttachment, response);
        
        // Try adding it again
        try {
            container.addAttachment(testAttachment);
            fail();
        } catch (IllegalStateException e) {
            // Good
        }
    }

    @Test
    public void testGetAttachment() {
        // Prepare
        TestLocalAttachment testAttachment = new TestLocalAttachment();
        
        Class<? extends Attachment> testClass = testAttachment.getClass();
        container.addAttachment(testAttachment);
        
        // Do the test
        assertSame(testAttachment, container.getAttachment(testClass));
        // Make sure its not destructive
        assertNotNull(container.getAttachment(testClass));
        
        // Ensure we can safely get unknown items
        assertNull(container.getAttachment(Attachment.class));
    }

    @Test
    public void testRemoveAttachment() {
        // Prepare
        TestLocalAttachment testAttachment = new TestLocalAttachment();
        
        container.addAttachment(testAttachment);
        
        // Do the test
        assertSame(testAttachment, container.removeAttachment(TestLocalAttachment.class));
        assertNull(container.getAttachment(TestLocalAttachment.class));
    }

    @Test
    public void testGetAttachments() {
        Attachment testAttachment1 = new TestLocalAttachment();
        Attachment testAttachment2 = new TestSessionAttachment();
        Attachment testAttachment3 = new TestPersistAttachment();
        
        container.addAttachment(testAttachment1);
        container.addAttachment(testAttachment2);
        container.addAttachment(testAttachment3);
        
        // Do the test
        Collection<Attachment> attachments = container.getAttachments();
        assertTrue(attachments.size() == 3);
        assertTrue(attachments.contains(testAttachment1));
        assertTrue(attachments.contains(testAttachment2));
        assertTrue(attachments.contains(testAttachment3));
        
        // Make sure its readonly
        try {
            attachments.add(testAttachment3);
            fail();
        } catch (UnsupportedOperationException e) {
            // Ok
        }
    }

    @Test
    public void testLocalAttachmentUpdate() {
        TestLocalAttachment testAttachment = new TestLocalAttachment();
        
        // Prepare test condition
        container.addAttachment(testAttachment);
        testAttachment.setDirty();
        
        // Test the update
        container.update();
        
        // Assert that nothing was done
        verifyZeroInteractions(channel, storage);
    }
    
    @Test
    public void testSessionAttachmentUpdate() {
        Attachment testAttachment = new TestSessionAttachment();
        
        // Prepare test condition
        container.addAttachment(testAttachment);
        testAttachment.setDirty();
        
        // Test the update
        container.update();
        
        // Assertions
        verify(storage, times(1)).set("attachment." + testAttachment.getClass().getName(), testAttachment);
        verify(storage, times(1)).set("attachments.session", Sets.newHashSet(testAttachment.getClass().getName()));
        verifyZeroInteractions(channel);
    }
    
    @Test
    public void testPersistentAttachmentUpdate() {
        Attachment testAttachment = new TestPersistAttachment();
        
        // Prepare test condition
        container.addAttachment(testAttachment);
        testAttachment.setDirty();
        
        // Test the update
        container.update();
        
        // Assertions
        verify(storage, times(1)).set("attachment." + testAttachment.getClass().getName(), testAttachment);
        verify(storage, times(1)).set("attachments.persist", Sets.newHashSet(testAttachment.getClass().getName()));
        verifyZeroInteractions(channel);
    }
    
    @Test
    public void testPersistentAttachmentUpdateAfterRemove() {
        Attachment testAttachment = new TestPersistAttachment();
        
        // Prepare test condition
        container.addAttachment(testAttachment);
        testAttachment.setDirty();
        container.update();
        reset(storage);
        
        // Remove the attachment
        container.removeAttachment(testAttachment.getClass());
        container.update(true);
        
        // Assertions
        verify(storage, times(1)).remove("attachment." + testAttachment.getClass().getName());
        verify(storage, times(1)).set("attachments.persist", Sets.newHashSet());
        verifyZeroInteractions(channel);
    }
    
    @Test
    public void testSessionAttachmentUpdateAfterRemove() {
        Attachment testAttachment = new TestSessionAttachment();
        
        // Prepare test condition
        container.addAttachment(testAttachment);
        testAttachment.setDirty();
        container.update();
        reset(storage);
        
        // Remove the attachment
        container.removeAttachment(testAttachment.getClass());
        container.update(true);
        
        // Assertions
        verify(storage, times(1)).remove("attachment." + testAttachment.getClass().getName());
        verify(storage, times(1)).set("attachments.session", Sets.newHashSet());
        verifyZeroInteractions(channel);
    }
    
    @Test
    public void testLoadPersistNonExist() {
        final Map<String, String> testValues = Maps.newHashMap();
        testValues.put("testa", "1");
        testValues.put("testb", "2");
        
        // Train to load the attachment
        when(storage.getSetString("attachments.persist")).thenReturn(Sets.newHashSet(TestPersistAttachment.class.getName()));
        when(storage.getStorable(eq("attachment." + TestPersistAttachment.class.getName()), any(TestPersistAttachment.class))).then(new Answer<Attachment>() {
            @Override
            public Attachment answer(InvocationOnMock invocation) throws Throwable {
                Attachment input = invocation.getArgumentAt(1, Attachment.class);
                
                if (input == null) {
                    fail("No input attachment");
                }
                
                input.load(testValues);
                
                return input;
            }
        });
        
        // Do the test
        container.load();
        assertNotNull(container.getAttachment(TestPersistAttachment.class));
        TestPersistAttachment attachment = container.getAttachment(TestPersistAttachment.class);
        assertEquals(testValues, attachment.values);
    }
    
    @Test
    public void testLoadPersistExist() {
        final Map<String, String> initialTestValues = Maps.newHashMap();
        initialTestValues.put("testa", "1");
        initialTestValues.put("testb", "2");
        
        final Map<String, String> finalTestValues = Maps.newHashMap();
        finalTestValues.put("testa", "1");
        finalTestValues.put("testb", "2");
        
        TestPersistAttachment existing = new TestPersistAttachment();
        existing.values.putAll(initialTestValues);
        container.addAttachment(existing);
        
        // Train to load the attachment
        when(storage.getSetString("attachments.persist")).thenReturn(Sets.newHashSet(TestPersistAttachment.class.getName()));
        when(storage.getStorable(eq("attachment." + TestPersistAttachment.class.getName()), any(TestPersistAttachment.class))).then(new Answer<Attachment>() {
            @Override
            public Attachment answer(InvocationOnMock invocation) throws Throwable {
                Attachment input = invocation.getArgumentAt(1, Attachment.class);
                
                if (input == null) {
                    fail("No input attachment");
                }
                
                input.load(finalTestValues);
                
                return input;
            }
        });
        
        // Do the test
        container.load();
        assertNotNull(container.getAttachment(TestPersistAttachment.class));
        TestPersistAttachment attachment = container.getAttachment(TestPersistAttachment.class);
        assertEquals(finalTestValues, attachment.values);
        assertSame(existing, attachment); // Should be the same object
    }
    
    @Test
    public void testLoadPersistRemove() {
        final Map<String, String> initialTestValues = Maps.newHashMap();
        initialTestValues.put("testa", "1");
        initialTestValues.put("testb", "2");
                
        TestPersistAttachment existing = new TestPersistAttachment();
        existing.values.putAll(initialTestValues);
        container.addAttachment(existing);
        
        // Train to discard the attachment
        when(storage.getSetString("attachments.persist")).thenReturn(Sets.<String>newHashSet());
        
        // Do the test
        container.load();
        assertNull(container.getAttachment(TestPersistAttachment.class));
    }
    
    @Test
    public void testLoadIgnoreLocal() {
        TestLocalAttachment localAttachment = new TestLocalAttachment();
        container.addAttachment(localAttachment);
        
        final Map<String, String> testValues = Maps.newHashMap();
        testValues.put("testa", "1");
        testValues.put("testb", "2");
        
        // Train to load the attachment
        when(storage.getSetString("attachments.persist")).thenReturn(Sets.newHashSet(TestPersistAttachment.class.getName()));
        when(storage.getStorable(eq("attachment." + TestPersistAttachment.class.getName()), any(TestPersistAttachment.class))).then(new Answer<Attachment>() {
            @Override
            public Attachment answer(InvocationOnMock invocation) throws Throwable {
                Attachment input = invocation.getArgumentAt(1, Attachment.class);
                
                if (input == null) {
                    fail("No input attachment");
                }
                
                input.load(testValues);
                
                return input;
            }
        });
        
        // Do the test
        container.load();
        // Must still be there
        assertSame(localAttachment, container.getAttachment(TestLocalAttachment.class));
    }
    
    @Test
    public void testModified() {
        TestLocalAttachment testAttachment1 = new TestLocalAttachment();
        TestLocalAttachment testAttachment2 = new TestLocalAttachment() {};
        container.addAttachment(testAttachment1);
        container.addAttachment(testAttachment2);
        container.update();
        
        assertFalse(container.isModified());
        
        // Only needs one to be set
        testAttachment2.setDirty();
        assertTrue(container.isModified());
    }
    
    @Test
    public void testModifiedOnAdd() {
        TestSessionAttachment testAttachment1 = new TestSessionAttachment();
        container.addAttachment(testAttachment1);
        
        assertTrue(container.isModified());
    }
    
    @Test
    public void testLoadMissingClass() {
        when(storage.getSetString("attachments.persist")).thenReturn(Sets.newHashSet("this.is.a.missing.clazz"));
        
        // Do the test
        container.load();
        verify(storage, never()).getStorable(eq("attachment.this.is.a.missing.clazz"), any(Attachment.class));
        
        // Be sure it actually knows about it
        container.update(true);
        verify(storage, times(1)).set("attachments.persist", Sets.newHashSet("this.is.a.missing.clazz"));
    }
    
    @Test
    public void testChangeBroadcast() {
        TestSessionAttachment unchanged = new TestSessionAttachment();
        TestPersistAttachment changed = new TestPersistAttachment();
        
        container.addAttachment(unchanged);
        container.addAttachment(changed);
        
        container.update();
        reset(channel);
        
        // Now we can set the state
        changed.setDirty();
        container.update();
        
        
        ArgumentCaptor<SyncAttachmentMessage> captor = ArgumentCaptor.forClass(SyncAttachmentMessage.class);
        // Do the test
        container.broadcastChanges();
        verify(channel, times(1)).broadcast(captor.capture());
        
        SyncAttachmentMessage message = captor.getValue();
        assertEquals(player.getUniqueId(), message.owner);
        assertEquals(Sets.newHashSet(TestPersistAttachment.class.getName()), message.updatedAttachments);
    }
    
    @Test
    public void testLoadIfNeeded() {
        // Not yet loaded
        container.loadIfNeeded();
        // Should now be loaded
        container.loadIfNeeded();
        
        // Only the first should have made calls to storage
        verify(storage, times(1)).getSetString("attachments.persist");
    }
    
    @Test
    public void testInvalidate() {
        container.loadIfNeeded();
        container.invalidate();
        container.loadIfNeeded();
        
        // Both loadIfNeeded() should have loaded due to invalidate
        verify(storage, times(2)).getSetString("attachments.persist");
    }
    
    @Test
    public void testOnAttachmentUpdate() {
        TestPersistAttachment testAttachment = new TestPersistAttachment();
        container.addAttachment(testAttachment);
        
        when(storage.getSetString("attachments.persist")).thenReturn(Sets.newHashSet(TestPersistAttachment.class.getName()));
        when(storage.getStorable(eq("attachment." + TestPersistAttachment.class.getName()), any(TestPersistAttachment.class))).thenReturn(testAttachment);
        
        SyncAttachmentMessage message = new SyncAttachmentMessage(player.getUniqueId(), Sets.newHashSet(TestPersistAttachment.class.getName()));
        container.onAttachmentUpdate(message);

        // Should have triggered a load
        verify(storage, times(1)).getSetString("attachments.persist");
        verify(platform, times(1)).callEvent(any(GlobalPlayerAttachmentUpdateEvent.class));
    }
    
    @Test
    public void testOnAttachmentUpdateMissing() {
        SyncAttachmentMessage message = new SyncAttachmentMessage(player.getUniqueId(), Sets.newHashSet(TestPersistAttachment.class.getName()));
        container.onAttachmentUpdate(message);

        // Should have triggered a load
        verify(storage, times(1)).getSetString("attachments.persist");
        verify(platform, never()).callEvent(any(GlobalPlayerAttachmentUpdateEvent.class));
    }
    
    public static class TestLocalAttachment extends Attachment {
        @Override
        public void save(Map<String, String> values) {
        }

        @Override
        public void load(Map<String, String> values) {
        }

        @Override
        public AttachmentType getType() {
            return AttachmentType.Local;
        }
    }
    
    public static class TestPersistAttachment extends Attachment {
        public Map<String, String> values;
        
        public TestPersistAttachment() {
            values = Maps.newHashMap();
        }
        
        @Override
        public void save(Map<String, String> values) {
            values.putAll(values);
        }

        @Override
        public void load(Map<String, String> values) {
            this.values.clear();
            this.values.putAll(values);
        }

        @Override
        public AttachmentType getType() {
            return AttachmentType.Persistent;
        }
    }
    
    public static class TestSessionAttachment extends Attachment {
        public Map<String, String> values;
        
        public TestSessionAttachment() {
            values = Maps.newHashMap();
        }
        
        @Override
        public void save(Map<String, String> values) {
            values.putAll(values);
        }

        @Override
        public void load(Map<String, String> values) {
            this.values.clear();
            this.values.putAll(values);
        }

        @Override
        public AttachmentType getType() {
            return AttachmentType.Session;
        }
    }
}
