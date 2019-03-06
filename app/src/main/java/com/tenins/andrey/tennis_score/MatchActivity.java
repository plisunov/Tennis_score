package com.tenins.andrey.tennis_score;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.tenins.andrey.tennis_score.ApplicationConstants.History_Points;
import com.tenins.andrey.tennis_score.ApplicationConstants.SCORE_CHANGE_TYPE;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class MatchActivity extends AppCompatActivity {

    private Map<Integer, HashMap<Integer, Integer>> score;

    private static int frameNumber;

    private static int playerOneFrames;

    private static int playerTwoFrames;

    private static int brakePl1 = 0;

    private static int brakePl2 = 0;

    private static int historyStep = 0;

    private static boolean isPaused;

    private static boolean player1Yellow = false;

    private static boolean player2Yellow = false;


    private Map<Integer, History_Points> history = new HashMap<>();

    private Map<Integer, SetSnapshot> setSnaphots = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        score = new HashMap<>();
        frameNumber = MainActivity.frameNumber;
        playerOneFrames = MainActivity.playerOneFrames;
        playerTwoFrames = MainActivity.playerTwoFrames;
        for (int i = 0; i < MainActivity.frameCount; i++) {
            score.put(i + 1, new HashMap<Integer, Integer>());
            score.get(i + 1).put(1, frameNumber == i + 1 ? MainActivity.scoreInFramePlayer1 : 0);
            score.get(i + 1).put(2, frameNumber == i + 1 ? MainActivity.scoreInFramePlayer2 : 0);
        }

        TextView player1 = findViewById(R.id.textView5);
        player1.setText(MainActivity.player1);

        brakePl1 = MainActivity.brakePl1;
        brakePl2 = MainActivity.brakePl2;
        showBrakeBraker();

        player1Yellow = MainActivity.player1Yellow;
        if (player1Yellow) {
            TextView player1Card = findViewById(R.id.textView14);
            player1Card.setBackgroundColor(0xffffbb33);
        }
        player2Yellow = MainActivity.player2Yellow;
        if (player2Yellow) {
            TextView player2Card = findViewById(R.id.textView15);
            player2Card.setBackgroundColor(0xffffbb33);
        }
        TextView player2 = findViewById(R.id.textView7);
        player2.setText(MainActivity.player2);
        TextView setInfo = findViewById(R.id.textView9);
        setInfo.setText("Сет " + String.valueOf(frameNumber) + " из (" + String.valueOf(MainActivity.frameCount) + ")");
        TextView matchInfo = findViewById(R.id.textView10);
        matchInfo.setText("Счет в матче " + playerOneFrames + " : " + playerTwoFrames);
        Button revB = (Button) findViewById(R.id.button3);
        revB.setVisibility(View.INVISIBLE);

        TextView player1Score = (TextView) findViewById(R.id.textView6);
        player1Score.setText(String.valueOf(score.get(frameNumber).get(1)));
        TextView player2Score = (TextView) findViewById(R.id.textView8);
        player2Score.setText(String.valueOf(score.get(frameNumber).get(2)));
        Button b1 = (Button) findViewById(R.id.button2);
        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                addPointToPlayerOne(view, SCORE_CHANGE_TYPE.ADD_POINT);
            }
        });

        Button b2 = (Button) findViewById(R.id.button4);
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                addPointToPlayerTwo(view, SCORE_CHANGE_TYPE.ADD_POINT);
            }
        });

        Button b12 = (Button) findViewById(R.id.button12);
        b12.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                player1RedCard(view, false);
            }
        });

        Button b14 = (Button) findViewById(R.id.button14);
        b14.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                player2RedCard(view, false);
            }
        });

       /* new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    imAlive();
                } catch (Exception e) {
                    Log.d(ApplicationConstants.logTag, e.toString());
                }
            }
        }, 0, 5000);*/
    }

    public void addPointToPlayerOne(View view, SCORE_CHANGE_TYPE type) {
        if (type.equals(SCORE_CHANGE_TYPE.ADD_POINT)) {
            historyStep++;
            history.put(historyStep, History_Points.PLAYER1INKSCORE);
        }
        Button revB = (Button) findViewById(R.id.button3);
        revB.setVisibility(View.VISIBLE);
        checkAfterRed();
        TextView player1Score = (TextView) findViewById(R.id.textView6);
        int playerScore = Integer.parseInt(player1Score.getText().toString()) + 1;
        if (!type.equals(SCORE_CHANGE_TYPE.YELLOW_CARD)) {
            player1Score.setText(String.valueOf(playerScore));
            score.get(frameNumber).put(1, playerScore);
        }
        if (type.equals(SCORE_CHANGE_TYPE.ADD_POINT)) {
            changeBrake(true);
        }
        try {
            sendScoreChangeToServer(1, type);
        } catch (Exception e) {
            Log.d(ApplicationConstants.logTag, e.toString());
        }
        if (checkFrameFinish()) {
            showSetEndDialog(view, 1);
        }
    }


    private void startNewFrame(int player) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Сет " + frameNumber + " закончен", Toast.LENGTH_LONG);
        toast.show();
        //Button revB = (Button) findViewById(R.id.button3);
        //revB.setVisibility(View.INVISIBLE);
        TextView player1Score = (TextView) findViewById(R.id.textView6);
        TextView player2Score = (TextView) findViewById(R.id.textView8);
        createSetSnapshot(player);
        player1Score.setText("0");
        player2Score.setText("0");
        historyStep = 0;
        brakePl1 = 1;
        brakePl2 = 1;
        frameNumber++;
        if (frameNumber % 2 == 1) {
            if (MainActivity.firstBrake == 1) {
                brakePl2 = 0;
                brakePl1 = 1;
            } else {
                brakePl2 = 1;
                brakePl1 = 0;
            }
        } else {
            if (MainActivity.firstBrake == 2) {
                brakePl2 = 0;
                brakePl1 = 1;
            } else {
                brakePl2 = 1;
                brakePl1 = 0;
            }
        }
        showBrakeBraker();
        TextView setInfo = findViewById(R.id.textView9);
        setInfo.setText("Сет " + frameNumber + " из (" + String.valueOf(MainActivity.frameCount) + ")");
        TextView matchInfo = findViewById(R.id.textView10);
        matchInfo.setText("Счет в матче " + playerOneFrames + " : " + playerTwoFrames);
    }

    private void createSetSnapshot(int player) {
        TextView player1Score = (TextView) findViewById(R.id.textView6);
        TextView player2Score = (TextView) findViewById(R.id.textView8);
        SetSnapshot snapshot = new SetSnapshot();
        snapshot.setHistory(new HashMap<Integer, History_Points>(history));
        snapshot.setSetNumber(frameNumber);
        if (player == 1) {
            snapshot.setPlayer1Score(playerOneFrames - 1);
            snapshot.setPlayer2Score(playerTwoFrames);
        } else {
            snapshot.setPlayer1Score(playerOneFrames);
            snapshot.setPlayer2Score(playerTwoFrames - 1);
        }
        snapshot.setPlayer1SetScore(Integer.parseInt(player1Score.getText().toString()));
        snapshot.setPlayer2SetScore(Integer.parseInt(player2Score.getText().toString()));
        snapshot.setPlayer1Brake(brakePl1);
        snapshot.setPlayer2Brake(brakePl2);
        history.clear();
        setSnaphots.put(frameNumber, snapshot);
    }

    private boolean checkFinishMatch() {
        if (MainActivity.frameCount == 5) {
            if (playerOneFrames == 3) {
                return true;
            }
            if (playerTwoFrames == 3) {
                return true;
            }
        } else {
            if (playerOneFrames == 4) {
                return true;
            }
            if (playerTwoFrames == 4) {
                return true;
            }
        }
        return false;
    }

    public void reversePlayerOne(View view, SCORE_CHANGE_TYPE type) {
        TextView player1Score = (TextView) findViewById(R.id.textView6);
        player1Score.setText(String.valueOf(Integer.parseInt(player1Score.getText().toString()) - 1));
        score.get(frameNumber).put(1, Integer.parseInt(player1Score.getText().toString()) - 1);
        if (type.equals(SCORE_CHANGE_TYPE.REVERSE_POINT)) {
            changeBrake(false);
        }
        try {
            sendScoreChangeToServer(1, SCORE_CHANGE_TYPE.REVERSE_POINT);
        } catch (Exception e) {
            Log.d(ApplicationConstants.logTag, e.toString());
        }
    }

    private boolean checkFrameFinish() {
        TextView player1Score = (TextView) findViewById(R.id.textView6);
        int pl1Score = Integer.parseInt(player1Score.getText().toString());
        TextView player2Score = (TextView) findViewById(R.id.textView8);
        int pl2Score = Integer.parseInt(player2Score.getText().toString());
        if (MainActivity.frameCount == 5) {
            if (((pl1Score == 11) && (pl2Score < 10)) || ((pl1Score > 10) && (pl1Score > pl2Score + 1))) {
                playerOneFrames++;
                return true;
            }
            if (((pl2Score == 11) && (pl1Score < 10)) || ((pl2Score > 10) && (pl2Score > pl1Score + 1))) {
                playerTwoFrames++;
                return true;
            }
        } else {
            if (pl1Score == 7) {
                playerOneFrames++;
                return true;
            }
            if (pl2Score == 7) {
                playerTwoFrames++;
                return true;
            }
        }
        return false;
    }

    public void addPointToPlayerTwo(View view, SCORE_CHANGE_TYPE type) {
        if (type.equals(SCORE_CHANGE_TYPE.ADD_POINT)) {
            historyStep++;
            history.put(historyStep, History_Points.PLAYER2INKSCORE);
        }
        Button revB = (Button) findViewById(R.id.button3);
        revB.setVisibility(View.VISIBLE);
        checkAfterRed();
        TextView player2Score = (TextView) findViewById(R.id.textView8);
        int playerScore = Integer.parseInt(player2Score.getText().toString()) + 1;
        if (!type.equals(SCORE_CHANGE_TYPE.YELLOW_CARD)) {
            player2Score.setText(String.valueOf(playerScore));

            score.get(frameNumber).put(2, playerScore);
        }
        if (type.equals(SCORE_CHANGE_TYPE.ADD_POINT)) {
            changeBrake(true);
        }
        try {
            sendScoreChangeToServer(2, type);
        } catch (Exception e) {
            Log.d(ApplicationConstants.logTag, e.toString());
        }
        if (checkFrameFinish()) {
            showSetEndDialog(view, 2);
        }
    }

    public void reversePlayerTwo(View view, SCORE_CHANGE_TYPE type) {
        TextView player2Score = (TextView) findViewById(R.id.textView8);
        player2Score.setText(String.valueOf(Integer.parseInt(player2Score.getText().toString()) - 1));
        score.get(frameNumber).put(2, Integer.parseInt(player2Score.getText().toString()) - 1);
        if (type.equals(SCORE_CHANGE_TYPE.REVERSE_POINT)) {
            changeBrake(false);
        }
        try {
            sendScoreChangeToServer(2, SCORE_CHANGE_TYPE.REVERSE_POINT);
        } catch (Exception e) {
            Log.d(ApplicationConstants.logTag, e.toString());
        }
    }

    public void finishMatch() {
        try {
            sendFinishGameToServer();
        } catch (Exception e) {
            Log.d(ApplicationConstants.logTag, e.toString());
        }
        MainActivity.player1 = "";
        MainActivity.player2 = "";
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

    private void sendFinishGameToServer() throws JSONException, UnsupportedEncodingException {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        JSONObject request = new JSONObject();
        request.put("match_id", MainActivity.matchId);
        StringEntity entity = new StringEntity(request.toString());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(getApplicationContext(), ApplicationConstants.baseURl + "/finishgame", entity, "application/json", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
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

    private boolean sendScoreChangeToServer(int playerNumber, SCORE_CHANGE_TYPE scoreType) throws UnsupportedEncodingException, JSONException {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        JSONObject request = new JSONObject();
        request.put("match_id", MainActivity.matchId);
        request.put("set_number", String.valueOf(frameNumber));
        request.put("player_number", String.valueOf(playerNumber));
        request.put("type_point", scoreType);
        request.put("brake_pl1", String.valueOf(brakePl1));
        request.put("brake_pl2", String.valueOf(brakePl2));
        request.put("player1Score", String.valueOf(playerOneFrames));
        request.put("player2Score", String.valueOf(playerTwoFrames));
        StringEntity entity = new StringEntity(request.toString());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(getApplicationContext(), ApplicationConstants.baseURl + "/changegame", entity, "application/json", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
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

        return true;
    }

    private void showGameEndDialog() {
        final Context context = MatchActivity.this;
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle("Подтверждение");
        ad.setMessage("Матч закончен?");
        ad.setCancelable(false);
        ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                finishMatch();
            }
        });
        ad.show();
    }

    private void showSetEndDialog(final View view, final int player) {
        final Context context = MatchActivity.this;
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle("Подтверждение");
        ad.setMessage("Закончить сет?");
        ad.setCancelable(false);
        ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (!checkFinishMatch()) {
                    startNewFrame(player);
                } else {
                    TextView matchInfo = findViewById(R.id.textView10);
                    matchInfo.setText("Счет в матче " + playerOneFrames + " : " + playerTwoFrames);
                    showGameEndDialog();
                }
                try {
                    if (!checkFinishMatch()) {
                        sendEndSetToServer(true);
                    } else {
                        sendEndSetToServer(false);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        ad.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (player == 1) {
                    playerOneFrames--;
                    reversePlayerOne(view, SCORE_CHANGE_TYPE.REVERSE_POINT);
                } else {
                    playerTwoFrames--;
                    reversePlayerTwo(view, SCORE_CHANGE_TYPE.REVERSE_POINT);
                }
            }
        });
        ad.show();
    }

    private boolean sendEndSetToServer(boolean isGameContinius) throws UnsupportedEncodingException, JSONException {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        JSONObject request = new JSONObject();
        request.put("match_id", MainActivity.matchId);
        request.put("last_frame", String.valueOf(isGameContinius));
        request.put("player1Score", String.valueOf(playerOneFrames));
        request.put("player2Score", String.valueOf(playerTwoFrames));
        request.put("brake_pl1", String.valueOf(brakePl1));
        request.put("brake_pl2", String.valueOf(brakePl2));
        StringEntity entity = new StringEntity(request.toString());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(getApplicationContext(), ApplicationConstants.baseURl + "/finishset", entity, "application/json", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
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
        return true;
    }

    public void timeoutPlayer1(View view) {
        try {
            sendTimeOutInfoToServer(1, "add");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showTimeoutDialog(MainActivity.player1, 1);
    }

    public void timeoutPlayer2(View view) {
        try {
            sendTimeOutInfoToServer(2, "add");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showTimeoutDialog(MainActivity.player2, 2);
    }


    private void imAlive() throws JSONException, UnsupportedEncodingException {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        RequestParams params = new RequestParams();
        params.put("match_id", MainActivity.matchId);
        client.get(getApplicationContext(), ApplicationConstants.baseURl + "/imalive", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //Do nothing
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

            @Override
            public boolean getUseSynchronousMode() {
                return false;
            }
        });
    }

    private boolean sendTimeOutInfoToServer(int playerNumber, String type) throws JSONException, UnsupportedEncodingException {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        JSONObject request = new JSONObject();
        request.put("match_id", MainActivity.matchId);
        request.put("player_number", String.valueOf(playerNumber));
        request.put("type_point", type);
        StringEntity entity = new StringEntity(request.toString());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(getApplicationContext(), ApplicationConstants.baseURl + "/timeout", entity, "application/json", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
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

        return true;
    }


    private void showTimeoutDialog(String gamer, final int player) {
        final Context context = MatchActivity.this;
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setMessage("Игрок " + gamer + " взял таймаут");
        ad.setNegativeButton("Таймаут закончен",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            sendTimeOutInfoToServer(player, "remove");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        dialog.cancel();
                    }
                });
        ad.setTitle("Таимаут");
        ad.setCancelable(false);
        ad.show();
    }

    private void changeBrake(boolean isAdd) {
        TextView player1Score = (TextView) findViewById(R.id.textView6);
        TextView player2Score = (TextView) findViewById(R.id.textView8);
        boolean deside = false;
        if ((Integer.parseInt(player1Score.getText().toString()) >= 10) && (Integer.parseInt(player2Score.getText().toString()) >= 10)) {
            deside = true;
        }
        if (isAdd) {
            if (!deside) {
                if (brakePl1 != 0) {
                    brakePl1++;
                    if (brakePl1 == 3) {
                        brakePl2 = 1;
                        brakePl1 = 0;
                    }
                } else {
                    brakePl2++;
                    if (brakePl2 == 3) {
                        brakePl1 = 1;
                        brakePl2 = 0;
                    }
                }
            } else {
                if (brakePl1 != 0) {
                    brakePl1++;
                    if (brakePl1 >= 2) {
                        brakePl2 = 1;
                        brakePl1 = 0;
                    }
                } else {
                    brakePl2++;
                    if (brakePl2 >= 2) {
                        brakePl1 = 1;
                        brakePl2 = 0;
                    }
                }
            }
        } else {
            if (!deside) {
                if (brakePl1 != 0) {
                    brakePl1--;
                    if (brakePl1 == 0) {
                        brakePl2 = 2;
                        brakePl1 = 0;
                    }
                } else {
                    brakePl2--;
                    if (brakePl2 == 0) {
                        brakePl1 = 2;
                        brakePl2 = 0;
                    }
                }
            } else {
                if (brakePl1 != 0) {
                    brakePl1--;
                    if (brakePl1 == 0) {
                        brakePl2 = 1;
                        brakePl1 = 0;
                    }
                } else {
                    brakePl2--;
                    if (brakePl2 == 0) {
                        brakePl1 = 1;
                        brakePl2 = 0;
                    }
                }
            }
        }
        showBrakeBraker();
    }


    private void showBrakeBraker() {
        if (brakePl1 != 0) {
            TextView player1Brake = findViewById(R.id.textView2);
            player1Brake.setText(String.valueOf(brakePl1));
            TextView player2Brake = findViewById(R.id.textView11);
            player2Brake.setText(String.valueOf(""));
        }
        if (brakePl2 != 0) {
            TextView player1Brake = findViewById(R.id.textView2);
            player1Brake.setText(String.valueOf(""));
            TextView player2Brake = findViewById(R.id.textView11);
            player2Brake.setText(String.valueOf(brakePl2));
        }
    }

    public void reversreBraker(View view) {
        int temp = brakePl1;
        brakePl1 = brakePl2;
        brakePl2 = temp;
        showBrakeBraker();
        Button revB = (Button) findViewById(R.id.button9);
        revB.setVisibility(View.INVISIBLE);
    }

    public void gamePause(View view) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        client.get(getApplicationContext(), ApplicationConstants.baseURl + "/pausegame?id=" + MainActivity.gameID, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                TextView matchInfo = findViewById(R.id.textView10);
                if (isPaused) {
                    matchInfo.setText("Счет в матче " + playerOneFrames + " : " + playerTwoFrames);
                    isPaused = false;
                } else {
                    matchInfo.setText("Матч приостановлен");
                    isPaused = true;
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    String str = new String(responseBody, "UTF-8");
                    Log.d(ApplicationConstants.logTag, str);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });

    }

    public void reverse(View view) {
        if (historyStep == 0 && frameNumber > 1) {
            frameNumber--;
            getSetSnapshot();
        }
        History_Points point = history.get(historyStep);
        switch (point) {
            case PLAYER1INKSCORE:
                reversePlayerOne(view, SCORE_CHANGE_TYPE.REVERSE_POINT);
                break;
            case PLAYER2INKSCORE:
                reversePlayerTwo(view, SCORE_CHANGE_TYPE.REVERSE_POINT);
                break;
            case PLAYER1YELLOW:
                player1Yellow = false;
                checkAfterRed();
                break;
            case PLAYER2YELLOW:
                player2Yellow = false;
                checkAfterRed();
                break;
            case PLAYER1RED:
                reversePlayerTwo(view, SCORE_CHANGE_TYPE.RED_CARD);
                player1Yellow = false;
                checkAfterRed();
                break;
            case PLAYER2RED:
                reversePlayerOne(view, SCORE_CHANGE_TYPE.RED_CARD);
                player2Yellow = false;
                checkAfterRed();
                break;
            case PLAYER1REDAFTERYELLOW:
                reversePlayerTwo(view, SCORE_CHANGE_TYPE.RED_CARD);
                player1Yellow = true;
                checkAfterRed();
                break;
            case PLAYER2REDAFTERYELLOW:
                reversePlayerOne(view, SCORE_CHANGE_TYPE.RED_CARD);
                player2Yellow = true;
                checkAfterRed();
                break;
        }
        historyStep--;
        TextView matchInfo = findViewById(R.id.textView10);
        matchInfo.setText("Счет в матче " + playerOneFrames + " : " + playerTwoFrames);
        TextView setInfo = findViewById(R.id.textView9);
        setInfo.setText("Сет " + frameNumber + " из (" + String.valueOf(MainActivity.frameCount) + ")");
        if (historyStep == 0 && frameNumber == 1) {
            Button revB = (Button) findViewById(R.id.button3);
            revB.setVisibility(View.INVISIBLE);
        }
    }

    private void getSetSnapshot() {
        SetSnapshot snapshot = setSnaphots.get(frameNumber);

        history = new HashMap<>(snapshot.getHistory());
        historyStep = history.size();
        brakePl1 = snapshot.getPlayer1Brake();
        brakePl2 = snapshot.getPlayer2Brake();
        playerOneFrames = snapshot.getPlayer1Score();
        playerTwoFrames = snapshot.getPlayer2Score();
        frameNumber = snapshot.getSetNumber();
        TextView player1Score = (TextView) findViewById(R.id.textView6);
        player1Score.setText(String.valueOf(snapshot.getPlayer1SetScore()));
        TextView player2Score = (TextView) findViewById(R.id.textView8);
        player2Score.setText(String.valueOf(snapshot.getPlayer2SetScore()));
        setSnaphots.remove(snapshot);
    }

    public void player1YellowCard(View view) {
        if (player1Yellow) {
            player1RedCard(view, true);
        } else {
            TextView player1Card = findViewById(R.id.textView14);
            player1Card.setBackgroundColor(0xffffbb33);
            player1Yellow = true;
            historyStep++;
            history.put(historyStep, History_Points.PLAYER1YELLOW);
            addPointToPlayerOne(view, SCORE_CHANGE_TYPE.YELLOW_CARD);
        }
    }

    public void player1RedCard(View view, boolean isAfterYellow) {
        addPointToPlayerTwo(view, SCORE_CHANGE_TYPE.RED_CARD);
        TextView player1Card = findViewById(R.id.textView14);
        player1Card.setBackgroundColor(0xffcc0000);
        player1Yellow = false;
        historyStep++;
        if (isAfterYellow) {
            history.put(historyStep, History_Points.PLAYER1REDAFTERYELLOW);
        } else {
            history.put(historyStep, History_Points.PLAYER1RED);
        }
    }


    public void player2YellowCard(View view) {
        if (player2Yellow) {
            player2RedCard(view, true);
        } else {
            TextView player2Card = findViewById(R.id.textView15);
            player2Card.setBackgroundColor(0xffffbb33);
            player2Yellow = true;
            historyStep++;
            history.put(historyStep, History_Points.PLAYER2YELLOW);
            addPointToPlayerTwo(view, SCORE_CHANGE_TYPE.YELLOW_CARD);
        }
    }

    public void player2RedCard(View view, boolean isAfterYellow) {
        addPointToPlayerOne(view, SCORE_CHANGE_TYPE.RED_CARD);
        TextView player2Card = findViewById(R.id.textView15);
        player2Card.setBackgroundColor(0xffcc0000);
        player2Yellow = false;
        historyStep++;
        if (isAfterYellow) {
            history.put(historyStep, History_Points.PLAYER2REDAFTERYELLOW);
        } else {
            history.put(historyStep, History_Points.PLAYER2RED);
        }
    }


    private void checkAfterRed() {
        TextView player1Card = findViewById(R.id.textView14);
        TextView player2Card = findViewById(R.id.textView15);
        if (!player1Yellow) {
            player1Card.setBackgroundColor(0xffffffff);
        } else {
            player1Card.setBackgroundColor(0xffffbb33);
        }
        if (!player2Yellow) {
            player2Card.setBackgroundColor(0xffffffff);
        } else {
            player2Card.setBackgroundColor(0xffffbb33);
        }
    }
}
