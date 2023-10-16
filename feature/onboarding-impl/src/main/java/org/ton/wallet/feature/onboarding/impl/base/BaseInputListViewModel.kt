package org.ton.wallet.feature.onboarding.impl.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.ton.wallet.domain.blockhain.api.GetRecoveryWordsUseCase
import org.ton.wallet.screen.viewmodel.BaseViewModel

abstract class BaseInputListViewModel : BaseViewModel() {

    private val getRecoveryWordsUseCase: GetRecoveryWordsUseCase by inject()

    private var allRecoveryWords = arrayOf<String>()

    private val _suggestWords = MutableStateFlow<List<String>>(emptyList())
    val suggestWordsFlow: StateFlow<List<String>> = _suggestWords

    val enteredWords = Array(getRecoveryWordsUseCase.wordsCount) { "" }

    init {
        viewModelScope.launch(Dispatchers.Default) {
            allRecoveryWords = getRecoveryWordsUseCase.getHints()
        }
    }

    open fun setEnteredWord(position: Int, word: String) {
        enteredWords[position] = word
        viewModelScope.launch(Dispatchers.Default) {
            if (word.isEmpty() || word.length < 2) {
                _suggestWords.tryEmit(emptyList())
            } else {
                val suggestedWords = allRecoveryWords.filter { it.startsWith(word, ignoreCase = true) }
                if (suggestedWords.isEmpty() || suggestedWords[0] == word) {
                    _suggestWords.tryEmit(emptyList())
                } else {
                    _suggestWords.tryEmit(suggestedWords)
                }
            }
        }
    }
}