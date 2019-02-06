package pl.com.testme.testme.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "answers")
@EqualsAndHashCode(callSuper=false)
public class Question extends BaseEntity {

    @Size(min = 10, max = 1000, message = "Minimalna ilość znaków to 10. Maksymalna to 1000")
    private String content;

    @Valid
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "question")
    private List<Answer> answers;

    private boolean singleAnswer;

    private long adminId;

    private long examCreatorId;

    private boolean usedInExam = false;

    private boolean active = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        if (!super.equals(o)) return false;
        Question question = (Question) o;
        return Objects.equals(getId(), question.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }
}
