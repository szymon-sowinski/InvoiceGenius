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
import com.google.gson.Gson
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStreamReader

class FileFragment : Fragment() {

    private var _binding: FragmentFileBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
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
        val fileViewModel =
            ViewModelProvider(this).get(FileViewModel::class.java)

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

        return root
    }

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
                binding.tvFilePath.text = fileName
                readFileContent(uri, fileName)
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
            .setTitle("Invalid File")
            .setMessage("Please select a file with .xml or .json extension.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun readFileContent(uri: Uri, fileName: String) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        inputStream?.use {
            when {
                fileName.endsWith(".json", true) -> {
                    val reader = InputStreamReader(it)
                    val data = Gson().fromJson(reader, Map::class.java)
                    displayData(data)
                }
                fileName.endsWith(".xml", true) -> {
                    val factory = XmlPullParserFactory.newInstance()
                    val parser = factory.newPullParser()
                    parser.setInput(it, null)
                    val data = parseXml(parser)
                    displayData(data)
                }
            }
        }
    }

    private fun parseXml(parser: XmlPullParser): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        var eventType = parser.eventType
        var currentTag: String? = null
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                }
                XmlPullParser.TEXT -> {
                    currentTag?.let {
                        result[it] = parser.text
                    }
                }
                XmlPullParser.END_TAG -> {
                    currentTag = null
                }
            }
            eventType = parser.next()
        }
        return result
    }

    private fun displayData(data: Map<*, *>) {
        val dataString = data.entries.joinToString(separator = "\n") { "${it.key}: ${it.value}" }
        AlertDialog.Builder(requireContext())
            .setTitle("File Content")
            .setMessage(dataString)
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
