package pl.com.testme.testme.security;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
@NoArgsConstructor
public class StudentsSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationMgr) throws Exception {
        authenticationMgr.jdbcAuthentication().dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder())
                .usersByUsernameQuery("SELECT userName, password, enabled FROM student WHERE userName=?")
                .authoritiesByUsernameQuery("SELECT userName, authority FROM student WHERE userName=?");
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/start").permitAll()
                .antMatchers("/main.css").permitAll()
                .antMatchers("/*.js").permitAll()
                .antMatchers("/welcome/").hasAnyRole("USER", "ADMIN")
                .antMatchers("/welcome").hasAnyRole("USER", "ADMIN")
                .antMatchers("/currentExam").hasAnyRole("USER", "ADMIN")
                .antMatchers("/currentExam/*").hasAnyRole("USER", "ADMIN")
                .antMatchers("/summaryOfTheCompletedExam").hasAnyRole("USER", "ADMIN")
                .antMatchers("/grades/**").hasRole("ADMIN")
                .antMatchers("/gradesIsEmpty").hasRole("ADMIN")
                .antMatchers("/addQuestion/*").hasRole("ADMIN")
                .antMatchers("/questionAlreadyExists").hasRole("ADMIN")
                .antMatchers("/listAllStudents/*").hasRole("ADMIN")
                .antMatchers("/deleteAnswer/*").hasRole("ADMIN")
                .antMatchers("/editAnswer/*").hasRole("ADMIN")
                .antMatchers("/deleteQuestion/*").hasRole("ADMIN")
                .antMatchers("/editQuestion/*").hasRole("ADMIN")
                .antMatchers("/deactivateQuestion/*").hasRole("ADMIN")
                .antMatchers("/activateQuestion/*").hasRole("ADMIN")
                .antMatchers("/examDetails/*").hasRole("ADMIN")
                .antMatchers("/showAllExams").hasRole("ADMIN")
                .antMatchers("/showAllExams/").hasRole("ADMIN")
                .antMatchers("/addExam").hasRole("ADMIN")
                .antMatchers("/editExam/*").hasRole("ADMIN")
                .antMatchers("/hidePoints/*").hasRole("ADMIN")
                .antMatchers("/showPoints/*").hasRole("ADMIN")
                .antMatchers("/examAlreadyExists").hasRole("ADMIN")
                .antMatchers("/manual").hasRole("ADMIN")
                .antMatchers("/cannotDelete").hasRole("ADMIN")
                .antMatchers("/deactivateExam/*").hasRole("ADMIN")
                .antMatchers("/activateExam/*").hasRole("ADMIN")
                .antMatchers("/addStudents/*").hasRole("ADMIN")
                .antMatchers("/addStudents").hasRole("ADMIN")
                .antMatchers("/editStudent/**").hasRole("ADMIN")
                .antMatchers("/editAdmin/**").hasRole("ADMIN")
                .antMatchers("/delStudentById/**").hasRole("ADMIN")
                .antMatchers("/**").denyAll()
                .and()
                .logout()
                .and()
                .csrf()
                .disable()
                .formLogin();
    }
}
