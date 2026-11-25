const audio      = document.getElementById("player");
const playBtn    = document.getElementById("playBtn");
const prevBtn    = document.getElementById("prevBtn");
const nextBtn    = document.getElementById("nextBtn");
const nowPlaying = document.getElementById("nowPlaying");
const items      = document.querySelectorAll("#tracklist li");
const record = document.querySelector(".record");
const tracklist = document.getElementById('tracklist');
const token = tracklist.dataset.token;
const lyricsBox = document.getElementById("lyricsBox");

// ---------- 2. Build the queue from the <li> items ----------
// Each entry: { id: "5", title: "Dogs" }
const queue = Array.from(items).map(li => ({
    id: li.dataset.id,
    title: li.dataset.title
}));

let currentIndex = 0;
let lrcLines = [];
let lrcEls = [];
let activeLine = -1;

// ---------- 3. Load a track (does NOT start playing) ----------
function loadTrack(i) {
    currentIndex = i;
    audio.src = 'audio?id=' + queue[i].id + '&token=' + encodeURIComponent(token);
    nowPlaying.textContent = queue[i].title;
    setMediaMetadata(queue[i].title);
    showLyrics(queue[i].id);
    items.forEach((li, idx) => li.classList.toggle("active", idx === i));
}

function setMediaMetadata(title) {
    if (!("mediaSession" in navigator)) return;

    const album =document.querySelector('.album-info h1').textContent.trim();
    const artist = document.querySelector('.album-info h2').textContent.trim();

    navigator.mediaSession.metadata = new MediaMetadata({
        title: title,
        artist: artist,
        album: album
    });
}

// ---------- 4. Play and pause ----------
function togglePlay() {
    if (audio.paused){
        audio.play().catch(()=>{});
    }else{
        audio.pause();
    }
}

// ---------- 5. Next and previous (with wrap-around) ----------
function playNext() {
    currentIndex=(currentIndex+1)%queue.length;
    loadTrack(currentIndex);
    audio.play().catch(()=>{});
}

function playPrev() {
    currentIndex=(currentIndex-1+queue.length)%queue.length;
    loadTrack(currentIndex);
    audio.play().catch(()=>{});
}

// ---------- 6. Keep the Play/Pause button text correct ----------
audio.addEventListener("play", () => {
    playBtn.textContent = "Pause";
    record.classList.add("playing");
    // TODO: if mediaSession exists, set playbackState to "playing"
    if("mediaSession" in navigator){
        navigator.mediaSession.playbackState = "playing";
    }
});

audio.addEventListener("pause", () => {
    playBtn.textContent = "Play";
    record.classList.remove("playing");
    // TODO: if mediaSession exists, set playbackState to "paused"
    if("mediaSession" in navigator){
        navigator.mediaSession.playbackState = "paused";
    }
});

// ---------- 7. Auto-advance when a song ends ----------
audio.addEventListener("ended", playNext);

// ---------- 8. Button clicks ----------
playBtn.addEventListener("click", togglePlay);
nextBtn.addEventListener("click", playNext);
prevBtn.addEventListener("click", playPrev);
if ("mediaSession" in navigator) {
    navigator.mediaSession.setActionHandler("play",          () =>audio.play());
    navigator.mediaSession.setActionHandler("pause",         () =>audio.pause());
    navigator.mediaSession.setActionHandler("nexttrack",   playNext);
    navigator.mediaSession.setActionHandler("previoustrack", playPrev);
}

// ---------- 9. Tap a track in the list ----------
items.forEach((li, index) => {
    li.addEventListener("click", () => {
        loadTrack(index);
        audio.play().catch(() => {});
    });
});

// ---------- 10. First load: show track 1 but do not autoplay ----------
if (queue.length > 0) {
    loadTrack(0);
}
const seekBar    = document.getElementById("seekBar");
const timeNow    = document.getElementById("timeNow");
const timeTotal  = document.getElementById("timeTotal");
const recordWrap = document.querySelector(".record-wrap");

let dragging = false;


function fmt(sec) {
    if (!isFinite(sec)) return "0:00";
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return m + ":" + String(s).padStart(2, "0");
}


