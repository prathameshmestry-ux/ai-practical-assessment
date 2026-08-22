(function () {
    var STATUS_TARGETS = {
        'open': ['in-progress', 'cancelled'],
        'in-progress': ['resolved', 'cancelled'],
        'resolved': ['closed'],
        'closed': [],
        'cancelled': []
    };

    var TERMINAL_STATUSES = ['closed', 'cancelled'];

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

    function hideMessage(id) {
        var el = document.getElementById(id);
        if (el) {
            el.hidden = true;
        }
    }

    function isTerminal(status) {
        return TERMINAL_STATUSES.indexOf(status) !== -1;
    }

    function getAllowedTargets(status) {
        return STATUS_TARGETS[status] || [];
    }

    function stripStatusClasses(el) {
        Array.prototype.slice.call(el.classList).forEach(function (cls) {
            if (cls.indexOf('ticket-mui-status-btn--') === 0 && cls !== 'ticket-mui-status-btn--readonly' && cls !== 'ticket-mui-status-btn--menu-open') {
                el.classList.remove(cls);
            }
        });
    }

    function closeStatusMenu(trigger, menu) {
        menu.hidden = true;
        trigger.setAttribute('aria-expanded', 'false');
        trigger.classList.remove('ticket-mui-status-btn--menu-open');
    }

    function openStatusMenu(trigger, menu) {
        menu.hidden = false;
        trigger.setAttribute('aria-expanded', 'true');
        trigger.classList.add('ticket-mui-status-btn--menu-open');
    }

    function rebuildStatusMenu(menu, targets, onSelect) {
        menu.innerHTML = '';
        targets.forEach(function (status) {
            var li = document.createElement('li');
            li.setAttribute('role', 'presentation');
            var btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'ticket-mui-status-menu__item';
            btn.setAttribute('role', 'option');
            btn.setAttribute('data-status', status);
            btn.textContent = status;
            btn.addEventListener('click', onSelect);
            li.appendChild(btn);
            menu.appendChild(li);
        });
    }

    function convertToTerminalBadge(statusRoot, status) {
        var dropdown = document.getElementById('ticket-status-dropdown');
        if (dropdown) {
            dropdown.remove();
        }
        var badge = document.createElement('span');
        badge.id = 'ticket-status-readonly';
        badge.className = 'ticket-mui-status-btn ticket-mui-status-btn--readonly ticket-mui-status-btn--' + status;
        badge.textContent = status;
        statusRoot.appendChild(badge);

        var notice = document.getElementById('ticket-terminal-notice');
        if (notice) {
            notice.hidden = false;
        }
    }

    function applyStatusToDom(statusRoot, status, lastModified) {
        statusRoot.setAttribute('data-current-status', status);

        var lastModifiedEl = document.getElementById('ticket-last-modified');
        if (lastModifiedEl && lastModified) {
            lastModifiedEl.textContent = lastModified;
        }

        if (isTerminal(status)) {
            convertToTerminalBadge(statusRoot, status);
            return;
        }

        var trigger = document.getElementById('ticket-status-trigger');
        var menu = document.getElementById('ticket-status-menu');
        if (!trigger || !menu) {
            return;
        }

        var label = trigger.querySelector('.ticket-mui-status-btn__label');
        if (label) {
            label.textContent = status;
        }
        stripStatusClasses(trigger);
        trigger.classList.add('ticket-mui-status-btn--' + status);
        closeStatusMenu(trigger, menu);
        rebuildStatusMenu(menu, getAllowedTargets(status), trigger._statusSelectHandler);
    }

    function bindStatusControl(ticketPath, statusRoot) {
        var trigger = document.getElementById('ticket-status-trigger');
        var menu = document.getElementById('ticket-status-menu');
        if (!trigger || !menu) {
            return;
        }

        trigger.addEventListener('click', function (event) {
            event.stopPropagation();
            if (menu.hidden) {
                openStatusMenu(trigger, menu);
            } else {
                closeStatusMenu(trigger, menu);
            }
        });

        function handleStatusSelect(event) {
            event.stopPropagation();
            var nextStatus = event.currentTarget.getAttribute('data-status');
            if (!nextStatus) {
                return;
            }
            closeStatusMenu(trigger, menu);
            hideMessage('ticket-detail-error');
            fetch(ticketPath + '.status.json', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'CSRF-Token': getCsrfToken()
                },
                body: JSON.stringify({ status: nextStatus })
            }).then(function (response) {
                return response.json();
            }).then(function (body) {
                if (body.success && body.data) {
                    applyStatusToDom(statusRoot, body.data.status, body.data.lastModified);
                    showMessage('ticket-detail-message', 'Status updated.');
                } else {
                    showMessage('ticket-detail-error', body.error ? body.error.message : 'Status update failed');
                }
            }).catch(function () {
                showMessage('ticket-detail-error', 'Status update failed');
            });
        }

        trigger._statusSelectHandler = handleStatusSelect;
        menu.querySelectorAll('[data-status]').forEach(function (item) {
            item.addEventListener('click', handleStatusSelect);
        });

        document.addEventListener('click', function () {
            if (!menu.hidden) {
                closeStatusMenu(trigger, menu);
            }
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && !menu.hidden) {
                closeStatusMenu(trigger, menu);
            }
        });
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
                        if (body.data && body.data.lastModified) {
                            var lastModifiedEl = document.getElementById('ticket-last-modified');
                            if (lastModifiedEl) {
                                lastModifiedEl.textContent = body.data.lastModified;
                            }
                        }
                    } else {
                        showMessage('ticket-detail-error', body.error ? body.error.message : 'Update failed');
                    }
                });
            });
        }

        var statusRoot = document.getElementById('ticket-status-root');
        if (statusRoot && !isTerminal(statusRoot.getAttribute('data-current-status'))) {
            bindStatusControl(ticketPath, statusRoot);
        }
    });
})();
