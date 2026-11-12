#!/usr/bin/env python3
"""Synthesises the small game sound effects into app/src/main/res/raw/*.wav
Run from the repo root:  python3 tools/make_sounds.py
"""
import math, struct, wave

RATE = 22050

def tone(freq, ms, vol=0.5, shape="sine", decay=6.0):
    n = int(RATE * ms / 1000)
    out = []
    for i in range(n):
        t = i / RATE
        env = math.exp(-decay * i / n) * min(1.0, i / (RATE * 0.004))
        if shape == "square":
            v = 1.0 if math.sin(2 * math.pi * freq * t) >= 0 else -1.0
        elif shape == "saw":
            v = 2 * ((freq * t) % 1.0) - 1
        else:
            v = math.sin(2 * math.pi * freq * t) + 0.25 * math.sin(4 * math.pi * freq * t)
        out.append(v * env * vol)
    return out

def seq(*parts):
    out = []
    for p in parts:
        out += p
    return out

def save(name, samples):
    peak = max(1e-9, max(abs(s) for s in samples))
    scale = min(1.0, 0.9 / peak)
    with wave.open(f"app/src/main/res/raw/{name}.wav", "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(s * scale * 32767)) for s in samples))

# note frequencies
C5, E5, G5, C6 = 523.25, 659.25, 783.99, 1046.5
save("sfx_tap", tone(1200, 40, 0.4, decay=9))
save("sfx_correct", seq(tone(E5, 90, 0.5, decay=4), tone(G5, 140, 0.5, decay=5)))
save("sfx_wrong", tone(140, 220, 0.45, shape="square", decay=3))
save("sfx_win", seq(tone(C5, 110), tone(E5, 110), tone(G5, 110), tone(C6, 380, decay=3.5)))
save("sfx_lose", seq(tone(G5, 140, 0.45), tone(E5, 140, 0.45), tone(C5 / 2, 380, 0.45, shape="saw", decay=4)))
print("ok")
