package pl.com.testme.testme;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import pl.com.testme.testme.bootstrap.Loader;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "pl.com.testme.testme.repositories")
@AllArgsConstructor
public class TestMeApp implements CommandLineRunner {

    private Loader loader;

    public static void main(String[] args) {

        SpringApplication.run(TestMeApp.class, args);
    }

    @Override
    public void run(String... args) {
//      loader.loadDataToDB();
    }

}
