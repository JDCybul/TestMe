package pl.com.testme.testme.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import pl.com.testme.testme.model.Answer;
import pl.com.testme.testme.model.ExamCreator;
import pl.com.testme.testme.model.Question;
import pl.com.testme.testme.repositories.AnswersRepository;
import pl.com.testme.testme.repositories.ExamCreatorRepository;
import pl.com.testme.testme.repositories.ExamSummaryRepository;
import pl.com.testme.testme.repositories.QuestionsRepository;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@AllArgsConstructor
public class QuestionsController {

    private ExamCreatorRepository examCreatorRepository;

    private ExamSummaryRepository examSummaryRepository;

    private AnswersRepository answersRepository;

    private QuestionsRepository questionsRepository;

    @RequestMapping("/addQuestion/{examId}")
    public String addQuestion(Model model, @PathVariable("examId") long examId, Principal principal) {
        if (!examCreatorRepository.existsByAdminId(Long.valueOf(principal.getName()))
        ) {
            return "manual";
        }
        if (examSummaryRepository.existsByExamCreatorId(examId)){
            return "questions/cannotAddQuestion";
        }
        Question question = new Question();
        long adminId = Long.valueOf(principal.getName());
        List<Answer> answers = new ArrayList<>();
        answers.add(Answer.builder().adminId(adminId).build());
        answers.add(Answer.builder().adminId(adminId).build());
        answers.add(Answer.builder().adminId(adminId).build());
        answers.add(Answer.builder().adminId(adminId).build());
        question.setAnswers(answers);
        model.addAttribute("question", question);
        model.addAttribute("currentUserName", principal.getName());
        model.addAttribute("examId", examId);
        return "questions/addQuestion";
    }

    @RequestMapping(value = "/addQuestion/{examId}", method = RequestMethod.POST)
    public String saveQuestion(@Valid Question question, @PathVariable("examId") long examId, Errors errors, Model model, Principal principal, HttpSession session) {
        model.addAttribute("examId", examId);
        if (questionsRepository.count() >= 5000) {
            return "questions/toManyQuestions";
        }
        if (errors.hasErrors()) {
            return "questions/addQuestion";
        }
        if (questionsRepository.existsByContentAndExamCreatorId(question.getContent(), examId)) {
            model.addAttribute("question", question);
            return "questions/questionAlreadyExists";
        } else {
            ExamCreator saveQuestionToThatExam = examCreatorRepository.findById(examId)
                    .orElseThrow(() -> new IllegalArgumentException("Niepoprawne id Egzaminu: " + examId));
            question.getAnswers().forEach(answer1 -> answer1.setQuestion(question));
            question.getAnswers().forEach(answer1 -> answer1.setAdminId(Long.valueOf(principal.getName())));
            question.getAnswers().forEach(answer1 -> answer1.setExamCreatorId(examId));
            question.setAdminId(Long.valueOf(principal.getName()));
            question.setExamCreatorId(saveQuestionToThatExam.getId());
            saveQuestionToThatExam.getQuestions().add(question);
            examCreatorRepository.save(saveQuestionToThatExam);
            model.addAttribute("question", question.getContent());
            model.addAttribute("answers", question.getAnswers());
            session.removeAttribute("drawQuestions");
            return "questions/addingQuestionSuccessful";
        }
    }

    @Transactional
    @RequestMapping("/deleteQuestion/{questionId}")
    public String deleteQuestion(@PathVariable("questionId") long questionId, Model model, Principal principal, HttpSession session) {
        Question question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Nie można usunać pytania o id: " + questionId + ", na które ktoś odpowiedział."));
        if (question.getAdminId() != Long.valueOf(principal.getName()) ||
                question.isUsedInExam()) {
            return "cannotDelete";
        }
        questionsRepository.delete(question);
        session.removeAttribute("drawQuestions");
        model.addAttribute("questions", questionsRepository.findAll());
        return "redirect:/examDetails/" + question.getExamCreatorId();
    }

    @Transactional
    @RequestMapping("/activateQuestion/{questionId}")
    public String activateQuestion(@PathVariable("questionId") long questionId) {
        questionsRepository.activate(questionId);
        ExamCreator examCreator = examCreatorRepository.findByQuestionsId(questionId);
        return "redirect:/examDetails/" + examCreator.getId();
    }

    @Transactional
    @RequestMapping("/deactivateQuestion/{questionId}")
    public String deactivateQuestion(@PathVariable("questionId") long questionId) {
        questionsRepository.deactivate(questionId);
        ExamCreator examCreator = examCreatorRepository.findByQuestionsId(questionId);
        return "redirect:/examDetails/" + examCreator.getId();
    }

    @RequestMapping("/deleteAnswer/{answerId}")
    public String deleteAnswer(@PathVariable("answerId") long answerId, Model model, Principal principal) {
        Answer answer = answersRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Nie można usunąć odpowiedzi: " + answerId + " jeżeli pytanie do którego była przypisana zostało użyte."));
        answersRepository.delete(answer);
        if (questionsRepository.findAllByAdminIdAndExamCreatorId(Long.valueOf(principal.getName()), answer.getQuestion().getExamCreatorId()).size() == 0) {
            return "manual";
        }
        model.addAttribute("answers", answersRepository.findAll());
        model.addAttribute("questions", questionsRepository.findAll());
        return "redirect:/examDetails/" + answer.getQuestion().getExamCreatorId();
    }

    @Transactional
    @PostMapping("/editQuestion/{questionId}")
    public String editQuestion(@Valid Question question, @PathVariable("questionId") Long questionId) {
        Question question1 = questionsRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Nie istnieje pytanie o id: " + questionId));
        question1.setContent(question.getContent());
        questionsRepository.save(question1);
        return "redirect:/examDetails/" + question1.getExamCreatorId();
    }

    @GetMapping("/editQuestion/{questionId}")
    public String editQuestion(@PathVariable("questionId") Long questionId, Principal principal, Model model) {
        Question question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Nie istnieje pytanie o id: " + questionId));
        if (question.getAdminId() != Long.valueOf(principal.getName()) || question.isUsedInExam()) {
            return "cannotDelete";
        }
        model.addAttribute("question", question);
        return "questions/editQuestion";
    }

    @Transactional
    @PostMapping("/editAnswer/{answerId}")
    public String editAnswer(@Valid Answer answer, @PathVariable("answerId") Long answerId) {
        Answer answer1 = answersRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Nie można edytować odpowiedzi, której już już ktoś udzielił: " + answerId));
        answer1.setContent(answer.getContent());
        answer1.setValue(answer.getValue());
        answersRepository.save(answer1);
        return "redirect:/examDetails/" + answer1.getQuestion().getExamCreatorId();
    }

    @GetMapping("/editAnswer/{answerId}")
    public String editAnswer(@PathVariable("answerId") Long answerId, Principal principal, Model model) {
        Answer answer1 = answersRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Nie można edytować odpowiedzi, której już już ktoś udzielił: " + answerId));
        if (answer1.getAdminId() != Long.valueOf(principal.getName()) || answer1.getQuestion().isUsedInExam()) {
            return "cannotDelete";
        }
        model.addAttribute("answer", answer1);
        model.addAttribute("answerId", answerId);
        return "questions/editAnswer";
    }
}

