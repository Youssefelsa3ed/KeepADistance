package com.homathon.tdudes.ui.hospital;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.homathon.tdudes.R;
import com.homathon.tdudes.databinding.FragmentHospitalMenuBinding;
import com.homathon.tdudes.ui.splash.SplashActivity;
import com.homathon.tdudes.utills.LiveBarcodeScanningActivity;
import com.homathon.tdudes.utills.ParentClass;
import com.homathon.tdudes.utills.SharedPrefManager;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.checkSelfPermission;

/**
 * A simple {@link Fragment} subclass.
 */
public class HospitalMenuFragment extends Fragment {
    private FragmentHospitalMenuBinding binding;
    private NavController navController;
    private boolean checked;

    public HospitalMenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHospitalMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        binding.scanQRLayout.setOnClickListener(v -> {
            if(checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                startActivityForResult(new Intent(requireContext() , LiveBarcodeScanningActivity.class) , 100);
            else
                requireActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        });
        binding.changeLanguageLayout.setOnClickListener(v -> openChangeLanguageDialog(view.getContext()));
        binding.logoutLayout.setOnClickListener(v -> logoutUser());
    }

    private void openChangeLanguageDialog(Context context){
        Dialog changeLanguageDialog = new Dialog(context);
        changeLanguageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        changeLanguageDialog.setContentView(R.layout.dialog_change_language);
        Objects.requireNonNull(changeLanguageDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        Objects.requireNonNull(changeLanguageDialog.getWindow()).setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        Button btnChange = changeLanguageDialog.findViewById(R.id.btnChange);
        RelativeLayout linearEnglish = changeLanguageDialog.findViewById(R.id.linearEnglish);
        RelativeLayout linearArabic = changeLanguageDialog.findViewById(R.id.linearArabic);
        ImageView linearEnglishSelected = changeLanguageDialog.findViewById(R.id.linearEnglishSelected);
        ImageView linearArabicSelected = changeLanguageDialog.findViewById(R.id.linearArabicSelected);
        if (ParentClass.getLocalization(context).equals("ar")) {
            checked = true;
            linearEnglishSelected.setVisibility(View.GONE);
            linearArabicSelected.setVisibility(View.VISIBLE);
        } else {
            checked = false;
            linearEnglishSelected.setVisibility(View.VISIBLE);
            linearArabicSelected.setVisibility(View.GONE);
        }

        linearArabic.setOnClickListener(view -> {
            checked = true;
            linearEnglishSelected.setVisibility(View.GONE);
            linearArabicSelected.setVisibility(View.VISIBLE);
        });

        linearEnglish.setOnClickListener(view -> {
            checked = false;
            linearEnglishSelected.setVisibility(View.VISIBLE);
            linearArabicSelected.setVisibility(View.GONE);
        });

        btnChange.setOnClickListener(v-> {
            if (checked && ParentClass.getLocalization(context).equals("en")) {
                changeLanguage("ar");
            }
            else
                changeLanguageDialog.dismiss();
            if (!checked && ParentClass.getLocalization(context).equals("ar"))
                changeLanguage("en");
            else
                changeLanguageDialog.dismiss();
        });
        changeLanguageDialog.show();
    }

    private void changeLanguage(String language) {
        SharedPreferences prefs = getContext().getSharedPreferences(getActivity().getPackageName(), MODE_PRIVATE);
        prefs.edit().putString("language", language).apply();
        startActivity(new Intent(getContext(), SplashActivity.class));
        getActivity().finishAffinity();
    }

    private void logoutUser() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(getContext());
        sharedPrefManager.logout();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getContext(), SplashActivity.class));
        getActivity().finish();
    }
}
