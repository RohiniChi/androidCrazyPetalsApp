package com.plugable.mcommerceapp.cpmvp1.ui.fragments


import ServiceGenerator
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.callbacks.EventListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnButtonClickListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.OnFavoriteListener
import com.plugable.mcommerceapp.cpmvp1.callbacks.SetOnBottomReachedListener
import com.plugable.mcommerceapp.cpmvp1.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.cpmvp1.mcommerce.db.AppDatabase
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Products
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.RequestAddToCart
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.ResponseAddToCart
import com.plugable.mcommerceapp.cpmvp1.mcommerce.webservices.ProjectApi
import com.plugable.mcommerceapp.cpmvp1.registration.LoginActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.CartActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.DashboardActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.ProductDetailActivity
import com.plugable.mcommerceapp.cpmvp1.ui.activities.SearchActivity
import com.plugable.mcommerceapp.cpmvp1.ui.adapters.ProductListAdapter
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.IntentFlags
import com.plugable.mcommerceapp.cpmvp1.utils.extension.hide
import com.plugable.mcommerceapp.cpmvp1.utils.extension.show
import com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences.SharedPreferences
import com.plugable.mcommerceapp.cpmvp1.utils.util.CountDrawable
import com.plugable.mcommerceapp.cpmvp1.utils.util.isNetworkAccessible
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_products.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*
import kotlinx.android.synthetic.main.layout_network_condition.*
import kotlinx.android.synthetic.main.layout_no_wish_list.*
import kotlinx.android.synthetic.main.layout_server_error_condition.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IndexOutOfBoundsException
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [WishListFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class WishListFragment : BaseFragment(), EventListener, OnFavoriteListener,
    SetOnBottomReachedListener, OnButtonClickListener {

    override fun onBottomReached(position: Int) {
    }

    private lateinit var productListAdapter: ProductListAdapter
    private var productList = ArrayList<Products.Data.ProductDetails>()
//    private lateinit var mixPanel: MixpanelAPI

//    private lateinit var onBottomReachedListener: SetOnBottomReachedListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_products, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bottom_navigation.hide()
        initializeViews()
        initializeTheme()
    }


    private fun initializeTheme() {
        val configDetail = SharedPreferences.getInstance(activity!!).themeDataPreference
        ApplicationThemeUtils.APP_NAME = configDetail!!.appName
        ApplicationThemeUtils.PRIMARY_COLOR = configDetail.primaryColor
        ApplicationThemeUtils.SECONDARY_COLOR = configDetail.secondryColor
        ApplicationThemeUtils.STATUS_BAR_COLOR = configDetail.statusBarColor
        ApplicationThemeUtils.TEXT_COLOR = configDetail.textColor
        ApplicationThemeUtils.CURRENCY_SYMBOL = configDetail.currencySymbol
        setToolBar(ApplicationThemeUtils.APP_NAME)
        setThemeToComponents()
    }

    private fun setThemeToComponents() {
        buttonBrowseMore.setBackgroundColor(Color.parseColor(ApplicationThemeUtils.SECONDARY_COLOR))
    }


    override fun onResume() {
        super.onResume()
        activity!!.invalidateOptionsMenu()

    }

    private fun initializeViews() {
        imgToolbarHomeLayout.setOnClickListener(this)
        buttonBrowseMore.setOnClickListener(this)
        productList =
            AppDatabase.getDatabase(activity!!).productListDao().getAllWishListedProducts() as ArrayList<Products.Data.ProductDetails>

//        onBottomReachedListener=this
        productListAdapter = ProductListAdapter(productList, activity!!, this, this, this)
        val gridLayoutManager = GridLayoutManager(context, 2)
        gridLayoutManager.orientation =
            RecyclerView.VERTICAL // set Horizontal Orientation
        recyclerViewProducts.layoutManager = gridLayoutManager
        recyclerViewProducts.itemAnimator = DefaultItemAnimator()
        recyclerViewProducts.adapter = productListAdapter

        if (productList.isEmpty()) showNoDataAvailableScreen() else showRecyclerViewData()
//        mixPanel = MixpanelAPI.getInstance(context, resources.getString(R.string.mix_panel_token))
//        sendMixPanelEvent()
    }
/*

    private fun sendMixPanelEvent() {
        val productObject = JSONObject()
        productObject.put(IntentFlags.MIXPANEL_PRODUCT_ID, getString(R.string.mixpanel_wishlist))
        mixPanel.track(IntentFlags.MIXPANEL_VISITED_WISH_LIST, productObject)
    }
*/


    override fun setToolBar(name: String) {
        toolbar.hide()

        txtToolbarTitle.text = ""
        imgToolbarHome.setImageResource(android.R.color.transparent)
    }

    override fun showNetworkCondition() {
        layoutNetworkCondition.show()
        layout_no_wishlist.hide()
        layoutServerError.hide()
        recyclerViewProducts.hide()
        filter_layout.hide()


        stopShimmerView()

    }

    override fun hideNetworkCondition() {
        layoutNetworkCondition.hide()
        layout_no_wishlist.hide()
        layoutServerError.hide()
        recyclerViewProducts.hide()
        filter_layout.hide()

        stopShimmerView()

    }

    override fun showServerErrorMessage() {
        layoutNetworkCondition.hide()
        layout_no_wishlist.hide()
        layoutServerError.show()
        recyclerViewProducts.hide()
        filter_layout.hide()


        stopShimmerView()

    }

    override fun hideServerErrorMessage() {
        layoutNetworkCondition.hide()
        layout_no_wishlist.hide()
        layoutServerError.hide()
        recyclerViewProducts.hide()
        filter_layout.hide()

        stopShimmerView()

    }

    override fun showRecyclerViewData() {
        layoutNetworkCondition.hide()
        layout_no_wishlist.hide()
        layoutServerError.hide()
        recyclerViewProducts.show()
        filter_layout.hide()
        stopShimmerView()
    }


    override fun showNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layout_no_wishlist.show()
        layoutServerError.hide()
        recyclerViewProducts.hide()
        filter_layout.hide()



        stopShimmerView()

    }

    override fun hideNoDataAvailableScreen() {
        layoutNetworkCondition.hide()
        layout_no_wishlist.hide()
        layoutServerError.hide()
        filter_layout.hide()
        recyclerViewProducts.hide()
        stopShimmerView()
    }

    override fun startShimmerView() {
        shimmerViewContainerProductList.show()
        shimmerViewContainerProductList.startShimmer()

        filter_layout.hide()
        layoutNetworkCondition.hide()
        layoutServerError.hide()
        layout_no_wishlist.hide()
        recyclerViewProducts.hide()
    }

    override fun stopShimmerView() {
        shimmerViewContainerProductList.hide()
        shimmerViewContainerProductList.stopShimmer()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonBrowseMore -> {
                fragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, HomeFragment())
                    ?.commit()
                (activity as DashboardActivity).nav_view?.menu?.getItem(1)?.isChecked = true
                (activity as DashboardActivity).setToolBar(ApplicationThemeUtils.APP_NAME)
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.product_list_menu, menu)

        //Custom cart badge count
        val cartItem = menu.findItem(R.id.action_cart)
        val cartIcon = cartItem.icon as LayerDrawable

        val cartBadge: CountDrawable

        // Reuse drawable if possible
        val reuseIcon = cartIcon.findDrawableByLayerId(R.id.ic_group_count_cart)
        if (reuseIcon != null && reuseIcon is CountDrawable) {
            cartBadge = reuseIcon
        } else {
            cartBadge = CountDrawable(activity!!)
        }

        var cartCountText = SharedPreferences.getInstance(activity!!).getCartCountString()

        if (cartCountText == "10") {
            cartCountText = "9+"
        }
        cartBadge.setCount(cartCountText!!)
        cartIcon.mutate()
        cartIcon.setDrawableByLayerId(R.id.ic_group_count_cart, cartBadge)

        super.onCreateOptionsMenu(menu, inflater)
    }

    object LastClickTimeSingleton {
        var lastClickTime: Long = 0
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                if (context?.isNetworkAccessible()!!) {
                    startActivity<SearchActivity>()
                } else toast(R.string.check_internet_connection)
            }
            R.id.action_cart -> {

                if (SharedPreferences.getInstance(context!!).isUserLoggedIn) {
                    if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return true
                    else {

                        startActivity<CartActivity>(
                            IntentFlags.REDIRECT_FROM to IntentFlags.WISHLIST
                        )
                    }

                    LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()

                } else {
                    startActivity<LoginActivity>(
                        IntentFlags.SHOULD_GO_TO_DASHBOARD to false,
                        IntentFlags.FRAGMENT_TO_BE_LOADED to R.id.nav_wishList
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onItemClickListener(position: Int) {

        if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
        else {
            activity?.startActivity<ProductDetailActivity>(
                IntentFlags.PRODUCT_MODEL to productList[position],
                IntentFlags.REDIRECT_FROM to IntentFlags.WISHLIST
            )
        }
        LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()
    }


    override fun onFavoriteClicked(clickedIndex: Int, isFavorite: Boolean) {
        try {

            AppDatabase.getDatabase(activity!!).productListDao()
                .deleteProduct(productList[clickedIndex])
            productList.remove(productList[clickedIndex])
            with(recyclerViewProducts.adapter!!) {
                notifyItemRemoved(clickedIndex)
                notifyItemRangeChanged(clickedIndex, productList.size)
            }
            if (productList.isEmpty()) showNoDataAvailableScreen()

            toast(getString(R.string.message_item_removed))
        } catch (a: ArrayIndexOutOfBoundsException) {

        }
        catch (e:IndexOutOfBoundsException){

        }
    }

    override fun onButtonClicked(productId: Int) {

        if (context!!.isNetworkAccessible()) {
            if (SharedPreferences.getInstance(activity!!).isUserLoggedIn) {
                val applicationUserId = SharedPreferences.getInstance(activity!!)
                    .getStringValue(IntentFlags.APPLICATION_USER_ID)
                val cartListVariables = RequestAddToCart(
                    ApplicationUserId = applicationUserId!!.toInt(),
                    ProductId = productId,
                    ColorsId = 0,
                    SizeId = 0
                )
                App.HostUrl = SharedPreferences.getInstance(activity!!).hostUrl!!
                val clientInstance = ServiceGenerator.createService(ProjectApi::class.java)
                val call = clientInstance.addToCartApi(cartListVariables)
                call.enqueue(object : Callback<ResponseAddToCart> {
                    override fun onFailure(call: Call<ResponseAddToCart>, t: Throwable) {
                    }

                    override fun onResponse(
                        call: Call<ResponseAddToCart>,
                        response: Response<ResponseAddToCart>
                    ) {
                        if (response.body()?.statusCode.equals("10")) {
//                                toast(getString(R.string.message_product_added_to_cart))
                            toast(getString(R.string.message_product_added_to_cart))
                            productListAdapter.notifyDataSetChanged()
                        } else if (response.body()?.statusCode.equals("30")) {

                            toast(response.body()!!.message)
                            productListAdapter.notifyDataSetChanged()

                        }

                    }
                })

            } else {
                if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
                else {

                    startActivity<LoginActivity>(
                        IntentFlags.SHOULD_GO_TO_DASHBOARD to false,
                        IntentFlags.FRAGMENT_TO_BE_LOADED to R.id.nav_wishList
                    )
                }

                LastClickTimeSingleton.lastClickTime =
                    SystemClock.elapsedRealtime()
            }

        }
    }

    /*override fun onDestroy() {
        mixPanel.flush()
        super.onDestroy()
    }*/

}