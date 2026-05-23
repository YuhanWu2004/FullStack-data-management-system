export default {
    students: (state) => state.students,
    selectedStudent: (state) => state.selectedStudent,
    loading: (state) => state.loading,
    error: (state) => state.error,
    totalItems: (state) => state.totalItems,
    totalPages: (state) => state.totalPages,
    currentPage: (state) => state.currentPage,
    pageSize: (state) => state.pageSize,
    studentById: (state) => (id) => {
        return state.students.find(s=>s.id === id)
    }

}