const ACCESS_AUTH_API = "/api/auth";
const ACCESS_CONTRIBUTOR_API = "/api/contributor-requests";
const ACCESS_ADMIN_API = "/api/admin/contributor-requests";

document.addEventListener("DOMContentLoaded", () => {
    const page = document.body.dataset.page;
    bindAccessLogoutButtons();
    showMessageFromQuery();

    if (page === "home") {
        initHomePage();
    }

    if (page === "login") {
        initLoginPage();
    }

    if (page === "register") {
        initRegisterPage();
    }

    if (page === "account") {
        initAccountPage();
    }

    if (page === "admin-approval") {
        initAdminApprovalPage();
    }
});

async function initHomePage() {
    try {
        const currentUser = await accessRequestJson(`${ACCESS_AUTH_API}/me`, { method: "GET" });
        renderHomeSession(currentUser);
    } catch (error) {
        renderHomeSession(null);
    }
}

async function initLoginPage() {
    const loginForm = document.getElementById("loginForm");
    if (!loginForm) return;

    try {
        const currentUser = await accessRequestJson(`${ACCESS_AUTH_API}/me`, { method: "GET" });
        window.location.href = resolvePostLoginRedirect(currentUser);
        return;
    } catch (error) {
        // Continue showing the login form when no valid session exists.
    }

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const payload = {
            email: document.getElementById("loginEmail").value.trim(),
            password: document.getElementById("loginPassword").value
        };

        try {
            const currentUser = await accessRequestJson(`${ACCESS_AUTH_API}/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            showAccessToast("Login successful.");
            window.location.href = resolvePostLoginRedirect(currentUser);
        } catch (error) {
            showAccessToast(error.message || "Unable to log in.");
        }
    });
}

async function initRegisterPage() {
    const registerForm = document.getElementById("registerForm");
    if (!registerForm) return;

    try {
        const currentUser = await accessRequestJson(`${ACCESS_AUTH_API}/me`, { method: "GET" });
        window.location.href = resolvePostLoginRedirect(currentUser);
        return;
    } catch (error) {
        // Continue showing the registration form when no valid session exists.
    }

    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const payload = {
            name: document.getElementById("registerName").value.trim(),
            email: document.getElementById("registerEmail").value.trim(),
            password: document.getElementById("registerPassword").value
        };

        try {
            await accessRequestJson(`${ACCESS_AUTH_API}/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            showAccessToast("Registration successful. Please log in.");
            window.location.href = `./login.html?message=${encodeURIComponent("Account created successfully. Please log in.")}`;
        } catch (error) {
            showAccessToast(error.message || "Unable to register.");
        }
    });
}

async function initAccountPage() {
    const currentUser = await requireAuthenticatedUser();
    await renderAccountPage(currentUser);

    const accountForm = document.getElementById("accountForm");
    if (accountForm) {
        accountForm.addEventListener("submit", async (event) => {
            event.preventDefault();

            const payload = {
                name: document.getElementById("accountName").value.trim(),
                email: document.getElementById("accountEmail").value.trim()
            };

            try {
                await accessRequestJson(`${ACCESS_AUTH_API}/account`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });

                const refreshedUser = await accessRequestJson(`${ACCESS_AUTH_API}/me`, { method: "GET" });
                await renderAccountPage(refreshedUser);
                showAccessToast("Account details updated.");
            } catch (error) {
                showAccessToast(error.message || "Unable to update account.");
            }
        });
    }

    const requestButton = document.getElementById("submitContributorRequestBtn");
    if (requestButton) {
        requestButton.addEventListener("click", async () => {
            requestButton.disabled = true;

            try {
                await accessRequestJson(`${ACCESS_CONTRIBUTOR_API}`, {
                    method: "POST"
                });
                const refreshedUser = await accessRequestJson(`${ACCESS_AUTH_API}/me`, { method: "GET" });
                await renderAccountPage(refreshedUser);
                showAccessToast("Contributor request submitted.");
            } catch (error) {
                showAccessToast(error.message || "Unable to submit contributor request.");
            } finally {
                requestButton.disabled = false;
            }
        });
    }
}

