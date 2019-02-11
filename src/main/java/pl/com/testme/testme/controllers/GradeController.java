package pl.com.testme.testme.controllers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.com.testme.testme.model.ExamCreator;
import pl.com.testme.testme.model.ExamSummary;
import pl.com.testme.testme.repositories.ExamCreatorRepository;
import pl.com.testme.testme.repositories.ExamSummaryRepository;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@AllArgsConstructor
public class GradeController {

    private ExamSummaryRepository examSummaryRepository;

    private ExamCreatorRepository examCreatorRepository;

    @RequestMapping("grades/{examId}/{studentIndex}")
    public String gradeTestController(@PathVariable("examId") long examId, @PathVariable("studentIndex") long studentIndex, Model model, Principal principal) {
        ExamCreator examCreator = examCreatorRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono egzaminu o danym id" + examId));
        if (examCreator.getAdminId() != Long.valueOf(principal.getName())) {
            return "manual";
        }
        model.addAttribute("examTitle", examCreator.getExamTitle());
        if (!examSummaryRepository.existsByExamCreatorId(examId)){
            return "grades/gradesIsEmpty";
        }
        model.addAttribute("studentIndex", studentIndex);
        int howManyStudentsShouldTakeTheExam = examCreator.getStudents().size();
        int howManyStudentsFinishedExam = findAllByExamCreatorIdAndAdminId(examId, Long.valueOf(principal.getName()));
        model.addAttribute("howManyStudentsShouldTakeTheExam", howManyStudentsShouldTakeTheExam);
        model.addAttribute("howManyStudentsFinishedExam", howManyStudentsFinishedExam);
        Set<Long> indexesOfStudentsWhoCanTakeExam = indexesOfStudentsWhoHaveNotFinishedYet(examCreator);
        List<ExamSummary> examSummaries = examSummaryRepository.findAllByExamCreatorIdAndAdminId(examId, Long.valueOf(principal.getName()));
        Set<Long> indexesOfStudentsWhoHaveAlreadyFinished = new HashSet<>();
        for (ExamSummary examSummary : examSummaries) {
            indexesOfStudentsWhoHaveAlreadyFinished.add(examSummary.getStudentIndex());
        }
        indexesOfStudentsWhoCanTakeExam.removeAll(indexesOfStudentsWhoHaveAlreadyFinished);
        Set<Long> theyHaveNotFinishedYet = new HashSet<>(indexesOfStudentsWhoCanTakeExam);
        model.addAttribute("theyHaveNotFinishedYet", theyHaveNotFinishedYet);
        model.addAttribute("examSummaries", examSummaries);
        model.addAttribute("howManyStudentsShouldTakeTheExam", examCreator.getStudents().size());
        model.addAttribute("howManyStudentsFinishedExam", findAllByExamCreatorIdAndAdminId(examId, Long.valueOf(principal.getName())));
        return "grades/grades";
    }

    private int findAllByExamCreatorIdAndAdminId(Long examId, Long adminId) {
        if (examSummaryRepository.findAllByExamCreatorIdAndAdminId(examId, adminId) == null) {
            return 0;
        }
        return examSummaryRepository.findAllByExamCreatorIdAndAdminId(examId, adminId).size();
    }

    private Set<Long> indexesOfStudentsWhoHaveNotFinishedYet(ExamCreator examCreator) {
        Set<Long> indexesOfStudentsWhoHaveNotFinishedYet = new HashSet<>();
        for (int i = 0; i < examCreator.getStudents().size(); i++) {
            indexesOfStudentsWhoHaveNotFinishedYet.add(examCreator.getStudents().get(i).getStudentIndex());
        }
        return indexesOfStudentsWhoHaveNotFinishedYet;
    }
}

