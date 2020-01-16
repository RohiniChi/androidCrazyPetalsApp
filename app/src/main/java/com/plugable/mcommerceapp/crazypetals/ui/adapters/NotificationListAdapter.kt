/*
 * Author : Chetan Patil.
 * Module : Notification module
 * Version : V 1.0
 * Sprint : 13
 * Date of Development : 07/10/2019
 * Date of Modified : 07/10/2019
 * Comments : This is adapter to set all the notification to RecyclerView
 * Output : List all the notification
 */

package com.plugable.mcommerceapp.crazypetals.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.callbacks.EventListener
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.Notifications
import kotlinx.android.synthetic.main.row_notification.view.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * [NotificationListAdapter] is an adapter used to load list of notifications in recycler view
 *
 * @property context
 * @property notificationList
 * @property itemClickListener
 */
class NotificationListAdapter(
    private var context: Context,
    private var notificationList: ArrayList<Notifications.Data.NotificationListItem?>,
    var itemClickListener: EventListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        if (viewHolder is MyViewHolder) {

            val timeStamp = notificationList[position]!!.timeStamp
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeStamp!!.toLong() * 1000
            val date = calendar.time
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
            val notificationDate = simpleDateFormat.format(date)

            val now = Calendar.getInstance()
            when {
                now.get(Calendar.DATE) == calendar.get(Calendar.DATE) -> {
                    viewHolder.itemView.notificationDate.text = "Today"
                }
                now.get(Calendar.DATE) - calendar.get(Calendar.DATE) == 1 -> {
                    viewHolder.itemView.notificationDate.text = "Yesterday"
                }
                else -> {
                    viewHolder.itemView.notificationDate.text = notificationDate
                }
            }
//            viewHolder.itemView.txtTitle.text = notificationList[position]!!.title
            viewHolder.itemView.txtTitle.text = notificationList[position]!!.modifiedTitle
            if (notificationList[position]!!.notificationType.equals("Text")) {
                viewHolder.itemView.txtMessage.visibility = View.VISIBLE
                viewHolder.itemView.ivBanner.visibility = View.GONE
                viewHolder.itemView.txtMessage.text = notificationList[position]!!.message
//                viewHolder.itemView.notificationDate.text = notificationDate

            } else {
                viewHolder.itemView.txtMessage.visibility = View.GONE
                viewHolder.itemView.ivBanner.visibility = View.VISIBLE
                Glide.with(context)
                    .load(notificationList[position]!!.imageUrl)
                    .placeholder(R.drawable.ic_placeholder_category)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fallback(R.drawable.ic_placeholder_category).fitCenter()
                    .apply(RequestOptions().fitCenter())
                    .error(R.drawable.ic_placeholder_category)
                    .into(viewHolder.itemView.ivBanner)
//                viewHolder.itemView.notificationDate.text = notificationDate
            }
        } else {

        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        override fun onClick(view: View?) {
            val position = adapterPosition
            if ((notificationList[position]?.categoryId.equals("0")) || (notificationList[position]?.category == null)) {
                return
            } else {
                itemClickListener.onItemClickListener(position)
                return
            }
        }

        init {
            itemView.setOnClickListener(this)
        }

    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        override fun onClick(view: View?) {
            val position = adapterPosition
            if ((notificationList[position]?.categoryId.equals("0")) || (notificationList[position]?.category == null)) {
                return
            } else {
                itemClickListener.onItemClickListener(position)
                return
            }
        }

        init {
            itemView.setOnClickListener(this)
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): RecyclerView.ViewHolder {

        if (position == VIEW_TYPE_ITEM) {
            return MyViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.row_notification,
                    viewGroup,
                    false
                )
            )
        } else {
            return LoadingViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.row_loading,
                    viewGroup,
                    false
                )
            )
        }


    }


    /**
     * The following method decides the type of ViewHolder to display in the RecyclerView
     *
     * @param position
     * @return
     */
    override fun getItemViewType(position: Int): Int {
        return if (notificationList[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}
