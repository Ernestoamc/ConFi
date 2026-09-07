const els = {
  apiStatus: document.getElementById("apiStatus"),
  refreshBtn: document.getElementById("refreshBtn"),
  desdeInput: document.getElementById("desdeInput"),
  hastaInput: document.getElementById("hastaInput"),
  accountSelect: document.getElementById("accountSelect"),
  topInput: document.getElementById("topInput"),
  kpiIngresos: document.getElementById("kpiIngresos"),
  kpiGastos: document.getElementById("kpiGastos"),
  kpiNeto: document.getElementById("kpiNeto"),
  kpiAhorro: document.getElementById("kpiAhorro"),
  insightsCount: document.getElementById("insightsCount"),
  topCategorias: document.getElementById("topCategorias"),
  recomendaciones: document.getElementById("recomendaciones"),
  unreadBadge: document.getElementById("unreadBadge"),
  mReminded: document.getElementById("mReminded"),
  mConfirmed: document.getElementById("mConfirmed"),
  mSkipped: document.getElementById("mSkipped"),
  mUnresolved: document.getElementById("mUnresolved"),
  mConversion: document.getElementById("mConversion"),
  periodClosedCount: document.getElementById("periodClosedCount"),
  periodEvents: document.getElementById("periodEvents"),
  notificaciones: document.getElementById("notificaciones")
};

function defaultRange() {
  const now = new Date();
  const start = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), 1, 0, 0, 0));
  return {
    desde: toLocalInputValue(start),
    hasta: toLocalInputValue(now)
  };
}

function toLocalInputValue(date) {
  const z = (n) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${z(date.getMonth() + 1)}-${z(date.getDate())}T${z(date.getHours())}:${z(date.getMinutes())}`;
}

function toIso(value) {
  if (!value) return null;
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? null : d.toISOString();
}

function money(v) {
  if (v == null) return "-";
  return new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN", maximumFractionDigits: 2 }).format(Number(v));
}

function pct(v) {
  if (v == null) return "-";
  return `${(Number(v) * 100).toFixed(2)}%`;
}

async function api(path) {
  const res = await fetch(path, { headers: { "Accept": "application/json" } });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status} ${res.statusText} ${text}`);
  }
  return res.json();
}

async function loadAccounts() {
  const accounts = await api("/api/accounts");
  els.accountSelect.innerHTML = '<option value="">Consolidado</option>';
  for (const a of accounts) {
    const option = document.createElement("option");
    option.value = a.id;
    option.textContent = `${a.nombre} (${a.tipo})`;
    els.accountSelect.appendChild(option);
  }
}

function setStatus(kind, text) {
  els.apiStatus.className = `pill ${kind}`;
  els.apiStatus.textContent = text;
}

function renderTopCategorias(items) {
  els.topCategorias.innerHTML = "";
  if (!items || items.length === 0) {
    els.topCategorias.innerHTML = "<p class=\"item-meta\">Sin gastos para el periodo seleccionado.</p>";
    return;
  }

  for (const item of items) {
    const row = document.createElement("div");
    row.className = "bar";
    const p = Number(item.porcentajeDelGasto || 0);
    row.innerHTML = `
      <div class="bar-row">
        <span>${item.categoriaId}</span>
        <span>${money(item.gastoTotal)} (${(p * 100).toFixed(1)}%)</span>
      </div>
      <div class="track"><div class="fill" style="width:${Math.max(2, Math.min(100, p * 100))}%"></div></div>
    `;
    els.topCategorias.appendChild(row);
  }
}

function renderList(container, rows, emptyText) {
  container.innerHTML = "";
  if (!rows || rows.length === 0) {
    container.innerHTML = `<li><div class=\"item-meta\">${emptyText}</div></li>`;
    return;
  }

  for (const row of rows) {
    const li = document.createElement("li");
    li.innerHTML = row;
    container.appendChild(li);
  }
}

async function refreshDashboard() {
  try {
    setStatus("neutral", "Actualizando...");
    const desde = toIso(els.desdeInput.value);
    const hasta = toIso(els.hastaInput.value);
    const cuentaId = els.accountSelect.value;
    const top = Math.max(1, Math.min(20, Number(els.topInput.value || 5)));

    if (!desde || !hasta) {
      throw new Error("Rango de fechas invalido");
    }

    const queryCuenta = cuentaId ? `&cuentaId=${encodeURIComponent(cuentaId)}` : "";
    const [income, insights, summary, reminderMetrics, periodClosed, periodEvents, notifications] = await Promise.all([
      api(`/api/reports/income-statement?desde=${encodeURIComponent(desde)}&hasta=${encodeURIComponent(hasta)}${queryCuenta}`),
      api(`/api/insights?desde=${encodeURIComponent(desde)}&hasta=${encodeURIComponent(hasta)}${queryCuenta}&top=${top}`),
      api("/api/notifications/summary"),
      api("/api/notifications/reminder-metrics"),
      api("/api/period-close"),
      api("/api/period-close/events?limit=20"),
      api("/api/notifications?limit=15")
    ]);

    els.kpiIngresos.textContent = money(income.totalIngresos);
    els.kpiGastos.textContent = money(income.totalGastos);
    els.kpiNeto.textContent = money(income.resultadoNeto);
    els.kpiAhorro.textContent = pct(insights.tasaAhorro);

    els.insightsCount.textContent = `${insights.topCategoriasGasto.length} categorias`;
    renderTopCategorias(insights.topCategoriasGasto);

    renderList(
      els.recomendaciones,
      (insights.recomendaciones || []).map((r) => `<div class=\"item-title\">${r}</div>`),
      "Sin recomendaciones para este periodo."
    );

    els.unreadBadge.textContent = `No leidas: ${summary.unreadCount}`;
    els.mReminded.textContent = String(reminderMetrics.reminded);
    els.mConfirmed.textContent = String(reminderMetrics.confirmedAfterReminder);
    els.mSkipped.textContent = String(reminderMetrics.skippedAfterReminder);
    els.mUnresolved.textContent = String(reminderMetrics.unresolved);
    els.mConversion.textContent = pct(reminderMetrics.conversionRate);

    els.periodClosedCount.textContent = `Cerrados: ${(periodClosed || []).length}`;
    renderList(
      els.periodEvents,
      (periodEvents || []).map((e) => `
        <div class="item-title">${e.eventType} ${e.period ? `(${e.period})` : ""}</div>
        <div>${e.reason ? `Motivo: ${e.reason}` : "Sin motivo"}</div>
        <div class="item-meta">${new Date(e.occurredAt).toLocaleString()}</div>
      `),
      "Sin eventos de periodo."
    );

    renderList(
      els.notificaciones,
      (notifications || []).map((n) => `
        <div class="item-title">${n.title}</div>
        <div>${n.message}</div>
        <div class="item-meta">${n.eventType} - ${new Date(n.occurredAt).toLocaleString()}</div>
      `),
      "Sin notificaciones recientes."
    );

    setStatus("ok", "Conectado");
  } catch (err) {
    console.error(err);
    setStatus("err", "Error API");
  }
}

function init() {
  const range = defaultRange();
  els.desdeInput.value = range.desde;
  els.hastaInput.value = range.hasta;

  els.refreshBtn.addEventListener("click", refreshDashboard);
  els.accountSelect.addEventListener("change", refreshDashboard);
  els.topInput.addEventListener("change", refreshDashboard);

  loadAccounts().then(refreshDashboard).catch((err) => {
    console.error(err);
    setStatus("err", "Error inicial");
  });
}

init();
