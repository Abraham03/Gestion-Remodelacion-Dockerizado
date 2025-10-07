package com.gestionremodelacion.gestion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestionremodelacion.gestion.model.Permission;
import com.gestionremodelacion.gestion.model.Permission.PermissionScope;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    @Override
    List<Permission> findAll(Sort sort);

    Boolean existsByName(String name);

    List<Permission> findByScope(PermissionScope scope, Sort sort);

    Page<Permission> findByScope(PermissionScope scope, Pageable pageable);

}
