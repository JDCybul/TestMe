package pl.com.testme.testme.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.com.testme.testme.model.AddStudentsDTO;
import pl.com.testme.testme.model.ExamCreator;
import pl.com.testme.testme.model.Student;
import pl.com.testme.testme.repositories.ExamCreatorRepository;
import pl.com.testme.testme.repositories.ExamSummaryRepository;
import pl.com.testme.testme.repositories.StudentsRepository;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Arrays;

@Slf4j
@Controller
@AllArgsConstructor
public class StudentsController {

    private static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String passwordsEncode(String password) {
        return passwordEncoder.encode(password);
    }

    private ExamCreatorRepository examCreatorRepository;

    private StudentsRepository studentsRepository;

    private ExamSummaryRepository examSummaryRepository;

    @GetMapping("/listAllStudents/{examId}")
    public String listAllStudents(@PathVariable("examId") long examId, Principal principal, Model model) {
        ExamCreator examCreator = examCreatorRepository.findById(examId).
                orElseThrow(() -> new IllegalArgumentException("Niepoprawne id egzaminu" + examId));
        if (examCreator.getAdminId() != Long.valueOf(principal.getName())) {
            return "manual";
        }
        model.addAttribute("examCreator", examCreator);
        return "students/listAllStudents";
    }

    @Transactional
    @PostMapping("/editStudent/{studentId}")
    public String editStudent(@Valid Student student, @PathVariable("studentId") Long studentId) {
        Student student1 = studentsRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Nie istnieje student o id: " + studentId));
        student1.setUsername(student.getUsername());
        long studentIndex = Long.valueOf(student.getUsername());
        student1.setStudentIndex(studentIndex);
        student1.setEnabled(student.isEnabled());
        return "redirect:/showAllExams";
    }

    @GetMapping("/editStudent/{studentId}")
    public String editStudent(@PathVariable("studentId") Long studentId, Principal principal, Model model) {
        Student student = studentsRepository
                .findById(studentId).orElseThrow(() -> new IllegalArgumentException("Nie istnieje student o id: " + studentId));
        if (student.getAdminId() != Long.valueOf(principal.getName()) || examSummaryRepository.existsByStudentIndexAndGradeNotNull(student.getStudentIndex())) {
            model.addAttribute("studentIndex", +student.getStudentIndex());
            return "students/cannotDelete";
        }
        model.addAttribute("student", student);
        return "students/editStudent";
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
            Student student;
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
        if (studentsRepository.findById(studentId).isPresent()) {
            Long studentIndex = studentsRepository.findById(studentId).get().getStudentIndex();
            if (examSummaryRepository.existsExamSummaryByStudentIndexAndExamCreator_Id(studentIndex, examCreatorId)) {
                ExamCreator examCreator = examCreatorRepository.findById(examCreatorId).orElseThrow(() -> new IllegalArgumentException("Nie istnieje egzamin o id " + examCreatorId));
                model.addAttribute("examCreatorId", +examCreatorId);
                model.addAttribute("examTitle", examCreator.getExamTitle());
                model.addAttribute("studentIndex", studentIndex);
                return "students/cannotDelete";
            }
        }
        examCreatorRepository.del(studentId, examCreatorId);
        return "redirect:/listAllStudents/" + examCreatorId;
    }

    @Transactional
    @PostMapping("/editAdmin/{adminId}")
    public String editAdmin(@Valid Student administrator, @PathVariable("adminId") Long adminId) {
        Student admin = studentsRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Nie istnieje admin o id: " + adminId));
        admin.setUsername(administrator.getUsername());
        long adminIndex = Long.valueOf(administrator.getUsername());
        admin.setStudentIndex(adminIndex);
        admin.setEnabled(administrator.isEnabled());
        return "redirect:/welcome";
    }

    @GetMapping("/editAdmin/{adminId}")
    public String editAdmin(@PathVariable("adminId") Long adminId, Principal principal, Model model) {
        if (!isSuperAdmin(principal) || examSummaryRepository.existsByAdminId(adminId)){
            return "cannotDelete";
        }
        Student admin = studentsRepository
                .findById(adminId).orElseThrow(() -> new IllegalArgumentException("Nie istnieje admin o id: " + adminId));
        model.addAttribute("student", admin);
        return "students/editAdmin";
    }

    private boolean isSuperAdmin(Principal principal){
        return studentsRepository.getByUsername(principal.getName()).getUsername().equals("1111");
    }
}

