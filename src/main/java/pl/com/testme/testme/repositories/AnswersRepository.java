package pl.com.testme.testme.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pl.com.testme.testme.model.Answer;
import java.util.List;

public interface AnswersRepository extends CrudRepository<Answer, Long> {

    @Query(value = "SELECT value FROM public.answer Where  question_id =:questionId", nativeQuery = true)
    List<Integer> value(@Param("questionId") Long l);

    @Query(value = "SELECT value FROM public.answer Where question_id =:id and value>0", nativeQuery = true)
    List<Integer> valueCounter(@Param("id") Long l);

    @Query(value = "SELECT value FROM public.answer Where value>0 AND exam_creator_id =:examCreatorId" , nativeQuery = true)
    List<Integer> positiveValue(@Param("examCreatorId")Long examCreatorId);
}