async function initAdminApprovalPage() {
    const currentUser = await requireAuthenticatedUser();
    if (currentUser.role !== "ADMINISTRATOR") {
        window.location.href = `./account.html?message=${encodeURIComponent("Administrator permission is required.")}`;
        return;
    }

    const refreshButton = document.getElementById("refreshPendingRequestsBtn");
    if (refreshButton) {
        refreshButton.addEventListener("click", loadPendingRequestList);
    }

    const listContainer = document.getElementById("pendingRequestList");
    if (listContainer) {
        listContainer.addEventListener("click", async (event) => {
            const button = event.target.closest("[data-decision]");
            if (!button) return;

            const requestId = button.dataset.requestId;
            const decision = button.dataset.decision;
            const commentInput = document.getElementById(`reviewComment-${requestId}`);

            button.disabled = true;

            try {
                await accessRequestJson(`${ACCESS_ADMIN_API}/${requestId}/decision`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        decision,
                        reviewComment: commentInput ? commentInput.value.trim() : ""
                    })
                });

                showAccessToast(`Request ${decision.toLowerCase()} successfully.`);
                await loadPendingRequestList();
            } catch (error) {
                showAccessToast(error.message || "Unable to review request.");
            } finally {
                button.disabled = false;
            }
        });
    }

    await loadPendingRequestList();
}

async function renderAccountPage(currentUser) {
    populateAccountHeader(currentUser);
    populateAccountForm(currentUser);

    const latestRequest = await accessRequestJson(`${ACCESS_CONTRIBUTOR_API}/me`, { method: "GET" });
    populateContributorPanel(currentUser, latestRequest);
    populateLatestRequestCard(latestRequest);
}

function populateAccountHeader(currentUser) {
    setText("accountHeroTitle", `${currentUser.name}'s account`);
    setText(
        "accountHeroSubtitle",
        "Update your basic profile details, monitor contributor approval, and enter the correct workspace for your role."
    );
    setText("accountRoleChip", formatEnumLabel(currentUser.role));
    setText("accountContributorChip", formatEnumLabel(currentUser.contributorStatus));
    setText("accountStatusText", buildAccountStatusCopy(currentUser));

    const workspaceLink = document.getElementById("workspaceLink");
    if (workspaceLink) {
        workspaceLink.classList.toggle("hidden", !currentUser.contributor);
    }

    const adminLink = document.getElementById("adminApprovalLink");
    if (adminLink) {
        adminLink.classList.toggle("hidden", currentUser.role !== "ADMINISTRATOR");
    }
}

function populateAccountForm(currentUser) {
    setValue("accountName", currentUser.name);
    setValue("accountEmail", currentUser.email);
}

function populateContributorPanel(currentUser, latestRequest) {
    const actionText = document.getElementById("contributorActionText");
    const actionButton = document.getElementById("submitContributorRequestBtn");
    if (!actionText || !actionButton) return;

    if (currentUser.role === "ADMINISTRATOR") {
        actionText.textContent = "Administrators review contributor requests and do not submit them.";
        actionButton.classList.add("hidden");
        return;
    }

    actionButton.classList.remove("hidden");

    if (currentUser.contributor) {
        actionText.textContent = "Your contributor request has been approved. You can now open the contributor workspace.";
        actionButton.classList.add("hidden");
        return;
    }

    if (latestRequest && latestRequest.status === "PENDING") {
        actionText.textContent = "Your contributor request is currently pending review.";
        actionButton.classList.add("hidden");
        return;
    }

    if (latestRequest && latestRequest.status === "REJECTED") {
        actionText.textContent = "Your last contributor request was rejected. You can submit a new request.";
        actionButton.classList.remove("hidden");
        return;
    }

    actionText.textContent = "You are a Registered Viewer. Submit a contributor request when you are ready.";
    actionButton.classList.remove("hidden");
}