function showProgress(p) {
    const pct = Math.min(Math.max(p, 0), 1);
    seekBar.value = pct * 1000;
    seekBar.style.setProperty("--fill", (pct * 100) + "%");
    recordWrap.style.setProperty("--progress", pct);
}


audio.addEventListener("timeupdate", () => {
    if (dragging || !isFinite(audio.duration) || audio.duration === 0) return;
    showProgress(audio.currentTime / audio.duration);
    timeNow.textContent = fmt(audio.currentTime);
});


audio.addEventListener("loadedmetadata", () => {
    timeTotal.textContent = fmt(audio.duration);
});


audio.addEventListener("loadstart", () => {
    showProgress(0);
    timeNow.textContent = "0:00";
    timeTotal.textContent = "0:00";
});


seekBar.addEventListener("input", () => {
    dragging = true;
    const p = seekBar.value / 1000;
    seekBar.style.setProperty("--fill", (p * 100) + "%");
    recordWrap.style.setProperty("--progress", p);
    if (isFinite(audio.duration)) timeNow.textContent = fmt(p * audio.duration);
});


seekBar.addEventListener("change", () => {
    if (isFinite(audio.duration)) {
        audio.currentTime = (seekBar.value / 1000) * audio.duration;
    }
    dragging = false;
});


seekBar.addEventListener("pointerup", () => {
    setTimeout(() => { dragging = false; }, 50);
});

// ---------- 11. LRC syncing: syncing lyrics in lyrics box ----------

function parseLrc(lrcText) {
    const result = [];
    const lines =lrcText.split(/\r?\n/);
    const lineRegex =/^\[(\d+):(\d{2})(?:\.(\d+))?\](.*)$/;

    for (const rawLine of lines) {
        const match = lineRegex.exec(rawLine.trim());

        if (!match) {
            continue;
        }
        const minutes =  Number(match[1]);
        const seconds = Number(match[2]);
        let fraction = 0;
        if (match[3] !== undefined) {
            fraction = Number(match[3]) / Math.pow(10, match[3].length);
        }
        const time = minutes * 60 + seconds + fraction;
        const text = match[4].trim();

        result.push({ time: time, text: text });
    }
    result.sort((a, b) => a.time - b.time);
    return result;
}
function showLyrics(trackId) {
    lrcLines = [];
    lrcEls = [];
    activeLine = -1;
    const source = document.getElementById("lyrics-" + trackId);
    const lrcSource = document.getElementById("lrc-" + trackId);
    const text = source ? source.textContent.trim() : "";
    const parsed = parseLrc(lrcSource ? lrcSource.textContent.trim() : "");
    lyricsBox.scrollTop = 0;
    if (parsed.length === 0) {
        lyricsBox.classList.remove("synced");
        lyricsBox.textContent = text || "No lyrics for this track";
        lyricsBox.classList.toggle("empty", !text);
        return;
    }
    if (parsed.length > 0 && parsed[0].time > 0) {
        parsed.unshift({ time: 0, text: "" });
    }
    lrcLines = parsed;
    lyricsBox.textContent = "";
    lyricsBox.classList.add("synced");
    lyricsBox.classList.remove("empty");
    for (const entry of lrcLines) {
        const lineDiv = document.createElement("div");
        lineDiv.className = "lyric-line";
        lineDiv.textContent = entry.text;
        lyricsBox.appendChild(lineDiv);
        lrcEls.push(lineDiv);
    }
}
function updateLyrics() {
    if (lrcLines.length === 0) return;

    let idx = -1;
    for (let i = 0; i < lrcLines.length; i++) {
        if (lrcLines[i].time <= audio.currentTime) {
            idx = i;
        } else {
            break; // the array is sorted, so no later line can match
        }
    }

    if (idx === activeLine) return;

    if (activeLine >= 0) lrcEls[activeLine].classList.remove("active");
    if (idx >= 0) lrcEls[idx].classList.add("active");
    activeLine = idx;
}

audio.addEventListener("timeupdate", updateLyrics);
audio.addEventListener("seeked", updateLyrics);