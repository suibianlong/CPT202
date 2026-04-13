const AUTH_API_BASE = "/api/auth";
const API_BASE = "/api/contributor/resources";
const HERO_IMAGE_FILES = ["1.jpg", "2.jpg", "3.jpg", "4.jpg", "5.jpg", "6.jpg"];
const PANEL_IMAGE_FILES = ["7.jpg", "8.jpg", "9.jpg", "10.jpg", "11.jpg", "12.jpg", "13.jpg", "14.jpg"];
const DASHBOARD_PANEL_IMAGE_FILES = ["15.jpg", "16.jpg", "17.jpg", "18.jpg", "19.jpg", "20.jpg", "21.jpg", "22.jpg"];
const MAIN_BACKGROUND_IMAGE_FILES = ["23.jpg", "24.jpg", "25.jpg"];
const SCROLL_SCENE_IMAGE_FILES = ["23.jpg", "24.jpg", "25.jpg", "26.jpg"];
const HERITAGE_MODULE_META = {
    "module-identity": {
        label: "Heritage Identity",
        hint: "Fill in the title, category, and resource type.",
        focusId: "title"
    },
    "module-location": {
        label: "Location",
        hint: "Add the place linked to this resource.",
        focusId: "place"
    },
    "module-media": {
    label: "Media Upload",
    hint: "Choose a resource type and upload the primary media file.",
    focusId: "resourceTypeMirror"
    },
    "module-description": {
        label: "Description",
        hint: "Write the historical background and cultural notes.",
        focusId: "description"
    },
    "module-preview": {
        label: "Preview Image",
        hint: "Upload a preview image for cards and cover display.",
        focusId: "previewImage"
    },
    "module-submit": {
        label: "Submit Review",
        hint: "Add a submission note and prepare the draft for review.",
        focusId: "submissionNote"
    },
    "module-tags": {
        label: "Tags & Keywords",
        hint: "Select tags and keywords for this resource.",
        focusId: "tagOptions"
    }
};

const mockCategoryOptions = [
    { id: 1, name: "Architecture" },
    { id: 2, name: "Tradition" },
    { id: 3, name: "Artifact" },
    { id: 4, name: "Place" }
];

const mockTagOptions = [
    { id: 1, name: "Temple" },
    { id: 2, name: "Local Memory" },
    { id: 3, name: "Festival" },
    { id: 4, name: "Village" },
    { id: 5, name: "Oral History" }
];

const mockResources = [
    {
        id: 101,
        title: "Traditional Temple Entrance",
        categoryId: 1,
        resourceType: "IMAGE",
        status: "DRAFT",
        updatedAt: "2026-04-08T16:20:00"
    },
    {
        id: 102,
        title: "Festival Parade Recording",
        categoryId: 2,
        resourceType: "VIDEO",
        status: "PENDING_REVIEW",
        updatedAt: "2026-04-07T13:15:00"
    }
];

let categoryOptionCache = [];
let useMockCategoryOptions = false;
let savedResourceTypeValue = "";
let pendingResourceTypeSave = Promise.resolve();

document.addEventListener("DOMContentLoaded", () => {
    const page = document.body.dataset.page;
    bindWorkspaceLogoutButtons();
    showWorkspaceMessageFromQuery();

    if (page === "my-resources") {
        initMyResourcesPage();
    }

    if (page === "resource-edit") {
        initResourceEditPage();
    }
});

async function initMyResourcesPage() {
    const currentUser = await ensureContributorWorkspaceAccess();
    populateWorkspaceSession(currentUser);
    applyMyResourcesArtwork();
    bindCreateDraftButton();
    bindListFilterButtons();
    await loadCategoryFilterOptions();
    await loadResourceList();
}

