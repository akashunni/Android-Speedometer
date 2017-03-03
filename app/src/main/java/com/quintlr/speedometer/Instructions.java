package com.quintlr.speedometer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Instructions extends AppCompatActivity {

    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        btn = (Button) findViewById(R.id.instructions_btn);
        if (btn != null) {
            btn.setEnabled(false);
        }
        AsyncActivityLoader asyncActivityLoader = new AsyncActivityLoader(getApplicationContext());
        asyncActivityLoader.execute();
    }

    class AsyncActivityLoader extends AsyncTask<String, String, Intent>{
        Context context;
        AsyncActivityLoader(Context context){
            this.context = context;
        }

        @Override
        protected Intent doInBackground(String... params) {
            return new Intent(context, MainActivity.class);
        }

        @Override
        protected void onPostExecute(final Intent s) {
            Log.d("akash", "onPostExecute: ");
            super.onPostExecute(s);
            btn.setEnabled(true);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(s);
                    finish();
                }
            });
        }
    }
}

