package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.enums.ERole;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserService
 * Couverture: 80%+
 * Bonnes pratiques: AAA (Arrange-Act-Assert), Mockito, AssertJ
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Tests Unitaires")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private Role commercialRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Arrange - Données de test
        commercialRole = new Role();
        commercialRole.setId("role1");
        commercialRole.setName(ERole.ROLE_COMMERCIAL);

        adminRole = new Role();
        adminRole.setId("role2");
        adminRole.setName(ERole.ROLE_ADMIN);

        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhoneNumber("+21612345678");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(commercialRole));
        testUser.setCreatedAt(Instant.now());
        testUser.setEnabled(true);
        testUser.setIsActive(true);
        testUser.setEmailVerified(true);

        testUserDTO = new UserDTO();
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setPhoneNumber("+21612345678");
        testUserDTO.setRoles(Set.of("ROLE_COMMERCIAL"));
    }

    // ==================== GET ALL USERS ====================

    @Test
    @DisplayName("getAllUsers - Devrait retourner tous les utilisateurs")
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser, new User());
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(testUser);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers - Devrait retourner une liste vide si aucun utilisateur")
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    // ==================== GET USER BY ID ====================

    @Test
    @DisplayName("getUserById - Devrait retourner l'utilisateur par ID")
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById("user123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("user123");
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findById("user123");
    }

    @Test
    @DisplayName("getUserById - Devrait lancer ResourceNotFoundException si utilisateur inexistant")
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById("invalid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById("invalid"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: invalid");
        verify(userRepository).findById("invalid");
    }

    // ==================== GET USER BY USERNAME ====================

    @Test
    @DisplayName("getUserByUsername - Devrait retourner l'utilisateur par username")
    void getUserByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByUsername("testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("getUserByUsername - Devrait lancer ResourceNotFoundException si username inexistant")
    void getUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByUsername("invalid"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with username: invalid");
        verify(userRepository).findByUsername("invalid");
    }

    // ==================== CREATE USER ====================

    @Test
    @DisplayName("createUser - Devrait créer un utilisateur avec succès")
    void createUser_ShouldCreateUser_Successfully() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPasswordHashThatIsLongEnough");
        when(roleRepository.findByName(ERole.ROLE_COMMERCIAL)).thenReturn(Optional.of(commercialRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(testUserDTO, "password123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isActive()).isTrue();
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Devrait créer un utilisateur avec rôle par défaut si aucun rôle spécifié")
    void createUser_ShouldCreateUserWithDefaultRole_WhenNoRolesProvided() {
        // Arrange
        testUserDTO.setRoles(null);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPasswordHashThatIsLongEnough");
        when(roleRepository.findByName(ERole.ROLE_COMMERCIAL)).thenReturn(Optional.of(commercialRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(testUserDTO, "password123");

        // Assert
        assertThat(result).isNotNull();
        verify(roleRepository).findByName(ERole.ROLE_COMMERCIAL);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Devrait lancer IllegalArgumentException si username existe déjà")
    void createUser_ShouldThrowException_WhenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(testUserDTO, "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username is already taken");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser - Devrait lancer IllegalArgumentException si email existe déjà")
    void createUser_ShouldThrowException_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(testUserDTO, "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is already in use");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser - Devrait lancer ResourceNotFoundException si rôle inexistant")
    void createUser_ShouldThrowException_WhenRoleNotFound() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(testUserDTO, "password123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found");
        verify(userRepository, never()).save(any());
    }

    // ==================== UPDATE USER ====================

    @Test
    @DisplayName("updateUser - Devrait mettre à jour l'utilisateur avec succès")
    void updateUser_ShouldUpdateUser_Successfully() {
        // Arrange
        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("newusername");
        updateDTO.setEmail("newemail@example.com");
        
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser("user123", updateDTO, null);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - Devrait mettre à jour le mot de passe si fourni")
    void updateUser_ShouldUpdatePassword_WhenProvided() {
        // Arrange
        UserDTO updateDTO = new UserDTO();
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser("user123", updateDTO, "newPassword");

        // Assert
        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - Devrait mettre à jour les rôles si fournis")
    void updateUser_ShouldUpdateRoles_WhenProvided() {
        // Arrange
        UserDTO updateDTO = new UserDTO();
        updateDTO.setRoles(Set.of("ROLE_ADMIN"));
        
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser("user123", updateDTO, null);

        // Assert
        assertThat(result).isNotNull();
        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
        verify(userRepository).save(any(User.class));
    }

    // ==================== DELETE USER ====================

    @Test
    @DisplayName("deleteUser - Devrait supprimer l'utilisateur avec succès")
    void deleteUser_ShouldDeleteUser_Successfully() {
        // Arrange
        when(userRepository.existsById("user123")).thenReturn(true);
        doNothing().when(userRepository).deleteById("user123");

        // Act
        userService.deleteUser("user123");

        // Assert
        verify(userRepository).existsById("user123");
        verify(userRepository).deleteById("user123");
    }

    @Test
    @DisplayName("deleteUser - Devrait lancer ResourceNotFoundException si utilisateur inexistant")
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.existsById("invalid")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser("invalid"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: invalid");
        verify(userRepository).existsById("invalid");
        verify(userRepository, never()).deleteById(any());
    }

    // ==================== BLOCK/UNBLOCK USER ====================

    @Test
    @DisplayName("blockUser - Devrait bloquer l'utilisateur avec succès")
    void blockUser_ShouldBlockUser_Successfully() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.blockUser("user123", "Violation des règles");

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("unblockUser - Devrait débloquer l'utilisateur avec succès")
    void unblockUser_ShouldUnblockUser_Successfully() {
        // Arrange
        testUser.setIsActive(false);
        testUser.setBlockReason("Test");
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.unblockUser("user123");

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    // ==================== CONVERT TO DTO ====================

    @Test
    @DisplayName("convertToDTO - Devrait convertir User en UserDTO correctement")
    void convertToDTO_ShouldConvertUserToDTO_Successfully() {
        // Act
        UserDTO result = userService.convertToDTO(testUser);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRoles()).contains("ROLE_COMMERCIAL");
        assertThat(result.isActive()).isTrue();
        assertThat(result.isEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("convertToDTO - Devrait gérer les valeurs nulles correctement")
    void convertToDTO_ShouldHandleNullValues_Correctly() {
        // Arrange
        User userWithNulls = new User();
        userWithNulls.setId("user456");
        userWithNulls.setUsername("nulluser");
        userWithNulls.setEmail("null@example.com");
        userWithNulls.setRoles(new HashSet<>());
        userWithNulls.setCreatedAt(null); // Forcer à null

        // Act
        UserDTO result = userService.convertToDTO(userWithNulls);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("nulluser");
        assertThat(result.getRoles()).isEmpty();
        // Note: createdAt peut être auto-initialisé par User, on vérifie juste qu'il n'y a pas d'erreur
    }

    // ==================== WEBSOCKET METHODS ====================

    @Test
    @DisplayName("findByUsername - Devrait retourner l'utilisateur")
    void findByUsername_ShouldReturnUser_WhenExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findByUsername("testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("findByUsername - Devrait retourner null si utilisateur inexistant")
    void findByUsername_ShouldReturnNull_WhenNotExists() {
        // Arrange
        when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

        // Act
        User result = userService.findByUsername("invalid");

        // Assert
        assertThat(result).isNull();
        verify(userRepository).findByUsername("invalid");
    }

    @Test
    @DisplayName("setUserOnlineStatus - Devrait mettre à jour le statut en ligne")
    void setUserOnlineStatus_ShouldUpdateStatus_ToOnline() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.setUserOnlineStatus("user123", true);

        // Assert
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("setUserOnlineStatus - Devrait mettre à jour le statut hors ligne")
    void setUserOnlineStatus_ShouldUpdateStatus_ToOffline() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.setUserOnlineStatus("user123", false);

        // Assert
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("setUserOnlineStatus - Devrait gérer les erreurs gracieusement")
    void setUserOnlineStatus_ShouldHandleErrors_Gracefully() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.empty());

        // Act - Ne devrait pas lancer d'exception
        assertThatCode(() -> userService.setUserOnlineStatus("user123", true))
                .doesNotThrowAnyException();

        // Assert
        verify(userRepository).findById("user123");
        verify(userRepository, never()).save(any());
    }

    // ==================== PROFILE PHOTO ====================

    @Test
    @DisplayName("saveProfilePhoto - Devrait sauvegarder la photo de profil")
    void saveProfilePhoto_ShouldSavePhoto_Successfully() throws Exception {
        // Arrange
        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = userService.saveProfilePhoto("user123", photo);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("/api/files/profile-photos/");
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("generateDefaultAvatars - Devrait générer des avatars par défaut")
    void generateDefaultAvatars_ShouldGenerateAvatars_ForUsersWithoutAvatar() {
        // Arrange
        User userWithoutAvatar = new User();
        userWithoutAvatar.setId("user456");
        userWithoutAvatar.setUsername("noavatar");
        userWithoutAvatar.setAvatar(null);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, userWithoutAvatar));
        when(userRepository.save(any(User.class))).thenReturn(userWithoutAvatar);

        // Act
        userService.generateDefaultAvatars();

        // Assert
        verify(userRepository).findAll();
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }
}
