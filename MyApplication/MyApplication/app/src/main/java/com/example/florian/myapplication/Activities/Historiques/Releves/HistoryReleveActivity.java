package com.example.florian.myapplication.Activities.Historiques.Releves;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.florian.myapplication.Activities.Historiques.Releves.InfoRelevesPopups.PopUpLigne;
import com.example.florian.myapplication.Activities.Historiques.Releves.InfoRelevesPopups.PopUpPoint;
import com.example.florian.myapplication.Activities.Historiques.Releves.InfoRelevesPopups.PopUpPolygone;
import com.example.florian.myapplication.Database.ReleveDatabase.HistoryDao;
import com.example.florian.myapplication.Database.ReleveDatabase.Releve;
import com.example.florian.myapplication.R;
import com.example.florian.myapplication.Tools.ReleveAdapter;

import java.util.List;

public class HistoryReleveActivity extends AppCompatActivity {

    protected ListView listReleves;
    protected HistoryDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dao = new HistoryDao(this);
        dao.open();

        setContentView(R.layout.activity_history);

        listReleves = (ListView) findViewById(R.id.listViewReleve);
        listReleves.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView nomRelTxt = (TextView)view.findViewById(R.id.nomRel);
                TextView typeRelTxt = (TextView)view.findViewById(R.id.typeReleve);
                TextView dateRelTxt = (TextView)view.findViewById(R.id.dateReleve);
                TextView heureRelTxt = (TextView)view.findViewById(R.id.heureReleve);

                Releve rel = dao.getReleveFromNomTypeDateHeure(new String[]{nomRelTxt.getText().toString(), typeRelTxt.getText().toString(), heureRelTxt.getText().toString()});

                Intent intent = generateGoodIntent(typeRelTxt.getText().toString());
                intent.putExtra("releve",rel);
                startActivity(intent);
            }
        });
    }

    protected Intent generateGoodIntent(String typeReleve){
        if(typeReleve.equals(getString(R.string.point)))
            return new Intent(HistoryReleveActivity.this, PopUpPoint.class);
        if(typeReleve.equals(getString(R.string.line)))
            return new Intent(HistoryReleveActivity.this, PopUpLigne.class);
        return new Intent(HistoryReleveActivity.this, PopUpPolygone.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAdapter();
    }

    private void setAdapter(){
        List<Releve> releves = dao.getReleveOfTheUsr(getCurrentUsrId());

        ReleveAdapter adapter = new ReleveAdapter(this,releves);
        listReleves.setAdapter(adapter);
    }

    protected long getCurrentUsrId(){
        SharedPreferences loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        return loginPreferences.getLong("usrId",0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dao.close();
    }
}