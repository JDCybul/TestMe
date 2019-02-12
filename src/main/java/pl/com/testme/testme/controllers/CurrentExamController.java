package pl.com.testme.testme.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.com.testme.testme.model.Answer;
import pl.com.testme.testme.model.ExamCreator;
import pl.com.testme.testme.model.ExamSummary;
import pl.com.testme.testme.model.Question;
import pl.com.testme.testme.repositories.*;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@AllArgsConstructor
public class CurrentExamController {

	private AnswersRepository answersRepository;

	private StudentsRepository studentsRepository;

	private ExamCreatorRepository examCreatorRepository;

	private ExamSummaryRepository examSummaryRepository;

	private QuestionsRepository questionsRepository;

	@Transactional
	@RequestMapping(value = "/currentExam/{examId}", method = RequestMethod.GET)
	public String getSelected(@PathVariable("examId") long examId, Model model, HttpSession session, Principal principal) {
		log.info("egzamin rozpoczął student o indeksie {}", principal.getName());
		model.addAttribute("examId", examId);
		ExamCreator examCreator = examCreatorRepository.findById(examId)
				.orElseThrow(() -> new IllegalArgumentException("Nie ma egzaminu o takim id" + examId));
		problemsWithExam(principal, examId, model, session, examCreator);
		LocalTime examStartTime = (LocalTime) session.getAttribute("examStartTime");
		examTimeDurationManagement(examStartTime, session, model, examCreator);
		List<Question> drawQuestions = (List<Question>) session.getAttribute("drawQuestions");
		if (drawQuestions == null) drawQuestions = shuffleQuestions(examCreator);
		if (isLastQuestion(drawQuestions) || haveTime(examCreator, session, principal)) {
			List<Answer> answersList = (List<Answer>) session.getAttribute("chosenAnswers");
			resultInPercent(answersList, examId);
			float grade = userGrade(answersList, examCreator.getThreshold(), model, resultInPercent(answersList, examId), examCreator);
			addToExamSummary(model, grade, userScore(answersList), principal, examCreator, maximumPossibleSumOfPoints(examCreator.getId()), resultInPercent(answersList, examId));
			if (adminExam(principal, session)) {
				addToExamSummary(model, grade, userScore(answersList), principal, examCreator, maximumPossibleSumOfPoints(examId), resultInPercent(answersList, examId));
				return "exams/adminExam";
			} else
				saveDataToExamSummary(answersList, principal, grade
						, Long.valueOf(principal.getName()), examId
						, resultInPercent(answersList, examId), maximumPossibleSumOfPoints(examCreator.getId()), userScore(answersList), session);
			addToExamSummary(model, grade, userScore(answersList), principal, examCreator, maximumPossibleSumOfPoints(examId), resultInPercent(answersList, examId));
			log.info("Odpowiedzi wybrane przez studenta o indeksie {} to: {}", principal.getName(), answersList);
			return "exams/summaryOfTheCompletedExam";
		}
		drawnQuestionsAndAnswersShuffler(drawQuestions, session, model, principal, examCreator);
		return "exams/currentExam";
	}

	@Transactional
	@PostMapping("/currentExam/{examId}")
	public String postAnswer(@PathVariable("examId") long examId, Model model, HttpSession session, @RequestParam(value = "answersIds", required = false)
	@Valid Long[] answerIds, Principal principal) {
		if (examCreatorRepository.findById(examId).isPresent()) {
			if (haveTime(examCreatorRepository.findById(examId).get(), session, principal)) {
				return getSelected(examId, model, session, principal);
			}
		}
		if (answerIds == null || answerIds.length <= 0) {
			model.addAttribute("noChoosenAnswers", true);
			return getSelected(examId, model, session, principal);
		}
		List<Answer> answers = Arrays.stream(answerIds)
				.map(answerId -> answersRepository.findById(answerId).
						get())
				.collect(Collectors.toList());
		Question question = answers.get(0).getQuestion();
		List<Answer> chosenAnswers = (List<Answer>) session.getAttribute("chosenAnswers");
		if (chosenAnswers == null) {
			chosenAnswers = new ArrayList<>();
		}
		chosenAnswers.addAll(answers);
		session.setAttribute("chosenAnswers", chosenAnswers);
		List<Question> drawQuestions = (List<Question>) session.getAttribute("drawQuestions");
		drawQuestions.remove(question);
		session.setAttribute("drawQuestions", drawQuestions);
		log.info("{} wybrał odpowiedzi o id: {} na pytanie {}", principal.getName(), answerIds, question.getContent());
		return getSelected(examId, model, session, principal);
	}

