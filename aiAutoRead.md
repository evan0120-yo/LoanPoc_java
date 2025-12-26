# 這裡是給ai自動coding看的文件

# 介紹
1. 注意這是印度市場專案，總QPS尖峰3000+，請注意效能
2. MQ溝通使用RabbitMQ，請注意
3. 如果需要確保MQ一致性，使用outbox

# 技術相關
1. 專案是java21，可幫我在設定檔（yml）加上開啟虛擬執行緒等
2. database使用postgresql，name: loan_java，帳號密碼先幫我寫預設，我再去修改
3. springboot使用，注意現在還不需要加上micro service相關，先單體運行即可，沒有部屬規劃

# share & common
1. share放共用工具類 common是共用module可以放共用entity

# yml
1. 內容幫我寫

# springboot版本
1. 使用最新穩定版，如果不是就幫我改過去（3.4.x）

# coding style
1. 使用四層架構，也就是一般三層+usecase
2. 傳輸方向 controller > usecase > service > repository，注意不是ＤＤＤ喔
3. usecase和service要實作CQRS，也就是query和store分開，記得開對應package
4. guard放在service層，一樣開獨立package(所以service層會有三個package, store, query, guard)
5. 資料流除了往下走外，平行的部分給你規則
    store的話只能usecase間溝通
    query的話可以usecase也可以直接call service無限制

# module
1. 如果module提到強耦合或是你認為這裡要強耦合，就直接依照上面coding style第五點方式直接溝通即可
2. 如果要解偶就用mq然後依照你判斷加上outbox

# bo, dto 等風格
1. module下開一個object資料夾，資料夾內再開bo, dto, resp, req等資料夾
2. 定義給你
    bo: 工廠傳輸使用，以及資料夾模式使用
    資料夾模式：例如可以在bo裡面放所有狀態機，開發流程有點像使用者拿一個資料夾(bo)跑關卡，開頭會驗證資料夾內容狀態對不對然後修改內容等等，有點類似健康檢查
    dto:僅限和甲方溝通使用，注意如果是controller一律使用req,resp
