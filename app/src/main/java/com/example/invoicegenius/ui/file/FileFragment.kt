package com.example.invoicegenius.ui.file

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.invoicegenius.databinding.FragmentFileBinding

class FileFragment : Fragment() {

    private var _binding: FragmentFileBinding? = null
    private val binding get() = _binding!!

    private val requestFilePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                chooseFile()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val chooseFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    validateAndDisplayFilePath(uri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnChooseFile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestFilePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                chooseFile()
            }
        }

        binding.generateButton.setOnClickListener {
            val filePath = binding.tvFilePath.text.toString()
            if (filePath.isNotEmpty() && fileUri != null) {
                val intent = Intent(requireContext(), GeneratePdfActivity::class.java).apply {
                    putExtra("file_uri", fileUri)
                    putExtra("file_name", filePath)
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Proszę załączyć plik przed wygenerowaniem PDF.", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private var fileUri: Uri? = null

    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
        }
        chooseFileLauncher.launch(intent)
    }

    private fun validateAndDisplayFilePath(uri: Uri) {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            val fileName = it.getString(nameIndex)
            if (isValidFile(fileName)) {
                fileUri = uri
                binding.tvFilePath.text = fileName
            } else {
                showInvalidFileDialog()
            }
        }
    }

    private fun isValidFile(fileName: String): Boolean {
        val lowerCaseFileName = fileName.toLowerCase()
        return lowerCaseFileName.endsWith(".xml") || lowerCaseFileName.endsWith(".json")
    }

    private fun showInvalidFileDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Nieobsługiwany plik")
            .setMessage("Proszę załączyć plik z rozszerzeniem .xml lub .json.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
