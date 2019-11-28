package com.plugable.mcommerceapp.cpmvp1.callbacks

import com.travijuu.numberpicker.library.Enums.ActionEnum

interface OnSubItemClickListener {

    fun onSubItemClicked(
        subItemType: String,
        position: Int,
        actionEnum: ActionEnum = ActionEnum.INCREMENT
    )
}