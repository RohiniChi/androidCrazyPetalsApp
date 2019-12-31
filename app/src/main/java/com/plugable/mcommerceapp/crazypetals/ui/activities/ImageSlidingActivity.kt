package com.plugable.mcommerceapp.crazypetals.ui.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.apptheme.ApplicationThemeUtils
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.ProductDetail
import com.plugable.mcommerceapp.crazypetals.ui.fragments.ImageFragment
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags.ITEM_POSITION
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags.PARCELABLE_OBJECT
import com.plugable.mcommerceapp.crazypetals.utils.extension.hide
import com.plugable.mcommerceapp.crazypetals.utils.extension.show
import kotlinx.android.synthetic.main.activity_image_sliding.*
import java.util.*

/**
 * [ImageSlidingActivity] is used to show fullscreen images
 *
 */
class ImageSlidingActivity : BaseActivity() {


    private var productImagesArrayList = ArrayList<ProductDetail.Data.Image>()
    private var position: Int = 0

    companion object {
        var NUM_PAGES = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_sliding)
        getDataFromIntent()

    }

    private fun getDataFromIntent() {
        val intent = intent
        productImagesArrayList.clear()
        productImagesArrayList.addAll( intent.getParcelableArrayListExtra(PARCELABLE_OBJECT)!!)
        position = intent.getIntExtra(ITEM_POSITION, 0)

        initializeViews()

    }

    private fun initializeViews() {
        imageViewClose.setOnClickListener(this)
        setToolBar(ApplicationThemeUtils.APP_NAME)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewpager.adapter = ImageSliderAdapter(this, productImagesArrayList,supportFragmentManager)
        tabIndicator.setViewPager(viewpager)
        tabIndicator.setDotIndicatorColor(Color.WHITE)
        tabIndicator.setStrokeDotsIndicatorColor(Color.WHITE)

        viewpager.setCurrentItem(position)

        NUM_PAGES = productImagesArrayList.size

        if (NUM_PAGES == 1) {
            tabIndicator.hide()
        } else {
            tabIndicator.show()
        }

        /* val tab = tabIndicator.getTabAt(position)
         tab?.select()*/


    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imageViewClose ->{

                onBackPressed()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item!!)
    }
}


internal class ImageSliderAdapter(
    private val context: Context,
    private val imageModelArrayList: ArrayList<ProductDetail.Data.Image>,
    manager: FragmentManager
) :
    FragmentPagerAdapter(manager) {
    override fun getItem(position: Int): Fragment {
        val fragment: Fragment
        val bundle = Bundle()
       fragment= ImageFragment()
       bundle.putParcelableArrayList("image",imageModelArrayList)
        bundle.putInt("position",position)
        fragment.arguments=bundle
        return fragment
    }

    override fun getCount(): Int {
        return imageModelArrayList.size
    }


  /*  override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_image_full_screen, container, false)

        val imageModelUrl = imageModelArrayList[position]

        val image=imageModelUrl.image

        Glide.with(context)
            .load(image)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.ic_placeholder_category)
            .fallback(R.drawable.ic_placeholder_category)
            .apply(RequestOptions())
            .error(R.drawable.ic_placeholder_category)
            .into(view.imgFullScreen)

//        Picasso.with(context).load(imageModelUrl).into(view.imgFullScreen)

       *//* val viewPager = container as ViewPager
        viewPager.addView(view, 0)*//*

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, viewObject: Any) {
        val viewPager = container as ViewPager
        val view = viewObject as View
        viewPager.removeView(view)
    }
*/

}
