package com.example.demo.service;

import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserService userService;

    @InjectMocks
    private WebSocketService webSocketService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("USER-001");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    void testNotifyUserConnected() {
        webSocketService.notifyUserConnected(testUser);

        assertTrue(webSocketService.isUserConnected("testuser"));
    }

    @Test
    void testNotifyUserDisconnected() {
        webSocketService.notifyUserConnected(testUser);
        
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        doNothing().when(userService).setUserOnlineStatus(anyString(), anyBoolean());

        webSocketService.notifyUserDisconnected("testuser");

        assertFalse(webSocketService.isUserConnected("testuser"));
    }

    @Test
    void testSendPrivateMessage() {
        doNothing().when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        webSocketService.sendPrivateMessage("user1", "user2", "Hello");

        verify(messagingTemplate).convertAndSendToUser(eq("user2"), eq("/queue/private"), any());
    }

    @Test
    void testSendChannelMessage() {
        assertDoesNotThrow(() -> {
            webSocketService.sendChannelMessage("user1", "channel1", "Hello channel");
        });
    }

    @Test
    void testGetConnectedUsers() {
        webSocketService.notifyUserConnected(testUser);

        Map<String, User> connectedUsers = webSocketService.getConnectedUsers();

        assertNotNull(connectedUsers);
        assertEquals(1, connectedUsers.size());
        assertTrue(connectedUsers.containsKey("testuser"));
    }

    @Test
    void testIsUserConnected() {
        assertFalse(webSocketService.isUserConnected("testuser"));

        webSocketService.notifyUserConnected(testUser);

        assertTrue(webSocketService.isUserConnected("testuser"));
    }

    @Test
    void testSendPing() {
        doNothing().when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        webSocketService.sendPing("testuser");

        verify(messagingTemplate).convertAndSendToUser(eq("testuser"), eq("/queue/pong"), any());
    }

    @Test
    void testNotifyUserDisconnected_UserNotFound() {
        when(userService.getUserByUsername("nonexistent")).thenReturn(null);

        webSocketService.notifyUserDisconnected("nonexistent");

        verify(userService, never()).setUserOnlineStatus(anyString(), anyBoolean());
    }

    @Test
    void testMultipleUsersConnected() {
        User user2 = new User();
        user2.setId("USER-002");
        user2.setUsername("user2");

        webSocketService.notifyUserConnected(testUser);
        webSocketService.notifyUserConnected(user2);

        Map<String, User> connectedUsers = webSocketService.getConnectedUsers();

        assertEquals(2, connectedUsers.size());
        assertTrue(webSocketService.isUserConnected("testuser"));
        assertTrue(webSocketService.isUserConnected("user2"));
    }

    @Test
    void testGetConnectedUsers_EmptyInitially() {
        Map<String, User> connectedUsers = webSocketService.getConnectedUsers();

        assertNotNull(connectedUsers);
        assertTrue(connectedUsers.isEmpty());
    }
}
