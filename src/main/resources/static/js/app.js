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

        // Build completed lessons set and per-module counts
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
      } catch (e) { /* not logged in */ }
    },

    async loadUserInfo() {
      // Spring Security exposes the OAuth2 user principal at /user (we'll add this endpoint)
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
      this.quizSubmitted = false;
      this.view = 'lesson';
      try {
        const res = await fetch(`/api/lessons/${this.currentModule.id}/${lesson.id}`);
        this.currentLesson = await res.json();
        // Mark lesson as viewed (requires auth; silently ignored if not logged in)
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

    allAnswered() {
      if (!this.currentLesson?.quiz) return false;
      return this.currentLesson.quiz.questions.every(q => this.quizAnswers[q.id] !== undefined);
    },

    async submitQuiz() {
      const questions = this.currentLesson.quiz.questions;
      let correct = 0;
      const answers = questions.map(q => {
        const chosen = this.quizAnswers[q.id];
        if (chosen === q.correct) correct++;
        return { questionId: q.id, chosen, correct: q.correct };
      });
      this.quizScore = Math.round((correct / questions.length) * 100);
      this.quizSubmitted = true;

      // Persist result if logged in
      fetch(`/api/quiz/submit/${this.currentLesson.id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          moduleId: this.currentModule.id,
          score: correct,
          total: questions.length,
          answers
        })
      }).then(async res => {
        if (res.ok) {
          const p = await res.json();
          if (p.completed) {
            this.completedLessons.add(this.currentLesson.id);
            const mod = this.progress[this.currentModule.id] || { completed: 0 };
            mod.completed = (mod.completed || 0) + 1;
            this.progress[this.currentModule.id] = mod;
          }
        }
      }).catch(() => {});
    },

    retryQuiz() {
      this.quizAnswers = {};
      this.quizSubmitted = false;
      this.quizScore = 0;
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
