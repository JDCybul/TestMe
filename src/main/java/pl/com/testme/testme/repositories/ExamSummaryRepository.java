package pl.com.testme.testme.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pl.com.testme.testme.model.ExamSummary;
import java.util.List;

public interface ExamSummaryRepository extends CrudRepository<ExamSummary, Long> {

    Boolean existsExamSummaryByStudentIndexAndExamCreator_Id(@Param("studentIndex") Long studentIndex, @Param("examCreatorId") Long examCreatorId);

    ExamSummary findByStudentIndexAndExamCreatorId(@Param("studentIndex") Long studentIndex, @Param("examCreatorId") Long examCreatorId);

    List<ExamSummary> findAllByExamCreatorIdAndAdminId(@Param("examId") Long examId, @Param("adminId") Long adminId);

    Boolean existsByExamCreatorId(@Param("examCreatorId")Long examCreatorId);

    Boolean existsByStudentIndexAndGradeNotNull(@Param("studentIndex") Long studentIndex);

    Boolean existsByStudentIndex(@Param("studentIndex") Long studentIndex);

    Boolean existsByAdminId(@Param("adminId") Long adminId);

    List<ExamSummary> findAllByStudentIndex(@Param("studentIndex") Long studentIndex);
}
