package com.example.plantdiseasedetection

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.plantdiseasedetection.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentPhotoUri: Uri? = null
    private var isUsingUrl = false

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { handleImageSelection(it) }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) currentPhotoUri?.let { handleImageSelection(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.fabAction.setOnClickListener { showImageSourceDialog() }

        // Live URL Preview with complete Coil configuration
        binding.etUrl.doAfterTextChanged { text ->
            val url = text.toString()
            if (URLUtil.isValidUrl(url)) {
                isUsingUrl = true
                binding.imgPreview.load(url) {
                    crossfade(true)
                    placeholder(R.drawable.placeholder_leaf)
                    error(R.drawable.ic_error_outline)
                    transformations(RoundedCornersTransformation(32f))
                }
            }
        }

        binding.btnAnalyze.setOnClickListener {
            val urlText = binding.etUrl.text.toString()
            if (isUsingUrl && URLUtil.isValidUrl(urlText)) {
                predictFromUrl(urlText)
            } else if (currentPhotoUri != null) {
                uploadImageToServer(currentPhotoUri!!)
            } else {
                Toast.makeText(this, "Please select or link an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Plant Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }.show()
    }

    private fun launchCamera() {
        val photoFile = File(externalCacheDir, "temp_plant.jpg")
        val photoUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
        currentPhotoUri = photoUri
        takePicture.launch(photoUri)
    }

    // Complete Coil configuration for local file selection
    private fun handleImageSelection(uri: Uri) {
        isUsingUrl = false
        currentPhotoUri = uri
        binding.imgPreview.load(uri) {
            crossfade(true)
            placeholder(R.drawable.placeholder_leaf)
            error(R.drawable.ic_error_outline)
            transformations(RoundedCornersTransformation(32f))
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.resultLayout.loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.resultLayout.contentLayout.alpha = if (isLoading) 0.5f else 1.0f
        binding.btnAnalyze.isEnabled = !isLoading
        binding.fabAction.isEnabled = !isLoading
    }

    private fun uploadImageToServer(uri: Uri) {
        val file = File(FileUtils.getPath(this, uri))
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        toggleLoading(true)
        resetUIStyles()

        RetrofitClient.instance.uploadImage(body).enqueue(object : Callback<PredictionResponse> {
            override fun onResponse(call: Call<PredictionResponse>, response: Response<PredictionResponse>) {
                toggleLoading(false)
                if (response.isSuccessful) {
                    updateUI(response.body())
                } else {
                    handleErrorResponse(response.code())
                }
            }

            override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
                toggleLoading(false)
                showErrorState("Connection Failed: Check if Server is running at 192.168.1.7")
            }
        })
    }

    private fun predictFromUrl(url: String) {
        toggleLoading(true)
        resetUIStyles()

        RetrofitClient.instance.predictByUrl(url).enqueue(object : Callback<PredictionResponse> {
            override fun onResponse(call: Call<PredictionResponse>, response: Response<PredictionResponse>) {
                toggleLoading(false)
                if (response.isSuccessful) {
                    updateUI(response.body())
                } else {
                    handleErrorResponse(response.code())
                }
            }

            override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
                toggleLoading(false)
                showErrorState("Check Connection. Is the URL reachable by the server?")
            }
        })
    }

    private fun handleErrorResponse(code: Int) {
        val errorMsg = when(code) {
            404 -> "Endpoint not found (404). Check Server Routes."
            500 -> "Server Crash (500). Check Python Logs."
            else -> "Server Error: $code"
        }
        showErrorState(errorMsg)
    }

    private fun showErrorState(message: String) {
        binding.resultLayout.diseaseText.text = "Analysis Failed"
        binding.resultLayout.diseaseText.setTextColor(Color.RED)
        binding.resultLayout.treatmentText.text = message
        binding.resultLayout.confidenceChip.text = "!"
        binding.resultLayout.preventionText.text = "Check server logs for details."

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun resetUIStyles() {
        binding.resultLayout.diseaseText.setTextColor(Color.parseColor("#1B5E20"))
    }

    private fun updateUI(result: PredictionResponse?) {
        result?.let {
            binding.resultLayout.diseaseText.text = it.disease
            binding.resultLayout.confidenceChip.text = "${(it.confidence * 100).toInt()}%"
            binding.resultLayout.treatmentText.text = it.treatment
            binding.resultLayout.preventionText.text = it.prevention

            binding.mainScrollView.smoothScrollTo(0, binding.resultLayout.root.top)
        }
    }
}









//package com.example.plantdiseasedetection
//
//import android.graphics.Color
//import android.net.Uri
//import android.os.Bundle
//import android.view.View
//import android.webkit.URLUtil
//import android.widget.Toast
//import androidx.activity.result.PickVisualMediaRequest
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.FileProvider
//import androidx.core.widget.doAfterTextChanged
//import coil.load
//import coil.transform.RoundedCornersTransformation
//import com.example.plantdiseasedetection.databinding.ActivityMainBinding
//import com.google.android.material.dialog.MaterialAlertDialogBuilder
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.RequestBody.Companion.asRequestBody
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import java.io.File
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//    private var currentPhotoUri: Uri? = null
//    private var isUsingUrl = false
//
//    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//        uri?.let { handleImageSelection(it) }
//    }
//
//    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
//        if (success) currentPhotoUri?.let { handleImageSelection(it) }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        setupClickListeners()
//    }
//
//    private fun setupClickListeners() {
//        binding.fabAction.setOnClickListener { showImageSourceDialog() }
//
//        binding.etUrl.doAfterTextChanged { text ->
//            val url = text.toString()
//            if (URLUtil.isValidUrl(url)) {
//                isUsingUrl = true
//                binding.imgPreview.load(url) {
//                    crossfade(true)
//                    transformations(RoundedCornersTransformation(28f))
//                }
//            }
//        }
//
//        binding.btnAnalyze.setOnClickListener {
//            val urlText = binding.etUrl.text.toString()
//            if (isUsingUrl && URLUtil.isValidUrl(urlText)) {
//                predictFromUrl(urlText)
//            } else if (currentPhotoUri != null) {
//                uploadImageToServer(currentPhotoUri!!)
//            } else {
//                Toast.makeText(this, "Please select or link an image first", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun showImageSourceDialog() {
//        val options = arrayOf("Take Photo", "Choose from Gallery")
//        MaterialAlertDialogBuilder(this)
//            .setTitle("Select Plant Image")
//            .setItems(options) { _, which ->
//                when (which) {
//                    0 -> launchCamera()
//                    1 -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//                }
//            }.show()
//    }
//
//    private fun launchCamera() {
//        val photoFile = File(externalCacheDir, "temp_plant.jpg")
//        val photoUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
//        currentPhotoUri = photoUri
//        takePicture.launch(photoUri)
//    }
//
//    private fun handleImageSelection(uri: Uri) {
//        isUsingUrl = false
//        currentPhotoUri = uri
//        binding.imgPreview.load(uri) {
//            crossfade(true)
//            transformations(RoundedCornersTransformation(28f))
//        }
//        // Optional: Trigger analysis immediately upon selection
//        // uploadImageToServer(uri)
//    }
//
//    private fun toggleLoading(isLoading: Boolean) {
//        binding.resultLayout.loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
//        binding.resultLayout.contentLayout.alpha = if (isLoading) 0.5f else 1.0f
//        binding.btnAnalyze.isEnabled = !isLoading
//        binding.fabAction.isEnabled = !isLoading
//    }
//
//    private fun uploadImageToServer(uri: Uri) {
//        val file = File(FileUtils.getPath(this, uri))
//        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
//        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
//
//        toggleLoading(true)
//        resetUIStyles()
//
//        RetrofitClient.instance.uploadImage(body).enqueue(object : Callback<PredictionResponse> {
//            override fun onResponse(call: Call<PredictionResponse>, response: Response<PredictionResponse>) {
//                toggleLoading(false)
//                if (response.isSuccessful) {
//                    updateUI(response.body())
//                } else {
//                    handleErrorResponse(response.code())
//                }
//            }
//
//            override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
//                toggleLoading(false)
//                showErrorState("Connection Failed: Check if Server is running at 192.168.1.7")
//            }
//        })
//    }
//
//    private fun predictFromUrl(url: String) {
//        toggleLoading(true)
//        resetUIStyles()
//
//        RetrofitClient.instance.predictByUrl(url).enqueue(object : Callback<PredictionResponse> {
//            override fun onResponse(call: Call<PredictionResponse>, response: Response<PredictionResponse>) {
//                toggleLoading(false)
//                if (response.isSuccessful) {
//                    updateUI(response.body())
//                } else {
//                    handleErrorResponse(response.code())
//                }
//            }
//
//            override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
//                toggleLoading(false)
//                showErrorState("Check Connection. Is the URL reachable by the server?")
//            }
//        })
//    }
//
//    private fun handleErrorResponse(code: Int) {
//        val errorMsg = when(code) {
//            404 -> "Endpoint not found (404). Check Server Routes."
//            500 -> "Server Crash (500). Check Python Logs."
//            else -> "Server Error: $code"
//        }
//        showErrorState(errorMsg)
//    }
//
//    private fun showErrorState(message: String) {
//        binding.resultLayout.diseaseText.text = "Analysis Failed"
//        binding.resultLayout.diseaseText.setTextColor(Color.RED)
//        binding.resultLayout.treatmentText.text = message
//        binding.resultLayout.confidenceChip.text = "!"
//        binding.resultLayout.preventionText.text = "Check server logs for details."
//
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
//    }
//
//    private fun resetUIStyles() {
//        binding.resultLayout.diseaseText.setTextColor(Color.parseColor("#1B5E20"))
//    }
//
//    private fun updateUI(result: PredictionResponse?) {
//        result?.let {
//            binding.resultLayout.diseaseText.text = it.disease
//            binding.resultLayout.confidenceChip.text = "${(it.confidence * 100).toInt()}%"
//            binding.resultLayout.treatmentText.text = it.treatment
//            binding.resultLayout.preventionText.text = it.prevention
//
//            binding.mainScrollView.smoothScrollTo(0, binding.resultLayout.root.top)
//        }
//    }
//}
