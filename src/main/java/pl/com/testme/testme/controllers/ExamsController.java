package pl.com.testme.testme.controllers;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import pl.com.testme.testme.model.AddStudentsDTO;
import pl.com.testme.testme.model.ExamCreator;
import pl.com.testme.testme.model.Student;
import pl.com.testme.testme.repositories.ExamCreatorRepository;
import pl.com.testme.testme.repositories.ExamSummaryRepository;
import pl.com.testme.testme.repositories.StudentsRepository;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;

@Controller
@AllArgsConstructor
public class ExamsController {

    private static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String passwordsEncode(String password) {
        return passwordEncoder.encode(password);
    }

    private ExamCreatorRepository examCreatorRepository;

    private StudentsRepository studentsRepository;

    private ExamSummaryRepository examSummaryRepository;

    @GetMapping("/showAllExams")
    public String showAllExams(Model model, Principal principal) {
        model.addAttribute("exams", examCreatorRepository.findAllByAdminId(Long.valueOf(principal.getName())));
        return "exams/showAllExams";
    }

    @GetMapping("examDetails/{examId}")
    public String examDetails(Model model, @PathVariable("examId") Long examId, Principal principal) {
        ExamCreator exam = examCreatorRepository.findByAdminIdAndId(Long.valueOf(principal.getName()), examId);
        model.addAttribute("exam", exam);
        model.addAttribute("howMany", exam.getStudents().size());
        model.addAttribute("questions", exam.getQuestions());
        return "exams/examDetails";
    }

    @RequestMapping("/addExam")
    public String addExam(Model model) {
        ExamCreator examCreator = new ExamCreator();
        model.addAttribute("examCreator", examCreator);
        return "exams/addExam";
    }

    @RequestMapping(value = "addExam", method = RequestMethod.POST)
    public String saveExam(@Valid ExamCreator examCreator, Errors errors, Model model, Principal principal) {
        if (examCreatorRepository.count() >= 500) {
            return "exams/toManyExams";
        }
        if (errors.hasErrors()) {
            return "exams/addExam";
        }
        if (examCreatorRepository.existsByExamTitle(examCreator.getExamTitle())) {
            model.addAttribute("examTitle", examCreator.getExamTitle());
            return "exams/examAlreadyExists";
        } else {
            model.addAttribute("examTitle", examCreator.getExamTitle());
            model.addAttribute("threshold", examCreator.getThreshold());
            examCreator.setAdminId(Long.valueOf(principal.getName()));
            examCreatorRepository.save(examCreator);
            return "exams/addingExamSuccessful";
        }
    }

    @Transactional
    @RequestMapping("deactivateExam/{id}")
    public String deactivateExam(@PathVariable("id") long id, Principal principal) {
        if (examCreatorRepository.existsByAdminId(Long.valueOf(principal.getName()))) {
            examCreatorRepository.deactivate(id);
            return "redirect:/showAllExams";
        }
        return "manual";
    }

    @Transactional
    @RequestMapping("activateExam/{id}")
    public String activateExam(@PathVariable("id") long id, Principal principal) {

        if ((examCreatorRepository.findById(id).get().getQuestions().size() == 0)
                || !examCreatorRepository.existsByAdminId(Long.valueOf(principal.getName()))
        ) {
            return "manual";
        }
        examCreatorRepository.activate(id);
        return "redirect:/showAllExams";
    }

    @Transactional
    @RequestMapping("showPoints/{id}")
    public String showPoints(@PathVariable("id") long id, Principal principal) {
        if (examCreatorRepository.existsByAdminId(Long.valueOf(principal.getName()))) {
            examCreatorRepository.showPoints(id);
            return "redirect:/showAllExams";
        }
        return "manual";
    }

    @Transactional
    @RequestMapping("hidePoints/{id}")
    public String hidePoints(@PathVariable("id") long id, Principal principal) {
        if (examCreatorRepository.existsByAdminId(Long.valueOf(principal.getName()))) {
            examCreatorRepository.hidePoints(id);
            return "redirect:/showAllExams";
        }
        return "manual";
    }

