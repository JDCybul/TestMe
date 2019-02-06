package pl.com.testme.testme.model;

import lombok.*;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;

@Entity
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ExamSummary extends BaseEntity {

    @ManyToOne
    private Student userName;

    @ManyToOne
    private ExamCreator examCreator;

    @ManyToMany
    private List<Answer> answers;

    private float grade;

    private long studentIndex;

    private long adminId;

    private float resultInPercent;

    private int maxScore;

    private int achievedScore;
}

