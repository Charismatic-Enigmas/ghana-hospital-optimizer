"""
Minimal dependency-free SVG line-chart renderer (Python stdlib only -- no
matplotlib/pip install required, so this runs anywhere Python 3 runs).
"""
import math

PALETTE = ["#2563eb", "#dc2626", "#16a34a", "#d97706", "#7c3aed", "#0891b2"]
WIDTH, HEIGHT = 760, 460
MARGIN_L, MARGIN_R, MARGIN_T, MARGIN_B = 80, 200, 50, 60


def _nice_ticks(lo, hi, count=5):
    if lo == hi:
        return [lo]
    step = (hi - lo) / count
    magnitude = 10 ** math.floor(math.log10(step)) if step > 0 else 1
    for mult in (1, 2, 2.5, 5, 10):
        if magnitude * mult >= step:
            step = magnitude * mult
            break
    start = math.floor(lo / step) * step
    ticks = []
    v = start
    while v <= hi + step * 0.5:
        ticks.append(v)
        v += step
    return ticks


def _fmt(v):
    if abs(v) >= 1_000_000:
        return f"{v/1_000_000:.1f}M"
    if abs(v) >= 1_000:
        return f"{v/1_000:.1f}k"
    if float(v).is_integer():
        return str(int(v))
    return f"{v:.2f}"


def line_chart(series, title, xlabel, ylabel, outfile, ylog=False):
    """series: dict[label] -> (xs: list[float], ys: list[float])"""
    all_xs = [x for xs, ys in series.values() for x in xs]
    all_ys = [y for xs, ys in series.values() for y in ys]
    x_lo, x_hi = min(all_xs), max(all_xs)
    if ylog:
        pos_ys = [y for y in all_ys if y > 0]
        y_lo, y_hi = math.log10(min(pos_ys)), math.log10(max(pos_ys))
    else:
        y_lo, y_hi = min(all_ys), max(all_ys)
    if y_lo == y_hi:
        y_lo -= 1
        y_hi += 1
    y_pad = (y_hi - y_lo) * 0.08
    y_lo -= y_pad
    y_hi += y_pad

    plot_w = WIDTH - MARGIN_L - MARGIN_R
    plot_h = HEIGHT - MARGIN_T - MARGIN_B

    def px(x):
        if x_hi == x_lo:
            return MARGIN_L + plot_w / 2
        return MARGIN_L + (x - x_lo) / (x_hi - x_lo) * plot_w

    def py(y):
        yv = math.log10(y) if (ylog and y > 0) else y
        return MARGIN_T + plot_h - (yv - y_lo) / (y_hi - y_lo) * plot_h

    parts = []
    parts.append(f'<svg xmlns="http://www.w3.org/2000/svg" width="{WIDTH}" height="{HEIGHT}" '
                  f'viewBox="0 0 {WIDTH} {HEIGHT}" font-family="Arial, sans-serif">')
    parts.append(f'<rect width="{WIDTH}" height="{HEIGHT}" fill="white"/>')
    parts.append(f'<text x="{WIDTH/2}" y="24" text-anchor="middle" font-size="16" font-weight="bold">{title}</text>')

    # y gridlines + labels
    y_ticks = _nice_ticks(y_lo if not ylog else y_lo, y_hi if not ylog else y_hi, 5) if not ylog else \
        [y_lo + i * (y_hi - y_lo) / 5 for i in range(6)]
    for yt in y_ticks:
        yp = MARGIN_T + plot_h - (yt - y_lo) / (y_hi - y_lo) * plot_h
        label = _fmt(10 ** yt) if ylog else _fmt(yt)
        parts.append(f'<line x1="{MARGIN_L}" y1="{yp:.1f}" x2="{WIDTH-MARGIN_R}" y2="{yp:.1f}" '
                      f'stroke="#e5e7eb" stroke-width="1"/>')
        parts.append(f'<text x="{MARGIN_L-8}" y="{yp+4:.1f}" text-anchor="end" font-size="11" fill="#374151">{label}</text>')

    # x ticks (use actual data x-values, sorted unique)
    x_vals = sorted(set(all_xs))
    for xv in x_vals:
        xp = px(xv)
        parts.append(f'<text x="{xp:.1f}" y="{HEIGHT-MARGIN_B+18}" text-anchor="middle" font-size="10" fill="#374151">{_fmt(xv)}</text>')
        parts.append(f'<line x1="{xp:.1f}" y1="{MARGIN_T}" x2="{xp:.1f}" y2="{HEIGHT-MARGIN_B}" stroke="#f3f4f6" stroke-width="1"/>')

    # axes
    parts.append(f'<line x1="{MARGIN_L}" y1="{MARGIN_T}" x2="{MARGIN_L}" y2="{HEIGHT-MARGIN_B}" stroke="#111827" stroke-width="1.5"/>')
    parts.append(f'<line x1="{MARGIN_L}" y1="{HEIGHT-MARGIN_B}" x2="{WIDTH-MARGIN_R}" y2="{HEIGHT-MARGIN_B}" stroke="#111827" stroke-width="1.5"/>')

    parts.append(f'<text x="{MARGIN_L + plot_w/2}" y="{HEIGHT-14}" text-anchor="middle" font-size="12" fill="#111827">{xlabel}</text>')
    parts.append(f'<text x="16" y="{MARGIN_T + plot_h/2}" text-anchor="middle" font-size="12" fill="#111827" '
                  f'transform="rotate(-90 16 {MARGIN_T + plot_h/2})">{ylabel}</text>')

    # data series
    for i, (label, (xs, ys)) in enumerate(sorted(series.items())):
        color = PALETTE[i % len(PALETTE)]
        pts = sorted(zip(xs, ys))
        path = " ".join(f"{'M' if j == 0 else 'L'}{px(x):.1f},{py(y):.1f}" for j, (x, y) in enumerate(pts))
        parts.append(f'<path d="{path}" fill="none" stroke="{color}" stroke-width="2"/>')
        for x, y in pts:
            parts.append(f'<circle cx="{px(x):.1f}" cy="{py(y):.1f}" r="3" fill="{color}"/>')
        # legend
        ly = MARGIN_T + i * 20
        lx = WIDTH - MARGIN_R + 20
        parts.append(f'<line x1="{lx}" y1="{ly+5}" x2="{lx+18}" y2="{ly+5}" stroke="{color}" stroke-width="2.5"/>')
        parts.append(f'<text x="{lx+24}" y="{ly+9}" font-size="11" fill="#111827">{label}</text>')

    parts.append("</svg>")
    with open(outfile, "w") as f:
        f.write("\n".join(parts))
    print(f"wrote {outfile}")


