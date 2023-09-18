package com.example.minesweeper_;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Random;
import java.util.Locale;
import android.os.Handler;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    private int time = 0;
    int isFirstClick = 0;
    private TextView timer;
    boolean isDigging = true;

    private boolean[][] bombLocation;

    TextView diggingTool;


    private static final int COLUMN_COUNT = 10; //12
    private static final int ROW_COUNT = 12; //10
    private String MINE_ICON = "\uD83D\uDCA3";
    private String FLAG_ICON = "\uD83D\uDEA9";

    private String PICK_ICON = "\u26CF";

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private TextView[][] cell_tvs;
    private int initialFlagCounter = 4;


    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new TextView[ROW_COUNT][COLUMN_COUNT];
        timer = findViewById(R.id.timer);
        timer.setText("0");

        if(isDigging) {
            diggingTool = findViewById(R.id.pick_icon);
        }
        diggingTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDigging) {
                    diggingTool.setText(FLAG_ICON);
                    isDigging = false;
                } else {
                    diggingTool.setText(PICK_ICON);
                    isDigging = true;
                }
            }
        });


        // Method (2): add four dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);

        for (int i = 0; i < ROW_COUNT; i++) { // Rows loop
            for (int j = 0; j < COLUMN_COUNT; j++) { // Columns loop

                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(25));
                tv.setWidth(dpToPixel(25));
                tv.setTextSize(14);
                TextView flagCounter = findViewById(R.id.flagCounter);
                flagCounter.setText("" + initialFlagCounter);
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.parseColor("lime"));
                tv.setOnClickListener(this::onClickTV);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);
                grid.addView(tv, lp);
                cell_tvs[i][j] = tv;
            }
        }
        setMines();
    }

    private void setMines() {
        Random rand = new Random();
        bombLocation = new boolean[ROW_COUNT][COLUMN_COUNT];

        int numMine = 0;
        while (numMine < 4) {
            int randRow = rand.nextInt(ROW_COUNT);
            int randCol = rand.nextInt(COLUMN_COUNT);

            if (!bombLocation[randRow][randCol]) {
                bombLocation[randRow][randCol] = true;
                numMine++;
            }
        }
    }
    private int[] findIndexOfCellTextView(TextView tv) {
        for (int n = 0; n < ROW_COUNT; n++) {
            for (int m = 0; m < COLUMN_COUNT; m++) {
                if (cell_tvs[n][m] == tv) {
                    return new int[]{n, m};
                }
            }
        }
        return new int[0];
    }


    public void onClickTV(View view){
        TextView tv = (TextView) view;
        int[] idx = findIndexOfCellTextView(tv);
        int i = idx[0];
        int j = idx[1];
        tv.setText(String.valueOf(i)+String.valueOf(j));
        isFirstClick+=1;
        if(isFirstClick == 1) {
            //timer logic
            new CountDownTimer(Long.MAX_VALUE, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    time++;
                    timer.setText(String.format(Locale.getDefault(), "%d", time));
                }
                @Override
                public void onFinish() {
                    //Not needed for this project, having it to implement abstract class
                }
            }.start();
        }

        //there is a bomb in this location
        if(bombLocation[i][j]) {
            tv.setText(MINE_ICON);
            Intent intent = new Intent(this, GameOverActivity.class);
            intent.putExtra("TIME", time);
            startActivity(intent);
            return;

        } else {
            if (tv.getCurrentTextColor() == Color.GRAY ) {
                tv.setTextColor(Color.GRAY);
                 tv.setBackgroundColor(Color.LTGRAY);
            }
        }
    }
}
