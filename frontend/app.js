let books = [],
  students = [];
let events = (() => {
  try {
    return JSON.parse(localStorage.getItem("pagepine_events")) || [];
  } catch {
    return [];
  }
})();
const api = async (path, options = {}) => {
  const response = await fetch(path, {
    ...options,
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok)
    throw new Error(data.error || `Request failed (${response.status})`);
  return data;
};
const refreshData = async () => {
  [books, students] = await Promise.all([
    api("/api/books"),
    api("/api/students"),
  ]);
  render();
};
const save = () =>
  localStorage.setItem("pagepine_events", JSON.stringify(events));
const esc = (s) =>
  String(s).replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
const studentFor = (id) => students.find((s) => s.id === id);
const initials = (n) =>
  n
    .split(/\s+/)
    .map((x) => x[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
const status = (b) =>
  `<span class="status ${b.available ? "available" : "issued"}">${b.available ? "Available" : "On loan"}</span>`;
function render() {
  document.getElementById("statBooks").textContent = books.length;
  document.getElementById("statAvailable").textContent = books.filter(
    (b) => b.available,
  ).length;
  document.getElementById("statIssued").textContent = books.filter(
    (b) => !b.available,
  ).length;
  document.getElementById("statStudents").textContent = students.length;
  const cover = (i) => `<span class="cover c${(i % 5) + 1}">▤</span>`;
  document.getElementById("recentBooks").innerHTML =
    books
      .slice(-5)
      .reverse()
      .map(
        (b, i) =>
          `<tr><td><div class="booktitle">${cover(i)}<b>${esc(b.title)}</b></div></td><td>${esc(b.author)}</td><td>${status(b)}</td></tr>`,
      )
      .join("") ||
    '<tr><td colspan="3" class="empty">Your shelves are waiting for their first book.</td></tr>';
  document.getElementById("booksTable").innerHTML =
    filterBooks()
      .map(
        (b, i) =>
          `<tr><td><div class="booktitle">${cover(i)}<b>${esc(b.title)}</b></div></td><td>${esc(b.author)}</td><td>#${b.id}</td><td>${status(b)}</td><td>${b.available ? `<button class="linkbtn" data-issue="${b.id}">Issue ↗</button>` : `<button class="linkbtn" data-return="${b.id}">Return ↙</button>`}</td></tr>`,
      )
      .join("") ||
    '<tr><td colspan="5" class="empty"><div>⌕</div>No books match your search.</td></tr>';
  const term = (
    document.getElementById("studentSearch")?.value || ""
  ).toLowerCase();
  let listed = students.filter(
    (s) => s.name.toLowerCase().includes(term) || String(s.id).includes(term),
  );
  document.getElementById("studentCount").textContent =
    `${students.length} reader${students.length === 1 ? "" : "s"}`;
  document.getElementById("studentsTable").innerHTML =
    listed
      .map((s) => {
        let n = books.filter(
          (b) => !b.available && b.studentId === s.id,
        ).length;
        return `<tr><td><div class="studentcell"><span class="studentmini">${initials(esc(s.name))}</span><b>${esc(s.name)}</b></div></td><td>#${s.id}</td><td>${n} book${n === 1 ? "" : "s"}</td><td><button class="linkbtn" data-issue-student="${s.id}">Issue a book ↗</button></td></tr>`;
      })
      .join("") ||
    '<tr><td colspan="4" class="empty"><div>♙</div>No students found.</td></tr>';
  const q = (document.getElementById("loanSearch")?.value || "").toLowerCase();
  let loans = books
    .filter((b) => !b.available)
    .filter((b) => {
      const s = studentFor(b.studentId);
      return (
        b.title.toLowerCase().includes(q) ||
        (s?.name || "").toLowerCase().includes(q)
      );
    });
  document.getElementById("loanCount").textContent =
    `${loans.length} active loan${loans.length === 1 ? "" : "s"}`;
  document.getElementById("loansTable").innerHTML =
    loans
      .map((b) => {
        const s = studentFor(b.studentId);
        return `<tr><td><b>${esc(b.title)}</b><div class="muted">${esc(b.author)}</div></td><td>${s ? esc(s.name) : "Unknown reader"}</td><td>#${b.id}</td><td>#${b.studentId}</td><td><button class="linkbtn" data-return="${b.id}">Return book ↙</button></td></tr>`;
      })
      .join("") ||
    '<tr><td colspan="5" class="empty"><div>✳</div>All books are home. A lovely sight.</td></tr>';
  document.getElementById("activity").innerHTML =
    events
      .slice(0, 4)
      .map(
        (e) =>
          `<div class="activityrow"><div class="activityico">${e.type === "issue" ? "↗" : "↙"}</div><div><p><b>${esc(e.book)}</b> ${e.type === "issue" ? "went home with" : "came back from"} ${esc(e.student)}.</p><small>${esc(e.time)}</small></div></div>`,
      )
      .join("") ||
    '<div class="activityrow"><div class="activityico">✳</div><div><p>Your library is ready for its first chapter.</p><small>Activity will appear here</small></div></div>';
}
function filterBooks() {
  const q = (document.getElementById("bookSearch")?.value || "").toLowerCase(),
    f = document.getElementById("bookFilter")?.value || "all";
  return books.filter(
    (b) =>
      (b.title.toLowerCase().includes(q) ||
        b.author.toLowerCase().includes(q)) &&
      (f === "all" || (f === "available") === b.available),
  );
}
function log(type, b, s) {
  events.unshift({
    type,
    book: b.title,
    student: s.name,
    time: new Date().toLocaleString(undefined, {
      month: "short",
      day: "numeric",
      hour: "numeric",
      minute: "2-digit",
    }),
  });
  events = events.slice(0, 12);
}
function toast(msg) {
  const t = document.getElementById("toast");
  t.textContent = msg;
  t.classList.add("show");
  setTimeout(() => t.classList.remove("show"), 2600);
}
const back = document.getElementById("modalback"),
  form = document.getElementById("form");
function openModal(kind, extra = {}) {
  const title = document.getElementById("modalTitle"),
    sub = document.getElementById("modalSub");
  if (kind === "book") {
    title.textContent = "Add a book";
    sub.textContent = "A new story for the shelves.";
    form.innerHTML =
      '<div class="field"><label for="title">Book title</label><input id="title" name="title" required placeholder="e.g. The Midnight Library"></div><div class="field"><label for="author">Author</label><input id="author" name="author" required placeholder="e.g. Matt Haig"></div><div class="modalfoot"><button type="button" class="btn" data-cancel>Cancel</button><button class="btn primary">Add book</button></div>';
  } else if (kind === "student") {
    title.textContent = "Add a student";
    sub.textContent = "Welcome a new reader to your library.";
    form.innerHTML =
      '<div class="field"><label for="name">Student name</label><input id="name" name="name" required placeholder="e.g. Maya Patel"></div><div class="modalfoot"><button type="button" class="btn" data-cancel>Cancel</button><button class="btn primary">Add student</button></div>';
  } else {
    title.textContent = "Issue a book";
    sub.textContent = "Pair a story with its next reader.";
    const available = books.filter((b) => b.available),
      choices = students;
    form.innerHTML = `<div class="field"><label for="bookId">Choose a book</label><select id="bookId" name="bookId" required><option value="">Select an available book</option>${available.map((b) => `<option value="${b.id}" ${extra.bookId === b.id ? "selected" : ""}>${esc(b.title)} · #${b.id}</option>`).join("")}</select></div><div class="field"><label for="studentId">Choose a student</label><select id="studentId" name="studentId" required><option value="">Select a student</option>${choices.map((s) => `<option value="${s.id}" ${extra.studentId === s.id ? "selected" : ""}>${esc(s.name)} · #${s.id}</option>`).join("")}</select></div>${!available.length ? '<p class="muted">All books are currently on loan. Return a book before issuing another.</p>' : ""}${!choices.length ? '<p class="muted">Add a student before issuing a book.</p>' : ""}<div class="modalfoot"><button type="button" class="btn" data-cancel>Cancel</button><button class="btn primary" ${!available.length || !choices.length ? "disabled" : ""}>Issue book</button></div>`;
  }
  back.classList.add("show");
  setTimeout(() => form.querySelector("input,select")?.focus(), 30);
}
async function returnBook(id) {
  const b = books.find((x) => x.id === id),
    s = studentFor(b?.studentId);
  if (!b || !s) return;
  try {
    await api("/api/returns", {
      method: "POST",
      body: JSON.stringify({ bookId: b.id, studentId: s.id }),
    });
    log("return", b, s);
    save();
    await refreshData();
    toast(`Returned: ${b.title}.`);
  } catch (error) {
    toast(error.message);
  }
}
document.getElementById("nav").addEventListener("click", (e) => {
  const btn = e.target.closest("[data-page]");
  if (!btn) return;
  showPage(btn.dataset.page);
});
function showPage(id) {
  document
    .querySelectorAll(".page")
    .forEach((p) => p.classList.toggle("active", p.id === id));
  document
    .querySelectorAll(".nav button")
    .forEach((b) => b.classList.toggle("active", b.dataset.page === id));
  document.getElementById("crumb").textContent = {
    dashboard: "Overview",
    books: "Books",
    students: "Students",
    circulation: "Circulation",
  }[id];
  document.getElementById("sidebar").classList.remove("open");
  render();
}
document.addEventListener("click", (e) => {
  const open = e.target.closest("[data-open]");
  if (open) {
    openModal(open.dataset.open);
    return;
  }
  const go = e.target.closest("[data-goto]");
  if (go) {
    showPage(go.dataset.goto);
    return;
  }
  const issue = e.target.closest("[data-issue]");
  if (issue) {
    openModal("issue", { bookId: Number(issue.dataset.issue) });
    return;
  }
  const who = e.target.closest("[data-issue-student]");
  if (who) {
    openModal("issue", { studentId: Number(who.dataset.issueStudent) });
    return;
  }
  const ret = e.target.closest("[data-return]");
  if (ret) {
    returnBook(Number(ret.dataset.return));
    return;
  }
  if (
    e.target.closest("[data-cancel]") ||
    e.target.id === "closeModal" ||
    e.target === back
  )
    back.classList.remove("show");
});
document
  .getElementById("quickIssue")
  .addEventListener("click", () => openModal("issue"));
document
  .getElementById("menuBtn")
  .addEventListener("click", () =>
    document.getElementById("sidebar").classList.toggle("open"),
  );
form.addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = new FormData(form);
  const modal = document.getElementById("modalTitle").textContent;
  try {
    if (modal === "Add a book") {
      await api("/api/books", {
        method: "POST",
        body: JSON.stringify({
          title: data.get("title").trim(),
          author: data.get("author").trim(),
        }),
      });
      toast("The new book is on the shelf.");
    } else if (modal === "Add a student") {
      await api("/api/students", {
        method: "POST",
        body: JSON.stringify({ name: data.get("name").trim() }),
      });
      toast("A new reader joined the library.");
    } else {
      const b = books.find((x) => x.id === Number(data.get("bookId"))),
        s = studentFor(Number(data.get("studentId")));
      if (!b || !s) return;
      await api("/api/loans", {
        method: "POST",
        body: JSON.stringify({ bookId: b.id, studentId: s.id }),
      });
      log("issue", b, s);
      toast(`Issued: ${b.title} ? ${s.name}.`);
    }
    save();
    back.classList.remove("show");
    await refreshData();
  } catch (error) {
    toast(error.message);
  }
});
document.getElementById("bookSearch").addEventListener("input", render);
document.getElementById("bookFilter").addEventListener("change", render);
document.getElementById("studentSearch").addEventListener("input", render);
document.getElementById("loanSearch").addEventListener("input", render);
document.getElementById("today").textContent = new Date().toLocaleDateString(
  undefined,
  { weekday: "short", month: "short", day: "numeric" },
);
refreshData().catch((error) =>
  toast("Backend connection failed: " + error.message),
);
