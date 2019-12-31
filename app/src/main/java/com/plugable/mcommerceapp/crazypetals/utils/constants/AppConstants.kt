package com.plugable.mcommerceapp.crazypetals.utils.constants


/**
 * [WebApi] is a constant used for loading URL paths
 */
object WebApi {

    const val BASE_URL_API = "PlugsApiConfig.json"

    //App theme
    const val APP_THEME = "/api/category/getapptheme"

    //category Api
    const val ALL_CATEGORY_API = "/api/category/getallcategories"

    //banner Api
    const val BANNER_API = "/api/category/getallbanners"

    //app update check api
    const val VERSION_INFO_API = "/api/Category/GetVersionInfo"

    //product list api
    const val ALL_PRODUCT_LIST_API_WITH_CATEGORY = "/api/category/getallproductforcategory"

    //exclusive products api
    const val EXCLUSIVE_PRODUCTS = "api/category/GetAllExclusiveProductForCategory"

    //Product detail api
    const val PRODUCT_DETAILS_API = "/api/category/getproductdetails"


    //    Search Product
    const val GLOBAL_SEARCH_PRODUCT_API = "/api/category/getallproductsforsearch"

    //Also Recommended
    const val ALSO_RECOMMENDED_API = "/api/category/getrecommendedproduct"

    //Get Filters
    const val GET_FILTERS_API = "/api/category/getallfilters"

    //Apply Filters
    const val APPLY_FILTERS_API = "/api/category/applyallfilters"

    //RequestBooking
    const val REQUEST_CUSTOMER = "api/Category/BookOrder"

    const val DELIVER_ADDRESS = "api/Category/GetDeliveryLocations"

    //Login
    const val LOGIN = "/api/account/login"

    //RegisterWithData with data
    const val REGISTER_WITH_DATA = "/api/account/RegisterWithData"

    //RegisterWithData with image
    const val REGISTER_WITH_IMAGE = "/api/account/RegisterWithImage"


    // Update Profile
    const val UPDATE_PROFILE = "api/Account/UpdateProfile"

    //Email Verification
    const val SEND_OTP = "/api/account/sendotp"

    //OTP Verification
    const val OTP_VERIFICATION = "/api/account/verifyotp"

    //Reset Password
    const val NEW_PASSWORD = "api/Account/ResetPassword"

    //Get Cart
    const val GET_CART = "api/Checkout/GetCartList"

    //Get Total price
    const val GET_TOTAL_PRICE = "api/Checkout/GetTotalPrice"

    //Add to Cart
    const val ADD_TO_CART = "/api/checkout/addtocart"

    //Update quantity of product
    const val INC_DEC_QUANTITY = "api/Checkout/IncDecProductQuantity"

    const val GET_PROFILE = "/api/account/getmyprofile"

    const val ADD_TOKEN = "/api/checkout/addtoken"

    const val GET_ALL_NOTIFICATIONS = "/api/category/getallnotifications"

    const val GET_MY_ORDERS = "api/Checkout/GetMyOrders"

    const val GET_ORDER_DETAIL = "/api/checkout/getorderdetails"


    //get cart product api
    const val CONNECT_TIMEOUT = 1L
    const val READ_TIMEOUT = 45L
    const val WRITE_TIMEOUT = 30L

    const val APP_ID = "CrazyPetals"
}

/**
 * [IntentFlags] is a constant used for loading intent keys
 */
object IntentFlags {

    const val PARCELABLE_OBJECT = "parcelable_list"
    const val ITEM_POSITION = "position"

    const val CATEGORY_ID = "categoryId"
    const val CATEGORY_NAME = "categoryName"

    const val APPLICATION_USER_ID = ""
    const val APPOINTMENT_ID = ""
    const val ORDER_ID = ""
    const val DeliveryAddressName = ""
    const val TOTAL_PRICE = ""

    const val PRODUCT_MODEL = "productModel"

    const val REDIRECT_FROM = "redirectFrom"
    const val FROM_REGISTER = "RegisterWithData"
    const val FROM_LOGIN = "Login"
    const val FROM_FORGOT_PASSWORD = "ForgotPassword"
    const val VERIFICATION_EMAIL_ADDRESS = "VerificationEmailAddress"
    const val VERIFICATION_PHONE_NUMBER = "VerificationPhoneNumber"
    const val VERIFY_EMAIL_ADDRESS = "VerifyEmailAddress"
    const val VERIFY_PHONE_NUMBER = "VerifyPhoneNumber"

