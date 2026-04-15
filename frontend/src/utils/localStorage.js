/**
 * LocalStorage utility for draft persistence
 */

const DRAFT_PREFIX = 'resource_draft_'
const DRAFT_LIST_KEY = 'draft_list'

export function saveDraft(resourceId, formData) {
  try {
    const key = `${DRAFT_PREFIX}${resourceId}`
    const draft = {
      formData,
      savedAt: new Date().toISOString(),
      resourceId
    }
    localStorage.setItem(key, JSON.stringify(draft))
    updateDraftList(resourceId, draft.savedAt)
    return true
  } catch (e) {
    console.error('Failed to save draft:', e)
    return false
  }
}

export function getDraft(resourceId) {
  try {
    const key = `${DRAFT_PREFIX}${resourceId}`
    const saved = localStorage.getItem(key)
    return saved ? JSON.parse(saved) : null
  } catch (e) {
    console.error('Failed to get draft:', e)
    return null
  }
}

export function deleteDraft(resourceId) {
  try {
    const key = `${DRAFT_PREFIX}${resourceId}`
    localStorage.removeItem(key)
    removeFromDraftList(resourceId)
    return true
  } catch (e) {
    console.error('Failed to delete draft:', e)
    return false
  }
}

export function getLatestDraft(resourceId) {
  const draft = getDraft(resourceId)
  return draft ? draft.formData : null
}

export function getAllDrafts() {
  try {
    const list = getDraftList()
    const drafts = []

    for (const item of list) {
      const draft = getDraft(item.resourceId)
      if (draft) {
        drafts.push({
          resourceId: item.resourceId,
          ...draft
        })
      }
    }

    return drafts.sort((a, b) => new Date(b.savedAt) - new Date(a.savedAt))
  } catch (e) {
    console.error('Failed to get all drafts:', e)
    return []
  }
}

function getDraftList() {
  try {
    const list = localStorage.getItem(DRAFT_LIST_KEY)
    return list ? JSON.parse(list) : []
  } catch (e) {
    return []
  }
}

function updateDraftList(resourceId, savedAt) {
  try {
    const list = getDraftList()
    const existingIndex = list.findIndex(item => item.resourceId === resourceId)

    const item = { resourceId, savedAt }

    if (existingIndex >= 0) {
      list[existingIndex] = item
    } else {
      list.push(item)
    }

    localStorage.setItem(DRAFT_LIST_KEY, JSON.stringify(list))
  } catch (e) {
    console.error('Failed to update draft list:', e)
  }
}

function removeFromDraftList(resourceId) {
  try {
    const list = getDraftList()
    const filtered = list.filter(item => item.resourceId !== resourceId)
    localStorage.setItem(DRAFT_LIST_KEY, JSON.stringify(filtered))
  } catch (e) {
    console.error('Failed to remove from draft list:', e)
  }
}

export default {
  saveDraft,
  getDraft,
  deleteDraft,
  getLatestDraft,
  getAllDrafts
}
console.log('localStorage.js loaded successfully')
export const test = 'test string'