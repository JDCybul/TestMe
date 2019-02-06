package pl.com.testme.testme.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pl.com.testme.testme.model.Student;

import java.util.List;

public interface StudentsRepository extends CrudRepository<Student, Long> {

    Student getByUsername(String username);

    Boolean existsUsersByUsername(String username);

    Boolean existsByAuthorityAndUsername(@Param("ROLE_ADMIN")String ROLE_ADMIN, @Param("username")String username);

    List<Student> findAllByAuthority(@Param("ROLE_ADMIN")String role_admin);

    @Modifying
    @Query(value = "UPDATE public.student SET  can_edit=false WHERE username=:username", nativeQuery = true)
    Integer setCanEditToFalse(@Param("username") String username);
}
