function app() {
  return {
    view: 'home',
    loading: true,
    lessonLoading: false,
    modules: [],
    currentModule: null,
    currentLesson: null,
    userInfo: null,
    progress: {},   // { moduleId: { completed: N } }
    completedLessons: new Set(),
    summary: null,
    quizAnswers: {},
    quizSubmitted: false,
    quizScore: 0,
    openAnswers: {},
    openQuizSubmitted: false,
    previousOpenAttempt: null,
    lessonFeedback: [],

    async init() {
      await Promise.all([this.loadModules(), this.loadUser(), this.loadUserInfo()]);
      this.loading = false;
    },

    async loadModules() {
      try {
        const res = await fetch('/api/modules');
        const data = await res.json();
        this.modules = data.modules || [];
      } catch (e) {
        console.error('Failed to load modules', e);
      }
    },

    async loadUser() {
      try {
        const res = await fetch('/api/progress');
        if (res.status === 401 || res.status === 403) return;
        const data = await res.json();
        this.summary = data.summary;

        const counts = {};
        const completed = new Set();
        (data.progress || []).forEach(p => {
          if (p.completed) {
            completed.add(p.lessonId);
            counts[p.moduleId] = counts[p.moduleId] || { completed: 0 };
            counts[p.moduleId].completed++;
          }
        });
        this.completedLessons = completed;
        this.progress = counts;
      } catch (e) { }
    },

    async loadUserInfo() {
      try {
        const res = await fetch('/api/user');
        if (res.ok) this.userInfo = await res.json();
      } catch (e) {}
    },

    openModule(mod) {
      this.currentModule = mod;
      this.view = 'module';
    },

    async openLesson(lesson) {
      this.currentLesson = null;
      this.lessonLoading = true;
      this.quizAnswers = {};
      this.openAnswers = {};
      this.openQuizSubmitted = false;
      this.previousOpenAttempt = null;
      this.lessonFeedback = [];
      this.quizSubmitted = false;
      this.quizScore = 0;
      this.view = 'lesson';
      try {
        const res = await fetch(`/api/lessons/${this.currentModule.id}/${lesson.id}`);
        this.currentLesson = await res.json();
        if (this.currentLesson?.quiz?.type === 'open_ended') {
          await this.loadPreviousOpenAnswers(lesson.id);
        }
        await this.loadFeedback(lesson.id);
        fetch(`/api/progress/lesson/${lesson.id}/view`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ moduleId: this.currentModule.id })
        }).catch(() => {});
      } catch (e) {
        console.error('Failed to load lesson', e);
      } finally {
        this.lessonLoading = false;
      }
    },

    async showProgress() {
      if (this.userInfo) await this.loadUser();
      this.view = 'progress';
    },

    async loadPreviousOpenAnswers(lessonId) {
      try {
        const res = await fetch(`/api/quiz/previous/${lessonId}`);
        if (res.ok) {
          const text = await res.text();
          const attempt = text ? JSON.parse(text) : null;
          if (attempt?.answers?.length) {
            this.previousOpenAttempt = attempt;
            attempt.answers.forEach(ans => {
              try {
                const data = JSON.parse(ans.answerData);
                if (data.answer) this.openAnswers[ans.questionId] = data.answer;
              } catch (e) {}
            });
          }
        }
      } catch (e) {}
    },

    async loadFeedback(lessonId) {
      try {
        const res = await fetch(`/api/quiz/feedback/${lessonId}`);
        if (res.ok) {
          this.lessonFeedback = await res.json();
          // Clear pre-filled answer for any question that has received feedback
          Object.keys(this.lessonFeedback).forEach(qId => {
            if (this.lessonFeedback[qId]?.length > 0) {
              this.openAnswers[qId] = '';
            }
          });
        }
      } catch (e) {}
    },

    allAnswered() {
      if (!this.currentLesson?.quiz) return false;
      return this.currentLesson.quiz.questions.every(q => this.quizAnswers[q.id] !== undefined);
    },

    allOpenAnswered() {
      if (!this.currentLesson?.quiz?.questions) return false;
      return this.currentLesson.quiz.questions.every(q =>
        this.openAnswers[q.id] && this.openAnswers[q.id].trim().length > 0
      );
    },

    async submitQuiz() {
      const questions = this.currentLesson.quiz.questions;
      let correct = 0;
      const answers = questions.map(q => {
        const chosen = this.quizAnswers[q.id];
        if (chosen === q.correct) correct++;
        return { questionId: q.id, questionText: q.question, options: q.options, explanation: q.explanation, chosen, correct: q.correct };
      });
      this.quizScore = Math.round((correct / questions.length) * 100);
      this.quizSubmitted = true;

      try {
        const res = await fetch(`/api/quiz/attempt/${this.currentLesson.id}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            moduleId: this.currentModule.id,
            quizType: 'MULTIPLE_CHOICE',
            answers
          })
        });
        if (res.ok) {
          const p = await res.json();
          const passed = p.status === 'MARKED' && p.score >= Math.ceil(p.totalQuestions * 0.7);
          if (passed && !this.completedLessons.has(this.currentLesson.id)) {
            this.completedLessons.add(this.currentLesson.id);
            const mod = this.progress[this.currentModule.id] || { completed: 0 };
            mod.completed = (mod.completed || 0) + 1;
            this.progress[this.currentModule.id] = mod;
            if (this.summary) this.summary = { ...this.summary, totalLessonsCompleted: (this.summary.totalLessonsCompleted || 0) + 1 };
          }
        }
      } catch (e) { console.error('Failed to save quiz attempt', e); }
    },

    async submitOpenQuiz() {
      const questions = this.currentLesson.quiz.questions;
      const answers = questions.map(q => ({
        questionId: q.id,
        questionText: q.question,
        text: this.openAnswers[q.id] || ''
      }));
      try {
        const res = await fetch(`/api/quiz/attempt/${this.currentLesson.id}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            moduleId: this.currentModule.id,
            quizType: 'OPEN_ENDED',
            answers
          })
        });
        if (res.ok) {
          this.previousOpenAttempt = await res.json();
          this.openQuizSubmitted = true;
        }
      } catch (e) {
        console.error('Failed to submit open quiz', e);
      }
    },

    retryQuiz() {
      this.quizAnswers = {};
      this.quizSubmitted = false;
      this.quizScore = 0;
    },

    tryParseAnswer(answerDataStr) {
      try {
        const d = typeof answerDataStr === 'string' ? JSON.parse(answerDataStr) : answerDataStr;
        return d?.answer || d?.text || '';
      } catch (e) { return ''; }
    },

    feedbackForQuestion(questionId) {
      const entries = [];
      (this.lessonFeedback || []).forEach(attempt => {
        (attempt.answers || []).forEach(ans => {
          if (ans.questionId === questionId) {
            entries.push({
              attemptNumber: attempt.attemptNumber,
              submittedAt: attempt.submittedAt,
              feedback: ans.feedback,
              answerData: ans.answerData
            });
          }
        });
      });
      return entries;
    },

    answerPreview(answerDataStr) {
      try {
        const d = typeof answerDataStr === 'string' ? JSON.parse(answerDataStr) : answerDataStr;
        if (d?.type === 'open_ended') return d.answer || '';
        if (d?.type === 'multiple_choice' && d.options) return d.options[d.chosen] || '';
        return '';
      } catch (e) { return ''; }
    },

    isCompleted(lessonId) {
      return this.completedLessons.has(lessonId);
    },

    progressPct(mod) {
      const done = this.progress[mod.id]?.completed || 0;
      return mod.lessons.length > 0 ? Math.round((done / mod.lessons.length) * 100) : 0;
    },

    levelBadge(level) {
      const badges = {
        1: 'bg-green-100 text-green-700',
        2: 'bg-blue-100 text-blue-700',
        3: 'bg-purple-100 text-purple-700',
        4: 'bg-orange-100 text-orange-700'
      };
      return badges[level] || 'bg-gray-100 text-gray-600';
    }
  };
}
