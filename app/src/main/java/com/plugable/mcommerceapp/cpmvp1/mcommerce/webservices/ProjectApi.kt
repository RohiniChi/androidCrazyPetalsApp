package com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices


import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.*
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.ADD_TOKEN
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.ADD_TO_CART
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.ALL_CATEGORY_API
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.ALL_PRODUCT_LIST_API_WITH_CATEGORY
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.ALSO_RECOMMENDED_API
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.APPLY_FILTERS_API
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.APP_THEME
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.BANNER_API
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.BASE_URL_API
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.DELIVER_ADDRESS
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.GET_ALL_NOTIFICATIONS
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.GET_CART
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.GET_FILTERS_API
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.GET_MY_ORDERS
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.GET_ORDER_DETAIL
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.GET_PROFILE
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.GET_TOTAL_PRICE
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.GLOBAL_SEARCH_PRODUCT_API
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.INC_DEC_QUANTITY
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.LOGIN
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.NEW_PASSWORD
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.OTP_VERIFICATION
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.PRODUCT_DETAILS_API
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.REGISTER_WITH_DATA
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.REGISTER_WITH_IMAGE
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.REQUEST_CUSTOMER
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.SEND_OTP
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.UPDATE_PROFILE
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi.VERSION_INFO_API
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*


/**
 * [ProjectApi] is an interface to do api call operations
 *
 */
interface ProjectApi {


    @GET(BASE_URL_API)
    fun getHostname(): Call<Host>

    @get:GET(APP_THEME)
    val myJSON: Call<ApplicationTheme>

    @GET(VERSION_INFO_API)
    fun getVersionInfo(): Call<VersionInfo>


    @GET(ALL_CATEGORY_API)
    fun getCategories(
        @Query("take") takeSize: Int,
        @Query("skip") skip: Int
    ): Call<Categories>

    @GET(BANNER_API)
    fun getBanners(
        @Query("take") takeSize: Int,
        @Query("skip") skip: Int
    ): Call<Banners>

    @GET(ALL_PRODUCT_LIST_API_WITH_CATEGORY)
    fun productModel(
        @Query("skip") skipCount: Int, @Query("take") takeCount: Int,
        @Query("categoryId") categoryId: Int
    ): Call<Products>


    @GET(GET_FILTERS_API)
    fun getFilters(
        @Query("categoryId") categoryId: Int
    ): Call<GetFilters>

    @POST(APPLY_FILTERS_API)
    fun applyFilters(
        @Body applyFilter: FilterData
    ): Call<Products>


    @GET(ALSO_RECOMMENDED_API)
    fun recommendedProducts(
        @Query("skip") skipCount: Int,
        @Query("take") takeCount: Int,
        @Query("categoryId") categoryId: Int,
        @Query("ProductId") productId: Int
    ): Call<Products>


    @GET(PRODUCT_DETAILS_API)
    fun productDetailModel(
        @Query("id") productId: Int
    ): Call<ProductDetail>


    @GET(GLOBAL_SEARCH_PRODUCT_API)
    fun globalSearchApi(
        @Query("take") take: Int,
        @Query("search") search: String,
        @Query("skip") skip: Int

    ): Call<Products>


    @POST(REQUEST_CUSTOMER)
    fun saveCustomerData(
        @Body bookData: ProductBooking.Data
    ): Call<ProductBooking>

    @GET(DELIVER_ADDRESS)
    fun getDeliveryAddress(): Call<DeliveryScheduleAddress>

    @POST(LOGIN)
    fun loginApi(
        @Body userLogin: LoginRequest
    ): Call<Login>


    @POST(REGISTER_WITH_DATA)
    fun registerApiWithData(
        @Body saveUserInfo: RegisterRequest
    ): Call<RegisterWithData>

    @Multipart
    @POST(REGISTER_WITH_IMAGE)
    fun registerApiWithImage(
        @Part image: MultipartBody.Part
    ): Call<RegisterWithImage>

    @POST(UPDATE_PROFILE)
    fun updateProfileWithData(
        @Body saveUserInfo: UpdateProfileData
    ): Call<UpdateProfile>


    @GET(SEND_OTP)
    fun sendOTPApi(
        @Query("phoneNumber") phoneNumber: String,
        @Query("subject") subject: String
    ): Call<SendOTPResponse>

    @POST(OTP_VERIFICATION)
    fun otpVerificationApi(
        @Body verifyOtp: VerifyOTPRequest
    ): Call<OTPVerification>

    @POST(NEW_PASSWORD)
    fun newPasswordApi(
        @Body resetUserPassword: ResetPassword
    ): Call<NewPassword>

    @POST(ADD_TO_CART)
    fun addToCartApi(
        @Body requestCart: RequestAddToCart
    ): Call<ResponseAddToCart>

    @GET(GET_CART)
    fun getCartApi(
        @Query("ApplicationUserId") applicationUserId: Int

    ): Call<GetCartResponse>

    @GET(GET_TOTAL_PRICE)
    fun getTotalPriceApi(
        @Query("ApplicationUserId") applicationUserId: Int,
        @Query("AppId") appId: String

    ): Call<GetTotalPrice>

    @POST(INC_DEC_QUANTITY)
    fun getUpdatedQuantity(
        @Body updateQuantityRequest: RequestUpdateQuantity
    ): Call<ResponseUpdateQuantity>

    @GET(GET_PROFILE)
    fun getProfileApi(
        @Query("applicationUserId") ApplicationUserId: String
    ): Call<GetMyProfile>

    @POST(ADD_TOKEN)
    fun addToken(
        @Body notificationTokenRequest: NotificationTokenRequest
    ): Call<NotificationToken>

    @GET(GET_ALL_NOTIFICATIONS)
    fun getAllNotificationApi(
        @Query("PageIndex") pageindex: Int,
        @Query("PageSize") pagesize: Int,
        @Query("applicationUserId") ApplicationUserId: String
    ): Call<Notifications>

    @GET(GET_MY_ORDERS)
    fun getMyOrders(
        @Query("ApplicationUserId") userId: String
    ): Call<MyOrder>

    @GET(GET_ORDER_DETAIL)
    fun getOrderDetails(
        @Query("OrderId") orderId: String
    ): Call<OrderDetailResponse>

    @POST("/api/checkout/addnewaddress")
    fun addAddress(
        @Body addressRequest: AddressRequest
    ): Call<AddressAddResponse>

    @POST("/api/checkout/editaddress")
    fun editAddress(
        @Body addressRequest: AddressRequest
    ): Call<AddressAddResponse>

    @POST("api/Checkout/DeleteAddress")
    fun deleteAddress(
        @Body addressRequest: DeleteAddressRequest
    ): Call<AddressAddResponse>

    @GET("api/Checkout/GetDeliveryDay")
    fun getDeliveryDay(@Query("addressId") addressId: String): Call<DeliveryDayResponse>

    @GET("/api/checkout/getaddresslist")
    fun getAddressList(@Query("applicationUserId") applicationUserId: String): Call<AddressListResponse>

    @POST("/api/checkout/placeorder")
    fun placeOrder(@Body placeOrderRequest: PlaceOrderRequest): Call<PlaceOrderResponse>

    @POST("api/Checkout/RemoveFromCart")
    fun removeFromCart(@Body removeFromCartRequest: RemoveFromCartRequest): Call<ResponseAddToCart>

    @GET("/api/checkout/GetDeliveryChart")
    fun getDeliveryChart(): Call<DeliveryChartResponse>

}