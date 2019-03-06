package com.tenins.andrey.tennis_score;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class MainActivity extends AppCompatActivity {

    public static String player1 = "";

    public static String player2 = "";

    public static String matchId = "";

    public static int frameCount;

    public static String gameID;

    public static int firstBrake = 1;

    public static int playerOneFrames = 0;

    public static int playerTwoFrames = 0;

    public static int brakePl1 = 0;

    public static int brakePl2 = 0;

    public static int frameNumber = 1;

    public static int scoreInFramePlayer1 = 0;

    public static int scoreInFramePlayer2 = 0;

    public static boolean player1Yellow = false;

    public static boolean player2Yellow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        if (!player1.isEmpty()) {
            EditText player1FromForm = (EditText) findViewById(R.id.editText6);
            player1FromForm.setText(player1);
        }
        if (!player2.isEmpty()) {
            EditText player2FromForm = (EditText) findViewById(R.id.editText7);
            player2FromForm.setText(player2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startMatch(View view) {
        EditText player1FromForm = (EditText) findViewById(R.id.editText6);
        player1 = player1FromForm.getText().toString();
        EditText player2FromForm = (EditText) findViewById(R.id.editText7);
        player2 = player2FromForm.getText().toString();
        RadioButton fiveOrSevenFrames = (RadioButton) findViewById(R.id.radioButton3);
        if (fiveOrSevenFrames.isChecked()) {
            frameCount = 7;
        } else {
            frameCount = 5;
        }
        RadioButton firstBraker = (RadioButton) findViewById(R.id.radioButton);
        if (firstBraker.isChecked()) {
            firstBrake = 1;
        } else {
            firstBrake = 2;
        }
        try {
            sendStartGameRequest(this);
        } catch (Exception ex) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Ошибка соединения с сервером", Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    private boolean sendStartGameRequest(final Context context) throws JSONException, UnsupportedEncodingException {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        JSONObject request = new JSONObject();
        request.put("firstBraker", firstBrake);
        request.put("frameCount", frameCount);
        StringEntity entity = new StringEntity(request.toString(), "UTF-8");
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(getApplicationContext(), ApplicationConstants.baseURl + "/startgame?matchId="+matchId, entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject matchJSON = new JSONObject(new String(responseBody, "UTF-8"));
                    gameID = matchJSON.optString("matchId");
                    playerOneFrames = matchJSON.optInt("player1Score");
                    playerTwoFrames = matchJSON.optInt("player2Score");
                    brakePl1 = matchJSON.optInt("brakePlayer1");
                    brakePl2 = matchJSON.optInt("brakePlayer2");
                    frameNumber = matchJSON.optInt("currentSet");
                    frameCount = matchJSON.optInt("frameCount");
                    JSONArray setsArray = matchJSON.optJSONArray("sets");
                    JSONObject currentSet = new JSONObject(setsArray.get(frameNumber-1).toString());
                    scoreInFramePlayer1 = currentSet.optInt("score1");
                    scoreInFramePlayer2 = currentSet.optInt("score2");
                    player1Yellow = matchJSON.optBoolean("player1Yellow");
                    player2Yellow = matchJSON.optBoolean("player2Yellow");
                    firstBrake = matchJSON.optInt("firstBraker");
                    //startActivityForResult(new Intent(context, MatchActivity.class),200);
                    startActivityForResult(new Intent(context, NewMatch_activity.class),200);
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    String str = new String(responseBody, "UTF-8");
                    gameID = new String();
                    Log.d(ApplicationConstants.logTag, str);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
            }
        });
        return true;
    }

    public void openChoosePlayers(View view) {
        Intent intent = new Intent(this, ChoosePlayers.class);
        startActivity(intent);
    }
}
