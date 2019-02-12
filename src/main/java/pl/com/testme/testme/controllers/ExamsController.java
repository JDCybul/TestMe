package pl.com.testme.testme.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import pl.com.testme.testme.model.ExamCreator;
import pl.com.testme.testme.repositories.ExamCreatorRepository;
import pl.com.testme.testme.repositories.ExamSummaryRepository;
import javax.validation.Valid;
import java.security.Principal;

@Slf4j
@Controller
@AllArgsConstructor
public class ExamsController {

	private ExamCreatorRepository examCreatorRepository;

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
		model.addAttribute("hasTimeLimit", exam.isHasTimeLimit());
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
	@RequestMapping("activateExam/{examId}")
	public String activateExam(@PathVariable("examId") long examId, Principal principal) {
		if (examCreatorRepository.findById(examId).isPresent()) {
			if ((examCreatorRepository.findById(examId).get().getQuestions().size() == 0)
					|| !examCreatorRepository.existsByAdminId(Long.valueOf(principal.getName()))
			) {
				return "manual";
			}
		}
		examCreatorRepository.activate(examId);
		return "redirect:/showAllExams";
	}

	@Transactional
	@RequestMapping("showPoints/{examId}")
	public String showPoints(@PathVariable("examId") long id, Principal principal) {
		if (examCreatorRepository.existsByAdminId(Long.valueOf(principal.getName()))) {
			examCreatorRepository.showPoints(id);
			return "redirect:/showAllExams";
		}
		return "manual";
	}

	@Transactional
	@RequestMapping("hidePoints/{examId}")
	public String hidePoints(@PathVariable("examId") long id, Principal principal) {
		if (examCreatorRepository.existsByAdminId(Long.valueOf(principal.getName()))) {
			examCreatorRepository.hidePoints(id);
			return "redirect:/showAllExams";
		}
		return "manual";
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

