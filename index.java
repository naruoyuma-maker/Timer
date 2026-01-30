        let schedule = [];
        let currentIndex = 0;
        let timer = null;
        let isRunning = false;
        let audioCtx = null;
        let actualStudySeconds = 0;
        
        // --- 追加・変更箇所 ---
        let lastTimestamp = null; 

        function toggleTimer() {
            const btn = document.getElementById('start-btn');
            if (isRunning) {
                clearInterval(timer);
                isRunning = false;
                btn.innerText = "▶ 再開";
                btn.className = "btn-start";
                lastTimestamp = null; // 停止時はリセット
            } else {
                if (currentIndex >= schedule.length) return;
                if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
                
                isRunning = true;
                lastTimestamp = Date.now(); // 開始時刻を記録
                
                btn.innerText = "⏸ 一時停止";
                btn.className = "btn-start btn-pause";
                timer = setInterval(tick, 1000);
            }
            render();
        }

        function tick() {
            if (!isRunning) return;

            const now = Date.now();
            // 前回チェック時からの経過秒数を計算（ミリ秒を秒に変換）
            const delta = Math.floor((now - lastTimestamp) / 1000);
            
            if (delta >= 1) {
                const item = schedule[currentIndex];
                
                // 経過した秒数分、データを更新
                item.spent += delta;
                if (item.type === 'study') actualStudySeconds += delta;

                if (item.mode === 'timer') {
                    item.current -= delta;
                    if (item.current <= 0) {
                        // タイマー終了時の処理
                        item.current = 0;
                        forceNext();
                    }
                } else {
                    item.current += delta;
                }

                lastTimestamp = now; // 基準時刻を更新
                render();
            }
        }
        
        // タブに戻ってきた時に強制的に再描画する処理
        document.addEventListener("visibilitychange", () => {
            if (document.visibilityState === "visible" && isRunning) {
                tick(); // 戻ってきた瞬間にズレを補正
            }
        });
