package com.plugable.mcommerceapp.cpmvp1.registration

import ServiceGenerator
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.RegisterRequest
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.RegisterWithData
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.RegisterWithImage
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.SendOTPResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.ui.activities.DashboardActivity
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.*
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.cpmvp1.utils.validation.EditTextValidations.MAX_NAME_LENGTH
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isEmpty
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isValidEmail
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isValidMobileNumber
import com.plugable.mcommerceapp.cpmvp1.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class RegisterActivity : AppCompatActivity(), View.OnClickListener {


    private var myImagePath: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        initializeTheme()
    }

    private fun initializeTheme() {
        val configDetail = SharedPreferences.getInstance(this).themeDataPreference
        ApplicationThemeUtils.APP_NAME = configDetail!!.appName
        ApplicationThemeUtils.PRIMARY_COLOR = configDetail.primaryColor
        ApplicationThemeUtils.SECONDARY_COLOR = configDetail.secondryColor
        ApplicationThemeUtils.STATUS_BAR_COLOR = configDetail.statusBarColor
        ApplicationThemeUtils.TEXT_COLOR = configDetail.textColor
        ApplicationThemeUtils.CURRENCY_SYMBOL = configDetail.currencySymbol
        ApplicationThemeUtils.TOOL_BAR_COLOR = configDetail.tertiaryColor
        setThemeToComponents()
    }

    override fun onBackPressed() {

    }

    private fun setThemeToComponents() {
        setStatusBarColor()
        buttonRegister.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        textViewRegisterLogin.setTextColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        viewLogin.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
        progressBarRegister.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
    }

    private fun initializeViews() {
        textChangeListeners()
        imageViewLogo.setOnClickListener { checkPermissions(this) }
        buttonRegister.setOnClickListener(this)
        textViewRegisterLogin.setOnClickListener(this)
        textViewSkip.setOnClickListener(this)
    }

    private fun checkPermissions(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 29
                )
            }
        } else {
//            dispatchTakePictureIntent()
//            startActivityForResult(onlyCameraGalChooser(), requestImageCapture)
            startActivityForResult(cameraAndGalleryIntentChooser(), requestImageCapture)
        }
    }


    private val requestImageCapture: Int = 1

    private fun cameraAndGalleryIntentChooser(): Intent? {

        // Determine Uri of camera image to save.

        val allIntents = ArrayList<Intent>()
        val packageManager = packageManager

        // collect all camera intents
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val listCam = packageManager.queryIntentActivities(captureIntent, 0)
        for (res in listCam) {
            val intent = Intent(captureIntent)
            intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intent.`package` = res.activityInfo.packageName
            allIntents.add(intent)
        }

        // collect all gallery intents
        val galleryIntent =
            Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        galleryIntent.type = ("image/*")

        val listGallery = packageManager.queryIntentActivities(galleryIntent, 0)
        for (res in listGallery) {
            val intent = Intent(galleryIntent)
            intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intent.`package` = res.activityInfo.packageName
            allIntents.add(intent)
        }
        // the main intent is the last in the list (android) so pickup the useless one
        var mainIntent = allIntents[allIntents.size - 1]
        for (intent in allIntents) {
            if (intent.component!!.className == "com.android.documentsui.DocumentsActivity") {
                mainIntent = intent
                break
            }
        }
        allIntents.remove(mainIntent)

        // Create a chooser from the main intent
        val chooserIntent =
            Intent.createChooser(mainIntent, getString(R.string.title_select_source))

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray<Parcelable>())

        return chooserIntent
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestImageCapture && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            val imageData = data?.data
            if (extras != null) {       //From camera
                val imageBitmap = extras.get("data") as Bitmap
//                imageViewProfile.setImageBitmap(imageBitmap)
                Glide.with(this).load(imageBitmap)
                    .apply(
                        RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.kmm_profile)
                    )
                    .into(imageViewLogo)
              /*  imageViewCamera.hide()
                textViewAddPhoto.hide()*/
                progressBarRegister.show()
                callRegisterApiWithImage(getImageUri(this, imageBitmap))
            } else if (imageData != null) { // From Gallery
//                imageViewProfile.setImageURI(imageData)
                Glide.with(this).load(imageData)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.kmm_profile))
                    .into(imageViewLogo)
            /*    imageViewCamera.hide()
                textViewAddPhoto.hide()*/
                progressBarRegister.show()
                callRegisterApiWithImage(imageData)
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun callRegisterApiWithImage(imageUri: Uri) {
        val file = File(getRealPathFromUri(this, imageUri))
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call = clientInstance.registerApiWithImage(body)
        call.enqueue(object : Callback<RegisterWithImage> {
            override fun onFailure(call: Call<RegisterWithImage>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<RegisterWithImage>,
                response: Response<RegisterWithImage>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.statusCode.equals("10")) {
                        progressBarRegister.hide()
                        val image = response.body()?.data

                        myImagePath = image!!


                    }

                }
            }


        })

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 29) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                //App has location permission, now create location request to check location settings
//                startActivityForResult(onlyCameraGalChooser(), requestImageCapture)
                startActivityForResult(cameraAndGalleryIntentChooser(), requestImageCapture)
