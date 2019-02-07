package pl.com.testme.testme.bootstrap;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        logger.info("Uruchomiono metodę ładującą dane do bazy.");
        //wersja wrzucona na serwer
        Student kinga = Student.builder().username("1972").studentIndex(1972L).adminId(1972L).password(passwordsEncode("*7Matematyka")).enabled(true).authority("ROLE_ADMIN").build();
        Student tomek = Student.builder().username("2222").studentIndex(6667L).adminId(6667L).password(passwordsEncode("NHormayYuser%$0")).enabled(true).authority("ROLE_USER").build();
        Student karol = Student.builder().username("1111").studentIndex(1111L).password(passwordsEncode("BramBilla(7*")).enabled(true).authority("ROLE_ADMIN").build();

        studentsRepository.save(karol);
        studentsRepository.save(kinga);
        studentsRepository.save(tomek);

    }
    private static final Logger logger = LoggerFactory.getLogger(Loader.class);
}
