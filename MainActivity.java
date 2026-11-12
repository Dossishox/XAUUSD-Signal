package com.example.xauusdsignal;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout root, results;
    private EditText apiKey;
    private TextView status;
    private Button refresh;
    private final String[] labels = {"M5", "M15", "M30", "H1", "H4"};
    private final String[] intervals = {"5min", "15min", "30min", "1h", "4h"};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(13, 20, 35));
        getWindow().setNavigationBarColor(Color.rgb(13, 20, 35));
        buildUi();
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
    private TextView text(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(dp(8), dp(6), dp(8), dp(6)); return t;
    }
    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(20)); root.setBackgroundColor(Color.rgb(13,20,35));
        scroll.addView(root); setContentView(scroll);

        TextView title = text("XAUUSD  •  GOLD SIGNAL", 23, Color.WHITE);
        title.setGravity(Gravity.CENTER); root.addView(title);
        TextView subtitle = text("Texnik indikatorlar asosidagi tahlil • Signal-only", 13, Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER); root.addView(subtitle);

        apiKey = new EditText(this); apiKey.setSingleLine(true); apiKey.setTextColor(Color.WHITE);
        apiKey.setHintTextColor(Color.GRAY); apiKey.setHint("Twelve Data API kalitini kiriting");
        apiKey.setInputType( android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        apiKey.setText(getPreferences(0).getString("api_key", ""));
        root.addView(apiKey, new LinearLayout.LayoutParams(-1, dp(52)));

        refresh = new Button(this); refresh.setText("SIGNALLARNI YANGILASH");
        root.addView(refresh);
        status = text("API kalitini kiriting va yangilashni bosing.", 13, Color.LTGRAY);
        root.addView(status);
        results = new LinearLayout(this); results.setOrientation(LinearLayout.VERTICAL); root.addView(results);

        TextView note = text("Strategiya: EMA 20/50 + RSI 14. BUY/SELL faqat ikkala indikator mos kelsa; aks holda WAIT. Bu moliyaviy maslahat emas.", 12, Color.LTGRAY);
        root.addView(note);
        refresh.setOnClickListener(v -> loadAll());
    }

    private void loadAll() {
        final String key = apiKey.getText().toString().trim();
        if (key.isEmpty()) { status.setText("API kaliti kerak. Twelve Data hisobidan kalit oling."); return; }
        getPreferences(0).edit().putString("api_key", key).apply();
        refresh.setEnabled(false); status.setText("Bozor ma’lumotlari olinmoqda...");
        results.removeAllViews();
        new Thread(() -> {
            ArrayList<String> cards = new ArrayList<>();
            String error = null;
            for (int i=0; i<labels.length; i++) {
                try { cards.add(analyze(labels[i], intervals[i], key)); }
                catch (Exception e) { cards.add(labels[i] + "\nMa’lumot olinmadi: " + safe(e.getMessage())); }
                try { Thread.sleep(850); } catch (InterruptedException ignored) {}
            }
            final ArrayList<String> out = cards;
            runOnUiThread(() -> {
                for (String card : out) addCard(card);
                status.setText("Yangilandi. Oxirgi yangilanish: " + new Date().toString());
                refresh.setEnabled(true);
            });
        }).start();
    }

    private String analyze(String label, String interval, String key) throws Exception {
        String q = "https://api.twelvedata.com/time_series?symbol=XAU/USD&interval=" +
                URLEncoder.encode(interval, "UTF-8") + "&outputsize=100&apikey=" + URLEncoder.encode(key, "UTF-8");
        HttpURLConnection c = (HttpURLConnection)new URL(q).openConnection();
        c.setConnectTimeout(12000); c.setReadTimeout(12000); c.setRequestMethod("GET");
        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        String body = read(is); c.disconnect();
        JSONObject obj = new JSONObject(body);
        if (obj.has("status") && "error".equalsIgnoreCase(obj.optString("status")))
            throw new Exception(obj.optString("message", "API xatosi"));
        JSONArray values = obj.optJSONArray("values");
        if (values == null || values.length() < 55) throw new Exception("Yetarli shamlar yo‘q yoki API limiti tugagan");
        double[] close = new double[values.length()];
        for (int i=0; i<values.length(); i++) close[i] = values.getJSONObject(values.length()-1-i).getDouble("close");
        double ema20 = ema(close, 20), ema50 = ema(close, 50), rsi = rsi(close, 14);
        String signal = "WAIT"; int score = 0;
        if (ema20 > ema50 && rsi >= 52 && rsi < 70) { signal = "BUY"; score = 2; }
        else if (ema20 < ema50 && rsi <= 48 && rsi > 30) { signal = "SELL"; score = 2; }
        else if (ema20 > ema50) { signal = "BUY?"; score = 1; }
        else if (ema20 < ema50) { signal = "SELL?"; score = 1; }
        String candleTime = values.getJSONObject(0).optString("datetime", "—");
        return label + "|" + signal + "|" + String.format(Locale.US,
                "Narx: %.2f\nEMA20: %.2f  •  EMA50: %.2f\nRSI14: %.1f\nOxirgi sham: %s\nTasdiq: %s/2",
                close[close.length-1], ema20, ema50, rsi, candleTime, score);
    }

    private double ema(double[] x, int period) {
        double k = 2.0/(period+1), value = x[0];
        for (int i=1; i<x.length; i++) value = x[i]*k + value*(1-k);
        return value;
    }
    private double rsi(double[] x, int p) {
        double gain=0, loss=0;
        for(int i=1; i<=p; i++){ double d=x[i]-x[i-1]; if(d>0) gain+=d; else loss-=d; }
        gain/=p; loss/=p;
        for(int i=p+1; i<x.length; i++){
            double d=x[i]-x[i-1]; gain=(gain*(p-1)+Math.max(d,0))/p;
            loss=(loss*(p-1)+Math.max(-d,0))/p;
        }
        if(loss==0) return 100;
        return 100-(100/(1+gain/loss));
    }
    private String read(InputStream is) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder b = new StringBuilder(); String line;
        while((line=r.readLine())!=null) b.append(line);
        r.close(); return b.toString();
    }
    private String safe(String s) { return s == null ? "Tarmoq yoki API xatosi" : s; }
    private void addCard(String data) {
        String[] p = data.split("\\|", 3);
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10)); card.setBackgroundColor(Color.rgb(26,37,57));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.setMargins(0, dp(7), 0, dp(7));
        results.addView(card, lp);
        String sig = p.length > 1 ? p[1] : "WAIT";
        int color = sig.startsWith("BUY") ? Color.rgb(60,210,140) : sig.startsWith("SELL") ? Color.rgb(255,100,100) : Color.WHITE;
        TextView head = text(p[0] + "     " + sig, 18, color); card.addView(head);
        card.addView(text(p.length > 2 ? p[2] : "", 13, Color.WHITE));
    }
}
