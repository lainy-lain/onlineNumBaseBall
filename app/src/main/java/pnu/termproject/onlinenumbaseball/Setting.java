package pnu.termproject.onlinenumbaseball;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.firebase.auth.FirebaseAuth;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class Setting extends AppCompatActivity {
    private View currentView;
    private ScrollView[] willBeAdd = new ScrollView[4];
    private boolean[] isLoaded = {false, false, false, false};
    private LinearLayout colorKind;
    private Switch bgOrText;
    private LinearLayout[] simpleColors = new LinearLayout[3];
    Button set_bgbtn;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        bgOrText = findViewById(R.id.bg_text);
        Button[] buttons = {
                findViewById(R.id.play_setbtn), findViewById(R.id.single_setbtn), findViewById(R.id.multi_setbtn)
                , findViewById(R.id.rank_setbtn), findViewById(R.id.set_setbtn), findViewById(R.id.set_bgbtn)
        };
        set_bgbtn = buttons[5];
        colorKind = findViewById(R.id.colorKind);
        Button[] colorKindButtons = {
                findViewById(R.id.red), findViewById(R.id.green), findViewById(R.id.blue)
                , findViewById(R.id.yellow), findViewById(R.id.cyan), findViewById(R.id.magenta)
                , findViewById(R.id.other)
        };
        simpleColors[0] = findViewById(R.id.reds);
        simpleColors[1] = findViewById(R.id.greens);
        simpleColors[2] = findViewById(R.id.blues);
        Button[] colorButtons = new Button[16];
        colorButtons[0] = findViewById(R.id.black);
        for (int i = 0; i < 5; i++) {
            colorButtons[1 + i] = (Button)simpleColors[0].getChildAt(i);
            colorButtons[6 + i] = (Button)simpleColors[1].getChildAt(i);
            colorButtons[11 + i] = (Button)simpleColors[2].getChildAt(i);
        }
        willBeAdd[0] = findViewById(R.id.forY);
        willBeAdd[1] = findViewById(R.id.forC);
        willBeAdd[2] = findViewById(R.id.forM);
        willBeAdd[3] = findViewById(R.id.forO);
        Button[] set_btns = {findViewById(R.id.confirm), findViewById(R.id.initialize)};
        RadioButton[] radiuses = {findViewById(R.id.basic), findViewById(R.id.round), findViewById(R.id.rround)};

        SharedPreferences sp = getSharedPreferences("setting", MODE_PRIVATE);
        ColorStateList[] colors = {ColorStateList.valueOf(sp.getInt("btn1bg", 0xFFFFEB3B)),
                ColorStateList.valueOf(sp.getInt("btn2bg", 0xFFCDDC39)),
                ColorStateList.valueOf(sp.getInt("btn3bg", 0xFF8BC34A)),
                ColorStateList.valueOf(sp.getInt("btn4bg", 0xFF00BCD4)),
                ColorStateList.valueOf(sp.getInt("btn5bg", 0xFF03A9F4)),
                ColorStateList.valueOf(sp.getInt("btnbgbg", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn1tx", 0xFF000000)),
                ColorStateList.valueOf(sp.getInt("btn2tx", 0xFF000000)),
                ColorStateList.valueOf(sp.getInt("btn3tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn4tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btn5tx", 0xFFFFFFFF)),
                ColorStateList.valueOf(sp.getInt("btnbgtx", 0xFF000000))
        };
        for (int i = 0; i < 6; i++) {
            buttons[i].setBackgroundTintList(colors[i]);
            buttons[i].setTextColor(colors[i + 6]);
        }
        buttons[5].getRootView().setBackgroundTintList(colors[5]);
        bgOrText.setTextColor(colors[11]);
        radiuses[0].setTextColor(colors[11]);
        radiuses[1].setTextColor(colors[11]);
        radiuses[2].setTextColor(colors[11]);
        int radiusChecked = sp.getInt("radius", 0);
        int radius = (radiusChecked + 1) * 8;
        for (int i = 0; i < 7; i++) {
            if (i < 2) {
                ((MaterialButton)set_btns[i]).setCornerRadius(radius);
            }
            if (i < 6) {
                ((MaterialButton)buttons[i]).setCornerRadius(radius);
            }
            ((MaterialButton)colorKindButtons[i]).setCornerRadius(radius);
        }
        radiuses[radiusChecked].setChecked(true);

        for (int i = 0; i < 6; i++) {
            buttons[i].setOnClickListener(v -> {
                colorKind.setY(v.getY());
                setCurrent(v);
                setInvisibleSimples(simpleColors);
                setInvisibleScrollViews(willBeAdd, isLoaded);
                if (colorKind.getVisibility() == INVISIBLE) {
                    colorKind.setVisibility(VISIBLE);
                } else {
                    colorKind.setVisibility(INVISIBLE);
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            setOnClickSimple(colorKindButtons, i);
        }

        for (int i = 0; i < 16; i++) {
            setOnClickColorBtn(colorButtons[i]);
        }

        setOnClickComplex(colorKindButtons[3], 0, R.layout.colors_y, R.id.yellows);
        setOnClickComplex(colorKindButtons[4], 1, R.layout.colors_c, R.id.cyans);
        setOnClickComplex(colorKindButtons[5], 2, R.layout.colors_m, R.id.magentas);
        colorKindButtons[6].setOnClickListener(new View.OnClickListener() {
            boolean first = true;
            TableLayout others;
            @Override
            public void onClick(View v) {
                if (first) {
                    willBeAdd[3].setY(currentView.getY() + colorKind.getHeight());
                    LayoutInflater inflater = getLayoutInflater();
                    inflater.inflate(R.layout.colors_others, willBeAdd[3], true);
                    others = findViewById(R.id.others);
                    isLoaded[3] = true;
                    first = false;
                    willBeAdd[3].setVisibility(INVISIBLE);
                    Button[] btn_others = new Button[125];
                    for (int i = 0; i < 12; i++) {
                        for (int j = 0; j < 10; j++) {
                            btn_others[i*10 + j] = (Button)((TableRow)others.getChildAt(i)).getChildAt(j);
                        }
                    }
                    for (int i = 0; i < 5; i++) {
                        btn_others[120 + i] = (Button)((TableRow)others.getChildAt(12)).getChildAt(i);
                    }
                    for (int i = 0; i < 125; i++) {
                        setOnClickColorBtn(btn_others[i]);
                    }
                }
                willBeAdd[3].setY(currentView.getY() + colorKind.getHeight());
                if (willBeAdd[3].getVisibility() == INVISIBLE) {
                    setInvisibleSimples(simpleColors);
                    setInvisibleScrollViews(willBeAdd, isLoaded);
                    willBeAdd[3].setVisibility(VISIBLE);
                } else {
                    willBeAdd[3].setVisibility(INVISIBLE);
                }
            }
        });

        SharedPreferences.Editor spedit = sp.edit();
        ((RadioGroup)findViewById(R.id.radius_select)).setOnCheckedChangeListener(((group, checkedId) -> {
            int i;
            int cornerRadius;
            if (checkedId == R.id.basic) {
                cornerRadius = 8;
            }
            else if (checkedId == R.id.round) {
                cornerRadius = 16;
            }
            else {
                cornerRadius = 24;
            }
            for (i = 0; i < 7; i++) {
                if (i < 2) {
                    ((MaterialButton)set_btns[i]).setCornerRadius(cornerRadius);
                }
                if (i < 6) {
                    ((MaterialButton)buttons[i]).setCornerRadius(cornerRadius);
                }
                ((MaterialButton)colorKindButtons[i]).setCornerRadius(cornerRadius);
            }
        }));

        set_btns[0].setOnClickListener(v -> {
            spedit.putInt("btn1bg", buttons[0].getBackgroundTintList().getDefaultColor());
            spedit.putInt("btn1tx", buttons[0].getTextColors().getDefaultColor());
            spedit.putInt("btn2bg", buttons[1].getBackgroundTintList().getDefaultColor());
            spedit.putInt("btn2tx", buttons[1].getTextColors().getDefaultColor());
            spedit.putInt("btn3bg", buttons[2].getBackgroundTintList().getDefaultColor());
            spedit.putInt("btn3tx", buttons[2].getTextColors().getDefaultColor());
            spedit.putInt("btn4bg", buttons[3].getBackgroundTintList().getDefaultColor());
            spedit.putInt("btn4tx", buttons[3].getTextColors().getDefaultColor());
            spedit.putInt("btn5bg", buttons[4].getBackgroundTintList().getDefaultColor());
            spedit.putInt("btn5tx", buttons[4].getTextColors().getDefaultColor());
            spedit.putInt("btnbgbg", buttons[5].getBackgroundTintList().getDefaultColor());
            spedit.putInt("btnbgtx", buttons[5].getTextColors().getDefaultColor());
            int i;
            for (i = 0; i < 3; i++) {
                if (radiuses[i].isChecked()) {
                    break;
                }
            }
            spedit.putInt("radius", i);
            spedit.apply();

            Intent intent = new Intent();
            intent.putExtra("btn1bg", buttons[0].getBackgroundTintList().getDefaultColor());
            intent.putExtra("btn1tx", buttons[0].getTextColors().getDefaultColor());
            intent.putExtra("btn2bg", buttons[1].getBackgroundTintList().getDefaultColor());
            intent.putExtra("btn2tx", buttons[1].getTextColors().getDefaultColor());
            intent.putExtra("btn3bg", buttons[2].getBackgroundTintList().getDefaultColor());
            intent.putExtra("btn3tx", buttons[2].getTextColors().getDefaultColor());
            intent.putExtra("btn4bg", buttons[3].getBackgroundTintList().getDefaultColor());
            intent.putExtra("btn4tx", buttons[3].getTextColors().getDefaultColor());
            intent.putExtra("btn5bg", buttons[4].getBackgroundTintList().getDefaultColor());
            intent.putExtra("btn5tx", buttons[4].getTextColors().getDefaultColor());
            intent.putExtra("btnbgbg", buttons[5].getBackgroundTintList().getDefaultColor());
            intent.putExtra("btnbgtx", buttons[5].getTextColors().getDefaultColor());
            intent.putExtra("radius", i);
            setResult(RESULT_OK, intent);
        });

        set_btns[1].setOnClickListener(v -> {
            spedit.clear();
            spedit.apply();
            buttons[0].setBackgroundTintList(ColorStateList.valueOf(0xFFFFEB3B));
            buttons[1].setBackgroundTintList(ColorStateList.valueOf(0xFFCDDC39));
            buttons[2].setBackgroundTintList(ColorStateList.valueOf(0xFF8BC34A));
            buttons[3].setBackgroundTintList(ColorStateList.valueOf(0xFF00BCD4));
            buttons[4].setBackgroundTintList(ColorStateList.valueOf(0xFF03A9F4));
            buttons[5].setBackgroundTintList(ColorStateList.valueOf(0xFFFFFFFF));
            buttons[0].setTextColor(ColorStateList.valueOf(0xFF000000));
            buttons[1].setTextColor(ColorStateList.valueOf(0xFF000000));
            buttons[2].setTextColor(ColorStateList.valueOf(0xFFFFFFFF));
            buttons[3].setTextColor(ColorStateList.valueOf(0xFFFFFFFF));
            buttons[4].setTextColor(ColorStateList.valueOf(0xFFFFFFFF));
            buttons[5].setTextColor(ColorStateList.valueOf(0xFF000000));
            buttons[5].getRootView().setBackgroundTintList(ColorStateList.valueOf(0xFFFFFFFF));
            bgOrText.setTextColor(ColorStateList.valueOf(0xFF000000));
            radiuses[0].setTextColor(ColorStateList.valueOf(0xFF000000));
            radiuses[1].setTextColor(ColorStateList.valueOf(0xFF000000));
            radiuses[2].setTextColor(ColorStateList.valueOf(0xFF000000));
            radiuses[0].setChecked(true);

            Intent intent = new Intent();
            intent.putExtra("btn1bg", 0xFFFFEB3B);
            intent.putExtra("btn1tx", 0xFF000000);
            intent.putExtra("btn2bg", 0xFFCDDC39);
            intent.putExtra("btn2tx", 0xFF000000);
            intent.putExtra("btn3bg", 0xFF8BC34A);
            intent.putExtra("btn3tx", 0xFFFFFFFF);
            intent.putExtra("btn4bg", 0xFF00BCD4);
            intent.putExtra("btn4tx", 0xFFFFFFFF);
            intent.putExtra("btn5bg", 0xFF03A9F4);
            intent.putExtra("btn5tx", 0xFFFFFFFF);
            intent.putExtra("btnbgbg", 0xFFFFFFFF);
            intent.putExtra("btnbgtx", 0xFF000000);
            intent.putExtra("radius", 0);
            setResult(RESULT_OK, intent);
        });
    }

    private void setCurrent(View view) {
        currentView = view;
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

    private void setOnClickSimple(Button[] buttons, int index) {
        buttons[index].setOnClickListener(v -> {
            simpleColors[index].setY(currentView.getY() + colorKind.getHeight());
            if (simpleColors[index].getVisibility() == INVISIBLE) {
                setInvisibleSimples(simpleColors);
                setInvisibleScrollViews(willBeAdd, isLoaded);
                simpleColors[index].setVisibility(VISIBLE);
            } else {
                simpleColors[index].setVisibility(INVISIBLE);
            }
        });
    }

    private void setOnClickComplex(Button button, int index, int layout, int id) {
        button.setOnClickListener(new View.OnClickListener() {
            boolean first = true;
            TableLayout colors;
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (first) {
                    willBeAdd[index].setY(currentView.getY() + colorKind.getHeight());
                    LayoutInflater inflater = getLayoutInflater();
                    inflater.inflate(layout, willBeAdd[index], true);
                    colors = findViewById(id);
                    isLoaded[index] = true;
                    first = false;
                    willBeAdd[index].setVisibility(INVISIBLE);
                    Button[] btn_colors = new Button[25];
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 5; j++) {
                            btn_colors[i*5 + j] = (Button)((TableRow)colors.getChildAt(i)).getChildAt(j);
                        }
                    }
                    for (int i = 0; i < 25; i++) {
                        setOnClickColorBtn(btn_colors[i]);
                    }
                }
                willBeAdd[index].setY(currentView.getY() + colorKind.getHeight());
                if (willBeAdd[index].getVisibility() == INVISIBLE) {
                    setInvisibleSimples(simpleColors);
                    setInvisibleScrollViews(willBeAdd, isLoaded);
                    willBeAdd[index].setVisibility(VISIBLE);
                } else {
                    willBeAdd[index].setVisibility(INVISIBLE);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setOnClickColorBtn(Button button) {
        button.setOnClickListener(v -> {
            if (bgOrText.isChecked()) {
                ((Button)currentView).setTextColor(v.getBackgroundTintList());
            } else {
                currentView.setBackgroundTintList(v.getBackgroundTintList());
            }
            if (currentView == set_bgbtn) {
                if (bgOrText.isChecked()) {
                    bgOrText.setTextColor(v.getBackgroundTintList());
                    ((RadioButton)findViewById(R.id.basic)).setTextColor(v.getBackgroundTintList());
                    ((RadioButton)findViewById(R.id.round)).setTextColor(v.getBackgroundTintList());
                    ((RadioButton)findViewById(R.id.rround)).setTextColor(v.getBackgroundTintList());
                } else {
                    (currentView.getRootView()).setBackgroundTintList(v.getBackgroundTintList());
                }
            }
        });
    }
}