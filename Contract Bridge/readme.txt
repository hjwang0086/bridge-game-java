一、遊戲架構：
打開遊戲時會有「Run As」視窗，
選擇client，為「參與」遊戲；
選擇server，為「維持」遊戲。

4個clients必須連到1個server才能開始遊戲，
所以進行遊戲前，必須有人先建立server端，
然後讓四個clients連進來(透過server顯示的IP位置)。

記得client要輸入的是「server的IP」，不是上面預設的「0.0.0.0」，
所以事先詢問創立server的人，在他上面顯示的IP為何。

自己建立server的玩家，也可以另開視窗，選擇client加自己的IP，
也就是一個人兼任維持遊戲連線，同時也參與遊戲。

server視窗建立好後，會顯示自己的IP和Port，
選項都會反白，是正常現象，
此時不要把視窗關閉，否則server端就會斷開連線。

二、常見問題：
1. 若打開game.jar是壓縮檔時，請到以下網站下載JRE：
https://java.com/zh_TW/download/
(說明：JRE是Java Runtime Environment，為執行java檔的環境)

2. 若遊戲進行時沒有顯示撲克牌，代表圖檔讀取失敗。
請確認game.jar附近有res資料夾。
若在Linux下執行，打開shell到此目錄，再執行「java -jar game.jar」
(Linux運作時的current directory預設不在此資料夾，會導致路徑問題)

3. 若client連不上(server not found)，且確認server已經建立，
是程式的bug，可以參考以下網址查詢自己IP:
http://www.whatismyip.com.tw/

-Game Maker: Hjwang
-Date Done: 2014/11/26
