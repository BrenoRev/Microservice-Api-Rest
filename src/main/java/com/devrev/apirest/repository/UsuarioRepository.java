package com.devrev.apirest.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.devrev.apirest.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>{

	@Query(value = "SELECT u FROM Usuario u WHERE u.login = ?1")
	Usuario findUserByLogin(String login);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value = "UPDATE usuario SET token = ?1 WHERE login= ?2")
	void atualizaTokenUser(String token, String login);
	
	@Query(value = "SELECT u FROM Usuario u WHERE LOWER(u.nome) LIKE (%?1%)")
	List<Usuario> findUserByName(String name);
	
	@Query(value="SELECT constraint_name from information_schema.constraint_column_usage  where table_name = 'usuarios_role' and column_name = 'role_id' and constraint_name <> 'unique_role_user';", nativeQuery = true)
	String consultaConstraintRole();
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value = "insert into usuarios_role (usuario_id, role_id) values(?1, (select id from role where nome_role = 'USER')); ")
	void insereAcessoRolePadrao(Long idUser);
	
}
