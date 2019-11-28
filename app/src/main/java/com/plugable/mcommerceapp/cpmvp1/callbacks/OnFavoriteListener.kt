package com.plugable.mcommerceapp.cpmvp1.callbacks

/**
 * [OnFavoriteListener] is a callback for favorite item clicks
 *
 */
interface OnFavoriteListener {
    fun onFavoriteClicked(clickedIndex: Int, isFavorite: Boolean)
}