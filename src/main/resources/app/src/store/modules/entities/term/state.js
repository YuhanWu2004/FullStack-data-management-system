export default {
    // No pagination fields here — unlike the other entity modules, terms are a short,
    // staff-managed list (a handful of rows, ever), and /api/term returns them all at once.
    terms: [],
    current: null,
    loading: false,
    error: null
}
