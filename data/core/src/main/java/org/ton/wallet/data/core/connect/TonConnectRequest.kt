package org.ton.wallet.data.core.connect

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TonConnectRequest(
    val manifestUrl: String,
    val items: List<ConnectItem>
) : Parcelable {

    @Parcelize
    sealed class ConnectItem(val name: String) : Parcelable {

        @Parcelize
        data object TonAddressItem : ConnectItem(TonConnect.ConnectItemNameTonAddress)
    }
}