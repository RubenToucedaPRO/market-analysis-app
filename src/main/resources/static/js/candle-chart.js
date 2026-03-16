/**
 * candle-chart.js — F2.10
 *
 * Renders a full interactive candlestick chart with SMA20, SMA50, and SMA200
 * line series using TradingView Lightweight Charts v4.
 *
 * Expected DOM: a <div id="candle-chart" data-ticker-id="..."> container.
 * The script fetches /analysis/ticker/{id}/candles and builds the chart.
 * All SMA series are computed client-side from the raw close prices so that
 * the server remains the single source of candle truth (SRP).
 */

document.addEventListener("DOMContentLoaded", function () {
  const container = document.getElementById("candle-chart");
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
          '<p class="text-muted text-center py-5">No hay datos de velas disponibles todavía.</p>';
        return;
      }

      const chart = LightweightCharts.createChart(container, {
        width: container.clientWidth,
        height: container.clientHeight || 500,
        layout: {
          background: { color: "#ffffff" },
          textColor: "#333333",
        },
        grid: {
          vertLines: { color: "#f0f0f0" },
          horzLines: { color: "#f0f0f0" },
        },
        rightPriceScale: { borderColor: "#cccccc" },
        timeScale: { borderColor: "#cccccc", timeVisible: true },
      });

      // Candlestick series
      const candleSeries = chart.addCandlestickSeries({
        upColor: "#26a69a",
        downColor: "#ef5350",
        borderVisible: false,
        wickUpColor: "#26a69a",
        wickDownColor: "#ef5350",
      });

      const candles = data.candles.map(function (c) {
        return {
          time: c.time,
          open: parseFloat(c.open),
          high: parseFloat(c.high),
          low: parseFloat(c.low),
          close: parseFloat(c.close),
        };
      });
      candleSeries.setData(candles);

      // Compute and plot SMA line series from close prices (client-side, SRP)
      const closes = candles.map(function (c) { return c.close; });

      function computeSMA(prices, period) {
        const result = [];
        for (let i = period - 1; i < prices.length; i++) {
          let sum = 0;
          for (let j = i - period + 1; j <= i; j++) sum += prices[j];
          result.push({ time: candles[i].time, value: sum / period });
        }
        return result;
      }

      const smaSeries20 = chart.addLineSeries({ color: "#2196f3", lineWidth: 1, title: "SMA20" });
      smaSeries20.setData(computeSMA(closes, 20));

      const smaSeries50 = chart.addLineSeries({ color: "#ff9800", lineWidth: 1, title: "SMA50" });
      smaSeries50.setData(computeSMA(closes, 50));

      const smaSeries200 = chart.addLineSeries({ color: "#9c27b0", lineWidth: 1, title: "SMA200" });
      smaSeries200.setData(computeSMA(closes, 200));

      chart.timeScale().fitContent();

      // Responsive resize
      window.addEventListener("resize", function () {
        chart.applyOptions({ width: container.clientWidth });
      });
    })
    .catch(function (err) {
      container.innerHTML =
        '<p class="text-danger text-center py-5">Error al cargar el gráfico.</p>';
      // eslint-disable-next-line no-console
      console.error("candle-chart error:", err);
    });
});
