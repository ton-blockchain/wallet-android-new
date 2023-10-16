package org.ton.wallet.screen

import android.view.*
import androidx.annotation.LayoutRes

abstract class BaseBinding private constructor(val root: View) {

    protected constructor(@LayoutRes layoutRes: Int, inflater: LayoutInflater, container: ViewGroup?) : this(inflater.inflate(layoutRes, container, false))

    protected constructor(@LayoutRes layoutRes: Int, viewGroup: ViewGroup): this(layoutRes, LayoutInflater.from(viewGroup.context), viewGroup)
}