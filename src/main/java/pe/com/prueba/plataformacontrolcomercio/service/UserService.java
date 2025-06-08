package pe.com.prueba.plataformacontrolcomercio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.com.prueba.plataformacontrolcomercio.model.User;
import pe.com.prueba.plataformacontrolcomercio.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService implements IUserService
{

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    public User findByEmail(String email)
    {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException(
                        "Usuario no encontrado con email: " + email));
    }

    public Optional<User> findById(Long id)
    {
        return userRepository.findById(id);
    }
}