    const val HOME_FRAGMENT="HomeFragment"
    const val PRODUCT_LIST="ProductList"
    const val PRODUCT_DETAIL = "ProductDetail"
    const val WISHLIST = "Wishlist"
    const val ORDER_DETAIL = "OrderDetail"
    const val APPOINTMENT_LIST = "AppointmentList"
    const val PRODUCT_ID = "ProductId"
    const val SHOULD_GO_TO_DASHBOARD = "should.go.to.dashboard"
    const val FRAGMENT_TO_BE_LOADED = "fragment.to.be.loaded"
    const val VERSION_NUMBER = "VersionNumber"

    const val MIXPANEL_PRODUCT_NAME = "Product Name"
    const val MIXPANEL_VISITED_PRODUCTS = "Visited Product"
    const val MIXPANEL_PRODUCT_ID = "Product Id"
    const val MIXPANEL_KEYWORD = "Keyword"
    const val MIXPANEL_SEARCHED_KEYWORD = "Searched Keywords"
    const val MIXPANEL_AREA_PINCODE = "Area"//need to revise//
    const val MIXPANEL_SUCCESSFULLY_PLACED_ORDER = "Successfully Placed  Order"//need to revise//
    const val MIXPANEL_FROM = "From "//need to revise//
    const val MIXPANEL_PRODUCT_DETAIL = "Product Detail"
    const val MIXPANEL_HAMBURGER_MENU = "Hamburger Menu"
    const val MIXPANEL_MOBILE_NUMBER = "Mobile Number"
    const val MIXPANEL_EMAIL_ADDRESS = "Email Address"
    const val MIXPANEL_ORDERED_PRODUCTS = "Ordered Items"
    const val MIXPANEL_VISITED_DASHBOARD = "Visited Dashboard"
    const val MIXPANEL_VISITED_CATEGORY_AND_SUBCATEGORY = "Visited Category&Subcategory"
    const val MIXPANEL_VISITED_PRODUCT_LIST = "Visited Product list"
    const val MIXPANEL_VISITED_WISH_LIST = "Visited Wish list"
    const val MIXPANEL_VISITED_REQUEST_BOOKING_SCREEN_FROM_HAMBURGER_MENU =
        "Visited Request Booking screen from Hamburger Menu "
    const val MIXPANEL_VISITED_REQUEST_BOOKING_SCREEN_FROM_PRODUCT_DETAIL =
        "Visited Request Booking screen from Product Detail "
    const val MIXPANEL_VISITED_BOOKING_SUCCESSFUL_SCREEN = "Visited Booking Successful screen"
    const val MIXPANEL_VISITED_ABOUT_US_SCREEN = "Visited About Us screen"
    const val MIXPANEL_VISITED_FAQS_SCREEN = "Visited FAQs screen"
    const val MIXPANEL_VISITED_CONTACT_US_SCREEN = "Visited Contact Us screen"
//    const val MIXPANEL_LOCATION = "Location"
//    const val MIXPANEL_AREA_NAME = "Area Name"
//    const val MIXPANEL_ZIP_CODE = "Zip Code"

}

/**
 * [SharedPreferences] is a constant used for shared preferences keys
 */
object SharedPreferences {
    const val FILE_NAME = "Shared_preference"
    const val APPLICATION_THEME_OBJECT = "app_theme"
    const val HOST_URL = "host_url"
    const val IS_USER_SKIPPED_LOGIN = "isUserSkipped"
    const val IS_USER_LOGGED_IN = "isUserLoggedIn"
    //For Hostname url
    const val HOST_FILE_NAME = "Shared_preference_host_name"
    const val HOST_NAME_FETCH_MODEL = "host_name_fetch_model"
    const val SHARED_PREFERENCES_LAST_NOTIFICATION_TIME = "last_notification_time"
    const val SHARED_PREFERENCES_NOTIFICATION_COUNT = "notification_count"
    const val SHARED_PREFERENCES_CART_COUNT = "cart_count"
    const val SHARED_PREFERENCES_CART_DATA = "cart_data"
    const val PROFILE = "user.profile"

    var cartItemList=HashSet<String>()
}

