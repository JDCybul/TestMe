package pl.com.testme.testme.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pl.com.testme.testme.model.ExamCreator;
import java.util.List;

public interface ExamCreatorRepository extends CrudRepository<ExamCreator, Long> {

    Boolean existsByExamTitle(@Param("examTitle") String examTitle);

    @Modifying
    @Query(value = "UPDATE public.exam_creator SET can_edit=false WHERE id =:id", nativeQuery = true)
    Integer cantEditSetToFalse(@Param("id")Long id);

    @Modifying
    @Query(value = "UPDATE public.exam_creator SET active=false WHERE id =:id", nativeQuery = true)
    Integer deactivate(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE public.exam_creator SET active=true WHERE id =:id", nativeQuery = true)
    Integer activate(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE public.exam_creator SET show_points=true WHERE id =:id", nativeQuery = true)
    Integer showPoints(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE public.exam_creator SET show_points=false WHERE id =:id", nativeQuery = true)
    Integer hidePoints(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM public.exam_creator_students WHERE students_id=:students_id AND exam_creator_id=:exam_creator_id", nativeQuery = true)
    int del(@Param("students_id") Long id, @Param("exam_creator_id") Long exam_creator_id);

    List<ExamCreator> findByActive(boolean active);

    List<ExamCreator> findAllByAdminId(@Param("admin_id") Long admin_id);

    ExamCreator findByAdminIdAndId(@Param("admin_id") Long admin_id, @Param("examId") Long examId);

    Boolean existsByAdminIdAndId(@Param("admin_id") Long admin_id, @Param("exam_creator_id") Long exam_creator_id);

    Boolean existsByAdminId(@Param("adminId") Long adminId);

    ExamCreator findByQuestionsId(@Param("questionId")Long id);

}
