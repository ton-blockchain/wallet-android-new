package org.ton.wallet.data.core.link

interface LinkActionHandler {

    fun processLinkAction(action: LinkAction)
}