async function initResourceEditPage() {
    const currentUser = await ensureContributorWorkspaceAccess();
    populateWorkspaceSession(currentUser);
    applyRandomEditorArtwork();
    bindHeritageLanding();
    bindModuleModalUI();
    bindCreateDraftButton();
    bindFilePickerUI();
    bindResourceTypeMirror();
    bindMetadataForm();
    bindUploadForm();
    bindSubmitForm();

    await loadCategorySelectOptions();
    await loadTagOptions();

    const resourceId = getResourceIdFromQuery();

    if (resourceId) {
        await loadResourceDetail(resourceId);
    } else {
        updateEditorMeta(null);
    }
}

function bindFilePickerUI() {
    bindSingleFilePicker("mediaFile", "mediaFileNameText");
    bindSingleFilePicker("previewImage", "previewImageNameText");
}

function bindSingleFilePicker(inputId, textId) {
    const input = document.getElementById(inputId);
    const text = document.getElementById(textId);
    const button = document.querySelector(`[data-file-target="${inputId}"]`);
    if (!input || !text) return;

    if (button) {
        button.addEventListener("click", () => {
            if (typeof input.showPicker === "function") {
                try {
                    input.showPicker();
                    return;
                } catch (error) {
                    // Fallback when showPicker exists but is rejected in this context.
                }
            }

            input.click();
        });
    }

    input.addEventListener("change", () => {
        const file = input.files?.[0];
        text.textContent = file ? file.name : "No file selected";
    });
}

function applyRandomEditorArtwork() {
    const scrollScene = document.querySelector('[data-random-art="scroll-scene"]');
    if (scrollScene) {
        const sceneImage = pickRandomItem(SCROLL_SCENE_IMAGE_FILES);
        scrollScene.style.setProperty("--scroll-scene-image", `url("./${sceneImage}")`);
    }

    const hero = document.querySelector('[data-random-art="hero"]');
    if (hero) {
        const heroImage = pickRandomItem(HERO_IMAGE_FILES);
        hero.style.setProperty("--hero-bg-image", `url("./${heroImage}")`);
    }

    const panels = Array.from(document.querySelectorAll('[data-random-art="panel"]'));
    if (!panels.length) return;

    const panelImages = shuffleArray([...PANEL_IMAGE_FILES]);
    panels.forEach((panel, index) => {
        const panelImage = panelImages[index % panelImages.length];
        panel.style.setProperty(
            "--panel-bg-image",
            `linear-gradient(180deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.08)), url("./${panelImage}")`
        );
    });
}

function bindHeritageLanding() {
    const nodes = Array.from(document.querySelectorAll(".heritage-node, .module-pill"));
    if (nodes.length === 0) return;

    document.body.classList.add("module-flow-enabled");

    window.setTimeout(() => {
        document.body.classList.add("scroll-unfurled");
    }, 180);

    nodes.forEach(node => {
        node.addEventListener("click", (event) => {
            event.preventDefault();
            openHeritageModuleModal(getModuleIdFromTrigger(node));
        });
    });

    const initialModuleId = getInitialModuleId();
    if (window.location.hash) {
        window.setTimeout(() => {
            openHeritageModuleModal(initialModuleId, { shouldFocus: false, updateHistory: false });
        }, 1200);
    } else {
        resetActiveModuleMeta();
        clearHeritageModuleSelection();
    }
}

function bindModuleModalUI() {
    document.querySelectorAll("[data-close-module-modal]").forEach(element => {
        element.addEventListener("click", (event) => {
            event.preventDefault();
            closeHeritageModuleModal();
        });
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && document.body.classList.contains("module-modal-open")) {
            closeHeritageModuleModal();
        }
    });
}

