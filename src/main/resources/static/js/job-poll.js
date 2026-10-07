/**
 * Background-job polling (shared by suggest-tickers and AI-valoration flows).
 * - Only active when the detail page carries a job banner (rendered while
 *   a job for the shown entity is active).
 * - The elapsed clock starts from the server-rendered job start instant
 *   (data-started-at), so it survives navigation and reloads.
 * - Polls the JSON status endpoint; on DONE reloads the detail page (results
 *   are already persisted), on FAILED/unknown shows an inline alert.
 *   Without JS, a manual refresh shows finished results.
 * - IA quirk: when DONE carries generated=false (fallback persisted), the
 *   reload appends ?ia=failed so the page shows the error flash.
 */
document.addEventListener('DOMContentLoaded', function () {
  var banner = document.getElementById('job-banner');
  if (!banner) {
    return;
  }
  var statusUrl = banner.dataset.statusUrl;
  var detailUrl = banner.dataset.detailUrl;
  var interruptedMsg = banner.dataset.msgInterrupted;
  var alertBox = document.getElementById('job-alert');
  var elapsedEl = document.getElementById('job-elapsed');
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
    return Number.isNaN(parsed) ? null : parsed;
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
        if (response.status === 404 || !contentType.includes('application/json')) {
          throw new Error('interrupted');
        }
        return response.json();
      })
      .then(function (job) {
        if (job.status === 'DONE') {
          stopClock();
          var target = detailUrl;
          if (job.generated === false) {
            target += (!detailUrl.includes('?') ? '?' : '&') + 'ia=failed';
          }
          window.location.href = target;
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
