(function () {
    function getCsrfToken() {
        var meta = document.querySelector('meta[name="csrf-token"]');
        return meta ? meta.getAttribute('content') : '';
    }

    function showMessage(id, text) {
        var el = document.getElementById(id);
        if (el) {
            el.textContent = text;
            el.hidden = false;
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        var root = document.querySelector('.cmp-ticket-detail');
        if (!root) {
            return;
        }
        var ticketPath = root.getAttribute('data-ticket-path');
        if (!ticketPath) {
            return;
        }

        var updateForm = document.getElementById('ticket-update-form');
        if (updateForm) {
            updateForm.addEventListener('submit', function (event) {
                event.preventDefault();
                var payload = {
                    title: updateForm.querySelector('[name="title"]').value,
                    description: updateForm.querySelector('[name="description"]').value,
                    priority: updateForm.querySelector('[name="priority"]').value,
                    assignee: updateForm.querySelector('[name="assignee"]').value
                };
                fetch(ticketPath + '.update.json', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'CSRF-Token': getCsrfToken()
                    },
                    body: JSON.stringify(payload)
                }).then(function (response) {
                    return response.json();
                }).then(function (body) {
                    if (body.success) {
                        showMessage('ticket-detail-message', 'Ticket updated.');
                    } else {
                        showMessage('ticket-detail-error', body.error ? body.error.message : 'Update failed');
                    }
                });
            });
        }

        var statusForm = document.getElementById('ticket-status-form');
        if (statusForm) {
            statusForm.addEventListener('submit', function (event) {
                event.preventDefault();
                var status = statusForm.querySelector('[name="status"]').value;
                fetch(ticketPath + '.status.json', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'CSRF-Token': getCsrfToken()
                    },
                    body: JSON.stringify({ status: status })
                }).then(function (response) {
                    return response.json();
                }).then(function (body) {
                    if (body.success) {
                        document.getElementById('ticket-status-label').textContent = body.data.status;
                        showMessage('ticket-detail-message', 'Status updated.');
                    } else {
                        showMessage('ticket-detail-error', body.error ? body.error.message : 'Status update failed');
                    }
                });
            });
        }
    });
})();
