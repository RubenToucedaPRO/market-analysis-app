/**
 * Suggest-ticker job polling.
 * - Only active when the detail page carries a suggest-job banner (?jobId=...).
 * - Polls the JSON status endpoint; on DONE reloads the detail page (results
 *   are already persisted as a suggestion snapshot), on FAILED/unknown shows
 *   an inline alert. Without JS, a manual refresh shows finished results.
 */
document.addEventListener('DOMContentLoaded', function () {
  var banner = document.getElementById('suggest-job-banner');
  if (!banner) {
    return;
  }
  var statusUrl = banner.getAttribute('data-status-url');
  var detailUrl = banner.getAttribute('data-detail-url');
  var interruptedMsg = banner.getAttribute('data-msg-interrupted');
  var alertBox = document.getElementById('suggest-job-alert');
  var elapsedEl = document.getElementById('suggest-job-elapsed');
  if (!statusUrl || !detailUrl || !alertBox || !elapsedEl) {
    return;
  }
  var startedAt = Date.now();
  var POLL_MS = 5000;
  var clockTimer = null;

  function formatElapsed() {
    var seconds = Math.floor((Date.now() - startedAt) / 1000);
    var minutes = Math.floor(seconds / 60);
    var rest = seconds % 60;
    return minutes + ':' + (rest < 10 ? '0' : '') + rest;
  }

  function showAlert(type, text) {
    alertBox.className = 'alert alert-' + type;
    alertBox.textContent = text;
  }

  function stopClock() {
    if (clockTimer !== null) {
      clearInterval(clockTimer);
      clockTimer = null;
    }
  }

  function onInterrupted() {
    stopClock();
    banner.classList.add('d-none');
    showAlert('warning', interruptedMsg);
  }

  function poll() {
    fetch(statusUrl, { headers: { Accept: 'application/json' } })
      .then(function (response) {
        var contentType = response.headers.get('content-type') || '';
        if (response.status === 404 || contentType.indexOf('application/json') === -1) {
          throw new Error('interrupted');
        }
        return response.json();
      })
      .then(function (job) {
        if (job.status === 'DONE') {
          stopClock();
          window.location.href = detailUrl;
        } else if (job.status === 'FAILED') {
          stopClock();
          banner.classList.add('d-none');
          showAlert('danger', job.message || interruptedMsg);
        } else {
          setTimeout(poll, POLL_MS);
        }
      })
      .catch(function () {
        onInterrupted();
      });
  }

  clockTimer = setInterval(function () {
    elapsedEl.textContent = formatElapsed();
  }, 1000);
  setTimeout(poll, POLL_MS);
});
