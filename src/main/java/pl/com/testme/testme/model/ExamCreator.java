package pl.com.testme.testme.model;

import lombok.*;
import org.hibernate.validator.constraints.Range;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExamCreator extends BaseEntity implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "exam_id", referencedColumnName = "id")
    private List<Question> questions;

    @ManyToMany
    private List<Student> students;

    @Size(min = 5, max = 50, message = "Nazwa egzaminu to nie mniej niż 5 znaków, nie więcej niż 50.")
    private String examTitle;

    @Range(min = 1L, max = 100L, message = "Próg zaliczenia musi mieścić się między 1 a 100.")
    private float threshold;

    private boolean active;

    private long adminId;

    private boolean showPoints;

    private boolean canEdit = true;

    private boolean canActivate;

    private boolean hasTimeLimit;

    @Range(min = 0, max = 600L, message = "Czas trwania egzaminu musi mieścić się między 0 a 600 minut.")
    private int durationInMinutes;
}
