package pl.com.testme.testme.model;

import lombok.Data;
import javax.validation.constraints.Pattern;

@Data
public class AddStudentsDTO extends BaseEntity{

    public AddStudentsDTO(long examId) {
        this.examId = examId;
    }

    private long examId;

    @Pattern(regexp = "[0-9,]*")
    private String indexes;
}