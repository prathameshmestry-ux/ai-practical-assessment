(function () {
    var DETAIL_QUERY = 'ticketId=';

    function getDetailUrl(root, ticketId) {
        var base = root.getAttribute('data-detail-page') || '';
        return base + '?' + DETAIL_QUERY + encodeURIComponent(ticketId);
    }

    function getDefaultEmptyMessage(root) {
        return root.getAttribute('data-empty-message') || 'No tickets yet.';
    }

    function getSelectedStatus() {
        var statusEl = document.getElementById('ticket-dashboard-status');
        return statusEl ? (statusEl.value || '').trim() : '';
    }

    function showError(message) {
        var errorEl = document.getElementById('ticket-dashboard-search-error');
        if (errorEl) {
            errorEl.textContent = message;
            errorEl.hidden = !message;
        }
    }

    function setEmptyVisible(show, message) {
        var emptyEl = document.getElementById('ticket-dashboard-empty');
        var tableWrap = document.getElementById('ticket-dashboard-table-wrap');
        if (emptyEl) {
            if (message) {
                var paragraph = emptyEl.querySelector('p');
                if (paragraph) {
                    paragraph.textContent = message;
                }
            }
            emptyEl.hidden = !show;
        }
        if (tableWrap) {
            tableWrap.hidden = show;
        }
    }

    function renderTickets(root, tickets, emptyMessage) {
        var tbody = document.getElementById('ticket-dashboard-body');
        if (!tbody) {
            return;
        }
        tbody.innerHTML = '';
        if (!tickets.length) {
            setEmptyVisible(true, emptyMessage || 'No tickets match your search.');
            return;
        }
        setEmptyVisible(false);
        tickets.forEach(function (ticket) {
            var row = document.createElement('tr');
            var idCell = document.createElement('td');
            var link = document.createElement('a');
            link.href = getDetailUrl(root, ticket.ticketId);
            link.textContent = ticket.ticketId;
            idCell.appendChild(link);
            row.appendChild(idCell);

            var titleCell = document.createElement('td');
            titleCell.textContent = ticket.title || '';
            row.appendChild(titleCell);

            var statusCell = document.createElement('td');
            var chip = document.createElement('span');
            chip.className = 'ticket-mui-chip';
            chip.textContent = ticket.status || '';
            statusCell.appendChild(chip);
            row.appendChild(statusCell);

            var priorityCell = document.createElement('td');
            priorityCell.textContent = ticket.priority || '';
            row.appendChild(priorityCell);

            var assigneeCell = document.createElement('td');
            assigneeCell.textContent = ticket.assignee || '';
            row.appendChild(assigneeCell);

            var modifiedCell = document.createElement('td');
            modifiedCell.textContent = ticket.lastModified || '';
            row.appendChild(modifiedCell);

            tbody.appendChild(row);
        });
    }

    function fetchJson(url) {
        return fetch(url, {
            method: 'GET',
            credentials: 'same-origin'
        }).then(function (response) {
            return response.json();
        });
    }

    function resetToAllTickets(root) {
        showError('');
        var listApiUrl = root.getAttribute('data-tickets-api-url');
        if (!listApiUrl) {
            showError('List reset is unavailable');
            return;
        }
        fetchJson(listApiUrl + '.list.json').then(function (body) {
            if (body.success && body.data && body.data.tickets) {
                renderTickets(root, body.data.tickets, getDefaultEmptyMessage(root));
            } else {
                showError(body.error ? body.error.message : 'Failed to load tickets');
            }
        }).catch(function () {
            showError('Failed to load tickets');
        });
    }

    function buildSearchUrl(searchApiUrl, keyword, status) {
        var params = [];
        if (keyword) {
            params.push('keyword=' + encodeURIComponent(keyword));
        }
        if (status) {
            params.push('status=' + encodeURIComponent(status));
        }
        return searchApiUrl + '.json' + (params.length ? '?' + params.join('&') : '');
    }

    function runFilter(root) {
        var input = document.getElementById('ticket-dashboard-search');
        if (!input) {
            return;
        }
        var keyword = (input.value || '').trim();
        var status = getSelectedStatus();
        if (!keyword && !status) {
            resetToAllTickets(root);
            return;
        }
        showError('');
        var searchApiUrl = root.getAttribute('data-search-api-url');
        if (!searchApiUrl) {
            showError('Search is unavailable');
            return;
        }
        fetchJson(buildSearchUrl(searchApiUrl, keyword, status)).then(function (body) {
            if (body.success && body.data && body.data.tickets) {
                renderTickets(root, body.data.tickets, 'No tickets match your filters.');
            } else {
                showError(body.error ? body.error.message : 'Search failed');
            }
        }).catch(function () {
            showError('Search failed');
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        var root = document.querySelector('.cmp-ticket-dashboard');
        var input = document.getElementById('ticket-dashboard-search');
        var button = document.getElementById('ticket-dashboard-search-btn');
        var statusSelect = document.getElementById('ticket-dashboard-status');
        if (!root || !input) {
            return;
        }
        if (button) {
            button.addEventListener('click', function () {
                runFilter(root);
            });
        }
        if (statusSelect) {
            statusSelect.addEventListener('change', function () {
                runFilter(root);
            });
        }
        input.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                runFilter(root);
            }
        });
    });
})();
