package pl.com.testme.testme.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Answer extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    private Question question;

    @Size(min = 3, max = 500, message = "Odpowiedź ma mieć minimum 3 znaki, maximum 500")
    private String content;

    @Digits(integer = 1, fraction = 0, message = "Minimalna wartość odpowiedzi to -9 a maksymalna 9")
    private int value;

    private long adminId;

    private long examCreatorId;
}

