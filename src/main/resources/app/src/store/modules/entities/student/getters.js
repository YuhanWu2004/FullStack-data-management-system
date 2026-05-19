export default {
    students: (state) => state.students,
    selectedStudent: (state) => state.selectedStudent,
    loading: (state) => state.loading,
    error: (state) => state.error,
    studentById: (state) => (id) => {
        return state.students.find(s=>s.id === id)
    }

}