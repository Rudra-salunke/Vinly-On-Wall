const audio      = document.getElementById("player");
const playBtn    = document.getElementById("playBtn");
const prevBtn    = document.getElementById("prevBtn");
const nextBtn    = document.getElementById("nextBtn");
const nowPlaying = document.getElementById("nowPlaying");
const items      = document.querySelectorAll("#tracklist li");
const record = document.querySelector(".record");

// ---------- 2. Build the queue from the <li> items ----------
// Each entry: { id: "5", title: "Dogs" }
const queue = Array.from(items).map(li => ({
    id: li.dataset.id,
    title: li.dataset.title
}));

let currentIndex = 0;

// ---------- 3. Load a track (does NOT start playing) ----------
function loadTrack(i) {
    currentIndex = i;
    audio.src = "audio?id=" + queue[i].id;
    nowPlaying.textContent = queue[i].title;
    items.forEach((li, idx) => li.classList.toggle("active", idx === i));
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
audio.addEventListener("play",  () => { playBtn.textContent = "Pause"; record.classList.add("playing"); });
audio.addEventListener("pause", () => { playBtn.textContent = "Play";  record.classList.remove("playing"); });

// ---------- 7. Auto-advance when a song ends ----------
audio.addEventListener("ended", playNext);

// ---------- 8. Button clicks ----------
playBtn.addEventListener("click", togglePlay);
nextBtn.addEventListener("click", playNext);
prevBtn.addEventListener("click", playPrev);

// ---------- 9. Tap a track in the list ----------
items.forEach((li, index) => {
    li.addEventListener("click", () => {
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