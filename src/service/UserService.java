package service;

import audit.AuditService;
import exception.DuplicateEntityException;
import exception.NotFoundException;
import exception.ValidationException;
import model.User;
import repository.UserRepository;

public class UserService {

    private final UserRepository userRepository = UserRepository.getInstance();
    private final AuditService audit = AuditService.getInstance();

    public User register(String nume, String email, String password) {
        if (nume == null || nume.trim().isEmpty()) {
            throw new ValidationException("Numele nu poate fi gol.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email-ul nu poate fi gol.");
        }
        if (password == null || password.length() < 6) {
            throw new ValidationException("Parola trebuie sa aiba minim 6 caractere.");
        }
        if (userRepository.findByEmail(email.trim()).isPresent()) {
            throw new DuplicateEntityException("Email-ul este deja inregistrat.");
        }

        int id = userRepository.nextId();
        User user = new User(id, nume.trim(), email.trim(), password);
        userRepository.save(user);
        audit.log("registerUser");
        return user;
    }

    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Introdu email-ul.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Introdu parola.");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new NotFoundException("Email-ul nu este inregistrat."));

        if (!user.getPassword().equals(password)) {
            throw new ValidationException("Parola este incorecta.");
        }

        audit.log("loginUser");
        return user;
    }
}
