(function () {
    function getCsrfToken() {
        var meta = document.querySelector('meta[name="csrf-token"]');
        return meta ? meta.getAttribute('content') : '';
    }

    document.addEventListener('DOMContentLoaded', function () {
        var root = document.querySelector('.cmp-ticket-comments');
        var form = document.getElementById('ticket-comment-form');
        if (!root || !form) {
            return;
        }
        var commentsPath = root.getAttribute('data-comments-path');
        if (!commentsPath) {
            return;
        }

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            var text = document.getElementById('comment-text').value;
            fetch(commentsPath + '.comment.json', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'CSRF-Token': getCsrfToken()
                },
                body: JSON.stringify({ text: text })
            }).then(function (response) {
                return response.json();
            }).then(function (body) {
                if (body.success) {
                    window.location.reload();
                } else {
                    var error = document.getElementById('ticket-comment-error');
                    error.textContent = body.error ? body.error.message : 'Comment failed';
                    error.hidden = false;
                }
            });
        });
    });
})();
