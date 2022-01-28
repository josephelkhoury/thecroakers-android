package com.thecroakers.app.MainMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thecroakers.app.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.thecroakers.app.R;

public class BlankFragment extends RootFragment {

    public BlankFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText("");
        return textView;
    }

}
