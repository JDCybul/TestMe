package pl.com.testme.testme.controllers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.com.testme.testme.model.ExamCreator;
import pl.com.testme.testme.repositories.ExamCreatorRepository;
import pl.com.testme.testme.repositories.ExamSummaryRepository;
import pl.com.testme.testme.repositories.StudentsRepository;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Controller
public class WelcomeController {

    private ExamCreatorRepository examCreatorRepository;

    private ExamSummaryRepository examSummaryRepository;

    private StudentsRepository studentsRepository;

    @RequestMapping({"/", "/start"})
    public String start() {

        return "welcome/start";
    }

    @RequestMapping("/welcome")
    public String getSelected(Model model, Principal principal) {
        Long studentIndex = Long.valueOf(principal.getName());
        List<ExamCreator> examCreators = examCreatorRepository.findByActive(true);
        model.addAttribute("studentIndex", studentIndex);
        Set<ExamCreator> examCreatorsStudents = new HashSet<>();
        examCreators.forEach(examCreator -> {
            for (int i = 0; i < examCreator.getStudents().size(); i++) {
                if ((examCreator.getStudents().get(i).getStudentIndex() == Long.valueOf(principal.getName())
                        && !examSummaryRepository.existsExamSummaryByStudentIndexAndExamCreator_Id(studentIndex, examCreator.getId()))
                        || examCreator.getAdminId() == Long.valueOf(principal.getName())) {
                    examCreatorsStudents.add(examCreator);
                    model.addAttribute("examCreators", examCreatorsStudents);
                }
            }
        });
        model.addAttribute("isEmpty", examCreatorsStudents.isEmpty());
        model.addAttribute("existByExamSummary", examSummaryRepository.existsByStudentIndex(studentIndex));
        model.addAttribute("examSummaries", examSummaryRepository.findAllByStudentIndex(Long.valueOf(principal.getName())));
        model.addAttribute("isSuperAdmin", principal.getName().equals("1111"));
        model.addAttribute("admins", studentsRepository.findAllByAuthority("ROLE_ADMIN"));
        System.out.println(studentsRepository.findAllByAuthority("ROLE_ADMIN"));
        return "welcome/welcome";
    }
}


