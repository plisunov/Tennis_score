package com.tenins.andrey.tennis_score;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class ChoosePlayers extends AppCompatActivity {

    private String tournamentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_players);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        getPlayersInfos();
    }


    private void getTournamentInfo() {
        final Context context = ChoosePlayers.this;
        final AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        client.get(getApplicationContext(), ApplicationConstants.baseURl + "/tournamentInfo", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String str = new String(responseBody, "UTF-8");
                    Map<String, String> responseMap[] = new ObjectMapper().readValue(str, HashMap[].class);

                    Spinner spinnerTournament = (Spinner) findViewById(R.id.spinner);
                    List<StringWithTag> list = new ArrayList<StringWithTag>();
                    for (int i = 0; i < responseMap.length; i++) {
                        list.add(new StringWithTag(String.valueOf(responseMap[i].get("id")), responseMap[i].get("pagetitle"), responseMap[i].get("description")));
                    }
                    ArrayAdapter<StringWithTag> dataAdapter = new ArrayAdapter<StringWithTag>(context, android.R.layout.simple_spinner_item, list);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTournament.setAdapter(dataAdapter);
                    spinnerTournament.setOnItemSelectedListener(new OnSpinnerTournamentSelectedItem());
                } catch (Exception e) {
                    Log.d(ApplicationConstants.logTag, e.toString());
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


    private void getTournamentInfos() {
        final Context context = ChoosePlayers.this;
        final AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        client.get(getApplicationContext(), ApplicationConstants.baseURl + "/selectplayers", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String str = new String(responseBody, "UTF-8");
                    Map<String, String> responseMap[] = new ObjectMapper().readValue(str, HashMap[].class);

                    Spinner spinnerPlayer = (Spinner) findViewById(R.id.spinner2);
                    List<StringPlayers> list = new ArrayList<StringPlayers>();
                    for (int i = 0; i < responseMap.length; i++) {
                        list.add(new StringPlayers(responseMap[i].get("player1"), responseMap[i].get("player2"), responseMap[i].get("stringGameId")));
                    }
                    ArrayAdapter<StringPlayers> dataAdapter = new ArrayAdapter<StringPlayers>(context, android.R.layout.simple_spinner_item, list);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPlayer.setAdapter(dataAdapter);
                } catch (Exception e) {

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

    private void getPlayersInfos() {
        final Context context = ChoosePlayers.this;
        final AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(ApplicationConstants.user, ApplicationConstants.password);
        client.get(getApplicationContext(), ApplicationConstants.baseURl + "/selectplayers", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String str = new String(responseBody, "UTF-8");
                    Map<String, String> responseMap[] = new ObjectMapper().readValue(str, HashMap[].class);

                    Spinner spinnerPlayer = (Spinner) findViewById(R.id.spinner2);
                    List<StringPlayers> list = new ArrayList<StringPlayers>();
                    for (int i = 0; i < responseMap.length; i++) {
                        list.add(new StringPlayers(responseMap[i].get("player1"), responseMap[i].get("player2"), responseMap[i].get("stringGameId")));
                    }
                    ArrayAdapter<StringPlayers> dataAdapter = new ArrayAdapter<StringPlayers>(context, android.R.layout.simple_spinner_item, list);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPlayer.setAdapter(dataAdapter);
                } catch (Exception e) {

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

    public void choosePlayers(View view) {
        Spinner spinnerPlayers = (Spinner) findViewById(R.id.spinner2);
        StringPlayers selectedPlayers = (StringPlayers) spinnerPlayers.getSelectedItem();
        if (selectedPlayers != null) {
            MainActivity.player1 = selectedPlayers.player1;
            MainActivity.player2 = selectedPlayers.player2;
            MainActivity.matchId = selectedPlayers.matchId;
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public class StringPlayers {
        public String player1;
        public String player2;
        public String matchId;

        public StringPlayers(String player1, String player2, String matchId) {
            this.player1 = player1;
            this.player2 = player2;
            this.matchId = matchId;
        }

        public String toString() {
            return this.player1 + "-" + this.player2;
        }

    }

    public class StringWithTag {
        public String key;
        public String value;
        public String description;

        public StringWithTag(String key, String value, String description) {
            this.key = key;
            this.value = value;
            this.description = description;
        }

        public String toString() {
            return this.value;
        }
    }

    public class OnSpinnerTournamentSelectedItem implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Spinner spinnerTournament = (Spinner) findViewById(R.id.spinner);
            StringWithTag selectedTournametn = (StringWithTag) spinnerTournament.getSelectedItem();
            tournamentID = selectedTournametn.key;
            getPlayersInfos();
            Log.d(ApplicationConstants.logTag, "Tournamet Id = " + tournamentID);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
}