function populateLatestRequestCard(latestRequest) {
    const container = document.getElementById("latestContributorRequest");
    if (!container) return;

    if (!latestRequest) {
        container.className = "request-detail-card empty-state";
        container.textContent = "No contributor request has been submitted yet.";
        return;
    }

    container.className = "request-detail-card";
    container.innerHTML = `
        <div class="detail-grid">
            <div class="detail-item">
                <span class="detail-label">Applicant</span>
                <strong>${escapeAccessHtml(latestRequest.userName || "-")}</strong>
            </div>
            <div class="detail-item">
                <span class="detail-label">Email</span>
                <strong>${escapeAccessHtml(latestRequest.userEmail || "-")}</strong>
            </div>
            <div class="detail-item">
                <span class="detail-label">Status</span>
                <strong>${escapeAccessHtml(formatEnumLabel(latestRequest.status))}</strong>
            </div>
            <div class="detail-item">
                <span class="detail-label">Requested At</span>
                <strong>${escapeAccessHtml(formatDateTime(latestRequest.requestedAt))}</strong>
            </div>
            <div class="detail-item">
                <span class="detail-label">Reviewed At</span>
                <strong>${escapeAccessHtml(formatDateTime(latestRequest.reviewedAt))}</strong>
            </div>
            <div class="detail-item">
                <span class="detail-label">Review Comment</span>
                <strong>${escapeAccessHtml(latestRequest.reviewComment || "-")}</strong>
            </div>
        </div>
    `;
}

async function loadPendingRequestList() {
    const container = document.getElementById("pendingRequestList");
    if (!container) return;

    container.innerHTML = `<div class="empty-state">Loading pending requests...</div>`;

    try {
        const requests = await accessRequestJson(`${ACCESS_ADMIN_API}/pending`, { method: "GET" });

        if (!requests || requests.length === 0) {
            container.innerHTML = `<div class="empty-state">There are no pending contributor requests right now.</div>`;
            return;
        }

        container.innerHTML = requests.map(request => `
            <article class="approval-item">
                <div class="approval-item-head">
                    <div>
                        <h3>${escapeAccessHtml(request.userName || "Unknown Applicant")}</h3>
                        <p>${escapeAccessHtml(request.userEmail || "-")}</p>
                    </div>
                    <span class="status-pill status-pending_review">${escapeAccessHtml(formatEnumLabel(request.status))}</span>
                </div>

                <div class="approval-item-grid">
                    <div class="detail-item">
                        <span class="detail-label">Request ID</span>
                        <strong>${request.requestId ?? "-"}</strong>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Requested At</span>
                        <strong>${escapeAccessHtml(formatDateTime(request.requestedAt))}</strong>
                    </div>
                </div>

                <div class="field">
                    <label for="reviewComment-${request.requestId}">Review Comment</label>
                    <textarea id="reviewComment-${request.requestId}" rows="3" placeholder="Optional comment for the applicant"></textarea>
                </div>

                <div class="portal-action-row">
                    <button type="button" class="btn btn-primary" data-request-id="${request.requestId}" data-decision="APPROVED">Approve</button>
                    <button type="button" class="btn btn-secondary" data-request-id="${request.requestId}" data-decision="REJECTED">Reject</button>
                </div>
            </article>
        `).join("");
    } catch (error) {
        container.innerHTML = `<div class="empty-state">${escapeAccessHtml(error.message || "Unable to load pending requests.")}</div>`;
    }
}

function renderHomeSession(currentUser) {
    const card = document.getElementById("homeSessionCard");
    if (!card) return;

    if (!currentUser) {
        card.className = "status-panel muted-panel";
        card.innerHTML = `
            <div class="status-panel-title">Current Session</div>
            <p>Not logged in yet. Open the login page or create a new account.</p>
        `;
        return;
    }

    card.className = "status-panel";
    card.innerHTML = `
        <div class="status-panel-title">${escapeAccessHtml(currentUser.name)}</div>
        <p>
            Logged in as ${escapeAccessHtml(formatEnumLabel(currentUser.role))}.
            Contributor status: ${escapeAccessHtml(formatEnumLabel(currentUser.contributorStatus))}.
        </p>
        <div class="portal-action-row">
            <a class="btn btn-secondary" href="${escapeAccessHtml(resolvePostLoginRedirect(currentUser))}">Continue</a>
            <button type="button" class="btn btn-ghost" data-logout-btn>Log Out</button>
        </div>
    `;
}

