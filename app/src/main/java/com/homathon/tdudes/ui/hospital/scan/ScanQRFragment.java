package com.homathon.tdudes.ui.hospital.scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homathon.tdudes.R;
import com.homathon.tdudes.databinding.FragmentScanQRBinding;
import com.homathon.tdudes.utills.LiveBarcodeScanningActivity;

import static androidx.core.content.ContextCompat.checkSelfPermission;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanQRFragment extends Fragment {
    private FragmentScanQRBinding binding;

    public ScanQRFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentScanQRBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.scanQR.setOnClickListener(v -> {
            if(checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                startActivityForResult(new Intent(requireContext() , LiveBarcodeScanningActivity.class) , 100);
            else
                requireActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        });
    }
}
