const audio      = document.getElementById("player");
const playBtn    = document.getElementById("playBtn");
const prevBtn    = document.getElementById("prevBtn");
const nextBtn    = document.getElementById("nextBtn");
const nowPlaying = document.getElementById("nowPlaying");
const items      = document.querySelectorAll("#tracklist li");

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

    // TIP: audio.play() returns a promise, add .catch(() => {}) to avoid console errors
}

// ---------- 5. Next and previous (with wrap-around) ----------
function playNext() {
    // TODO: new index = (currentIndex + 1) % queue.length
    currentIndex=(currentIndex+1)%queue.length;
    // TODO: loadTrack(newIndex), then audio.play()
    loadTrack(currentIndex);
    audio.play().catch(()=>{});
}

function playPrev() {
    currentIndex=(currentIndex-1+queue.length)%queue.length;
    loadTrack(currentIndex);
    audio.play().catch(()=>{});
}

// ---------- 6. Keep the Play/Pause button text correct ----------
audio.addEventListener("play",  () => {playBtn.textContent = "Pause" });
audio.addEventListener("pause", () => {playBtn.textContent = "Play"   });

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