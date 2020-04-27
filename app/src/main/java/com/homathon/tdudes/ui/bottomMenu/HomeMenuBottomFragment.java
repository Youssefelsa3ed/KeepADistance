package com.homathon.tdudes.ui.bottomMenu;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.homathon.tdudes.R;

public class HomeMenuBottomFragment extends BottomSheetDialogFragment {

    public static HomeMenuBottomFragment getInstance(){
        return new HomeMenuBottomFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.bottomSheetDialogTheme);
    }
}
