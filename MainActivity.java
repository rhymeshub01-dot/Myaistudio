package com.personal.myaistudio;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import java.net.*;
import java.io.*;

public class MainActivity extends Activity {
    EditText endpoint, prompt;
    TextView status;
    Button generate, open;
    String videoUrl = "";
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(36,48,36,24);
        layout.setBackgroundColor(Color.rgb(248,249,252));
        TextView title = new TextView(this);
        title.setText("My AI Studio"); title.setTextSize(27); title.setTextColor(Color.BLACK);
        layout.addView(title);
        TextView note = new TextView(this);
        note.setText("Personal text-to-video client. Connect a compatible HTTPS AI backend to generate real videos.");
        layout.addView(note);
        endpoint = new EditText(this); endpoint.setSingleLine(true);
        endpoint.setHint("HTTPS backend URL (e.g. https://example.com/generate)");
        layout.addView(endpoint);
        prompt = new EditText(this); prompt.setHint("Describe your video...");
        prompt.setMinLines(4); prompt.setGravity(48); layout.addView(prompt);
        generate = new Button(this); generate.setText("Generate video"); layout.addView(generate);
        status = new TextView(this); status.setText("Backend not configured."); layout.addView(status);
        open = new Button(this); open.setText("Open / download video"); open.setEnabled(false); layout.addView(open);
        setContentView(layout);
        generate.setOnClickListener(v -> submit());
        open.setOnClickListener(v -> {
            if (!videoUrl.isEmpty()) startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)));
        });
    }
    void submit() {
        String url = endpoint.getText().toString().trim();
        String text = prompt.getText().toString().trim();
        if (!url.startsWith("https://") || text.isEmpty()) {
            status.setText("Enter a valid HTTPS endpoint and a video prompt."); return;
        }
        generate.setEnabled(false); open.setEnabled(false);
        status.setText("Requesting video from backend...");
        new Thread(() -> {
            String message; String result = "";
            try {
                HttpURLConnection c = (HttpURLConnection)new URL(url).openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(20000); c.setReadTimeout(180000);
                c.setDoOutput(true); c.setRequestProperty("Content-Type","application/json");
                byte[] data = new JSONObject().put("prompt",text).toString().getBytes("UTF-8");
                try(OutputStream out=c.getOutputStream()){out.write(data);}
                int code=c.getResponseCode();
                InputStream stream = code>=200 && code<300 ? c.getInputStream() : c.getErrorStream();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096]; int n;
                while((n=stream.read(buffer))!=-1)bytes.write(buffer,0,n);
                JSONObject response = new JSONObject(bytes.toString("UTF-8"));
                if(code<200 || code>=300)throw new Exception(response.optString("error","HTTP "+code));
                result = response.optString("video_url","");
                if(!result.startsWith("https://"))throw new Exception("Backend must return HTTPS video_url.");
                message="Video ready! Tap Open / download video.";
                c.disconnect();
            } catch(Exception e){message="Generation failed: "+e.getMessage();}
            final String finalUrl=result, finalMessage=message;
            runOnUiThread(() -> {
                videoUrl=finalUrl; status.setText(finalMessage);
                open.setEnabled(!finalUrl.isEmpty()); generate.setEnabled(true);
            });
        }).start();
    }
}
