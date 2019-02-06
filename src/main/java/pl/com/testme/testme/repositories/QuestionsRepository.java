package pl.com.testme.testme.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pl.com.testme.testme.model.Question;
import java.util.List;

public interface QuestionsRepository extends CrudRepository<Question, Long> {

    Boolean existsByContentAndExamCreatorId(@Param("content") String question, @Param("examId") Long examId);

    List<Question> findAllByAdminIdAndExamCreatorId(@Param("adminId")Long adminId, @Param("examCreatorId") long examCreatorId);

    @Modifying
    @Query(value ="UPDATE public.question SET active=true WHERE question.id=:id", nativeQuery = true)
    Integer activate(@Param("id") Long questionId);

    @Modifying
    @Query(value ="UPDATE public.question SET active=false WHERE question.id=:id", nativeQuery = true)
    Integer deactivate(@Param("id") Long questionId);

    @Modifying
    @Query(value = "UPDATE public.question SET used_in_exam=true WHERE question.id=:id", nativeQuery = true)
    Integer usedInExam(@Param("id") Long questionId);
}
