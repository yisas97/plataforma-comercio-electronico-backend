package pe.com.prueba.plataformacontrolcomercio.service;

import pe.com.prueba.plataformacontrolcomercio.model.User;

import java.util.Optional;

public interface IUserService
{

    public User findByEmail(String email);

    public Optional<User> findById(Long id);
}
