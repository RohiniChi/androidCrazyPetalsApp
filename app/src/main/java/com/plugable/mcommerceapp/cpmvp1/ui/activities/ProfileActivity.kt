package com.plugable.mcommerceapp.cpmvp1.ui.activities

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
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.RegisterWithImage
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.UpdateProfile
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.UpdateProfileData
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hideKeyboard
import com.plugable.mcommerceapp.cpmvp1.utils.extension.invisible
import com.plugable.mcommerceapp.cpmvp1.utils.extension.setStatusBarColor
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.cpmvp1.utils.validation.EditTextValidations.MAX_NAME_LENGTH
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isEmpty
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isValidEmail
import com.plugable.mcommerceapp.cpmvp1.utils.validation.isValidMobileNumber
import com.plugable.mcommerceapp.cpmvp1.utils.validation.onTextChanged
import kotlinx.android.synthetic.main.activity_profile.*
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

class ProfileActivity : BaseActivity() {

    private var userProfile: String? = null
    private lateinit var imageBitmap: Bitmap
    private var imageData: Uri? = null
    private var extras: Bundle? = null
    private var profilePicture: String? = null
    private var myImagePath: String = ""
    private var isProfilePictureChanged = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

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
        setExistingProfileData()
    }

    private fun setExistingProfileData() {
        val profile = SharedPreferences.getInstance(this).getProfile()
        if (profile != null) {
            textInputEditTextEmailId.setText(String.format("%s", profile.emailId))
            textInputEditTextName.setText(String.format("%s", profile.name))
            textInputEditTextPhoneNumber.setText(String.format("%s", profile.mobileNumber))


            profilePicture = App.HostUrl.plus(profile.profilePicture)
            Glide.with(imageViewLogo).load(profilePicture)
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.kmm_profile))
                .into(imageViewLogo)
//            imageViewCamera.hide()
//            textViewAddPhoto.hide()
            isProfilePictureChanged = false
        }/* else {
            imageViewCamera.show()
            textViewAddPhoto.show()
        }*/
    }


    private fun setThemeToComponents() {
        setStatusBarColor()
        buttonUpdate.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))

        progressBarProfile.indeterminateDrawable.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
    }

    private fun initializeViews() {
        textChangeListeners()
        imageViewLogo.setOnClickListener {
            if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return@setOnClickListener
            else {

                checkPermissions(this)
            }
            LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

        }
        buttonUpdate.setOnClickListener(this)

        layoutBackArrowProfile.setOnClickListener(this)
    }

    object LastClickTimeSingleton {
        var lastClickTime: Long = 0
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
            extras = data?.extras
            imageData = data?.data
            if (extras != null) {       //From camera
                imageBitmap = extras?.get("data") as Bitmap
//                imageViewProfile.setImageBitmap(imageBitmap)
                Glide.with(this).load(imageBitmap)
                    .apply(
                        RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.kmm_profile)
                    )
                    .into(imageViewLogo)
                isProfilePictureChanged = true
                /*     imageViewCamera.hide()
                     textViewAddPhoto.hide()*/
//                progressBarProfile.show()
//                callRegisterApiWithImage(getImageUri(this, imageBitmap))
            } else if (imageData != null) { // From Gallery
//                imageViewProfile.setImageURI(imageData)
                Glide.with(this).load(imageData)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.kmm_profile))
                    .into(imageViewLogo)
                /*  imageViewCamera.hide()
                  textViewAddPhoto.hide()*/
                progressBarProfile.show()
                isProfilePictureChanged = true
//                callRegisterApiWithImage(imageData)
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
//                        progressBarProfile.hide()
                        val image = response.body()?.data
                        myImagePath = image!!
                        buttonUpdate.isClickable = false
                        progressBarProfile.show()

                        attemptApiCall()

                    } else {
                        buttonUpdate.isClickable = true
                        progressBarProfile.hide()
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

            R.id.layoutBackArrowProfile -> {

                textViewNameError.invisible()
                textViewEmailIdError.invisible()
                textViewMobileNoError.invisible()

                startActivity<DashboardActivity>()

            }

            R.id.buttonUpdate -> {
                hideKeyboard(v)
                contactValidation()
                emailValidation()
                nameValidation()
                if (nameValidation() && emailValidation() && contactValidation()) {

//                    attemptApiCall()
                    if (isProfilePictureChanged) {
                        if (extras != null) {
                            callRegisterApiWithImage(getImageUri(this, imageBitmap))
                        } else if (imageData != null) {
                            callRegisterApiWithImage(imageData!!)
                            buttonUpdate.isClickable = false
                            progressBarProfile.show()

                        }
                    } else {
                        attemptApiCall()
                        buttonUpdate.isClickable = false
                        progressBarProfile.show()

                    }
                }
            }
        }
    }

    override fun onResume() {
        buttonUpdate.isClickable = true
        progressBarProfile.hide()
        super.onResume()
    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            progressBarProfile.show()
            callUpdateProfileApi()
        } else {
            toast(getString(R.string.oops_no_internet_connection))
        }
    }

    private fun callUpdateProfileApi() {
        val applicationUserId =
            SharedPreferences.getInstance(this).getStringValue(IntentFlags.APPLICATION_USER_ID)
        val profile = SharedPreferences.getInstance(this).getProfile()

        userProfile = profile?.profilePicture
        val userInfo = UpdateProfileData(
            ApplicationUserId = applicationUserId!!,
            Email = textInputEditTextEmailId.text.toString(),
            Image = if (myImagePath.isNullOrEmpty()) userProfile else myImagePath,
            Name = textInputEditTextName.text.toString(),
            PhoneNumber = textInputEditTextPhoneNumber.text.toString()
        )

        App.HostUrl = SharedPreferences.getInstance(this).hostUrl!!

        val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)

        val call = clientInstance.updateProfileWithData(userInfo)

        call.enqueue(object : Callback<UpdateProfile> {
            override fun onFailure(call: Call<UpdateProfile>, t: Throwable) {
                progressBarProfile.hide()
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(call: Call<UpdateProfile>, response: Response<UpdateProfile>) {
                if (response.isSuccessful) {
                    if (response.body()?.statusCode.equals("10")) {

                        toast(response.body()!!.message)
                        if (isNetworkAccessible()) {
                            buttonUpdate.isClickable = false
                            progressBarProfile.show()
                            startActivity<DashboardActivity>()


                        }
                    } else {
                        buttonUpdate.isClickable = true
                        progressBarProfile.hide()
                        toast("Profile is not updated successfully ,please try again")
                    }
                } else {
                    buttonUpdate.isClickable = true
                    progressBarProfile.hide()
                    toast(response.body()!!.message)
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


}
