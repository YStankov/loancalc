package com.nbu.loan;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TypeHelpActivity extends Activity  implements  View.OnClickListener{
    private Button closeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
    }

    public void onClick(View view) {
        if(view == closeButton){
            finish();
        }
    }
}