function applyMyResourcesArtwork() {
    const main = document.querySelector('[data-random-art="main"]');
    if (main) {
        const mainImage = pickRandomItem(MAIN_BACKGROUND_IMAGE_FILES);
        main.style.setProperty("--page-bg-image", `url("./${mainImage}")`);
    }

    const hero = document.querySelector('[data-random-art="hero"]');
    if (hero) {
        const heroImage = pickRandomItem(HERO_IMAGE_FILES);
        hero.style.setProperty("--hero-bg-image", `url("./${heroImage}")`);
    }

    const panels = Array.from(document.querySelectorAll('[data-random-art="panel"]'));
    if (!panels.length) return;

    const panelImages = shuffleArray([...DASHBOARD_PANEL_IMAGE_FILES]);
    panels.forEach((panel, index) => {
        const panelImage = panelImages[index % panelImages.length];
        panel.style.setProperty(
            "--panel-bg-image",
            `linear-gradient(180deg, rgba(255, 255, 255, 0.18), rgba(255, 255, 255, 0.06)), url("./${panelImage}")`
        );
    });
}

function pickRandomItem(items) {
    return items[Math.floor(Math.random() * items.length)];
}

function shuffleArray(items) {
    for (let i = items.length - 1; i > 0; i -= 1) {
        const j = Math.floor(Math.random() * (i + 1));
        [items[i], items[j]] = [items[j], items[i]];
    }
    return items;
}

function bindCreateDraftButton() {
    const createDraftBtn = document.getElementById("createDraftBtn");
    if (!createDraftBtn) return;

    createDraftBtn.addEventListener("click", async () => {
        createDraftBtn.disabled = true;

        try {
            const resource = await requestJson(API_BASE, {
                method: "POST"
            });

            showToast("Draft created successfully.");
            window.location.href = `./resource-edit.html?id=${resource.id}`;
        } catch (error) {
            showToast(error.message || "Failed to create draft.");
        } finally {
            createDraftBtn.disabled = false;
        }
    });
}

function bindResourceTypeMirror() {
    const primarySelect = document.getElementById("resourceType");
    const mirrorSelect = document.getElementById("resourceTypeMirror");
    if (!primarySelect || !mirrorSelect) return;

    primarySelect.addEventListener("change", () => {
        mirrorSelect.value = primarySelect.value;
    });

    mirrorSelect.addEventListener("change", () => {
        primarySelect.value = mirrorSelect.value;
        pendingResourceTypeSave = persistResourceTypeSelection({ showError: true });
    });

    syncResourceTypeMirror();
}

function bindListFilterButtons() {
    const searchBtn = document.getElementById("searchBtn");
    const resetBtn = document.getElementById("resetBtn");
    const keywordInput = document.getElementById("keyword");

    if (searchBtn) {
        searchBtn.addEventListener("click", loadResourceList);
    }

    if (resetBtn) {
        resetBtn.addEventListener("click", async () => {
            document.getElementById("keyword").value = "";
            document.getElementById("statusFilter").value = "";
            document.getElementById("categoryFilter").value = "";
            await loadResourceList();
        });
    }

    if (keywordInput) {
        keywordInput.addEventListener("keydown", async (event) => {
            if (event.key === "Enter") {
                event.preventDefault();
                await loadResourceList();
            }
        });
    }
}

async function loadCategoryFilterOptions() {
    const select = document.getElementById("categoryFilter");
    if (!select) return;

    try {
        const options = await getCategoryOptions();
        renderSelectOptions(select, options, "All Categories");
    } catch (error) {
        categoryOptionCache = [];
        useMockCategoryOptions = false;
        renderSelectOptions(select, [], "All Categories");
        showToast(error.message || "Failed to load categories.");
    }
}

async function loadCategorySelectOptions() {
    const select = document.getElementById("categoryId");
    if (!select) return;

    try {
        const options = await getCategoryOptions();
        renderSelectOptions(select, options, "Select category");
    } catch (error) {
        categoryOptionCache = [];
        useMockCategoryOptions = false;
        renderSelectOptions(select, [], "Select category");
        showToast(error.message || "Failed to load categories.");
    }
}

