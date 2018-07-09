package com.example.eden62.GENSMobile.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.eden62.GENSMobile.Database.LoadingDatabase.TaxUsrDAO;
import com.example.eden62.GENSMobile.Parser.CsvToSQLite.TaxRefParser;
import com.example.eden62.GENSMobile.Parser.CsvToSQLite.UserParser;
import com.example.eden62.GENSMobile.R;
import com.example.eden62.GENSMobile.Tools.Utils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Activité login qui permet de se log via un login/mot de passe.
 */
public class LoginActivity extends AppCompatActivity {

    private TaxUsrDAO dao;

    /**
     * Garde en mémoiré l'état de la connection pour s'assurer qu'on puisse l'arrêter si c'est demandé.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mLoginView;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dao = new TaxUsrDAO(this);
        dao.open();
        loadData();

        // Set up the login form.
        mLoginView = (EditText) findViewById(R.id.login);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        if(loginPreferences.getInt("idCampagne",-1) == -1) {
            loginPrefsEditor.putInt("idCampagne", 0);
            loginPrefsEditor.commit();
        }

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Créer un fichier vide pour vérifier si les bases ont été chargé ou non
     *
     * @param filename Le nom du fichier à créer
     * @param content Le contenu du fichier
     */
    private void createNewFile(String filename, String content){
       FileOutputStream outputStream;
       try {
           outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
           outputStream.write(content.getBytes());
           outputStream.close();
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    /**
     * Vérifie si le chargement des bases de données à déjà été réalisé
     *
     * @param filename Le nom du fichier de vérification
     * @return <code>True</code> si oui, <code>false</code> sinon
     */
    private boolean loadComplete(String filename){
        return new File(this.getFilesDir(),filename).exists();
    }

    /**
     * Charge les bases de données Taxref et Utilisateur
     */
    private void loadData() {
        String filename = "loadComplete.txt";
        if(!loadComplete(filename)){
            String fileContents = "toto";
            UserParser parserUsr = new UserParser(this);
            parserUsr.parseCSVFileToDb(dao);
            TaxRefParser parserTax = new TaxRefParser(this);
            parserTax.parseCSVFileToDb(dao);
            createNewFile(filename, fileContents);
        }
    }

    /**
     * Tente de connecter l'utilisateur via le login/mdp qu'il a fournit.
     * Si le couple ne correspond pas (pas dans la base, champ manquant, etc.),
     * un message d'erreur est affiché et aucune tentative de conenction est réalisée
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLoginView.setError(null);

        // Store values at the time of the login attempt.
        String login = mLoginView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid login.
        if (TextUtils.isEmpty(login)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        } else if (!isLoginValid(login)) {
            mLoginView.setError(getString(R.string.error_incorrect_login));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(login);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isLoginValid(String email) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dao.close();
    }


    /**
     * Représente une tâche asynchrone qui permet de connecter l'utilisateur
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mLogin;

        UserLoginTask(String login) {
            mLogin = login;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Cursor c = dao.checkUsrValid(new String[] {mLogin});
            return c.moveToNext();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                rememberUsrId();
                Utils.hideKeyboard(getApplicationContext(),getCurrentFocus());
                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            } else {
                mLoginView.setError(getString(R.string.error_incorrect_login));
                mLoginView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }

        private void rememberUsrId(){
            long usrId = dao.getUsrId(new String[]{mLogin});
            loginPrefsEditor.putString("username", mLogin);
            loginPrefsEditor.putLong("usrId",usrId);
            loginPrefsEditor.commit();
        }
    }
}

