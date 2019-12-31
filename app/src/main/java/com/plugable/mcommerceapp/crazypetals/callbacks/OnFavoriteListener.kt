package com.plugable.mcommerceapp.crazypetals.callbacks

/**
 * [OnFavoriteListener] is a callback for favorite item clicks
 *
 */
interface OnFavoriteListener {
    fun onFavoriteClicked(clickedIndex: Int, isFavorite: Boolean)
}