	private void saveDataToExamSummary(List<Answer> answers, Principal principal, float grade, long studentIndex, long examId, float resultInPercent, int maxScore, int achievedScore, HttpSession session) {
		ExamCreator examCreator = examCreatorRepository.findById(examId).orElseThrow(() -> new IllegalArgumentException("Nie ma egzaminu o takim id" + examId));
		ExamSummary examSummary = new ExamSummary(studentsRepository.getByUsername(principal.getName()), examCreator, answers, grade, studentIndex, examCreator.getAdminId(), resultInPercent, maxScore, achievedScore);
		for (Answer answer : answers) {
			questionsRepository.usedInExam(answer.getQuestion().getId());
		}
		examCreatorRepository.cantEditSetToFalse(examId);
		studentsRepository.setCanEditToFalse(principal.getName());
		examSummaryRepository.save(examSummary);
		session.removeAttribute("examStartTime");
		session.removeAttribute("currentExamId");
		log.info("{} ukończył egzamin z oceną {} osiągając wynik {}% ", principal.getName(), grade, resultInPercent);
	}

	private int maximumPossibleSumOfPoints(long examId) {
		ExamCreator examCreator = examCreatorRepository.findById(examId).orElseThrow(() -> new IllegalArgumentException("Nie ma egzaminu o takim id" + examId));
		long examCreatorId = examCreator.getId();
		List<Integer> tempIntegerList;
		tempIntegerList = answersRepository.positiveValue(examCreatorId);
		return tempIntegerList.stream().mapToInt(Integer::intValue).sum();
	}

	private int userScore(List<Answer> answersList) {
		int userScore = 0;
		for (Answer anAnswersList : answersList) {
			userScore = userScore + anAnswersList.getValue();
		}
		return userScore;
	}

	private float resultInPercent(List<Answer> answersList, long examCreatorId) {
		float resultInPercent = (float) userScore(answersList) * 100 / maximumPossibleSumOfPoints(examCreatorId);
		if (resultInPercent < 0) {
			resultInPercent = 0;
		}
		return resultInPercent;
	}

	private float userGrade(List<Answer> answersList, float threshold, Model model, float resultInPercent, ExamCreator examCreator) {
		if (resultInPercent < threshold) {
			return 2f;
		}
		float above2 = 100 - threshold;
		float range = above2 / 5;
		float maximumOfCurrentRange = threshold + range;
		float grade = 3f;
		for (int i = 1; i < 5; i++) {
			if (resultInPercent >= maximumOfCurrentRange) {
				grade = grade + 0.5f;
			}
			maximumOfCurrentRange = maximumOfCurrentRange + range;
		}
		model.addAttribute("resultInPercent", resultInPercent);
		model.addAttribute("grade", grade);
		return grade;
	}

	private void drawnQuestionsAndAnswersShuffler(List<Question> drawQuestions, HttpSession session, Model model, Principal principal, ExamCreator examCreator) {
		Question shuffledQuestion = drawQuestions.get(0);
		session.setAttribute("drawQuestions", drawQuestions);
		model.addAttribute("question", shuffledQuestion);
		List<Answer> allAnswers = shuffledQuestion.getAnswers();
		Collections.shuffle(allAnswers);
		model.addAttribute("answers", allAnswers);
		List<Integer> maxPoints = new ArrayList<>();
		List<Integer> maxPointsPerQuestion = new ArrayList<>(answersRepository.valueCounter(shuffledQuestion.getId()));
		int maxPointsForAnswer = maxPointsPerQuestion.stream().mapToInt(Integer::intValue).sum();
		model.addAttribute("showPoints", examCreator.isShowPoints());
		model.addAttribute("maxPointsForAnswer", maxPointsForAnswer);
		maxPoints.add(Collections.max(answersRepository.value(shuffledQuestion.getId())));
		model.addAttribute("maximumPoints", maxPoints);
		model.addAttribute("examTitle", examCreator.getExamTitle());
		model.addAttribute("questionAmount", questionAmount(drawQuestions));
		model.addAttribute("currentUserName", principal.getName());
	}