async function loadTagOptions() {
    const container = document.getElementById("tagOptions");
    if (!container) return;

    try {
        const options = await getTagOptions();
        renderTagChips(container, options, []);
    } catch (error) {
        renderTagChips(container, [], []);
        showToast(error.message || "Failed to load tags.");
    }
}

async function loadResourceList() {
    const tbody = document.getElementById("resourceTableBody");
    const listMeta = document.getElementById("listMeta");

    if (!tbody) return;

    tbody.innerHTML = `<tr><td colspan="7" class="empty-row">Loading resources...</td></tr>`;

    try {
        const keyword = document.getElementById("keyword")?.value?.trim() || "";
        const status = document.getElementById("statusFilter")?.value || "";
        const categoryId = document.getElementById("categoryFilter")?.value || "";

        const query = new URLSearchParams();
        if (keyword) query.set("keyword", keyword);
        if (status) query.set("status", status);
        if (categoryId) query.set("categoryId", categoryId);

        const url = `${API_BASE}/my${query.toString() ? `?${query.toString()}` : ""}`;
        const resources = await requestJson(url, { method: "GET" });

        renderResourceTable(resources);
        if (listMeta) {
            listMeta.textContent = `${resources.length} item(s)`;
        }
    } catch (error) {
        if (error.isNetworkError) {
            const resources = filterMockResources();
            renderResourceTable(resources);
            if (listMeta) {
                listMeta.textContent = `${resources.length} item(s) · mock`;
            }
            showToast("Backend not available. Mock list loaded.");
            return;
        }

        tbody.innerHTML = `<tr><td colspan="7" class="empty-row">${escapeHtml(error.message || "Failed to load resources.")}</td></tr>`;
        if (listMeta) {
            listMeta.textContent = "Load failed";
        }
        showToast(error.message || "Failed to load resources.");
    }
}

function renderResourceTable(resources) {
    const tbody = document.getElementById("resourceTableBody");
    if (!tbody) return;

    if (!resources || resources.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-row">No resources found.</td></tr>`;
        return;
    }

    tbody.innerHTML = resources.map(resource => {
        const categoryText = getCategoryNameById(resource.categoryId);
        const updatedAtText = formatDateTime(resource.updatedAt);

        return `
            <tr>
                <td>${resource.id ?? "-"}</td>
                <td>${escapeHtml(resource.title ?? "-")}</td>
                <td>${escapeHtml(categoryText)}</td>
                <td>${escapeHtml(resource.resourceType ?? "-")}</td>
                <td><span class="status-pill status-${String(resource.status || "").toLowerCase()}">${formatStatus(resource.status)}</span></td>
                <td>${updatedAtText}</td>
                <td>
                    <a class="action-link" href="./resource-edit.html?id=${resource.id}">Edit</a>
                </td>
            </tr>
        `;
    }).join("");
}