def dual_axis_chart(x_labels, series_left, series_right, title, xlabel,
                     left_label, right_label, outfile):
    """series_left/right: (label, ys) single series each, sharing x_labels positions."""
    plot_w = WIDTH - MARGIN_L - MARGIN_R
    plot_h = HEIGHT - MARGIN_T - MARGIN_B
    xs = list(range(len(x_labels)))

    def px(x):
        if len(xs) == 1:
            return MARGIN_L + plot_w / 2
        return MARGIN_L + x / (len(xs) - 1) * plot_w

    left_name, left_ys = series_left
    right_name, right_ys = series_right
    l_lo, l_hi = min(left_ys), max(left_ys)
    r_lo, r_hi = min(right_ys), max(right_ys)
    if l_lo == l_hi: l_hi += 1
    if r_lo == r_hi: r_hi += 1
    l_pad, r_pad = (l_hi - l_lo) * 0.1, (r_hi - r_lo) * 0.1
    l_lo -= l_pad; l_hi += l_pad
    r_lo -= r_pad; r_hi += r_pad

    def py_left(y):
        return MARGIN_T + plot_h - (y - l_lo) / (l_hi - l_lo) * plot_h

    def py_right(y):
        return MARGIN_T + plot_h - (y - r_lo) / (r_hi - r_lo) * plot_h

    parts = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{WIDTH}" height="{HEIGHT}" '
             f'viewBox="0 0 {WIDTH} {HEIGHT}" font-family="Arial, sans-serif">',
             f'<rect width="{WIDTH}" height="{HEIGHT}" fill="white"/>',
             f'<text x="{WIDTH/2}" y="24" text-anchor="middle" font-size="16" font-weight="bold">{title}</text>']

    for x, lbl in zip(xs, x_labels):
        parts.append(f'<text x="{px(x):.1f}" y="{HEIGHT-MARGIN_B+18}" text-anchor="middle" font-size="10" fill="#374151">{lbl}</text>')
        parts.append(f'<line x1="{px(x):.1f}" y1="{MARGIN_T}" x2="{px(x):.1f}" y2="{HEIGHT-MARGIN_B}" stroke="#f3f4f6" stroke-width="1"/>')

    parts.append(f'<line x1="{MARGIN_L}" y1="{MARGIN_T}" x2="{MARGIN_L}" y2="{HEIGHT-MARGIN_B}" stroke="{PALETTE[1]}" stroke-width="1.5"/>')
    parts.append(f'<line x1="{MARGIN_L}" y1="{HEIGHT-MARGIN_B}" x2="{WIDTH-MARGIN_R}" y2="{HEIGHT-MARGIN_B}" stroke="#111827" stroke-width="1.5"/>')
    parts.append(f'<line x1="{WIDTH-MARGIN_R}" y1="{MARGIN_T}" x2="{WIDTH-MARGIN_R}" y2="{HEIGHT-MARGIN_B}" stroke="{PALETTE[0]}" stroke-width="1.5"/>')

    parts.append(f'<text x="{MARGIN_L + plot_w/2}" y="{HEIGHT-14}" text-anchor="middle" font-size="12" fill="#111827">{xlabel}</text>')
    parts.append(f'<text x="16" y="{MARGIN_T + plot_h/2}" text-anchor="middle" font-size="12" fill="{PALETTE[1]}" '
                 f'transform="rotate(-90 16 {MARGIN_T + plot_h/2})">{left_label}</text>')
    parts.append(f'<text x="{WIDTH-16}" y="{MARGIN_T + plot_h/2}" text-anchor="middle" font-size="12" fill="{PALETTE[0]}" '
                 f'transform="rotate(-90 {WIDTH-16} {MARGIN_T + plot_h/2})">{right_label}</text>')

    path_l = " ".join(f"{'M' if i == 0 else 'L'}{px(x):.1f},{py_left(y):.1f}" for i, (x, y) in enumerate(zip(xs, left_ys)))
    parts.append(f'<path d="{path_l}" fill="none" stroke="{PALETTE[1]}" stroke-width="2"/>')
    for x, y in zip(xs, left_ys):
        parts.append(f'<circle cx="{px(x):.1f}" cy="{py_left(y):.1f}" r="3" fill="{PALETTE[1]}"/>')

    path_r = " ".join(f"{'M' if i == 0 else 'L'}{px(x):.1f},{py_right(y):.1f}" for i, (x, y) in enumerate(zip(xs, right_ys)))
    parts.append(f'<path d="{path_r}" fill="none" stroke="{PALETTE[0]}" stroke-width="2"/>')
    for x, y in zip(xs, right_ys):
        parts.append(f'<circle cx="{px(x):.1f}" cy="{py_right(y):.1f}" r="3" fill="{PALETTE[0]}"/>')

    ly = MARGIN_T
    parts.append(f'<line x1="{WIDTH-MARGIN_R+20}" y1="{ly+5}" x2="{WIDTH-MARGIN_R+38}" y2="{ly+5}" stroke="{PALETTE[1]}" stroke-width="2.5"/>')
    parts.append(f'<text x="{WIDTH-MARGIN_R+44}" y="{ly+9}" font-size="11" fill="#111827">{left_name}</text>')
    parts.append(f'<line x1="{WIDTH-MARGIN_R+20}" y1="{ly+25}" x2="{WIDTH-MARGIN_R+38}" y2="{ly+25}" stroke="{PALETTE[0]}" stroke-width="2.5"/>')
    parts.append(f'<text x="{WIDTH-MARGIN_R+44}" y="{ly+29}" font-size="11" fill="#111827">{right_name}</text>')

    parts.append("</svg>")
    with open(outfile, "w") as f:
        f.write("\n".join(parts))
    print(f"wrote {outfile}")
