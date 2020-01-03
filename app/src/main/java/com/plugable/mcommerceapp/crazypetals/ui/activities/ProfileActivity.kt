package com.plugable.mcommerceapp.crazypetals.ui.activities

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
import android.graphics.BitmapFactory
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
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.RegisterWithImage
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.UpdateProfile
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.UpdateProfileData
import com.plugable.mcommerceapp.crazypetals.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.crazypetals.utils.application.App
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags
import com.plugable.mcommerceapp.crazypetals.utils.extension.*
import com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.crazypetals.utils.util.isNetworkAccessible
import com.plugable.mcommerceapp.crazypetals.utils.validation.EditTextValidations.MAX_NAME_LENGTH
import com.plugable.mcommerceapp.crazypetals.utils.validation.isEmpty
import com.plugable.mcommerceapp.crazypetals.utils.validation.isValidEmail
import com.plugable.mcommerceapp.crazypetals.utils.validation.isValidMobileNumber
import com.plugable.mcommerceapp.crazypetals.utils.validation.onTextChanged
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
import java.io.FileNotFoundException
import java.io.InputStream

class ProfileActivity : BaseActivity() {

    private lateinit var registerWithImageApi: Call<RegisterWithImage>
    private lateinit var updateProfileApi: Call<UpdateProfile>
    private var bitmapImage: Bitmap?=null
    private var imageStream: InputStream?=null
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
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.cp_profile))
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
//            imageData = data?.data
            if (extras != null) {       //From camera
                if (extras!!.get("data") != null) {
                    imageBitmap = extras?.get("data") as Bitmap
//                imageViewProfile.setImageBitmap(imageBitmap)
                    Glide.with(this).load(imageBitmap)
                        .apply(
                            RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.cp_profile)
                        )
                        .into(imageViewLogo)
                    isProfilePictureChanged = true
                    /*     imageViewCamera.hide()
                    textViewAddPhoto.hide()*/
//                progressBarProfile.show()
//                callRegisterApiWithImage(getImageUri(this, imageBitmap))
                } else {
                    toast("Error while loading image")
                }
            } else {
                if (data?.data != null) {// From Gallery
                    imageData = data.data
                    if (imageData != null) {
//                imageViewProfile.setImageURI(imageData)
                        try {
                            imageStream = contentResolver.openInputStream(
                                imageData!!
                            )
                        } catch (e: FileNotFoundException) {

                        }
                        bitmapImage = BitmapFactory.decodeStream(imageStream)

                        Glide.with(this).load(bitmapImage)
                            .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.cp_profile))
                            .into(imageViewLogo)
                        /*  imageViewCamera.hide()
                textViewAddPhoto.hide()*/
                        progressBarProfile.show()
                        isProfilePictureChanged = true
//                callRegisterApiWithImage(imageData)
                    }
                }
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
        registerWithImageApi = clientInstance.registerApiWithImage(body)
        registerWithImageApi.enqueue(object : Callback<RegisterWithImage> {
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
                        showProgress(this@ProfileActivity)

                        attemptApiCall()

                    } else {
                        buttonUpdate.isClickable = true
                        hideProgress(this@ProfileActivity)
                        toast("Failed to update profile,please try again")
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
                        showProgress(this)
                        if (extras != null) {
                            callRegisterApiWithImage(getImageUri(this, imageBitmap))
                        } else if (imageData != null) {
                            callRegisterApiWithImage(getImageUri(this, bitmapImage!!))
                            buttonUpdate.isClickable = false
                            showProgress(this)

                        }
                    } else {
                        attemptApiCall()
                        buttonUpdate.isClickable = false
                        showProgress(this)
                    }
                }
            }
        }
    }

    override fun onResume() {
        buttonUpdate.isClickable = true
        hideProgress(this)
        super.onResume()
    }

    private fun attemptApiCall() {
        if (isNetworkAccessible()) {
            showProgress(this)
            callUpdateProfileApi()
        } else {
            hideProgress(this)
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

        updateProfileApi  = clientInstance.updateProfileWithData(userInfo)

        updateProfileApi .enqueue(object : Callback<UpdateProfile> {
            override fun onFailure(call: Call<UpdateProfile>, t: Throwable) {
                hideProgress(this@ProfileActivity)
                toast(getString(R.string.message_something_went_wrong))
            }

            override fun onResponse(call: Call<UpdateProfile>, response: Response<UpdateProfile>) {
                if (response.isSuccessful) {
                    if (response.body()?.statusCode.equals("10")) {

                        toast(response.body()!!.message)
                        if (isNetworkAccessible()) {
                            buttonUpdate.isClickable = false
                            showProgress(this@ProfileActivity)
                            startActivity<DashboardActivity>()

                        }
                    } else {
                        buttonUpdate.isClickable = true
                        hideProgress(this@ProfileActivity)
                        toast("Failed to update profile,please try again")
                    }
                } else {
                    buttonUpdate.isClickable = true
                    hideProgress(this@ProfileActivity)
                    toast("Failed to update profile,please try again")
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

    override fun onStop() {
        super.onStop()
        cancelTasks()
    }

    private fun showProgress(activity: Activity) {
//        window?.setBackgroundDrawableResource(android.R.color.transparent)
        activity.disableWindowClicks()
        progressBarProfile.show()
    }

    private fun hideProgress(activity: Activity) {
        activity.enableWindowClicks()
        progressBarProfile.hide()
    }

    private fun cancelTasks() {
        if (::updateProfileApi.isInitialized && updateProfileApi != null) updateProfileApi.cancel()
        if (::registerWithImageApi.isInitialized && registerWithImageApi != null) registerWithImageApi.cancel()
    }
}
