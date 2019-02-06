package pl.com.testme.testme.bootstrap;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.testme.testme.model.Student;
import pl.com.testme.testme.repositories.StudentsRepository;
import static pl.com.testme.testme.controllers.ExamsController.passwordsEncode;

@Component
@AllArgsConstructor
public class Loader {

    private StudentsRepository studentsRepository;

    @Transactional
    public void loadDataToDB() {

        Student kinga = Student.builder().username("1972").studentIndex(1972L).adminId(1972L).password(passwordsEncode("1972")).enabled(true).authority("ROLE_ADMIN").build();
        Student tomek = Student.builder().username("6667").studentIndex(6667L).adminId(6667L).password(passwordsEncode("6667")).enabled(true).authority("ROLE_USER").build();
        Student karol = Student.builder().username("1111").studentIndex(1111L).password(passwordsEncode("1111")).enabled(true).authority("ROLE_ADMIN").build();

        studentsRepository.save(karol);
        studentsRepository.save(kinga);
        studentsRepository.save(tomek);

    }
}
