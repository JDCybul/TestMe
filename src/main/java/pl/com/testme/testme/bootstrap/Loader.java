package pl.com.testme.testme.bootstrap;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.testme.testme.model.Student;
import pl.com.testme.testme.repositories.StudentsRepository;

import static pl.com.testme.testme.controllers.StudentsController.passwordsEncode;

@Slf4j
@Component
@AllArgsConstructor
public class Loader {

    private StudentsRepository studentsRepository;

    @Transactional
    public void loadDataToDB() {
        log.info("Uruchomiono metodę ładującą dane do bazy");
        Student kinga = Student.builder().username("1972").studentIndex(1972L).adminId(1972L).password(passwordsEncode("*7Matematyka")).enabled(true).authority("ROLE_ADMIN").build();
                Student karol = Student.builder().username("1111").studentIndex(1111L).password(passwordsEncode("BramBilla(7*")).enabled(true).authority("ROLE_ADMIN").build();

        studentsRepository.save(karol);
        studentsRepository.save(kinga);


    }
}
