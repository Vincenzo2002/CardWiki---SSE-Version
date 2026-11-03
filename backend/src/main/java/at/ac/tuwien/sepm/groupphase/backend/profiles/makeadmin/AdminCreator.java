package at.ac.tuwien.sepm.groupphase.backend.profiles.makeadmin;

import at.ac.tuwien.sepm.groupphase.backend.entity.User;
import at.ac.tuwien.sepm.groupphase.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;

@Component
@Profile("makeAdmin")
public class AdminCreator {
    private final String username;

    private final UserRepository userRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public AdminCreator(final UserRepository userRepository, @Value("${admin-username}") final String username) {
        this.userRepository = userRepository;
        this.username = username;
    }

    @PostConstruct
    private void makeAdmin() {
        userRepository.findByUsername(username).ifPresentOrElse(
            (final User user) -> {
                user.setAdmin(true);
                userRepository.save(user);
            },
            () -> LOGGER.error("Could not find user {}", username)
        );
    }
}
