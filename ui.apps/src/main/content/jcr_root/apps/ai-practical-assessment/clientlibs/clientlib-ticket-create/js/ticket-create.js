(function () {
    function getCsrfToken() {
        var meta = document.querySelector('meta[name="csrf-token"]');
        return meta ? meta.getAttribute('content') : '';
    }

    document.addEventListener('DOMContentLoaded', function () {
        var form = document.getElementById('ticket-create-form');
        if (!form) {
            return;
        }
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            var result = document.getElementById('ticket-create-result');
            var error = document.getElementById('ticket-create-error');
            result.hidden = true;
            error.hidden = true;

            var payload = {
                title: document.getElementById('ticket-title').value,
                description: document.getElementById('ticket-description').value,
                priority: document.getElementById('ticket-priority').value
            };

            fetch('/content/ai-practical-assessment/support-tickets.ticket.json', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'CSRF-Token': getCsrfToken()
                },
                body: JSON.stringify(payload)
            }).then(function (response) {
                return response.json().then(function (body) {
                    return { status: response.status, body: body };
                });
            }).then(function (resultWrapper) {
                if (resultWrapper.body.success) {
                    result.textContent = 'Ticket created: ' + resultWrapper.body.data.ticketId;
                    result.hidden = false;
                    form.reset();
                } else {
                    error.textContent = resultWrapper.body.error ? resultWrapper.body.error.message : 'Create failed';
                    error.hidden = false;
                }
            }).catch(function () {
                error.textContent = 'Create failed';
                error.hidden = false;
            });
        });
    });
})();
