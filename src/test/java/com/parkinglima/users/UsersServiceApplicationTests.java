
package com.parkinglima.users;

import com.parkinglima.users.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=" +
    "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
    "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
class UsersServiceApplicationTests {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EntityManager entityManager;

    @Test
    void contextLoads() {
    }
}
