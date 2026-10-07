/**
 * Suggest-ticker job polling.
 * - Only active when the detail page carries a suggest-job banner (rendered
 *   when opening detail with ?jobId= or while a job is active).
 * - The elapsed clock starts from the server-rendered job start instant
 *   (data-started-at), so it survives navigation and reloads.
 * - Polls the JSON status endpoint; on DONE reloads the detail page (results
 *   are already persisted as a suggestion snapshot), on FAILED/unknown shows
 *   an inline alert. Without JS, a manual refresh shows finished results.
 */
document.addEventListener('DOMContentLoaded', function () {
  var banner = document.getElementById('suggest-job-banner');
  if (!banner) {
    return;
  }
  var statusUrl = banner.dataset.statusUrl;
  var detailUrl = banner.dataset.detailUrl;
  var interruptedMsg = banner.dataset.msgInterrupted;
  var alertBox = document.getElementById('suggest-job-alert');
  var elapsedEl = document.getElementById('suggest-job-elapsed');
  if (!statusUrl || !detailUrl || !alertBox || !elapsedEl) {
    return;
  }
  var pageLoadedAt = Date.now();
  var jobStartedAt = parseStartedAt(banner.dataset.startedAt);
  var POLL_MS = 5000;
  var clockTimer = null;

  function parseStartedAt(value) {
    if (!value) {
      return null;
    }
    var parsed = Date.parse(value);
    return isNaN(parsed) ? null : parsed;
  }

  function elapsedBase() {
    return jobStartedAt !== null ? jobStartedAt : pageLoadedAt;
  }

  function formatElapsed() {
    var seconds = Math.floor((Date.now() - elapsedBase()) / 1000);
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

  function tickClock() {
    elapsedEl.textContent = formatElapsed();
  }

  tickClock();
  clockTimer = setInterval(tickClock, 1000);
  setTimeout(poll, POLL_MS);
});