function buildAccountStatusCopy(currentUser) {
    if (currentUser.role === "ADMINISTRATOR") {
        return "You can review contributor applications and maintain your own account profile.";
    }

    if (currentUser.contributor) {
        return "Your contributor request is approved, so the resource creation workspace is available.";
    }

    if (currentUser.contributorStatus === "PENDING") {
        return "Your contributor request is pending. Contributor-only resource submission remains locked until approval.";
    }

    if (currentUser.contributorStatus === "REJECTED") {
        return "Your last contributor request was rejected. You can update your account details and submit a new request.";
    }

    return "You are currently a Registered Viewer. Basic account maintenance is available, but contributor-only pages remain locked.";
}

async function requireAuthenticatedUser() {
    try {
        return await accessRequestJson(`${ACCESS_AUTH_API}/me`, { method: "GET" });
    } catch (error) {
        if (error.status === 401) {
            const next = `${window.location.pathname}${window.location.search}`;
            window.location.href = `./login.html?next=${encodeURIComponent(next)}`;
        } else {
            showAccessToast(error.message || "Authentication is required.");
        }
        throw error;
    }
}

function bindAccessLogoutButtons() {
    document.addEventListener("click", async (event) => {
        const button = event.target.closest("[data-logout-btn]");
        if (!button) return;

        event.preventDefault();

        try {
            await accessRequestJson(`${ACCESS_AUTH_API}/logout`, { method: "POST" });
        } catch (error) {
            // Redirect to the public page even if the session has already expired.
        }

        window.location.href = `./index.html?message=${encodeURIComponent("You have logged out successfully.")}`;
    });
}

function resolvePostLoginRedirect(currentUser) {
    const params = new URLSearchParams(window.location.search);
    const next = params.get("next");
    if (next) {
        return next;
    }

    if (currentUser.role === "ADMINISTRATOR") {
        return "./admin-approval.html";
    }

    if (currentUser.contributor) {
        return "./my-resources.html";
    }

    return "./account.html";
}

function showMessageFromQuery() {
    const params = new URLSearchParams(window.location.search);
    const message = params.get("message");
    if (message) {
        showAccessToast(message);
    }
}

async function accessRequestJson(url, options = {}) {
    let response;

    try {
        response = await fetch(url, options);
    } catch (error) {
        const requestError = new Error("Unable to connect to the server.");
        requestError.isNetworkError = true;
        throw requestError;
    }

    if (!response.ok) {
        let message = "Request failed.";
        let details = [];

        try {
            const data = await response.json();
            message = data.message || message;
            details = Array.isArray(data.details) ? data.details : [];
        } catch (error) {
            try {
                message = await response.text();
            } catch (ignored) {
            }
        }

        const requestError = new Error(details.length ? `${message} ${details.join(" ")}` : message);
        requestError.status = response.status;
        requestError.details = details;
        throw requestError;
    }

    const contentType = response.headers.get("content-type") || "";
    if (!contentType.includes("application/json")) {
        return null;
    }

    return response.json();
}

function showAccessToast(message) {
    const toast = document.getElementById("toast");
    if (!toast) return;

    toast.textContent = message;
    toast.classList.add("show");

    clearTimeout(showAccessToast._timer);
    showAccessToast._timer = window.setTimeout(() => {
        toast.classList.remove("show");
    }, 2800);
}

function escapeAccessHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function formatEnumLabel(value) {
    if (!value) return "None";
    return value
        .toString()
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, char => char.toUpperCase());
}

function formatDateTime(value) {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    return date.toLocaleString();
}

function setText(id, value) {
    const element = document.getElementById(id);
    if (!element) return;
    element.textContent = value ?? "";
}

function setValue(id, value) {
    const element = document.getElementById(id);
    if (!element) return;
    element.value = value ?? "";
}
