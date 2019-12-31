package com.plugable.mcommerceapp.crazypetals.ui.adapters

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.ProductDetail
import com.plugable.mcommerceapp.crazypetals.ui.activities.ImageSlidingActivity
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags.ITEM_POSITION
import com.plugable.mcommerceapp.crazypetals.utils.constants.IntentFlags.PARCELABLE_OBJECT
import kotlinx.android.synthetic.main.layout_item_slider.view.*
import kotlin.collections.ArrayList

/**
 * [ImageSlidingActivity] is an adapter used to load array og images in fullscreen
 *
 * @property context
 * @property imageModelArrayList
 */
internal class ImageSliderAdapter(
    private val context: Context,
    private val imageModelArrayList: ArrayList<ProductDetail.Data.Image>
) :
    PagerAdapter() {

    override fun getCount(): Int {
        return imageModelArrayList.size
    }

    override fun isViewFromObject(view: View, viewObject: Any): Boolean {
        return view === viewObject
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_item_slider, container, false)

        val imageModelUrl = imageModelArrayList[position]

        val image=imageModelUrl.image
        Glide.with(context)
            .load(image)
            .placeholder(R.drawable.ic_placeholder_category)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fallback(R.drawable.ic_placeholder_category)
            .error(R.drawable.ic_placeholder_category)
            .into(view.imageViewViewPager)



        view.imageViewViewPager.setOnClickListener {
            if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return@setOnClickListener
            else {
            val intent = Intent(context, ImageSlidingActivity::class.java)
            intent.putParcelableArrayListExtra(PARCELABLE_OBJECT, imageModelArrayList)
            intent.putExtra(ITEM_POSITION, position)
            context.startActivity(intent)
            }
            LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()
        }

        val viewPager = container as ViewPager
        viewPager.addView(view, 0)

        return view
    }

    object LastClickTimeSingleton {
        var lastClickTime: Long = 0
    }

    override fun destroyItem(container: ViewGroup, position: Int, viewObject: Any) {
        val viewPager = container as ViewPager
        val view = viewObject as View
        viewPager.removeView(view)
    }


}