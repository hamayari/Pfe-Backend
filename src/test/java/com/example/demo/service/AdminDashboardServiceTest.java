package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.enums.ERole;
import com.example.demo.model.*;
import com.example.demo.payload.response.UserDTO;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private ConventionRepository conventionRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private ZoneGeographiqueRepository zoneGeographiqueRepository;
    @Mock private StructureRepository structureRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminDashboardService service;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new Role();
        mockRole.setId("role1");
        mockRole.setName(ERole.ROLE_USER);

        mockUser = new User();
        mockUser.setId("user1");
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setName("Test User");
        mockUser.setPassword("hashedPassword");
        mockUser.setRoles(Set.of(mockRole));
        mockUser.setEnabled(true);
        mockUser.setCreatedAt(Instant.now());
    }

    @Test
    void testGetUserById_Found() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));

        Optional<User> result = service.getUserById("user1");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById("user999")).thenReturn(Optional.empty());

        Optional<User> result = service.getUserById("user999");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser));

        List<User> result = service.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void testCreateUserFromRequest_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setName("New User");
        request.setPassword("password123");
        request.setPhoneNumber("123456789");
        request.setCountry("Tunisia");
        request.setRoles(Arrays.asList("USER"));

        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = service.createUserFromRequest(request);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("hashedPassword", result.getPassword());
        assertTrue(result.isEnabled());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUserFromRequest_WithMultipleRoles() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setEmail("admin@example.com");
        request.setPassword("admin123");
        request.setRoles(Arrays.asList("ADMIN", "USER"));

        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);

        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = service.createUserFromRequest(request);

        assertNotNull(result);
        assertEquals(2, result.getRoles().size());
    }

    @Test
    void testCreateUserFromRequest_NoRoles_UsesDefault() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("norolesuser");
        request.setEmail("noroles@example.com");
        request.setPassword("password123");

        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = service.createUserFromRequest(request);

        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(mockRole));
    }

    @Test
    void testCreateUser() {
        User newUser = new User();
        newUser.setUsername("created");
        newUser.setEmail("created@example.com");

        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = service.createUser(newUser);

        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertTrue(result.isEnabled());
        assertNotNull(result.getAvatar());
    }

    @Test
    void testUpdateUserComplete_Success() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(mockRole));

        User result = service.updateUserComplete("user1", "updated", "updated@example.com", 
            "Updated Name", "987654321", "France", "ADMIN");

        assertNotNull(result);
        assertEquals("updated", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("Updated Name", result.getName());
        assertEquals("987654321", result.getPhoneNumber());
        assertEquals("France", result.getCountry());
    }

    @Test
    void testUpdateUserComplete_UserNotFound() {
        when(userRepository.findById("user999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            service.updateUserComplete("user999", "updated", "updated@example.com", 
                "Updated Name", null, null, null));
    }

    @Test
    void testUpdateUser() {
        User updateData = new User();
        updateData.setUsername("updated");
        updateData.setEmail("updated@example.com");

        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = service.updateUser("user1", updateData);

        assertNotNull(result);
        assertEquals("updated", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void testUpdateUserWithRole() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(mockUser));
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = service.updateUserWithRole("user1", "updated", "updated@example.com", 
            "Updated Name", "ROLE_ADMIN");

        assertNotNull(result);
        assertEquals("updated", result.getUsername());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById("user1");

        service.deleteUser("user1");

        verify(userRepository, times(1)).deleteById("user1");
    }

    @Test
    void testSearchUsers() {
        Page<User> page = new PageImpl<>(Arrays.asList(mockUser));
        when(userRepository.findByUsernameContainingOrEmailContaining(anyString(), anyString(), any(Pageable.class)))
            .thenReturn(page);

        Page<User> result = service.searchUsers("test", Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetUsersByRole() {
        when(userRepository.findByRoles_Name(ERole.ROLE_USER)).thenReturn(Arrays.asList(mockUser));

        List<User> result = service.getUsersByRole(ERole.ROLE_USER);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAdminDashboardStats() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByEnabledTrue()).thenReturn(80L);
        when(userRepository.countByEnabledFalse()).thenReturn(20L);
        when(userRepository.countByCreatedAtAfter(any())).thenReturn(10L);
        when(structureRepository.count()).thenReturn(50L);
        when(applicationRepository.count()).thenReturn(30L);
        when(zoneGeographiqueRepository.count()).thenReturn(20L);
        when(userRepository.findByRoles_Name(any())).thenReturn(new ArrayList<>());
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser));

        Map<String, Object> result = service.getAdminDashboardStats();

        assertNotNull(result);
        assertEquals(100L, result.get("totalUsers"));
        assertEquals(80L, result.get("activeUsers"));
        assertEquals(20L, result.get("pendingUsers"));
        assertEquals(50L, result.get("totalStructures"));
        assertTrue(result.containsKey("usersByRoleChart"));
        assertTrue(result.containsKey("usersEvolution"));
    }

    @Test
    void testGetAdminDashboardStats_WithException() {
        when(userRepository.count()).thenThrow(new RuntimeException("DB Error"));

        Map<String, Object> result = service.getAdminDashboardStats();

        assertNotNull(result);
        assertEquals(0L, result.get("totalUsers"));
    }

    @Test
    void testGetNomenclatures() {
        Application app = new Application();
        app.setId("app1");
        app.setLibelle("App 1");

        when(applicationRepository.findAll()).thenReturn(Arrays.asList(app));
        when(zoneGeographiqueRepository.findAll()).thenReturn(new ArrayList<>());
        when(structureRepository.findAll()).thenReturn(new ArrayList<>());

        List<Object> result = service.getNomenclatures();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetAdminOverview() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser));
        when(applicationRepository.findAll()).thenReturn(new ArrayList<>());
        when(zoneGeographiqueRepository.findAll()).thenReturn(new ArrayList<>());
        when(structureRepository.findAll()).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.getAdminOverview();

        assertNotNull(result);
        assertTrue(result.containsKey("users"));
        assertTrue(result.containsKey("totalUsers"));
        assertTrue(result.containsKey("nomenclatures"));
        assertTrue(result.containsKey("alerts"));
        assertTrue(result.containsKey("auditLogs"));
    }

    @Test
    void testGetAllUserDTOs() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser));

        List<UserDTO> result = service.getAllUserDTOs();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void testMapToUserDTO() {
        UserDTO result = service.mapToUserDTO(mockUser);

        assertNotNull(result);
        assertEquals("user1", result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertTrue(result.isEnabled());
        assertNotNull(result.getRoles());
        assertFalse(result.getRoles().isEmpty());
    }
}
