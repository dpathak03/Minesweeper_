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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
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

    //calculate the number that should be printed in a cell based on adjacent bomb locations
    private int totalAdjacent(int i, int j)
    {
        int bombCount = 0;

        for (int r = i - 1; r <= i + 1; r++) {
            for (int c = j - 1; c <= j + 1; c++) {
                // Skip if out of bounds or if it's the current cell
                if (r < 0 || r >= ROW_COUNT || c < 0 || c >= COLUMN_COUNT ) {
                    continue;
                }
                if (bombLocation[r][c]) {
                    bombCount++;
                }
            }
        }
        return bombCount;
    }


    //bfs implementation over here
    private void initialDig(int r, int c)
    {
        Queue<int[]> cellsToProcess = new LinkedList<>();
        boolean[][] visited = new boolean[ROW_COUNT][COLUMN_COUNT];
        cellsToProcess.add(new int[]{r, c});

        while (!cellsToProcess.isEmpty()) {
            int[] cell = cellsToProcess.poll(); // Remove the first cell

            int row = cell[0];
            int col = cell[1];
            //boundary check
            if (row < 0 || row >= ROW_COUNT || col < 0 || col >= COLUMN_COUNT || visited[row][col] || bombLocation[row][col]) {
                continue; // Skip this cell if it's out of bounds, visited, or contains a bomb
            }

            visited[row][col] = true; // Mark cell as visited

            // Process the cell if it doesn't have a bomb
            TextView tv = cell_tvs[row][col];
            //tv.setText(String.valueOf(row) + String.valueOf(col));
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);

            int bombCount = totalAdjacent(row, col);
            if (bombCount > 0) {
                cell_tvs[row][col].setText(String.valueOf(bombCount));
                continue;
            }

            // Add neighboring cells to be processed
            cellsToProcess.add(new int[]{row - 1, col}); // Up
            cellsToProcess.add(new int[]{row + 1, col}); // Down
            cellsToProcess.add(new int[]{row, col - 1}); // Left
            cellsToProcess.add(new int[]{row, col + 1}); // Right
        }
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

            //bfs dig
            initialDig(i,j);
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
                int bombCount = totalAdjacent(i, j);
                if (bombCount > 0) {
                    cell_tvs[i][j].setText(String.valueOf(bombCount));
                }
                if(bombCount == 0)
                {
                    initialDig(i,j);
                }
                tv.setTextColor(Color.GRAY);
                 tv.setBackgroundColor(Color.LTGRAY);
            }
        }
    }
}