//                dispatchTakePictureIntent()
            } else toast(getString(R.string.message_grant_permission))
        }
    }

    private fun getRealPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {

        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.textViewSkip -> {

                textInputEditTextName.text?.clear()
                textInputEditTextEmailId.text?.clear()
                textInputEditTextPhoneNumber.text?.clear()
                textInputEditTextPassword.text?.clear()

                textViewNameError.invisible()
                textViewEmailIdError.invisible()
                textViewMobileNoError.invisible()
                textViewPasswordError.invisible()

                startActivity<DashboardActivity>()
                ActivityCompat.finishAffinity(this)
                SharedPreferences.getInstance(this).isUserSkippedLogin = true
            }

            R.id.textViewRegisterLogin -> {
                textInputEditTextName.text?.clear()
                textInputEditTextEmailId.text?.clear()
                textInputEditTextPhoneNumber.text?.clear()
                textInputEditTextPassword.text?.clear()

                textViewNameError.invisible()
                textViewEmailIdError.invisible()
                textViewMobileNoError.invisible()
                textViewPasswordError.invisible()

                startActivity<LoginActivity>()
                ActivityCompat.finishAffinity(this)
            }

            R.id.buttonRegister -> {
                hideKeyboard(v)
                passwordValidation()
                contactValidation()
                emailValidation()
                nameValidation()

                if (nameValidation() && emailValidation() && contactValidation() && passwordValidation()) {
                    buttonRegister.isClickable = false
                    attemptApiCall()
                }
            }
        }
    }

    override fun onResume() {
        buttonRegister.isClickable = true
        progressBarRegister.hide()
        super.onResume()

    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            progressBarRegister.show()
            callRegisterApiWithData()
        } else {
            toast(getString(R.string.oops_no_internet_connection))
        }
    }


    private fun callRegisterApiWithData() {
        val userInfo = RegisterRequest(
            textInputEditTextEmailId.text.toString(),
            textInputEditTextPassword.text.toString(),
            textInputEditTextPhoneNumber.text.toString(),
            textInputEditTextName.text.toString(),
            myImagePath
        )
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!

        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)

        val call = clientInstance.registerApiWithData(userInfo)

        call.enqueue(object : Callback<RegisterWithData> {
            override fun onFailure(call: Call<RegisterWithData>, t: Throwable) {
                progressBarRegister.hide()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<RegisterWithData>,
                response: Response<RegisterWithData>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.statusCode.equals("10")) {
                        toast(response.body()!!.message)
                        if (isNetworkAccessible()) {
                            val applicationUserId = response.body()!!.data.applicationUserId
                            SharedPreferences.getInstance(this@RegisterActivity).setStringValue(
                                IntentFlags.APPLICATION_USER_ID,
                                applicationUserId.toString()
                            )

                            callSendOTPApi()
                        }
                    } else {
                        buttonRegister.isClickable = true
                        progressBarRegister.hide()
                        toast(response.body()!!.message)
                    }
                } else {
                    buttonRegister.isClickable = true
                    progressBarRegister.hide()
                    toast(response.body()!!.message)
                }

            }

        })
    }

    private fun callSendOTPApi() {
        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!

        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
        val call =
            clientInstance.sendOTPApi(
                textInputEditTextPhoneNumber.text.toString(),
                getString(R.string.send_otp_register_subject)
            )
        call.enqueue(object : Callback<SendOTPResponse> {
            override fun onFailure(call: Call<SendOTPResponse>, t: Throwable) {
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(
                call: Call<SendOTPResponse>,
                response: Response<SendOTPResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.statusCode == "10") {

                        val intent = Intent(this@RegisterActivity, OTPActivity::class.java)
                        intent.putExtra(IntentFlags.REDIRECT_FROM, IntentFlags.FROM_REGISTER)
                        intent.putExtra(
                            IntentFlags.VERIFICATION_PHONE_NUMBER,
                            textInputEditTextPhoneNumber.text.toString()
                        )
                        startActivity(intent)
                        textInputLayoutPassword.clearFocus()
                        textInputLayoutEmailId.clearFocus()
                        textInputLayoutPhoneNumber.clearFocus()
                        textInputLayoutName.clearFocus()
                        textInputEditTextName.text?.clear()
                        textInputEditTextPhoneNumber.text?.clear()
                        textInputEditTextEmailId.text?.clear()
                        textInputEditTextPassword.text?.clear()

                        Glide.with(this@RegisterActivity).load(R.drawable.kmm_profile)
                            .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.kmm_profile))
                            .into(imageViewLogo)
                    /*    imageViewCamera.show()
                        textViewAddPhoto.show()
*/
                        textViewNameError.invisible()
                        textViewEmailIdError.invisible()
                        textViewMobileNoError.invisible()
                        textViewPasswordError.invisible()
                    } else {
                        buttonRegister.isClickable = true
                        progressBarRegister.hide()
                        toast(response.body()!!.message)
                    }
                }
            }

        })
    }

    private fun textChangeListeners() {

        textInputEditTextName.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                nameValidation()
            }
        }

        textInputEditTextEmailId.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                emailValidation()
            }
        }

        textInputEditTextPhoneNumber.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                contactValidation()
            }
        }

        textInputEditTextPassword.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                this.hideKeyboard(view)
                passwordValidation()
            }
        }

        textInputEditTextName.onTextChanged {
            textViewNameError.invisible()
        }

        textInputEditTextEmailId.onTextChanged {
            textViewEmailIdError.invisible()
        }

        textInputEditTextPhoneNumber.onTextChanged {
            textViewMobileNoError.invisible()
        }

    }

    private fun nameValidation(): Boolean {
        when {
            textInputEditTextName.isEmpty() -> {
                textViewNameError.show()
                textViewNameError.text = getString(R.string.validation_enter_name)
                return false
            }
            textInputEditTextName.text.toString().startsWith(" ") -> {
                textViewNameError.show()
                textViewNameError.text = getString(R.string.validation_empty_space)
                return false
            }
            textInputEditTextName.length() > MAX_NAME_LENGTH -> {
                textViewNameError.show()
                textViewNameError.text = getString(R.string.validation_name_length)
                return false
            }
        }
        textViewNameError.invisible()
        return true
    }

    private fun emailValidation(): Boolean {

        when {
            textInputEditTextEmailId.isEmpty() -> {
                textViewEmailIdError.show()
                textViewEmailIdError.text = getString(R.string.validation_enter_email)
                return false
            }
            !textInputEditTextEmailId.isValidEmail() -> {
                textViewEmailIdError.show()
                textViewEmailIdError.text = getString(R.string.validation_email)
                return false
            }

        }
        textViewEmailIdError.invisible()
        return true
    }

    private fun contactValidation(): Boolean {

        when {
            textInputEditTextPhoneNumber.isEmpty() -> {

                textViewMobileNoError.show()
                textViewMobileNoError.text = getString(R.string.validation_enter_number)

                return false
            }
            !textInputEditTextPhoneNumber.isValidMobileNumber() -> {

                textViewMobileNoError.show()
                textViewMobileNoError.text = getString(R.string.validation_number)


                return false
            }

        }
        textViewMobileNoError.invisible()

        return true
    }


    private fun passwordValidation(): Boolean {
        when {
            textInputEditTextPassword.isEmpty() -> {
                textViewPasswordError.show()
                textViewPasswordError.text = getString(R.string.validation_enter_password)
                return false
            }
            textInputEditTextPassword.text.toString().startsWith(" ") -> {
                textViewPasswordError.show()
                textViewPasswordError.text = getString(R.string.validation_empty_space)
                return false
            }
            textInputEditTextPassword.length() < 6 -> {
                textViewPasswordError.show()
                textViewPasswordError.text = getString(R.string.validation_password_length)
                return false
            }

        }
        textViewPasswordError.invisible()
        return true
    }
}
