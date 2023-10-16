package org.ton.wallet.data.wallet.api.model

data class AccountDto(
    val id: Int,
    val walletId: Int,
    val address: String,
    val version: Int,
    val revision: Int,
    var balance: Long,
    var lastTransactionId: Long?,
    var lastTransactionHash: ByteArray?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountDto

        if (id != other.id) return false
        if (walletId != other.walletId) return false
        if (address != other.address) return false
        if (version != other.version) return false
        if (revision != other.revision) return false
        if (balance != other.balance) return false
        if (lastTransactionId != other.lastTransactionId) return false
        if (lastTransactionHash != null) {
            if (other.lastTransactionHash == null) return false
            if (!lastTransactionHash.contentEquals(other.lastTransactionHash)) return false
        } else if (other.lastTransactionHash != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + walletId
        result = 31 * result + address.hashCode()
        result = 31 * result + version
        result = 31 * result + revision
        result = 31 * result + balance.hashCode()
        result = 31 * result + (lastTransactionId?.hashCode() ?: 0)
        result = 31 * result + (lastTransactionHash?.contentHashCode() ?: 0)
        return result
    }
}