	private boolean examAlreadyPassed(Principal principal, ExamCreator examCreator, Model model) {
		if (examSummaryRepository.existsExamSummaryByStudentIndexAndExamCreator_Id(Long.valueOf(principal.getName()), examCreator.getId())) {
			model.addAttribute("studentIndex", Long.valueOf(principal.getName()));
			model.addAttribute("examSummary", examSummaryRepository.findByStudentIndexAndExamCreatorId(Long.valueOf(principal.getName()), examCreator.getId()));
			return true;
		}
		return false;
	}

	private boolean adminExam(Principal principal, HttpSession session) {
		if (studentsRepository.getByUsername(principal.getName()).getAuthority().equals("ROLE_ADMIN")) {
			session.invalidate();
			return true;
		}
		return false;
	}

	private void addToExamSummary(Model model, float grade, int score, Principal principal, ExamCreator examCreator, int maxScore, float resultInPercent) {
		model.addAttribute("grade", grade);
		model.addAttribute("score", score);
		model.addAttribute("currentUserName", principal.getName());
		model.addAttribute("threshold", examCreator.getThreshold());
		model.addAttribute("examTitle", examCreator.getExamTitle());
		model.addAttribute("allPoints", maxScore);
		model.addAttribute("resultInPercent", resultInPercent);
	}

	private boolean haveTime(ExamCreator examCreator, HttpSession session, Principal principal) {
		if (examCreator.getDurationInMinutes() <= 0) return false;
		LocalTime examStartTime = (LocalTime) session.getAttribute("examStartTime");
		LocalTime examEndTime = examStartTime.plusMinutes(examCreator.getDurationInMinutes());
		return !examEndTime.isAfter(LocalTime.now());

	}

	private void examTimeDurationManagement(LocalTime localTime, HttpSession session, Model model, ExamCreator examCreator) {
		if (session.getAttribute("examStartTime") == null) {
			session.setAttribute("examStartTime", LocalTime.now());
		}
		session.setAttribute("currentExamId", examCreator.getId());
		LocalTime examStartTime = (LocalTime) session.getAttribute("examStartTime");
		long secondsThatExamGoing = ChronoUnit.SECONDS.between(examStartTime, LocalTime.now());
		long examDurationInSeconds = examCreator.getDurationInMinutes() * 60;
		long secondsLeft = examDurationInSeconds - secondsThatExamGoing;
		model.addAttribute("hasTimeLimit", examCreator.isHasTimeLimit());
		model.addAttribute("secondsLeft", secondsLeft);
		model.addAttribute("outOfTime", secondsLeft == 0);
	}

	private List<Question> shuffleQuestions(ExamCreator examCreator) {
		List<Question> questions = examCreator.getQuestions();
		List<Question> activeQuestions = new ArrayList<>();
		questions.forEach(question -> {
			if (question.isActive()) {
				activeQuestions.add(question);
			}
		});
		Collections.shuffle(activeQuestions);
		activeQuestions.forEach(question -> question.getAnswers().size());
		return activeQuestions;
	}

	private String problemsWithExam(Principal principal, long examId, Model model, HttpSession session, ExamCreator examCreator) {
		if (!examCreatorRepository.existsByAdminIdAndId(Long.valueOf(principal.getName()), examId) && studentsRepository.existsByAuthorityAndUsername("ROLE_ADMIN", principal.getName())) {
			return "manual";
		}

		if (examAlreadyPassed(principal, examCreator, model)) {
			return "exams/examAlreadyPassed";
		}
		Long currentExamId = (Long) session.getAttribute("currentExamId");
		if (currentExamId != null && !currentExamId.equals(examId)) {
			return "exams/backToExam";

		}
		return "";
	}

	private int questionAmount(List<Question> list) {
		return list.size();
	}

	private boolean isLastQuestion(List<Question> drawQuestions) {
		return drawQuestions.isEmpty();
	}
}

