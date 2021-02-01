package com.example.test_1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.function.Predicate;
import java.util.prefs.PreferenceChangeEvent;

public class MainActivity extends AppCompatActivity {

    private EditText txtGuess;
    private Button btnGuess;
    private TextView lblOutput;
    private int theNumber;
    private int numberOfTries = 0;
    private int numberOfTriesMax = 7;
    private int range = 0;
    private TextView lblRange;
    private static final String GAMESWON = "gamesWon";

    public void checkGuess() {
        String guessText = txtGuess.getText().toString();
        String message = "";
        numberOfTries++;
        try {
            int guess = Integer.parseInt(guessText);
            if (guess < theNumber && numberOfTries <= numberOfTriesMax)
                message = "\"" + guess + "\" is too low. " + numberOfTries + " tries from "
                        + numberOfTriesMax + ".\nTry again.";
            else if (guess > theNumber && numberOfTries <= numberOfTriesMax)
                message = "\"" + guess + "\" is too high. " + numberOfTries + " tries from "
                        + numberOfTriesMax + ".\nTry again.";
            else if (guess == theNumber && numberOfTries <= numberOfTriesMax){
                message = "\"" + guess + "\" is correct. \nYOU WIN after " + numberOfTries
                        + " tries!";

                //добавили +1 чтобы учесть победу
                int gameWon = readSharedPreferences(GAMESWON, 0) + 1;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                //создаем редактор что бы написать новое значение в общие настройки
                SharedPreferences.Editor editor = preferences.edit();
                // помещаем целочисленное значение gameWon в общие настройки под ключем
                // с соответствующим именем
                editor.putInt(GAMESWON, gameWon);
                // применяем изменения
                editor.apply();

                //всплывающее уведомление
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                newGame();
            }
            else {
                message = "GAME OVER! \nYou have spent all " + numberOfTriesMax + " attempts!";
                //всплывающее уведомление
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                newGame();
            }
        } catch(Exception e){
            message = "Enter a whole number between 1 and " + range + ".";
        } finally {
            lblOutput.setText(message);
            //возвращаем курсор в текстовое поле
            txtGuess.requestFocus();
            //выделяем весь текст
            txtGuess.selectAll();
        }
    }

    //создаем новую игру
    public void newGame(){
        //создаем генератор загадываемого числа
        theNumber = (int) (Math.random() * range + 1);
        //предложение ввести число от 1 до значения переменной range
        lblRange.setText("Enter a number between 1 and " + range + ".");
        //ввод значения по умолчанию в текстовое поле равное половине range
        txtGuess.setText("" + range / 2);
        txtGuess.requestFocus();
        txtGuess.selectAll();
        //обнуляем счетчик
        numberOfTries = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtGuess = (EditText) findViewById(R.id.txtGuess);
        btnGuess = (Button) findViewById(R.id.btnGuess);
        lblOutput = (TextView) findViewById(R.id.lblOutput);
        lblRange = (TextView) findViewById(R.id.textView2);

        //получаем значение верхней границы диапазона из общих настроек
        range = readSharedPreferences("range", 100);

        newGame();

        //добавляем слушатель клика пальцем
        btnGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGuess();
            }
        });

        //добавляем слушатель кнопки Enter
        txtGuess.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                checkGuess();
                return true;
            }
        });
    }

    //создаем кнопку МЕНЮ (три точки)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    //описываем логику при нажатии на копки меню
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_setting:
                //отображает окно оповещения со списком из 3х вариантов загаданного числа
                final CharSequence[] items = {"1 to 10", "1 to 100", "1 to 1000"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select the Range:");
                //принимает список элементов и слушатель событий для обработки выбора пользователя
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        //присваеваем переменной range значение выбранное пользователем
                        switch(item){
                            case 0:
                                range = 10;
                                storeRange(10);
                                newGame();;
                                break;
                            case 1:
                                range = 100;
                                storeRange(100);
                                newGame();;
                                break;
                            case 2:
                                range = 1000;
                                storeRange(1000);
                                newGame();;
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case R.id.action_newgame:
                newGame();
                return  true;
            case R.id.action_gamestats:
                int gamesWon = readSharedPreferences(GAMESWON, 0);
                AlertDialog statDialog = new AlertDialog.Builder(MainActivity.this).create();
                statDialog.setTitle("Guessing Game Stats");
                statDialog.setMessage("You have won" + gamesWon + " games. Way to go!");
                statDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                statDialog.show();
                return true;
            //при нажатии на кнопку меню ABOUT открывается настраиваемое всплывающее окно
            case R.id.action_about:
                //создаем экземпляр класса настраиваемого всплывающего окна
                AlertDialog aboutDialog = new AlertDialog.Builder(MainActivity.this).create();
                //текст заголовка всплывающег оокна
                aboutDialog.setTitle("About Guessing Game");
                //простое сообщение
                aboutDialog.setMessage("(c)2021 dgioto");
                //добавляем кнопку с текстом ОК
                aboutDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //закрываем всплывающее окно
                                dialog.dismiss();
                            }
                        });
                aboutDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Хранение и получение предпочтительного диапазона пользователя
    public void storeRange(int newRange){
        //получение доступа к объекту общих настроек по умолчанию
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //для записи в общие настройки используем объект Editor
        SharedPreferences.Editor editor = preferences.edit();
        //сохраняем в переменную значение newRange для ключа range
        editor.putInt("range", newRange);
        //применяем обновление значений общих настроек
        editor.apply();
    }

    private int readSharedPreferences(String name, int defaultValue) {
        //получаем доступ к объекту общих настроек
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //получаем значение хранящееся в ключе, со значением по умолчанию
        return preferences.getInt(name, defaultValue);
    }
}