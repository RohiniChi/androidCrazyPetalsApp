package com.plugable.mcommerceapp.crazypetals.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.Banners
import kotlinx.android.synthetic.main.layout_help_item_slider.view.*

/**
 * [HelpSliderAdapter] is an adapter to load help image in view pager
 *
 * @property context
 * @property helpDataArrayList
 */
internal class HelpSliderAdapter(private val context: Context, private val helpDataArrayList: ArrayList<Banners.Data.Banner>,
                                 private val onBannerClick: OnBannerClick) :
    androidx.viewpager.widget.PagerAdapter() {

    override fun getCount(): Int {
        return helpDataArrayList.size
    }

    override fun isViewFromObject(view: View, viewObject: Any): Boolean {
        return view === viewObject
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_help_item_slider, container, false)

        val imageModelUrl = helpDataArrayList[position]

        val image=imageModelUrl.image
        Glide.with(context)
            .load(image)
            .placeholder(R.drawable.ic_placeholder_category)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fallback(R.drawable.ic_placeholder_category)
            .apply(RequestOptions()).centerInside()
            .error(R.drawable.ic_placeholder_category)
            .into(view.imageViewHelp)

        view.imageViewHelp.setOnClickListener {
            onBannerClick.onBannerClick(position)
        }

        val viewPager = container as androidx.viewpager.widget.ViewPager
        viewPager.addView(view, 0)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, viewObject: Any) {
        val viewPager = container as androidx.viewpager.widget.ViewPager
        val view = viewObject as View
        viewPager.removeView(view)
    }

    public interface OnBannerClick {
        fun onBannerClick(position: Int)
    }

}
