package com.plugable.mcommerceapp.cpmvp1.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.plugable.mcommerceapp.cpmvp1.R
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.ProductDetail
import kotlinx.android.synthetic.main.layout_image_full_screen.*

class ImageFragment : Fragment() {

    private var imageArrayList= ArrayList<ProductDetail.Data.Image>()
    var position=0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setData()
    }

    private fun setData() {
        imageArrayList=arguments!!.getParcelableArrayList("image")!!
        position=arguments!!.getInt("position")

        Glide.with(context!!)
            .load(imageArrayList[position].image)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.ic_placeholder_category)
            .fallback(R.drawable.ic_placeholder_category)
            .apply(RequestOptions())
            .error(R.drawable.ic_placeholder_category)
            .into(imgFullScreen)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_image_full_screen, container, false)
    }

}
