package com.example.florian.myapplication.HistoryAdapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.florian.myapplication.Activities.Historiques.Stocker.InventoryStocker;
import com.example.florian.myapplication.Database.CampagneDatabase.Inventaire;
import com.example.florian.myapplication.Database.LoadingDatabase.TaxUsrDAO;
import com.example.florian.myapplication.R;
import com.example.florian.myapplication.Tools.Utils;

import java.util.ArrayList;
import java.util.List;

public class InventaireAdapter extends ArrayAdapter<Inventaire>{

    private TaxUsrDAO dao;
    private InventoryStocker checkedInventairesStocker;
    public List<CheckBox> allCheckBoxes;

    public InventaireAdapter(Context context, List<Inventaire> inventaires) {
        super(context, 0, inventaires);
        dao = new TaxUsrDAO(context);
        checkedInventairesStocker = new InventoryStocker(context);
        allCheckBoxes = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_item_inventaires,parent, false);
        }

        InventaireAdapter.InventaireViewHolder viewHolder = (InventaireAdapter.InventaireViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new InventaireAdapter.InventaireViewHolder();
            viewHolder.nomEspece = (TextView) convertView.findViewById(R.id.nomEspeceInv);
            viewHolder.denombrement = (TextView) convertView.findViewById(R.id.denombrementInv);
            viewHolder.date = (TextView) convertView.findViewById(R.id.dateInv);
            viewHolder.heure = (TextView) convertView.findViewById(R.id.heureInventaire);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.itemCheckbox);
            allCheckBoxes.add(viewHolder.checkBox);

            convertView.setTag(viewHolder);
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        final Inventaire inv = getItem(position);

        String nom;
        String nomFr= inv.getNomFr();
        //il ne reste plus qu'à remplir notre vue
        nom = inv.getNomLatin();
        dao.open();
        int nv = dao.getNiveau(new String[]{nom,nomFr});
        dao.close();

        nom = addSpToString(nv,nom);

        setNiceColorToView(nv, viewHolder.nomEspece);

        if(!nomFr.isEmpty())
            nom += " - " + nomFr;

        viewHolder.nomEspece.setText(nom);
        int nb = inv.getNombre();
        // Si le dénombrement n'a pas été défini
        if(nb == 0)
            viewHolder.denombrement.setText("Présence");
        else
            viewHolder.denombrement.setText(nb + "");
        viewHolder.date.setText(Utils.printDateWithYearIn2Digit(inv.getDate()));
        viewHolder.heure.setText(inv.getHeure());

        if(inv.getErr() == 1)
            convertView.setBackgroundColor(Color.RED);

        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    checkedInventairesStocker.add(inv);
                else
                    checkedInventairesStocker.remove(inv);
            }
        });

        return convertView;
    }

    private String addSpToString(int nv, String name){
        if(nv == 5)
            return name + " sp.";
        return name;
    }

    private void setNiceColorToView(int nv, TextView view){
        int color;
        if(nv == 5){
            color = Color.BLUE;
        } else if(nv == 6)
            color = Color.BLACK;
        else if(nv == 7)
            color = Color.GRAY;
        else
            color = Color.rgb(255,140,0);
        view.setTextColor(color);
    }

    private class InventaireViewHolder {
        public TextView nomEspece;
        public TextView denombrement;
        public TextView date;
        public TextView heure;
        public CheckBox checkBox;
    }

    public InventoryStocker getCheckedInventairesStocker() {
        return checkedInventairesStocker;
    }
}