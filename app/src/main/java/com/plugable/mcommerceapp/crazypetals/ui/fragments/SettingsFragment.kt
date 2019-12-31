package com.plugable.mcommerceapp.crazypetals.ui.fragments



import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.plugable.mcommerceapp.crazypetals.R
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.layout_common_toolbar.*


/**
 * [SettingsFragment] is used to show settings of application
 *
 */
class SettingsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initializeViews()
    }


    private fun initializeViews(){

        textViewAboutUs.setOnClickListener {
//            startActivity<AboutUsActivity>()
        }

        textViewFAQs.setOnClickListener {
//            startActivity<FAQsActivity>()
        }

        textViewContactUs.setOnClickListener {
//            startActivity<ContactUsActivity>()
        }
    }

    fun setToolBar() {


        txtToolbarTitle.text = ""
        imgToolbarHome.setImageResource(android.R.color.transparent)

    }

}


