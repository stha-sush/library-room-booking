// ---------- shared helpers ----------
function isOnLoginPage() {
  return window.location.pathname === "/login";
}

async function parseApiError(res) {
  let text = "";
  try {
    text = await res.text();
  } catch (_) {}

  if (!text) return { message: "Something went wrong. Please try again.", errors: null };

  try {
    const obj = JSON.parse(text);
    return {
      message: obj?.message || "Something went wrong. Please try again.",
      errors: obj?.errors || null
    };
  } catch (_) {
    return { message: text, errors: null };
  }
}

function formatErrors(message, errors) {
  if (!errors) return message;
  const items = Object.entries(errors).map(([field, msg]) => `${field}: ${msg}`);
  return items.length ? `${message} ${items.join(" • ")}` : message;
}

/**
 * API helpers:
 * - DO NOT auto-redirect on 401 here.
 * - Let the caller decide (login page wants to show a message).
 */
async function apiGet(url) {
  const res = await fetch(url, { credentials: "include" });

  if (!res.ok) {
    const err = await parseApiError(res);
    const e = new Error(formatErrors(err.message, err.errors));
    e.status = res.status;
    throw e;
  }

  return await res.json();
}

async function apiPost(url, body, contentType = "application/json") {
  const opts = { method: "POST", credentials: "include", headers: {}, body };
  if (contentType) opts.headers["Content-Type"] = contentType;

  const res = await fetch(url, opts);

  if (!res.ok) {
    const err = await parseApiError(res);
    const e = new Error(formatErrors(err.message, err.errors));
    e.status = res.status;
    throw e;
  }

  const txt = await res.text();
  return txt ? JSON.parse(txt) : {};
}

async function apiDelete(url) {
  const res = await fetch(url, { method: "DELETE", credentials: "include" });

  if (!res.ok) {
    const err = await parseApiError(res);
    const e = new Error(formatErrors(err.message, err.errors));
    e.status = res.status;
    throw e;
  }
}

function redirectToLoginIfUnauthorized(err) {
  if (err && err.status === 401 && !isOnLoginPage()) {
    window.location.href = "/login";
    return true;
  }
  return false;
}

function todayISO() {
  return new Date().toISOString().split("T")[0];
}

function isPastISODate(dateStr) {
  return dateStr < todayISO();
}

function escapeHtml(s) {
  return String(s ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeJs(s) {
  return String(s ?? "").replaceAll("\\", "\\\\").replaceAll("'", "\\'");
}

function showAlert(containerId, text, kind) {
  const el = document.getElementById(containerId);
  if (!el) return;
  el.className =
    "alert " +
    (kind === "error" ? "alert-danger" : kind === "success" ? "alert-success" : "alert-secondary");
  el.textContent = text;
  el.style.display = "block";
}

function clearAlert(containerId) {
  const el = document.getElementById(containerId);
  if (!el) return;
  el.textContent = "";
  el.className = "";
  el.style.display = "none";
}

// ---------- DOM Ready ----------
document.addEventListener("DOMContentLoaded", async () => {
  // LOGIN
  const loginForm = document.getElementById("loginForm");
  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const fd = new FormData(loginForm);
      const payload = { username: fd.get("username"), password: fd.get("password") };

      const msg = document.getElementById("loginMsg");
      if (msg) {
        msg.textContent = "Logging in...";
        msg.className = "text-muted small";
      }

      try {
        const data = await apiPost("/api/auth/login", JSON.stringify(payload));
        window.location.href = data.role === "ADMIN" ? "/admin" : "/";
      } catch (err) {
        // ✅ show message on page (no redirect on /login)
        if (msg) {
          msg.textContent = err.message || "Login failed.";
          msg.className = "text-danger small";
        }
      }
    });
  }

  // LOGOUT
  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", async () => {
      try { await apiPost("/api/auth/logout", "", null); } catch (_) {}
      window.location.href = "/login";
    });
  }

  // INDEX: date min + buildings
  const searchForm = document.getElementById("searchForm");
  if (searchForm) {
    const dateInput = searchForm.querySelector("input[name='date']");
    if (dateInput) dateInput.min = todayISO();

    const buildingSelect = document.getElementById("buildingSelect");
    if (buildingSelect) {
      try {
        const buildings = await apiGet("/api/buildings");
        if (buildings && Array.isArray(buildings)) {
          buildingSelect.innerHTML = '<option value="">Any</option>';
          buildings.forEach((b) => {
            const opt = document.createElement("option");
            opt.value = b;
            opt.textContent = b;
            buildingSelect.appendChild(opt);
          });
        }
      } catch (_) {
        // non-blocking
      }
    }

    searchForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearAlert("searchMsg");

      const fd = new FormData(searchForm);
      const date = fd.get("date");
      const start = fd.get("start");
      const end = fd.get("end");

      if (isPastISODate(date)) {
        showAlert("searchMsg", "Past dates cannot be searched.", "error");
        return;
      }

      const params = new URLSearchParams({ date, start, end });
      const capacity = fd.get("capacity");
      const type = fd.get("type");
      const building = fd.get("building");
      if (capacity) params.append("capacity", capacity);
      if (type) params.append("type", type);
      if (building) params.append("building", building);

      renderResultsInfo("Searching...");

      try {
        const rooms = await apiGet("/api/availability?" + params.toString());
        renderRooms(rooms, { date, start, end });
      } catch (err) {
        if (redirectToLoginIfUnauthorized(err)) return;
        renderResultsError(err.message || "Unable to search rooms.");
      }
    });
  }

  // ADMIN: create room
  const createRoomForm = document.getElementById("createRoomForm");
  if (createRoomForm) {
    createRoomForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearAlert("roomMsg");

      const fd = new FormData(createRoomForm);
      const payload = {
        building: fd.get("building"),
        floor: Number(fd.get("floor") || 0),
        roomNumber: fd.get("roomNumber"),
        capacity: Number(fd.get("capacity") || 1),
        type: fd.get("type"),
        projector: fd.get("projector") === "on",
        whiteboard: fd.get("whiteboard") === "on",
        status: fd.get("status"),
      };

      try {
        await apiPost("/api/admin/rooms", JSON.stringify(payload));
        showAlert("roomMsg", "Room created successfully.", "success");
        window.location.reload();
      } catch (err) {
        if (redirectToLoginIfUnauthorized(err)) return;
        showAlert("roomMsg", err.message || "Unable to create room.", "error");
      }
    });
  }

  // ADMIN: create student
  const createStudentForm = document.getElementById("createStudentForm");
  if (createStudentForm) {
    createStudentForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const msgEl = document.getElementById("studentMsg");
      if (msgEl) {
        msgEl.textContent = "Creating...";
        msgEl.className = "small text-muted";
      }

      const fd = new FormData(createStudentForm);
      const payload = {
        username: fd.get("username"),
        fullName: fd.get("fullName"),
        email: fd.get("email"),
        tempPassword: fd.get("tempPassword"),
      };

      try {
        await apiPost("/api/admin/students", JSON.stringify(payload));
        if (msgEl) {
          msgEl.textContent = "Student created successfully.";
          msgEl.className = "small text-success";
        }
        window.location.reload();
      } catch (err) {
        if (redirectToLoginIfUnauthorized(err)) return;
        if (msgEl) {
          msgEl.textContent = err.message || "Unable to create student.";
          msgEl.className = "small text-danger";
        }
      }
    });
  }
});

