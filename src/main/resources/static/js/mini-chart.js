/**
 * mini-chart.js — F2.11
 *
 * Renders a compact, non-interactive candlestick thumbnail inside
 * <div id="mini-chart" data-ticker-id="..."> on the ticker-detail page.
 * Clicking the chart navigates to the full chart view
 * (/analysis/ticker/{id}/chart).
 *
 * No inline chart logic is placed in the HTML template — all interactions
 * are registered here (SonarQube compliant).
 */

document.addEventListener("DOMContentLoaded", function () {
  const container = document.getElementById("mini-chart");
  if (!container) return;

  const tickerId = container.dataset.tickerId;
  if (!tickerId) return;

  fetch("/analysis/ticker/" + tickerId + "/candles")
    .then(function (res) {
      if (!res.ok) throw new Error("HTTP " + res.status);
      return res.json();
    })
    .then(function (data) {
      if (!data.candles || data.candles.length === 0) {
        container.innerHTML =
          '<p class="text-muted text-center py-3 small">Sin datos de velas</p>';
        return;
      }

      const chart = LightweightCharts.createChart(container, {
        width: container.clientWidth,
        height: 150,
        layout: {
          background: { color: "transparent" },
          textColor: "#666666",
        },
        grid: {
          vertLines: { visible: false },
          horzLines: { visible: false },
        },
        rightPriceScale: { visible: false },
        leftPriceScale: { visible: false },
        timeScale: { visible: false },
        crosshair: { mode: LightweightCharts.CrosshairMode.Hidden },
        handleScroll: false,
        handleScale: false,
      });

      const candleSeries = chart.addCandlestickSeries({
        upColor: "#26a69a",
        downColor: "#ef5350",
        borderVisible: false,
        wickUpColor: "#26a69a",
        wickDownColor: "#ef5350",
      });

      // Show last 60 candles in the thumbnail
      const recent = data.candles.slice(-150);
      candleSeries.setData(
        recent.map(function (c) {
          return {
            time: c.time,
            open: Number.parseFloat(c.open),
            high: Number.parseFloat(c.high),
            low: Number.parseFloat(c.low),
            close: Number.parseFloat(c.close),
          };
        }),
      );

      chart.timeScale().applyOptions({
        rightOffset: 20,
      });

      // Click → navigate to full chart view
      container.style.cursor = "pointer";
      container.addEventListener("click", function () {
        globalThis.location.href = "/analysis/ticker/" + tickerId + "/chart";
      });

      // Resize
      window.addEventListener("resize", function () {
        chart.applyOptions({ width: container.clientWidth });
      });
    })
    .catch(function (err) {
      container.innerHTML =
        '<p class="text-muted text-center py-3 small">Gráfico no disponible</p>';
      // eslint-disable-next-line no-console
      console.error("mini-chart error:", err);
    });
});