function bindMetadataForm() {
    const form = document.getElementById("metadataForm");
    if (!form) return;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const resourceId = getResourceIdFromQuery();
        if (!resourceId) {
            showToast("Please create a draft first.");
            return;
        }

        const payload = {
            title: document.getElementById("title").value.trim(),
            categoryId: parseNullableLong(document.getElementById("categoryId").value),
            place: document.getElementById("place").value.trim(),
            description: document.getElementById("description").value.trim(),
            resourceType: document.getElementById("resourceType").value,
            tagIds: getSelectedTagIds()
        };

        try {
            const detail = await requestJson(`${API_BASE}/${resourceId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            fillEditor(detail);
            showToast("Metadata saved successfully.");
        } catch (error) {
            showToast(error.message || "Failed to save metadata.");
        }
    });
}

function bindUploadForm() {
    const form = document.getElementById("uploadForm");
    if (!form) return;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const resourceId = getResourceIdFromQuery();
        if (!resourceId) {
            showToast("Please create a draft first.");
            return;
        }

        const previewImage = document.getElementById("previewImage").files[0];
        const mediaFile = document.getElementById("mediaFile").files[0];

        if (!previewImage && !mediaFile) {
            showToast("Select at least one file.");
            return;
        }

        const formData = new FormData();
        if (previewImage) formData.append("previewImage", previewImage);
        if (mediaFile) formData.append("mediaFile", mediaFile);

        try {
            try {
                await pendingResourceTypeSave;
            } catch (error) {
                pendingResourceTypeSave = Promise.resolve();
            }

            await persistResourceTypeSelection({ showError: false });

            const detail = await requestJson(`${API_BASE}/${resourceId}/files`, {
                method: "POST",
                body: formData
            });

            fillEditor(detail);
            showToast("Files uploaded successfully.");
        } catch (error) {
            showToast(error.message || "Failed to upload files.");
        }
    });
}

function bindSubmitForm() {
    const form = document.getElementById("submitForm");
    if (!form) return;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const resourceId = getResourceIdFromQuery();
        if (!resourceId) {
            showToast("Please create a draft first.");
            return;
        }

        const payload = {
            submissionNote: document.getElementById("submissionNote").value.trim()
        };

        try {
            await requestJson(`${API_BASE}/${resourceId}/submit`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            showToast("Submitted for review.");
            await loadResourceDetail(resourceId);
        } catch (error) {
            showToast(error.message || "Failed to submit resource.");
        }
    });
}

async function loadResourceDetail(resourceId) {
    try {
        const detail = await requestJson(`${API_BASE}/${resourceId}`, { method: "GET" });
        fillEditor(detail);
    } catch (error) {
        if (error.isNetworkError) {
            showToast("Backend not available. Editor remains in local mode.");
            updateEditorMeta({ id: resourceId, status: "DRAFT" });
            return;
        }

        showToast(error.message || "Failed to load resource detail.");
    }
}

function fillEditor(detail) {
    if (!detail) return;

    setValue("title", detail.title);
    setValue("categoryId", detail.categoryId);
    setValue("place", detail.place);
    setValue("description", detail.description);
    setValue("resourceType", detail.resourceType);
    setValue("submissionNote", "");

    const previewText = document.getElementById("previewImagePathText");
    const mediaText = document.getElementById("mediaFilePathText");

    if (previewText) {
        previewText.textContent = detail.previewImage || "—";
    }

    if (mediaText) {
        mediaText.textContent = detail.mediaUrl || "—";
    }

    syncResourceTypeMirror();
    savedResourceTypeValue = detail.resourceType ?? "";
    updateEditorMeta(detail);

    const tagContainer = document.getElementById("tagOptions");
    if (tagContainer) {
        getTagOptions()
            .then(options => {
                renderTagChips(tagContainer, options, detail.tagIds || []);
            })
            .catch(error => {
                renderTagChips(tagContainer, [], detail.tagIds || []);
                showToast(error.message || "Failed to load tags.");
            });
    }
}

function updateEditorMeta(detail) {
    const resourceIdText = document.getElementById("resourceIdText");
    const resourceStatusText = document.getElementById("resourceStatusText");
    const updatedAtText = document.getElementById("updatedAtText");
    const badge = document.getElementById("resourceStatusBadge");

    const idValue = detail?.id ?? getResourceIdFromQuery() ?? "Not created";
    const statusValue = detail?.status ?? "DRAFT";
    const updatedValue = detail?.updatedAt ? formatDateTime(detail.updatedAt) : "—";

    if (resourceIdText) resourceIdText.textContent = idValue;
    if (resourceStatusText) resourceStatusText.textContent = formatStatus(statusValue);
    if (updatedAtText) updatedAtText.textContent = updatedValue;

    if (badge) {
        badge.textContent = formatStatus(statusValue);
        badge.className = `status-pill status-${String(statusValue).toLowerCase()}`;
    }
}

function getInitialModuleId() {
    const hashValue = window.location.hash.replace("#", "");
    if (hashValue && HERITAGE_MODULE_META[hashValue]) {
        return hashValue;
    }
    return "module-identity";
}

function getModuleIdFromTrigger(trigger) {
    const targetId = trigger?.dataset?.target;
    if (targetId) return targetId;

    const href = trigger?.getAttribute?.("href") || "";
    if (href.startsWith("#")) {
        return href.slice(1);
    }

    return "";
}

function activateHeritageModule(moduleId, options = {}) {
    if (!moduleId) return;

    const target = document.getElementById(moduleId);
    if (!target) return;

    const activeForm = target.closest("form");

    document.querySelectorAll(".module-stack").forEach(form => {
        form.classList.toggle("active-workspace", form === activeForm);
    });

    document.querySelectorAll(".module-card").forEach(card => {
        const isActive = card === target;
        card.classList.toggle("is-active", isActive);
        card.setAttribute("aria-hidden", isActive ? "false" : "true");
    });

    document.querySelectorAll(".heritage-node, .module-pill").forEach(trigger => {
        const isActive = getModuleIdFromTrigger(trigger) === moduleId;
        trigger.classList.toggle("is-active", isActive);
        trigger.setAttribute("aria-current", isActive ? "true" : "false");
    });

    updateActiveModuleMeta(moduleId);
    highlightModuleCard(target);

    if (options.shouldScroll) {
        target.scrollIntoView({ behavior: "smooth", block: "start" });
    }

    if (options.shouldFocus) {
        focusModuleField(moduleId);
    }

    if (options.updateHistory !== false && window.history?.replaceState) {
        window.history.replaceState(null, "", `#${moduleId}`);
    }
}

function openHeritageModuleModal(moduleId, options = {}) {
    if (!moduleId) return;

    document.body.classList.add("module-modal-open");
    activateHeritageModule(moduleId, {
        shouldScroll: false,
        shouldFocus: options.shouldFocus !== false,
        updateHistory: options.updateHistory
    });
}

function closeHeritageModuleModal() {
    document.body.classList.remove("module-modal-open");
    clearHeritageModuleSelection();
    resetActiveModuleMeta();

    if (window.history?.replaceState) {
        const cleanUrl = `${window.location.pathname}${window.location.search}`;
        window.history.replaceState(null, "", cleanUrl);
    }
}

function highlightModuleCard(target) {
    if (!target?.classList) return;

    document.querySelectorAll(".module-card.module-focus").forEach(card => {
        card.classList.remove("module-focus");
    });

    target.classList.add("module-focus");

    window.clearTimeout(highlightModuleCard._timer);
    highlightModuleCard._timer = window.setTimeout(() => {
        target.classList.remove("module-focus");
    }, 1800);
}

function clearHeritageModuleSelection() {
    document.querySelectorAll(".module-stack").forEach(form => {
        form.classList.remove("active-workspace");
    });

    document.querySelectorAll(".module-card").forEach(card => {
        card.classList.remove("is-active", "module-focus");
        card.setAttribute("aria-hidden", "true");
    });

    document.querySelectorAll(".heritage-node, .module-pill").forEach(trigger => {
        trigger.classList.remove("is-active");
        trigger.setAttribute("aria-current", "false");
    });
}

function updateActiveModuleMeta(moduleId) {
    const meta = HERITAGE_MODULE_META[moduleId];
    if (!meta) return;

    const label = document.getElementById("activeModuleLabel");
    const hint = document.getElementById("activeModuleHint");

    if (label) {
        label.textContent = meta.label;
    }

    if (hint) {
        hint.textContent = meta.hint;
    }
}

function resetActiveModuleMeta() {
    const label = document.getElementById("activeModuleLabel");
    const hint = document.getElementById("activeModuleHint");

    if (label) {
        label.textContent = "Click an icon to begin";
    }

    if (hint) {
        hint.textContent = "Select any icon on the scroll to open the matching entry panel.";
    }
}

function focusModuleField(moduleId) {
    const meta = HERITAGE_MODULE_META[moduleId];
    if (!meta) return;

    window.setTimeout(() => {
        let element = document.getElementById(meta.focusId);

        if (moduleId === "module-tags") {
            element = document.querySelector("#tagOptions .chip") || element;
        }

        if (!element) return;

        if (typeof element.focus === "function") {
            element.focus({ preventScroll: true });
        }
    }, 420);
}

function syncResourceTypeMirror() {
    const primarySelect = document.getElementById("resourceType");
    const mirrorSelect = document.getElementById("resourceTypeMirror");
    if (!primarySelect || !mirrorSelect) return;

    mirrorSelect.value = primarySelect.value;
}

async function persistResourceTypeSelection(options = {}) {
    const primarySelect = document.getElementById("resourceType");
    const mirrorSelect = document.getElementById("resourceTypeMirror");
    const resourceId = getResourceIdFromQuery();

    if (!primarySelect || !mirrorSelect || !resourceId) {
        return null;
    }

    const currentValue = mirrorSelect.value ?? "";
    primarySelect.value = currentValue;

    if (!options.force && currentValue === savedResourceTypeValue) {
        return null;
    }

    try {
        const detail = await requestJson(`${API_BASE}/${resourceId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                resourceType: currentValue
            })
        });

        if (detail) {
            fillEditor(detail);
        } else {
            savedResourceTypeValue = currentValue;
        }

        return detail;
    } catch (error) {
        if (options.showError !== false) {
            showToast(error.message || "Failed to save resource type.");
        }
        throw error;
    }
}

function renderSelectOptions(selectElement, options, placeholderText) {
    const currentValue = selectElement.value;
    const html = [`<option value="">${placeholderText}</option>`];

    options.forEach(option => {
        html.push(`<option value="${option.id}">${escapeHtml(option.name)}</option>`);
    });

    selectElement.innerHTML = html.join("");

    if (currentValue) {
        selectElement.value = currentValue;
    }
}

function renderTagChips(container, options, selectedIds) {
    const selectedSet = new Set((selectedIds || []).map(Number));

    container.innerHTML = options.map(option => {
        const active = selectedSet.has(Number(option.id)) ? "active" : "";
        return `
            <button
                type="button"
                class="chip ${active}"
                data-tag-id="${option.id}">
                ${escapeHtml(option.name)}
            </button>
        `;
    }).join("");

    container.querySelectorAll(".chip").forEach(chip => {
        chip.addEventListener("click", () => {
            chip.classList.toggle("active");
        });
    });
}

function getSelectedTagIds() {
    const chips = document.querySelectorAll("#tagOptions .chip.active");
    return Array.from(chips).map(chip => Number(chip.dataset.tagId));
}

async function getCategoryOptions() {
    try {
        const options = await requestJson(`${API_BASE}/category-options`, { method: "GET" });
        categoryOptionCache = Array.isArray(options) ? options : [];
        useMockCategoryOptions = false;
        return categoryOptionCache;
    } catch (error) {
        if (error.isNetworkError) {
            categoryOptionCache = mockCategoryOptions;
            useMockCategoryOptions = true;
            return mockCategoryOptions;
        }
        categoryOptionCache = [];
        useMockCategoryOptions = false;
        throw error;
    }
}

async function getTagOptions() {
    try {
        return await requestJson(`${API_BASE}/tag-options`, { method: "GET" });
    } catch (error) {
        if (error.isNetworkError) {
            return mockTagOptions;
        }
        throw error;
    }
}

async function ensureContributorWorkspaceAccess() {
    try {
        const currentUser = await requestJson(`${AUTH_API_BASE}/me`, { method: "GET" });
        if (!currentUser?.contributor) {
            window.location.href = `./account.html?message=${encodeURIComponent("Contributor access requires an approved contributor request.")}`;
            throw new Error("Contributor access required.");
        }
        return currentUser;
    } catch (error) {
        if (error.status === 401) {
            const next = `${window.location.pathname}${window.location.search}`;
            window.location.href = `./login.html?next=${encodeURIComponent(next)}`;
            throw error;
        }

        if (error.status === 403) {
            window.location.href = `./account.html?message=${encodeURIComponent(error.message || "Contributor access required.")}`;
            throw error;
        }

        throw error;
    }
}

function populateWorkspaceSession(currentUser) {
    const nameNode = document.getElementById("resourceUserName");
    const accessNode = document.getElementById("resourceAccessText");

    if (nameNode) {
        nameNode.textContent = currentUser?.name || "Contributor";
    }

    if (accessNode) {
        accessNode.textContent = currentUser?.contributor
            ? "Approved contributor access is active for this workspace."
            : "Contributor permission is required for this workspace.";
    }
}

function bindWorkspaceLogoutButtons() {
    document.addEventListener("click", async (event) => {
        const button = event.target.closest("[data-logout-btn]");
        if (!button) return;

        event.preventDefault();

        try {
            await requestJson(`${AUTH_API_BASE}/logout`, { method: "POST" });
        } catch (error) {
            // Redirect even if the session has already expired.
        }

        window.location.href = `./index.html?message=${encodeURIComponent("You have logged out successfully.")}`;
    });
}

function showWorkspaceMessageFromQuery() {
    const params = new URLSearchParams(window.location.search);
    const message = params.get("message");
    if (message) {
        showToast(message);
    }
}

async function requestJson(url, options = {}) {
    let response;

    try {
        response = await fetch(url, options);
    } catch (error) {
        const networkError = new Error("Unable to connect to server.");
        networkError.isNetworkError = true;
        throw networkError;
    }

    if (!response.ok) {
        let message = "Request failed.";

        try {
            const data = await response.json();
            message = data.message || message;
        } catch (error) {
            try {
                message = await response.text();
            } catch (ignored) {
            }
        }

        const requestError = new Error(message);
        requestError.status = response.status;
        requestError.isNetworkError = false;
        throw requestError;
    }

    const contentType = response.headers.get("content-type") || "";
    if (!contentType.includes("application/json")) {
        return null;
    }

    return response.json();
}

function getResourceIdFromQuery() {
    const params = new URLSearchParams(window.location.search);
    return params.get("id");
}

function parseNullableLong(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }
    return Number(value);
}

function setValue(id, value) {
    const element = document.getElementById(id);
    if (!element) return;
    element.value = value ?? "";
}

function formatStatus(status) {
    if (!status) return "-";
    return status
        .toString()
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, char => char.toUpperCase());
}

function formatDateTime(value) {
    if (!value) return "—";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
}

function getCategoryNameById(categoryId) {
    const item = categoryOptionCache.find(option => Number(option.id) === Number(categoryId))
        || (useMockCategoryOptions
            ? mockCategoryOptions.find(option => Number(option.id) === Number(categoryId))
            : null);
    return item ? item.name : (categoryId ?? "-");
}

function filterMockResources() {
    const keyword = document.getElementById("keyword")?.value?.trim().toLowerCase() || "";
    const status = document.getElementById("statusFilter")?.value || "";
    const categoryId = document.getElementById("categoryFilter")?.value || "";

    return mockResources.filter(item => {
        const matchesKeyword = !keyword || (item.title || "").toLowerCase().includes(keyword);
        const matchesStatus = !status || item.status === status;
        const matchesCategory = !categoryId || String(item.categoryId) === String(categoryId);
        return matchesKeyword && matchesStatus && matchesCategory;
    });
}

function showToast(message) {
    const toast = document.getElementById("toast");
    if (!toast) return;

    toast.textContent = message;
    toast.classList.add("show");

    clearTimeout(showToast._timer);
    showToast._timer = setTimeout(() => {
        toast.classList.remove("show");
    }, 2600);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