// ---------- Results rendering ----------
function resultsContainer() {
  return document.getElementById("results");
}

function renderResultsInfo(msg) {
  const results = resultsContainer();
  if (!results) return;
  results.innerHTML = `<div class="col-12"><div class="alert alert-secondary mb-0">${escapeHtml(msg)}</div></div>`;
}

function renderResultsError(msg) {
  const results = resultsContainer();
  if (!results) return;
  results.innerHTML = `<div class="col-12"><div class="alert alert-danger mb-0">${escapeHtml(msg)}</div></div>`;
}

function renderRooms(rooms, ctx) {
  const results = resultsContainer();
  if (!results) return;

  if (!rooms || rooms.length === 0) {
    renderResultsInfo("No rooms available for this time slot.");
    return;
  }

  results.innerHTML = rooms.map(r => `
    <div class="col-md-4">
      <div class="card shadow-sm h-100">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-start">
            <h6 class="mb-1">${escapeHtml(r.building)} ${escapeHtml(r.roomNumber)}</h6>
            <span class="badge text-bg-success">${escapeHtml(r.type)}</span>
          </div>
          <div class="text-muted small">Floor ${escapeHtml(String(r.floor))} • Capacity ${escapeHtml(String(r.capacity))}</div>

          <hr class="my-3"/>

          <button class="btn btn-primary btn-sm"
            type="button"
            onclick="openBookingPanel(${r.id}, '${ctx.date}', '${ctx.start}', '${ctx.end}', '${escapeJs(r.building)} ${escapeJs(r.roomNumber)}')">
            Book this room
          </button>
        </div>
      </div>
    </div>
  `).join("");
}

// ---------- Booking panel ----------
function openBookingPanel(roomId, date, start, end, roomLabel) {
  const panel = document.getElementById("bookingPanel");
  const label = document.getElementById("bookingRoomLabel");
  if (!panel) return;

  panel.classList.remove("d-none");
  if (label) label.textContent = roomLabel;

  document.querySelector("#bookingForm input[name='roomId']").value = roomId;
  document.querySelector("#bookingForm input[name='bookingDate']").value = date;
  document.querySelector("#bookingForm input[name='startTime']").value = start;
  document.querySelector("#bookingForm input[name='endTime']").value = end;

  clearAlert("bookingMsg");
}

function closeBookingPanel() {
  const panel = document.getElementById("bookingPanel");
  if (panel) panel.classList.add("d-none");
  clearAlert("bookingMsg");
}

async function submitBooking(e) {
  e.preventDefault();
  clearAlert("bookingMsg");

  const form = document.getElementById("bookingForm");
  const fd = new FormData(form);

  const bookingDate = fd.get("bookingDate");
  if (isPastISODate(bookingDate)) {
    showAlert("bookingMsg", "Past dates cannot be booked.", "error");
    return;
  }

  const payload = {
    roomId: Number(fd.get("roomId")),
    bookingDate: fd.get("bookingDate"),
    startTime: fd.get("startTime"),
    endTime: fd.get("endTime"),
    participants: Number(fd.get("participants")),
    purpose: String(fd.get("purpose") || "").trim(),
    note: String(fd.get("note") || "").trim()
  };

  if (!payload.purpose) {
    showAlert("bookingMsg", "Purpose is required.", "error");
    return;
  }
  if (!payload.participants || payload.participants < 1) {
    showAlert("bookingMsg", "Participants must be at least 1.", "error");
    return;
  }

  try {
    await apiPost("/api/bookings", JSON.stringify(payload));
    showAlert("bookingMsg", "Booking confirmed! Redirecting...", "success");
    window.location.href = "/student/my-bookings";
  } catch (err) {
    if (redirectToLoginIfUnauthorized(err)) return;
    showAlert("bookingMsg", err.message || "Unable to create booking.", "error");
  }
}