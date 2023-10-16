package org.ton.wallet.feature.settings.impl.adapter

import org.ton.wallet.lib.lists.diff.DiffUtilItem

interface SettingsUiItem : DiffUtilItem {

    val id: Int
}