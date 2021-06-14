package ru.skillbranch.skillarticles.viewmodels

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format

class ArticleViewModel(private val articleId : String) : BaseViewModel<ArticleState>(ArticleState()), IArticleViewModel {

    private val repository = ArticleRepository

    init {
        subscribeOnDataSource(getArticleData()){ article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                shareLink = article.shareLink,
                title = article.title,
                category = article.category,
                categoryIcon = article.categoryIcon,
                date = article.date.format()
            )
        }
            //данные из сети
        subscribeOnDataSource(getArticleContent()) { content, state ->
            content ?: return@subscribeOnDataSource null
            state.copy(
                isLoadingContent = false,
                content = content
            )
        }
            //данные понравившиеся пользователю,хранятся локально
        subscribeOnDataSource(getArticlePersonalInfo()) { info, state ->
            info ?: return@subscribeOnDataSource null
            state.copy(
                isBookmark = info.isBookmark,
                isLike = info.isLike
            )

        }
            //подписка на репозиторий, потому что все данные не нужно модифировать, связаны с LiveData
            //вся модификация происходит на уровне репозитория
        subscribeOnDataSource(repository.getAppSettings()) { settings, state ->
            settings ?: return@subscribeOnDataSource null
            state.copy(
                isDarkMode = settings.isDarkMode,
                isBigText = settings.isBigText
            )
        }
    }

    override fun getArticleContent() : LiveData<List<Any>?> {
        return repository.loadArticleContent(articleId)
    }

    override fun getArticleData() : LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }

    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }
    override fun handleLike() {
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }

        toggleLike()

        val message = if (currentState.isLike) Notify.TextMessage("Mark is liked")
        else {
            Notify.ActionMessage(
                "Don`t like it anymore",
                "No, still like it",
                toggleLike
            )
        }
        notify(message)
    }

    override fun handleBookmark() {
        val personalInfo = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(personalInfo.copy(isBookmark = !personalInfo.isBookmark))
    }

    override fun handleShare() {
        val message = "Share is not implemented"
        notify(Notify.ErrorMessage(message, "OK", null))
    }

    override fun handleToggleMenu() {
        updateState {
            it.copy(isShowMenu = !it.isShowMenu)
        }
    }

    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    override fun handleSearchMode(isSearch: Boolean) {
        TODO("Not yet implemented")
    }

    override fun handleSearch(query: String?) {
        TODO("Not yet implemented")
    }

}

data class ArticleState(
    val isAuth : Boolean = false,
    val isLoadingContent : Boolean = true,
    val isLoadingReviewers : Boolean = true,
    val isLike : Boolean = false,
    val isBookmark : Boolean = false,
    val isShowMenu : Boolean = false,
    val isBigText : Boolean = false,
    val isDarkMode : Boolean = false,
    val isSearch : Boolean = false,
    val searchQuery : String? = null,
    val searchResults : List<Pair<Int, Int>> = emptyList(),
    val searchPosition : Int = 0,
    val shareLink : String? = null,
    val title : String? = null,
    val category : String? = null,
    val categoryIcon : Any? = null,
    val date : String? = null,
    val author : Any? = null,
    val poster : String? = null,
    val content : List<Any> = emptyList(),
    val reviewers : List<Any> = emptyList()
)