    @RequestMapping("/addStudents/{examId}")
    public String addStudents(Model model, @PathVariable("examId") long examId, Principal principal) {
        if (!examCreatorRepository.existsByAdminId(Long.valueOf(principal.getName()))
        ) {
            return "manual";
        }
        AddStudentsDTO addStudentsDTO = new AddStudentsDTO(examId);
        model.addAttribute("exam", examCreatorRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Nie można znaleźć egzaminu o id: " + examId)));
        model.addAttribute("addStudentsDTO", addStudentsDTO);
        return "exams/addStudents";
    }
    @Transactional
    @PostMapping("/addStudents")
    public String addStudents(@Valid AddStudentsDTO addStudentsDTO, Model model, Principal principal, Errors errors) {
        ExamCreator examCreator = examCreatorRepository.findById(addStudentsDTO.getExamId())
                .orElseThrow(() -> new IllegalArgumentException("Nie istnieje egzamin o Id: " + addStudentsDTO.getExamId()));
        model.addAttribute("addStudentsDTO", addStudentsDTO);
        if (errors.hasErrors()) {
            return "manual";
        }
        Arrays.stream(addStudentsDTO.getIndexes().split(",")).forEach(index -> {
            Student student = null;
            if (!studentsRepository.existsUsersByUsername(index)) {
                student = Student.builder().canEdit(true).adminId(Long.valueOf(principal.getName()))
                        .password(passwordsEncode("Kfiatki2020")).studentIndex(Long.valueOf(index)).username(index).enabled(true).authority("ROLE_USER").build();

                studentsRepository.save(student);
            } else {
                student = studentsRepository.getByUsername(index);
            }
            if (!examCreator.getStudents().contains(student)) {
                examCreator.getStudents().add(student);
            }
        });
        examCreatorRepository.save(examCreator);
        return "redirect:/showAllExams";
    }

    @Transactional
    @RequestMapping("/delStudentById/{studentId}/{examCreatorId}")
    public String delStudentbyId(@PathVariable("studentId") long studentId, @PathVariable("examCreatorId") long examCreatorId, Model model) {
        Long studentIndex = studentsRepository.findById(studentId).get().getStudentIndex();
        System.out.println(examCreatorRepository.findById(examCreatorId) + " istnieje");
        if (examSummaryRepository.existsExamSummaryByStudentIndexAndExamCreator_Id(studentIndex, examCreatorId)) {
            ExamCreator examCreator = examCreatorRepository.findById(examCreatorId).orElseThrow(() -> new IllegalArgumentException("Nie istnieje egzamin o id " + examCreatorId));
            model.addAttribute("examCreatorId", +examCreatorId);
            model.addAttribute("examTitle", examCreator.getExamTitle());
            model.addAttribute("studentIndex", +studentIndex);
            return "students/cannotDelete";
        }
        examCreatorRepository.del(studentId, examCreatorId);
        return "redirect:/listAllStudents/" + examCreatorId;
    }

    @Transactional
    @PostMapping("/editExam/{examId}")
    public String editAnswer(@Valid ExamCreator examCreator, @PathVariable("examId") Long examId) {
        ExamCreator examCreator1 = examCreatorRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Nie istnieje egzamin o id: " + examId));
        examCreator1.setDurationInMinutes(examCreator.getDurationInMinutes());
        examCreator1.setExamTitle(examCreator.getExamTitle());
        examCreator1.setThreshold(examCreator.getThreshold());
        examCreator1.setShowPoints(examCreator.isShowPoints());
        examCreator1.setHasTimeLimit(examCreator.isHasTimeLimit());
        return "redirect:/showAllExams";
    }

    @GetMapping("/editExam/{examId}")
    public String editAnswer(@PathVariable("examId") Long examId, Principal principal, Model model) {
        ExamCreator examCreator1 = examCreatorRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Nie istnieje egzamin o id: " + examId));

        if (examCreator1.getAdminId() != Long.valueOf(principal.getName()) || examSummaryRepository.existsByExamCreatorId(examId)) {
            return "cannotDelete";
        }
        model.addAttribute("canEdit", true);
        model.addAttribute("examCreator", examCreator1);
        return "exams/editExam";
    }
}

