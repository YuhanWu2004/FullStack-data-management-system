export default {
    terms: (state) => state.terms,
    current: (state) => state.current,
    currentTermId: (state) => (state.current ? state.current.id : null),
    loading: (state) => state.loading,
    error: (state) => state.error
}
