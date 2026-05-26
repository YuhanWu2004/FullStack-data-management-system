export default {
    allCourses: (state) => state.courses,
    courses: (state) => state.courses,
    selectedCourse: (state) => state.selectedCourse,
    loading: (state) => state.loading,
    error: (state) =>  state.error,
    totalItems: (state) => state.totalItems,
    totalPages: (state) => state.totalPages,
    currentPage: (state) => state.currentPage,
    pageSize: (state) => state.pageSize,

}