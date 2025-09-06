package com.tiendapc.repository;

import com.tiendapc.model.Role;
import com.tiendapc.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Búsquedas básicas
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Roles / estado
    List<Usuario> findByRole(Role role);
    List<Usuario> findByEnabledTrue();
    List<Usuario> findByRoleAndEnabled(Role role, Boolean enabled);

    long countByRole(Role role);

    // Login: username o email
    @Query("SELECT u FROM Usuario u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<Usuario> findByUsernameOrEmail(@Param("identifier") String identifier);

    // Búsqueda simplificada
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Usuario> buscarPorNombre(@Param("searchTerm") String searchTerm);
}