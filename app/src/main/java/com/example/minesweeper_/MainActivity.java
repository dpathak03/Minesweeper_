package com.example.minesweeper_;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Locale;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    private int time = 0;
    int isFirstClick = 0;
    private TextView timer;
    boolean isDigging = true;

    boolean isWinner = false;

    private boolean[][] bombLocation;

    private CountDownTimer clock;

    private boolean[][] visited;

    TextView digger;
    TextView flagCounter;
    private static final int COLUMN_COUNT = 10; //12
    private static final int ROW_COUNT = 12; //10
    private String MINE_ICON = "\uD83D\uDCA3";
    private String FLAG_ICON = "\uD83D\uDEA9";
    private String PICK_ICON = "\u26CF";
    private TextView[][] cell_tvs;
    private int initialFlagCounter = 4;
    private boolean bombClicked = false;
    private boolean winnerFound = false;
    private String[][] originalString = new String[ROW_COUNT][COLUMN_COUNT];
    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    //calculate the number that should be printed in a cell based on adjacent bomb locations
    private int totalAdjacent(int i, int j)
    {
        int counter = 0;
        for (int r = i - 1; r <= i + 1; r++) {
            for (int c = j - 1; c <= j + 1; c++) {
                //boundary check
                if (r < 0 || c < 0 || r >= ROW_COUNT || c >= COLUMN_COUNT) {
                    continue;
                }
                if (bombLocation[r][c]) {
                    counter++;
                }
            }
        }
        //return the number of bombs in the adjacent cells
        return counter;
    }


    //bfs implementation over here
    private void initialDig(int r, int c)
    {
        Queue<int[]> cellsNeighbors = new LinkedList<>();
        cellsNeighbors.add(new int[]{r, c});
        visited = new boolean[ROW_COUNT][COLUMN_COUNT];
        while (!cellsNeighbors.isEmpty()) {
            int[] cell = cellsNeighbors.poll();
            int row = cell[0];
            int col = cell[1];
            if (row < 0 || row >= ROW_COUNT || col < 0 || col >= COLUMN_COUNT) {
                continue;
            }
            if (visited[row][col] || bombLocation[row][col]) {
                continue;
            }
            visited[row][col] = true;
            TextView tv = cell_tvs[row][col];
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);
            int bombCount = totalAdjacent(row, col);
            if (bombCount > 0) {
                cell_tvs[row][col].setText(String.valueOf(bombCount));
                continue;
            }
            cellsNeighbors.add(new int[]{row - 1, col});
            cellsNeighbors.add(new int[]{row + 1, col});
            cellsNeighbors.add(new int[]{row, col - 1});
            cellsNeighbors.add(new int[]{row, col + 1});
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
            digger = findViewById(R.id.pick_icon);
            flagCounter = findViewById(R.id.flagCounter);
        }
        digger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDigging) {
                    digger.setText(FLAG_ICON);
                    isDigging = false;
                } else {
                    digger.setText(PICK_ICON);
                    isDigging = true;
                }
            }
        });
        // Method (2): add four dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
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
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                originalString[i][j] = cell_tvs[i][j].getText().toString();
            }
        }
    }
    //check if there is a winner
    private void checkWinner()
    {
        int greenCellCount = 0;
        // Iterate through all cells
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = cell_tvs[i][j];
                // Check if the background color is green
                if (((ColorDrawable)tv.getBackground()).getColor() == Color.parseColor("lime")) {
                    greenCellCount++;
                }
            }
        }
        if (greenCellCount == 4) {
            isWinner = true;
        }
    }
    private void setMines() {
        Random rand = new Random();
        bombLocation = new boolean[ROW_COUNT][COLUMN_COUNT];
        int numMine = 0;
        while (numMine < 4) {
            int rc = rand.nextInt(COLUMN_COUNT);
            int rr = rand.nextInt(ROW_COUNT);
            if (!bombLocation[rr][rc]) {
                bombLocation[rr][rc] = true;
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
    public void onClickTV(View view) {
        TextView tv = (TextView) view;
        int[] idx = findIndexOfCellTextView(tv);
        int i = idx[0];
        int j = idx[1];
        isFirstClick += 1;
        if (isFirstClick == 1) {
            //timer logic
            clock = new CountDownTimer(Long.MAX_VALUE, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    time++;
                    timer.setText(String.format(Locale.getDefault(), "%d", time));
                }
                @Override
                public void onFinish() {
                }
            }.start();
        }
        if (isDigging) {
            if (isFirstClick == 1) {
                //bfs dig
                initialDig(i, j);
            }
            if(bombClicked) {
                Intent intent = new Intent(this, GameOverActivity.class);
                intent.putExtra("TIME", time);
                startActivity(intent);
            }
            //there is a bomb in this location
            if (bombLocation[i][j] && !winnerFound) {
                bombClicked = true;
                clock.cancel();
                //iterate through and set string in all of the bombs
                for (int m = 0; m < ROW_COUNT; m++) {
                    for (int n = 0; n < COLUMN_COUNT; n++) {
                        if (bombLocation[m][n]) {
                            cell_tvs[m][n].setText(MINE_ICON);
                            cell_tvs[m][n].setBackgroundColor(Color.parseColor("red"));
                        }
                    }
                }
            } else {
                if (tv.getCurrentTextColor() == Color.GRAY) {
                    int bombCount = totalAdjacent(i, j);
                    if (bombCount > 0) {
                        cell_tvs[i][j].setText(String.valueOf(bombCount));
                    }
                    if (bombCount == 0) {
                        initialDig(i, j);
                    }
                    tv.setTextColor(Color.GRAY);
                    tv.setBackgroundColor(Color.LTGRAY);
                    //winner!
                    if(winnerFound) {
                        Intent intent = new Intent(this, GameWinnerActivity.class);
                        intent.putExtra("TIME", time);
                        startActivity(intent);
                    }
                    checkWinner();
                    if(isWinner) {
                        winnerFound = true;
                        clock.cancel();
                    }
                }
            }
        } else {
            //FLAG LOGIC
            if (cell_tvs[i][j].getText().equals(FLAG_ICON)) {
                cell_tvs[i][j].setText(originalString[i][j]);
                initialFlagCounter++;
            } else {
                if(((ColorDrawable)tv.getBackground()).getColor() == Color.parseColor("lime")) {
                    cell_tvs[i][j].setText(FLAG_ICON);
                    initialFlagCounter--;
                }
            }
            flagCounter.setText(String.valueOf(initialFlagCounter));
        }

        }


    }

