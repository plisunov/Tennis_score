package com.tenins.andrey.tennis_score;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.GAME_REVERSED;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.GAME_START;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.MATCH_PAUSED;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.PLAYER1_GET_POINT;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.PLAYER1_GET_RED_CARD;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.PLAYER1_GET_TIMEOUT;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.PLAYER1_GET_YELLOW_CARD;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.PLAYER2_GET_POINT;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.PLAYER2_GET_RED_CARD;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.PLAYER2_GET_TIMEOUT;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.PLAYER2_GET_YELLOW_CARD;
import static com.tenins.andrey.tennis_score.ApplicationConstants.GAME_ACTION.REVERSE_BRAKER;

public class NewMatch_activity extends AppCompatActivity {

    public String matchId;

    private boolean gamePaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_match_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        /*try {
            sendRequestToServer(GAME_START);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        matchId = MainActivity.matchId;
        TextView player1 = findViewById(R.id.textView17);
        player1.setText(MainActivity.player1);

        TextView player2 = findViewById(R.id.textView19);
        player2.setText(MainActivity.player2);

        Button score1 = findViewById(R.id.button17);
        score1.setText(String.valueOf(MainActivity.scoreInFramePlayer1));

        Button score2 = findViewById(R.id.button21);
        score2.setText(String.valueOf(MainActivity.scoreInFramePlayer2));


        TextView player1Brake = (TextView) findViewById(R.id.textView20);
        player1Brake.setText(String.valueOf(MainActivity.brakePl1));
        TextView player2Brake = (TextView) findViewById(R.id.textView21);
        player2Brake.setText(String.valueOf(MainActivity.brakePl2));

        TextView frameInfo = (TextView) findViewById(R.id.textView16);
        frameInfo.setText("Сет " + String.valueOf(MainActivity.frameNumber) + " из " + String.valueOf(MainActivity.frameCount));

        TextView totalScore = (TextView) findViewById(R.id.textView18);
        totalScore.setText(String.valueOf(MainActivity.playerOneFrames) + ": " + String.valueOf(MainActivity.playerTwoFrames));

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    imAlive();
                } catch (Exception e) {
                    Log.d(ApplicationConstants.logTag, e.toString());
                }
            }
        }, 0, 5000);
    }


    private void sendRequestToServer(ApplicationConstants.GAME_ACTION action) throws JSONException {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        JSONObject request = new JSONObject();
        request.put("action", action);
        StringEntity entity = new StringEntity(request.toString(), "UTF-8");
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(getApplicationContext(), ApplicationConstants.baseURl + "/gameProcess?matchid=" + matchId, entity, "application/json", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                JSONObject matchJSON = null;
                try {
                    matchJSON = new JSONObject(new String(responseBody, "UTF-8"));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                updateMatchView(matchJSON);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    String str = new String(responseBody, "UTF-8");
                    Log.d(ApplicationConstants.logTag, str);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
            }
        });
    }

    private void updateMatchView(JSONObject matchJSON) {
        TextView frameInfo = (TextView) findViewById(R.id.textView16);
        TextView totalScore = (TextView) findViewById(R.id.textView18);
        TextView player1Name = (TextView) findViewById(R.id.textView17);
        TextView player2Name = (TextView) findViewById(R.id.textView19);
        TextView player1Brake = (TextView) findViewById(R.id.textView20);
        TextView player2Brake = (TextView) findViewById(R.id.textView21);
        Button player1FrameScore = (Button) findViewById(R.id.button17);
        Button player2FrameScore = (Button) findViewById(R.id.button21);

        try {
            ApplicationConstants.GAME_ACTION lastAction = ApplicationConstants.getGameActionByName(matchJSON.getString("lastaction"));
            switch (lastAction) {
                case SET_ENDED:
                    showSetFinishDialog(String.valueOf(matchJSON.getInt("setNumber")));
                    break;
                case MATCH_ENDED:
                    showMatchEndDialog();
                    break;
            }
            frameInfo.setText("Сет " + String.valueOf(matchJSON.getInt("setNumber")) + " из " + String.valueOf(MainActivity.frameCount));
            String additionalInfo = String.valueOf(matchJSON.getString("additionalInfo"));
            String scoreLine = String.valueOf(matchJSON.getString("score1")) + ": " + String.valueOf(matchJSON.getString("score2")) + "(" + additionalInfo + ")";
            if (gamePaused) {
                scoreLine+=" ПРИОСТАНОВЛЕНО";
                //totalScore.setBackgroundColor(-65536);
            } else {
                //totalScore.setBackgroundColor(0);
            }

            totalScore.setText(scoreLine);
            // player1Name.setText(matchJSON.getString("player1"));
            // player2Name.setText(matchJSON.getString("player2"));
            player1Brake.setText(String.valueOf(matchJSON.getInt("brake1")));
            player2Brake.setText(String.valueOf(matchJSON.getInt("brake2")));
            player1FrameScore.setText(String.valueOf(matchJSON.getInt("setScore1")));
            player2FrameScore.setText(String.valueOf(matchJSON.getInt("setScore2")));
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void showMatchEndDialog() {
        final Context context = NewMatch_activity.this;
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle("Информация");
        ad.setCancelable(false);
        ad.setMessage("Матч закончен");
        ad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishCurrentGame();
            }
        });
        ad.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    sendRequestToServer(GAME_REVERSED);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        ad.show();
    }

    private void showSetFinishDialog(String setNumber) {
        final Context context = NewMatch_activity.this;
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle("Информация");
        ad.setMessage("Сет " + String.valueOf(Integer.parseInt(setNumber) - 1) + " закончен");
        ad.setCancelable(false);
        ad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        ad.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    sendRequestToServer(GAME_REVERSED);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        ad.show();
    }

    private void finishCurrentGame() {
        MainActivity.player1 = "";
        MainActivity.player2 = "";
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void addScorePlayer1(View view) throws JSONException {
        sendRequestToServer(PLAYER1_GET_POINT);
    }

    public void player1GotRed(View view) throws JSONException {
        sendRequestToServer(PLAYER1_GET_RED_CARD);
    }

    public void player1Timeout(View view) throws JSONException {
        sendRequestToServer(PLAYER1_GET_TIMEOUT);
        final Context context = NewMatch_activity.this;
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setMessage(MainActivity.player1 + " взял таймаут");
        ad.setCancelable(false);
        ad.setPositiveButton("Закончить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    sendRequestToServer(PLAYER1_GET_TIMEOUT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialogInterface.cancel();
            }
        });
        ad.show();
    }

    public void player2GotYellow(View view) throws JSONException {
        sendRequestToServer(PLAYER2_GET_YELLOW_CARD);
    }

    public void player2GotRed(View view) throws JSONException {
        sendRequestToServer(PLAYER2_GET_RED_CARD);
    }

    public void addScorePlayer2(View view) throws JSONException {
        sendRequestToServer(PLAYER2_GET_POINT);
    }

    public void player2Timeout(View view) throws JSONException {
        sendRequestToServer(PLAYER2_GET_TIMEOUT);
        final Context context = NewMatch_activity.this;
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setCancelable(false);
        ad.setMessage(MainActivity.player2 + " взял таймаут");
        ad.setPositiveButton("Закончить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    sendRequestToServer(PLAYER2_GET_TIMEOUT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialogInterface.cancel();
            }
        });
        ad.show();
    }

    public void revertGame(View view) throws JSONException {
        sendRequestToServer(GAME_REVERSED);
    }

    public void pauseGame(View view) throws JSONException {
        gamePaused = gamePaused ? false : true;
        sendRequestToServer(MATCH_PAUSED);
        /*final Context context = NewMatch_activity.this;
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setCancelable(false);
        ad.setMessage("Игра приостановлена");
        ad.setPositiveButton("Возобновить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    sendRequestToServer(MATCH_PAUSED);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialogInterface.cancel();
            }
        });
        ad.show();*/
    }

    public void player1GotYellow(View view) throws JSONException {
        sendRequestToServer(PLAYER1_GET_YELLOW_CARD);
    }

    private void imAlive() throws JSONException, UnsupportedEncodingException {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        RequestParams params = new RequestParams();
        params.put("match_id", MainActivity.matchId);
        client.get(getApplicationContext(), ApplicationConstants.baseURl + "/imalive", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    String str = new String(responseBody, "UTF-8");
                    Log.d(ApplicationConstants.logTag, str);
                } catch (UnsupportedEncodingException e) {
                    Log.d(ApplicationConstants.logTag, e.getMessage());

                }
            }

            @Override
            public boolean getUseSynchronousMode() {
                return false;
            }
        });
    }

    public void revertBraker(View view) throws JSONException {
        sendRequestToServer(REVERSE_BRAKER);
    }
}
