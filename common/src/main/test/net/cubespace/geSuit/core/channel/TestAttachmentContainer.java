package net.cubespace.geSuit.core.channel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.Platform;
import net.cubespace.geSuit.core.attachments.Attachment;
import net.cubespace.geSuit.core.attachments.AttachmentContainer;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.SyncAttachmentMessage;
import net.cubespace.geSuit.core.storage.StorageInterface;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TestAttachmentContainer {
    @Test
    public void testAddAttachment() {
        AttachmentContainer container = new AttachmentContainer(null, null, null, null);
        
        Attachment testAttachment = new Attachment() {
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
        };
        
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
        AttachmentContainer container = new AttachmentContainer(null, null, null, null);
        
        Attachment testAttachment = new Attachment() {
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
        };
        
        Class<? extends Attachment> testClass = testAttachment.getClass();
        
        container.addAttachment(testAttachment);
        
        assertSame(testAttachment, container.getAttachment(testClass));
        // Make sure its not destructive
        assertNotNull(container.getAttachment(testClass));
        
        // Ensure we can safely get unknown items
        assertNull(container.getAttachment(Attachment.class));
    }

    @Test
    public void testRemoveAttachment() {
        AttachmentContainer container = new AttachmentContainer(null, null, null, null);
        
        Attachment testAttachment = new Attachment() {
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
        };
        
        Class<? extends Attachment> testClass = testAttachment.getClass();
        
        container.addAttachment(testAttachment);
        
        assertSame(testAttachment, container.removeAttachment(testClass));
        assertNull(container.getAttachment(testClass));
    }

    @Test
    public void testGetAttachments() {
        AttachmentContainer container = new AttachmentContainer(null, null, null, null);
        
        Attachment testAttachment = new Attachment() {
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
        };
        
        container.addAttachment(testAttachment);
        
        Collection<Attachment> attachments = container.getAttachments();
        assertTrue(attachments.size() == 1);
        assertTrue(attachments.contains(testAttachment));
        
        // Make sure its readonly
        try {
            attachments.add(testAttachment);
            fail();
        } catch (UnsupportedOperationException e) {
            // Ok
        }
    }

    @Test
    public void testLocalAttachmentUpdate() {
        Channel<BaseMessage> channel = mock(Channel.class);
        StorageInterface storage = mock(StorageInterface.class);
        Platform platform = mock(Platform.class);
        
        AttachmentContainer container = new AttachmentContainer(null, channel, storage, platform);
        
        Attachment testAttachment = new Attachment() {
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
        };
        
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
        Channel<BaseMessage> channel = mock(Channel.class);
        StorageInterface storage = mock(StorageInterface.class);
        Platform platform = mock(Platform.class);
        UUID testUUID = UUID.randomUUID();
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(testUUID);
        
        AttachmentContainer container = new AttachmentContainer(player, channel, storage, platform);
        
        Attachment testAttachment = new Attachment() {
            @Override
            public void save(Map<String, String> values) {
            }
            
            @Override
            public void load(Map<String, String> values) {
            }
            
            @Override
            public AttachmentType getType() {
                return AttachmentType.Session;
            }
        };
        
        // Prepare test condition
        container.addAttachment(testAttachment);
        testAttachment.setDirty();
        
        // Test the update
        container.update();
        
        // Assertions
        verify(channel).broadcast(any(SyncAttachmentMessage.class));
        verifyNoMoreInteractions(channel);
        verifyZeroInteractions(storage);
    }
    
    @Test
    public void testPersistentAttachmentUpdate() {
        Channel<BaseMessage> channel = mock(Channel.class);
        StorageInterface storage = mock(StorageInterface.class);
        Platform platform = mock(Platform.class);
        
        AttachmentContainer container = new AttachmentContainer(null, channel, storage, platform);
        
        Attachment testAttachment = new Attachment() {
            @Override
            public void save(Map<String, String> values) {
            }
            
            @Override
            public void load(Map<String, String> values) {
            }
            
            @Override
            public AttachmentType getType() {
                return AttachmentType.Persistent;
            }
        };
        
        Set<String> names = Sets.newHashSet(testAttachment.getClass().getName());
        
        // Prepare test condition
        container.addAttachment(testAttachment);
        testAttachment.setDirty();
        
        // Test the update
        container.update();
        
        // Assertions
        verify(storage).set("attachments", names);
        verify(storage).set(testAttachment.getClass().getSimpleName(), testAttachment);
        
        verifyZeroInteractions(channel);
    }
    
    @Test
    public void testPersistentAttachmentUpdateAfterRemove() {
        Channel<BaseMessage> channel = mock(Channel.class);
        StorageInterface storage = mock(StorageInterface.class);
        Platform platform = mock(Platform.class);
        
        AttachmentContainer container = new AttachmentContainer(null, channel, storage, platform);
        
        Attachment testAttachment = new Attachment() {
            @Override
            public void save(Map<String, String> values) {
            }
            
            @Override
            public void load(Map<String, String> values) {
            }
            
            @Override
            public AttachmentType getType() {
                return AttachmentType.Persistent;
            }
        };
        
        // Prepare test condition
        container.addAttachment(testAttachment);
        testAttachment.setDirty();
        container.update();
        
        // Remove the attachment
        container.removeAttachment(testAttachment.getClass());
        
        container.update(true);
        // Assertions
        verify(storage, times(2)).set("attachments", Sets.newHashSet());
        
        verifyZeroInteractions(channel);
    }

    @Test
    public void testLoad() {
        StorageInterface storage = mock(StorageInterface.class);
        Platform platform = mock(Platform.class);
        AttachmentContainer container = new AttachmentContainer(null, null, storage, platform);
        
        Set<String> attachmentNames = Sets.newHashSet(TestPersistAttachment.class.getName());
        TestPersistAttachment attachment = new TestPersistAttachment();
        
        // Train the mock
        when(storage.getSetString("attachments")).thenReturn(attachmentNames);
        when(storage.getStorable(eq("testpersistattachment"), any(TestPersistAttachment.class))).thenReturn(attachment);
        
        // Pre check
        assertNull(container.getAttachment(TestPersistAttachment.class));
        
        // Load
        container.load();
        
        // Assertions
        assertNotNull(container.getAttachment(TestPersistAttachment.class));
    }

    @Test
    public void testOnAttachmentUpdate() {
        Platform platform = mock(Platform.class);
        UUID testUUID = UUID.randomUUID();
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(testUUID);
        
        AttachmentContainer container = new AttachmentContainer(player, null, null, platform);
        
        Map<String, String> values = Maps.newHashMap();
        SyncAttachmentMessage message = new SyncAttachmentMessage(testUUID, TestSessionAttachment.class, values);
        
        // Pre check
        assertNull(container.getAttachment(TestSessionAttachment.class));
        
        // Load
        container.onAttachmentUpdate(message);
        
        // Check it
        assertNotNull(container.getAttachment(TestSessionAttachment.class));
    }
    
    @Test
    public void testOnAttachmentUpdateInvalid() {
        Platform platform = mock(Platform.class);
        UUID testUUID = UUID.randomUUID();
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(testUUID);
        AttachmentContainer container = new AttachmentContainer(player, null, null, platform);
        
        Map<String, String> values = Maps.newHashMap();
        SyncAttachmentMessage message = new SyncAttachmentMessage(testUUID, TestPersistAttachment.class, values);
        
        // Pre check
        assertNull(container.getAttachment(TestPersistAttachment.class));
        
        // Load
        container.onAttachmentUpdate(message);
        
        // Check it
        // Would not have loaded as it is a persistent attachment
        assertNull(container.getAttachment(TestPersistAttachment.class));
    }
    
    public static class TestPersistAttachment extends Attachment {
        @Override
        public void save(Map<String, String> values) {
        }

        @Override
        public void load(Map<String, String> values) {
        }

        @Override
        public AttachmentType getType() {
            return AttachmentType.Persistent;
        }
    }
    
    public static class TestSessionAttachment extends Attachment {
        @Override
        public void save(Map<String, String> values) {
        }

        @Override
        public void load(Map<String, String> values) {
        }

        @Override
        public AttachmentType getType() {
            return AttachmentType.Session;
        }
    }
}
