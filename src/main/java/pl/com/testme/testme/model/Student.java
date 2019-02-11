package pl.com.testme.testme.model;

import lombok.*;
import javax.persistence.Entity;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Student extends BaseEntity {

    @Size(min = 4, max = 15, message = "Numer indeksu to nie mniej niż 4 cyfry a mniej niż 15.")
    @Digits(integer = 1000, fraction = 0, message = "Numer indeksu to tylko liczby. To pole nie przyjmie nic innego.")
    private String username;

    @Size(min = 6, max = 128, message = "Pole ma mieć więcej niż 6 znaków a mniej niż 128.")
    private String password;

    private long studentIndex;

    private boolean enabled;

    private boolean canEdit;

    private String authority = "ROLE_USER";

    private long adminId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        if (!super.equals(o)) return false;
        Student student = (Student) o;
        return username.equals(student.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username);
    }
}
