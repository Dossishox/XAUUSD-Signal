# XAUUSD Signal (Android)

Uzbek tilidagi, faqat signal ko‘rsatadigan Android starter ilova. M5, M15, M30, H1 va H4 grafiklari uchun XAU/USD ma’lumotini Twelve Data API orqali oladi.

## Signal qoidasi
- BUY: EMA20 > EMA50 va RSI14 52–70 oralig‘ida.
- SELL: EMA20 < EMA50 va RSI14 30–48 oralig‘ida.
- BUY?/SELL?: EMA yo‘nalishi mos, RSI tasdig‘i yetarli emas.
- WAIT: aniq tasdiq yo‘q.

Bu oddiy boshlang‘ich strategiya; foyda kafolatlanmaydi. Real hisobda ishlatishdan oldin demo hisobda sinang. Signal narxi va broker kotirovkasi orasida farq bo‘lishi mumkin.

## GitHub orqali APK olish
1. GitHub’da yangi repository oching.
2. Ushbu ZIP ichidagi barcha fayllarni repository root’iga yuklang.
3. `Actions` bo‘limiga kiring va `Build Android APK` workflow’ini ishga tushiring (yoki `main` ga push qiling).
4. Tugagan workflow ichidagi `XAUUSD-Signal-debug-apk` artifact’ini yuklab oling.
5. ZIP’ni oching va `app-debug.apk` faylini Android telefonga o‘rnating. Android ruxsat so‘rasa, ushbu manbadan o‘rnatishga ruxsat bering.

## API kaliti
Twelve Data’da API kalit oling: https://twelvedata.com/
Ilovani ochib kalitni kiriting va `SIGNALLARNI YANGILASH` tugmasini bosing. Kalit telefonning ilova preferences qismida saqlanadi. Uni GitHub’ga yoki ommaviy kodga joylamang.

## Cheklovlar
- Bu versiyada push bildirishnoma, avtomatik savdo, SL/TP hisoblash va brokerga ulanish yo‘q.
- API tarif/limitlari va XAU/USD instrument mavjudligi provayder hisobiga bog‘liq.
- API kalitni telefonda saqlash oddiy qulaylik; yuqori xavfsizlik uchun keyingi bosqichda backend/proxy qo‘shish tavsiya etiladi.
