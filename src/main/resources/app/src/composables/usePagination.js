import { ref, computed } from 'vue'

export function usePagination(items, defaultPageSize = 10) {

    const currentPage = ref(1)
    const pageSize = ref(defaultPageSize)

    const totalItems = computed(() => items.value.length)

    const totalPages = computed(() =>
        Math.ceil(totalItems.value / pageSize.value)
    )

    const paginatedItems = computed(() => {
        const start = (currentPage.value - 1) * pageSize.value
        const end = start + pageSize.value
        return items.value.slice(start, end)
    })

    function onPageChanged(page) {
        currentPage.value = page
    }

    function resetPage() {
        currentPage.value = 1
    }

    return {
        currentPage,
        pageSize,
        totalItems,
        totalPages,
        paginatedItems,
        onPageChanged,
        resetPage
    }
}