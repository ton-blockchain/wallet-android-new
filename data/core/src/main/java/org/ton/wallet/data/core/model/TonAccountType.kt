package org.ton.wallet.data.core.model

enum class TonAccountType(
    val version: Int,
    val revision: Int
) {
    v3r1(3, 1),
    v3r2(3, 2),
    v4r2(4, 2);

    fun getString(): String {
        return "v${version}R${revision}"
    }

    companion object {

        fun getAccountType(version: Int, revision: Int): TonAccountType {
            return when (version) {
                3 -> if (revision == 1) v3r1 else v3r2
                4 -> v4r2
                else -> throw IllegalArgumentException("Unsupported account version $version")
            }
        }
    }
}