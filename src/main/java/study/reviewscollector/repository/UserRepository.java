package study.reviewscollector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.reviewscollector.entity.User;


public interface UserRepository extends JpaRepository<User, Long> {
    User getUserByLogin(String login);
}
