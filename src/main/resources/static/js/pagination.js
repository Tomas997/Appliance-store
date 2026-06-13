function changePageSize(size) {
    const url = new URL(window.location.href);
    url.searchParams.set('size', size);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}

function goToPage(page) {
    const url = new URL(window.location.href);
    url.searchParams.set('page', page);
    window.location.href = url.toString();
}

function sortBy(field) {
    const url = new URL(window.location.href);
    const currentSort = url.searchParams.get('sort') || 'id,asc';
    const [currentField, currentDir] = currentSort.split(',');

    const newDir = (currentField === field && currentDir === 'asc') ? 'desc' : 'asc';
    url.searchParams.set('sort', field + ',' + newDir);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}