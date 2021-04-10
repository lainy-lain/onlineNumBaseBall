package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class Setting extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Button[] buttons = {
                findViewById(R.id.play_setbtn), findViewById(R.id.single_setbtn), findViewById(R.id.multi_setbtn)
                , findViewById(R.id.rank_setbtn), findViewById(R.id.set_setbtn)
        };
        LinearLayout colorKind = findViewById(R.id.colorKind);
        Button[] colorKindButtons = {
                findViewById(R.id.red), findViewById(R.id.green), findViewById(R.id.blue)
                , findViewById(R.id.yellow), findViewById(R.id.cyan), findViewById(R.id.magenta)
                , findViewById(R.id.other)
        };
        LinearLayout[] simpleColors = {
                findViewById(R.id.reds), findViewById(R.id.greens), findViewById(R.id.blues)
        };

        Button[] colorButtons = new Button[16];
        colorButtons[0] = findViewById(R.id.black);
        for (int i = 0; i < 5; i++) {
            colorButtons[1 + i] = (Button)simpleColors[0].getChildAt(i);
            colorButtons[6 + i] = (Button)simpleColors[1].getChildAt(i);
            colorButtons[11 + i] = (Button)simpleColors[2].getChildAt(i);
        }

        Switch bgOrText = findViewById(R.id.bg_text);
        View[] currentView = new View[1];
        ScrollView[] willBeAdd = {findViewById(R.id.forY), findViewById(R.id.forC), findViewById(R.id.forM), findViewById(R.id.forO)};
        boolean[] isLoaded = {false, false, false, false};

        for (int i = 0; i < 5; i++) {
            buttons[i].setOnClickListener(v -> {
                colorKind.setY(v.getY());
                setCurrent(currentView, v);
                setInvisibleSimples(simpleColors);
                setInvisibleScrollViews(willBeAdd, isLoaded);
                if (colorKind.getVisibility() == INVISIBLE) {
                    colorKind.setVisibility(VISIBLE);
                } else {
                    colorKind.setVisibility(INVISIBLE);
                }
            });
        }

        colorKindButtons[0].setOnClickListener(v -> {
            simpleColors[0].setY(currentView[0].getY() + colorKind.getHeight());
            if (simpleColors[0].getVisibility() == INVISIBLE) {
                setInvisibleSimples(simpleColors);
                setInvisibleScrollViews(willBeAdd, isLoaded);
                simpleColors[0].setVisibility(VISIBLE);
            } else {
                simpleColors[0].setVisibility(INVISIBLE);
            }
        });
        colorKindButtons[1].setOnClickListener(v -> {
            simpleColors[1].setY(currentView[0].getY() + colorKind.getHeight());
            if (simpleColors[1].getVisibility() == INVISIBLE) {
                setInvisibleSimples(simpleColors);
                setInvisibleScrollViews(willBeAdd, isLoaded);
                simpleColors[1].setVisibility(VISIBLE);
            } else {
                simpleColors[1].setVisibility(INVISIBLE);
            }
        });
        colorKindButtons[2].setOnClickListener(v -> {
            simpleColors[2].setY(currentView[0].getY() + colorKind.getHeight());
            if (simpleColors[2].getVisibility() == INVISIBLE) {
                setInvisibleSimples(simpleColors);
                setInvisibleScrollViews(willBeAdd, isLoaded);
                simpleColors[2].setVisibility(VISIBLE);
            } else {
                simpleColors[2].setVisibility(INVISIBLE);
            }
        });

        for (int i = 0; i < 16; i++) {
            colorButtons[i].setOnClickListener(v -> {
                if (bgOrText.isChecked()) {
                    ((Button)currentView[0]).setTextColor(v.getBackgroundTintList());
                } else {
                    currentView[0].setBackgroundTintList(v.getBackgroundTintList());
                }
            });
        }

        colorKindButtons[3].setOnClickListener(new View.OnClickListener() {
            boolean first = true;
            TableLayout yellows;
            @Override
            public void onClick(View v) {
                if (first) {
                    willBeAdd[0].setY(currentView[0].getY() + colorKind.getHeight());
                    LayoutInflater inflater = getLayoutInflater();
                    inflater.inflate(R.layout.colors_y, willBeAdd[0], true);
                    yellows = findViewById(R.id.yellows);
                    isLoaded[0] = true;
                    first = false;
                    willBeAdd[0].setVisibility(INVISIBLE);
                    Button[] btn_yellows = new Button[25];
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 5; j++) {
                            btn_yellows[i*5 + j] = (Button)((TableRow)yellows.getChildAt(i)).getChildAt(j);
                        }
                    }
                    for (int i = 0; i < 25; i++) {
                        btn_yellows[i].setOnClickListener(v1 -> {
                            if (bgOrText.isChecked()) {
                                ((Button)currentView[0]).setTextColor(v1.getBackgroundTintList());
                            } else {
                                currentView[0].setBackgroundTintList(v1.getBackgroundTintList());
                            }
                        });
                    }
                }
                willBeAdd[0].setY(currentView[0].getY() + colorKind.getHeight());
                if (willBeAdd[0].getVisibility() == INVISIBLE) {
                    setInvisibleSimples(simpleColors);
                    setInvisibleScrollViews(willBeAdd, isLoaded);
                    willBeAdd[0].setVisibility(VISIBLE);
                } else {
                    willBeAdd[0].setVisibility(INVISIBLE);
                }
            }
        });
        colorKindButtons[4].setOnClickListener(new View.OnClickListener() {
            boolean first = true;
            TableLayout cyans;
            @Override
            public void onClick(View v) {
                if (first) {
                    willBeAdd[1].setY(currentView[0].getY() + colorKind.getHeight());
                    LayoutInflater inflater = getLayoutInflater();
                    inflater.inflate(R.layout.colors_c, willBeAdd[1], true);
                    cyans = findViewById(R.id.cyans);
                    isLoaded[1] = true;
                    first = false;
                    willBeAdd[1].setVisibility(INVISIBLE);
                    Button[] btn_cyans = new Button[25];
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 5; j++) {
                            btn_cyans[i*5 + j] = (Button)((TableRow)cyans.getChildAt(i)).getChildAt(j);
                        }
                    }
                    for (int i = 0; i < 25; i++) {
                        btn_cyans[i].setOnClickListener(v12 -> {
                            if (bgOrText.isChecked()) {
                                ((Button)currentView[0]).setTextColor(v12.getBackgroundTintList());
                            } else {
                                currentView[0].setBackgroundTintList(v12.getBackgroundTintList());
                            }
                        });
                    }
                }
                willBeAdd[1].setY(currentView[0].getY() + colorKind.getHeight());
                if (willBeAdd[1].getVisibility() == INVISIBLE) {
                    setInvisibleSimples(simpleColors);
                    setInvisibleScrollViews(willBeAdd, isLoaded);
                    willBeAdd[1].setVisibility(VISIBLE);
                } else {
                    willBeAdd[1].setVisibility(INVISIBLE);
                }
            }
        });
        colorKindButtons[5].setOnClickListener(new View.OnClickListener() {
            boolean first = true;
            TableLayout magentas;
            @Override
            public void onClick(View v) {
                if (first) {
                    willBeAdd[2].setY(currentView[0].getY() + colorKind.getHeight());
                    LayoutInflater inflater = getLayoutInflater();
                    inflater.inflate(R.layout.colors_m, willBeAdd[2], true);
                    magentas = findViewById(R.id.magentas);
                    isLoaded[2] = true;
                    first = false;
                    willBeAdd[2].setVisibility(INVISIBLE);
                    Button[] btn_magentas = new Button[25];
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 5; j++) {
                            btn_magentas[i*5 + j] = (Button)((TableRow)magentas.getChildAt(i)).getChildAt(j);
                        }
                    }
                    for (int i = 0; i < 25; i++) {
                        btn_magentas[i].setOnClickListener(v13 -> {
                            if (bgOrText.isChecked()) {
                                ((Button)currentView[0]).setTextColor(v13.getBackgroundTintList());
                            } else {
                                currentView[0].setBackgroundTintList(v13.getBackgroundTintList());
                            }
                        });
                    }
                }
                willBeAdd[2].setY(currentView[0].getY() + colorKind.getHeight());
                if (willBeAdd[2].getVisibility() == INVISIBLE) {
                    setInvisibleSimples(simpleColors);
                    setInvisibleScrollViews(willBeAdd, isLoaded);
                    willBeAdd[2].setVisibility(VISIBLE);
                } else {
                    willBeAdd[2].setVisibility(INVISIBLE);
                }
            }
        });
        colorKindButtons[6].setOnClickListener(new View.OnClickListener() {
            boolean first = true;
            TableLayout others;
            @Override
            public void onClick(View v) {
                if (first) {
                    willBeAdd[3].setY(currentView[0].getY() + colorKind.getHeight());
                    LayoutInflater inflater = getLayoutInflater();
                    inflater.inflate(R.layout.colors_others, willBeAdd[3], true);
                    others = findViewById(R.id.others);
                    isLoaded[3] = true;
                    first = false;
                    willBeAdd[3].setVisibility(INVISIBLE);
                    Button[] btn_others = new Button[30];
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 10; j++) {
                            btn_others[i*10 + j] = (Button)((TableRow)others.getChildAt(i)).getChildAt(j);
                        }
                    }
                    for (int i = 0; i < 30; i++) {
                        btn_others[i].setOnClickListener(v14 -> {
                            if (bgOrText.isChecked()) {
                                ((Button)currentView[0]).setTextColor(v14.getBackgroundTintList());
                            } else {
                                currentView[0].setBackgroundTintList(v14.getBackgroundTintList());
                            }
                        });
                    }
                }
                willBeAdd[3].setY(currentView[0].getY() + colorKind.getHeight());
                if (willBeAdd[3].getVisibility() == INVISIBLE) {
                    setInvisibleSimples(simpleColors);
                    setInvisibleScrollViews(willBeAdd, isLoaded);
                    willBeAdd[3].setVisibility(VISIBLE);
                } else {
                    willBeAdd[3].setVisibility(INVISIBLE);
                }
            }
        });
    }

    private void setCurrent(View[] current, View view) {
        current[0] = view;
    }

    private void setInvisibleSimples(LinearLayout[] simples) {
        for (int i = 0; i < 4; i++) {
            if (i < 3) {
                simples[i].setVisibility(INVISIBLE);
            }
        }
    }

    private void setInvisibleScrollViews(ScrollView[] willBeAdd, boolean[] isLoaded) {
        for (int i = 0; i < 4; i++) {
            if (isLoaded[i]) {
                willBeAdd[i].setVisibility(INVISIBLE);
            }
        }
    }
}