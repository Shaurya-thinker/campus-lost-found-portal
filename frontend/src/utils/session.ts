/**
 * Lightweight browser session identity utility using sessionStorage.
 * NOTE: For demo/academic purposes only. Does not replace cryptographic authentication.
 */

export interface StudentSession {
  id: number
  name: string
  email: string
}

const SESSION_KEY = 'campus_lost_found_student'

/**
 * Retrieves the currently remembered student identity from sessionStorage.
 * Returns null if no student is registered in this session or if the data is malformed.
 */
export function getStoredStudent(): StudentSession | null {
  try {
    const raw = window.sessionStorage.getItem(SESSION_KEY)
    if (!raw) return null

    const parsed = JSON.parse(raw)
    if (
      parsed &&
      typeof parsed.id === 'number' &&
      typeof parsed.name === 'string' &&
      typeof parsed.email === 'string'
    ) {
      return {
        id: parsed.id,
        name: parsed.name,
        email: parsed.email,
      }
    }
    // Corrupted or incomplete record
    clearStoredStudent()
    return null
  } catch {
    clearStoredStudent()
    return null
  }
}

/**
 * Persists the registered student identity into sessionStorage.
 */
export function saveStoredStudent(student: StudentSession): void {
  try {
    window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(student))
  } catch (err) {
    console.warn('Unable to persist student session in sessionStorage:', err)
  }
}

/**
 * Clears the student identity from sessionStorage upon sign-out.
 */
export function clearStoredStudent(): void {
  try {
    window.sessionStorage.removeItem(SESSION_KEY)
  } catch (err) {
    console.warn('Unable to clear student session:', err)
  }
}

